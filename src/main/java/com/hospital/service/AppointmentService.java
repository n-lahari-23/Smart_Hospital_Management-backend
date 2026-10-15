package com.hospital.service;

import com.hospital.dto.*;
import com.hospital.entity.*;
import com.hospital.exception.BadRequestException;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

    private static final ZoneId HOSPITAL_ZONE = ZoneId.of("Asia/Kolkata");

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final DoctorUnavailabilityRepository unavailabilityRepository;
    private final NotificationService notificationService;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              PatientRepository patientRepository,
                              DoctorRepository doctorRepository,
                              PrescriptionRepository prescriptionRepository,
                              DoctorUnavailabilityRepository unavailabilityRepository,
                              NotificationService notificationService) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.unavailabilityRepository = unavailabilityRepository;
        this.notificationService = notificationService;
    }

    public AvailableSlotsResponse getAvailableSlots(Long doctorId, LocalDate date) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + doctorId));

        String doctorName = doctor.getUser() != null ? doctor.getUser().getName() : "Doctor";
        LocalDate today = LocalDate.now(HOSPITAL_ZONE);
        LocalTime nowTime = LocalTime.now(HOSPITAL_ZONE);

        if (date.isBefore(today)) {
            return new AvailableSlotsResponse(doctorId, doctorName, date,
                    date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH),
                    false, "Cannot schedule appointments for past dates", Collections.emptyList());
        }

        String dayOfWeek = date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        String availableDays = doctor.getAvailableDays() != null ? doctor.getAvailableDays() : "";
        if (!availableDays.toLowerCase().contains(dayOfWeek.toLowerCase())) {
            return new AvailableSlotsResponse(doctorId, doctorName, date, dayOfWeek,
                    false, doctorName + " is not available on " + dayOfWeek + "s", Collections.emptyList());
        }

        List<DoctorUnavailability> unavailabilities = unavailabilityRepository.findByDoctorIdAndUnavailableDate(doctorId, date);
        for (DoctorUnavailability u : unavailabilities) {
            if (u.getStartTime() == null && u.getEndTime() == null) {
                String reason = (u.getReason() != null && !u.getReason().isEmpty()) ? u.getReason() : "Scheduled Leave";
                return new AvailableSlotsResponse(doctorId, doctorName, date, dayOfWeek,
                        false, doctorName + " is on leave on this date (" + reason + ")", Collections.emptyList());
            }
        }

        LocalTime start = doctor.getAvailableTimeStart() != null ? doctor.getAvailableTimeStart() : LocalTime.of(9, 0);
        LocalTime end = doctor.getAvailableTimeEnd() != null ? doctor.getAvailableTimeEnd() : LocalTime.of(17, 0);

        List<Appointment> bookedAppointments = appointmentRepository.findByDoctorIdAndAppointmentDateAndStatusNot(doctorId, date, "CANCELLED");
        Set<LocalTime> bookedTimes = bookedAppointments.stream()
                .map(Appointment::getAppointmentTime)
                .collect(Collectors.toSet());

        List<SlotInfo> slotList = new ArrayList<>();
        DateTimeFormatter timeLabelFmt = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);

        LocalTime current = start;
        while (current.isBefore(end)) {
            String timeStr = current.toString();
            if (timeStr.length() == 5) timeStr += ":00";
            String label = current.format(timeLabelFmt);

            boolean isAvailable = true;
            String reason = null;

            if (date.isEqual(today) && current.isBefore(nowTime)) {
                isAvailable = false;
                reason = "Past time";
            }

            if (isAvailable && bookedTimes.contains(current)) {
                isAvailable = false;
                reason = "Booked";
            }

            if (isAvailable) {
                for (DoctorUnavailability u : unavailabilities) {
                    if (u.getStartTime() != null && u.getEndTime() != null) {
                        if (!current.isBefore(u.getStartTime()) && current.isBefore(u.getEndTime())) {
                            isAvailable = false;
                            reason = (u.getReason() != null) ? u.getReason() : "Doctor Unavailable";
                            break;
                        }
                    }
                }
            }

            slotList.add(new SlotInfo(timeStr, label, isAvailable, reason));
            current = current.plusMinutes(30);
        }

        boolean hasAnyAvailable = slotList.stream().anyMatch(SlotInfo::isAvailable);
        String msg = hasAnyAvailable ? "Slots available for booking" : "All slots are fully booked for this date";

        return new AvailableSlotsResponse(doctorId, doctorName, date, dayOfWeek, hasAnyAvailable, msg, slotList);
    }

    @Transactional
    public synchronized AppointmentDTO bookAppointment(Long userId, AppointmentRequest req) {
        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found"));

        Doctor doctor = doctorRepository.findById(req.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + req.getDoctorId()));

        LocalDate today = LocalDate.now(HOSPITAL_ZONE);
        if (req.getAppointmentDate().isBefore(today)) {
            throw new BadRequestException("Appointment date cannot be in the past");
        }

        String dayOfWeek = req.getAppointmentDate().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        if (doctor.getAvailableDays() != null && !doctor.getAvailableDays().toLowerCase().contains(dayOfWeek.toLowerCase())) {
            throw new BadRequestException(doctor.getUser().getName() + " does not practice on " + dayOfWeek + "s");
        }

        List<DoctorUnavailability> unavailabilities = unavailabilityRepository
                .findByDoctorIdAndUnavailableDate(doctor.getId(), req.getAppointmentDate());
        for (DoctorUnavailability u : unavailabilities) {
            if (u.getStartTime() == null && u.getEndTime() == null) {
                throw new BadRequestException("Doctor is on leave on this date: " + u.getReason());
            }
            if (u.getStartTime() != null && u.getEndTime() != null) {
                if (!req.getAppointmentTime().isBefore(u.getStartTime()) && req.getAppointmentTime().isBefore(u.getEndTime())) {
                    throw new BadRequestException("Doctor is unavailable during this time period: " + u.getReason());
                }
            }
        }

        if (appointmentRepository.existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusNot(
                doctor.getId(), req.getAppointmentDate(), req.getAppointmentTime(), "CANCELLED")) {
            throw new BadRequestException("This time slot is already booked for " + doctor.getUser().getName() + ". Please choose another slot.");
        }

        long existingCount = appointmentRepository.countByDoctorIdAndAppointmentDate(doctor.getId(), req.getAppointmentDate());
        int tokenNumber = (int) existingCount + 1;

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setAppointmentDate(req.getAppointmentDate());
        appointment.setAppointmentTime(req.getAppointmentTime());
        appointment.setTokenNumber(tokenNumber);
        appointment.setSymptoms(req.getSymptoms());
        appointment.setStatus("PENDING");

        appointment = appointmentRepository.save(appointment);

        // Notifications
        if (patient.getUser() != null) {
            notificationService.createNotification(
                    patient.getUser(),
                    "Appointment Booked - Token #" + tokenNumber,
                    "Your appointment with " + doctor.getUser().getName() + " on " + req.getAppointmentDate() + " at " + req.getAppointmentTime() + " is confirmed. Token #" + tokenNumber,
                    "BOOKED",
                    appointment.getId()
            );
        }
        if (doctor.getUser() != null) {
            notificationService.createNotification(
                    doctor.getUser(),
                    "New Patient Queued - Token #" + tokenNumber,
                    "Patient " + patient.getUser().getName() + " booked a consultation for " + req.getAppointmentDate() + " at " + req.getAppointmentTime(),
                    "BOOKED",
                    appointment.getId()
            );
        }

        return mapToDTO(appointment);
    }

    @Transactional
    public AppointmentDTO cancelAppointment(Long appointmentId, Long userId, String role) {
        Appointment app = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + appointmentId));

        boolean isAuthorized = "ADMIN".equalsIgnoreCase(role)
                || (app.getPatient() != null && app.getPatient().getUser() != null && app.getPatient().getUser().getId().equals(userId))
                || (app.getDoctor() != null && app.getDoctor().getUser() != null && app.getDoctor().getUser().getId().equals(userId));

        if (!isAuthorized) {
            throw new BadRequestException("Unauthorized to cancel this appointment");
        }

        if ("CANCELLED".equalsIgnoreCase(app.getStatus())) {
            throw new BadRequestException("Appointment is already cancelled");
        }

        if ("COMPLETED".equalsIgnoreCase(app.getStatus())) {
            throw new BadRequestException("Completed appointments cannot be cancelled");
        }

        app.setStatus("CANCELLED");
        app = appointmentRepository.save(app);

        // Notify patient & doctor that slot is freed
        if (app.getPatient() != null && app.getPatient().getUser() != null) {
            notificationService.createNotification(
                    app.getPatient().getUser(),
                    "Appointment Cancelled",
                    "Your appointment with " + (app.getDoctor() != null ? app.getDoctor().getUser().getName() : "Doctor") +
                            " on " + app.getAppointmentDate() + " at " + app.getAppointmentTime() + " has been cancelled.",
                    "CANCELLED",
                    app.getId()
            );
        }
        if (app.getDoctor() != null && app.getDoctor().getUser() != null) {
            notificationService.createNotification(
                    app.getDoctor().getUser(),
                    "Appointment Cancelled",
                    "Appointment for " + (app.getPatient() != null ? app.getPatient().getUser().getName() : "Patient") +
                            " on " + app.getAppointmentDate() + " at " + app.getAppointmentTime() + " was cancelled. Slot is now available.",
                    "CANCELLED",
                    app.getId()
            );
        }

        return mapToDTO(app);
    }

    @Transactional
    public synchronized AppointmentDTO rescheduleAppointment(Long appointmentId, Long userId, String role, RescheduleRequest req) {
        Appointment app = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + appointmentId));

        boolean isAuthorized = "ADMIN".equalsIgnoreCase(role)
                || (app.getPatient() != null && app.getPatient().getUser() != null && app.getPatient().getUser().getId().equals(userId))
                || (app.getDoctor() != null && app.getDoctor().getUser() != null && app.getDoctor().getUser().getId().equals(userId));

        if (!isAuthorized) {
            throw new BadRequestException("Unauthorized to reschedule this appointment");
        }

        if ("CANCELLED".equalsIgnoreCase(app.getStatus()) || "COMPLETED".equalsIgnoreCase(app.getStatus())) {
            throw new BadRequestException("Cannot reschedule " + app.getStatus().toLowerCase() + " appointments");
        }

        Doctor doctor = app.getDoctor();
        LocalDate today = LocalDate.now(HOSPITAL_ZONE);
        if (req.getNewDate().isBefore(today)) {
            throw new BadRequestException("New appointment date cannot be in the past");
        }

        String dayOfWeek = req.getNewDate().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        if (doctor.getAvailableDays() != null && !doctor.getAvailableDays().toLowerCase().contains(dayOfWeek.toLowerCase())) {
            throw new BadRequestException(doctor.getUser().getName() + " does not practice on " + dayOfWeek + "s");
        }

        List<DoctorUnavailability> unavailabilities = unavailabilityRepository
                .findByDoctorIdAndUnavailableDate(doctor.getId(), req.getNewDate());
        for (DoctorUnavailability u : unavailabilities) {
            if (u.getStartTime() == null && u.getEndTime() == null) {
                throw new BadRequestException("Doctor is on leave on this date: " + u.getReason());
            }
            if (u.getStartTime() != null && u.getEndTime() != null) {
                if (!req.getNewTime().isBefore(u.getStartTime()) && req.getNewTime().isBefore(u.getEndTime())) {
                    throw new BadRequestException("Doctor is unavailable during this time: " + u.getReason());
                }
            }
        }

        if (appointmentRepository.existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusNot(
                doctor.getId(), req.getNewDate(), req.getNewTime(), "CANCELLED")) {
            throw new BadRequestException("The selected slot is already booked. Please choose another slot.");
        }

        LocalDate oldDate = app.getAppointmentDate();
        LocalTime oldTime = app.getAppointmentTime();

        app.setAppointmentDate(req.getNewDate());
        app.setAppointmentTime(req.getNewTime());
        app.setStatus("PENDING");

        app = appointmentRepository.save(app);

        // Notifications
        if (app.getPatient() != null && app.getPatient().getUser() != null) {
            notificationService.createNotification(
                    app.getPatient().getUser(),
                    "Appointment Rescheduled",
                    "Your appointment with " + doctor.getUser().getName() + " has been rescheduled from " +
                            oldDate + " " + oldTime + " to " + req.getNewDate() + " at " + req.getNewTime() + ".",
                    "RESCHEDULED",
                    app.getId()
            );
        }
        if (doctor.getUser() != null) {
            notificationService.createNotification(
                    doctor.getUser(),
                    "Appointment Rescheduled",
                    "Appointment with " + (app.getPatient() != null ? app.getPatient().getUser().getName() : "Patient") +
                            " rescheduled to " + req.getNewDate() + " at " + req.getNewTime() + ".",
                    "RESCHEDULED",
                    app.getId()
            );
        }

        return mapToDTO(app);
    }

    public List<AppointmentDTO> getPatientAppointments(Long userId) {
        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));
        return appointmentRepository.findByPatientIdOrderByAppointmentDateDescAppointmentTimeDesc(patient.getId())
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public List<AppointmentDTO> getDoctorAppointments(Long userId, LocalDate date) {
        Doctor doctor = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found"));
        if (date == null) {
            return appointmentRepository.findByDoctorIdOrderByAppointmentDateDescAppointmentTimeAsc(doctor.getId())
                    .stream().map(this::mapToDTO).collect(Collectors.toList());
        } else {
            return appointmentRepository.findByDoctorIdAndAppointmentDateOrderByTokenNumberAsc(doctor.getId(), date)
                    .stream().map(this::mapToDTO).collect(Collectors.toList());
        }
    }

    public List<AppointmentDTO> getAllAppointments() {
        return appointmentRepository.findAllByOrderByAppointmentDateDescAppointmentTimeDesc()
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Transactional
    public AppointmentDTO updateStatus(Long appointmentId, String status) {
        Appointment app = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));
        String cleanStatus = status.toUpperCase();
        app.setStatus(cleanStatus);
        app = appointmentRepository.save(app);

        // Notify patient of status update (e.g. CONFIRMED, COMPLETED)
        if (app.getPatient() != null && app.getPatient().getUser() != null) {
            notificationService.createNotification(
                    app.getPatient().getUser(),
                    "Appointment " + cleanStatus,
                    "Your appointment with " + (app.getDoctor() != null ? app.getDoctor().getUser().getName() : "Doctor") +
                            " on " + app.getAppointmentDate() + " is now " + cleanStatus + ".",
                    "STATUS_UPDATE",
                    app.getId()
            );
        }

        return mapToDTO(app);
    }

    public AppointmentDTO getAppointmentById(Long id) {
        Appointment app = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));
        return mapToDTO(app);
    }

    public AppointmentDTO mapToDTO(Appointment app) {
        AppointmentDTO dto = new AppointmentDTO();
        dto.setId(app.getId());
        if (app.getPatient() != null) {
            dto.setPatientId(app.getPatient().getId());
            if (app.getPatient().getUser() != null) {
                dto.setPatientName(app.getPatient().getUser().getName());
                dto.setPatientPhone(app.getPatient().getUser().getPhone());
            }
            dto.setPatientAge(app.getPatient().getAge());
            dto.setPatientGender(app.getPatient().getGender());
        }
        if (app.getDoctor() != null) {
            dto.setDoctorId(app.getDoctor().getId());
            if (app.getDoctor().getUser() != null) {
                dto.setDoctorName(app.getDoctor().getUser().getName());
            }
            dto.setDoctorSpecialization(app.getDoctor().getSpecialization());
            if (app.getDoctor().getDepartment() != null) {
                dto.setDepartmentName(app.getDoctor().getDepartment().getName());
            }
            dto.setConsultationFee(app.getDoctor().getConsultationFee());
        }
        dto.setAppointmentDate(app.getAppointmentDate());
        dto.setAppointmentTime(app.getAppointmentTime());
        dto.setTokenNumber(app.getTokenNumber());
        dto.setStatus(app.getStatus());
        dto.setSymptoms(app.getSymptoms());

        prescriptionRepository.findByAppointmentId(app.getId())
                .ifPresent(p -> dto.setPrescriptionId(p.getId()));

        return dto;
    }
}

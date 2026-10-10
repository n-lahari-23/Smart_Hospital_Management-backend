package com.hospital.service;

import com.hospital.dto.AppointmentDTO;
import com.hospital.dto.AppointmentRequest;
import com.hospital.entity.Appointment;
import com.hospital.entity.Doctor;
import com.hospital.entity.Patient;
import com.hospital.entity.Prescription;
import com.hospital.exception.BadRequestException;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final PrescriptionRepository prescriptionRepository;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              PatientRepository patientRepository,
                              DoctorRepository doctorRepository,
                              PrescriptionRepository prescriptionRepository) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.prescriptionRepository = prescriptionRepository;
    }

    @Transactional
    public AppointmentDTO bookAppointment(Long userId, AppointmentRequest req) {
        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found"));

        Doctor doctor = doctorRepository.findById(req.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + req.getDoctorId()));

        if (appointmentRepository.existsByDoctorIdAndAppointmentDateAndAppointmentTime(
                doctor.getId(), req.getAppointmentDate(), req.getAppointmentTime())) {
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
        return mapToDTO(appointment);
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
        return appointmentRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public AppointmentDTO updateStatus(Long appointmentId, String status) {
        Appointment app = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));
        app.setStatus(status.toUpperCase());
        app = appointmentRepository.save(app);
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

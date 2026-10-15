package com.hospital.service;

import com.hospital.dto.AppointmentDTO;
import com.hospital.dto.AppointmentRequest;
import com.hospital.dto.AvailableSlotsResponse;
import com.hospital.dto.RescheduleRequest;
import com.hospital.entity.Appointment;
import com.hospital.entity.Doctor;
import com.hospital.entity.Notification;
import com.hospital.entity.Patient;
import com.hospital.entity.User;
import com.hospital.exception.BadRequestException;
import com.hospital.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private PatientRepository patientRepository;
    @Mock
    private DoctorRepository doctorRepository;
    @Mock
    private PrescriptionRepository prescriptionRepository;
    @Mock
    private DoctorUnavailabilityRepository unavailabilityRepository;
    @Mock
    private NotificationRepository notificationRepository;

    private NotificationService notificationService;
    private AppointmentService appointmentService;

    private User doctorUser;
    private User patientUser;
    private Doctor doctor;
    private Patient patient;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(notificationRepository);
        lenient().when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> {
            Notification n = i.getArgument(0);
            n.setId(999L);
            return n;
        });
        appointmentService = new AppointmentService(
                appointmentRepository,
                patientRepository,
                doctorRepository,
                prescriptionRepository,
                unavailabilityRepository,
                notificationService
        );
        doctorUser = new User("Dr. Rajesh", "rajesh@hospital.com", "pass", "DOCTOR", "9876543210");
        doctorUser.setId(10L);

        patientUser = new User("John Doe", "john@gmail.com", "pass", "PATIENT", "9123456789");
        patientUser.setId(20L);

        doctor = new Doctor();
        doctor.setId(1L);
        doctor.setUser(doctorUser);
        doctor.setSpecialization("Cardiology");
        doctor.setConsultationFee(new BigDecimal("800.00"));
        doctor.setAvailableDays("Monday, Tuesday, Wednesday, Thursday, Friday");
        doctor.setAvailableTimeStart(LocalTime.of(9, 0));
        doctor.setAvailableTimeEnd(LocalTime.of(17, 0));

        patient = new Patient();
        patient.setId(1L);
        patient.setUser(patientUser);
    }

    @Test
    void testGetAvailableSlots_DoctorNotFound() {
        when(doctorRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> appointmentService.getAvailableSlots(99L, LocalDate.now().plusDays(1)));
    }

    @Test
    void testGetAvailableSlots_DoctorNotWorkingOnDay() {
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        // Pick a Sunday (e.g. 2026-10-18 is Sunday)
        LocalDate sunday = LocalDate.now().plusDays(1);
        while (sunday.getDayOfWeek() != java.time.DayOfWeek.SUNDAY) {
            sunday = sunday.plusDays(1);
        }

        AvailableSlotsResponse res = appointmentService.getAvailableSlots(1L, sunday);
        assertFalse(res.isAvailable());
        assertTrue(res.getMessage().contains("not available on Sundays"));
    }

    @Test
    void testGetAvailableSlots_WorkingDayWithSlots() {
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        // Pick a Monday in the future
        LocalDate monday = LocalDate.now().plusDays(1);
        while (monday.getDayOfWeek() != java.time.DayOfWeek.MONDAY) {
            monday = monday.plusDays(1);
        }

        when(unavailabilityRepository.findByDoctorIdAndUnavailableDate(1L, monday)).thenReturn(Collections.emptyList());
        when(appointmentRepository.findByDoctorIdAndAppointmentDateAndStatusNot(1L, monday, "CANCELLED")).thenReturn(Collections.emptyList());

        AvailableSlotsResponse res = appointmentService.getAvailableSlots(1L, monday);
        assertTrue(res.isAvailable());
        assertFalse(res.getSlots().isEmpty());
        assertEquals("09:00:00", res.getSlots().get(0).getTime());
        assertTrue(res.getSlots().get(0).isAvailable());
    }

    @Test
    void testBookAppointment_Success() {
        LocalDate nextMonday = LocalDate.now().plusDays(1);
        while (nextMonday.getDayOfWeek() != java.time.DayOfWeek.MONDAY) {
            nextMonday = nextMonday.plusDays(1);
        }

        when(patientRepository.findByUserId(20L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(unavailabilityRepository.findByDoctorIdAndUnavailableDate(1L, nextMonday)).thenReturn(Collections.emptyList());
        when(appointmentRepository.existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusNot(1L, nextMonday, LocalTime.of(10, 0), "CANCELLED")).thenReturn(false);
        when(appointmentRepository.countByDoctorIdAndAppointmentDate(1L, nextMonday)).thenReturn(2L);

        Appointment savedApp = new Appointment();
        savedApp.setId(101L);
        savedApp.setDoctor(doctor);
        savedApp.setPatient(patient);
        savedApp.setAppointmentDate(nextMonday);
        savedApp.setAppointmentTime(LocalTime.of(10, 0));
        savedApp.setTokenNumber(3);
        savedApp.setStatus("PENDING");
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(savedApp);

        AppointmentRequest req = new AppointmentRequest();
        req.setDoctorId(1L);
        req.setAppointmentDate(nextMonday);
        req.setAppointmentTime(LocalTime.of(10, 0));
        req.setSymptoms("Chest pain");

        AppointmentDTO dto = appointmentService.bookAppointment(20L, req);
        assertNotNull(dto);
        assertEquals(101L, dto.getId());
        assertEquals(3, dto.getTokenNumber());
        assertEquals("PENDING", dto.getStatus());

        verify(notificationRepository, times(2)).save(any(Notification.class));
    }

    @Test
    void testBookAppointment_DoubleBookingPrevented() {
        LocalDate nextMonday = LocalDate.now().plusDays(1);
        while (nextMonday.getDayOfWeek() != java.time.DayOfWeek.MONDAY) {
            nextMonday = nextMonday.plusDays(1);
        }

        when(patientRepository.findByUserId(20L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(unavailabilityRepository.findByDoctorIdAndUnavailableDate(1L, nextMonday)).thenReturn(Collections.emptyList());
        when(appointmentRepository.existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusNot(1L, nextMonday, LocalTime.of(10, 0), "CANCELLED")).thenReturn(true);

        AppointmentRequest req = new AppointmentRequest();
        req.setDoctorId(1L);
        req.setAppointmentDate(nextMonday);
        req.setAppointmentTime(LocalTime.of(10, 0));

        assertThrows(BadRequestException.class, () -> appointmentService.bookAppointment(20L, req));
    }

    @Test
    void testCancelAppointment_ReleasesSlot() {
        Appointment app = new Appointment();
        app.setId(50L);
        app.setPatient(patient);
        app.setDoctor(doctor);
        app.setAppointmentDate(LocalDate.now().plusDays(2));
        app.setAppointmentTime(LocalTime.of(11, 0));
        app.setStatus("PENDING");

        when(appointmentRepository.findById(50L)).thenReturn(Optional.of(app));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(i -> i.getArgument(0));

        AppointmentDTO result = appointmentService.cancelAppointment(50L, 20L, "PATIENT");
        assertEquals("CANCELLED", result.getStatus());
        verify(notificationRepository, times(2)).save(any(Notification.class));
    }

    @Test
    void testRescheduleAppointment_Success() {
        LocalDate oldDate = LocalDate.now().plusDays(2);
        LocalDate newMonday = LocalDate.now().plusDays(5);
        while (newMonday.getDayOfWeek() != java.time.DayOfWeek.MONDAY) {
            newMonday = newMonday.plusDays(1);
        }

        Appointment app = new Appointment();
        app.setId(60L);
        app.setPatient(patient);
        app.setDoctor(doctor);
        app.setAppointmentDate(oldDate);
        app.setAppointmentTime(LocalTime.of(10, 0));
        app.setStatus("PENDING");

        when(appointmentRepository.findById(60L)).thenReturn(Optional.of(app));
        when(unavailabilityRepository.findByDoctorIdAndUnavailableDate(1L, newMonday)).thenReturn(Collections.emptyList());
        when(appointmentRepository.existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusNot(1L, newMonday, LocalTime.of(14, 0), "CANCELLED")).thenReturn(false);
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(i -> i.getArgument(0));

        RescheduleRequest req = new RescheduleRequest(newMonday, LocalTime.of(14, 0), "Personal reason");
        AppointmentDTO result = appointmentService.rescheduleAppointment(60L, 20L, "PATIENT", req);

        assertEquals(newMonday, result.getAppointmentDate());
        assertEquals(LocalTime.of(14, 0), result.getAppointmentTime());
        verify(notificationRepository, times(2)).save(any(Notification.class));
    }
}

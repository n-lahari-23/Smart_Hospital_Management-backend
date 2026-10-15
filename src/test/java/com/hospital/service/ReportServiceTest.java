package com.hospital.service;

import com.hospital.dto.AdminAnalyticsDTO;
import com.hospital.dto.DashboardStatsDTO;
import com.hospital.entity.Appointment;
import com.hospital.entity.Department;
import com.hospital.entity.Doctor;
import com.hospital.entity.Patient;
import com.hospital.repository.AppointmentRepository;
import com.hospital.repository.DepartmentRepository;
import com.hospital.repository.DoctorRepository;
import com.hospital.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.StringWriter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReportServiceTest {

    @Mock
    private PatientRepository patientRepository;
    @Mock
    private DoctorRepository doctorRepository;
    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private ReportService reportService;

    @Test
    void testGetAdminStats() {
        when(patientRepository.count()).thenReturn(10L);
        when(doctorRepository.count()).thenReturn(5L);
        when(appointmentRepository.count()).thenReturn(25L);
        when(appointmentRepository.countByAppointmentDate(LocalDate.now())).thenReturn(4L);
        when(appointmentRepository.countByStatus("PENDING")).thenReturn(8L);
        when(appointmentRepository.countByStatus("COMPLETED")).thenReturn(12L);
        when(departmentRepository.count()).thenReturn(6L);

        DashboardStatsDTO stats = reportService.getAdminStats();
        assertEquals(10L, stats.getTotalPatients());
        assertEquals(5L, stats.getTotalDoctors());
        assertEquals(25L, stats.getTotalAppointments());
        assertEquals(6L, stats.getTotalDepartments());
    }

    @Test
    void testGetAdminAnalytics() {
        when(patientRepository.count()).thenReturn(10L);
        when(doctorRepository.count()).thenReturn(5L);
        when(appointmentRepository.count()).thenReturn(25L);
        when(departmentRepository.count()).thenReturn(6L);

        when(appointmentRepository.countByStatus("PENDING")).thenReturn(5L);
        when(appointmentRepository.countByStatus("CONFIRMED")).thenReturn(10L);
        when(appointmentRepository.countByStatus("COMPLETED")).thenReturn(8L);
        when(appointmentRepository.countByStatus("CANCELLED")).thenReturn(2L);

        when(appointmentRepository.findByAppointmentDateBetweenOrderByAppointmentDateAsc(any(), any()))
                .thenReturn(Collections.emptyList());

        Department cardio = new Department("Cardiology", "Heart", "icon");
        cardio.setId(1L);
        when(departmentRepository.findAll()).thenReturn(List.of(cardio));
        when(appointmentRepository.findAll()).thenReturn(Collections.emptyList());
        when(doctorRepository.findByDepartmentId(1L)).thenReturn(Collections.emptyList());

        AdminAnalyticsDTO analytics = reportService.getAdminAnalytics(7);
        assertEquals(10L, analytics.getTotalPatients());
        assertEquals(4, analytics.getStatusBreakdown().size());
        assertEquals(7, analytics.getDailyTrends().size());
        assertEquals(1, analytics.getDepartmentDistribution().size());
        assertEquals("Cardiology", analytics.getDepartmentDistribution().get(0).getDepartmentName());
    }

    @Test
    void testExportAppointmentsCsv() throws Exception {
        Appointment app = new Appointment();
        app.setId(1L);
        app.setTokenNumber(1);
        app.setAppointmentDate(LocalDate.of(2026, 10, 15));
        app.setAppointmentTime(LocalTime.of(10, 0));
        app.setStatus("CONFIRMED");
        app.setSymptoms("Fever");

        when(appointmentRepository.findAllByOrderByAppointmentDateDescAppointmentTimeDesc())
                .thenReturn(List.of(app));

        StringWriter sw = new StringWriter();
        reportService.exportAppointmentsCsv(sw);

        String csv = sw.toString();
        assertTrue(csv.contains("Appointment ID,Token #,Patient Name"));
        assertTrue(csv.contains("CONFIRMED"));
        assertTrue(csv.contains("Fever"));
    }
}

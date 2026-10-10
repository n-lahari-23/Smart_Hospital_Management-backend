package com.hospital.service;

import com.hospital.dto.DashboardStatsDTO;
import com.hospital.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class ReportService {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final DepartmentRepository departmentRepository;

    public ReportService(PatientRepository patientRepository, DoctorRepository doctorRepository,
                          AppointmentRepository appointmentRepository, DepartmentRepository departmentRepository) {
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.departmentRepository = departmentRepository;
    }

    public DashboardStatsDTO getAdminStats() {
        long totalPatients = patientRepository.count();
        long totalDoctors = doctorRepository.count();
        long totalAppointments = appointmentRepository.count();
        long todayAppointments = appointmentRepository.countByAppointmentDate(LocalDate.now());
        long pendingAppointments = appointmentRepository.countByStatus("PENDING");
        long completedAppointments = appointmentRepository.countByStatus("COMPLETED");
        long totalDepartments = departmentRepository.count();

        return new DashboardStatsDTO(totalPatients, totalDoctors, totalAppointments, todayAppointments, pendingAppointments, completedAppointments, totalDepartments);
    }
}

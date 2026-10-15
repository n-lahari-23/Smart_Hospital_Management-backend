package com.hospital.service;

import com.hospital.dto.AdminAnalyticsDTO;
import com.hospital.dto.DashboardStatsDTO;
import com.hospital.entity.Appointment;
import com.hospital.entity.Department;
import com.hospital.repository.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.Writer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final DepartmentRepository departmentRepository;

    public ReportService(PatientRepository patientRepository,
                         DoctorRepository doctorRepository,
                         AppointmentRepository appointmentRepository,
                         DepartmentRepository departmentRepository) {
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

    public AdminAnalyticsDTO getAdminAnalytics(int days) {
        if (days <= 0) days = 14;
        if (days > 90) days = 90;

        AdminAnalyticsDTO analytics = new AdminAnalyticsDTO();
        analytics.setTotalPatients(patientRepository.count());
        analytics.setTotalDoctors(doctorRepository.count());
        analytics.setTotalAppointments(appointmentRepository.count());
        analytics.setTotalDepartments(departmentRepository.count());

        // Status breakdown
        Map<String, Long> statusBreakdown = new LinkedHashMap<>();
        statusBreakdown.put("PENDING", appointmentRepository.countByStatus("PENDING"));
        statusBreakdown.put("CONFIRMED", appointmentRepository.countByStatus("CONFIRMED"));
        statusBreakdown.put("COMPLETED", appointmentRepository.countByStatus("COMPLETED"));
        statusBreakdown.put("CANCELLED", appointmentRepository.countByStatus("CANCELLED"));
        analytics.setStatusBreakdown(statusBreakdown);

        // Daily trends for the last 'days' days
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        List<Appointment> recentAppointments = appointmentRepository
                .findByAppointmentDateBetweenOrderByAppointmentDateAsc(startDate, endDate);

        Map<LocalDate, List<Appointment>> appsByDate = recentAppointments.stream()
                .collect(Collectors.groupingBy(Appointment::getAppointmentDate));

        DateTimeFormatter labelFmt = DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH);
        List<AdminAnalyticsDTO.DailyTrendDTO> dailyTrends = new ArrayList<>();

        for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {
            List<Appointment> dayList = appsByDate.getOrDefault(d, Collections.emptyList());
            long total = dayList.size();
            long completed = dayList.stream().filter(a -> "COMPLETED".equalsIgnoreCase(a.getStatus())).count();
            long cancelled = dayList.stream().filter(a -> "CANCELLED".equalsIgnoreCase(a.getStatus())).count();

            dailyTrends.add(new AdminAnalyticsDTO.DailyTrendDTO(
                    d.toString(),
                    d.format(labelFmt),
                    total,
                    completed,
                    cancelled
            ));
        }
        analytics.setDailyTrends(dailyTrends);

        // Department distribution
        List<Department> departments = departmentRepository.findAll();
        List<Appointment> allAppointments = appointmentRepository.findAll();

        Map<Long, Long> appCountsByDept = new HashMap<>();
        for (Appointment a : allAppointments) {
            if (a.getDoctor() != null && a.getDoctor().getDepartment() != null) {
                Long deptId = a.getDoctor().getDepartment().getId();
                appCountsByDept.put(deptId, appCountsByDept.getOrDefault(deptId, 0L) + 1L);
            }
        }

        List<AdminAnalyticsDTO.DepartmentStatDTO> deptDistribution = new ArrayList<>();
        for (Department dept : departments) {
            long docCount = doctorRepository.findByDepartmentId(dept.getId()).size();
            long appCount = appCountsByDept.getOrDefault(dept.getId(), 0L);
            deptDistribution.add(new AdminAnalyticsDTO.DepartmentStatDTO(
                    dept.getId(),
                    dept.getName(),
                    appCount,
                    docCount
            ));
        }
        analytics.setDepartmentDistribution(deptDistribution);

        return analytics;
    }

    public void exportAppointmentsCsv(Writer writer) throws IOException {
        List<Appointment> appointments = appointmentRepository.findAllByOrderByAppointmentDateDescAppointmentTimeDesc();

        writer.write("Appointment ID,Token #,Patient Name,Patient Phone,Patient Age,Patient Gender,Doctor Name,Department,Date,Time,Fee (INR),Status,Symptoms\n");

        for (Appointment a : appointments) {
            String pName = (a.getPatient() != null && a.getPatient().getUser() != null) ? a.getPatient().getUser().getName() : "N/A";
            String pPhone = (a.getPatient() != null && a.getPatient().getUser() != null) ? a.getPatient().getUser().getPhone() : "N/A";
            String pAge = (a.getPatient() != null && a.getPatient().getAge() != null) ? String.valueOf(a.getPatient().getAge()) : "N/A";
            String pGender = (a.getPatient() != null && a.getPatient().getGender() != null) ? a.getPatient().getGender() : "N/A";

            String dName = (a.getDoctor() != null && a.getDoctor().getUser() != null) ? a.getDoctor().getUser().getName() : "N/A";
            String dDept = (a.getDoctor() != null && a.getDoctor().getDepartment() != null) ? a.getDoctor().getDepartment().getName() : "N/A";
            String fee = (a.getDoctor() != null && a.getDoctor().getConsultationFee() != null) ? a.getDoctor().getConsultationFee().toString() : "0.00";

            String symptoms = a.getSymptoms() != null ? a.getSymptoms().replace("\"", "\"\"").replace("\n", " ") : "";

            writer.write(String.format("%d,%d,\"%s\",\"%s\",%s,\"%s\",\"%s\",\"%s\",%s,%s,%s,\"%s\",\"%s\"\n",
                    a.getId(),
                    a.getTokenNumber(),
                    pName,
                    pPhone,
                    pAge,
                    pGender,
                    dName,
                    dDept,
                    a.getAppointmentDate(),
                    a.getAppointmentTime(),
                    fee,
                    a.getStatus(),
                    symptoms
            ));
        }
        writer.flush();
    }
}

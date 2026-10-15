package com.hospital.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminAnalyticsDTO {
    private long totalPatients;
    private long totalDoctors;
    private long totalAppointments;
    private long totalDepartments;
    private Map<String, Long> statusBreakdown = new HashMap<>();
    private List<DailyTrendDTO> dailyTrends = new ArrayList<>();
    private List<DepartmentStatDTO> departmentDistribution = new ArrayList<>();

    public AdminAnalyticsDTO() {}

    public long getTotalPatients() { return totalPatients; }
    public void setTotalPatients(long totalPatients) { this.totalPatients = totalPatients; }
    public long getTotalDoctors() { return totalDoctors; }
    public void setTotalDoctors(long totalDoctors) { this.totalDoctors = totalDoctors; }
    public long getTotalAppointments() { return totalAppointments; }
    public void setTotalAppointments(long totalAppointments) { this.totalAppointments = totalAppointments; }
    public long getTotalDepartments() { return totalDepartments; }
    public void setTotalDepartments(long totalDepartments) { this.totalDepartments = totalDepartments; }
    public Map<String, Long> getStatusBreakdown() { return statusBreakdown; }
    public void setStatusBreakdown(Map<String, Long> statusBreakdown) { this.statusBreakdown = statusBreakdown; }
    public List<DailyTrendDTO> getDailyTrends() { return dailyTrends; }
    public void setDailyTrends(List<DailyTrendDTO> dailyTrends) { this.dailyTrends = dailyTrends; }
    public List<DepartmentStatDTO> getDepartmentDistribution() { return departmentDistribution; }
    public void setDepartmentDistribution(List<DepartmentStatDTO> departmentDistribution) { this.departmentDistribution = departmentDistribution; }

    public static class DailyTrendDTO {
        private String date; // YYYY-MM-DD
        private String label; // e.g. "15 Oct"
        private long total;
        private long completed;
        private long cancelled;

        public DailyTrendDTO() {}

        public DailyTrendDTO(String date, String label, long total, long completed, long cancelled) {
            this.date = date;
            this.label = label;
            this.total = total;
            this.completed = completed;
            this.cancelled = cancelled;
        }

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        public long getTotal() { return total; }
        public void setTotal(long total) { this.total = total; }
        public long getCompleted() { return completed; }
        public void setCompleted(long completed) { this.completed = completed; }
        public long getCancelled() { return cancelled; }
        public void setCancelled(long cancelled) { this.cancelled = cancelled; }
    }

    public static class DepartmentStatDTO {
        private Long departmentId;
        private String departmentName;
        private long appointmentCount;
        private long doctorCount;

        public DepartmentStatDTO() {}

        public DepartmentStatDTO(Long departmentId, String departmentName, long appointmentCount, long doctorCount) {
            this.departmentId = departmentId;
            this.departmentName = departmentName;
            this.appointmentCount = appointmentCount;
            this.doctorCount = doctorCount;
        }

        public Long getDepartmentId() { return departmentId; }
        public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
        public String getDepartmentName() { return departmentName; }
        public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
        public long getAppointmentCount() { return appointmentCount; }
        public void setAppointmentCount(long appointmentCount) { this.appointmentCount = appointmentCount; }
        public long getDoctorCount() { return doctorCount; }
        public void setDoctorCount(long doctorCount) { this.doctorCount = doctorCount; }
    }
}

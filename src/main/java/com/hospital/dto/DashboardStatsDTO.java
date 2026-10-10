package com.hospital.dto;

public class DashboardStatsDTO {
    private long totalPatients;
    private long totalDoctors;
    private long totalAppointments;
    private long todayAppointments;
    private long pendingAppointments;
    private long completedAppointments;
    private long totalDepartments;

    public DashboardStatsDTO() {}
    public DashboardStatsDTO(long totalPatients, long totalDoctors, long totalAppointments, long todayAppointments, long pendingAppointments, long completedAppointments, long totalDepartments) {
        this.totalPatients = totalPatients;
        this.totalDoctors = totalDoctors;
        this.totalAppointments = totalAppointments;
        this.todayAppointments = todayAppointments;
        this.pendingAppointments = pendingAppointments;
        this.completedAppointments = completedAppointments;
        this.totalDepartments = totalDepartments;
    }

    public long getTotalPatients() { return totalPatients; }
    public void setTotalPatients(long totalPatients) { this.totalPatients = totalPatients; }
    public long getTotalDoctors() { return totalDoctors; }
    public void setTotalDoctors(long totalDoctors) { this.totalDoctors = totalDoctors; }
    public long getTotalAppointments() { return totalAppointments; }
    public void setTotalAppointments(long totalAppointments) { this.totalAppointments = totalAppointments; }
    public long getTodayAppointments() { return todayAppointments; }
    public void setTodayAppointments(long todayAppointments) { this.todayAppointments = todayAppointments; }
    public long getPendingAppointments() { return pendingAppointments; }
    public void setPendingAppointments(long pendingAppointments) { this.pendingAppointments = pendingAppointments; }
    public long getCompletedAppointments() { return completedAppointments; }
    public void setCompletedAppointments(long completedAppointments) { this.completedAppointments = completedAppointments; }
    public long getTotalDepartments() { return totalDepartments; }
    public void setTotalDepartments(long totalDepartments) { this.totalDepartments = totalDepartments; }
}

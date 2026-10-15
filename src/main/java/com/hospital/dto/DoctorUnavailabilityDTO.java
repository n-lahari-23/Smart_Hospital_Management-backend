package com.hospital.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class DoctorUnavailabilityDTO {
    private Long id;
    private Long doctorId;
    private String doctorName;
    private LocalDate unavailableDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String reason;
    private LocalDateTime createdAt;

    public DoctorUnavailabilityDTO() {}

    public DoctorUnavailabilityDTO(Long id, Long doctorId, String doctorName, LocalDate unavailableDate, LocalTime startTime, LocalTime endTime, String reason, LocalDateTime createdAt) {
        this.id = id;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.unavailableDate = unavailableDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.reason = reason;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }
    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
    public LocalDate getUnavailableDate() { return unavailableDate; }
    public void setUnavailableDate(LocalDate unavailableDate) { this.unavailableDate = unavailableDate; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

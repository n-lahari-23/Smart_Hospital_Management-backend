package com.hospital.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class DoctorUnavailabilityRequest {
    private LocalDate unavailableDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String reason;

    public DoctorUnavailabilityRequest() {}

    public LocalDate getUnavailableDate() { return unavailableDate; }
    public void setUnavailableDate(LocalDate unavailableDate) { this.unavailableDate = unavailableDate; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}

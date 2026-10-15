package com.hospital.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class RescheduleRequest {
    private LocalDate newDate;
    private LocalTime newTime;
    private String reason;

    public RescheduleRequest() {}

    public RescheduleRequest(LocalDate newDate, LocalTime newTime, String reason) {
        this.newDate = newDate;
        this.newTime = newTime;
        this.reason = reason;
    }

    public LocalDate getNewDate() { return newDate; }
    public void setNewDate(LocalDate newDate) { this.newDate = newDate; }
    public LocalTime getNewTime() { return newTime; }
    public void setNewTime(LocalTime newTime) { this.newTime = newTime; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}

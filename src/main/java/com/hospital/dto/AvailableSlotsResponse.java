package com.hospital.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AvailableSlotsResponse {
    private Long doctorId;
    private String doctorName;
    private LocalDate date;
    private String dayOfWeek;
    private boolean available;
    private String message;
    private List<SlotInfo> slots = new ArrayList<>();

    public AvailableSlotsResponse() {}

    public AvailableSlotsResponse(Long doctorId, String doctorName, LocalDate date, String dayOfWeek, boolean available, String message, List<SlotInfo> slots) {
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.date = date;
        this.dayOfWeek = dayOfWeek;
        this.available = available;
        this.message = message;
        this.slots = slots;
    }

    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }
    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public List<SlotInfo> getSlots() { return slots; }
    public void setSlots(List<SlotInfo> slots) { this.slots = slots; }
}

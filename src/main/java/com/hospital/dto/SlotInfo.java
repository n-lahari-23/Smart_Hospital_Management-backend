package com.hospital.dto;

public class SlotInfo {
    private String time; // e.g., "09:00:00"
    private String label; // e.g., "09:00 AM"
    private boolean available;
    private String reason; // e.g., "Booked", "Past time", null

    public SlotInfo() {}

    public SlotInfo(String time, String label, boolean available, String reason) {
        this.time = time;
        this.label = label;
        this.available = available;
        this.reason = reason;
    }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}

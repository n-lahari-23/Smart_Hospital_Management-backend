package com.hospital.dto;

import java.time.LocalTime;

public class DoctorScheduleRequest {
    private String availableDays;
    private LocalTime availableTimeStart;
    private LocalTime availableTimeEnd;
    private Integer maxPatientsPerDay;

    public DoctorScheduleRequest() {}

    public String getAvailableDays() { return availableDays; }
    public void setAvailableDays(String availableDays) { this.availableDays = availableDays; }
    public LocalTime getAvailableTimeStart() { return availableTimeStart; }
    public void setAvailableTimeStart(LocalTime availableTimeStart) { this.availableTimeStart = availableTimeStart; }
    public LocalTime getAvailableTimeEnd() { return availableTimeEnd; }
    public void setAvailableTimeEnd(LocalTime availableTimeEnd) { this.availableTimeEnd = availableTimeEnd; }
    public Integer getMaxPatientsPerDay() { return maxPatientsPerDay; }
    public void setMaxPatientsPerDay(Integer maxPatientsPerDay) { this.maxPatientsPerDay = maxPatientsPerDay; }
}

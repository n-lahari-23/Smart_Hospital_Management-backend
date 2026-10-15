package com.hospital.dto;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class DoctorScheduleDTO {
    private Long doctorId;
    private String doctorName;
    private String availableDays;
    private LocalTime availableTimeStart;
    private LocalTime availableTimeEnd;
    private Integer maxPatientsPerDay;
    private List<DoctorUnavailabilityDTO> unavailabilities = new ArrayList<>();

    public DoctorScheduleDTO() {}

    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }
    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
    public String getAvailableDays() { return availableDays; }
    public void setAvailableDays(String availableDays) { this.availableDays = availableDays; }
    public LocalTime getAvailableTimeStart() { return availableTimeStart; }
    public void setAvailableTimeStart(LocalTime availableTimeStart) { this.availableTimeStart = availableTimeStart; }
    public LocalTime getAvailableTimeEnd() { return availableTimeEnd; }
    public void setAvailableTimeEnd(LocalTime availableTimeEnd) { this.availableTimeEnd = availableTimeEnd; }
    public Integer getMaxPatientsPerDay() { return maxPatientsPerDay; }
    public void setMaxPatientsPerDay(Integer maxPatientsPerDay) { this.maxPatientsPerDay = maxPatientsPerDay; }
    public List<DoctorUnavailabilityDTO> getUnavailabilities() { return unavailabilities; }
    public void setUnavailabilities(List<DoctorUnavailabilityDTO> unavailabilities) { this.unavailabilities = unavailabilities; }
}

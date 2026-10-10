package com.hospital.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class AppointmentRequest {
    private Long doctorId;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private String symptoms;

    public AppointmentRequest() {}

    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }
    public LocalDate getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(LocalDate appointmentDate) { this.appointmentDate = appointmentDate; }
    public LocalTime getAppointmentTime() { return appointmentTime; }
    public void setAppointmentTime(LocalTime appointmentTime) { this.appointmentTime = appointmentTime; }
    public String getSymptoms() { return symptoms; }
    public void setSymptoms(String symptoms) { this.symptoms = symptoms; }
}

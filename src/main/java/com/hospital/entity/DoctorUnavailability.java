package com.hospital.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "doctor_unavailability")
public class DoctorUnavailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(name = "unavailable_date", nullable = false)
    private LocalDate unavailableDate;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(length = 255)
    private String reason;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public DoctorUnavailability() {}

    public DoctorUnavailability(Doctor doctor, LocalDate unavailableDate, LocalTime startTime, LocalTime endTime, String reason) {
        this.doctor = doctor;
        this.unavailableDate = unavailableDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.reason = reason;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Doctor getDoctor() { return doctor; }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }
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

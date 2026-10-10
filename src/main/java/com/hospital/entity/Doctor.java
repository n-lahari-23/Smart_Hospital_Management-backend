package com.hospital.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalTime;

@Entity
@Table(name = "doctors")
public class Doctor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false, unique = true)
    private User user;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(nullable = false, length = 100)
    private String specialization;

    private String qualification;

    @Column(name = "experience_years")
    private Integer experienceYears = 0;

    @Column(name = "consultation_fee", nullable = false)
    private BigDecimal consultationFee = new BigDecimal("500.00");

    @Column(name = "available_days")
    private String availableDays = "Monday, Tuesday, Wednesday, Thursday, Friday";

    @Column(name = "available_time_start")
    private LocalTime availableTimeStart = LocalTime.of(9, 0);

    @Column(name = "available_time_end")
    private LocalTime availableTimeEnd = LocalTime.of(17, 0);

    @Column(name = "max_patients_per_day")
    private Integer maxPatientsPerDay = 30;

    public Doctor() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }
    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
    public String getQualification() { return qualification; }
    public void setQualification(String qualification) { this.qualification = qualification; }
    public Integer getExperienceYears() { return experienceYears; }
    public void setExperienceYears(Integer experienceYears) { this.experienceYears = experienceYears; }
    public BigDecimal getConsultationFee() { return consultationFee; }
    public void setConsultationFee(BigDecimal consultationFee) { this.consultationFee = consultationFee; }
    public String getAvailableDays() { return availableDays; }
    public void setAvailableDays(String availableDays) { this.availableDays = availableDays; }
    public LocalTime getAvailableTimeStart() { return availableTimeStart; }
    public void setAvailableTimeStart(LocalTime availableTimeStart) { this.availableTimeStart = availableTimeStart; }
    public LocalTime getAvailableTimeEnd() { return availableTimeEnd; }
    public void setAvailableTimeEnd(LocalTime availableTimeEnd) { this.availableTimeEnd = availableTimeEnd; }
    public Integer getMaxPatientsPerDay() { return maxPatientsPerDay; }
    public void setMaxPatientsPerDay(Integer maxPatientsPerDay) { this.maxPatientsPerDay = maxPatientsPerDay; }
}

package com.hospital.dto;

import java.math.BigDecimal;
import java.time.LocalTime;

public class DoctorDTO {
    private Long id;
    private Long userId;
    private String name;
    private String email;
    private String phone;
    private Long departmentId;
    private String departmentName;
    private String specialization;
    private String qualification;
    private Integer experienceYears;
    private BigDecimal consultationFee;
    private String availableDays;
    private LocalTime availableTimeStart;
    private LocalTime availableTimeEnd;

    public DoctorDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
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
}

package com.hospital.dto;

public class AuthResponse {
    private String token;
    private Long id;
    private String name;
    private String email;
    private String role;
    private Long patientId;
    private Long doctorId;

    public AuthResponse() {}
    public AuthResponse(String token, Long id, String name, String email, String role, Long patientId, Long doctorId) {
        this.token = token;
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.patientId = patientId;
        this.doctorId = doctorId;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }
}

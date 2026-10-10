package com.hospital.service;

import com.hospital.dto.*;
import com.hospital.entity.*;
import com.hospital.exception.BadRequestException;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.*;
import com.hospital.security.JwtUtil;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository, PatientRepository patientRepository,
                       DoctorRepository doctorRepository, DepartmentRepository departmentRepository,
                       PasswordEncoder passwordEncoder, JwtUtil jwtUtil,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new BadRequestException("Email is already registered!");
        }

        User user = new User();
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRole(req.getRole() != null ? req.getRole().toUpperCase() : "PATIENT");
        user.setPhone(req.getPhone());
        user = userRepository.save(user);

        Long patientId = null;
        Long doctorId = null;

        if ("PATIENT".equalsIgnoreCase(user.getRole())) {
            Patient patient = new Patient();
            patient.setUser(user);
            patient.setAge(req.getAge());
            patient.setGender(req.getGender());
            patient.setBloodGroup(req.getBloodGroup());
            patient.setAddress(req.getAddress());
            patient.setEmergencyContact(req.getEmergencyContact());
            patient = patientRepository.save(patient);
            patientId = patient.getId();
        } else if ("DOCTOR".equalsIgnoreCase(user.getRole())) {
            Doctor doctor = new Doctor();
            doctor.setUser(user);
            doctor.setSpecialization(req.getSpecialization() != null ? req.getSpecialization() : "General Practitioner");
            doctor.setQualification(req.getQualification() != null ? req.getQualification() : "MBBS");
            doctor.setExperienceYears(req.getExperienceYears() != null ? req.getExperienceYears() : 0);
            if (req.getConsultationFee() != null) doctor.setConsultationFee(req.getConsultationFee());
            if (req.getAvailableDays() != null) doctor.setAvailableDays(req.getAvailableDays());

            if (req.getDepartmentId() != null) {
                Department dept = departmentRepository.findById(req.getDepartmentId())
                        .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
                doctor.setDepartment(dept);
            }
            doctor = doctorRepository.save(doctor);
            doctorId = doctor.getId();
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getRole(), patientId, doctorId);
    }

    public AuthResponse login(AuthRequest req) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Long patientId = null;
        Long doctorId = null;

        if ("PATIENT".equalsIgnoreCase(user.getRole())) {
            patientId = patientRepository.findByUserId(user.getId()).map(Patient::getId).orElse(null);
        } else if ("DOCTOR".equalsIgnoreCase(user.getRole())) {
            doctorId = doctorRepository.findByUserId(user.getId()).map(Doctor::getId).orElse(null);
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getRole(), patientId, doctorId);
    }

    public User getProfile(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}

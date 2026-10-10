package com.hospital.controller;

import com.hospital.dto.AppointmentDTO;
import com.hospital.dto.PatientDTO;
import com.hospital.entity.Prescription;
import com.hospital.repository.UserRepository;
import com.hospital.service.AppointmentService;
import com.hospital.service.PatientService;
import com.hospital.service.PrescriptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;
    private final AppointmentService appointmentService;
    private final PrescriptionService prescriptionService;
    private final UserRepository userRepository;

    public PatientController(PatientService patientService, AppointmentService appointmentService,
                             PrescriptionService prescriptionService, UserRepository userRepository) {
        this.patientService = patientService;
        this.appointmentService = appointmentService;
        this.prescriptionService = prescriptionService;
        this.userRepository = userRepository;
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<PatientDTO> getProfile(Authentication authentication) {
        Long userId = userRepository.findByEmail(authentication.getName()).orElseThrow().getId();
        return ResponseEntity.ok(patientService.getPatientByUserId(userId));
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<PatientDTO> updateProfile(Authentication authentication, @RequestBody PatientDTO dto) {
        Long userId = userRepository.findByEmail(authentication.getName()).orElseThrow().getId();
        return ResponseEntity.ok(patientService.updatePatientProfile(userId, dto));
    }

    @GetMapping("/my-appointments")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<AppointmentDTO>> getMyAppointments(Authentication authentication) {
        Long userId = userRepository.findByEmail(authentication.getName()).orElseThrow().getId();
        return ResponseEntity.ok(appointmentService.getPatientAppointments(userId));
    }

    @GetMapping("/my-prescriptions")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<Prescription>> getMyPrescriptions(Authentication authentication) {
        Long userId = userRepository.findByEmail(authentication.getName()).orElseThrow().getId();
        return ResponseEntity.ok(prescriptionService.getPatientPrescriptions(userId));
    }
}

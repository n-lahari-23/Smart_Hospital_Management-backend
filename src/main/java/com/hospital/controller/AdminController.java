package com.hospital.controller;

import com.hospital.dto.DashboardStatsDTO;
import com.hospital.dto.PatientDTO;
import com.hospital.entity.User;
import com.hospital.repository.UserRepository;
import com.hospital.service.PatientService;
import com.hospital.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final ReportService reportService;
    private final UserRepository userRepository;
    private final PatientService patientService;

    public AdminController(ReportService reportService, UserRepository userRepository, PatientService patientService) {
        this.reportService = reportService;
        this.userRepository = userRepository;
        this.patientService = patientService;
    }

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> getStats() {
        return ResponseEntity.ok(reportService.getAdminStats());
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @GetMapping("/patients")
    public ResponseEntity<List<PatientDTO>> getAllPatients() {
        return ResponseEntity.ok(patientService.getAllPatients());
    }
}

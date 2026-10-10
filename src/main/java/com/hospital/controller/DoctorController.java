package com.hospital.controller;

import com.hospital.dto.AppointmentDTO;
import com.hospital.dto.DoctorDTO;
import com.hospital.repository.UserRepository;
import com.hospital.service.AppointmentService;
import com.hospital.service.DoctorService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorService doctorService;
    private final AppointmentService appointmentService;
    private final UserRepository userRepository;

    public DoctorController(DoctorService doctorService, AppointmentService appointmentService, UserRepository userRepository) {
        this.doctorService = doctorService;
        this.appointmentService = appointmentService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<DoctorDTO>> getAllDoctors(@RequestParam(required = false) Long departmentId) {
        if (departmentId != null) {
            return ResponseEntity.ok(doctorService.getDoctorsByDepartment(departmentId));
        }
        return ResponseEntity.ok(doctorService.getAllDoctors());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DoctorDTO> getDoctorById(@PathVariable Long id) {
        return ResponseEntity.ok(doctorService.getDoctorById(id));
    }

    @GetMapping("/my-appointments")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<AppointmentDTO>> getMyAppointments(
            Authentication authentication,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Long userId = userRepository.findByEmail(authentication.getName()).orElseThrow().getId();
        return ResponseEntity.ok(appointmentService.getDoctorAppointments(userId, date));
    }

    @GetMapping("/my-queue")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<AppointmentDTO>> getTodayQueue(Authentication authentication) {
        Long userId = userRepository.findByEmail(authentication.getName()).orElseThrow().getId();
        return ResponseEntity.ok(appointmentService.getDoctorAppointments(userId, LocalDate.now()));
    }
}

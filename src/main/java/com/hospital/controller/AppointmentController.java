package com.hospital.controller;

import com.hospital.dto.AppointmentDTO;
import com.hospital.dto.AppointmentRequest;
import com.hospital.dto.AvailableSlotsResponse;
import com.hospital.dto.RescheduleRequest;
import com.hospital.entity.User;
import com.hospital.repository.UserRepository;
import com.hospital.service.AppointmentService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final UserRepository userRepository;

    public AppointmentController(AppointmentService appointmentService, UserRepository userRepository) {
        this.appointmentService = appointmentService;
        this.userRepository = userRepository;
    }

    private User getAuthUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName()).orElseThrow();
    }

    private String getAuthRole(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(a -> a.replace("ROLE_", ""))
                .findFirst().orElse("PATIENT");
    }

    @GetMapping("/available-slots")
    public ResponseEntity<AvailableSlotsResponse> getAvailableSlots(
            @RequestParam Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(appointmentService.getAvailableSlots(doctorId, date));
    }

    @PostMapping("/book")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<AppointmentDTO> bookAppointment(Authentication authentication, @RequestBody AppointmentRequest req) {
        Long userId = getAuthUser(authentication).getId();
        return ResponseEntity.ok(appointmentService.bookAppointment(userId, req));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<AppointmentDTO> cancelAppointment(Authentication authentication, @PathVariable Long id) {
        User user = getAuthUser(authentication);
        String role = getAuthRole(authentication);
        return ResponseEntity.ok(appointmentService.cancelAppointment(id, user.getId(), role));
    }

    @PutMapping("/{id}/reschedule")
    public ResponseEntity<AppointmentDTO> rescheduleAppointment(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody RescheduleRequest req) {
        User user = getAuthUser(authentication);
        String role = getAuthRole(authentication);
        return ResponseEntity.ok(appointmentService.rescheduleAppointment(id, user.getId(), role, req));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppointmentDTO> getAppointmentById(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.getAppointmentById(id));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<AppointmentDTO> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String status = body.get("status");
        return ResponseEntity.ok(appointmentService.updateStatus(id, status));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AppointmentDTO>> getAllAppointments() {
        return ResponseEntity.ok(appointmentService.getAllAppointments());
    }
}

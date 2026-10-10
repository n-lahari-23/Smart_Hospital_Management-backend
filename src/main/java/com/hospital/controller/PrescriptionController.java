package com.hospital.controller;

import com.hospital.dto.PrescriptionRequest;
import com.hospital.entity.Prescription;
import com.hospital.service.PrescriptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/prescriptions")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    public PrescriptionController(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Prescription> createPrescription(@RequestBody PrescriptionRequest req) {
        return ResponseEntity.ok(prescriptionService.createPrescription(req));
    }

    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<Prescription> getPrescriptionByAppointmentId(@PathVariable Long appointmentId) {
        return ResponseEntity.ok(prescriptionService.getPrescriptionByAppointmentId(appointmentId));
    }
}

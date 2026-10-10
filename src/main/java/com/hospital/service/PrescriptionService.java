package com.hospital.service;

import com.hospital.dto.PrescriptionRequest;
import com.hospital.entity.*;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;

    public PrescriptionService(PrescriptionRepository prescriptionRepository,
                               AppointmentRepository appointmentRepository,
                               PatientRepository patientRepository) {
        this.prescriptionRepository = prescriptionRepository;
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
    }

    @Transactional
    public Prescription createPrescription(PrescriptionRequest req) {
        Appointment appointment = appointmentRepository.findById(req.getAppointmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        Prescription prescription = new Prescription();
        prescription.setAppointment(appointment);
        prescription.setDiagnosis(req.getDiagnosis());
        prescription.setConsultationNotes(req.getConsultationNotes());
        prescription.setAdvice(req.getAdvice());
        prescription.setFollowUpDate(req.getFollowUpDate());

        if (req.getItems() != null) {
            for (PrescriptionRequest.ItemDTO itemDTO : req.getItems()) {
                PrescriptionItem item = new PrescriptionItem();
                item.setMedicineName(itemDTO.getMedicineName());
                item.setDosage(itemDTO.getDosage());
                item.setFrequency(itemDTO.getFrequency());
                item.setDuration(itemDTO.getDuration());
                item.setInstructions(itemDTO.getInstructions());
                prescription.addItem(item);
            }
        }

        // Mark appointment as COMPLETED
        appointment.setStatus("COMPLETED");
        appointmentRepository.save(appointment);

        return prescriptionRepository.save(prescription);
    }

    public Prescription getPrescriptionByAppointmentId(Long appointmentId) {
        return prescriptionRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found for appointment: " + appointmentId));
    }

    public List<Prescription> getPatientPrescriptions(Long userId) {
        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found"));
        return prescriptionRepository.findByAppointmentPatientIdOrderByCreatedAtDesc(patient.getId());
    }
}

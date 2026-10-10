package com.hospital.service;

import com.hospital.dto.PatientDTO;
import com.hospital.entity.Patient;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PatientService {
    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public PatientDTO getPatientByUserId(Long userId) {
        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found for user: " + userId));
        return mapToDTO(patient);
    }

    public PatientDTO updatePatientProfile(Long userId, PatientDTO updatedDTO) {
        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found"));

        if (updatedDTO.getAge() != null) patient.setAge(updatedDTO.getAge());
        if (updatedDTO.getGender() != null) patient.setGender(updatedDTO.getGender());
        if (updatedDTO.getBloodGroup() != null) patient.setBloodGroup(updatedDTO.getBloodGroup());
        if (updatedDTO.getAddress() != null) patient.setAddress(updatedDTO.getAddress());
        if (updatedDTO.getEmergencyContact() != null) patient.setEmergencyContact(updatedDTO.getEmergencyContact());
        if (updatedDTO.getName() != null && patient.getUser() != null) {
            patient.getUser().setName(updatedDTO.getName());
        }
        if (updatedDTO.getPhone() != null && patient.getUser() != null) {
            patient.getUser().setPhone(updatedDTO.getPhone());
        }

        patient = patientRepository.save(patient);
        return mapToDTO(patient);
    }

    public List<PatientDTO> getAllPatients() {
        return patientRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public PatientDTO mapToDTO(Patient p) {
        PatientDTO dto = new PatientDTO();
        dto.setId(p.getId());
        if (p.getUser() != null) {
            dto.setUserId(p.getUser().getId());
            dto.setName(p.getUser().getName());
            dto.setEmail(p.getUser().getEmail());
            dto.setPhone(p.getUser().getPhone());
        }
        dto.setAge(p.getAge());
        dto.setGender(p.getGender());
        dto.setBloodGroup(p.getBloodGroup());
        dto.setAddress(p.getAddress());
        dto.setEmergencyContact(p.getEmergencyContact());
        return dto;
    }
}

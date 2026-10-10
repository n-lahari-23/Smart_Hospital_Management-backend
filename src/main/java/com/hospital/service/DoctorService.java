package com.hospital.service;

import com.hospital.dto.DoctorDTO;
import com.hospital.entity.Doctor;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.DoctorRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DoctorService {
    private final DoctorRepository doctorRepository;

    public DoctorService(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    public List<DoctorDTO> getAllDoctors() {
        return doctorRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public List<DoctorDTO> getDoctorsByDepartment(Long departmentId) {
        return doctorRepository.findByDepartmentId(departmentId).stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public DoctorDTO getDoctorById(Long id) {
        Doctor doc = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + id));
        return mapToDTO(doc);
    }

    public DoctorDTO getDoctorByUserId(Long userId) {
        Doctor doc = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found for user id: " + userId));
        return mapToDTO(doc);
    }

    public DoctorDTO mapToDTO(Doctor doc) {
        DoctorDTO dto = new DoctorDTO();
        dto.setId(doc.getId());
        if (doc.getUser() != null) {
            dto.setUserId(doc.getUser().getId());
            dto.setName(doc.getUser().getName());
            dto.setEmail(doc.getUser().getEmail());
            dto.setPhone(doc.getUser().getPhone());
        }
        if (doc.getDepartment() != null) {
            dto.setDepartmentId(doc.getDepartment().getId());
            dto.setDepartmentName(doc.getDepartment().getName());
        }
        dto.setSpecialization(doc.getSpecialization());
        dto.setQualification(doc.getQualification());
        dto.setExperienceYears(doc.getExperienceYears());
        dto.setConsultationFee(doc.getConsultationFee());
        dto.setAvailableDays(doc.getAvailableDays());
        dto.setAvailableTimeStart(doc.getAvailableTimeStart());
        dto.setAvailableTimeEnd(doc.getAvailableTimeEnd());
        return dto;
    }
}

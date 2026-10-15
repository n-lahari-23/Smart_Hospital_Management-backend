package com.hospital.service;

import com.hospital.dto.*;
import com.hospital.entity.Doctor;
import com.hospital.entity.DoctorUnavailability;
import com.hospital.exception.BadRequestException;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.DoctorRepository;
import com.hospital.repository.DoctorUnavailabilityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DoctorService {
    private final DoctorRepository doctorRepository;
    private final DoctorUnavailabilityRepository unavailabilityRepository;

    public DoctorService(DoctorRepository doctorRepository, DoctorUnavailabilityRepository unavailabilityRepository) {
        this.doctorRepository = doctorRepository;
        this.unavailabilityRepository = unavailabilityRepository;
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

    public DoctorScheduleDTO getDoctorSchedule(Long userId) {
        Doctor doc = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found for user id: " + userId));

        DoctorScheduleDTO dto = new DoctorScheduleDTO();
        dto.setDoctorId(doc.getId());
        dto.setDoctorName(doc.getUser() != null ? doc.getUser().getName() : "Doctor");
        dto.setAvailableDays(doc.getAvailableDays());
        dto.setAvailableTimeStart(doc.getAvailableTimeStart());
        dto.setAvailableTimeEnd(doc.getAvailableTimeEnd());
        dto.setMaxPatientsPerDay(doc.getMaxPatientsPerDay());

        List<DoctorUnavailabilityDTO> unavailabilities = unavailabilityRepository
                .findByDoctorIdOrderByUnavailableDateAscStartTimeAsc(doc.getId())
                .stream()
                .map(this::mapUnavailabilityToDTO)
                .collect(Collectors.toList());
        dto.setUnavailabilities(unavailabilities);

        return dto;
    }

    @Transactional
    public DoctorScheduleDTO updateDoctorSchedule(Long userId, DoctorScheduleRequest req) {
        Doctor doc = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found for user id: " + userId));

        if (req.getAvailableDays() != null && !req.getAvailableDays().trim().isEmpty()) {
            doc.setAvailableDays(req.getAvailableDays().trim());
        }
        if (req.getAvailableTimeStart() != null) {
            doc.setAvailableTimeStart(req.getAvailableTimeStart());
        }
        if (req.getAvailableTimeEnd() != null) {
            if (req.getAvailableTimeStart() != null && req.getAvailableTimeEnd().isBefore(req.getAvailableTimeStart())) {
                throw new BadRequestException("Available end time must be after start time");
            }
            doc.setAvailableTimeEnd(req.getAvailableTimeEnd());
        }
        if (req.getMaxPatientsPerDay() != null && req.getMaxPatientsPerDay() > 0) {
            doc.setMaxPatientsPerDay(req.getMaxPatientsPerDay());
        }

        doc = doctorRepository.save(doc);
        return getDoctorSchedule(userId);
    }

    public List<DoctorUnavailabilityDTO> getDoctorUnavailabilities(Long userId) {
        Doctor doc = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found"));
        return unavailabilityRepository.findByDoctorIdOrderByUnavailableDateAscStartTimeAsc(doc.getId())
                .stream()
                .map(this::mapUnavailabilityToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public DoctorUnavailabilityDTO addDoctorUnavailability(Long userId, DoctorUnavailabilityRequest req) {
        Doctor doc = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found"));

        if (req.getUnavailableDate() == null) {
            throw new BadRequestException("Unavailable date is required");
        }
        if (req.getUnavailableDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Unavailable date cannot be in the past");
        }
        if (req.getStartTime() != null && req.getEndTime() != null && !req.getEndTime().isAfter(req.getStartTime())) {
            throw new BadRequestException("End time must be after start time");
        }

        DoctorUnavailability unavailability = new DoctorUnavailability(
                doc,
                req.getUnavailableDate(),
                req.getStartTime(),
                req.getEndTime(),
                req.getReason() != null ? req.getReason().trim() : "Scheduled Leave"
        );
        unavailability = unavailabilityRepository.save(unavailability);
        return mapUnavailabilityToDTO(unavailability);
    }

    @Transactional
    public void deleteDoctorUnavailability(Long userId, Long unavailabilityId) {
        Doctor doc = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found"));

        DoctorUnavailability unavailability = unavailabilityRepository.findById(unavailabilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Unavailability record not found with id: " + unavailabilityId));

        if (!unavailability.getDoctor().getId().equals(doc.getId())) {
            throw new BadRequestException("Unauthorized access to this unavailability record");
        }

        unavailabilityRepository.delete(unavailability);
    }

    public DoctorUnavailabilityDTO mapUnavailabilityToDTO(DoctorUnavailability u) {
        return new DoctorUnavailabilityDTO(
                u.getId(),
                u.getDoctor().getId(),
                u.getDoctor().getUser() != null ? u.getDoctor().getUser().getName() : "Doctor",
                u.getUnavailableDate(),
                u.getStartTime(),
                u.getEndTime(),
                u.getReason(),
                u.getCreatedAt()
        );
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

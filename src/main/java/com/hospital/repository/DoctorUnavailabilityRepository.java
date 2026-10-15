package com.hospital.repository;

import com.hospital.entity.DoctorUnavailability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DoctorUnavailabilityRepository extends JpaRepository<DoctorUnavailability, Long> {
    List<DoctorUnavailability> findByDoctorIdOrderByUnavailableDateAscStartTimeAsc(Long doctorId);
    List<DoctorUnavailability> findByDoctorIdAndUnavailableDate(Long doctorId, LocalDate date);
    List<DoctorUnavailability> findByDoctorIdAndUnavailableDateGreaterThanEqualOrderByUnavailableDateAsc(Long doctorId, LocalDate date);
}

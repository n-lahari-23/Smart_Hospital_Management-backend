package com.hospital.repository;

import com.hospital.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByPatientIdOrderByAppointmentDateDescAppointmentTimeDesc(Long patientId);
    List<Appointment> findByDoctorIdOrderByAppointmentDateDescAppointmentTimeAsc(Long doctorId);
    List<Appointment> findByDoctorIdAndAppointmentDateOrderByTokenNumberAsc(Long doctorId, LocalDate date);
    boolean existsByDoctorIdAndAppointmentDateAndAppointmentTime(Long doctorId, LocalDate date, LocalTime time);
    long countByDoctorIdAndAppointmentDate(Long doctorId, LocalDate date);
    long countByAppointmentDate(LocalDate date);
    long countByStatus(String status);
}

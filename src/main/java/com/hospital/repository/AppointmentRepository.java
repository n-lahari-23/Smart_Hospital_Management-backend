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
    List<Appointment> findByDoctorIdAndAppointmentDateAndStatusNot(Long doctorId, LocalDate date, String excludedStatus);
    
    boolean existsByDoctorIdAndAppointmentDateAndAppointmentTime(Long doctorId, LocalDate date, LocalTime time);
    boolean existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusNot(Long doctorId, LocalDate date, LocalTime time, String excludedStatus);
    
    long countByDoctorIdAndAppointmentDate(Long doctorId, LocalDate date);
    long countByDoctorIdAndAppointmentDateAndStatusNot(Long doctorId, LocalDate date, String excludedStatus);
    long countByAppointmentDate(LocalDate date);
    long countByStatus(String status);
    long countByAppointmentDateAndStatus(LocalDate date, String status);

    List<Appointment> findByAppointmentDateBetweenOrderByAppointmentDateAsc(LocalDate startDate, LocalDate endDate);
    List<Appointment> findAllByOrderByAppointmentDateDescAppointmentTimeDesc();
}

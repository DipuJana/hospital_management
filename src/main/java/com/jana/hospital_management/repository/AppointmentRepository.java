package com.jana.hospital_management.repository;

import com.jana.hospital_management.entity.Appointment;
import com.jana.hospital_management.entity.AppointmentStatus;
import com.jana.hospital_management.entity.Patient;
import com.jana.hospital_management.entity.Doctor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // 1. Check if a doctor is already booked at a specific time for a specific status
    boolean existsByDoctorAndAppointmentDateTimeAndStatus(
            Doctor doctor,
            LocalDateTime appointmentDateTime,
            AppointmentStatus status
    );

    boolean existsByDoctorAndAppointmentDateTimeAndStatusAndIdNot(
            Doctor doctor,
            LocalDateTime appointmentDateTime,
            AppointmentStatus status,
            Long id
    );

    // 2. Get appointments of a doctor in a time range
    List<Appointment> findByDoctorAndAppointmentDateTimeBetween(
            Doctor doctor,
            LocalDateTime start,
            LocalDateTime end,
            AppointmentStatus status
    );

    // 3. Get appointment of a patient in a time range
    List<Appointment> findByPatientAndAppointmentDateTimeBetween(
            Patient patient,
            LocalDateTime start,
            LocalDateTime end,
            AppointmentStatus status
    );

    Page<Appointment> findByPatient(Patient patient, Pageable pageable);
    Page<Appointment> findByDoctor(Doctor doctor, Pageable pageable);

}

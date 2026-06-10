package com.jana.hospital_management.service;

import com.jana.hospital_management.dto.AppointmentRequestDTO;
import com.jana.hospital_management.dto.AppointmentResponseDTO;
import com.jana.hospital_management.dto.PageResponse;
import com.jana.hospital_management.entity.*;
import com.jana.hospital_management.exception.ConflictException;
import com.jana.hospital_management.exception.ResourceNotFoundException;
import com.jana.hospital_management.repository.AppointmentRepository;
import com.jana.hospital_management.repository.DoctorRepository;
import com.jana.hospital_management.repository.PatientRepository;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

@Service
public class AppointmentService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id",
            "appointmentDateTime",
            "status",
            "reason"
    );

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            PatientRepository patientRepository,
            DoctorRepository doctorRepository
    ) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
    }

    // =========================
    // Create Appointment
    // =========================

    @Transactional
    public AppointmentResponseDTO createAppointment(AppointmentRequestDTO request) {

        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Patient not found with id " + request.getPatientId()));

        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Doctor not found with id " + request.getDoctorId()));

        boolean isBooked =
                appointmentRepository.existsByDoctorAndAppointmentDateTimeAndStatus(
                        doctor,
                        request.getAppointmentDateTime(),
                        AppointmentStatus.SCHEDULED
                );

        if (isBooked) {
            throw new ConflictException("Doctor already booked at this time");
        }

        Appointment appointment = new Appointment(
                patient,
                doctor,
                request.getAppointmentDateTime(),
                request.getReason()
        );

        Appointment saved = appointmentRepository.save(appointment);

        return mapToDTO(saved);
    }

    // =========================
    // Cancel Appointment
    // =========================

    @Transactional
    public AppointmentResponseDTO cancelAppointment(Long appointmentId) {

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Appointment not found with id " + appointmentId));

        appointment.cancel();

        return mapToDTO(appointment);
    }

    // =========================
    // Complete Appointment
    // =========================

    @Transactional
    public AppointmentResponseDTO completeAppointment(Long appointmentId) {

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Appointment not found with id " + appointmentId));

        appointment.complete();

        return mapToDTO(appointment);
    }

    // =========================
    // Reschedule Appointment
    // =========================

    @Transactional
    public AppointmentResponseDTO rescheduleAppointment(
            Long appointmentId,
            LocalDateTime newDateTime
    ) {

        if (newDateTime == null) {
            throw new IllegalArgumentException(
                    "New appointment time cannot be null");
        }

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Appointment not found with id " + appointmentId));

        boolean isBooked =
                appointmentRepository
                        .existsByDoctorAndAppointmentDateTimeAndStatusAndIdNot(
                                appointment.getDoctor(),
                                newDateTime,
                                AppointmentStatus.SCHEDULED,
                                appointment.getId()
                        );

        if (isBooked) {
            throw new ConflictException(
                    "Doctor already booked at this time");
        }

        appointment.reschedule(newDateTime);

        return mapToDTO(appointment);
    }

    // =========================
    // Get Appointments By Patient
    // =========================

    @Transactional(readOnly = true)
    public PageResponse<AppointmentResponseDTO> getAppointmentsByPatient(
            Long patientId,
            int page,
            int size,
            String sortBy,
            String direction
    ) {

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Patient not found with id " + patientId));

        Pageable pageable =
                createPageable(page, size, sortBy, direction);

        Page<AppointmentResponseDTO> pageResult =
                appointmentRepository
                        .findByPatient(patient, pageable)
                        .map(this::mapToDTO);

        return new PageResponse<>(pageResult);
    }

    // =========================
    // Get Appointments By Doctor
    // =========================

    @Transactional(readOnly = true)
    public PageResponse<AppointmentResponseDTO> getAppointmentsByDoctor(
            Long doctorId,
            int page,
            int size,
            String sortBy,
            String direction
    ) {

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Doctor not found with id " + doctorId));

        Pageable pageable =
                createPageable(page, size, sortBy, direction);

        Page<AppointmentResponseDTO> pageResult =
                appointmentRepository
                        .findByDoctor(doctor, pageable)
                        .map(this::mapToDTO);

        return new PageResponse<>(pageResult);
    }

    // =========================
    // Pagination Helper
    // =========================

    private Pageable createPageable(
            int page,
            int size,
            String sortBy,
            String direction
    ) {

        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page number cannot be negative");
        }

        if (size <= 0 || size > 50) {
            throw new IllegalArgumentException(
                    "Page size must be between 1 and 50");
        }

        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new IllegalArgumentException(
                    "Invalid sort field: "
                            + sortBy
                            + ". Allowed fields: "
                            + ALLOWED_SORT_FIELDS);
        }

        Sort.Direction sortDirection;

        try {
            sortDirection =
                    Sort.Direction.fromString(direction.trim());
        } catch (Exception ex) {
            throw new IllegalArgumentException(
                    "Invalid sort direction: " + direction);
        }

        return PageRequest.of(
                page,
                size,
                Sort.by(sortDirection, sortBy)
        );
    }

    // =========================
    // DTO Mapper
    // =========================

    private AppointmentResponseDTO mapToDTO(Appointment a) {

        return new AppointmentResponseDTO(
                a.getId(),
                a.getPatient().getId(),
                a.getPatient().getFullName(),
                a.getDoctor().getId(),
                a.getDoctor().getFullName(),
                a.getAppointmentDateTime(),
                a.getStatus(),
                a.getReason()
        );
    }
}
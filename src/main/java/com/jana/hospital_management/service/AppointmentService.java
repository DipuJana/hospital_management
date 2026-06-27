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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private final PaginationService paginationService;
    private static final Logger logger =
            LoggerFactory.getLogger(AppointmentService.class);

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            PatientRepository patientRepository,
            DoctorRepository doctorRepository,
            PaginationService paginationService
    ) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.paginationService = paginationService;
    }

    // =========================
    // Create Appointment
    // =========================

    @Transactional
    public AppointmentResponseDTO createAppointment(AppointmentRequestDTO request) {

        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> {
                    logger.warn(
                            "Appointment creation failed: patient with id {} not found.",
                            request.getPatientId()
                    );

                    return new ResourceNotFoundException(
                            "Patient not found with id " + request.getPatientId());
                });

        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> {
                    logger.warn(
                            "Appointment creation failed: doctor with id {} not found.",
                            request.getDoctorId()
                    );

                    return new ResourceNotFoundException(
                            "Doctor not found with id " + request.getDoctorId());
                });

        boolean isBooked =
                appointmentRepository.existsByDoctorAndAppointmentDateTimeAndStatus(
                        doctor,
                        request.getAppointmentDateTime(),
                        AppointmentStatus.SCHEDULED
                );

        if (isBooked) {

            logger.warn(
                    "Appointment scheduling conflict: doctor '{}' (ID={}) is already booked at {}.",
                    doctor.getFullName(),
                    doctor.getId(),
                    request.getAppointmentDateTime()
            );

            throw new ConflictException("Doctor already booked at this time");
        }

        Appointment appointment = new Appointment(
                patient,
                doctor,
                request.getAppointmentDateTime(),
                request.getReason()
        );

        Appointment saved = appointmentRepository.save(appointment);

        logger.info(
                "Appointment {} created for patient '{}' (ID={}) with doctor '{}' (ID={}) at {}.",
                saved.getId(),
                patient.getFullName(),
                patient.getId(),
                doctor.getFullName(),
                doctor.getId(),
                saved.getAppointmentDateTime()
        );

        return mapToDTO(saved);
    }

    // =========================
    // Cancel Appointment
    // =========================

    @Transactional
    public AppointmentResponseDTO cancelAppointment(Long appointmentId) {

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> {

                    logger.warn(
                            "Appointment cancellation failed: appointment {} not found.",
                            appointmentId
                    );

                    return new ResourceNotFoundException(
                            "Appointment not found with id " + appointmentId);
                });

        appointment.cancel();

        logger.info(
                "Appointment {} cancelled.",
                appointment.getId()
        );

        return mapToDTO(appointment);
    }

    // =========================
    // Complete Appointment
    // =========================

    @Transactional
    public AppointmentResponseDTO completeAppointment(Long appointmentId) {

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> {

                    logger.warn(
                            "Appointment completion failed: appointment {} not found.",
                            appointmentId
                    );

                    return new ResourceNotFoundException(
                            "Appointment not found with id " + appointmentId);
                });

        appointment.complete();

        logger.info(
                "Appointment {} completed.",
                appointment.getId()
        );

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

            logger.warn(
                    "Appointment {} reschedule failed: new appointment time is null.",
                    appointmentId
            );

            throw new IllegalArgumentException(
                    "New appointment time cannot be null");
        }

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> {

                    logger.warn(
                            "Appointment reschedule failed: appointment {} not found.",
                            appointmentId
                    );

                    return new ResourceNotFoundException(
                            "Appointment not found with id " + appointmentId);
                });

        boolean isBooked =
                appointmentRepository
                        .existsByDoctorAndAppointmentDateTimeAndStatusAndIdNot(
                                appointment.getDoctor(),
                                newDateTime,
                                AppointmentStatus.SCHEDULED,
                                appointment.getId()
                        );

        if (isBooked) {
            logger.warn(
                    "Appointment {} could not be rescheduled because doctor {} is already booked at {}.",
                    appointment.getId(),
                    appointment.getDoctor().getId(),
                    newDateTime
            );

            throw new ConflictException(
                    "Doctor already booked at this time");
        }

        LocalDateTime oldDateTime = appointment.getAppointmentDateTime();

        appointment.reschedule(newDateTime);

        logger.info(
                "Appointment {} rescheduled from {} to {}.",
                appointment.getId(),
                oldDateTime,
                newDateTime
        );

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
                .orElseThrow(() -> {
                    logger.warn(
                            "Patient {} not found while retrieving appointments.",
                            patientId
                    );
                    return new ResourceNotFoundException(
                            "Patient not found with id " + patientId);
                });

        Pageable pageable =
                paginationService.createPageable(
                        page,
                        size,
                        sortBy,
                        direction,
                        ALLOWED_SORT_FIELDS
                );

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
                .orElseThrow(() -> {
                    logger.warn(
                            "Doctor {} not found while retrieving appointments.",
                            doctorId
                    );
                    return new ResourceNotFoundException(
                            "Doctor not found with id " + doctorId);
                });

        Pageable pageable =
                paginationService.createPageable(
                        page,
                        size,
                        sortBy,
                        direction,
                        ALLOWED_SORT_FIELDS
                );

        Page<AppointmentResponseDTO> pageResult =
                appointmentRepository
                        .findByDoctor(doctor, pageable)
                        .map(this::mapToDTO);

        return new PageResponse<>(pageResult);
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
package com.jana.hospital_management.service;

import com.jana.hospital_management.dto.AppointmentRequestDTO;
import com.jana.hospital_management.dto.AppointmentResponseDTO;
import com.jana.hospital_management.entity.*;
import com.jana.hospital_management.exception.ResourceNotFoundException;
import com.jana.hospital_management.repository.*;
import com.jana.hospital_management.exception.ConflictException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppointmentService {

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

    // 1. Create Appointment
    @Transactional
    public AppointmentResponseDTO createAppointment(AppointmentRequestDTO request){

        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id " + request.getPatientId()));

        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id " + request.getDoctorId()));

        boolean isBooked = appointmentRepository
                .existsByDoctorAndAppointmentDateTimeAndStatus(
                        doctor,
                        request.getAppointmentDateTime(),
                        AppointmentStatus.SCHEDULED
                );

        if(isBooked){
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

    // 2. Cancel Appointment
    @Transactional
    public AppointmentResponseDTO cancelAppointment(Long appointmentId){
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id " + appointmentId));

        appointment.cancel();

        return mapToDTO(appointment);
    }

    // 3. Complete Appointment
    @Transactional
    public AppointmentResponseDTO completeAppointment(Long appointmentId){
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id " + appointmentId));

        appointment.complete();

        return mapToDTO(appointment);
    }

    // 4. Reschedule
    @Transactional
    public AppointmentResponseDTO rescheduleAppointment(
            Long appointmentId,
            java.time.LocalDateTime newDateTime

    ){

        if (newDateTime == null) {
            throw new IllegalArgumentException("New appointment time cannot be null");
        }

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id " + appointmentId));

        boolean isBooked = appointmentRepository.existsByDoctorAndAppointmentDateTimeAndStatusAndIdNot(
                appointment.getDoctor(),
                newDateTime,
                AppointmentStatus.SCHEDULED,
                appointment.getId()
        );

        if(isBooked){
            throw new ConflictException("Doctor already booked at this time.");
        }

        appointment.reschedule(newDateTime);

        return mapToDTO(appointment);
    }

    // Get by Patient
    @Transactional(readOnly = true)
    public Page<AppointmentResponseDTO> getAppointmentsByPatient(
            Long patientID,
            Pageable pageable
    ) {
        Patient patient = patientRepository.findById(patientID)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id " + patientID));

        return appointmentRepository.findByPatient(patient, pageable)
                .map(this::mapToDTO);
    }

    // Get by Doctor
    @Transactional(readOnly = true)
    public Page<AppointmentResponseDTO> getAppointmentsByDoctor(
            Long doctorID,
            Pageable pageable
    ) {
        Doctor doctor = doctorRepository.findById(doctorID)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id " + doctorID));

        return appointmentRepository.findByDoctor(doctor, pageable)
                .map(this::mapToDTO);
    }

    private AppointmentResponseDTO mapToDTO(Appointment a){
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

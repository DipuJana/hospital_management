package com.jana.hospital_management.service;

import com.jana.hospital_management.dto.AppointmentRequestDTO;
import com.jana.hospital_management.dto.AppointmentResponseDTO;
import com.jana.hospital_management.entity.*;
import com.jana.hospital_management.exception.ConflictException;
import com.jana.hospital_management.exception.ResourceNotFoundException;
import com.jana.hospital_management.repository.AppointmentRepository;
import com.jana.hospital_management.repository.DoctorRepository;
import com.jana.hospital_management.repository.PatientRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private PaginationService paginationService;

    @InjectMocks
    private AppointmentService appointmentService;

    private Patient patient;
    private Doctor doctor;
    private Appointment appointment;
    private AppointmentRequestDTO request;

    @BeforeEach
    void setUp() {

        patient = new Patient(
                "John Doe",
                LocalDate.of(2000, 1, 1),
                "john@hospital.com",
                "9876543210",
                Gender.MALE
        );

        ReflectionTestUtils.setField(
                patient,
                "id",
                1L
        );

        doctor = new Doctor(
                "Dr. Smith",
                Specialization.CARDIOLOGY,
                "doctor@hospital.com",
                "9999999999"
        );

        ReflectionTestUtils.setField(
                doctor,
                "id",
                1L
        );

        appointment = new Appointment(
                patient,
                doctor,
                LocalDateTime.now().plusDays(1),
                "Regular checkup"
        );

        ReflectionTestUtils.setField(
                appointment,
                "id",
                1L
        );

        request = new AppointmentRequestDTO();
        request.setPatientId(1L);
        request.setDoctorId(1L);
        request.setAppointmentDateTime(
                LocalDateTime.now().plusDays(1)
        );
        request.setReason("Regular checkup");
    }

    @Test
    void createAppointment_ShouldCreateAppointment_WhenSlotAvailable() {

        when(patientRepository.findById(1L))
                .thenReturn(Optional.of(patient));

        when(doctorRepository.findById(1L))
                .thenReturn(Optional.of(doctor));

        when(appointmentRepository
                .existsByDoctorAndAppointmentDateTimeAndStatus(
                        doctor,
                        request.getAppointmentDateTime(),
                        AppointmentStatus.SCHEDULED
                ))
                .thenReturn(false);

        when(appointmentRepository.save(any(Appointment.class)))
                .thenReturn(appointment);

        AppointmentResponseDTO response =
                appointmentService.createAppointment(request);

        ArgumentCaptor<Appointment> captor =
                ArgumentCaptor.forClass(Appointment.class);

        verify(appointmentRepository)
                .save(captor.capture());

        Appointment savedAppointment =
                captor.getValue();

        assertEquals(
                patient,
                savedAppointment.getPatient()
        );

        assertEquals(
                doctor,
                savedAppointment.getDoctor()
        );

        assertEquals(
                request.getReason(),
                savedAppointment.getReason()
        );

        assertEquals(
                AppointmentStatus.SCHEDULED,
                savedAppointment.getStatus()
        );

        assertEquals(
                appointment.getId(),
                response.getId()
        );

        assertEquals(
                patient.getId(),
                response.getPatientId()
        );

        assertEquals(
                doctor.getId(),
                response.getDoctorId()
        );

        verify(patientRepository)
                .findById(1L);

        verify(doctorRepository)
                .findById(1L);
    }

    @Test
    void createAppointment_ShouldThrowException_WhenPatientNotFound() {

        when(patientRepository.findById(1L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> appointmentService.createAppointment(request)
                );

        assertEquals(
                "Patient not found with id 1",
                exception.getMessage()
        );

        verify(patientRepository)
                .findById(1L);

        verify(doctorRepository, never())
                .findById(anyLong());

        verify(appointmentRepository, never())
                .save(any(Appointment.class));
    }

    @Test
    void createAppointment_ShouldThrowException_WhenDoctorNotFound() {

        when(patientRepository.findById(1L))
                .thenReturn(Optional.of(patient));

        when(doctorRepository.findById(1L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> appointmentService.createAppointment(request)
                );

        assertEquals(
                "Doctor not found with id 1",
                exception.getMessage()
        );

        verify(patientRepository)
                .findById(1L);

        verify(doctorRepository)
                .findById(1L);

        verify(appointmentRepository, never())
                .save(any(Appointment.class));
    }

    @Test
    void createAppointment_ShouldThrowException_WhenDoctorAlreadyBooked() {

        when(patientRepository.findById(1L))
                .thenReturn(Optional.of(patient));

        when(doctorRepository.findById(1L))
                .thenReturn(Optional.of(doctor));

        when(appointmentRepository
                .existsByDoctorAndAppointmentDateTimeAndStatus(
                        doctor,
                        request.getAppointmentDateTime(),
                        AppointmentStatus.SCHEDULED
                ))
                .thenReturn(true);

        ConflictException exception =
                assertThrows(
                        ConflictException.class,
                        () -> appointmentService.createAppointment(request)
                );

        assertEquals(
                "Doctor already booked at this time",
                exception.getMessage()
        );

        verify(patientRepository)
                .findById(1L);

        verify(doctorRepository)
                .findById(1L);

        verify(appointmentRepository)
                .existsByDoctorAndAppointmentDateTimeAndStatus(
                        doctor,
                        request.getAppointmentDateTime(),
                        AppointmentStatus.SCHEDULED
                );

        verify(appointmentRepository, never())
                .save(any(Appointment.class));
    }

    @Test
    void cancelAppointment_ShouldCancelAppointment() {

        when(appointmentRepository.findById(1L))
                .thenReturn(Optional.of(appointment));

        AppointmentResponseDTO response =
                appointmentService.cancelAppointment(1L);

        assertEquals(
                AppointmentStatus.CANCELLED,
                appointment.getStatus()
        );

        assertEquals(
                AppointmentStatus.CANCELLED,
                response.getStatus()
        );

        verify(appointmentRepository)
                .findById(1L);
    }

    @Test
    void cancelAppointment_ShouldThrowException_WhenAppointmentNotFound() {

        when(appointmentRepository.findById(1L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> appointmentService.cancelAppointment(1L)
                );

        assertEquals(
                "Appointment not found with id 1",
                exception.getMessage()
        );

        verify(appointmentRepository)
                .findById(1L);
    }

    @Test
    void completeAppointment_ShouldCompleteAppointment() {

        when(appointmentRepository.findById(1L))
                .thenReturn(Optional.of(appointment));

        AppointmentResponseDTO response =
                appointmentService.completeAppointment(1L);

        assertEquals(
                AppointmentStatus.COMPLETED,
                appointment.getStatus()
        );

        assertEquals(
                AppointmentStatus.COMPLETED,
                response.getStatus()
        );

        verify(appointmentRepository)
                .findById(1L);
    }

    @Test
    void completeAppointment_ShouldThrowException_WhenAppointmentNotFound() {

        when(appointmentRepository.findById(1L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> appointmentService.completeAppointment(1L)
                );

        assertEquals(
                "Appointment not found with id 1",
                exception.getMessage()
        );

        verify(appointmentRepository)
                .findById(1L);
    }

    @Test
    void rescheduleAppointment_ShouldRescheduleAppointment() {

        LocalDateTime newDateTime =
                LocalDateTime.now().plusDays(2);

        when(appointmentRepository.findById(1L))
                .thenReturn(Optional.of(appointment));

        when(appointmentRepository
                .existsByDoctorAndAppointmentDateTimeAndStatusAndIdNot(
                        doctor,
                        newDateTime,
                        AppointmentStatus.SCHEDULED,
                        1L
                ))
                .thenReturn(false);

        AppointmentResponseDTO response =
                appointmentService.rescheduleAppointment(
                        1L,
                        newDateTime
                );

        assertEquals(
                newDateTime,
                appointment.getAppointmentDateTime()
        );

        assertEquals(
                newDateTime,
                response.getAppointmentDateTime()
        );

        verify(appointmentRepository)
                .findById(1L);

        verify(appointmentRepository)
                .existsByDoctorAndAppointmentDateTimeAndStatusAndIdNot(
                        doctor,
                        newDateTime,
                        AppointmentStatus.SCHEDULED,
                        1L
                );
    }

    @Test
    void rescheduleAppointment_ShouldThrowException_WhenAppointmentNotFound() {

        LocalDateTime newDateTime =
                LocalDateTime.now().plusDays(2);

        when(appointmentRepository.findById(1L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> appointmentService.rescheduleAppointment(
                                1L,
                                newDateTime
                        )
                );

        assertEquals(
                "Appointment not found with id 1",
                exception.getMessage()
        );

        verify(appointmentRepository)
                .findById(1L);

        verify(appointmentRepository, never())
                .existsByDoctorAndAppointmentDateTimeAndStatusAndIdNot(
                        any(),
                        any(),
                        any(),
                        anyLong()
                );
    }

    @Test
    void rescheduleAppointment_ShouldThrowException_WhenNewDateIsNull() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> appointmentService.rescheduleAppointment(
                                1L,
                                null
                        )
                );

        assertEquals(
                "New appointment time cannot be null",
                exception.getMessage()
        );

        verify(appointmentRepository, never())
                .findById(anyLong());

        verify(appointmentRepository, never())
                .existsByDoctorAndAppointmentDateTimeAndStatusAndIdNot(
                        any(),
                        any(),
                        any(),
                        anyLong()
                );
    }

    @Test
    void rescheduleAppointment_ShouldThrowException_WhenDoctorAlreadyBooked() {

        LocalDateTime newDateTime =
                LocalDateTime.now().plusDays(2);

        when(appointmentRepository.findById(1L))
                .thenReturn(Optional.of(appointment));

        when(appointmentRepository
                .existsByDoctorAndAppointmentDateTimeAndStatusAndIdNot(
                        doctor,
                        newDateTime,
                        AppointmentStatus.SCHEDULED,
                        1L
                ))
                .thenReturn(true);

        ConflictException exception =
                assertThrows(
                        ConflictException.class,
                        () -> appointmentService.rescheduleAppointment(
                                1L,
                                newDateTime
                        )
                );

        assertEquals(
                "Doctor already booked at this time",
                exception.getMessage()
        );

        verify(appointmentRepository)
                .findById(1L);

        verify(appointmentRepository)
                .existsByDoctorAndAppointmentDateTimeAndStatusAndIdNot(
                        doctor,
                        newDateTime,
                        AppointmentStatus.SCHEDULED,
                        1L
                );

        verify(appointmentRepository, never())
                .save(any(Appointment.class));
    }

    @Test
    void completeAppointment_ShouldThrowException_WhenAppointmentIsCancelled() {

        appointment.cancel();

        when(appointmentRepository.findById(1L))
                .thenReturn(Optional.of(appointment));

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> appointmentService.completeAppointment(1L)
                );

        assertEquals(
                "Only scheduled appointments can be completed.",
                exception.getMessage()
        );

        verify(appointmentRepository)
                .findById(1L);
    }

    @Test
    void cancelAppointment_ShouldThrowException_WhenAppointmentIsCompleted() {

        appointment.complete();

        when(appointmentRepository.findById(1L))
                .thenReturn(Optional.of(appointment));

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> appointmentService.cancelAppointment(1L)
                );

        assertEquals(
                "Only scheduled appointments can be cancelled",
                exception.getMessage()
        );

        verify(appointmentRepository)
                .findById(1L);
    }

    @Test
    void rescheduleAppointment_ShouldThrowException_WhenAppointmentIsCancelled() {

        appointment.cancel();

        when(appointmentRepository.findById(1L))
                .thenReturn(Optional.of(appointment));

        LocalDateTime newDateTime =
                LocalDateTime.now().plusDays(2);

        when(appointmentRepository
                .existsByDoctorAndAppointmentDateTimeAndStatusAndIdNot(
                        doctor,
                        newDateTime,
                        AppointmentStatus.SCHEDULED,
                        1L
                ))
                .thenReturn(false);

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> appointmentService.rescheduleAppointment(
                                1L,
                                newDateTime
                        )
                );

        assertEquals(
                "Only scheduled appointments can be rescheduled.",
                exception.getMessage()
        );

        verify(appointmentRepository)
                .findById(1L);
    }

    @Test
    void rescheduleAppointment_ShouldThrowException_WhenAppointmentIsCompleted() {

        appointment.complete();

        when(appointmentRepository.findById(1L))
                .thenReturn(Optional.of(appointment));

        LocalDateTime newDateTime =
                LocalDateTime.now().plusDays(2);

        when(appointmentRepository
                .existsByDoctorAndAppointmentDateTimeAndStatusAndIdNot(
                        doctor,
                        newDateTime,
                        AppointmentStatus.SCHEDULED,
                        1L
                ))
                .thenReturn(false);

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> appointmentService.rescheduleAppointment(
                                1L,
                                newDateTime
                        )
                );

        assertEquals(
                "Only scheduled appointments can be rescheduled.",
                exception.getMessage()
        );

        verify(appointmentRepository)
                .findById(1L);
    }

}
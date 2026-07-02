package com.jana.hospital_management.service;

import com.jana.hospital_management.dto.DoctorRequestDTO;
import com.jana.hospital_management.dto.DoctorResponseDTO;
import com.jana.hospital_management.entity.Doctor;
import com.jana.hospital_management.entity.Specialization;
import com.jana.hospital_management.exception.DuplicateResourceException;
import com.jana.hospital_management.exception.ResourceNotFoundException;
import com.jana.hospital_management.repository.DoctorRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorServiceTest {

    @Mock
    private DoctorRepository doctorRepository;

    @InjectMocks
    private DoctorService doctorService;

    private Doctor doctor;
    private DoctorRequestDTO request;

    @BeforeEach
    void setUp() {

        doctor = new Doctor(
                "Dr. John Doe",
                Specialization.CARDIOLOGY,
                "john@hospital.com",
                "9876543210"
        );

        ReflectionTestUtils.setField(
                doctor,
                "id",
                1L
        );

        request = new DoctorRequestDTO();
        request.setFullName("Dr. John Doe");
        request.setSpecialization(Specialization.CARDIOLOGY);
        request.setEmail("john@hospital.com");
        request.setPhoneNumber("9876543210");
    }

    @Test
    void createDoctor_ShouldCreateDoctor_WhenEmailDoesNotExist() {

        when(doctorRepository.existsByEmail("john@hospital.com"))
                .thenReturn(false);

        when(doctorRepository.save(any(Doctor.class)))
                .thenReturn(doctor);

        DoctorResponseDTO response =
                doctorService.createDoctor(request);

        ArgumentCaptor<Doctor> captor =
                ArgumentCaptor.forClass(Doctor.class);

        verify(doctorRepository)
                .save(captor.capture());

        Doctor savedDoctor = captor.getValue();

        assertEquals(
                "Dr. John Doe",
                savedDoctor.getFullName()
        );

        assertEquals(
                Specialization.CARDIOLOGY,
                savedDoctor.getSpecialization()
        );

        assertEquals(
                "john@hospital.com",
                savedDoctor.getEmail()
        );

        assertEquals(
                "9876543210",
                savedDoctor.getPhoneNumber()
        );

        assertEquals(
                doctor.getId(),
                response.getId()
        );

        assertEquals(
                doctor.getFullName(),
                response.getFullName()
        );

        verify(doctorRepository)
                .existsByEmail("john@hospital.com");
    }

    @Test
    void createDoctor_ShouldThrowException_WhenEmailAlreadyExists() {

        when(doctorRepository.existsByEmail("john@hospital.com"))
                .thenReturn(true);

        DuplicateResourceException exception =
                assertThrows(
                        DuplicateResourceException.class,
                        () -> doctorService.createDoctor(request)
                );

        assertEquals(
                "Doctor with this email already exists",
                exception.getMessage()
        );

        verify(doctorRepository)
                .existsByEmail("john@hospital.com");

        verify(doctorRepository, never())
                .save(any(Doctor.class));
    }

    @Test
    void getDoctorById_ShouldReturnDoctor_WhenDoctorExists() {

        when(doctorRepository.findById(1L))
                .thenReturn(Optional.of(doctor));

        DoctorResponseDTO response =
                doctorService.getDoctorById(1L);

        assertNotNull(response);

        assertEquals(
                doctor.getId(),
                response.getId()
        );

        assertEquals(
                doctor.getFullName(),
                response.getFullName()
        );

        assertEquals(
                doctor.getSpecialization(),
                response.getSpecialization()
        );

        assertEquals(
                doctor.getEmail(),
                response.getEmail()
        );

        assertEquals(
                doctor.getPhoneNumber(),
                response.getPhoneNumber()
        );

        verify(doctorRepository)
                .findById(1L);
    }

    @Test
    void getDoctorById_ShouldThrowException_WhenDoctorNotFound() {

        when(doctorRepository.findById(1L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> doctorService.getDoctorById(1L)
                );

        assertEquals(
                "Doctor not found with id : 1",
                exception.getMessage()
        );

        verify(doctorRepository)
                .findById(1L);
    }

    @Test
    void updateDoctor_ShouldUpdateDoctor_WhenDoctorExists() {

        when(doctorRepository.findById(1L))
                .thenReturn(Optional.of(doctor));

        when(doctorRepository.save(any(Doctor.class)))
                .thenReturn(doctor);

        DoctorRequestDTO updateRequest =
                new DoctorRequestDTO();

        updateRequest.setFullName("Dr. John Updated");
        updateRequest.setSpecialization(
                Specialization.NEUROLOGY
        );
        updateRequest.setEmail("john@hospital.com");
        updateRequest.setPhoneNumber("9999999999");

        DoctorResponseDTO response =
                doctorService.updateDoctor(
                        1L,
                        updateRequest
                );

        ArgumentCaptor<Doctor> captor =
                ArgumentCaptor.forClass(Doctor.class);

        verify(doctorRepository)
                .save(captor.capture());

        Doctor updatedDoctor =
                captor.getValue();

        assertEquals(
                "Dr. John Updated",
                updatedDoctor.getFullName()
        );

        assertEquals(
                Specialization.NEUROLOGY,
                updatedDoctor.getSpecialization()
        );

        assertEquals(
                "9999999999",
                updatedDoctor.getPhoneNumber()
        );

        assertEquals(
                "john@hospital.com",
                updatedDoctor.getEmail()
        );

        assertEquals(
                doctor.getId(),
                response.getId()
        );

        assertEquals(
                "Dr. John Updated",
                response.getFullName()
        );

        verify(doctorRepository)
                .findById(1L);
    }

    @Test
    void updateDoctor_ShouldThrowException_WhenDoctorNotFound() {

        when(doctorRepository.findById(1L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> doctorService.updateDoctor(
                                1L,
                                request
                        )
                );

        assertEquals(
                "Doctor not found with id : 1",
                exception.getMessage()
        );

        verify(doctorRepository)
                .findById(1L);

        verify(doctorRepository, never())
                .save(any(Doctor.class));
    }

    @Test
    void updateDoctor_ShouldThrowException_WhenEmailAlreadyExists() {

        Doctor existingDoctor = new Doctor(
                "Dr. John Doe",
                Specialization.CARDIOLOGY,
                "old@hospital.com",
                "9876543210"
        );

        ReflectionTestUtils.setField(
                existingDoctor,
                "id",
                1L
        );

        DoctorRequestDTO updateRequest =
                new DoctorRequestDTO();

        updateRequest.setFullName("Dr. John Doe");
        updateRequest.setSpecialization(
                Specialization.CARDIOLOGY
        );
        updateRequest.setEmail("new@hospital.com");
        updateRequest.setPhoneNumber("9876543210");

        when(doctorRepository.findById(1L))
                .thenReturn(Optional.of(existingDoctor));

        when(doctorRepository.existsByEmail("new@hospital.com"))
                .thenReturn(true);

        DuplicateResourceException exception =
                assertThrows(
                        DuplicateResourceException.class,
                        () -> doctorService.updateDoctor(
                                1L,
                                updateRequest
                        )
                );

        assertEquals(
                "Doctor with this email already exists",
                exception.getMessage()
        );

        verify(doctorRepository)
                .findById(1L);

        verify(doctorRepository)
                .existsByEmail("new@hospital.com");

        verify(doctorRepository, never())
                .save(any(Doctor.class));
    }

    @Test
    void deleteDoctor_ShouldDeleteDoctor_WhenDoctorExists() {

        when(doctorRepository.findById(1L))
                .thenReturn(Optional.of(doctor));

        doctorService.deleteDoctor(1L);

        verify(doctorRepository)
                .findById(1L);

        verify(doctorRepository)
                .delete(doctor);

        verify(doctorRepository, never())
                .save(any(Doctor.class));
    }

    @Test
    void deleteDoctor_ShouldThrowException_WhenDoctorNotFound() {

        when(doctorRepository.findById(1L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> doctorService.deleteDoctor(1L)
                );

        assertEquals(
                "Doctor not found with id : 1",
                exception.getMessage()
        );

        verify(doctorRepository)
                .findById(1L);

        verify(doctorRepository, never())
                .delete(any(Doctor.class));
    }
}
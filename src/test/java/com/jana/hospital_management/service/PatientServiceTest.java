package com.jana.hospital_management.service;

import com.jana.hospital_management.dto.PatientRequestDTO;
import com.jana.hospital_management.dto.PatientResponseDTO;
import com.jana.hospital_management.entity.Gender;
import com.jana.hospital_management.entity.Patient;
import com.jana.hospital_management.exception.DuplicateResourceException;
import com.jana.hospital_management.exception.ResourceNotFoundException;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private PatientService patientService;

    private PatientRequestDTO request;
    private Patient patient;

    @BeforeEach
    void setUp() {

        request = new PatientRequestDTO();

        request.setFullName("John Doe");
        request.setDateOfBirth(LocalDate.of(2000, 1, 1));
        request.setEmail("john@hospital.com");
        request.setPhoneNumber("9876543210");
        request.setGender(Gender.MALE);

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
    }

    @Test
    void createPatient_ShouldCreatePatient_WhenEmailDoesNotExist() {

        when(patientRepository.existsByEmail(
                request.getEmail().toLowerCase()))
                .thenReturn(false);

        when(patientRepository.save(any(Patient.class)))
                .thenReturn(patient);

        PatientResponseDTO response =
                patientService.createPatient(request);

        ArgumentCaptor<Patient> captor =
                ArgumentCaptor.forClass(Patient.class);

        verify(patientRepository)
                .save(captor.capture());

        Patient savedPatient = captor.getValue();

        assertEquals(
                request.getFullName(),
                savedPatient.getFullName()
        );

        assertEquals(
                request.getEmail().toLowerCase(),
                savedPatient.getEmail()
        );

        assertEquals(
                request.getPhoneNumber(),
                savedPatient.getPhoneNumber()
        );

        assertEquals(
                request.getGender(),
                savedPatient.getGender()
        );

        assertNotNull(response);

        assertEquals(
                1L,
                response.getId()
        );

        assertEquals(
                "John Doe",
                response.getFullName()
        );

        assertEquals(
                "john@hospital.com",
                response.getEmail()
        );

        verify(patientRepository)
                .existsByEmail(
                        request.getEmail().toLowerCase()
                );

        verify(patientRepository)
                .save(any(Patient.class));
    }

    @Test
    void createPatient_ShouldThrowException_WhenEmailAlreadyExists() {

        when(patientRepository.existsByEmail(
                request.getEmail().toLowerCase()))
                .thenReturn(true);

        DuplicateResourceException exception =
                assertThrows(
                        DuplicateResourceException.class,
                        () -> patientService.createPatient(request)
                );

        assertEquals(
                "Patient with this email already exists",
                exception.getMessage()
        );

        verify(patientRepository, never())
                .save(any(Patient.class));
    }

    @Test
    void getPatientById_ShouldReturnPatient_WhenPatientExists() {

        when(patientRepository.findById(1L))
                .thenReturn(Optional.of(patient));

        PatientResponseDTO response =
                patientService.getPatientById(1L);

        assertNotNull(response);

        assertEquals(
                patient.getId(),
                response.getId()
        );

        assertEquals(
                patient.getFullName(),
                response.getFullName()
        );

        assertEquals(
                patient.getDateOfBirth(),
                response.getDateOfBirth()
        );

        assertEquals(
                patient.getEmail(),
                response.getEmail()
        );

        assertEquals(
                patient.getPhoneNumber(),
                response.getPhoneNumber()
        );

        assertEquals(
                patient.getGender(),
                response.getGender()
        );

        verify(patientRepository)
                .findById(1L);
    }

    @Test
    void getPatientById_ShouldThrowException_WhenPatientNotFound() {

        when(patientRepository.findById(1L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> patientService.getPatientById(1L)
                );

        assertEquals(
                "Patient not found with id : 1",
                exception.getMessage()
        );

        verify(patientRepository)
                .findById(1L);
    }

    @Test
    void updatePatient_ShouldUpdatePatient_WhenPatientExists() {

        when(patientRepository.findById(1L))
                .thenReturn(Optional.of(patient));

        when(patientRepository.save(any(Patient.class)))
                .thenReturn(patient);

        PatientRequestDTO updateRequest =
                new PatientRequestDTO();

        updateRequest.setFullName("John Updated");
        updateRequest.setDateOfBirth(
                LocalDate.of(1999, 5, 10)
        );
        updateRequest.setEmail("john@hospital.com");
        updateRequest.setPhoneNumber("9999999999");
        updateRequest.setGender(Gender.MALE);

        PatientResponseDTO response =
                patientService.updatePatient(
                        1L,
                        updateRequest
                );

        ArgumentCaptor<Patient> captor =
                ArgumentCaptor.forClass(Patient.class);

        verify(patientRepository)
                .save(captor.capture());

        Patient updatedPatient =
                captor.getValue();

        assertEquals(
                "John Updated",
                updatedPatient.getFullName()
        );

        assertEquals(
                LocalDate.of(1999, 5, 10),
                updatedPatient.getDateOfBirth()
        );

        assertEquals(
                "9999999999",
                updatedPatient.getPhoneNumber()
        );

        assertEquals(
                "john@hospital.com",
                updatedPatient.getEmail()
        );

        assertEquals(
                1L,
                response.getId()
        );

        assertEquals(
                "John Updated",
                response.getFullName()
        );

        verify(patientRepository)
                .findById(1L);

        verify(patientRepository)
                .save(any(Patient.class));
    }

    @Test
    void updatePatient_ShouldThrowException_WhenPatientNotFound() {

        when(patientRepository.findById(1L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> patientService.updatePatient(
                                1L,
                                request
                        )
                );

        assertEquals(
                "Patient not found with id : 1",
                exception.getMessage()
        );

        verify(patientRepository, never())
                .save(any(Patient.class));
    }

    @Test
    void updatePatient_ShouldThrowException_WhenEmailAlreadyExists() {

        Patient existingPatient = new Patient(
                "John Doe",
                LocalDate.of(2000, 1, 1),
                "old@hospital.com",
                "9876543210",
                Gender.MALE
        );

        ReflectionTestUtils.setField(
                existingPatient,
                "id",
                1L
        );

        PatientRequestDTO updateRequest =
                new PatientRequestDTO();

        updateRequest.setFullName("John Doe");
        updateRequest.setDateOfBirth(
                LocalDate.of(2000, 1, 1)
        );
        updateRequest.setEmail("new@hospital.com");
        updateRequest.setPhoneNumber("9876543210");
        updateRequest.setGender(Gender.MALE);

        when(patientRepository.findById(1L))
                .thenReturn(Optional.of(existingPatient));

        when(patientRepository.existsByEmail("new@hospital.com"))
                .thenReturn(true);

        DuplicateResourceException exception =
                assertThrows(
                        DuplicateResourceException.class,
                        () -> patientService.updatePatient(
                                1L,
                                updateRequest
                        )
                );

        assertEquals(
                "Patient with this email already exists.",
                exception.getMessage()
        );

        verify(patientRepository)
                .findById(1L);

        verify(patientRepository)
                .existsByEmail("new@hospital.com");

        verify(patientRepository, never())
                .save(any(Patient.class));
    }

    @Test
    void deletePatient_ShouldDeletePatient_WhenPatientExists() {

        when(patientRepository.findById(1L))
                .thenReturn(Optional.of(patient));

        patientService.deletePatient(1L);

        verify(patientRepository)
                .findById(1L);

        verify(patientRepository)
                .delete(patient);
    }

    @Test
    void deletePatient_ShouldThrowException_WhenPatientNotFound() {

        when(patientRepository.findById(1L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> patientService.deletePatient(1L)
                );

        assertEquals(
                "Patient not found with id : 1",
                exception.getMessage()
        );

        verify(patientRepository)
                .findById(1L);

        verify(patientRepository, never())
                .delete(any(Patient.class));
    }

}
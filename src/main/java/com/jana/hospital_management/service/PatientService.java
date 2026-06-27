package com.jana.hospital_management.service;

import com.jana.hospital_management.dto.PageResponse;
import com.jana.hospital_management.dto.PatientFilterRequest;
import com.jana.hospital_management.dto.PatientRequestDTO;
import com.jana.hospital_management.dto.PatientResponseDTO;
import com.jana.hospital_management.entity.Gender;
import com.jana.hospital_management.entity.Patient;
import com.jana.hospital_management.exception.DuplicateResourceException;
import com.jana.hospital_management.exception.ResourceNotFoundException;
import com.jana.hospital_management.repository.PatientRepository;
import com.jana.hospital_management.specification.PatientSpecification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class PatientService {

    private static final Logger logger =
            LoggerFactory.getLogger(PatientService.class);

    private final PatientRepository patientRepository;
    private final PaginationService paginationService;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id",
            "fullName",
            "dateOfBirth",
            "email",
            "phoneNumber",
            "gender"
    );

    public PatientService(
            PatientRepository patientRepository,
            PaginationService paginationService
    ) {
        this.patientRepository = patientRepository;
        this.paginationService = paginationService;
    }

    // CREATE
    @Transactional
    public PatientResponseDTO createPatient(PatientRequestDTO dto) {

        if (patientRepository.existsByEmail(dto.getEmail().toLowerCase())) {

            logger.warn(
                    "Patient creation failed: email '{}' already exists.",
                    dto.getEmail()
            );

            throw new DuplicateResourceException(
                    "Patient with this email already exists"
            );
        }

        Patient patient = new Patient(
                dto.getFullName(),
                dto.getDateOfBirth(),
                dto.getEmail(),
                dto.getPhoneNumber(),
                dto.getGender()
        );

        Patient saved = patientRepository.save(patient);

        logger.info(
                "Patient '{}' (ID={}) created.",
                saved.getFullName(),
                saved.getId()
        );

        return mapToDTO(saved);
    }

    // GET BY ID
    @Transactional(readOnly = true)
    public PatientResponseDTO getPatientById(Long id) {

        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> {

                    logger.warn(
                            "Patient {} not found.",
                            id
                    );

                    return new ResourceNotFoundException(
                            "Patient not found with id : " + id
                    );
                });

        return mapToDTO(patient);
    }

    // GET ALL
    @Transactional(readOnly = true)
    public PageResponse<PatientResponseDTO> getAllPatients(
            PatientFilterRequest filter,
            int page,
            int size,
            String sortBy,
            String direction
    ) {

        Pageable pageable = paginationService.createPageable(
                page,
                size,
                sortBy,
                direction,
                ALLOWED_SORT_FIELDS
        );

        String name = filter != null ? filter.getName() : null;
        String email = filter != null ? filter.getEmail() : null;
        Gender gender = filter != null ? filter.getGender() : null;

        Specification<Patient> spec =
                PatientSpecification.withFilters(
                        name,
                        email,
                        gender
                );

        Page<PatientResponseDTO> pageResult =
                patientRepository.findAll(spec, pageable)
                        .map(this::mapToDTO);

        return new PageResponse<>(pageResult);
    }

    // UPDATE
    @Transactional
    public PatientResponseDTO updatePatient(
            Long id,
            PatientRequestDTO dto
    ) {

        Patient existingPatient = patientRepository.findById(id)
                .orElseThrow(() -> {

                    logger.warn(
                            "Patient update failed: patient {} not found.",
                            id
                    );

                    return new ResourceNotFoundException(
                            "Patient not found with id : " + id
                    );
                });

        if (!existingPatient.getEmail().equalsIgnoreCase(dto.getEmail())) {

            if (patientRepository.existsByEmail(
                    dto.getEmail().toLowerCase()
            )) {

                logger.warn(
                        "Patient update failed: email '{}' already exists.",
                        dto.getEmail()
                );

                throw new DuplicateResourceException(
                        "Patient with this email already exists."
                );
            }
        }

        existingPatient.updateDetails(
                dto.getFullName(),
                dto.getDateOfBirth(),
                dto.getEmail(),
                dto.getPhoneNumber(),
                dto.getGender()
        );

        Patient updated = patientRepository.save(existingPatient);

        logger.info(
                "Patient '{}' (ID={}) updated.",
                updated.getFullName(),
                updated.getId()
        );

        return mapToDTO(updated);
    }

    // DELETE
    @Transactional
    public void deletePatient(Long id) {

        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> {

                    logger.warn(
                            "Patient deletion failed: patient {} not found.",
                            id
                    );

                    return new ResourceNotFoundException(
                            "Patient not found with id : " + id
                    );
                });

        patientRepository.delete(patient);

        logger.info(
                "Patient '{}' (ID={}) deleted.",
                patient.getFullName(),
                patient.getId()
        );
    }

    // DTO Mapper
    private PatientResponseDTO mapToDTO(Patient patient) {

        return new PatientResponseDTO(
                patient.getId(),
                patient.getFullName(),
                patient.getDateOfBirth(),
                patient.getEmail(),
                patient.getPhoneNumber(),
                patient.getGender()
        );
    }
}
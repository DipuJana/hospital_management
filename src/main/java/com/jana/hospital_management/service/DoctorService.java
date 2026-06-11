package com.jana.hospital_management.service;

import com.jana.hospital_management.dto.DoctorRequestDTO;
import com.jana.hospital_management.dto.DoctorResponseDTO;
import com.jana.hospital_management.dto.PageResponse;
import com.jana.hospital_management.entity.Doctor;
import com.jana.hospital_management.exception.DuplicateResourceException;
import com.jana.hospital_management.exception.ResourceNotFoundException;
import com.jana.hospital_management.repository.DoctorRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final PaginationService paginationService;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id",
            "fullName",
            "specialization",
            "email",
            "phoneNumber"
    );

    public DoctorService(
            DoctorRepository doctorRepository,
            PaginationService paginationService
    ) {
        this.doctorRepository = doctorRepository;
        this.paginationService = paginationService;
    }

    // CREATE
    @Transactional
    public DoctorResponseDTO createDoctor(DoctorRequestDTO dto) {

        if (doctorRepository.existsByEmail(dto.getEmail().toLowerCase())) {
            throw new DuplicateResourceException(
                    "Doctor with this email already exists"
            );
        }

        Doctor doctor = new Doctor(
                dto.getFullName(),
                dto.getSpecialization(),
                dto.getEmail(),
                dto.getPhoneNumber()
        );

        Doctor saved = doctorRepository.save(doctor);

        return mapToDTO(saved);
    }

    // GET BY ID
    @Transactional(readOnly = true)
    public DoctorResponseDTO getDoctorById(Long id) {

        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Doctor not found with id : " + id
                        )
                );

        return mapToDTO(doctor);
    }

    // GET ALL
    @Transactional(readOnly = true)
    public PageResponse<DoctorResponseDTO> getAllDoctors(
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

        Page<DoctorResponseDTO> pageResult =
                doctorRepository.findAll(pageable)
                        .map(this::mapToDTO);

        return new PageResponse<>(pageResult);
    }

    // UPDATE
    @Transactional
    public DoctorResponseDTO updateDoctor(
            Long id,
            DoctorRequestDTO dto
    ) {

        Doctor existingDoctor = doctorRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Doctor not found with id : " + id
                        )
                );

        if (!existingDoctor.getEmail().equalsIgnoreCase(dto.getEmail())) {

            if (doctorRepository.existsByEmail(
                    dto.getEmail().toLowerCase()
            )) {

                throw new DuplicateResourceException(
                        "Doctor with this email already exists"
                );
            }
        }

        existingDoctor.updateDetails(
                dto.getFullName(),
                dto.getSpecialization(),
                dto.getEmail(),
                dto.getPhoneNumber()
        );

        Doctor updated = doctorRepository.save(existingDoctor);

        return mapToDTO(updated);
    }

    // DELETE
    @Transactional
    public void deleteDoctor(Long id) {

        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Doctor not found with id : " + id
                        )
                );

        doctorRepository.delete(doctor);
    }

    private DoctorResponseDTO mapToDTO(Doctor doctor) {

        return new DoctorResponseDTO(
                doctor.getId(),
                doctor.getFullName(),
                doctor.getSpecialization(),
                doctor.getEmail(),
                doctor.getPhoneNumber()
        );
    }
}
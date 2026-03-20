package com.jana.hospital_management.service;

import com.jana.hospital_management.dto.PageResponse;
import com.jana.hospital_management.dto.PatientRequestDTO;
import com.jana.hospital_management.dto.PatientResponseDTO;
import com.jana.hospital_management.entity.Patient;
import com.jana.hospital_management.exception.DuplicateResourceException;
import com.jana.hospital_management.repository.PatientRepository;
import com.jana.hospital_management.exception.ResourceNotFoundException;

import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class PatientService {

    private final PatientRepository patientRepository;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id",
            "fullName",
            "dateOfBirth",
            "email",
            "phoneNumber",
            "gender"
    );

    public PatientService(PatientRepository patientRepository){

        this.patientRepository = patientRepository;
    }

    //CREATE
    @Transactional
    public PatientResponseDTO createPatient(PatientRequestDTO dto){

        if(patientRepository.existsByEmail(dto.getEmail().toLowerCase())){
            throw new DuplicateResourceException("Patient with this email already exists");
        }

        Patient patient = new Patient(
                dto.getFullName(),
                dto.getDateOfBirth(),
                dto.getEmail(),
                dto.getPhoneNumber(),
                dto.getGender()
        );

        Patient saved = patientRepository.save(patient);
        return mapToDTO(saved);
    }

    //GET BY ID
    @Transactional(readOnly = true)
    public PatientResponseDTO getPatientById(Long id){

        Patient patient =  patientRepository.findById(id)
                .orElseThrow( () -> new ResourceNotFoundException("Patient not found with id : " + id )
                );
        return mapToDTO(patient);
    }

    //GET ALL
    @Transactional(readOnly = true)
    public PageResponse<PatientResponseDTO> getAllPatients(
            int page,
            int size,
            String sortBy,
            String direction
    ){
        //validate page
        if(page < 0){
            throw new IllegalArgumentException("Page number can not be negative");
        }
        //validate size
        if(size <= 0 || size > 50){
            throw new IllegalArgumentException("Page size must be between 1 and 50");
        }
        //validate sort field
        if(!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new IllegalArgumentException(
                    "Invalid sort field: " + sortBy + ". Allowed fields: " + ALLOWED_SORT_FIELDS
            );
        }

        //validate direction
        Sort.Direction sortDirection;
        try {
            sortDirection = Sort.Direction.fromString(direction.trim());
        } catch (IllegalArgumentException ex){
            throw new IllegalArgumentException("Invalid sort direction: " + direction);
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(sortDirection, sortBy)
        );

        Page<PatientResponseDTO> pageResult = patientRepository.findAll(pageable).map(this::mapToDTO);

        return new PageResponse<>(pageResult);
    }

    //UPDATE
    @Transactional
    public PatientResponseDTO updatePatient(Long id, PatientRequestDTO dto) {

        Patient existingPatient = patientRepository.findById(id)
                .orElseThrow( () ->
                        new ResourceNotFoundException("Patient not found with id : " + id)
                );

        if(!existingPatient.getEmail().equalsIgnoreCase(dto.getEmail())) {
            if (patientRepository.existsByEmail(dto.getEmail().toLowerCase())) {
                throw new DuplicateResourceException("Patient with this email already exists.");
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
        return mapToDTO(updated);
    }

    //DELETE
    @Transactional
    public void deletePatient(Long id){
        Patient patient = patientRepository.findById(id)
                .orElseThrow( () ->
                        new ResourceNotFoundException("Patient not found with id : " + id)
                );
        patientRepository.delete(patient);
    }

    private PatientResponseDTO mapToDTO(Patient patient){
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

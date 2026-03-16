package com.jana.hospital_management.service;

import com.jana.hospital_management.dto.PatientRequestDTO;
import com.jana.hospital_management.dto.PatientResponseDTO;
import com.jana.hospital_management.entity.Patient;
import com.jana.hospital_management.exception.DuplicateResourceException;
import com.jana.hospital_management.repository.PatientRepository;
import com.jana.hospital_management.exception.ResourceNotFoundException;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PatientService {

    private final PatientRepository patientRepository;

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
    public List<PatientResponseDTO> getAllPatients(){

        return patientRepository.findAll()
                .stream()
                .map(this :: mapToDTO)
                .collect(Collectors.toList());
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

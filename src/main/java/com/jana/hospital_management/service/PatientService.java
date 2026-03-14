package com.jana.hospital_management.service;

import com.jana.hospital_management.entity.Patient;
import com.jana.hospital_management.entity.Gender;
import com.jana.hospital_management.exception.DuplicateResourceException;
import com.jana.hospital_management.repository.PatientRepository;
import com.jana.hospital_management.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class PatientService {

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository){
        this.patientRepository = patientRepository;
    }

    //CREATE
    @Transactional
    public Patient createPatient(
            String fullName,
            LocalDate dateOfBirth,
            String email,
            String phoneNumber,
            Gender gender
    ){
        if(patientRepository.existsByEmail(email.toLowerCase())){
            throw new DuplicateResourceException("Patient with this email already exists");
        }

        Patient patient = new Patient(
                fullName,
                dateOfBirth,
                email,
                phoneNumber,
                gender
        );
        return patientRepository.save(patient);
    }

    //GET BY ID
    @Transactional(readOnly = true)
    public Patient getPatientById(Long id){

        return patientRepository.findById(id)
                .orElseThrow( () -> new ResourceNotFoundException("Patient not found with id : " + id )
                );
    }

    //GET ALL
    @Transactional(readOnly = true)
    public List<Patient> getAllPatients(){
        return patientRepository.findAll();
    }

    //UPDATE
    @Transactional
    public Patient updatePatient(
            Long id,
            String fullName,
            LocalDate dateOfBirth,
            String email,
            String phoneNumber,
            Gender gender
    ) {

        Patient existingPatient = patientRepository.findById(id)
                .orElseThrow( () ->
                        new ResourceNotFoundException("Patient not found with id : " + id)
                );

        if(!existingPatient.getEmail().equalsIgnoreCase(email)) {
            if (patientRepository.existsByEmail(email.toLowerCase())) {
                throw new DuplicateResourceException("Patient with this email already exists.");
            }
        }

        existingPatient.updateDetails(
                fullName,
                dateOfBirth,
                email,
                phoneNumber,
                gender
        );
        return patientRepository.save(existingPatient);
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
}

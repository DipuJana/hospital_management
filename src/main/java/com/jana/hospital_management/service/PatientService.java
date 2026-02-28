package com.jana.hospital_management.service;

import com.jana.hospital_management.entity.Patient;
import com.jana.hospital_management.entity.Gender;
import com.jana.hospital_management.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class PatientService {

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository){
        this.patientRepository = patientRepository;
    }

    @Transactional
    public Patient createPatient(
            String fullName,
            LocalDate dateOfBirth,
            String email,
            String phoneNumber,
            Gender gender
    ){
        if(patientRepository.existsByEmail(email.toLowerCase())){
            throw new IllegalArgumentException("Patient with this email already exists");
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
}

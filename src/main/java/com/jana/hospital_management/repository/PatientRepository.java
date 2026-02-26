package com.jana.hospital_management.repository;

import com.jana.hospital_management.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    boolean existsByEmail(String email);
    Optional<Patient> findByEmail(String email);

}

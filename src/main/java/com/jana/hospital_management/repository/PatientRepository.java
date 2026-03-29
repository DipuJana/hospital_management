package com.jana.hospital_management.repository;

import com.jana.hospital_management.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long>, JpaSpecificationExecutor<Patient> {

    boolean existsByEmail(String email);
    Optional<Patient> findByEmail(String email);

}

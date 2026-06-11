package com.jana.hospital_management.dto;

import com.jana.hospital_management.entity.Specialization;

public class DoctorResponseDTO {

    private Long id;
    private String fullName;
    private Specialization specialization;
    private String email;
    private String phoneNumber;

    public DoctorResponseDTO(
            Long id,
            String fullName,
            Specialization specialization,
            String email,
            String phoneNumber
    ) {
        this.id = id;
        this.fullName = fullName;
        this.specialization = specialization;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public Specialization getSpecialization() {
        return specialization;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
}
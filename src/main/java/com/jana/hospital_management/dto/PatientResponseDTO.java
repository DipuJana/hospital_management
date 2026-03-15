package com.jana.hospital_management.dto;

import com.jana.hospital_management.entity.Gender;

import java.time.LocalDate;

public class PatientResponseDTO {

    private final Long id;
    private final String fullName;
    private final LocalDate dateOfBirth;
    private final String email;
    private final String phoneNumber;
    private final Gender gender;

    public PatientResponseDTO(
            Long id,
            String fullName,
            LocalDate dateOfBirth,
            String email,
            String phoneNumber,
            Gender gender
    ) {
        this.id = id;
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.gender = gender;
    }

    public Long getId(){
        return id;
    }

    public String getFullName(){
        return fullName;
    }

    public LocalDate getDateOfBirth(){
        return dateOfBirth;
    }

    public String getEmail(){
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public Gender getGender() {
        return gender;
    }
}

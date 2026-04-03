package com.jana.hospital_management.dto;

import com.jana.hospital_management.entity.Gender;

import java.time.LocalDate;

public class PatientResponseDTO {

    private Long id;
    private String fullName;
    private LocalDate dateOfBirth;
    private String email;
    private String phoneNumber;
    private Gender gender;

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

    //Default constructor
    public PatientResponseDTO() { }

    //GETTERS
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

    //SETTERS
    public void setId(Long id) {
        this.id = id;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }
}

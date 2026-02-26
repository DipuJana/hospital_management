package com.jana.hospital_management.entity;

import jakarta.persistence.*;
import jakarta.validation.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;

@Entity
@Table(
        name = "patients",
        indexes = {
                @Index(name="idx_patient_email", columnList = "email", unique = true)
        }
)
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  Long id;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String fullName;

    @NotNull
    @Past
    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @NotBlank
    @Email
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @NotBlank
    @Column(nullable = false, length = 15)
    private  String phoneNumber;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private  Gender gender;

    protected Patient(){
        //JPA requires a non-arg constructor
    }

    public Patient(
            String fullName,
            LocalDate dateOfBirth,
            String email,
            String phoneNumber,
            Gender gender
    ) {
        validateDateOfBirth(dateOfBirth);

        this.fullName = fullName.trim();
        this.dateOfBirth = dateOfBirth;
        this.email = normalizeEmail(email);
        this.phoneNumber = phoneNumber;
        this.gender = gender;
    }

    private void validateDateOfBirth(LocalDate dob){
        if(dob.isAfter(LocalDate.now())){
            throw new IllegalArgumentException("Date of birth can not be in futre");
        }
        int age = Period.between(dob, LocalDate.now()).getYears();
        if(age>120){
            throw new IllegalArgumentException("Age exceeds realistic human limits");
        }
    }

    private String normalizeEmail(String email){
        return email.trim().toLowerCase();
    }

    private Long getId(){
        return id;
    }

    private String getFullName(){
        return fullName;
    }

    private LocalDate getDateOfBirth(){
        return dateOfBirth;
    }

    private String getEmail(){
        return email;
    }

    private String getPhoneNumber(){
        return phoneNumber;
    }

    private Gender getGender(){
        return gender;
    }
}


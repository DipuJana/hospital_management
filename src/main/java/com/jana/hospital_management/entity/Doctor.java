package com.jana.hospital_management.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Entity
@Table(
        name = "doctors",
        indexes = {
                @Index(name = "idx_doctor_email", columnList = "email", unique = true)
        }
)
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String fullName;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Specialization specialization;

    @NotBlank
    @Email
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @NotBlank
    @Pattern(regexp = "^[0-9]{10}$", message = "Invalid phone number")
    @Column(nullable = false, length = 15)
    private String phoneNumber;

    protected Doctor() {
        // JPA requires a no-arg constructor
    }

    public Doctor(
            String fullName,
            Specialization specialization,
            String email,
            String phoneNumber
    ) {
        this.fullName = normalizeName(fullName);
        this.specialization = specialization;
        this.email = normalizeEmail(email);
        this.phoneNumber = phoneNumber;
    }

    // UPDATE METHOD
    public void updateDetails(
            String fullName,
            Specialization specialization,
            String email,
            String phoneNumber
    ) {
        this.fullName = normalizeName(fullName);
        this.specialization = specialization;
        this.email = normalizeEmail(email);
        this.phoneNumber = phoneNumber;
    }

    // NORMALIZATION METHODS
    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private String normalizeName(String name) {
        return name.trim();
    }

    // GETTERS (READ ONLY)
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
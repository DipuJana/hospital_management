package com.jana.hospital_management.dto;

import com.jana.hospital_management.entity.Gender;

public class PatientFilterRequest {

    private String name;
    private String email;
    private Gender gender;

    public PatientFilterRequest() { }

    public String getName() { return name; }
    public String getEmail() { return email; }
    public Gender getGender() { return gender; }

    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setGender(Gender gender) { this.gender = gender; }

}

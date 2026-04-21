package com.jana.hospital_management.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Future;

import java.time.LocalDateTime;

public class AppointmentRequestDTO {

    @NotNull(message = "Patient ID is required.")
    private Long patientId;

    @NotNull(message = "Doctor ID is required.")
    private Long doctorId;

    @NotNull(message = "Appointment time is required.")
    @Future(message = "Appointment time must be in future.")
    private LocalDateTime appointmentDateTime;

    private String reason;

    // Getters
    public Long getPatientId(){
        return patientId;
    }
    public Long getDoctorId(){
        return doctorId;
    }
    public LocalDateTime getAppointmentDateTime(){
        return appointmentDateTime;
    }
    public String getReason(){
        return reason;
    }

    // Setters
    public void setPatientId(Long patientId){
        this.patientId = patientId;
    }
    public void setDoctorId(Long doctorId){
        this.doctorId = doctorId;
    }
    public void setAppointmentDateTime(LocalDateTime appointmentDateTime){
        this.appointmentDateTime = appointmentDateTime;
    }
    public void setReason(String reason){
        this.reason = reason;
    }

}

package com.jana.hospital_management.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "appointments",
        indexes = {
                @Index(name = "idx_appointment_doctor_time_status", columnList = "doctor_id, appointment_datetime, status"),
                @Index(name = "idx_appointment_patient", columnList = "patient_id")
        }
)

public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // RELATIONSHIPS

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    // Appointment Details
    @NotNull
    @Column(name = "appointment_datetime", nullable = false)
    private LocalDateTime appointmentDateTime;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AppointmentStatus status;

    @Column(length = 255)
    private String reason;

    protected Appointment(){

    }

    public Appointment(
            Patient patient,
            Doctor doctor,
            LocalDateTime appointmentDateTime,
            String reason
    ) {
        validateAppointmentTime(appointmentDateTime);

        this.patient = patient;
        this.doctor = doctor;
        this.appointmentDateTime = appointmentDateTime;
        this.reason = normalizeReason(reason);
        this.status = AppointmentStatus.SCHEDULED;
    }

    //Update method
    public void reschedule(LocalDateTime newDateTime){
        if(this.status != AppointmentStatus.SCHEDULED){
            throw new IllegalStateException("Only scheduled appointments can be rescheduled.");
        }
        validateAppointmentTime(newDateTime);
        this.appointmentDateTime = newDateTime;
    }

    public void cancel(){
        if(this.status != AppointmentStatus.SCHEDULED){
            throw new IllegalStateException("Only scheduled appointments can be cancelled");
        }
        this.status = AppointmentStatus.CANCELLED;
    }

    public void complete(){
        if(this.status != AppointmentStatus.SCHEDULED){
            throw new IllegalStateException("Only scheduled appointments can be completed.");
        }
        this.status = AppointmentStatus.COMPLETED;
    }

    private void validateAppointmentTime(LocalDateTime dateTime){
        if(dateTime.isBefore(LocalDateTime.now())){
            throw new IllegalArgumentException("Appointment time cannot be in the past.");
        }
    }

    private String normalizeReason(String reason){
        return reason == null ? null : reason.trim();
    }

    //Getters

    public Long getId(){
        return id;
    }

    public Patient getPatient() {
        return patient;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public LocalDateTime getAppointmentDateTime() {
        return appointmentDateTime;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }
}

package com.jana.hospital_management.dto;

import com.jana.hospital_management.entity.AppointmentStatus;

import java.time.LocalDateTime;

public class AppointmentResponseDTO {

    private final Long id;

    private final Long patientId;
    private final String patientName;

    private final Long doctorId;
    private final String doctorName;

    private final LocalDateTime appointmentDateTime;
    private final AppointmentStatus status;
    private final String reason;

    public AppointmentResponseDTO(
            Long id,
            Long patientId,
            String patientName,
            Long doctorId,
            String doctorName,
            LocalDateTime appointmentDateTime,
            AppointmentStatus status,
            String reason
    ) {
        this.id = id;
        this.patientId = patientId;
        this.patientName = patientName;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.appointmentDateTime = appointmentDateTime;
        this.status = status;
        this.reason = reason;
    }

    // Getters
    public Long getId(){
        return id;
    }
    public Long getPatientId(){
        return patientId;
    }
    public String getPatientName(){
        return patientName;
    }
    public Long getDoctorId(){
        return doctorId;
    }
    public String getDoctorName(){
        return doctorName;
    }
    public LocalDateTime getAppointmentDateTime(){
        return appointmentDateTime;
    }
    public AppointmentStatus getStatus(){
        return status;
    }
    public String getReason(){
        return reason;
    }

}

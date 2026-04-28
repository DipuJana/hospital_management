package com.jana.hospital_management.controller;

import com.jana.hospital_management.dto.AppointmentRequestDTO;
import com.jana.hospital_management.dto.AppointmentResponseDTO;
import com.jana.hospital_management.service.AppointmentService;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService){
        this.appointmentService = appointmentService;
    }

    // 1. Create Appointment
    @PostMapping
    public ResponseEntity<AppointmentResponseDTO> createAppointment(
            @Valid @RequestBody AppointmentRequestDTO request){
        AppointmentResponseDTO response = appointmentService.createAppointment(request);

        return ResponseEntity
                .created(URI.create("/api/appointments/" + response.getId()))
                .body(response);
    }

    // 2. Cancel Appointment
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<AppointmentResponseDTO> cancelAppointment(
            @PathVariable Long id
    ){
        return ResponseEntity.ok(
                appointmentService.cancelAppointment(id)
        );
    }

    // 3. Complete Appointment
    @PatchMapping("/{id}/complete")
    public ResponseEntity<AppointmentResponseDTO> completeAppointment(
            @PathVariable Long id
    ){
        return ResponseEntity.ok(
                appointmentService.completeAppointment(id)
        );
    }

    // 4. Reschedule Appointment
    @PatchMapping("/{id}/reschedule")
    public ResponseEntity<AppointmentResponseDTO> rescheduleAppointment(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime newDateTime
    ){
        return ResponseEntity.ok(
                appointmentService.rescheduleAppointment(id, newDateTime)
        );
    }

    // 5.  Get Appointments by patients
    @GetMapping("/patients/{patientId}")
    public ResponseEntity<Page<AppointmentResponseDTO>> getByPatient(
            @PathVariable Long patientId,
            @PageableDefault(size = 10, sort = "appointmentDateTime") Pageable pageable
    ) {
        return ResponseEntity.ok(
                appointmentService.getAppointmentsByPatient(patientId,  pageable)
        );
    }

    // 6. Get Appointments by doctors
    @GetMapping("/doctors/{doctorId}")
    public ResponseEntity<Page<AppointmentResponseDTO>> getByDoctor(
            @PathVariable Long doctorId,
            @PageableDefault(size = 10, sort = "appointmentDateTime") Pageable pageable
    ) {
        return ResponseEntity.ok(
                appointmentService.getAppointmentsByDoctor(doctorId,  pageable)
        );
    }

}

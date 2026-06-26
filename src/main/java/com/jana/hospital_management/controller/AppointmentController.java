package com.jana.hospital_management.controller;

import com.jana.hospital_management.dto.AppointmentRequestDTO;
import com.jana.hospital_management.dto.AppointmentResponseDTO;
import com.jana.hospital_management.dto.PageResponse;
import com.jana.hospital_management.service.AppointmentService;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    // Create Appointment
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    public ResponseEntity<AppointmentResponseDTO> createAppointment(
            @Valid @RequestBody AppointmentRequestDTO request
    ) {

        AppointmentResponseDTO response =
                appointmentService.createAppointment(request);

        return ResponseEntity
                .created(URI.create("/api/appointments/" + response.getId()))
                .body(response);
    }

    // Cancel Appointment
    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    public ResponseEntity<AppointmentResponseDTO> cancelAppointment(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                appointmentService.cancelAppointment(id)
        );
    }

    // Complete Appointment
    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    public ResponseEntity<AppointmentResponseDTO> completeAppointment(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                appointmentService.completeAppointment(id)
        );
    }

    // Reschedule Appointment
    @PatchMapping("/{id}/reschedule")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    public ResponseEntity<AppointmentResponseDTO> rescheduleAppointment(
            @PathVariable Long id,
            @RequestParam("newDateTime")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime newDateTime
    ) {
        return ResponseEntity.ok(
                appointmentService.rescheduleAppointment(id, newDateTime)
        );
    }

    // Get Appointments By Patient
    @GetMapping("/patients/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','DOCTOR')")
    public ResponseEntity<PageResponse<AppointmentResponseDTO>> getByPatient(
            @PathVariable Long patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "appointmentDateTime") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        return ResponseEntity.ok(
                appointmentService.getAppointmentsByPatient(
                        patientId,
                        page,
                        size,
                        sortBy,
                        direction
                )
        );
    }

    // Get Appointments By Doctor
    @GetMapping("/doctors/{doctorId}")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','DOCTOR')")
    public ResponseEntity<PageResponse<AppointmentResponseDTO>> getByDoctor(
            @PathVariable Long doctorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "appointmentDateTime") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        return ResponseEntity.ok(
                appointmentService.getAppointmentsByDoctor(
                        doctorId,
                        page,
                        size,
                        sortBy,
                        direction
                )
        );
    }
}

package com.jana.hospital_management.controller;

import com.jana.hospital_management.dto.DoctorRequestDTO;
import com.jana.hospital_management.dto.DoctorResponseDTO;
import com.jana.hospital_management.dto.PageResponse;
import com.jana.hospital_management.service.DoctorService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    // CREATE
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DoctorResponseDTO> createDoctor(
            @Valid @RequestBody DoctorRequestDTO request
    ) {

        DoctorResponseDTO saved =
                doctorService.createDoctor(request);

        return ResponseEntity.status(201).body(saved);
    }

    // GET BY ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','DOCTOR')")
    public ResponseEntity<DoctorResponseDTO> getDoctorById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                doctorService.getDoctorById(id)
        );
    }

    // GET ALL
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','DOCTOR')")
    public ResponseEntity<PageResponse<DoctorResponseDTO>> getAllDoctors(

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size,

            @RequestParam(defaultValue = "id")
            String sortBy,

            @RequestParam(defaultValue = "asc")
            String direction
    ) {

        PageResponse<DoctorResponseDTO> response =
                doctorService.getAllDoctors(
                        page,
                        size,
                        sortBy,
                        direction
                );

        return ResponseEntity.ok(response);
    }

    // UPDATE
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DoctorResponseDTO> updateDoctor(

            @PathVariable Long id,

            @Valid
            @RequestBody DoctorRequestDTO request
    ) {

        return ResponseEntity.ok(
                doctorService.updateDoctor(id, request)
        );
    }

    // DELETE
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteDoctor(
            @PathVariable Long id
    ) {

        doctorService.deleteDoctor(id);

        return ResponseEntity.noContent().build();
    }
}
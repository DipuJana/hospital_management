package com.jana.hospital_management.controller;

import com.jana.hospital_management.dto.DoctorRequestDTO;
import com.jana.hospital_management.dto.DoctorResponseDTO;
import com.jana.hospital_management.dto.PageResponse;
import com.jana.hospital_management.service.DoctorService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<DoctorResponseDTO> createDoctor(
            @Valid @RequestBody DoctorRequestDTO request
    ) {

        DoctorResponseDTO saved =
                doctorService.createDoctor(request);

        return ResponseEntity.status(201).body(saved);
    }

    // GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<DoctorResponseDTO> getDoctorById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                doctorService.getDoctorById(id)
        );
    }

    // GET ALL
    @GetMapping
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
    public ResponseEntity<Void> deleteDoctor(
            @PathVariable Long id
    ) {

        doctorService.deleteDoctor(id);

        return ResponseEntity.noContent().build();
    }
}
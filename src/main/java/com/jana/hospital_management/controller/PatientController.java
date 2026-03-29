package com.jana.hospital_management.controller;

import com.jana.hospital_management.dto.PageResponse;
import com.jana.hospital_management.dto.PatientFilterRequest;
import com.jana.hospital_management.dto.PatientRequestDTO;
import com.jana.hospital_management.dto.PatientResponseDTO;
import com.jana.hospital_management.service.PatientService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService){
        this.patientService = patientService;
    }

    //CREATE
    @PostMapping
    public ResponseEntity<PatientResponseDTO> createPatient(@Valid @RequestBody PatientRequestDTO request){
        PatientResponseDTO saved = patientService.createPatient(request);
        return ResponseEntity.status(201).body(saved);
    }

    //GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<PatientResponseDTO> getPatientById(@PathVariable Long id){
        return ResponseEntity.ok(patientService.getPatientById(id));
    }

    //GET ALL
    @GetMapping
    public ResponseEntity<PageResponse<PatientResponseDTO>> getAllPatients(
            @ModelAttribute PatientFilterRequest filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ){
        PageResponse<PatientResponseDTO> response = patientService.getAllPatients(filter, page, size, sortBy, direction);
        return ResponseEntity.ok(response);
    }

    //UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<PatientResponseDTO> updatePatient(
            @PathVariable Long id,
            @Valid @RequestBody PatientRequestDTO request
    ) {


        return ResponseEntity.ok(patientService.updatePatient(id, request));
    }

    //DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePatient(@PathVariable Long id){
        patientService.deletePatient(id);

        return ResponseEntity.noContent().build();
    }
}

package com.jana.hospital_management.controller;

import com.jana.hospital_management.dto.AuthResponseDTO;
import com.jana.hospital_management.dto.LoginRequestDTO;
import com.jana.hospital_management.dto.RegisterRequestDTO;
import com.jana.hospital_management.service.AuthService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> register(
            @Valid
            @RequestBody RegisterRequestDTO request
    ) {

        authService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO request
    ) {

        AuthResponseDTO response =
                authService.login(request);

        return ResponseEntity.ok(response);
    }
}
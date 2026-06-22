package com.jana.hospital_management.controller;

import com.jana.hospital_management.dto.AuthResponseDTO;
import com.jana.hospital_management.dto.LoginRequestDTO;
import com.jana.hospital_management.service.AuthService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
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
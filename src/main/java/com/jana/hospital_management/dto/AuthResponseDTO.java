package com.jana.hospital_management.dto;

public record AuthResponseDTO(
        String token,
        String email,
        String role
) {}
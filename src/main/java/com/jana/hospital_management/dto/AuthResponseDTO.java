package com.jana.hospital_management.dto;

import com.jana.hospital_management.entity.Role;

public record AuthResponseDTO(
        String token,
        String email,
        Role role
) {}
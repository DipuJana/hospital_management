package com.jana.hospital_management.service;

import com.jana.hospital_management.dto.AuthResponseDTO;
import com.jana.hospital_management.dto.LoginRequestDTO;
import com.jana.hospital_management.entity.User;
import com.jana.hospital_management.repository.UserRepository;

import com.jana.hospital_management.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public AuthService(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtService = jwtService;

    }

    @Transactional(readOnly = true)
    public AuthResponseDTO login(LoginRequestDTO request) {

        try {

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email(),
                            request.password()
                    )
            );

        } catch (AuthenticationException ex) {
            throw new IllegalArgumentException(
                    "Invalid email or password"
            );
        }

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Invalid email or password"
                        )
                );

        String token = jwtService.generateToken(user.getEmail());

        return new AuthResponseDTO(
                token, // JWT token comes later
                user.getEmail(),
                user.getRole()
        );
    }
}
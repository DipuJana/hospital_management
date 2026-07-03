package com.jana.hospital_management.service;

import com.jana.hospital_management.dto.AuthResponseDTO;
import com.jana.hospital_management.dto.LoginRequestDTO;
import com.jana.hospital_management.dto.RegisterRequestDTO;
import com.jana.hospital_management.entity.User;
import com.jana.hospital_management.repository.UserRepository;
import com.jana.hospital_management.exception.DuplicateResourceException;

import com.jana.hospital_management.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    private static final Logger logger =
            LoggerFactory.getLogger(AuthService.class);

    public AuthService(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            JwtService jwtService,
            PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;

    }

    @Transactional
    public void register(RegisterRequestDTO request) {

        if (userRepository.existsByEmail(request.email())) {

            logger.warn(
                    "Registration failed: email '{}' already exists.",
                    request.email()
            );
            throw new DuplicateResourceException(
                    "Email already exists"
            );
        }

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(request.role())
                .build();

        userRepository.save(user);

        logger.info(
                "Created {} account for '{}'.",
                user.getRole(),
                user.getEmail()
        );
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

            logger.warn(
                    "Failed login attempt for '{}'.",
                    request.email()
            );
            throw new IllegalArgumentException(
                    "Invalid email or password"
            );
        }

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> {

                    logger.error(
                            "Authentication succeeded but user '{}' could not be found in the database.",
                            request.email()
                    );

                    return new IllegalArgumentException(
                            "Invalid email or password"
                    );
                });

        String token = jwtService.generateToken(user.getEmail());

        logger.info(
                "User '{}' logged in successfully with role {}.",
                user.getEmail(),
                user.getRole()
        );

        return new AuthResponseDTO(
                token, // JWT token comes later
                user.getEmail(),
                user.getRole()
        );
    }
}
package com.jana.hospital_management.service;

import com.jana.hospital_management.exception.DuplicateResourceException;
import com.jana.hospital_management.repository.UserRepository;
import com.jana.hospital_management.security.JwtService;
import com.jana.hospital_management.dto.RegisterRequestDTO;
import com.jana.hospital_management.dto.AuthResponseDTO;
import com.jana.hospital_management.dto.LoginRequestDTO;
import com.jana.hospital_management.entity.Role;
import com.jana.hospital_management.entity.User;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_ShouldCreateUser_WhenEmailDoesNotExist() {

        // Arrange
        RegisterRequestDTO request = new RegisterRequestDTO(
                "doctor@hospital.com",
                "password123",
                Role.DOCTOR
        );

        when(userRepository.existsByEmail(request.email()))
                .thenReturn(false);

        when(passwordEncoder.encode(request.password()))
                .thenReturn("encodedPassword");

        // Act
        authService.register(request);

        // Assert
        ArgumentCaptor<User> userCaptor =
                ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertEquals(
                "doctor@hospital.com",
                savedUser.getEmail()
        );

        assertEquals(
                "encodedPassword",
                savedUser.getPassword()
        );

        assertEquals(
                Role.DOCTOR,
                savedUser.getRole()
        );
    }

    @Test
    void register_ShouldThrowException_WhenEmailAlreadyExists() {

        // Arrange
        RegisterRequestDTO request = new RegisterRequestDTO(
                "doctor@hospital.com",
                "password123",
                Role.DOCTOR
        );

        when(userRepository.existsByEmail(request.email()))
                .thenReturn(true);

        // Act + Assert
        DuplicateResourceException exception =
                assertThrows(
                        DuplicateResourceException.class,
                        () -> authService.register(request)
                );

        assertEquals(
                "Email already exists",
                exception.getMessage()
        );

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_ShouldReturnJwt_WhenCredentialsAreValid() {

        // Arrange
        LoginRequestDTO request = new LoginRequestDTO(
                "doctor@hospital.com",
                "password123"
        );

        User user = User.builder()
                .id(1L)
                .email("doctor@hospital.com")
                .password("encodedPassword")
                .role(Role.DOCTOR)
                .build();

        when(userRepository.findByEmail(request.email()))
                .thenReturn(Optional.of(user));

        when(jwtService.generateToken(user.getEmail()))
                .thenReturn("fake-jwt-token");

        // Act
        AuthResponseDTO response = authService.login(request);

        // Assert
        assertNotNull(response);

        assertEquals(
                "fake-jwt-token",
                response.token()
        );

        assertEquals(
                "doctor@hospital.com",
                response.email()
        );

        assertEquals(
                Role.DOCTOR,
                response.role()
        );

        verify(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        verify(userRepository)
                .findByEmail(request.email());

        verify(jwtService)
                .generateToken(user.getEmail());
    }

    @Test
    void login_ShouldThrowException_WhenAuthenticationFails() {

        // Arrange
        LoginRequestDTO request = new LoginRequestDTO(
                "doctor@hospital.com",
                "wrongPassword"
        );

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // Act + Assert
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> authService.login(request)
                );

        assertEquals(
                "Invalid email or password",
                exception.getMessage()
        );

        verify(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        verify(userRepository, never())
                .findByEmail(any());

        verify(jwtService, never())
                .generateToken(any());
    }

    @Test
    void login_ShouldThrowException_WhenAuthenticatedUserNotFound() {

        // Arrange
        LoginRequestDTO request = new LoginRequestDTO(
                "doctor@hospital.com",
                "password123"
        );

        when(userRepository.findByEmail(request.email()))
                .thenReturn(Optional.empty());

        // Act + Assert
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> authService.login(request)
                );

        assertEquals(
                "Invalid email or password",
                exception.getMessage()
        );

        verify(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        verify(userRepository)
                .findByEmail(request.email());

        verify(jwtService, never())
                .generateToken(any());
    }


}
package com.jana.hospital_management.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jana.hospital_management.dto.LoginRequestDTO;
import com.jana.hospital_management.dto.RegisterRequestDTO;
import com.jana.hospital_management.entity.Role;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void login_ShouldReturnJwt_WhenCredentialsAreValid() throws Exception {

        LoginRequestDTO request =
                new LoginRequestDTO(
                        "admin@hospital.com",
                        "Admin123"
                );

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email")
                        .value("admin@hospital.com"))
                .andExpect(jsonPath("$.role")
                        .value("ADMIN"));
    }

    @Test
    void login_ShouldReturnBadRequest_WhenPasswordIsInvalid() throws Exception {

        LoginRequestDTO request =
                new LoginRequestDTO(
                        "admin@hospital.com",
                        "WrongPassword"
                );

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Invalid email or password"));
    }

    @Test
    void login_ShouldReturnBadRequest_WhenEmailDoesNotExist() throws Exception {

        LoginRequestDTO request =
                new LoginRequestDTO(
                        "unknown@hospital.com",
                        "Admin123"
                );

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Invalid email or password"));
    }

    @Test
    void register_ShouldCreateUser_WhenAdminIsAuthenticated() throws Exception {

        String token = getAdminToken();

        RegisterRequestDTO registerRequest =
                new RegisterRequestDTO(
                        "doctor1@hospital.com",
                        "Doctor123",
                        Role.DOCTOR
                );

        mockMvc.perform(
                        post("/api/auth/register")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(registerRequest)
                                )
                )
                .andExpect(status().isCreated());
    }

    @Test
    void register_ShouldReturnConflict_WhenEmailAlreadyExists() throws Exception {

        String token = getAdminToken();

        RegisterRequestDTO registerRequest =
                new RegisterRequestDTO(
                        "admin@hospital.com",
                        "Admin123",
                        Role.ADMIN
                );

        mockMvc.perform(
                        post("/api/auth/register")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(registerRequest)
                                )
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message")
                        .value("Email already exists"));
    }

    @Test
    void register_ShouldReturnUnauthorized_WhenJwtIsMissing() throws Exception {

        RegisterRequestDTO request =
                new RegisterRequestDTO(
                        "doctor2@hospital.com",
                        "Doctor123",
                        Role.DOCTOR
                );

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message")
                        .value("You do not have permission to access this resource."));
    }

    @Test
    void login_ShouldReturnValidationError_WhenEmailIsInvalid() throws Exception {

        LoginRequestDTO request =
                new LoginRequestDTO(
                        "invalid-email",
                        "Admin123"
                );

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.email")
                        .value("Invalid email format"));
    }

    @Test
    void register_ShouldReturnValidationError_WhenPasswordIsTooShort() throws Exception {

        String token = getAdminToken();

        RegisterRequestDTO request =
                new RegisterRequestDTO(
                        "doctor3@hospital.com",
                        "123",
                        Role.DOCTOR
                );

        mockMvc.perform(
                        post("/api/auth/register")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.password")
                        .value("Password must be between 6 and 50 characters"));
    }

    private String getAdminToken() throws Exception {

        LoginRequestDTO loginRequest =
                new LoginRequestDTO(
                        "admin@hospital.com",
                        "Admin123"
                );

        String loginResponse =
                mockMvc.perform(
                                post("/api/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                objectMapper.writeValueAsString(loginRequest)
                                        )
                        )
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        return objectMapper
                .readTree(loginResponse)
                .get("token")
                .asText();
    }
}
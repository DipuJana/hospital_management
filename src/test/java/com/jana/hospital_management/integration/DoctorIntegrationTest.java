package com.jana.hospital_management.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jana.hospital_management.dto.DoctorRequestDTO;
import com.jana.hospital_management.dto.LoginRequestDTO;
import com.jana.hospital_management.dto.RegisterRequestDTO;
import com.jana.hospital_management.entity.Role;
import com.jana.hospital_management.entity.Specialization;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class DoctorIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createDoctor_ShouldReturnCreated_WhenRequestIsValid()
            throws Exception {

        String token = getAdminToken();

        DoctorRequestDTO request = new DoctorRequestDTO();
        request.setFullName("Dr. John Smith");
        request.setSpecialization(Specialization.CARDIOLOGY);
        request.setEmail("doctor@hospital.com");
        request.setPhoneNumber("9876543210");

        mockMvc.perform(
                        post("/api/doctors")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fullName")
                        .value("Dr. John Smith"))
                .andExpect(jsonPath("$.email")
                        .value("doctor@hospital.com"))
                .andExpect(jsonPath("$.specialization")
                        .value("CARDIOLOGY"));
    }

    @Test
    void createDoctor_ShouldReturnConflict_WhenEmailAlreadyExists()
            throws Exception {

        String token = getAdminToken();

        DoctorRequestDTO request = new DoctorRequestDTO();
        request.setFullName("Dr. John Smith");
        request.setSpecialization(Specialization.CARDIOLOGY);
        request.setEmail("doctor@hospital.com");
        request.setPhoneNumber("9876543210");

        mockMvc.perform(
                post("/api/doctors")
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(request)
                        )
        ).andExpect(status().isCreated());

        mockMvc.perform(
                        post("/api/doctors")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message")
                        .value("Doctor with this email already exists"));
    }

    @Test
    void createDoctor_ShouldReturnValidationError_WhenRequestIsInvalid()
            throws Exception {

        String token = getAdminToken();

        DoctorRequestDTO request = new DoctorRequestDTO();
        request.setFullName("");
        request.setSpecialization(null);
        request.setEmail("invalid-email");
        request.setPhoneNumber("123");

        mockMvc.perform(
                        post("/api/doctors")
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
                .andExpect(jsonPath("$.validationErrors.fullName").exists())
                .andExpect(jsonPath("$.validationErrors.specialization").exists())
                .andExpect(jsonPath("$.validationErrors.email").exists())
                .andExpect(jsonPath("$.validationErrors.phoneNumber").exists());
    }

    @Test
    void createDoctor_ShouldReturnForbidden_WhenReceptionistCreatesDoctor()
            throws Exception {

        String token = getReceptionistToken();

        DoctorRequestDTO request = new DoctorRequestDTO();
        request.setFullName("Dr. John Smith");
        request.setSpecialization(Specialization.CARDIOLOGY);
        request.setEmail("doctor@hospital.com");
        request.setPhoneNumber("9876543210");

        mockMvc.perform(
                        post("/api/doctors")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error")
                        .value("Forbidden"));
    }

    @Test
    void getDoctorById_ShouldReturnDoctor_WhenDoctorExists()
            throws Exception {

        String adminToken = getAdminToken();

        DoctorRequestDTO request = new DoctorRequestDTO();
        request.setFullName("Dr. John Smith");
        request.setSpecialization(Specialization.CARDIOLOGY);
        request.setEmail("doctor@hospital.com");
        request.setPhoneNumber("9876543210");

        String response =
                mockMvc.perform(
                                post("/api/doctors")
                                        .header(
                                                "Authorization",
                                                "Bearer " + adminToken
                                        )
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                objectMapper.writeValueAsString(request)
                                        )
                        )
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        Long doctorId = objectMapper
                .readTree(response)
                .get("id")
                .asLong();

        String receptionistToken = getReceptionistToken();

        mockMvc.perform(
                        get("/api/doctors/{id}", doctorId)
                                .header(
                                        "Authorization",
                                        "Bearer " + receptionistToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(doctorId))
                .andExpect(jsonPath("$.fullName")
                        .value("Dr. John Smith"))
                .andExpect(jsonPath("$.email")
                        .value("doctor@hospital.com"));
    }

    @Test
    void getDoctorById_ShouldReturnNotFound_WhenDoctorDoesNotExist()
            throws Exception {

        String token = getDoctorToken();

        mockMvc.perform(
                        get("/api/doctors/{id}", 999L)
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Doctor not found with id : 999"));
    }

    @Test
    void updateDoctor_ShouldReturnUpdatedDoctor()
            throws Exception {

        String adminToken = getAdminToken();

        DoctorRequestDTO createRequest = new DoctorRequestDTO();
        createRequest.setFullName("Dr. John Smith");
        createRequest.setSpecialization(Specialization.CARDIOLOGY);
        createRequest.setEmail("doctor@hospital.com");
        createRequest.setPhoneNumber("9876543210");

        String response =
                mockMvc.perform(
                                post("/api/doctors")
                                        .header(
                                                "Authorization",
                                                "Bearer " + adminToken
                                        )
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                objectMapper.writeValueAsString(createRequest)
                                        )
                        )
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        Long doctorId = objectMapper
                .readTree(response)
                .get("id")
                .asLong();

        DoctorRequestDTO updateRequest = new DoctorRequestDTO();
        updateRequest.setFullName("Dr. John Updated");
        updateRequest.setSpecialization(Specialization.NEUROLOGY);
        updateRequest.setEmail("doctor@hospital.com");
        updateRequest.setPhoneNumber("9999999999");

        mockMvc.perform(
                        put("/api/doctors/{id}", doctorId)
                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(updateRequest)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(doctorId))
                .andExpect(jsonPath("$.fullName")
                        .value("Dr. John Updated"))
                .andExpect(jsonPath("$.phoneNumber")
                        .value("9999999999"))
                .andExpect(jsonPath("$.specialization")
                        .value("NEUROLOGY"));
    }

    @Test
    void updateDoctor_ShouldReturnConflict_WhenEmailAlreadyExists()
            throws Exception {

        String adminToken = getAdminToken();

        DoctorRequestDTO doctorOne = new DoctorRequestDTO();
        doctorOne.setFullName("Dr. John");
        doctorOne.setSpecialization(Specialization.CARDIOLOGY);
        doctorOne.setEmail("doctor1@hospital.com");
        doctorOne.setPhoneNumber("9876543210");

        String response =
                mockMvc.perform(
                                post("/api/doctors")
                                        .header(
                                                "Authorization",
                                                "Bearer " + adminToken
                                        )
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                objectMapper.writeValueAsString(doctorOne)
                                        )
                        )
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        Long doctorId = objectMapper
                .readTree(response)
                .get("id")
                .asLong();

        DoctorRequestDTO doctorTwo = new DoctorRequestDTO();
        doctorTwo.setFullName("Dr. Jane");
        doctorTwo.setSpecialization(Specialization.DERMATOLOGY);
        doctorTwo.setEmail("doctor2@hospital.com");
        doctorTwo.setPhoneNumber("9123456789");

        mockMvc.perform(
                post("/api/doctors")
                        .header(
                                "Authorization",
                                "Bearer " + adminToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(doctorTwo)
                        )
        ).andExpect(status().isCreated());

        doctorOne.setEmail("doctor2@hospital.com");

        mockMvc.perform(
                        put("/api/doctors/{id}", doctorId)
                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(doctorOne)
                                )
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message")
                        .value("Doctor with this email already exists"));
    }

    @Test
    void deleteDoctor_ShouldReturnNoContent_WhenAdminDeletesDoctor()
            throws Exception {

        String adminToken = getAdminToken();

        DoctorRequestDTO request = new DoctorRequestDTO();
        request.setFullName("Dr. John Smith");
        request.setSpecialization(Specialization.CARDIOLOGY);
        request.setEmail("doctor@hospital.com");
        request.setPhoneNumber("9876543210");

        String response =
                mockMvc.perform(
                                post("/api/doctors")
                                        .header(
                                                "Authorization",
                                                "Bearer " + adminToken
                                        )
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                objectMapper.writeValueAsString(request)
                                        )
                        )
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        Long doctorId = objectMapper
                .readTree(response)
                .get("id")
                .asLong();

        mockMvc.perform(
                        delete("/api/doctors/{id}", doctorId)
                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken
                                )
                )
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteDoctor_ShouldReturnForbidden_WhenReceptionistDeletesDoctor()
            throws Exception {

        String adminToken = getAdminToken();

        DoctorRequestDTO request = new DoctorRequestDTO();
        request.setFullName("Dr. John Smith");
        request.setSpecialization(Specialization.CARDIOLOGY);
        request.setEmail("doctor@hospital.com");
        request.setPhoneNumber("9876543210");

        String response =
                mockMvc.perform(
                                post("/api/doctors")
                                        .header(
                                                "Authorization",
                                                "Bearer " + adminToken
                                        )
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                objectMapper.writeValueAsString(request)
                                        )
                        )
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        Long doctorId = objectMapper
                .readTree(response)
                .get("id")
                .asLong();

        String receptionistToken = getReceptionistToken();

        mockMvc.perform(
                        delete("/api/doctors/{id}", doctorId)
                                .header(
                                        "Authorization",
                                        "Bearer " + receptionistToken
                                )
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error")
                        .value("Forbidden"));
    }

    @Test
    void getAllDoctors_ShouldReturnPagedDoctors()
            throws Exception {

        String adminToken = getAdminToken();

        DoctorRequestDTO doctor1 = new DoctorRequestDTO();
        doctor1.setFullName("Dr. John Smith");
        doctor1.setSpecialization(Specialization.CARDIOLOGY);
        doctor1.setEmail("doctor1@hospital.com");
        doctor1.setPhoneNumber("9876543210");

        DoctorRequestDTO doctor2 = new DoctorRequestDTO();
        doctor2.setFullName("Dr. Jane Smith");
        doctor2.setSpecialization(Specialization.DERMATOLOGY);
        doctor2.setEmail("doctor2@hospital.com");
        doctor2.setPhoneNumber("9123456789");

        mockMvc.perform(
                post("/api/doctors")
                        .header(
                                "Authorization",
                                "Bearer " + adminToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(doctor1)
                        )
        ).andExpect(status().isCreated());

        mockMvc.perform(
                post("/api/doctors")
                        .header(
                                "Authorization",
                                "Bearer " + adminToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(doctor2)
                        )
        ).andExpect(status().isCreated());

        String receptionistToken = getReceptionistToken();

        mockMvc.perform(
                        get("/api/doctors")
                                .header(
                                        "Authorization",
                                        "Bearer " + receptionistToken
                                )
                                .param("page", "0")
                                .param("size", "10")
                                .param("sortBy", "id")
                                .param("direction", "asc")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.pageSize").value(10));
    }

    @Test
    void getAllDoctors_ShouldReturnUnauthorized_WhenJwtMissing()
            throws Exception {

        mockMvc.perform(
                        get("/api/doctors")
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error")
                        .value("Unauthorized"))
                .andExpect(jsonPath("$.message")
                        .value("Authentication is required to access this resource."));
    }

    // =====================================================
    // Helper Methods
    // =====================================================

    private String getAdminToken() throws Exception {

        LoginRequestDTO request =
                new LoginRequestDTO(
                        "admin@hospital.com",
                        "Admin123"
                );

        String response =
                mockMvc.perform(
                                post("/api/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                objectMapper.writeValueAsString(request)
                                        )
                        )
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        return objectMapper.readTree(response)
                .get("token")
                .asText();
    }

    private String getReceptionistToken() throws Exception {

        String adminToken = getAdminToken();

        RegisterRequestDTO registerRequest =
                new RegisterRequestDTO(
                        "receptionist@hospital.com",
                        "Reception123",
                        Role.RECEPTIONIST
                );

        mockMvc.perform(
                post("/api/auth/register")
                        .header(
                                "Authorization",
                                "Bearer " + adminToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(registerRequest)
                        )
        );

        LoginRequestDTO loginRequest =
                new LoginRequestDTO(
                        "receptionist@hospital.com",
                        "Reception123"
                );

        String response =
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

        return objectMapper.readTree(response)
                .get("token")
                .asText();
    }

    private String getDoctorToken() throws Exception {

        String adminToken = getAdminToken();

        RegisterRequestDTO registerRequest =
                new RegisterRequestDTO(
                        "doctor@hospital.com",
                        "Doctor123",
                        Role.DOCTOR
                );

        mockMvc.perform(
                post("/api/auth/register")
                        .header(
                                "Authorization",
                                "Bearer " + adminToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(registerRequest)
                        )
        );

        LoginRequestDTO loginRequest =
                new LoginRequestDTO(
                        "doctor@hospital.com",
                        "Doctor123"
                );

        String response =
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

        return objectMapper.readTree(response)
                .get("token")
                .asText();
    }
}
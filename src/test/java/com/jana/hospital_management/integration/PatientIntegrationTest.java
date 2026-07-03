package com.jana.hospital_management.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jana.hospital_management.dto.LoginRequestDTO;
import com.jana.hospital_management.dto.PatientRequestDTO;
import com.jana.hospital_management.dto.RegisterRequestDTO;
import com.jana.hospital_management.entity.Gender;
import com.jana.hospital_management.entity.Role;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PatientIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createPatient_ShouldReturnCreated_WhenRequestIsValid() throws Exception {

        String token = getReceptionistToken();

        PatientRequestDTO request = new PatientRequestDTO();
        request.setFullName("John Doe");
        request.setDateOfBirth(LocalDate.of(1998, 5, 10));
        request.setEmail("john@hospital.com");
        request.setPhoneNumber("9876543210");
        request.setGender(Gender.MALE);

        mockMvc.perform(
                        post("/api/patients")
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
                        .value("John Doe"))
                .andExpect(jsonPath("$.email")
                        .value("john@hospital.com"))
                .andExpect(jsonPath("$.gender")
                        .value("MALE"));
    }

    @Test
    void createPatient_ShouldReturnConflict_WhenEmailAlreadyExists()
            throws Exception {

        String token = getReceptionistToken();

        PatientRequestDTO request = new PatientRequestDTO();
        request.setFullName("John Doe");
        request.setDateOfBirth(LocalDate.of(1998, 5, 10));
        request.setEmail("john@hospital.com");
        request.setPhoneNumber("9876543210");
        request.setGender(Gender.MALE);

        mockMvc.perform(
                post("/api/patients")
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
                        post("/api/patients")
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
                        .value("Patient with this email already exists"));
    }

    @Test
    void createPatient_ShouldReturnValidationError_WhenRequestIsInvalid()
            throws Exception {

        String token = getReceptionistToken();

        PatientRequestDTO request = new PatientRequestDTO();
        request.setFullName("");
        request.setDateOfBirth(LocalDate.now().plusDays(1));
        request.setEmail("invalid-email");
        request.setPhoneNumber("123");
        request.setGender(null);

        mockMvc.perform(
                        post("/api/patients")
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
                .andExpect(jsonPath("$.validationErrors.email").exists())
                .andExpect(jsonPath("$.validationErrors.phoneNumber").exists())
                .andExpect(jsonPath("$.validationErrors.dateOfBirth").exists())
                .andExpect(jsonPath("$.validationErrors.gender").exists());
    }

    @Test
    void createPatient_ShouldReturnForbidden_WhenDoctorCreatesPatient()
            throws Exception {

        String token = getDoctorToken();

        PatientRequestDTO request = new PatientRequestDTO();
        request.setFullName("John Doe");
        request.setDateOfBirth(LocalDate.of(1998, 5, 10));
        request.setEmail("john@hospital.com");
        request.setPhoneNumber("9876543210");
        request.setGender(Gender.MALE);

        mockMvc.perform(
                        post("/api/patients")
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
    void getPatientById_ShouldReturnPatient_WhenPatientExists()
            throws Exception {

        String receptionistToken = getReceptionistToken();

        PatientRequestDTO request = new PatientRequestDTO();
        request.setFullName("John Doe");
        request.setDateOfBirth(LocalDate.of(1998, 5, 10));
        request.setEmail("john@hospital.com");
        request.setPhoneNumber("9876543210");
        request.setGender(Gender.MALE);

        String response =
                mockMvc.perform(
                                post("/api/patients")
                                        .header(
                                                "Authorization",
                                                "Bearer " + receptionistToken
                                        )
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                objectMapper.writeValueAsString(request)
                                        )
                        )
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        Long patientId = objectMapper
                .readTree(response)
                .get("id")
                .asLong();

        String doctorToken = getDoctorToken();

        mockMvc.perform(
                        get("/api/patients/{id}", patientId)
                                .header(
                                        "Authorization",
                                        "Bearer " + doctorToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(patientId))
                .andExpect(jsonPath("$.fullName")
                        .value("John Doe"))
                .andExpect(jsonPath("$.email")
                        .value("john@hospital.com"));
    }

    @Test
    void getPatientById_ShouldReturnNotFound_WhenPatientDoesNotExist()
            throws Exception {

        String token = getDoctorToken();

        mockMvc.perform(
                        get("/api/patients/{id}", 999L)
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Patient not found with id : 999"));
    }

    @Test
    void updatePatient_ShouldReturnUpdatedPatient() throws Exception {

        String receptionistToken = getReceptionistToken();

        PatientRequestDTO createRequest = new PatientRequestDTO();
        createRequest.setFullName("John Doe");
        createRequest.setDateOfBirth(LocalDate.of(1998, 5, 10));
        createRequest.setEmail("john@hospital.com");
        createRequest.setPhoneNumber("9876543210");
        createRequest.setGender(Gender.MALE);

        String response =
                mockMvc.perform(
                                post("/api/patients")
                                        .header(
                                                "Authorization",
                                                "Bearer " + receptionistToken
                                        )
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                objectMapper.writeValueAsString(createRequest)
                                        )
                        )
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        Long patientId = objectMapper
                .readTree(response)
                .get("id")
                .asLong();

        PatientRequestDTO updateRequest = new PatientRequestDTO();
        updateRequest.setFullName("John Updated");
        updateRequest.setDateOfBirth(LocalDate.of(1999, 6, 15));
        updateRequest.setEmail("john@hospital.com");
        updateRequest.setPhoneNumber("9999999999");
        updateRequest.setGender(Gender.MALE);

        mockMvc.perform(
                        put("/api/patients/{id}", patientId)
                                .header(
                                        "Authorization",
                                        "Bearer " + receptionistToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(updateRequest)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(patientId))
                .andExpect(jsonPath("$.fullName")
                        .value("John Updated"))
                .andExpect(jsonPath("$.phoneNumber")
                        .value("9999999999"));
    }

    @Test
    void updatePatient_ShouldReturnConflict_WhenEmailAlreadyExists()
            throws Exception {

        String receptionistToken = getReceptionistToken();

        PatientRequestDTO patientOne = new PatientRequestDTO();
        patientOne.setFullName("John");
        patientOne.setDateOfBirth(LocalDate.of(1998, 5, 10));
        patientOne.setEmail("john@hospital.com");
        patientOne.setPhoneNumber("9876543210");
        patientOne.setGender(Gender.MALE);

        String response =
                mockMvc.perform(
                                post("/api/patients")
                                        .header(
                                                "Authorization",
                                                "Bearer " + receptionistToken
                                        )
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                objectMapper.writeValueAsString(patientOne)
                                        )
                        )
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        Long patientId = objectMapper
                .readTree(response)
                .get("id")
                .asLong();

        PatientRequestDTO patientTwo = new PatientRequestDTO();
        patientTwo.setFullName("Jane");
        patientTwo.setDateOfBirth(LocalDate.of(1997, 4, 12));
        patientTwo.setEmail("jane@hospital.com");
        patientTwo.setPhoneNumber("9123456789");
        patientTwo.setGender(Gender.FEMALE);

        mockMvc.perform(
                post("/api/patients")
                        .header(
                                "Authorization",
                                "Bearer " + receptionistToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(patientTwo)
                        )
        ).andExpect(status().isCreated());

        patientOne.setEmail("jane@hospital.com");

        mockMvc.perform(
                        put("/api/patients/{id}", patientId)
                                .header(
                                        "Authorization",
                                        "Bearer " + receptionistToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(patientOne)
                                )
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message")
                        .value("Patient with this email already exists."));
    }

    @Test
    void deletePatient_ShouldReturnNoContent_WhenAdminDeletesPatient()
            throws Exception {

        String receptionistToken = getReceptionistToken();

        PatientRequestDTO request = new PatientRequestDTO();
        request.setFullName("John Doe");
        request.setDateOfBirth(LocalDate.of(1998, 5, 10));
        request.setEmail("john@hospital.com");
        request.setPhoneNumber("9876543210");
        request.setGender(Gender.MALE);

        String response =
                mockMvc.perform(
                                post("/api/patients")
                                        .header(
                                                "Authorization",
                                                "Bearer " + receptionistToken
                                        )
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                objectMapper.writeValueAsString(request)
                                        )
                        )
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        Long patientId = objectMapper
                .readTree(response)
                .get("id")
                .asLong();

        String adminToken = getAdminToken();

        mockMvc.perform(
                        delete("/api/patients/{id}", patientId)
                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken
                                )
                )
                .andExpect(status().isNoContent());
    }

    @Test
    void deletePatient_ShouldReturnForbidden_WhenReceptionistDeletesPatient()
            throws Exception {

        String receptionistToken = getReceptionistToken();

        PatientRequestDTO request = new PatientRequestDTO();
        request.setFullName("John Doe");
        request.setDateOfBirth(LocalDate.of(1998, 5, 10));
        request.setEmail("john@hospital.com");
        request.setPhoneNumber("9876543210");
        request.setGender(Gender.MALE);

        String response =
                mockMvc.perform(
                                post("/api/patients")
                                        .header(
                                                "Authorization",
                                                "Bearer " + receptionistToken
                                        )
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                objectMapper.writeValueAsString(request)
                                        )
                        )
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        Long patientId = objectMapper
                .readTree(response)
                .get("id")
                .asLong();

        mockMvc.perform(
                        delete("/api/patients/{id}", patientId)
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
    void getAllPatients_ShouldReturnPagedPatients() throws Exception {

        String receptionistToken = getReceptionistToken();

        PatientRequestDTO patient1 = new PatientRequestDTO();
        patient1.setFullName("John Doe");
        patient1.setDateOfBirth(LocalDate.of(1998, 5, 10));
        patient1.setEmail("john@hospital.com");
        patient1.setPhoneNumber("9876543210");
        patient1.setGender(Gender.MALE);

        PatientRequestDTO patient2 = new PatientRequestDTO();
        patient2.setFullName("Jane Doe");
        patient2.setDateOfBirth(LocalDate.of(1997, 4, 12));
        patient2.setEmail("jane@hospital.com");
        patient2.setPhoneNumber("9123456789");
        patient2.setGender(Gender.FEMALE);

        mockMvc.perform(
                post("/api/patients")
                        .header(
                                "Authorization",
                                "Bearer " + receptionistToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(patient1)
                        )
        ).andExpect(status().isCreated());

        mockMvc.perform(
                post("/api/patients")
                        .header(
                                "Authorization",
                                "Bearer " + receptionistToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(patient2)
                        )
        ).andExpect(status().isCreated());

        String doctorToken = getDoctorToken();

        mockMvc.perform(
                        get("/api/patients")
                                .header(
                                        "Authorization",
                                        "Bearer " + doctorToken
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
    void getAllPatients_ShouldReturnUnauthorized_WhenJwtMissing()
            throws Exception {

        mockMvc.perform(
                        get("/api/patients")
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
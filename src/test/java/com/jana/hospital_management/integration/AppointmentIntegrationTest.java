package com.jana.hospital_management.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jana.hospital_management.dto.AppointmentRequestDTO;
import com.jana.hospital_management.dto.DoctorRequestDTO;
import com.jana.hospital_management.dto.LoginRequestDTO;
import com.jana.hospital_management.dto.PatientRequestDTO;
import com.jana.hospital_management.dto.RegisterRequestDTO;
import com.jana.hospital_management.entity.Gender;
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

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AppointmentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createAppointment_ShouldReturnCreated_WhenRequestIsValid()
            throws Exception {

        String receptionistToken = getReceptionistToken();

        Long patientId = createPatient(receptionistToken);

        String adminToken = getAdminToken();

        Long doctorId = createDoctor(adminToken);

        AppointmentRequestDTO request =
                new AppointmentRequestDTO();

        request.setPatientId(patientId);
        request.setDoctorId(doctorId);
        request.setAppointmentDateTime(
                LocalDateTime.now().plusDays(1)
        );
        request.setReason("Regular Checkup");

        mockMvc.perform(
                        post("/api/appointments")
                                .header(
                                        "Authorization",
                                        "Bearer " + receptionistToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.patientId")
                        .value(patientId))
                .andExpect(jsonPath("$.doctorId")
                        .value(doctorId))
                .andExpect(jsonPath("$.status")
                        .value("SCHEDULED"))
                .andExpect(jsonPath("$.reason")
                        .value("Regular Checkup"));
    }

    @Test
    void createAppointment_ShouldReturnConflict_WhenDoctorAlreadyBooked()
            throws Exception {

        String receptionistToken = getReceptionistToken();

        Long patientOne = createPatient(receptionistToken);
        Long patientTwo = createSecondPatient(receptionistToken);

        String adminToken = getAdminToken();

        Long doctorId = createDoctor(adminToken);

        LocalDateTime appointmentTime =
                LocalDateTime.now().plusDays(1);

        AppointmentRequestDTO firstAppointment =
                new AppointmentRequestDTO();

        firstAppointment.setPatientId(patientOne);
        firstAppointment.setDoctorId(doctorId);
        firstAppointment.setAppointmentDateTime(
                appointmentTime
        );
        firstAppointment.setReason("Checkup");

        mockMvc.perform(
                post("/api/appointments")
                        .header(
                                "Authorization",
                                "Bearer " + receptionistToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(firstAppointment)
                        )
        ).andExpect(status().isCreated());

        AppointmentRequestDTO secondAppointment =
                new AppointmentRequestDTO();

        secondAppointment.setPatientId(patientTwo);
        secondAppointment.setDoctorId(doctorId);
        secondAppointment.setAppointmentDateTime(
                appointmentTime
        );
        secondAppointment.setReason("Emergency");

        mockMvc.perform(
                        post("/api/appointments")
                                .header(
                                        "Authorization",
                                        "Bearer " + receptionistToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(secondAppointment)
                                )
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status")
                        .value(409))
                .andExpect(jsonPath("$.message")
                        .value("Doctor already booked at this time"));
    }

    @Test
    void createAppointment_ShouldReturnNotFound_WhenPatientDoesNotExist()
            throws Exception {

        String receptionistToken = getReceptionistToken();

        String adminToken = getAdminToken();

        Long doctorId = createDoctor(adminToken);

        AppointmentRequestDTO request =
                new AppointmentRequestDTO();

        request.setPatientId(999L);
        request.setDoctorId(doctorId);
        request.setAppointmentDateTime(
                LocalDateTime.now().plusDays(1)
        );
        request.setReason("Checkup");

        mockMvc.perform(
                        post("/api/appointments")
                                .header(
                                        "Authorization",
                                        "Bearer " + receptionistToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status")
                        .value(404))
                .andExpect(jsonPath("$.message")
                        .value("Patient not found with id 999"));
    }

    @Test
    void createAppointment_ShouldReturnNotFound_WhenDoctorDoesNotExist()
            throws Exception {

        String receptionistToken = getReceptionistToken();

        Long patientId = createPatient(receptionistToken);

        AppointmentRequestDTO request =
                new AppointmentRequestDTO();

        request.setPatientId(patientId);
        request.setDoctorId(999L);
        request.setAppointmentDateTime(
                LocalDateTime.now().plusDays(1)
        );
        request.setReason("Checkup");

        mockMvc.perform(
                        post("/api/appointments")
                                .header(
                                        "Authorization",
                                        "Bearer " + receptionistToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status")
                        .value(404))
                .andExpect(jsonPath("$.message")
                        .value("Doctor not found with id 999"));
    }

    @Test
    void cancelAppointment_ShouldReturnCancelledAppointment()
            throws Exception {

        String receptionistToken = getReceptionistToken();

        Long patientId = createPatient(receptionistToken);

        String adminToken = getAdminToken();

        Long doctorId = createDoctor(adminToken);

        AppointmentRequestDTO request =
                new AppointmentRequestDTO();

        request.setPatientId(patientId);
        request.setDoctorId(doctorId);
        request.setAppointmentDateTime(
                LocalDateTime.now().plusDays(1)
        );
        request.setReason("Checkup");

        String response =
                mockMvc.perform(
                                post("/api/appointments")
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

        Long appointmentId = objectMapper
                .readTree(response)
                .get("id")
                .asLong();

        mockMvc.perform(
                        patch("/api/appointments/{id}/cancel", appointmentId)
                                .header(
                                        "Authorization",
                                        "Bearer " + receptionistToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(appointmentId))
                .andExpect(jsonPath("$.status")
                        .value("CANCELLED"));
    }

    @Test
    void cancelAppointment_ShouldReturnBadRequest_WhenAlreadyCancelled()
            throws Exception {

        String receptionistToken = getReceptionistToken();

        Long patientId = createPatient(receptionistToken);

        String adminToken = getAdminToken();

        Long doctorId = createDoctor(adminToken);

        AppointmentRequestDTO request =
                new AppointmentRequestDTO();

        request.setPatientId(patientId);
        request.setDoctorId(doctorId);
        request.setAppointmentDateTime(
                LocalDateTime.now().plusDays(1)
        );
        request.setReason("Checkup");

        String response =
                mockMvc.perform(
                                post("/api/appointments")
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

        Long appointmentId = objectMapper
                .readTree(response)
                .get("id")
                .asLong();

        mockMvc.perform(
                patch("/api/appointments/{id}/cancel", appointmentId)
                        .header(
                                "Authorization",
                                "Bearer " + receptionistToken
                        )
        ).andExpect(status().isOk());

        mockMvc.perform(
                        patch("/api/appointments/{id}/cancel", appointmentId)
                                .header(
                                        "Authorization",
                                        "Bearer " + receptionistToken
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status")
                        .value(400))
                .andExpect(jsonPath("$.message")
                        .value("Only scheduled appointments can be cancelled"));
    }

    @Test
    void completeAppointment_ShouldReturnCompletedAppointment()
            throws Exception {

        String receptionistToken = getReceptionistToken();

        Long patientId = createPatient(receptionistToken);

        String adminToken = getAdminToken();

        Long doctorId = createDoctor(adminToken);

        AppointmentRequestDTO request =
                new AppointmentRequestDTO();

        request.setPatientId(patientId);
        request.setDoctorId(doctorId);
        request.setAppointmentDateTime(
                LocalDateTime.now().plusDays(1)
        );
        request.setReason("Checkup");

        String response =
                mockMvc.perform(
                                post("/api/appointments")
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

        Long appointmentId = objectMapper
                .readTree(response)
                .get("id")
                .asLong();

        String doctorToken = getDoctorToken();

        mockMvc.perform(
                        patch("/api/appointments/{id}/complete", appointmentId)
                                .header(
                                        "Authorization",
                                        "Bearer " + doctorToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(appointmentId))
                .andExpect(jsonPath("$.status")
                        .value("COMPLETED"));
    }

    @Test
    void completeAppointment_ShouldReturnBadRequest_WhenAlreadyCompleted()
            throws Exception {

        String receptionistToken = getReceptionistToken();

        Long patientId = createPatient(receptionistToken);

        String adminToken = getAdminToken();

        Long doctorId = createDoctor(adminToken);

        AppointmentRequestDTO request =
                new AppointmentRequestDTO();

        request.setPatientId(patientId);
        request.setDoctorId(doctorId);
        request.setAppointmentDateTime(
                LocalDateTime.now().plusDays(1)
        );
        request.setReason("Checkup");

        String response =
                mockMvc.perform(
                                post("/api/appointments")
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

        Long appointmentId = objectMapper
                .readTree(response)
                .get("id")
                .asLong();

        String doctorToken = getDoctorToken();

        mockMvc.perform(
                patch("/api/appointments/{id}/complete", appointmentId)
                        .header(
                                "Authorization",
                                "Bearer " + doctorToken
                        )
        ).andExpect(status().isOk());

        mockMvc.perform(
                        patch("/api/appointments/{id}/complete", appointmentId)
                                .header(
                                        "Authorization",
                                        "Bearer " + doctorToken
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status")
                        .value(400))
                .andExpect(jsonPath("$.message")
                        .value("Only scheduled appointments can be completed."));
    }

    @Test
    void completeAppointment_ShouldReturnBadRequest_WhenAppointmentIsCancelled()
            throws Exception {

        String receptionistToken = getReceptionistToken();

        Long patientId = createPatient(receptionistToken);

        String adminToken = getAdminToken();

        Long doctorId = createDoctor(adminToken);

        AppointmentRequestDTO request =
                new AppointmentRequestDTO();

        request.setPatientId(patientId);
        request.setDoctorId(doctorId);
        request.setAppointmentDateTime(
                LocalDateTime.now().plusDays(1)
        );
        request.setReason("Checkup");

        String response =
                mockMvc.perform(
                                post("/api/appointments")
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

        Long appointmentId = objectMapper
                .readTree(response)
                .get("id")
                .asLong();

        mockMvc.perform(
                patch("/api/appointments/{id}/cancel", appointmentId)
                        .header(
                                "Authorization",
                                "Bearer " + receptionistToken
                        )
        ).andExpect(status().isOk());

        String doctorToken = getDoctorToken();

        mockMvc.perform(
                        patch("/api/appointments/{id}/complete", appointmentId)
                                .header(
                                        "Authorization",
                                        "Bearer " + doctorToken
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status")
                        .value(400))
                .andExpect(jsonPath("$.message")
                        .value("Only scheduled appointments can be completed."));
    }

    @Test
    void rescheduleAppointment_ShouldReturnUpdatedAppointment()
            throws Exception {

        String receptionistToken = getReceptionistToken();

        Long patientId = createPatient(receptionistToken);

        String adminToken = getAdminToken();

        Long doctorId = createDoctor(adminToken);

        AppointmentRequestDTO request =
                new AppointmentRequestDTO();

        request.setPatientId(patientId);
        request.setDoctorId(doctorId);
        request.setAppointmentDateTime(
                LocalDateTime.now().plusDays(1)
        );
        request.setReason("Checkup");

        String response =
                mockMvc.perform(
                                post("/api/appointments")
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

        Long appointmentId = objectMapper
                .readTree(response)
                .get("id")
                .asLong();

        LocalDateTime newDateTime =
                LocalDateTime.now().plusDays(2);

        mockMvc.perform(
                        patch("/api/appointments/{id}/reschedule", appointmentId)
                                .header(
                                        "Authorization",
                                        "Bearer " + receptionistToken
                                )
                                .param(
                                        "newDateTime",
                                        newDateTime.toString()
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(appointmentId))
                .andExpect(jsonPath("$.status")
                        .value("SCHEDULED"));
    }

    @Test
    void rescheduleAppointment_ShouldReturnConflict_WhenDoctorAlreadyBooked()
            throws Exception {

        String receptionistToken = getReceptionistToken();

        Long patientOne = createPatient(receptionistToken);
        Long patientTwo = createSecondPatient(receptionistToken);

        String adminToken = getAdminToken();

        Long doctorId = createDoctor(adminToken);

        LocalDateTime firstTime =
                LocalDateTime.now().plusDays(1);

        LocalDateTime secondTime =
                LocalDateTime.now().plusDays(2);

        AppointmentRequestDTO appointmentOne =
                new AppointmentRequestDTO();

        appointmentOne.setPatientId(patientOne);
        appointmentOne.setDoctorId(doctorId);
        appointmentOne.setAppointmentDateTime(firstTime);
        appointmentOne.setReason("Checkup");

        String firstResponse =
                mockMvc.perform(
                                post("/api/appointments")
                                        .header(
                                                "Authorization",
                                                "Bearer " + receptionistToken
                                        )
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                objectMapper.writeValueAsString(appointmentOne)
                                        )
                        )
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        AppointmentRequestDTO appointmentTwo =
                new AppointmentRequestDTO();

        appointmentTwo.setPatientId(patientTwo);
        appointmentTwo.setDoctorId(doctorId);
        appointmentTwo.setAppointmentDateTime(secondTime);
        appointmentTwo.setReason("Follow Up");

        mockMvc.perform(
                post("/api/appointments")
                        .header(
                                "Authorization",
                                "Bearer " + receptionistToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(appointmentTwo)
                        )
        ).andExpect(status().isCreated());

        Long appointmentId = objectMapper
                .readTree(firstResponse)
                .get("id")
                .asLong();

        mockMvc.perform(
                        patch("/api/appointments/{id}/reschedule", appointmentId)
                                .header(
                                        "Authorization",
                                        "Bearer " + receptionistToken
                                )
                                .param(
                                        "newDateTime",
                                        secondTime.toString()
                                )
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status")
                        .value(409))
                .andExpect(jsonPath("$.message")
                        .value("Doctor already booked at this time"));
    }

    @Test
    void getAppointmentsByPatient_ShouldReturnPagedAppointments()
            throws Exception {

        String receptionistToken = getReceptionistToken();

        Long patientId = createPatient(receptionistToken);

        String adminToken = getAdminToken();

        Long doctorId = createDoctor(adminToken);

        AppointmentRequestDTO request =
                new AppointmentRequestDTO();

        request.setPatientId(patientId);
        request.setDoctorId(doctorId);
        request.setAppointmentDateTime(
                LocalDateTime.now().plusDays(1)
        );
        request.setReason("Checkup");

        mockMvc.perform(
                post("/api/appointments")
                        .header(
                                "Authorization",
                                "Bearer " + receptionistToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(request)
                        )
        ).andExpect(status().isCreated());

        String doctorToken = getDoctorToken();

        mockMvc.perform(
                        get("/api/appointments/patients/{patientId}", patientId)
                                .header(
                                        "Authorization",
                                        "Bearer " + doctorToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void getAppointmentsByDoctor_ShouldReturnPagedAppointments()
            throws Exception {

        String receptionistToken = getReceptionistToken();

        Long patientId = createPatient(receptionistToken);

        String adminToken = getAdminToken();

        Long doctorId = createDoctor(adminToken);

        AppointmentRequestDTO request =
                new AppointmentRequestDTO();

        request.setPatientId(patientId);
        request.setDoctorId(doctorId);
        request.setAppointmentDateTime(
                LocalDateTime.now().plusDays(1)
        );
        request.setReason("Checkup");

        mockMvc.perform(
                post("/api/appointments")
                        .header(
                                "Authorization",
                                "Bearer " + receptionistToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(request)
                        )
        ).andExpect(status().isCreated());

        String doctorToken = getDoctorToken();

        mockMvc.perform(
                        get("/api/appointments/doctors/{doctorId}", doctorId)
                                .header(
                                        "Authorization",
                                        "Bearer " + doctorToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void createAppointment_ShouldReturnForbidden_WhenDoctorCreatesAppointment()
            throws Exception {

        String receptionistToken = getReceptionistToken();

        Long patientId = createPatient(receptionistToken);

        String adminToken = getAdminToken();

        Long doctorId = createDoctor(adminToken);

        String doctorToken = getDoctorToken();

        AppointmentRequestDTO request =
                new AppointmentRequestDTO();

        request.setPatientId(patientId);
        request.setDoctorId(doctorId);
        request.setAppointmentDateTime(
                LocalDateTime.now().plusDays(1)
        );
        request.setReason("Checkup");

        mockMvc.perform(
                        post("/api/appointments")
                                .header(
                                        "Authorization",
                                        "Bearer " + doctorToken
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
    void getAppointmentsByPatient_ShouldReturnUnauthorized_WhenJwtMissing()
            throws Exception {

        mockMvc.perform(
                        get("/api/appointments/patients/{patientId}", 1L)
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

    private Long createPatient(String token) throws Exception {

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
                                                "Bearer " + token
                                        )
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                objectMapper.writeValueAsString(request)
                                        )
                        )
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        return objectMapper.readTree(response)
                .get("id")
                .asLong();
    }

    private Long createSecondPatient(String token) throws Exception {

        PatientRequestDTO request = new PatientRequestDTO();
        request.setFullName("Jane Doe");
        request.setDateOfBirth(LocalDate.of(1997, 6, 15));
        request.setEmail("jane@hospital.com");
        request.setPhoneNumber("9123456789");
        request.setGender(Gender.FEMALE);

        String response =
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
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        return objectMapper.readTree(response)
                .get("id")
                .asLong();
    }

    private Long createDoctor(String token) throws Exception {

        DoctorRequestDTO request = new DoctorRequestDTO();
        request.setFullName("Dr. John Smith");
        request.setSpecialization(Specialization.CARDIOLOGY);
        request.setEmail("doctor@hospital.com");
        request.setPhoneNumber("9999999999");

        String response =
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
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        return objectMapper.readTree(response)
                .get("id")
                .asLong();
    }
}
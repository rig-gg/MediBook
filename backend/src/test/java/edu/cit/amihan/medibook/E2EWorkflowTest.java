package edu.cit.amihan.medibook;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.cit.amihan.medibook.appointment.dto.AppointmentRequest;
import edu.cit.amihan.medibook.config.DataSeeder;
import edu.cit.amihan.medibook.patient.dto.PatientRequest;
import edu.cit.amihan.medibook.user.entity.User;
import edu.cit.amihan.medibook.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("End-to-End Workflow Tests (Integration)")
class E2EWorkflowTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private DataSeeder dataSeeder;
    @Autowired private UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private User patientUser;
    private User staffUser;

    @BeforeEach
    void setUp() {
        dataSeeder.run();
        patientUser = userRepository.findByUsername("patient1").orElseThrow();
        staffUser = userRepository.findByUsername("staff1").orElseThrow();
    }

    @Nested
    @DisplayName("Workflow 1: Patient books -> Staff confirms -> Patient views")
    class Workflow1 {

        @Test
        @DisplayName("Full appointment lifecycle happy path")
        void fullAppointmentLifecycle() throws Exception {
            AppointmentRequest bookRequest = new AppointmentRequest();
            bookRequest.setScheduleId(1L);

            mockMvc.perform(post("/api/appointments")
                            .with(SecurityMockMvcRequestPostProcessors.user(patientUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(bookRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.appointmentId").isNumber())
                    .andExpect(jsonPath("$.status").value("PENDING"))
                    .andExpect(jsonPath("$.patientName").value("Juan Dela Cruz"));

            mockMvc.perform(patch("/api/appointments/1/status")
                            .with(SecurityMockMvcRequestPostProcessors.user(staffUser))
                            .param("status", "CONFIRMED"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("CONFIRMED"));

            mockMvc.perform(get("/api/appointments/me")
                            .with(SecurityMockMvcRequestPostProcessors.user(patientUser)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].appointmentId").value(1))
                    .andExpect(jsonPath("$[0].status").value("CONFIRMED"));
        }
    }

    @Nested
    @DisplayName("Workflow 2: Invalid state transition - completed -> confirmed")
    class Workflow2 {

        @Test
        @DisplayName("Staff tries to confirm COMPLETED appointment - rejected")
        void invalidStateTransitionRejected() throws Exception {
            AppointmentRequest bookRequest = new AppointmentRequest();
            bookRequest.setScheduleId(2L);

            String postResponse = mockMvc.perform(post("/api/appointments")
                            .with(SecurityMockMvcRequestPostProcessors.user(patientUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(bookRequest)))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();

            long appointmentId = objectMapper.readTree(postResponse).get("appointmentId").asLong();

            mockMvc.perform(patch("/api/appointments/" + appointmentId + "/status")
                            .with(SecurityMockMvcRequestPostProcessors.user(staffUser))
                            .param("status", "CONFIRMED"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("CONFIRMED"));

            mockMvc.perform(patch("/api/appointments/" + appointmentId + "/status")
                            .with(SecurityMockMvcRequestPostProcessors.user(staffUser))
                            .param("status", "COMPLETED"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("COMPLETED"));

            mockMvc.perform(patch("/api/appointments/" + appointmentId + "/status")
                            .with(SecurityMockMvcRequestPostProcessors.user(staffUser))
                            .param("status", "CONFIRMED"))
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("Workflow 3: Patient profile validation")
    class Workflow3 {

        @Test
        @DisplayName("Patient updates profile successfully")
        void patientUpdatesProfile() throws Exception {
            PatientRequest request = new PatientRequest();
            request.setFullName("Juan Dela Cruz Updated");
            request.setContactNumber("09171234567");

            mockMvc.perform(put("/api/patients/me")
                            .with(SecurityMockMvcRequestPostProcessors.user(patientUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.fullName").value("Juan Dela Cruz Updated"));
        }

        @Test
        @DisplayName("Patient tries to update with blank name - returns 400")
        void blankNameRejected() throws Exception {
            PatientRequest request = new PatientRequest();
            request.setFullName("");

            mockMvc.perform(put("/api/patients/me")
                            .with(SecurityMockMvcRequestPostProcessors.user(patientUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }
}

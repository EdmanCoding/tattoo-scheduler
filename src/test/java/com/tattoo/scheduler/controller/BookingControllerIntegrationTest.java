package com.tattoo.scheduler.controller;

import com.tattoo.scheduler.model.UserEntity;
import com.tattoo.scheduler.repository.UserRepository;
import com.tattoo.scheduler.util.TestData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Sql(scripts = "/test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
public class BookingControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;

    @Test
    void creatBooking_ShouldReturn201AndCorrectBookingTest() throws Exception {
        // Given
        UserEntity userEntity = userRepository.save(TestData.createTestUser1());
        String requestBody = """
                {
                "sessionType": "MEDIUM",
                "startTime": "2026-04-15T10:00:00",
                "notes": "This is a test booking",
                "imagePath": null
                }
                """;
        // When / Then
        mockMvc.perform(post("/api/bookings")
                .header("X-User-Id", userEntity.getId().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andDo(print())  // Optional: prints request/response for debugging
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.userId").value(userEntity.getId().toString()))
                .andExpect(jsonPath("$.sessionType").value("MEDIUM"))
                .andExpect(jsonPath("$.startTime").value("2026-04-15T10:00:00"))
                .andExpect(jsonPath("$.endTime").value("2026-04-15T14:00:00"));
    }
    @Test
    void userNotFound_ShouldReturn404Test() throws Exception {
        // Given
        String requestBody = """
                {
                "sessionType": "MEDIUM",
                "startTime": "2026-04-15T10:00:00"
                }
                """;
        // When / Then
        mockMvc.perform(post("/api/bookings")
                .header("X-User-Id", 483L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User with id 483 not found"));
    }
    @Test
    void conflict_ShouldReturn409Test() throws Exception {
        // Given
        UserEntity userEntity = userRepository.save(TestData.createTestUser1());
        String requestBody = """
                {
                "sessionType": "MEDIUM",
                "startTime": "2026-04-15T10:00:00",
                "notes": "existing booking"
                }
                """;
        // When / Then
        mockMvc.perform(post("/api/bookings")
                .header("X-User-Id", userEntity.getId().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.notes").value("existing booking"));

        String requestBodyConflict = """
                {
                "sessionType": "MEDIUM",
                "startTime": "2026-04-15T15:00:00",
                "notes": "conflicts with buffer of existing booking"
                }
                """;
        mockMvc.perform(post("/api/bookings")
                .header("X-User-Id", userEntity.getId().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBodyConflict))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        "Cannot book at 2026-04-15T15:00 for artist 1 due to conflict with existing booking"
                ));
    }
    @Test
    void validationError_ShouldReturn400Test() throws Exception {
        // Given
        UserEntity userEntity = userRepository.save(TestData.createTestUser1());
        String requestBody = """
                {
                "startTime": "2026-04-15T10:00:00",
                "notes": "sessionType missed"
                }
                """;
        mockMvc.perform(post("/api/bookings")
                .header("X-User-Id", userEntity.getId().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}

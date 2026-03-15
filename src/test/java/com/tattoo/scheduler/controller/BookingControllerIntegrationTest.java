package com.tattoo.scheduler.controller;

import com.tattoo.scheduler.model.User;
import com.tattoo.scheduler.repository.UserRepository;
import com.tattoo.scheduler.util.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static com.tattoo.scheduler.util.TestData.TEST_USER_ID;
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

    @BeforeEach
    void setUp() {
        User user = userRepository.save(TestData.createTestUser1());
    }

    @Test
    void creatBooking_ShouldReturn201AndCorrectBooking() throws Exception {
        // Given
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
                .header("X-User-Id", TEST_USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andDo(print())  // Optional: prints request/response for debugging
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.userId").value(TEST_USER_ID))
                .andExpect(jsonPath("$.sessionType").value("MEDIUM"))
                .andExpect(jsonPath("$.startTime").value("2026-04-15T10:00:00"))
                .andExpect(jsonPath("$.endTime").value("2026-04-15T14:00:00"));
    }
}

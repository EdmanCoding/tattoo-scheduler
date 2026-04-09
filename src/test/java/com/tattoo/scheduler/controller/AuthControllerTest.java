package com.tattoo.scheduler.controller;

import com.tattoo.scheduler.dto.LoginRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test-h2")
@Sql(scripts = "/test-data-with-user.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    // === Authentication tests (200 / 401) ===
    @Test
    void login_returnsToken_whenValidCredentials() throws Exception {
        LoginRequest request = new LoginRequest("testuser@example.com", "secret");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email").value("testuser@example.com"));
    }
    @Test
    void login_returns401_whenInvalidEmail() throws Exception {
        LoginRequest request = new LoginRequest("nonexistent@example.com", "secret");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
    @Test
    void login_returns401_whenInvalidPassword() throws Exception {
        LoginRequest request = new LoginRequest("testuser@example.com", "invalidPassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // === Validation tests (400) ===
    @Test
    void login_returns400_whenEmailMissing() throws Exception {
        LoginRequest request = new LoginRequest(null, "secret");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("email: Email is required"));
    }
    @Test
    void login_returns400_whenEmailMalformed() throws Exception {
        LoginRequest request = new LoginRequest(".com@testuser", "secret");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("email: Invalid email format"));
    }
    @Test
    void login_returns400_whenPasswordMissing() throws Exception {
        LoginRequest request = new LoginRequest("testuser@example.com", null);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("password: Password is required"));
    }
    @Test
    void login_returns400_whenEmailMalformedAndPasswordMissing() throws Exception {
        LoginRequest request = new LoginRequest(".com@testuser", null);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("email: Invalid email format, " +
                        "password: Password is required"));
    }
}

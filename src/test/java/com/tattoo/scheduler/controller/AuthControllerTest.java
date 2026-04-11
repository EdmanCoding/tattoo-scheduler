package com.tattoo.scheduler.controller;

import com.tattoo.scheduler.dto.auth.LoginRequest;
import com.tattoo.scheduler.dto.auth.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;

import static com.tattoo.scheduler.util.TestData.DEFAULT_BIRTH_DATE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test-h2")
@Sql(scripts = "/test-data-h2.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Transactional
public class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    // === Authentication tests (200 / 401) ===
    // === /api/auth/login/ ===
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

    // === /api/auth/register/ ===
    @Test
    void register_returns201_whenRegisterDataValid() throws Exception {
        RegisterRequest request = new RegisterRequest("Vasya", "vasya_huligan228@gmail.com",
                "qwerty12345", "8 800 555 35 35", DEFAULT_BIRTH_DATE);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email").value("vasya_huligan228@gmail.com"))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Vasya"));
    }
    @Test
    void register_returns409_whenEmailExists() throws Exception {
        RegisterRequest request = new RegisterRequest("Vasya", "testuser@example.com",
                "qwerty12345", "8 800 555 35 35", DEFAULT_BIRTH_DATE);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email already registered: testuser@example.com"));
    }
    @Test
    void register_returns201_whenAgeExactly18() throws Exception {
        RegisterRequest request = new RegisterRequest("Adult", "adult@example.com",
                "qwerty123", "123456789", LocalDate.now().minusYears(18));
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }
    @Test
    void register_returns201_whenPasswordExactly6Chars() throws Exception {
        RegisterRequest request = new RegisterRequest("SixChar", "six@example.com",
                "qwerty", "123456789", DEFAULT_BIRTH_DATE);
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    // === Validation tests (400) ===
    // === /api/auth/login/ ===
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
                .andExpect(result -> {
                    String message = result.getResponse().getContentAsString();
                    assertThat(message).contains("email: Invalid email format");
                    assertThat(message).contains("password: Password is required");
                });
    }

    // === /api/auth/register/ ===
    @Test
    void register_returns400_whenNameIsEmpty() throws Exception {
        RegisterRequest request = new RegisterRequest("", "vasya_huligan228@gmail.com",
                "qwerty12345", "8 800 555 35 35", DEFAULT_BIRTH_DATE);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("name: Name is required"));
    }
    @Test
    void register_returns400_whenEmailIsNull() throws Exception {
        RegisterRequest request = new RegisterRequest("Vasya", null,
                "qwerty12345", "8 800 555 35 35", DEFAULT_BIRTH_DATE);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("email: Email is required"));
    }
    @Test
    void register_returns400_whenPasswordIsNull() throws Exception {
        RegisterRequest request = new RegisterRequest("Vasya", "vasya_huligan228@gmail.com",
                null, "8 800 555 35 35", DEFAULT_BIRTH_DATE);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("password: Password is required"));
    }
    @Test
    void register_returns400_whenPhoneNumberIsEmpty() throws Exception {
        RegisterRequest request = new RegisterRequest("Vasya", "vasya_huligan228@gmail.com",
                "qwerty12345", "", DEFAULT_BIRTH_DATE);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("phoneNumber: Phone number is required"));
    }
    @Test
    void register_returns400_whenBirthDateInFuture() throws Exception {
        RegisterRequest request = new RegisterRequest("Vasya", "vasya_huligan228@gmail.com",
                "qwerty12345", "8 800 555 35 35", LocalDate.now().plusMonths(1));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> {
                    String message = result.getResponse().getContentAsString();
                    assertThat(message).contains("birthDate: Birth date must be in the past");
                });
    }
    @Test
    void register_returns400_whenBirthDateIsNull() throws Exception {
        RegisterRequest request = new RegisterRequest("Vasya", "vasya_huligan228@gmail.com",
                "qwerty12345", "8 800 555 35 35", null);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("birthDate: Birth date is required"));
    }
    @Test
    void register_returns400_whenAllFieldsInvalid() throws Exception {
        RegisterRequest request = new RegisterRequest(null, "vasyagmail.com",
                "qwert", null, LocalDate.now().minusYears(17).plusMonths(10));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> {
                    String message = result.getResponse().getContentAsString();
                    assertThat(message).contains("birthDate: You must be at least 18 years old");
                    assertThat(message).contains("phoneNumber: Phone number is required");
                    assertThat(message).contains("name: Name is required");
                    assertThat(message).contains("password: Password must be at least 6 characters");
                    assertThat(message).contains("email: Invalid email format");
                });
    }
}

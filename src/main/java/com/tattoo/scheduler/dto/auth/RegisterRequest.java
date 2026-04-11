package com.tattoo.scheduler.dto.auth;


import com.tattoo.scheduler.validation.AgeValidator;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record RegisterRequest(
    @NotBlank(message = "Name is required")
    String name,

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email,

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    String password,

    @NotBlank(message = "Phone number is required")
    String phoneNumber,

    @Past(message = "Birth date must be in the past")
    @NotNull(message = "Birth date is required")
    @AgeValidator(min = 18, message = "You must be at least 18 years old")
    LocalDate birthDate

    ){}

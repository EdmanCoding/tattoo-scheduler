package com.tattoo.scheduler.dto.auth;

public record AuthResponse(
        String token,
        String email
) {}

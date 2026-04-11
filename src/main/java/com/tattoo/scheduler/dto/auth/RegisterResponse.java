package com.tattoo.scheduler.dto.auth;

public record RegisterResponse(
        String token,
        Long id,
        String email,
        String name
) {}

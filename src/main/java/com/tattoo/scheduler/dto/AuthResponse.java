package com.tattoo.scheduler.dto;

public record AuthResponse(
        String token,
        String email
) {}

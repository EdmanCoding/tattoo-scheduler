package com.tattoo.scheduler.dto;

import com.tattoo.scheduler.model.SessionType;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

// For creating a booking (POST)
public record CreateBookingRequest (
    @NotNull SessionType sessionType,
    @NotNull LocalDateTime startTime,
    String notes,
    String imagePath
){}

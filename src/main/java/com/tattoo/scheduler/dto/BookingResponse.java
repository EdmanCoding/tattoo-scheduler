package com.tattoo.scheduler.dto;

import com.tattoo.scheduler.model.BookingStatus;
import com.tattoo.scheduler.model.SessionType;

import java.time.LocalDateTime;

public record BookingResponse(
        Long id,
        Long userId,
        Long artistId,
        SessionType sessionType,
        LocalDateTime startTime,
        LocalDateTime endTime,
        LocalDateTime endOfBufferTime,
        BookingStatus status,
        String notes,
        String imagePath,
        LocalDateTime createdAt
){}

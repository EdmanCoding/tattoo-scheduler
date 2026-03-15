package com.tattoo.scheduler.dto;

import com.tattoo.scheduler.model.Booking;
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
        LocalDateTime createdAt ) {
        // Static factory method to create from entity
        public static BookingResponse from(Booking booking) {
                return new BookingResponse(
                        booking.getId(),
                        booking.getUser().getId(),
                        booking.getArtist().getId(),
                        booking.getSessionType(),
                        booking.getStartTime(),
                        booking.getEndTime(),
                        booking.getEndOfBufferTime(),
                        booking.getStatus(),
                        booking.getNotes(),
                        booking.getImagePath(),
                        booking.getCreatedAt());
        }
}

package com.tattoo.scheduler.dto;

import com.tattoo.scheduler.model.BookingEntity;
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
        public static BookingResponse from(BookingEntity bookingEntity) {
                return new BookingResponse(
                        bookingEntity.getId(),
                        bookingEntity.getUserEntity().getId(),
                        bookingEntity.getArtistEntity().getId(),
                        bookingEntity.getSessionType(),
                        bookingEntity.getStartTime(),
                        bookingEntity.getEndTime(),
                        bookingEntity.getEndOfBufferTime(),
                        bookingEntity.getStatus(),
                        bookingEntity.getNotes(),
                        bookingEntity.getImagePath(),
                        bookingEntity.getCreatedAt());
        }
}

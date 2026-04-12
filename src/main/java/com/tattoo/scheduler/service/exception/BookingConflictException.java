package com.tattoo.scheduler.service.exception;

import java.time.LocalDateTime;

public class BookingConflictException extends RuntimeException {

    public BookingConflictException(Long artistId, LocalDateTime requestedStart) {
        super(String.format("Cannot book at %s for artist %d due to conflict with existing booking",
                requestedStart, artistId));
    }

    public BookingConflictException(Long artistId, LocalDateTime requestedStart, String message) {
        super(String.format("Cannot book at %s for artist %d: %s",
                requestedStart, artistId, message));
    }
}

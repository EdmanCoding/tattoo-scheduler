package com.tattoo.scheduler.service.exception;

import java.time.LocalDateTime;

public class BookingConflictException extends RuntimeException {
    // Simple message constructor
    public BookingConflictException(String message) {
        super(message);
    }
    // Constructor with artist and requested time
    public BookingConflictException(Long artistId, LocalDateTime requestedStart) {
        super(String.format("Cannot book at %s for artist %d due to conflict with existing booking",
                requestedStart, artistId));
    }
    // Constructor with full details
    public BookingConflictException(Long artistId,
                                    LocalDateTime requestedStart,
                                    LocalDateTime conflictingStart) {
        super(String.format("Requested slot at %s conflicts with existing booking at %s for artist %d",
                requestedStart, conflictingStart, artistId));
    }
}

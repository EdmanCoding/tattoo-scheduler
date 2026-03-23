package com.tattoo.scheduler.service.exception;

import java.time.LocalDateTime;

public class BookingOutsideWorkingHoursException extends RuntimeException {
    public BookingOutsideWorkingHoursException(LocalDateTime start) {
        super(String.format("Session starting at %s must be within 10:00–20:00 and finish by 20:00",
                start.toLocalTime()));
    }
}

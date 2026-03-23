package com.tattoo.scheduler.service.exception;

import java.time.LocalDate;

public class BookingDateNotAllowedException extends RuntimeException {
    public BookingDateNotAllowedException(LocalDate date) {
        super("Booking cannot be made on " + date + ". Only tomorrow onwards is allowed.");
    }
}

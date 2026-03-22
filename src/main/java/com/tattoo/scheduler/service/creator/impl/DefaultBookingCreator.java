package com.tattoo.scheduler.service.creator.impl;

import com.tattoo.scheduler.domain.Booking;
import com.tattoo.scheduler.service.creator.BookingCreator;
import com.tattoo.scheduler.service.validator.BookingValidator;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DefaultBookingCreator implements BookingCreator {
    private final BookingValidator validator;

    public DefaultBookingCreator(BookingValidator validator) {
        this.validator = validator;
    }

    @Override
    public Booking enrichAndValidate(Booking booking) {
        // Calculate times
        LocalDateTime startTime = booking.getStartTime();
        booking.setEndTime(startTime.plusMinutes(
                booking.getSessionType().getDurationMinutes()));
        booking.setEndOfBufferTime(startTime.plusMinutes(
                booking.getSessionType().getBufferAfterMinutes()));

        // Validate everything
        validator.validate(booking);
        return booking;
    }
}

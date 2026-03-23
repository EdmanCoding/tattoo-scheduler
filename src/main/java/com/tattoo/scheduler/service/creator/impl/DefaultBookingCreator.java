package com.tattoo.scheduler.service.creator.impl;

import com.tattoo.scheduler.domain.Booking;
import com.tattoo.scheduler.model.BookingStatus;
import com.tattoo.scheduler.service.creator.BookingCreator;
import com.tattoo.scheduler.service.resolver.ArtistResolver;
import com.tattoo.scheduler.service.validator.BookingValidator;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DefaultBookingCreator implements BookingCreator {
    private final BookingValidator validator;
    private final ArtistResolver artistResolver;

    public DefaultBookingCreator(BookingValidator validator, ArtistResolver artistResolver) {
        this.validator = validator;
        this.artistResolver = artistResolver;
    }

    @Override
    public Booking enrichAndValidate(Booking booking) {
        // Calculate times
        LocalDateTime startTime = booking.getStartTime();
        booking.setEndTime(startTime.plusMinutes(
                booking.getSessionType().getDurationMinutes()));
        booking.setEndOfBufferTime(booking.getEndTime().plusMinutes(
                booking.getSessionType().getBufferAfterMinutes()));

        // Set status
        booking.setStatus(BookingStatus.PENDING);

        // Set default artist if not provided
        if (booking.getArtistId() == null)
            booking.setArtistId(artistResolver.getDefaultArtistId());

        // Validate everything
        validator.validate(booking);
        return booking;
    }
}

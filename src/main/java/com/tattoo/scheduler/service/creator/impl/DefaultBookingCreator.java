package com.tattoo.scheduler.service.creator.impl;

import com.tattoo.scheduler.domain.Booking;
import com.tattoo.scheduler.model.BookingStatus;
import com.tattoo.scheduler.service.creator.BookingCreator;
import com.tattoo.scheduler.service.resolver.ArtistResolver;
import com.tattoo.scheduler.service.validator.BookingValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DefaultBookingCreator implements BookingCreator {
    private final BookingValidator validator;
    private final ArtistResolver artistResolver;

    @Override
    public Booking enrichAndValidate(Booking booking) {
        LocalDateTime startTime = booking.getStartTime();
        booking.setEndTime(startTime.plusMinutes(
                booking.getSessionType().getDurationMinutes()));
        booking.setEndOfBufferTime(booking.getEndTime().plusMinutes(
                booking.getSessionType().getBufferAfterMinutes()));

        booking.setStatus(BookingStatus.PENDING);

        if (booking.getArtistId() == null)
            booking.setArtistId(artistResolver.getDefaultArtistId());

        validator.validate(booking);
        return booking;
    }
}

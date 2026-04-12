package com.tattoo.scheduler.service.validator.impl;

import com.tattoo.scheduler.domain.Booking;
import com.tattoo.scheduler.repository.ArtistRepository;
import com.tattoo.scheduler.service.exception.ArtistNotFoundException;
import com.tattoo.scheduler.service.exception.BookingConflictException;
import com.tattoo.scheduler.service.exception.BookingDateNotAllowedException;
import com.tattoo.scheduler.service.exception.BookingOutsideWorkingHoursException;
import com.tattoo.scheduler.service.fetcher.BookingFetcher;
import com.tattoo.scheduler.service.policy.BookingPolicy;
import com.tattoo.scheduler.service.validator.BookingValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Component
@RequiredArgsConstructor
public class DefaultBookingValidator implements BookingValidator {
    private final BookingPolicy policy;
    private final ArtistRepository artistRepository;
    private final BookingFetcher bookingFetcher;

    public void validate(Booking booking) {
        if (!artistRepository.existsById(booking.getArtistId()))
            throw new ArtistNotFoundException(booking.getArtistId());

        LocalDateTime start = booking.getStartTime();
        LocalDate date = start.toLocalDate();
        if (!policy.isDateAllowed(date))
            throw new BookingDateNotAllowedException(date);

        if (!policy.isWithinWorkingHours(start, booking.getSessionType()))
            throw new BookingOutsideWorkingHoursException(start);

        List<Booking> existing = bookingFetcher.getActiveBookingsForDay(booking.getArtistId(), date);

        if (!policy.respectsLargeExclusivity(booking.getSessionType(), existing))
            throw new BookingConflictException(booking.getArtistId(), start,
                    "LARGE session rules violated");

        if (!policy.hasNoConflict(start, booking.getSessionType(), existing))
            throw new BookingConflictException(booking.getArtistId(), start);
    }
}

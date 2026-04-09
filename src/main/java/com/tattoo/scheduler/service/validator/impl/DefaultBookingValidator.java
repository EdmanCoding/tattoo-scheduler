package com.tattoo.scheduler.service.validator.impl;

import com.tattoo.scheduler.domain.Booking;
import com.tattoo.scheduler.repository.ArtistRepository;
import com.tattoo.scheduler.repository.UserRepository;
import com.tattoo.scheduler.service.exception.*;
import com.tattoo.scheduler.service.fetcher.BookingFetcher;
import com.tattoo.scheduler.service.policy.BookingPolicy;
import com.tattoo.scheduler.service.validator.BookingValidator;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Component
public class DefaultBookingValidator implements BookingValidator {
    private final BookingPolicy policy;
    private final UserRepository userRepository;
    private final ArtistRepository artistRepository;
    private final BookingFetcher bookingFetcher;

    public DefaultBookingValidator(BookingPolicy policy,
                                   UserRepository userRepository,
                                   ArtistRepository artistRepository,
                                   BookingFetcher bookingFetcher) {
        this.policy = policy;
        this.userRepository = userRepository;
        this.artistRepository = artistRepository;
        this.bookingFetcher = bookingFetcher;
    }

    public void validate(Booking booking) {
        // 1. Artist exists?
        if (!artistRepository.existsById(booking.getArtistId()))
            throw new ArtistNotFoundException(booking.getArtistId());

        // 2. Date allowed?
        LocalDateTime start = booking.getStartTime();
        LocalDate date = start.toLocalDate();
        if (!policy.isDateAllowed(date))
            throw new BookingDateNotAllowedException(date);

        // 3. Within working hours?
        if (!policy.isWithinWorkingHours(start, booking.getSessionType()))
            throw new BookingOutsideWorkingHoursException(start);

        // 4. Fetch existing bookings for the day
        List<Booking> existing = bookingFetcher.getActiveBookingsForDay(booking.getArtistId(), date);

        // 5. LARGE exclusivity
        if (!policy.respectsLargeExclusivity(booking.getSessionType(), existing))
            throw new BookingConflictException(booking.getArtistId(), start,
                    "LARGE session rules violated");

        // 6. Conflict with existing bookings
        if (!policy.hasNoConflict(start, booking.getSessionType(), existing))
            throw new BookingConflictException(booking.getArtistId(), start);
    }
}

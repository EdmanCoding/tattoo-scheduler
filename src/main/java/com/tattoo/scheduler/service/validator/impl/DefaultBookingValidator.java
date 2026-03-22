package com.tattoo.scheduler.service.validator.impl;

import com.tattoo.scheduler.domain.Booking;
import com.tattoo.scheduler.mapper.BookingMapper;
import com.tattoo.scheduler.model.BookingEntity;
import com.tattoo.scheduler.model.BookingStatus;
import com.tattoo.scheduler.repository.ArtistRepository;
import com.tattoo.scheduler.repository.BookingRepository;
import com.tattoo.scheduler.repository.UserRepository;
import com.tattoo.scheduler.service.exception.ArtistNotFoundException;
import com.tattoo.scheduler.service.exception.BookingConflictException;
import com.tattoo.scheduler.service.exception.UserNotFoundException;
import com.tattoo.scheduler.service.policy.BookingPolicy;
import com.tattoo.scheduler.service.validator.BookingValidator;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.tattoo.scheduler.service.constants.BookingConstants.WORK_END_HOUR;
import static com.tattoo.scheduler.service.constants.BookingConstants.WORK_START_HOUR;

@Component
public class DefaultBookingValidator implements BookingValidator {
    private final BookingPolicy policy;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ArtistRepository artistRepository;
    private final BookingMapper bookingMapper;

    public DefaultBookingValidator(BookingPolicy policy, BookingRepository bookingRepository,
                                   UserRepository userRepository, ArtistRepository artistRepository,
                                   BookingMapper bookingMapper) {
        this.policy = policy;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.artistRepository = artistRepository;
        this.bookingMapper = bookingMapper;
    }

    public void validate(Booking booking) {
        // 1. User exists?
        if(!userRepository.existsById(booking.getUserId()))
            throw new UserNotFoundException(booking.getUserId());

        // 2. Artist exists?
        if(!artistRepository.existsById(booking.getArtistId()))
            throw new ArtistNotFoundException(booking.getArtistId());

        // 3. Date allowed?
        LocalDateTime start = booking.getStartTime();
        LocalDate date = start.toLocalDate();
        if(!policy.isDateAllowed(date))
            throw new IllegalArgumentException("Booking can only be made from tomorrow onwards");

        // 4. Within working hours?
        if(!policy.isWithinWorkingHours(start, booking.getSessionType()))
            throw new IllegalArgumentException("Session must be within 10:00–20:00 and finish by 20:00");

        // 5. Fetch existing bookings for the day
        LocalDateTime dayStart = date.atTime(WORK_START_HOUR, 0);
        LocalDateTime dayEnd = date.atTime(WORK_END_HOUR, 0);
        List<BookingEntity> entities = bookingRepository.findOccupiedIntervals(
                booking.getArtistId(), dayStart, dayEnd, BookingStatus.CANCELLED);
        List<Booking> existing = entities.stream()
                .map(bookingMapper::toDomain).toList();

        // 6. LARGE exclusivity
        if (!policy.respectsLargeExclusivity(booking.getSessionType(), existing))
            throw new BookingConflictException(booking.getArtistId(), start,
                    "LARGE session rules violated");

        // 7. Conflict with existing bookings
        if (!policy.hasNoConflict(start, booking.getSessionType(), existing))
            throw new BookingConflictException(booking.getArtistId(), start);
    }
}

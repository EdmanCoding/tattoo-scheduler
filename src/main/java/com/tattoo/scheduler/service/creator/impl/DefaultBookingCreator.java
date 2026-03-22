package com.tattoo.scheduler.service.creator.impl;

import com.tattoo.scheduler.domain.Booking;
import com.tattoo.scheduler.mapper.BookingMapper;
import com.tattoo.scheduler.model.BookingEntity;
import com.tattoo.scheduler.model.BookingStatus;
import com.tattoo.scheduler.repository.BookingRepository;
import com.tattoo.scheduler.service.creator.BookingCreator;
import com.tattoo.scheduler.service.exception.BookingConflictException;
import com.tattoo.scheduler.service.policy.BookingPolicy;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.tattoo.scheduler.service.constants.BookingConstants.WORK_END_HOUR;
import static com.tattoo.scheduler.service.constants.BookingConstants.WORK_START_HOUR;

@Component
public class DefaultBookingCreator implements BookingCreator {
    private final BookingPolicy policy;
    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;

    public DefaultBookingCreator(BookingPolicy policy,
                                 BookingRepository bookingRepository,
                                 BookingMapper bookingMapper) {
        this.policy = policy;
        this.bookingRepository = bookingRepository;
        this.bookingMapper = bookingMapper;
    }

    @Override
    public Booking enrichAndValidate(Booking booking) {
        // 1. Calculate times
        LocalDateTime start = booking.getStartTime();
        booking.setEndTime(start.plusMinutes(booking
                .getSessionType().getDurationMinutes()));
        booking.setEndOfBufferTime(start.plusMinutes(booking
                .getSessionType().getBufferAfterMinutes()));

        // 2. Date allowed?
        LocalDate date = start.toLocalDate();
        if(!policy.isDateAllowed(date))
            throw new IllegalArgumentException("Booking can only be made from tomorrow onwards");

        // 3. Within working hours?
        if(!policy.isWithinWorkingHours(start, booking.getSessionType()))
            throw new IllegalArgumentException("Session must be within 10:00–20:00 and finish by 20:00");

        // 4. Fetch existing bookings for the day
        LocalDateTime dayStart = date.atTime(WORK_START_HOUR, 0);
        LocalDateTime dayEnd = date.atTime(WORK_END_HOUR, 0);
        List<BookingEntity> entities = bookingRepository.findOccupiedIntervals(
                booking.getArtistId(), dayStart, dayEnd, BookingStatus.CANCELLED);
        List<Booking> existing = entities.stream()
                .map(bookingMapper::toDomain).toList();

        // 5. LARGE exclusivity
        if(!policy.respectsLargeExclusivity(booking.getSessionType(),existing))
            throw new IllegalArgumentException("LARGE session rules violated");

        // 6. Conflict with existing booking
        if(!policy.hasNoConflict(start, booking.getSessionType(), existing))
            throw new BookingConflictException(booking.getArtistId(), start);

        return booking;
    }
}

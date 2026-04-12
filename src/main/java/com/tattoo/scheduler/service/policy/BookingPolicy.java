package com.tattoo.scheduler.service.policy;

import com.tattoo.scheduler.domain.Booking;
import com.tattoo.scheduler.model.SessionType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface BookingPolicy {
    /** Returns true if a booking can be made on the given date. */
    boolean isDateAllowed(LocalDate date);

    /** Returns true if a session starting at the given time respects working hours. */
    boolean isWithinWorkingHours(LocalDateTime start, SessionType type);

    /**
     * Returns true if a new session (with its buffer) does NOT conflict with any existing booking.
     * @param start requested start time
     * @param type session type
     * @param existingBookings list of active bookings (non‑cancelled)
     */
    boolean hasNoConflict(LocalDateTime start, SessionType type, List<Booking> existingBookings);

    /**
     * Returns true if the day (with existing bookings) respects LARGE session rules.
     * @param newType type of the new booking (or null if just checking existing day)
     * @param existingBookings list of active bookings (non‑cancelled) for that day
     */
    boolean respectsLargeExclusivity(SessionType newType, List<Booking> existingBookings);
}

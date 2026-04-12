package com.tattoo.scheduler.service.validator;

import com.tattoo.scheduler.domain.Booking;

/**
 * Validates a booking before creation.
 * <p>
 * Checks business rules: artist existence, date allowance, working hours,
 * LARGE session exclusivity, and conflicts with existing bookings.
 */
public interface BookingValidator {
    /**
     * Validates the booking.
     *
     * @param booking the booking to validate
     * @throws com.tattoo.scheduler.service.exception.ArtistNotFoundException if artist doesn't exist
     * @throws com.tattoo.scheduler.service.exception.BookingDateNotAllowedException if date is invalid
     * @throws com.tattoo.scheduler.service.exception.BookingOutsideWorkingHoursException if outside working hours
     * @throws com.tattoo.scheduler.service.exception.BookingConflictException if rules are violated
     */
    void validate(Booking booking);
}

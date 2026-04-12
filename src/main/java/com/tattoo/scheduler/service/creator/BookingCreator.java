package com.tattoo.scheduler.service.creator;

import com.tattoo.scheduler.domain.Booking;

/**
 * Enriches a booking request with calculated fields and validates it.
 * <p>
 * Responsibilities:
 * <ul>
 *     <li>Calculates end time based on session duration</li>
 *     <li>Calculates buffer end time based on session type</li>
 *     <li>Sets initial status to PENDING</li>
 *     <li>Assigns default artist if none provided</li>
 *     <li>Delegates validation to {@link com.tattoo.scheduler.service.validator.BookingValidator}</li>
 * </ul>
 */
public interface BookingCreator {
    /**
     * Enriches and validates a booking.
     * <p>
     * The returned booking has all time fields populated and is ready for persistence.
     *
     * @param booking the initial booking with minimal fields (startTime, sessionType, userId, optional artistId)
     * @return the enriched booking with endTime, endOfBufferTime, status, and default artist if needed
     * @throws com.tattoo.scheduler.service.exception.BookingDateNotAllowedException if date invalid
     * @throws com.tattoo.scheduler.service.exception.BookingOutsideWorkingHoursException if outside working hours
     * @throws com.tattoo.scheduler.service.exception.BookingConflictException if conflicts with existing bookings
     * @throws com.tattoo.scheduler.service.exception.ArtistNotFoundException if artist doesn't exist
     */
    Booking enrichAndValidate(Booking booking);
}

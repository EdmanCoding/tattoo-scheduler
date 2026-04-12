package com.tattoo.scheduler.service.fetcher;

import com.tattoo.scheduler.domain.Booking;

import java.time.LocalDate;
import java.util.List;

/**
 * Retrieves active bookings for a specific artist and date.
 * <p>
 * Excludes cancelled bookings as they do not occupy time slots.
 */
public interface BookingFetcher {
    /**
     * Returns all active (non‑cancelled) bookings for the given artist on the given date.
     *
     * @param artistId the ID of the artist
     * @param date the date to fetch bookings for
     * @return list of active bookings, never null
     */
    List<Booking> getActiveBookingsForDay(Long artistId, LocalDate date);
}

package com.tattoo.scheduler.service.slot;

import com.tattoo.scheduler.domain.Booking;
import com.tattoo.scheduler.model.SessionType;

import java.time.LocalDateTime;
import java.util.List;

public interface SlotGenerator {
    /**
     * Generates available start times for a given day and session type.
     * @param dayStart start of the day (e.g., 10:00)
     * @param dayEnd end of the day (e.g., 20:00)
     * @param sessionType the type of session to book
     * @param existingBookings list of active bookings on that day (non‑cancelled)
     * @return list of available start times (LocalDateTime)
     */
    List<LocalDateTime> generate(LocalDateTime dayStart, LocalDateTime dayEnd,
                                 SessionType sessionType, List<Booking> existingBookings);
}

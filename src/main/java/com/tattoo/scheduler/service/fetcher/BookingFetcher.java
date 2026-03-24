package com.tattoo.scheduler.service.fetcher;

import com.tattoo.scheduler.domain.Booking;

import java.time.LocalDate;
import java.util.List;

public interface BookingFetcher {
    List<Booking> getActiveBookingsForDay(Long artistId, LocalDate date);
}

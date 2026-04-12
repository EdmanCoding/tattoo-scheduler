package com.tattoo.scheduler.service;

import com.tattoo.scheduler.domain.Booking;
import com.tattoo.scheduler.model.SessionType;
import com.tattoo.scheduler.service.fetcher.BookingFetcher;
import com.tattoo.scheduler.service.policy.BookingPolicy;
import com.tattoo.scheduler.service.resolver.ArtistResolver;
import com.tattoo.scheduler.service.slot.SlotGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static com.tattoo.scheduler.service.constants.BookingConstants.WORK_END_HOUR;
import static com.tattoo.scheduler.service.constants.BookingConstants.WORK_START_HOUR;

@Service
@RequiredArgsConstructor
public class AvailabilityService {
    private final ArtistResolver artistResolver;
    private final BookingPolicy bookingPolicy;
    private final SlotGenerator slotGenerator;
    private final BookingFetcher bookingFetcher;

    public List<LocalDateTime> getAvailableStartTimes(LocalDate date,
                                                      SessionType sessionType,
                                                      Long artistId) {
        if (!bookingPolicy.isDateAllowed(date))
            return Collections.emptyList();

        Long resolvedArtistId = artistId != null ? artistId : artistResolver.getDefaultArtistId();

        LocalDateTime dayStart = date.atTime(WORK_START_HOUR, 0);
        LocalDateTime dayEnd = date.atTime(WORK_END_HOUR, 0);

        List<Booking> existingBookings = bookingFetcher.getActiveBookingsForDay(
                resolvedArtistId, date);

        if (!bookingPolicy.respectsLargeExclusivity(sessionType, existingBookings))
            return Collections.emptyList();

        return slotGenerator.generate(dayStart, dayEnd, sessionType, existingBookings);
    }
}

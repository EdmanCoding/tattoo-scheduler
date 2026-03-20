package com.tattoo.scheduler.service;

import com.tattoo.scheduler.model.Artist;
import com.tattoo.scheduler.model.Booking;
import com.tattoo.scheduler.model.BookingStatus;
import com.tattoo.scheduler.model.SessionType;
import com.tattoo.scheduler.repository.BookingRepository;
import com.tattoo.scheduler.service.resolver.ArtistResolver;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AvailabilityService {
    private final BookingRepository bookingRepository;
    private final ArtistResolver artistResolver;
    private static final int SLOT_GRANULARITY_MINUTES = 15;
    private static final int WORK_START_HOUR = 10;
    private static final int WORK_END_HOUR = 20;

    public AvailabilityService(BookingRepository bookingRepository,
                               ArtistResolver artistResolver) {
        this.bookingRepository = bookingRepository;
        this.artistResolver = artistResolver;
    }

    public List<LocalDateTime> getAvailableStartTimes(LocalDate date,
                                                      SessionType sessionType, Long artistId) {
        // Resolver handles null -> default
        Artist artist = artistResolver.getArtist(artistId);

        // Define working hours
        LocalDateTime dayStart = date.atTime(WORK_START_HOUR, 0);
        LocalDateTime dayEnd = date.atTime(WORK_END_HOUR, 0);

        // Calculate durations
        long duration = sessionType.getDurationMinutes();
        long buffer = sessionType.getBufferAfterMinutes();

        // Fetch existing bookings for this artist on this day
        List<Booking> existingBookings = bookingRepository.findOccupiedIntervals(
                artist.getId(), dayStart, dayEnd, BookingStatus.CANCELLED);

        // Generate available slots
        return generateAvailableSlots(dayStart, dayEnd, duration, buffer, existingBookings);
    }

    private List<LocalDateTime> generateAvailableSlots(LocalDateTime dayStart,
                                                       LocalDateTime dayEnd,
                                                       long duration,
                                                       long buffer,
                                                       List<Booking> existingBookings) {
        List<LocalDateTime> availableSlots = new ArrayList<>();
        LocalDateTime candidate = dayStart;
        long totalBlock = duration + buffer;

        while (candidate.plusMinutes(duration).isBefore(dayEnd) ||
                candidate.plusMinutes(duration).isEqual(dayEnd)) {
            LocalDateTime protectedEnd = candidate.plusMinutes(totalBlock);

            // Check if candidate conflicts with any existing booking
            boolean conflict = hasConflict(candidate, protectedEnd, existingBookings);
            if (!conflict) {
                availableSlots.add(candidate);
                candidate = protectedEnd; // Jump over occupied block
            } else {
                candidate = candidate.plusMinutes(SLOT_GRANULARITY_MINUTES);
            }
        }
        return availableSlots;
    }
    private boolean hasConflict(LocalDateTime start, LocalDateTime protectedEnd,
                                List<Booking> existingBookings) {
        return existingBookings.stream().anyMatch(booking ->
                start.isBefore(booking.getEndOfBufferTime()) &&
                        protectedEnd.isAfter(booking.getStartTime())
        );
    }
}

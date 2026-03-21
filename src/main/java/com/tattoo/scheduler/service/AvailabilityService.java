package com.tattoo.scheduler.service;

import com.tattoo.scheduler.model.Artist;
import com.tattoo.scheduler.model.Booking;
import com.tattoo.scheduler.model.BookingStatus;
import com.tattoo.scheduler.model.SessionType;
import com.tattoo.scheduler.repository.BookingRepository;
import com.tattoo.scheduler.service.policy.BookingPolicy;
import com.tattoo.scheduler.service.resolver.ArtistResolver;
import com.tattoo.scheduler.service.slot.SlotGenerator;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static com.tattoo.scheduler.service.constants.BookingConstants.WORK_END_HOUR;
import static com.tattoo.scheduler.service.constants.BookingConstants.WORK_START_HOUR;

@Service
public class AvailabilityService {
    private final BookingRepository bookingRepository;
    private final ArtistResolver artistResolver;
    private final BookingPolicy bookingPolicy;;
    private final SlotGenerator slotGenerator;

    public AvailabilityService(BookingRepository bookingRepository,
                               ArtistResolver artistResolver,
                               BookingPolicy bookingPolicy,
                               SlotGenerator slotGenerator) {
        this.bookingRepository = bookingRepository;
        this.artistResolver = artistResolver;
        this.bookingPolicy = bookingPolicy;
        this.slotGenerator = slotGenerator;
    }

    public List<LocalDateTime> getAvailableStartTimes(LocalDate date,
                                                      SessionType sessionType,
                                                      Long artistId) {
        if(!bookingPolicy.isDateAllowed(date))
            return Collections.emptyList();

        Artist artist = artistResolver.getArtist(artistId);
        LocalDateTime dayStart = date.atTime(WORK_START_HOUR, 0);
        LocalDateTime dayEnd = date.atTime(WORK_END_HOUR, 0);

        List<Booking> existingBookings = bookingRepository.findOccupiedIntervals(
                artist.getId(), dayStart, dayEnd, BookingStatus.CANCELLED);

        if(!bookingPolicy.respectsLargeExclusivity(sessionType, existingBookings))
            return Collections.emptyList();

        return slotGenerator.generate(dayStart, dayEnd, sessionType, existingBookings);
    }
}

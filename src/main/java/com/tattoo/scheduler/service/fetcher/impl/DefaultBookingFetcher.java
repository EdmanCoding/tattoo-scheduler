package com.tattoo.scheduler.service.fetcher.impl;

import com.tattoo.scheduler.domain.Booking;
import com.tattoo.scheduler.mapper.BookingMapper;
import com.tattoo.scheduler.model.BookingEntity;
import com.tattoo.scheduler.model.BookingStatus;
import com.tattoo.scheduler.repository.BookingRepository;
import com.tattoo.scheduler.service.fetcher.BookingFetcher;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.tattoo.scheduler.service.constants.BookingConstants.WORK_END_HOUR;
import static com.tattoo.scheduler.service.constants.BookingConstants.WORK_START_HOUR;

@Component
public class DefaultBookingFetcher implements BookingFetcher {
    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    public DefaultBookingFetcher(BookingRepository bookingRepository,
                                 BookingMapper bookingMapper) {
        this.bookingRepository = bookingRepository;
        this.bookingMapper = bookingMapper;
    }
    @Override
    public List<Booking> getActiveBookingsForDay (Long artistId, LocalDate date){
        LocalDateTime dayStart = date.atTime(WORK_START_HOUR, 0);
        LocalDateTime dayEnd = date.atTime(WORK_END_HOUR, 0);

        List<BookingEntity> entities = bookingRepository.findOccupiedIntervals(
                artistId, dayStart, dayEnd, BookingStatus.CANCELLED);

        return entities.stream().map(bookingMapper::toDomain).toList();
    }
}

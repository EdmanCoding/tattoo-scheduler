package com.tattoo.scheduler.service;

import com.tattoo.scheduler.model.Booking;
import com.tattoo.scheduler.model.BookingStatus;
import com.tattoo.scheduler.repository.BookingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingService {
    private final BookingRepository bookingRepository;
    public BookingService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }
    @Transactional
    public Booking createBooking(Booking booking) {
        // 1. Auto-calculate endTime if not provided
        if (booking.getEndTime() == null && booking.getSessionType() != null) {
            booking.setEndTime(booking.getStartTime()
                    .plusMinutes(booking.getSessionType().getDurationMinutes()));
        }
        // 2. Validate required fields
        if (booking.getArtist() == null || booking.getUser() == null) {
            throw new IllegalArgumentException("Artist and User are required");
        }
        // 3. Check for overlapping bookings (DB-level query)
        boolean conflict = bookingRepository.hasOverlap(
                booking.getArtist().getId(),
                booking.getStartTime(),
                booking.getEndTime(),
                BookingStatus.CANCELLED
        );
        if (conflict) {
            throw new IllegalArgumentException("Time slot already booked");
        }

        return bookingRepository.save(booking);
    }
}

package com.tattoo.scheduler.service;

import com.tattoo.scheduler.dto.BookingResponse;
import com.tattoo.scheduler.dto.CreateBookingRequest;
import com.tattoo.scheduler.model.Booking;
import com.tattoo.scheduler.model.BookingStatus;
import com.tattoo.scheduler.model.User;
import com.tattoo.scheduler.repository.ArtistRepository;
import com.tattoo.scheduler.repository.BookingRepository;
import com.tattoo.scheduler.repository.UserRepository;
import com.tattoo.scheduler.service.exception.BookingConflictException;
import com.tattoo.scheduler.service.exception.UserNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ArtistRepository artistRepository;
    public BookingService(BookingRepository bookingRepository,
                          UserRepository userRepository,
                          ArtistRepository artistRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.artistRepository = artistRepository;
    }
    @Transactional
    public BookingResponse createBooking(Long userId, CreateBookingRequest request) {
        // 1. Auto-calculate endTime
        Booking booking = new Booking();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        booking.setUser(user);
        booking.setArtist(artistRepository.getReferenceById(1L));
        booking.setStartTime(request.startTime());
        booking.setSessionType(request.sessionType());
        booking.setNotes(request.notes());
        booking.setImagePath(request.imagePath());
        booking.setEndTime(request.startTime()
                .plusMinutes(request.sessionType().getDurationMinutes()));

        // 2. Check for overlapping bookings taking into account buffer time (DB-level query)
        boolean conflict = bookingRepository.hasOverlap(
                booking.getArtist().getId(),
                booking.getStartTime(),
                booking.getEndTime().plusMinutes(request.sessionType().getBufferAfterMinutes()),
                BookingStatus.CANCELLED
        );
        if (conflict) {
            throw new BookingConflictException(
                    booking.getArtist().getId(),
                    booking.getStartTime()
            );
        }

        Booking savedBooking = bookingRepository.save(booking);
        return BookingResponse.from(savedBooking);
    }
}

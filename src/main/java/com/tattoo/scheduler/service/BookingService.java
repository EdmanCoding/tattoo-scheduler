package com.tattoo.scheduler.service;

import com.tattoo.scheduler.dto.BookingResponse;
import com.tattoo.scheduler.dto.CreateBookingRequest;
import com.tattoo.scheduler.model.Artist;
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

import java.time.LocalDateTime;

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
        // 1. create booking. Auto-calculate endTime and bufferTime
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        Artist artist = artistRepository.getReferenceById(1L);

        LocalDateTime startTime = request.startTime();
        LocalDateTime endTime = startTime.plusMinutes(request.sessionType().getDurationMinutes());
        LocalDateTime endOfBufferTime = endTime.plusMinutes(request.sessionType().getBufferAfterMinutes());

        Booking booking = Booking.builder()
                .user(user)
                .artist(artist)
                .sessionType(request.sessionType())
                .notes(request.notes())
                .imagePath(request.imagePath())
                .startTime(request.startTime())
                .endTime(endTime)
                .endOfBufferTime(endOfBufferTime)
                .build();

        // 2. Check for overlapping bookings taking into account buffer time (DB-level query)
        boolean conflict = bookingRepository.hasOverlap(
                booking.getArtist().getId(),
                booking.getStartTime(),
                booking.getEndOfBufferTime(),
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

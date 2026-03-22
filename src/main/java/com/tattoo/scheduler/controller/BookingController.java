package com.tattoo.scheduler.controller;

import com.tattoo.scheduler.controller.mapper.BookingDTOMapper;
import com.tattoo.scheduler.domain.Artist;
import com.tattoo.scheduler.domain.Booking;
import com.tattoo.scheduler.domain.User;
import com.tattoo.scheduler.dto.BookingResponse;
import com.tattoo.scheduler.dto.CreateBookingRequest;
import com.tattoo.scheduler.service.BookingService;
import com.tattoo.scheduler.service.resolver.ArtistResolver;
import com.tattoo.scheduler.service.resolver.UserResolver;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    private final BookingService bookingService;
    private final UserResolver userResolver;
    private final ArtistResolver artistResolver;
    private final BookingDTOMapper bookingDTOMapper;
    public BookingController(BookingService bookingService,
                             UserResolver userResolver,
                             ArtistResolver artistResolver,
                             BookingDTOMapper bookingDTOMapper) {
        this.bookingService = bookingService;
        this.userResolver = userResolver;
        this.artistResolver = artistResolver;
        this.bookingDTOMapper = bookingDTOMapper;
    }

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody @Valid CreateBookingRequest request,
            @RequestParam(required = false) Long artistId) {

        // 1. Map request to domain
        Booking booking = bookingDTOMapper.toDomain(request);
        booking.setUserId(userId);
        booking.setArtistId(artistId);

        // 2. Create booking (domain)
        Booking saved = bookingService.createBooking(booking);

        // 3. Map to response
        return ResponseEntity.status(201).body(bookingDTOMapper.toResponse(saved));
    }
}

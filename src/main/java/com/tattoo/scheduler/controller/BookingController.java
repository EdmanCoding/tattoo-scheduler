package com.tattoo.scheduler.controller;

import com.tattoo.scheduler.controller.mapper.BookingDTOMapper;
import com.tattoo.scheduler.domain.Booking;
import com.tattoo.scheduler.dto.BookingResponse;
import com.tattoo.scheduler.dto.CreateBookingRequest;
import com.tattoo.scheduler.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    private final BookingService bookingService;
    private final BookingDTOMapper bookingDTOMapper;
    public BookingController(BookingService bookingService,
                             BookingDTOMapper bookingDTOMapper) {
        this.bookingService = bookingService;
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
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingDTOMapper.toResponse(saved));
    }
}

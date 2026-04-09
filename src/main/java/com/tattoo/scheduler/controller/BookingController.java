package com.tattoo.scheduler.controller;

import com.tattoo.scheduler.controller.mapper.BookingDTOMapper;
import com.tattoo.scheduler.domain.Booking;
import com.tattoo.scheduler.dto.BookingResponse;
import com.tattoo.scheduler.dto.CreateBookingRequest;
import com.tattoo.scheduler.service.BookingService;
import com.tattoo.scheduler.service.dto.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;
    private final BookingDTOMapper bookingDTOMapper;

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestBody @Valid CreateBookingRequest request,
            @RequestParam(required = false) Long artistId) {

        // 1. Map request to domain
        Booking booking = bookingDTOMapper.toDomain(request, currentUser.getId(), artistId);

        // 2. Create booking (domain)
        Booking saved = bookingService.createBooking(booking);

        // 3. Map to response
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingDTOMapper.toResponse(saved));
    }
}

package com.tattoo.scheduler.service;

import com.tattoo.scheduler.model.Artist;
import com.tattoo.scheduler.model.Booking;
import com.tattoo.scheduler.model.SessionType;
import com.tattoo.scheduler.model.User;
import com.tattoo.scheduler.repository.ArtistRepository;
import com.tattoo.scheduler.repository.BookingRepository;
import com.tattoo.scheduler.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;  // 👈 New package!

import java.time.LocalDate;
import java.time.LocalDateTime;

@DataJpaTest
public class BookingServiceTest {
    @Autowired private BookingRepository bookingRepository;
    @Autowired private ArtistRepository artistRepository;
    @Autowired private UserRepository userRepository;
    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        // Manually create the service with the real repository
        bookingService = new BookingService(bookingRepository);
    }

    @Test
    @DisplayName("Should prevent overlapping bookings for the same artist")
    void shouldPreventOverlappingBookingsTest() {
        // Arrange: Create artist, user, and existing booking with time 14:00 - 18:00
        Artist artist = Artist.builder()
                .name("TestArtist")
                .email("testMail@email.com")
                .password("secret").build();
        artist = artistRepository.save(artist);  // ✅ now persisted
        User user = User.builder()
                .name("TestUser")
                .phoneNumber("123-4567")
                .email("testMailUser@email.com")
                .password("secret")
                .birthDate(LocalDate.of(2001,4,15)).build();
        user = userRepository.save(user);        // ✅ now persisted

        Booking existing = new Booking();
        existing.setUser(user);
        existing.setArtist(artist);
        existing.setSessionType(SessionType.MEDIUM);
        existing.setStartTime(LocalDateTime.of(2026,3,10,14,0));
        existing.setEndTime(LocalDateTime.of(2026,3,10,18,0));
        bookingRepository.save(existing);

        // Act and Assert: Try to book 16:00 - 20:00 (overlaps) -> expect exception
        Booking overlapping = new Booking();
        overlapping.setUser(user);
        overlapping.setArtist(artist);
        overlapping.setSessionType(SessionType.MEDIUM);
        overlapping.setStartTime(LocalDateTime.of(2026,3,10,16,0));
        overlapping.setEndTime(LocalDateTime.of(2026,3,10,20,0));

        Assertions.assertThatThrownBy(()-> bookingService.createBooking(overlapping))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already booked");
    }
}

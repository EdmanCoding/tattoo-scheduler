package com.tattoo.scheduler.service;

import com.tattoo.scheduler.model.Artist;
import com.tattoo.scheduler.model.Booking;
import com.tattoo.scheduler.model.SessionType;
import com.tattoo.scheduler.model.User;
import com.tattoo.scheduler.repository.ArtistRepository;
import com.tattoo.scheduler.repository.BookingRepository;
import com.tattoo.scheduler.repository.UserRepository;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;

@DataJpaTest
@ActiveProfiles("test")
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
        artist = artistRepository.save(artist);

        User user = User.builder()
                .name("TestUser")
                .phoneNumber("123-4567")
                .email("testMailUser@email.com")
                .password("secret")
                .birthDate(LocalDate.of(2001,4,15)).build();
        user = userRepository.save(user);

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

        assertThatThrownBy(()-> bookingService.createBooking(overlapping))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already booked");
    }
    @Test
    @DisplayName("Should work successfully")
    void notOverlappingBookingsTest() {
        // Arrange: Create artist, user, and existing booking 14:00 - 18:00
        Artist artist = Artist.builder()
                .name("TestArtist")
                .email("testMail@email.com")
                .password("secret").build();
        artist = artistRepository.save(artist);

        User user = User.builder()
                .name("TestUser")
                .phoneNumber("123-4567")
                .email("testMailUser@email.com")
                .password("secret")
                .birthDate(LocalDate.of(2001,4,15)).build();
        user = userRepository.save(user);

        Booking existing = new Booking();
        existing.setUser(user);
        existing.setArtist(artist);
        existing.setSessionType(SessionType.MEDIUM);
        existing.setStartTime(LocalDateTime.of(2026,3,10,14,0));
        existing.setEndTime(LocalDateTime.of(2026,3,10,18,0));
        bookingRepository.save(existing);

        // Act: Create non-overlapping booking 9:00-13:00
        Booking notOverlapping = new Booking();
        notOverlapping.setUser(user);
        notOverlapping.setArtist(artist);
        notOverlapping.setSessionType(SessionType.MEDIUM);
        notOverlapping.setStartTime(LocalDateTime.of(2026,3,10,9,0));
        notOverlapping.setEndTime(LocalDateTime.of(2026,3,10,13,0));

        Booking saved = bookingService.createBooking(notOverlapping);

        // Assert
        assertThat(saved.getId()).isNotNull();                     // ID generated
        assertThat(saved.getStartTime()).isEqualTo(LocalDateTime.of(2026,3,10,9,0));
        assertThat(saved.getEndTime()).isEqualTo(LocalDateTime.of(2026,3,10,13,0));
        assertThat(saved.getUser().getId()).isEqualTo(user.getId());  // references correct user
        assertThat(saved.getArtist().getId()).isEqualTo(artist.getId());

        assertThat(bookingRepository.count()).isEqualTo(2);
    }
}

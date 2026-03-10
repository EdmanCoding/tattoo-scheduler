package com.tattoo.scheduler.service;

import com.tattoo.scheduler.util.TestData;
import com.tattoo.scheduler.model.*;
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

import java.time.LocalDateTime;

@DataJpaTest
@ActiveProfiles("test")
public class BookingServiceIntegratedTest {
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
        Artist artist = TestData.createTestArtist();
        artist = artistRepository.save(artist);

        User user = TestData.createTestUser1();
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
    void nonOverlappingBookingsTest() {
        // Arrange: Create artist, user, and existing booking 14:00 - 18:00
        Artist artist = TestData.createTestArtist();
        artist = artistRepository.save(artist);

        User user = TestData.createTestUser1();
        user = userRepository.save(user);

        Booking existing = new Booking();
        existing.setUser(user);
        existing.setArtist(artist);
        existing.setSessionType(SessionType.MEDIUM);
        existing.setStartTime(LocalDateTime.of(2026,3,10,14,0));
        existing.setEndTime(LocalDateTime.of(2026,3,10,18,0));
        bookingRepository.save(existing);

        // Act: Create non-overlapping booking 9:00-13:00
        Booking nonOverlapping = new Booking();
        nonOverlapping.setUser(user);
        nonOverlapping.setArtist(artist);
        nonOverlapping.setSessionType(SessionType.MEDIUM);
        nonOverlapping.setStartTime(LocalDateTime.of(2026,3,10,9,0));
        nonOverlapping.setEndTime(LocalDateTime.of(2026,3,10,13,0));

        Booking saved = bookingService.createBooking(nonOverlapping);

        // Assert
        assertThat(saved.getId()).isNotNull();                     // ID generated
        assertThat(saved.getStartTime()).isEqualTo(LocalDateTime.of(2026,3,10,9,0));
        assertThat(saved.getEndTime()).isEqualTo(LocalDateTime.of(2026,3,10,13,0));
        assertThat(saved.getUser().getId()).isEqualTo(user.getId());  // references correct user
        assertThat(saved.getArtist().getId()).isEqualTo(artist.getId());

        assertThat(bookingRepository.count()).isEqualTo(2);
    }
    @Test
    @DisplayName("Should not overlap at an adjacent time")
    void adjacentBookingsTest() {
        // Arrange: Create artist, user, and existing booking 10:00 - 14:00
        Artist artist = TestData.createTestArtist();
        artist = artistRepository.save(artist);

        User user = TestData.createTestUser1();
        user = userRepository.save(user);

        Booking existing = new Booking();
        existing.setUser(user);
        existing.setArtist(artist);
        existing.setSessionType(SessionType.MEDIUM);
        existing.setStartTime(LocalDateTime.of(2026, 3, 10, 10, 0));
        existing.setEndTime(LocalDateTime.of(2026, 3, 10, 14, 0));
        bookingRepository.save(existing);

        // Act: Create adjacent booking 14:00-18:00
        Booking adjacent = new Booking();
        adjacent.setUser(user);
        adjacent.setArtist(artist);
        adjacent.setSessionType(SessionType.MEDIUM);
        adjacent.setStartTime(LocalDateTime.of(2026,3,10,14,0));
        adjacent.setEndTime(LocalDateTime.of(2026,3,10,18,0));

        Booking saved = bookingService.createBooking(adjacent);

        // Assert
        assertThat(saved.getId()).isNotNull();                     // ID generated
        assertThat(saved.getStartTime()).isEqualTo(LocalDateTime.of(2026,3,10,14,0));
        assertThat(saved.getEndTime()).isEqualTo(LocalDateTime.of(2026,3,10,18,0));
        assertThat(saved.getUser().getId()).isEqualTo(user.getId());  // references correct user
        assertThat(saved.getArtist().getId()).isEqualTo(artist.getId());

        assertThat(bookingRepository.count()).isEqualTo(2);
    }
    @Test
    @DisplayName("Should prevent bookings at the same time for the same artist")
    void sameTimeBookingsTest() {
        // Arrange: Create artist, user, and existing booking with time 14:00 - 18:00
        Artist artist = TestData.createTestArtist();
        artist = artistRepository.save(artist);

        User user = TestData.createTestUser1();
        user = userRepository.save(user);

        Booking existing = new Booking();
        existing.setUser(user);
        existing.setArtist(artist);
        existing.setSessionType(SessionType.MEDIUM);
        existing.setStartTime(LocalDateTime.of(2026,3,10,14,0));
        existing.setEndTime(LocalDateTime.of(2026,3,10,18,0));
        bookingRepository.save(existing);

        // Act and Assert: Try to book the same time
        Booking sameTime = new Booking();
        sameTime.setUser(user);
        sameTime.setArtist(artist);
        sameTime.setSessionType(SessionType.MEDIUM);
        sameTime.setStartTime(LocalDateTime.of(2026,3,10,14,0));
        sameTime.setEndTime(LocalDateTime.of(2026,3,10,18,0));

        assertThatThrownBy(()-> bookingService.createBooking(sameTime))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already booked");
    }
    @Test
    @DisplayName("Can be overlapped with cancelled session")
    void cancelledSessionOverlappingTest() {
        // Arrange: Create artist, user, and existing cancelled booking 10:00 - 14:00
        Artist artist = TestData.createTestArtist();
        artist = artistRepository.save(artist);

        User user = TestData.createTestUser1();
        user = userRepository.save(user);

        Booking existing = new Booking();
        existing.setUser(user);
        existing.setArtist(artist);
        existing.setSessionType(SessionType.MEDIUM);
        existing.setStatus(BookingStatus.CANCELLED);
        existing.setStartTime(LocalDateTime.of(2026, 3, 10, 10, 0));
        existing.setEndTime(LocalDateTime.of(2026, 3, 10, 14, 0));
        bookingRepository.save(existing);

        // Act: Create booking at the same time
        Booking notOverlapping = new Booking();
        notOverlapping.setUser(user);
        notOverlapping.setArtist(artist);
        notOverlapping.setSessionType(SessionType.MEDIUM);
        notOverlapping.setStartTime(LocalDateTime.of(2026,3,10,10,0));
        notOverlapping.setEndTime(LocalDateTime.of(2026,3,10,14,0));

        Booking saved = bookingService.createBooking(notOverlapping);

        // Assert
        assertThat(saved.getId()).isNotNull();                     // ID generated
        assertThat(saved.getStartTime()).isEqualTo(LocalDateTime.of(2026,3,10,10,0));
        assertThat(saved.getEndTime()).isEqualTo(LocalDateTime.of(2026,3,10,14,0));
        assertThat(saved.getUser().getId()).isEqualTo(user.getId());  // references correct user
        assertThat(saved.getArtist().getId()).isEqualTo(artist.getId());

        assertThat(bookingRepository.count()).isEqualTo(2);
    }
    @Test
    @DisplayName("Overlapping validation should work on bookings from different users")
    void differentUsersOverlappingBookingTest() {
        // Arrange: Create artist, two users, and existing booking 10:00 - 14:00
        Artist artist = TestData.createTestArtist();
        artist = artistRepository.save(artist);

        User user1 = TestData.createTestUser1();
        user1 = userRepository.save(user1);

        User user2 = TestData.createTestUser2();
        user2 = userRepository.save(user2);

        Booking existing = new Booking();
        existing.setUser(user1);
        existing.setArtist(artist);
        existing.setSessionType(SessionType.MEDIUM);
        existing.setStartTime(LocalDateTime.of(2026,3,10,14,0));
        existing.setEndTime(LocalDateTime.of(2026,3,10,18,0));
        bookingRepository.save(existing);

        // Act and Assert: Try to book 16:00 - 20:00 (overlaps) -> expect exception
        Booking overlapping = new Booking();
        overlapping.setUser(user2);
        overlapping.setArtist(artist);
        overlapping.setSessionType(SessionType.MEDIUM);
        overlapping.setStartTime(LocalDateTime.of(2026,3,10,16,0));
        overlapping.setEndTime(LocalDateTime.of(2026,3,10,20,0));

        assertThatThrownBy(()-> bookingService.createBooking(overlapping))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already booked");
    }
    @Test
    @DisplayName("Should auto-calculate endTime when not provided (integration)")
    void autoCalculateEndTimeIntegrationTest() {
        // Arrange
        Artist artist = TestData.createTestArtist();
        artist = artistRepository.save(artist);

        User user = TestData.createTestUser1();
        user = userRepository.save(user);

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setArtist(artist);
        booking.setSessionType(SessionType.MEDIUM); // 4 hours
        booking.setStartTime(LocalDateTime.of(2026, 3, 10, 14, 0));
        // endTime is NOT set

        // Act
        Booking saved = bookingService.createBooking(booking);

        // Assert
        assertThat(saved.getEndTime())
                .isEqualTo(LocalDateTime.of(2026, 3, 10, 18, 0)); // 14:00 + 4h
    }
}

package com.tattoo.scheduler.service;

import com.tattoo.scheduler.dto.BookingResponse;
import com.tattoo.scheduler.dto.CreateBookingRequest;
import com.tattoo.scheduler.service.exception.BookingConflictException;
import com.tattoo.scheduler.util.TestData;
import com.tattoo.scheduler.model.*;
import com.tattoo.scheduler.repository.ArtistRepository;
import com.tattoo.scheduler.repository.BookingRepository;
import com.tattoo.scheduler.repository.UserRepository;

import static com.tattoo.scheduler.util.TestData.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@ActiveProfiles("test")
@Sql(scripts = "/test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
public class BookingServiceIntegratedTest {
    @Autowired private BookingRepository bookingRepository;
    @Autowired private ArtistRepository artistRepository;
    @Autowired private UserRepository userRepository;
    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        // Manually create the service with the real repository
        bookingService = new BookingService(bookingRepository,userRepository,artistRepository);
    }

    @Test
    @DisplayName("Should prevent overlapping bookings for the same artist")
    void overlappingBookingsTest() {
        // Arrange
        UserEntity userEntity = userRepository.save(TestData.createTestUser1());
        ArtistEntity artistEntity = artistRepository.getReferenceById(1L);

        // Create and save existing booking
        BookingEntity existing = BookingEntity.builder()
                .artistEntity(artistEntity)
                .userEntity(userEntity)
                .sessionType(SessionType.MEDIUM)
                .startTime(DEFAULT_START_TIME)                  //10:00
                .endTime(DEFAULT_END_TIME)                      //14:00
                .endOfBufferTime(DEFAULT_END_OF_BUFFER_TIME)    //16:00
                .build();
        bookingRepository.save(existing);

        // Create overlapping request (11:00-15:00)
        CreateBookingRequest request = new CreateBookingRequest(SessionType.MEDIUM,
                DEFAULT_START_TIME.plusHours(1), "Overlapping attempt", null);

        // Act and Assert
        assertThatThrownBy(() -> bookingService.createBooking(userEntity.getId(), request))
                .isInstanceOf(BookingConflictException.class)
                .hasMessageContaining("Cannot book at ");
    }
    @DisplayName("Should prevent bookings at the same time for the same artist")
    void sameTimeBookingsTest() {
        // Arrange
        UserEntity userEntity = userRepository.save(TestData.createTestUser1());
        ArtistEntity artistEntity = artistRepository.getReferenceById(1L);

        BookingEntity existing = BookingEntity.builder()
                .artistEntity(artistEntity)
                .userEntity(userEntity)
                .sessionType(SessionType.MEDIUM)
                .startTime(DEFAULT_START_TIME)                  //10:00
                .endTime(DEFAULT_END_TIME)                      //14:00
                .endOfBufferTime(DEFAULT_END_OF_BUFFER_TIME)    //16:00
                .build();
        bookingRepository.save(existing);

        // Create overlapping request at the same time (10:00-14:00)
        CreateBookingRequest request = new CreateBookingRequest(SessionType.MEDIUM,
                DEFAULT_START_TIME, "Overlapping attempt", null);

        // Act and Assert
        assertThatThrownBy(() -> bookingService.createBooking(userEntity.getId(), request))
                .isInstanceOf(BookingConflictException.class)
                .hasMessageContaining("Cannot book at ");
    }
    @Test
    @DisplayName("Should prevent overlapping with buffer of previous booking for the same artist")
    void overlappingWithBufferTest() {
        // Arrange
        UserEntity userEntity = userRepository.save(TestData.createTestUser1());
        ArtistEntity artistEntity = artistRepository.getReferenceById(1L);

        BookingEntity existing = BookingEntity.builder()
                .artistEntity(artistEntity)
                .userEntity(userEntity)
                .sessionType(SessionType.MEDIUM)    // 2 hours buffer
                .startTime(DEFAULT_START_TIME)                  //10:00
                .endTime(DEFAULT_END_TIME)                      //14:00
                .endOfBufferTime(DEFAULT_END_OF_BUFFER_TIME)    //16:00
                .build();
        bookingRepository.save(existing);

        // Create overlapping with buffer (start at 15:00) request
        CreateBookingRequest request = new CreateBookingRequest(SessionType.MEDIUM,
                DEFAULT_START_TIME.plusHours(5), "Overlapping with buffer 14:00-16:00", null);

        // Act and Assert
        assertThatThrownBy(() -> bookingService.createBooking(userEntity.getId(), request))
                .isInstanceOf(BookingConflictException.class)
                .hasMessageContaining("Cannot book at ");
    }
    @Test
    @DisplayName("Should prevent overlapping with buffer of previous booking " +
            "for the same artist with different session types")
    void overlappingWithBufferDifferentSessionsTest() {
        // Arrange
        UserEntity userEntity = userRepository.save(TestData.createTestUser1());
        ArtistEntity artistEntity = artistRepository.getReferenceById(1L);

        BookingEntity existing = BookingEntity.builder()
                .artistEntity(artistEntity)
                .userEntity(userEntity)
                .sessionType(SessionType.SMALL)             // 1 hour buffer
                .startTime(DEFAULT_START_TIME)              // 10:00
                .endTime(DEFAULT_START_TIME.plusHours(1))   // 11:00
                .endOfBufferTime(DEFAULT_START_TIME.plusHours(2))  // 12:00
                .build();
        bookingRepository.save(existing);

        // Create overlapping with buffer (start at 11:30) request
        CreateBookingRequest request = new CreateBookingRequest(SessionType.MEDIUM,
                DEFAULT_START_TIME.plusHours(1).plusMinutes(30),
                "Overlapping with buffer 11:00-12:00", null);

        // Act and Assert
        assertThatThrownBy(() -> bookingService.createBooking(userEntity.getId(), request))
                .isInstanceOf(BookingConflictException.class)
                .hasMessageContaining("Cannot book at ");
    }
    @Test
    @DisplayName("Overlapping validation should work on bookings from different users")
    void differentUsersOverlappingBookingTest() {
        // Arrange
        UserEntity userEntity1 = userRepository.save(TestData.createTestUser1());
        UserEntity userEntity2 = userRepository.save(TestData.createTestUser2());
        ArtistEntity artistEntity = artistRepository.getReferenceById(1L);

        BookingEntity existing = BookingEntity.builder()
                .artistEntity(artistEntity)
                .userEntity(userEntity1)
                .sessionType(SessionType.MEDIUM)        // 2 hours buffer
                .startTime(DEFAULT_START_TIME)                  // 10:00
                .endTime(DEFAULT_END_TIME)                      // 14:00
                .endOfBufferTime(DEFAULT_END_OF_BUFFER_TIME)    // 16:00
                .build();
        bookingRepository.save(existing);

        // Create overlapping (11:00-15:00) request
        CreateBookingRequest request = new CreateBookingRequest(SessionType.MEDIUM,
                DEFAULT_START_TIME.plusHours(1),
                "Overlapping booking 11:00-15:00", null);

        // Act and Assert
        assertThatThrownBy(() -> bookingService.createBooking(userEntity2.getId(), request))
                .isInstanceOf(BookingConflictException.class)
                .hasMessageContaining("Cannot book at ");
    }
    @Test
    @DisplayName("Should allow booking exactly when buffer period ends")
    void adjacentAfterBufferTest() {
        // Arrange
        UserEntity userEntity = userRepository.save(TestData.createTestUser1());
        ArtistEntity artistEntity = artistRepository.getReferenceById(1L);

        BookingEntity existing = BookingEntity.builder()
                .artistEntity(artistEntity)
                .userEntity(userEntity)
                .sessionType(SessionType.MEDIUM)        // 2 hours buffer
                .startTime(DEFAULT_START_TIME)                  // 10:00
                .endTime(DEFAULT_END_TIME)                      // 14:00
                .endOfBufferTime(DEFAULT_END_OF_BUFFER_TIME)    // 16:00
                .build();
        bookingRepository.save(existing);

        // Try to book exactly when buffer ends (16:00)
        CreateBookingRequest request = new CreateBookingRequest(SessionType.MEDIUM,
                DEFAULT_START_TIME.plusHours(6), "Adjacent booking", null);
        // Act
        BookingResponse response = bookingService.createBooking(userEntity.getId(), request);
        // Assert
        assertThat(response).isNotNull();
        assertThat(response.id()).isNotNull();                     // ID generated
        assertThat(response.startTime()).isEqualTo(DEFAULT_START_TIME.plusHours(6));
        assertThat(response.endTime()).isEqualTo(DEFAULT_START_TIME.plusHours(10));
        assertThat(response.endOfBufferTime()).isEqualTo(DEFAULT_START_TIME.plusHours(12));
        assertThat(response.userId()).isEqualTo(userEntity.getId());  // references correct user
        assertThat(response.artistId()).isEqualTo(artistEntity.getId());
        assertThat(response.sessionType()).isEqualTo(SessionType.MEDIUM);
        assertThat(response.status()).isEqualTo(BookingStatus.PENDING);
        assertThat(response.notes()).isEqualTo("Adjacent booking");
        assertThat(response.imagePath()).isNull();
        assertThat(response.createdAt()).isNotNull();

        assertThat(bookingRepository.count()).isEqualTo(2);
    }
    @Test
    @DisplayName("Should create booking when no conflicts exist")
    void happyPathTest() {
        // Arrange
        UserEntity userEntity = userRepository.save(TestData.createTestUser1());
        // No existing bookings
        CreateBookingRequest request = new CreateBookingRequest(SessionType.MEDIUM,
                DEFAULT_START_TIME, "First booking", null);
        // Act
        BookingResponse response = bookingService.createBooking(userEntity.getId(), request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.id()).isNotNull();                     // ID generated
        assertThat(response.startTime()).isEqualTo(DEFAULT_START_TIME);
        assertThat(response.endTime()).isEqualTo(DEFAULT_END_TIME);
        assertThat(response.endOfBufferTime()).isEqualTo(DEFAULT_END_OF_BUFFER_TIME);
        assertThat(response.userId()).isEqualTo(userEntity.getId());  // references correct user
        assertThat(response.artistId()).isEqualTo(artistRepository.getReferenceById(1L).getId());
        assertThat(response.sessionType()).isEqualTo(SessionType.MEDIUM);
        assertThat(response.status()).isEqualTo(BookingStatus.PENDING);
        assertThat(response.notes()).isEqualTo("First booking");
        assertThat(response.imagePath()).isNull();
        assertThat(response.createdAt()).isNotNull();

        assertThat(bookingRepository.count()).isEqualTo(1);
    }
    @Test
    @DisplayName("Can be overlapped with cancelled session")
    void cancelledSessionOverlappingTest() {
        // Arrange
        UserEntity userEntity = userRepository.save(TestData.createTestUser1());
        ArtistEntity artistEntity = artistRepository.getReferenceById(1L);

        BookingEntity existing = BookingEntity.builder()
                .artistEntity(artistEntity)
                .userEntity(userEntity)
                .status(BookingStatus.CANCELLED)
                .sessionType(SessionType.MEDIUM)    // 2 hours buffer
                .startTime(DEFAULT_START_TIME)                  // 10:00
                .endTime(DEFAULT_END_TIME)                      // 14:00
                .endOfBufferTime(DEFAULT_END_OF_BUFFER_TIME)    // 16:00
                .build();
        bookingRepository.save(existing);

        // Try to book exactly the same time (10:00-14:00)
        CreateBookingRequest request = new CreateBookingRequest(SessionType.MEDIUM,
                DEFAULT_START_TIME, "Same time booking", null);
        // Act
        BookingResponse response = bookingService.createBooking(userEntity.getId(), request);
        // Assert
        assertThat(response).isNotNull();
        assertThat(response.id()).isNotNull();                     // ID generated
        assertThat(response.startTime()).isEqualTo(DEFAULT_START_TIME);
        assertThat(response.endTime()).isEqualTo(DEFAULT_END_TIME);
        assertThat(response.endOfBufferTime()).isEqualTo(DEFAULT_END_OF_BUFFER_TIME);
        assertThat(response.userId()).isEqualTo(userEntity.getId());  // references correct user
        assertThat(response.artistId()).isEqualTo(artistEntity.getId());
        assertThat(response.sessionType()).isEqualTo(SessionType.MEDIUM);
        assertThat(response.status()).isEqualTo(BookingStatus.PENDING);
        assertThat(response.notes()).isEqualTo("Same time booking");
        assertThat(response.imagePath()).isNull();
        assertThat(response.createdAt()).isNotNull();

        assertThat(bookingRepository.count()).isEqualTo(2);
    }
}

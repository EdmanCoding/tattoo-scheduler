package com.tattoo.scheduler.service;

import com.tattoo.scheduler.domain.Booking;
import com.tattoo.scheduler.model.ArtistEntity;
import com.tattoo.scheduler.model.BookingEntity;
import com.tattoo.scheduler.model.BookingStatus;
import com.tattoo.scheduler.model.UserEntity;
import com.tattoo.scheduler.repository.ArtistRepository;
import com.tattoo.scheduler.repository.BookingRepository;
import com.tattoo.scheduler.repository.UserRepository;
import com.tattoo.scheduler.service.exception.ArtistNotFoundException;
import com.tattoo.scheduler.service.exception.BookingConflictException;
import com.tattoo.scheduler.service.exception.BookingOutsideWorkingHoursException;
import com.tattoo.scheduler.service.exception.UserNotFoundException;
import com.tattoo.scheduler.util.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static com.tattoo.scheduler.model.SessionType.*;
import static com.tattoo.scheduler.util.TestData.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test-postgres")
@Transactional
@Sql(scripts = "/test-data-postgre.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class BookingServiceIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
    @Autowired
    private BookingService bookingService;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private ArtistRepository artistRepository;
    @Autowired
    private UserRepository userRepository;

    private ArtistEntity artist;
    private UserEntity user;

    @BeforeEach
    void setUp() {
        // Use the artist already inserted by test-data-h2.sql
        artist = artistRepository.findById(TEST_ARTIST_ID)
                .orElseThrow(() -> new ArtistNotFoundException(TEST_ARTIST_ID));
        // Use the user already inserted by test-data-h2.sql
        user = userRepository.findById(TEST_USER_ID)
                .orElseThrow(() -> new UserNotFoundException(TEST_USER_ID));
    }

    @Test
    void shouldReturnDomain_whenDayEmpty(){
        // Arrange
        Booking request = Booking.builder()
                .sessionType(MEDIUM)
                .startTime(DEFAULT_START_TIME)
                .userId(TEST_USER_ID)
                .artistId(TEST_ARTIST_ID).build();
        // Act
        Booking result = bookingService.createBooking(request);
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();
        assertThat(result.getArtistId()).isEqualTo(TEST_ARTIST_ID);
        assertThat(result.getUserId()).isEqualTo(TEST_USER_ID);
        assertThat(result.getSessionType()).isEqualTo(MEDIUM);
        assertThat(result.getStartTime()).isEqualTo(DEFAULT_START_TIME);
        assertThat(result.getEndTime()).isEqualTo(
                DEFAULT_START_TIME.plusMinutes(MEDIUM.getDurationMinutes()));
        assertThat(result.getEndOfBufferTime()).isEqualTo(
                DEFAULT_START_TIME.plusMinutes(MEDIUM.getDurationMinutes())
                        .plusMinutes(MEDIUM.getBufferAfterMinutes()));
        assertThat(result.getStatus()).isEqualTo(BookingStatus.PENDING);
    }
    @Test
    void shouldThrowException_whenConflictWithExisting(){
        // Arrange
        // existing booking 10:00-14:00, buffer until 16:00
        BookingEntity mediumBooking = TestData.createTestBookingEntity(
                user, artist, MEDIUM, DEFAULT_START_TIME);
        bookingRepository.save(mediumBooking);

        Booking request = Booking.builder()
                .sessionType(MEDIUM)
                // conflict with buffer of existing
                .startTime(DEFAULT_START_TIME.plusHours(5))
                .userId(TEST_USER_ID)
                .artistId(TEST_ARTIST_ID).build();
        // Act and Assert
        assertThatThrownBy(()-> bookingService.createBooking(request))
                .isInstanceOf(BookingConflictException.class)
                .hasMessageContaining("Cannot book at");
    }
    @Test
    void shouldThrowException_whenArtistNotFound(){
        // Arrange
        Booking request = Booking.builder()
                .sessionType(MEDIUM)
                .startTime(DEFAULT_START_TIME)
                .userId(TEST_USER_ID)
                .artistId(TEST_NONEXISTING_ARTIST_ID).build();
        // Act and Assert
        assertThatThrownBy(()-> bookingService.createBooking(request))
                .isInstanceOf(ArtistNotFoundException.class)
                .hasMessage("Artist with id " + TEST_NONEXISTING_ARTIST_ID + " not found");
    }
    @Test
    void shouldThrowException_whenBookLargeWhileDayHasBooking(){
        // Arrange
        // existing booking 19:30-20:00, buffer until 20:30
        BookingEntity mediumBooking = TestData.createTestBookingEntity(
                user, artist, SMALL_CONSULTATION, DEFAULT_START_TIME.plusHours(9).plusMinutes(30));
        bookingRepository.save(mediumBooking);

        Booking request = Booking.builder()
                .sessionType(LARGE)
                .startTime(DEFAULT_START_TIME)
                .userId(TEST_USER_ID)
                .artistId(TEST_ARTIST_ID).build();

        // Act and Assert
        assertThatThrownBy(()-> bookingService.createBooking(request))
                .isInstanceOf(BookingConflictException.class)
                .hasMessage("Cannot book at " + DEFAULT_START_TIME + " for artist "
                + TEST_ARTIST_ID + ": LARGE session rules violated");
    }
    @Test
    void shouldReturnDomain_whenBookEmptyWhileDayFree(){
        // Arrange
        Booking request = Booking.builder()
                .sessionType(LARGE)
                .startTime(DEFAULT_START_TIME)
                .userId(TEST_USER_ID)
                .artistId(TEST_ARTIST_ID).build();
        // Act
        Booking result = bookingService.createBooking(request);
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();
        assertThat(result.getArtistId()).isEqualTo(TEST_ARTIST_ID);
        assertThat(result.getUserId()).isEqualTo(TEST_USER_ID);
        assertThat(result.getSessionType()).isEqualTo(LARGE);
        assertThat(result.getStartTime()).isEqualTo(DEFAULT_START_TIME);
        assertThat(result.getEndTime()).isEqualTo(
                DEFAULT_START_TIME.plusMinutes(LARGE.getDurationMinutes()));
        assertThat(result.getEndOfBufferTime()).isEqualTo(
                DEFAULT_START_TIME.plusMinutes(LARGE.getDurationMinutes())
                        .plusMinutes(LARGE.getBufferAfterMinutes()));
        assertThat(result.getStatus()).isEqualTo(BookingStatus.PENDING);
    }
    @Test
    void shouldThrowException_whenBookingOutsideWorkingHours(){
        // Arrange
        Booking request = Booking.builder()
                .sessionType(MEDIUM)
                // Session ends at 21:00
                .startTime(DEFAULT_START_TIME.plusHours(7))
                .userId(TEST_USER_ID)
                .artistId(TEST_ARTIST_ID).build();
        // Act and Assert
        assertThatThrownBy(()-> bookingService.createBooking(request))
                .isInstanceOf(BookingOutsideWorkingHoursException.class)
                .hasMessage("Session starting at "
                        + DEFAULT_START_TIME.plusHours(7).toLocalTime()
                        + " must be within 10:00–20:00 and finish by 20:00");
    }
}

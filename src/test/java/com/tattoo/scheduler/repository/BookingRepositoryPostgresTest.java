package com.tattoo.scheduler.repository;

import com.tattoo.scheduler.model.*;
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

import java.time.LocalDateTime;
import java.util.List;

import static com.tattoo.scheduler.util.TestData.*;
import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test-postgre")
@Transactional
@Sql(scripts = "/test-data-postgre.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class BookingRepositoryPostgresTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);
    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ArtistRepository artistRepository;

    private ArtistEntity artist;
    private UserEntity user;
    // working hours -> 10:00 - 20:00; default date -> 15.04.2026
    private final LocalDateTime dayStart = DEFAULT_START_TIME;
    private final LocalDateTime dayEnd = DEFAULT_DAY_END_TIME;

    @BeforeEach
    void setUp() {
        // Use the artist already inserted by test-data-postgre.sql
        artist = artistRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Test artist not found"));
        // Insert user before each test
        user = userRepository.save(TestData.createTestUserEntity1());
    }

    @Test
    void shouldReturnBooking_whenOverlapWithDayStart() {
        BookingEntity booking = TestData.createTestBookingEntity(user,
                artist, SessionType.MEDIUM, DEFAULT_START_TIME);
        bookingRepository.save(booking);

        List<BookingEntity> result = bookingRepository.findOccupiedIntervals(
                artist.getId(), dayStart, dayEnd, BookingStatus.CANCELLED);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(booking.getId());
    }
    @Test
    void shouldReturnBooking_whenOverlapWithDayEnd() {
        BookingEntity booking = TestData.createTestBookingEntity(user,
                artist, SessionType.MEDIUM, DEFAULT_START_TIME.plusHours(7));
        bookingRepository.save(booking);

        List<BookingEntity> result = bookingRepository.findOccupiedIntervals(
                artist.getId(), dayStart, dayEnd, BookingStatus.CANCELLED);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(booking.getId());
    }
    @Test
    void shouldReturnBookings_whenExactlyAtBoundaries(){
        BookingEntity bookingAtStart = TestData.createTestBookingEntity(user,
                artist, SessionType.SMALL, DEFAULT_START_TIME);
        bookingRepository.save(bookingAtStart);
        BookingEntity bookingAtEnd = TestData.createTestBookingEntity(user,
                artist, SessionType.SMALL, DEFAULT_START_TIME.plusHours(8));
        bookingRepository.save(bookingAtEnd);

        List<BookingEntity> result = bookingRepository.findOccupiedIntervals(
                artist.getId(), dayStart, dayEnd, BookingStatus.CANCELLED);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(bookingAtStart.getId());
        assertThat(result.get(1).getId()).isEqualTo(bookingAtEnd.getId());
    }
    @Test
    void shouldExcludeBooking_whenCancelled(){
        BookingEntity cancelledBooking = TestData.createTestBookingEntity(user,
                artist, SessionType.MEDIUM, DEFAULT_START_TIME.plusHours(2));
        cancelledBooking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(cancelledBooking);

        List<BookingEntity> result = bookingRepository.findOccupiedIntervals(
                artist.getId(), dayStart, dayEnd, BookingStatus.CANCELLED);

        assertThat(result).isEmpty();
    }
    @Test
    void shouldReturnOnlyArtistBookings_whenMultipleArtistsExist(){
        ArtistEntity secondArtist = artistRepository.findById(2L)
                .orElseThrow(() -> new RuntimeException("Test artist not found"));
        BookingEntity bookingOfFirstArtist = TestData.createTestBookingEntity(user,
                artist, SessionType.MEDIUM, DEFAULT_START_TIME.plusHours(2));
        bookingRepository.save(bookingOfFirstArtist);
        BookingEntity bookingOfSecondArtist = TestData.createTestBookingEntity(user,
                secondArtist, SessionType.LARGE, DEFAULT_START_TIME);
        bookingRepository.save(bookingOfSecondArtist);

        List<BookingEntity> result = bookingRepository.findOccupiedIntervals(
                artist.getId(), dayStart, dayEnd, BookingStatus.CANCELLED);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(bookingOfFirstArtist.getId());
        assertThat(result.get(0).getSessionType()).isEqualTo(bookingOfFirstArtist.getSessionType());
        assertThat(result.get(0).getStartTime()).isEqualTo(bookingOfFirstArtist.getStartTime());
        assertThat(result.get(0).getArtistEntity()).isEqualTo(bookingOfFirstArtist.getArtistEntity());
    }
    @Test
    void shouldReturnEmpty_whenNoOverlap(){
        BookingEntity bookingBefore = TestData.createTestBookingEntity(user,
                artist, SessionType.SMALL_CONSULTATION, DEFAULT_START_TIME.minusHours(1));
        bookingRepository.save(bookingBefore);
        BookingEntity bookingAfter = TestData.createTestBookingEntity(user,
                artist, SessionType.SMALL, DEFAULT_START_TIME.plusHours(10));
        bookingRepository.save(bookingAfter);

        List<BookingEntity> result = bookingRepository.findOccupiedIntervals(
                artist.getId(), dayStart, dayEnd, BookingStatus.CANCELLED);

        assertThat(result).isEmpty();
    }
}

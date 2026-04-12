package com.tattoo.scheduler.service;

import com.tattoo.scheduler.model.ArtistEntity;
import com.tattoo.scheduler.model.BookingEntity;
import com.tattoo.scheduler.model.UserEntity;
import com.tattoo.scheduler.repository.ArtistRepository;
import com.tattoo.scheduler.repository.BookingRepository;
import com.tattoo.scheduler.repository.UserRepository;
import com.tattoo.scheduler.service.exception.ArtistNotFoundException;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.tattoo.scheduler.model.SessionType.*;
import static com.tattoo.scheduler.util.TestData.*;
import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test-postgre")
@Transactional
@Sql(scripts = "/test-data-postgre.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class AvailabilityServiceIntegrationTest {
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
    private AvailabilityService availabilityService;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private ArtistRepository artistRepository;
    @Autowired
    private UserRepository userRepository;

    private ArtistEntity artist;
    private UserEntity user;

    // working hours -> 10:00 - 20:00; default date -> 15.04.2026
    private final LocalDateTime dayStart = DEFAULT_START_TIME;
    private final LocalDate date = DEFAULT_DATE;

    @BeforeEach
    void setUp() {
        // Use the artist already inserted by test-data-postgre.sql
        artist = artistRepository.findById(TEST_ARTIST_ID)
                .orElseThrow(() -> new ArtistNotFoundException(TEST_ARTIST_ID));
        // Insert user before each test
        user = userRepository.save(TestData.createTestUserEntity1());
    }

    @Test
    void shouldReturnAvailableSlots_WhenMediumBookingExists(){
        // 10:00-14:00, buffer until 16:00
        BookingEntity mediumBooking = TestData.createTestBookingEntity(
                user, artist, MEDIUM, dayStart);
        bookingRepository.save(mediumBooking);

        List<LocalDateTime> expectedSlots = List.of(
                dayStart.plusHours(6),  // 16:00
                dayStart.plusHours(7),  // 17:00
                dayStart.plusHours(8),  // 18:00
                dayStart.plusHours(9)); // 19:00

        List<LocalDateTime> slots = availabilityService.getAvailableStartTimes(
                date, SMALL_CONSULTATION, TEST_ARTIST_ID);
        assertThat(slots).containsExactlyElementsOf(expectedSlots);
    }
    @Test
    void shouldReturnAllSlots_WhenDayEmpty() {
        List<LocalDateTime> expectedSlots = List.of(
                dayStart,               // 10:00
                dayStart.plusHours(2),  // 12:00
                dayStart.plusHours(4),  // 14:00
                dayStart.plusHours(6),  // 16:00
                dayStart.plusHours(8)); // 18:00

        List<LocalDateTime> slots = availabilityService.getAvailableStartTimes(
                date, SMALL, TEST_ARTIST_ID);

        assertThat(slots).containsExactlyElementsOf(expectedSlots);
    }
    @Test
    void shouldReturnNoSlots_WhenLargeBookingExists(){

        BookingEntity existingBooking = TestData.createTestBookingEntity(
                user, artist, LARGE, dayStart);
        bookingRepository.save(existingBooking);
        List<LocalDateTime> expectedSlots = List.of();

        List<LocalDateTime> slots = availabilityService.getAvailableStartTimes(
                date, SMALL_CONSULTATION, TEST_ARTIST_ID);

        assertThat(slots).containsExactlyElementsOf(expectedSlots);
    }
    @Test
    void shouldReturnAvailableSlots_WhenMultipleBookingsExist(){
        // multiple bookings
        // SMALL 10-11, LARGE_CONSULTATION 14-15, SMALL_CONSULTATION 19:30-20:00
        BookingEntity existingBooking = TestData.createTestBookingEntity(
                user, artist, SMALL, dayStart);
        BookingEntity existingBooking2 = TestData.createTestBookingEntity(
                user, artist, LARGE_CONSULTATION, dayStart.plusHours(4));
        BookingEntity existingBooking3 = TestData.createTestBookingEntity(
                user, artist, SMALL_CONSULTATION, dayStart.plusHours(9).plusMinutes(30));
        bookingRepository.save(existingBooking);
        bookingRepository.save(existingBooking2);
        bookingRepository.save(existingBooking3);
        List<LocalDateTime> expectedSlots = List.of(
                dayStart.plusHours(2),  // 12:00
                dayStart.plusHours(3),  // 13:00
                dayStart.plusHours(6),  // 16:00
                dayStart.plusHours(7),  // 17:00
                dayStart.plusHours(8)); // 18:00

        List<LocalDateTime> slots = availabilityService.getAvailableStartTimes(
                date, SMALL_CONSULTATION, TEST_ARTIST_ID);

        assertThat(slots).containsExactlyElementsOf(expectedSlots);
    }
}

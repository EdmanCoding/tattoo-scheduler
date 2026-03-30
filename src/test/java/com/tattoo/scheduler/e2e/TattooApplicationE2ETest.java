package com.tattoo.scheduler.e2e;

import com.tattoo.scheduler.dto.BookingResponse;
import com.tattoo.scheduler.dto.CreateBookingRequest;
import com.tattoo.scheduler.model.SessionType;
import com.tattoo.scheduler.repository.BookingRepository;
import com.tattoo.scheduler.util.TestRequestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.time.LocalDateTime;
import java.util.List;

import static com.tattoo.scheduler.util.TestData.*;
import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test-postgres")
@Sql(scripts = "/test-data-with-user.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class TattooApplicationE2ETest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private BookingRepository bookingRepository;

    @LocalServerPort
    private int port;

    private RestTestClient client;

    @BeforeEach
    void setUp() {
        // Simple, modern, and uses your application's Jackson 3 configuration
        client = RestTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }
    @BeforeEach
    void clearDatabase() {
        // Delete all bookings (the only mutable data in our E2E tests)
        bookingRepository.deleteAll();
        // If you later create other entities (e.g., users) in a test, delete them here too.
    }

    @Test
    void endToEnd_slotShouldDisappear_WhenBookingCreated() {
        // 1. Check available slots for a given day and session type
        List<LocalDateTime> slots = client.get()
                .uri("/api/availability?date={date}&sessionType={type}",
                        DEFAULT_DATE, SessionType.MEDIUM)
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<List<LocalDateTime>>() {})
                .returnResult()
                .getResponseBody();
        assertThat(slots).containsExactly(DEFAULT_START_TIME, DEFAULT_START_TIME.plusHours(6));

        // 2. Create a booking for the 10:00 slot
        CreateBookingRequest bookingRequest = TestRequestFactory.request()
                .ofType(SessionType.MEDIUM)
                .at(DEFAULT_START_TIME)
                .withNotes("Test booking")
                .build();

        BookingResponse booking = client.post()
                .uri("/api/bookings")
                .header("X-User-Id", TEST_USER_ID.toString())
                .body(bookingRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(BookingResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(booking).isNotNull();

        // 3. Check availability again
        List<LocalDateTime> newSlots = client.get()
                .uri("/api/availability?date={date}&sessionType={type}",
                        DEFAULT_DATE, SessionType.MEDIUM)
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<List<LocalDateTime>>() {})
                .returnResult()
                .getResponseBody();

        assertThat(newSlots).containsExactly(DEFAULT_START_TIME.plusHours(6));

        // 4. Try to book the same slot again
        client.post()
                .uri("/api/bookings")
                .header("X-User-Id", TEST_USER_ID.toString())
                .body(bookingRequest)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);
    }
}

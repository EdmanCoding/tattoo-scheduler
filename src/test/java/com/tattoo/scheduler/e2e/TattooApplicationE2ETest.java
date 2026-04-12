package com.tattoo.scheduler.e2e;

import com.tattoo.scheduler.dto.BookingResponse;
import com.tattoo.scheduler.dto.CreateBookingRequest;
import com.tattoo.scheduler.dto.auth.RegisterRequest;
import com.tattoo.scheduler.dto.auth.RegisterResponse;
import com.tattoo.scheduler.model.BookingStatus;
import com.tattoo.scheduler.model.SessionType;
import com.tattoo.scheduler.repository.BookingRepository;
import com.tattoo.scheduler.util.TestJwtGenerator;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.tattoo.scheduler.util.TestData.*;
import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test-postgre")
@Sql(scripts = "/test-data-postgre.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class TattooApplicationE2ETest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private TestJwtGenerator jwtGenerator;
    private String validToken;

    @LocalServerPort
    private int port;

    private RestTestClient client;

    @BeforeEach
    void setUp() {
        client = RestTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
        validToken = jwtGenerator.generateValidToken("testuser@example.com");
    }

    @BeforeEach
    void clearDatabase() {
        bookingRepository.deleteAll();
    }

    @Test
    void endToEnd_slotShouldDisappear_WhenBookingCreated() {
        // Verifies that after creating a booking, the slot is no longer available
        // and cannot be booked again.

        // 1. Check available slots for a given day and session type
        List<LocalDateTime> slots = client.get()
                .uri("/api/availability?date={date}&sessionType={type}",
                        DEFAULT_DATE, SessionType.MEDIUM)
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<List<LocalDateTime>>() {
                })
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
                .header("Authorization", "Bearer " + validToken)
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
                .expectBody(new ParameterizedTypeReference<List<LocalDateTime>>() {
                })
                .returnResult()
                .getResponseBody();

        assertThat(newSlots).containsExactly(DEFAULT_START_TIME.plusHours(6));

        // 4. Try to book the same slot again
        client.post()
                .uri("/api/bookings")
                .header("Authorization", "Bearer " + validToken)
                .body(bookingRequest)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void endToEnd_duplicateEmailPreventsSecondRegistration() {
        // Ensures email uniqueness: first registration works, duplicate fails with 409.

        // 1. Register first user
        RegisterRequest request1 = new RegisterRequest(
                "Vasya", "vasya_huligan228@gmail.com", "qwerty123",
                "+88005553535", LocalDate.of(2002, 2, 22)
        );

        RegisterResponse response1 = client.post()
                .uri("/api/auth/register")
                .body(request1)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(RegisterResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(response1).isNotNull();
        assertThat(response1.token()).isNotEmpty();
        assertThat(response1.email()).isEqualTo("vasya_huligan228@gmail.com");
        assertThat(response1.name()).isEqualTo("Vasya");
        assertThat(response1.id()).isPositive();

        String token = response1.token();

        // 2. Use token for booking (proves token is valid)
        CreateBookingRequest bookingRequest = new CreateBookingRequest(
                SessionType.SMALL,
                DEFAULT_START_TIME,
                null,
                null
        );

        BookingResponse bookingResponse = client.post()
                .uri("/api/bookings")
                .header("Authorization", "Bearer " + token)
                .body(bookingRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(BookingResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(bookingResponse).isNotNull();
        assertThat(bookingResponse.userId()).isEqualTo(response1.id());
        assertThat(bookingResponse.sessionType()).isEqualTo(SessionType.SMALL);
        assertThat(bookingResponse.startTime()).isEqualTo(DEFAULT_START_TIME);
        assertThat(bookingResponse.endTime()).isEqualTo(DEFAULT_START_TIME.plusHours(1));
        assertThat(bookingResponse.endOfBufferTime()).isEqualTo(DEFAULT_START_TIME.plusHours(2));
        assertThat(bookingResponse.status()).isEqualTo(BookingStatus.PENDING);
        assertThat(bookingResponse.notes()).isNull();
        assertThat(bookingResponse.imagePath()).isNull();
        assertThat(bookingResponse.createdAt()).isNotNull();
        assertThat(bookingResponse.artistId()).isEqualTo(TEST_ARTIST_ID);

        // 3. Try to register with same email → should fail
        client.post()
                .uri("/api/auth/register")
                .body(request1)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Email already registered: vasya_huligan228@gmail.com");
    }
}

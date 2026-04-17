package com.tattoo.scheduler.controller;

import com.tattoo.scheduler.controller.mapper.BookingDTOMapper;
import com.tattoo.scheduler.domain.Booking;
import com.tattoo.scheduler.dto.BookingResponse;
import com.tattoo.scheduler.dto.CreateBookingRequest;
import com.tattoo.scheduler.model.SessionType;
import com.tattoo.scheduler.service.BookingService;
import com.tattoo.scheduler.service.exception.ArtistNotFoundException;
import com.tattoo.scheduler.service.exception.BookingConflictException;
import com.tattoo.scheduler.service.exception.BookingDateNotAllowedException;
import com.tattoo.scheduler.service.exception.BookingOutsideWorkingHoursException;
import com.tattoo.scheduler.util.TestJwtGenerator;
import com.tattoo.scheduler.util.TestResponseFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static com.tattoo.scheduler.util.TestData.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test-h2")
@ExtendWith(MockitoExtension.class)
@Sql(scripts = "/test-data-h2.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestJwtGenerator jwtGenerator;
    private String validToken;

    @MockitoBean
    private BookingService bookingService;

    @MockitoBean
    private BookingDTOMapper bookingDTOMapper;

    @Captor
    private ArgumentCaptor<CreateBookingRequest> requestCaptor;
    @Captor
    private ArgumentCaptor<Long> userIdCaptor;
    @Captor
    private ArgumentCaptor<Long> artistIdCaptor;

    @BeforeEach
    void setUp() {
        validToken = jwtGenerator.generateValidToken("testuser@example.com");
    }

    @Test
    void createBooking_shouldMapAndSetIdsCorrectly_whenHappyPath() throws Exception {
        String requestBody = """
                {
                    "sessionType": "MEDIUM",
                    "startTime": "2026-06-15T10:00",
                    "notes": "Test"
                }
                """;
        Booking mappedBooking = new Booking();
        Booking savedBooking = new Booking();
        BookingResponse response = TestResponseFactory.response().withNotes("Test").build();

        when(bookingDTOMapper.toDomain(any(CreateBookingRequest.class), anyLong(), any()))
                .thenReturn(mappedBooking);
        when(bookingService.createBooking(any())).thenReturn(savedBooking);
        when(bookingDTOMapper.toResponse(savedBooking)).thenReturn(response);

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sessionType").value("MEDIUM"))
                .andExpect(jsonPath("$.startTime").value("2026-06-15T10:00:00"))
                .andExpect(jsonPath("$.notes").value("Test"))
                .andExpect(jsonPath("$.userId").value(TEST_USER_ID))
                .andExpect(jsonPath("$.artistId").value(TEST_ARTIST_ID))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(bookingDTOMapper).toDomain(requestCaptor.capture(),
                userIdCaptor.capture(), artistIdCaptor.capture());
        assertThat(requestCaptor.getValue().sessionType()).isEqualTo(SessionType.MEDIUM);
        assertThat(requestCaptor.getValue().startTime()).isEqualTo(DEFAULT_START_TIME);
        assertThat(requestCaptor.getValue().notes()).isEqualTo("Test");
        assertThat(requestCaptor.getValue().imagePath()).isNull();
        assertThat(userIdCaptor.getValue()).isEqualTo(TEST_USER_ID);
        assertThat(artistIdCaptor.getValue()).isNull();
    }
    @Test
    void createBooking_shouldPassArtistId_whenProvided() throws Exception {
        Long providedArtistId = 2L;
        String requestBody = """
                {
                    "sessionType": "MEDIUM",
                    "startTime": "2026-04-15T10:00"
                }
                """;
        Booking mappedBooking = new Booking();
        Booking savedBooking = new Booking();
        BookingResponse response = TestResponseFactory.response().withNotes("Test").build();

        when(bookingDTOMapper.toDomain(any(CreateBookingRequest.class), anyLong(), any()))
                .thenReturn(mappedBooking);
        when(bookingService.createBooking(any())).thenReturn(savedBooking);
        when(bookingDTOMapper.toResponse(savedBooking)).thenReturn(response);

        mockMvc.perform(post("/api/bookings?artistId=" + providedArtistId)
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());

        verify(bookingDTOMapper).toDomain(requestCaptor.capture(),
                userIdCaptor.capture(), artistIdCaptor.capture());
        assertThat(artistIdCaptor.getValue()).isEqualTo(providedArtistId);
    }
    @Test
    void jwtFilter_shouldReturn401_whenTokenForNonExistentUser() throws Exception {
        String tokenForGhost = jwtGenerator.generateTokenForNonExistentUser();

        String requestBody = """
            {
                "sessionType": "MEDIUM",
                "startTime": "2026-04-15T10:00"
            }
            """;

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + tokenForGhost)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid token"));
    }
    @Test
    void createBooking_shouldReturn404_whenArtistNotFound() throws Exception {
        String requestBody = """
            {
                "sessionType": "MEDIUM",
                "startTime": "2026-04-15T10:00"
            }
            """;
        Booking mappedBooking = new Booking();
        when(bookingDTOMapper.toDomain(any(CreateBookingRequest.class), anyLong(), any()))
                .thenReturn(mappedBooking);
        when(bookingService.createBooking(any()))
                .thenThrow(new ArtistNotFoundException(TEST_NONEXISTING_ARTIST_ID));

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Artist with id 658 not found"));
    }
    @Test
    void createBooking_shouldReturn409_whenConflictWithExistingBooking() throws Exception {
        String requestBody = """
            {
                "sessionType": "MEDIUM",
                "startTime": "2026-06-15T10:00"
            }
            """;
        Booking mappedBooking = new Booking();
        when(bookingDTOMapper.toDomain(any(CreateBookingRequest.class), anyLong(), any()))
                .thenReturn(mappedBooking);
        when(bookingService.createBooking(any()))
                .thenThrow(new BookingConflictException(TEST_ARTIST_ID, DEFAULT_START_TIME));

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Cannot book at 2026-06-15T10:00 " +
                        "for artist 1 due to conflict with existing booking"));
    }
    @Test
    void createBooking_shouldReturn400_whenDateNotAllowed() throws Exception {
        String requestBody = """
            {
                "sessionType": "MEDIUM",
                "startTime": "2026-02-15T10:00"
            }
            """;
        Booking mappedBooking = new Booking();
        when(bookingDTOMapper.toDomain(any(CreateBookingRequest.class), anyLong(), any()))
                .thenReturn(mappedBooking);
        when(bookingService.createBooking(any()))
                .thenThrow(new BookingDateNotAllowedException(
                        DEFAULT_START_TIME.minusMonths(2).toLocalDate()));

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Booking cannot be made on "
                        + DEFAULT_START_TIME.minusMonths(2).toLocalDate().toString()
                        + ". Only tomorrow onwards is allowed."));
    }
    @Test
    void createBooking_shouldReturn400_whenOutsideWorkingHours() throws Exception {
        String requestBody = """
            {
                "sessionType": "MEDIUM",
                "startTime": "2026-04-15T07:00"
            }
            """;
        Booking mappedBooking = new Booking();
        when(bookingDTOMapper.toDomain(any(CreateBookingRequest.class), anyLong(), any()))
                .thenReturn(mappedBooking);
        when(bookingService.createBooking(any()))
                .thenThrow(new BookingOutsideWorkingHoursException(
                        DEFAULT_START_TIME.minusHours(3)));

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Session starting at " +
                        DEFAULT_START_TIME.minusHours(3).toLocalTime().toString() +
                        " must be within 10:00–20:00 and finish by 20:00"));
    }
    @Test
    void createBooking_shouldReturn400_whenSessionTypeMissing() throws Exception {
        String requestBody = """
                {
                    "startTime": "2026-04-15T10:00"
                }
                """;

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("sessionType: must not be null"));
    }

    @Test
    void createBooking_shouldReturn400_whenStartTimeMissing() throws Exception {
        String requestBody = """
                {
                    "sessionType": "SMALL"
                }
                """;

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("startTime: must not be null"));
    }

    @Test
    void createBooking_shouldReturn400_whenStartTimeInvalidFormat() throws Exception {
        String requestBody = """
                {
                    "sessionType": "MEDIUM",
                    "startTime": "15-04-2026 10:00"
                }
                """;

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "Invalid date format. Please use ISO format: yyyy-MM-ddTHH:mm:ss"));
    }

    @Test
    void createBooking_shouldReturn400_whenSessionTypeInvalidEnum() throws Exception {
        String requestBody = """
                {
                    "sessionType": "HIGH",
                    "startTime": "2026-04-15T10:00"
                }
                """;

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "Invalid sessionType. Allowed values: " +
                                "SMALL_CONSULTATION, SMALL, LARGE_CONSULTATION, MEDIUM, LARGE"));
    }

    @Test
    void jwtFilter_shouldReturn401_whenHeaderMissing() throws Exception {
        String requestBody = """
                {
                    "sessionType": "SMALL",
                    "startTime": "2026-04-15T10:00"
                }
                """;

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid token"));
    }
    @Test
    void jwtFilter_shouldReturn401_whenInvalidHeaderFormat() throws Exception {

        String requestBody = """
            {
                "sessionType": "MEDIUM",
                "startTime": "2026-04-15T10:00"
            }
            """;

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bear " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid token"));
    }
    @Test
    void jwtFilter_shouldReturn401_whenInvalidTokenStructure() throws Exception {
        String malformedToken = jwtGenerator.generateMalformedToken();

        String requestBody = """
            {
                "sessionType": "MEDIUM",
                "startTime": "2026-04-15T10:00"
            }
            """;

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + malformedToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid token"));
    }
    @Test
    void jwtFilter_shouldReturn401_whenTokenExpired() throws Exception {
        String expiredToken = jwtGenerator.generateExpiredToken("testuser@example.com");

        String requestBody = """
            {
                "sessionType": "MEDIUM",
                "startTime": "2026-04-15T10:00"
            }
            """;

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + expiredToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid token"));
    }
}

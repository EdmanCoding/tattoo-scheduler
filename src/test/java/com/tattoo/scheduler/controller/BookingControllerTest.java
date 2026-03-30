package com.tattoo.scheduler.controller;

import com.tattoo.scheduler.controller.mapper.BookingDTOMapper;
import com.tattoo.scheduler.domain.Booking;
import com.tattoo.scheduler.dto.BookingResponse;
import com.tattoo.scheduler.dto.CreateBookingRequest;
import com.tattoo.scheduler.model.SessionType;
import com.tattoo.scheduler.service.BookingService;
import com.tattoo.scheduler.service.exception.*;
import com.tattoo.scheduler.util.TestResponseFactory;
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
import org.springframework.test.web.servlet.MockMvc;

import static com.tattoo.scheduler.util.TestData.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test-h2")
@ExtendWith(MockitoExtension.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookingService bookingService;

    @MockitoBean
    private BookingDTOMapper bookingDTOMapper;

    @Captor
    private ArgumentCaptor<Booking> bookingCaptor;
    @Captor
    private ArgumentCaptor<CreateBookingRequest> requestCaptor;

    @Test
    void createBooking_shouldMapAndSetIdsCorrectly_whenHappyPath() throws Exception {
        // Given
        String requestBody = """
                {
                    "sessionType": "MEDIUM",
                    "startTime": "2026-04-15T10:00",
                    "notes": "Test"
                }
                """;
        Booking mappedBooking = new Booking(); // the object returned by the mapper
        Booking savedBooking = new Booking();  // the object returned by the service
        BookingResponse response = TestResponseFactory.response().withNotes("Test").build();

        when(bookingDTOMapper.toDomain(any())).thenReturn(mappedBooking);
        when(bookingService.createBooking(any())).thenReturn(savedBooking);
        when(bookingDTOMapper.toResponse(savedBooking)).thenReturn(response);

        // When
        mockMvc.perform(post("/api/bookings")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sessionType").value("MEDIUM"))
                .andExpect(jsonPath("$.startTime").value("2026-04-15T10:00:00"))
                .andExpect(jsonPath("$.notes").value("Test"))
                .andExpect(jsonPath("$.userId").value("1"))
                .andExpect(jsonPath("$.artistId").value("1"))
                .andExpect(jsonPath("$.status").value("PENDING"));

        // Then
        verify(bookingDTOMapper).toDomain(requestCaptor.capture());
        assertThat(requestCaptor.getValue().sessionType()).isEqualTo(SessionType.MEDIUM);
        assertThat(requestCaptor.getValue().startTime()).isEqualTo(DEFAULT_START_TIME);
        assertThat(requestCaptor.getValue().notes()).isEqualTo("Test");

        verify(bookingService).createBooking(bookingCaptor.capture());
        Booking passedBooking = bookingCaptor.getValue();
        assertThat(passedBooking.getUserId()).isEqualTo(TEST_USER_ID);
        assertThat(passedBooking.getArtistId()).isNull();
    }
    @Test
    void createBooking_shouldReturn404_whenUserNotFound() throws Exception {
        String requestBody = """
            {
                "sessionType": "MEDIUM",
                "startTime": "2026-04-15T10:00"
            }
            """;
        Booking mappedBooking = new Booking();
        when(bookingDTOMapper.toDomain(any())).thenReturn(mappedBooking);
        when(bookingService.createBooking(any()))
                .thenThrow(new UserNotFoundException(TEST_NONEXISTING_USER_ID));

        mockMvc.perform(post("/api/bookings")
                        .header("X-User-Id", "658")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User with id 658 not found"));
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
        when(bookingDTOMapper.toDomain(any())).thenReturn(mappedBooking);
        when(bookingService.createBooking(any()))
                .thenThrow(new ArtistNotFoundException(TEST_NONEXISTING_ARTIST_ID));

        mockMvc.perform(post("/api/bookings")
                        .header("X-User-Id", "1")
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
                "startTime": "2026-04-15T10:00"
            }
            """;
        Booking mappedBooking = new Booking();
        when(bookingDTOMapper.toDomain(any())).thenReturn(mappedBooking);
        when(bookingService.createBooking(any()))
                .thenThrow(new BookingConflictException(TEST_ARTIST_ID, DEFAULT_START_TIME));

        mockMvc.perform(post("/api/bookings")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Cannot book at 2026-04-15T10:00 " +
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
        when(bookingDTOMapper.toDomain(any())).thenReturn(mappedBooking);
        when(bookingService.createBooking(any()))
                .thenThrow(new BookingDateNotAllowedException(
                        DEFAULT_START_TIME.minusMonths(2).toLocalDate()));

        mockMvc.perform(post("/api/bookings")
                        .header("X-User-Id", "1")
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
        when(bookingDTOMapper.toDomain(any())).thenReturn(mappedBooking);
        when(bookingService.createBooking(any()))
                .thenThrow(new BookingOutsideWorkingHoursException(
                        DEFAULT_START_TIME.minusHours(3)));

        mockMvc.perform(post("/api/bookings")
                        .header("X-User-Id", "1")
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
                        .header("X-User-Id", String.valueOf(1L))
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
                        .header("X-User-Id", String.valueOf(1L))
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
                        .header("X-User-Id", 1L)
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
                        .header("X-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "Invalid sessionType. Allowed values: " +
                                "SMALL_CONSULTATION, SMALL, LARGE_CONSULTATION, MEDIUM, LARGE"));
    }

    @Test
    void createBooking_shouldReturn400_whenUserIdHeaderMissing() throws Exception {
        String requestBody = """
                {
                    "sessionType": "SMALL",
                    "startTime": "2026-04-15T10:00"
                }
                """;

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Missing required header: X-User-Id"));
    }
}

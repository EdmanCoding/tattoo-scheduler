package com.tattoo.scheduler.service;

import com.tattoo.scheduler.dto.BookingResponse;
import com.tattoo.scheduler.dto.CreateBookingRequest;
import com.tattoo.scheduler.model.*;
import com.tattoo.scheduler.repository.ArtistRepository;
import com.tattoo.scheduler.repository.BookingRepository;
import com.tattoo.scheduler.repository.UserRepository;
import com.tattoo.scheduler.service.exception.BookingConflictException;
import com.tattoo.scheduler.service.exception.UserNotFoundException;
import com.tattoo.scheduler.util.TestData;
import com.tattoo.scheduler.util.TestRequestFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BookingServiceUnitTest {
    // === Mocks and InjectMocks ===
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ArtistRepository artistRepository;
    @InjectMocks
    private BookingService bookingService;

    // === Constants ===
    private static final Long TEST_USER_ID = 1L;
    private static final Long TEST_ARTIST_ID = 1L;
    private static final Long TEST_NONEXISTING_USER_ID = 658L;
    private static final LocalDateTime TEST_START_TIME = LocalDateTime.of(2026, 4, 10, 10, 0);

    // === Captors ===
    @Captor
    private ArgumentCaptor<Long> artistIdCaptor;
    @Captor
    private ArgumentCaptor<LocalDateTime> startTimeCaptor;
    @Captor
    private ArgumentCaptor<LocalDateTime> endTimeCaptor;
    @Captor
    private ArgumentCaptor<BookingStatus> statusCaptor;
    @Captor
    private ArgumentCaptor<Booking> bookingCaptor;

    @Test
    @DisplayName("Should auto-calculate endTime when not provided")
    void autoCalculateEndTimeTest() {
        // Arrange
        User user = TestData.createTestUser1();
        user.setId(TEST_USER_ID);

        Artist artist = TestData.createTestArtist();
        artist.setId(TEST_ARTIST_ID);

        CreateBookingRequest request = TestRequestFactory.request()
                .ofType(SessionType.MEDIUM).at(TEST_START_TIME).build();

        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(user));
        when(artistRepository.getReferenceById(TEST_ARTIST_ID)).thenReturn(artist);
        when(bookingRepository.hasOverlap(anyLong(), any(), any(), any())).thenReturn(false);
        when(bookingRepository.save(bookingCaptor.capture())).thenAnswer(invocation -> {
            Booking b = bookingCaptor.getValue();
            b.setId(1L);
            return b;
        });

        // Act
        BookingResponse response = bookingService.createBooking(TEST_USER_ID, request);

        // Assert
        Booking savedBooking = bookingCaptor.getValue();
        assertThat(savedBooking.getEndTime()).isEqualTo(
                request.startTime().plusMinutes(SessionType.MEDIUM.getDurationMinutes()));
    }
    @Test
    @DisplayName("Should throw exception with non-existing user id")
    void nonExistingUserIdTest() {
        // Arrange
        CreateBookingRequest request = TestRequestFactory.request().build();

        // Act and Assert
        assertThatThrownBy(()->bookingService.createBooking(TEST_NONEXISTING_USER_ID, request))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User with id ");
    }
    @Test
    @DisplayName("Should call hasOverlap with correct parameters")
    void hasOverlapWithCorrectParametersTest() {
        // Arrange
        User user = TestData.createTestUser1();
        user.setId(TEST_USER_ID);

        Artist artist = TestData.createTestArtist();
        artist.setId(TEST_ARTIST_ID);

        CreateBookingRequest request = TestRequestFactory.request()
                .ofType(SessionType.MEDIUM).at(TEST_START_TIME).build();

        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(user));
        when(artistRepository.getReferenceById(TEST_ARTIST_ID)).thenReturn(artist);
        // Mock hasOverlap to return false (no conflict)
        when(bookingRepository.hasOverlap(anyLong(), any(), any(), any()))
                .thenReturn(false);
        // Mock save to return the booking
        when(bookingRepository.save(any(Booking.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        bookingService.createBooking(TEST_USER_ID, request);

        // Assert - capture and verify arguments
        verify(bookingRepository).hasOverlap(
                artistIdCaptor.capture(),
                startTimeCaptor.capture(),
                endTimeCaptor.capture(),
                statusCaptor.capture()
        );

        assertThat(artistIdCaptor.getValue()).isEqualTo(TEST_ARTIST_ID);
        assertThat(startTimeCaptor.getValue()).isEqualTo(TEST_START_TIME);
        assertThat(endTimeCaptor.getValue()).isEqualTo(LocalDateTime.of(2026,4,10,16,0));
        assertThat(statusCaptor.getValue()).isEqualTo(BookingStatus.CANCELLED);
    }
    @Test
    @DisplayName("Should throw BookingConflictException when time slot is taken")
    void conflictTest() {
        // Arrange
        User user = TestData.createTestUser1();
        user.setId(TEST_USER_ID);
        Artist artist = TestData.createTestArtist();
        artist.setId(TEST_ARTIST_ID);

        CreateBookingRequest request = TestRequestFactory.request()
                .ofType(SessionType.MEDIUM).at(TEST_START_TIME).build();

        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(user));
        when(artistRepository.getReferenceById(TEST_ARTIST_ID)).thenReturn(artist);
        when(bookingRepository.hasOverlap(anyLong(), any(), any(), any())).thenReturn(true); // Conflict!

        // Act & Assert
        assertThatThrownBy(() -> bookingService.createBooking(TEST_USER_ID, request))
                .isInstanceOf(BookingConflictException.class);
    }
}

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
import org.junit.jupiter.api.Disabled;
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

import static com.tattoo.scheduler.util.TestData.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Disabled("Keeping for reference — needs refactoring")
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
    private static final LocalDateTime TEST_START_TIME = LocalDateTime.of(2026, 4, 10, 10, 0);

    // === Captors ===
    @Captor
    private ArgumentCaptor<Long> artistIdCaptor;
    @Captor
    private ArgumentCaptor<LocalDateTime> startTimeCaptor;
    @Captor
    private ArgumentCaptor<LocalDateTime> endOfBufferTimeCaptor;
    @Captor
    private ArgumentCaptor<BookingStatus> statusCaptor;
    @Captor
    private ArgumentCaptor<BookingEntity> bookingCaptor;

    @Test
    @DisplayName("Should auto-calculate endTime")
    void autoCalculateEndTimeTest() {
        // Arrange
        UserEntity userEntity = TestData.createTestUser1();
        userEntity.setId(TEST_USER_ID);

        ArtistEntity artistEntity = TestData.createTestArtist();
        artistEntity.setId(TEST_ARTIST_ID);

        CreateBookingRequest request = TestRequestFactory.request()
                .ofType(SessionType.MEDIUM).at(TEST_START_TIME).build();

        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(userEntity));
        when(artistRepository.getReferenceById(TEST_ARTIST_ID)).thenReturn(artistEntity);
        when(bookingRepository.hasOverlap(anyLong(), any(), any(), any())).thenReturn(false);
        when(bookingRepository.save(bookingCaptor.capture())).thenAnswer(invocation -> {
            BookingEntity b = bookingCaptor.getValue();
            b.setId(1L);
            return b;
        });

        // Act
        BookingResponse response = bookingService.createBooking(TEST_USER_ID, request);

        // Assert
        BookingEntity savedBookingEntity = bookingCaptor.getValue();
        assertThat(savedBookingEntity.getEndTime()).isEqualTo(
                request.startTime().plusMinutes(SessionType.MEDIUM.getDurationMinutes()));
    }
    @Test
    @DisplayName("Should auto-calculate endOfBufferTime")
    void autoCalculateEndOfBufferTimeTest() {
        // Arrange
        UserEntity userEntity = TestData.createTestUser1();
        userEntity.setId(TEST_USER_ID);

        ArtistEntity artistEntity = TestData.createTestArtist();
        artistEntity.setId(TEST_ARTIST_ID);

        CreateBookingRequest request = TestRequestFactory.request()
                .ofType(SessionType.MEDIUM).at(TEST_START_TIME).build();

        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(userEntity));
        when(artistRepository.getReferenceById(TEST_ARTIST_ID)).thenReturn(artistEntity);
        when(bookingRepository.hasOverlap(anyLong(), any(), any(), any())).thenReturn(false);
        when(bookingRepository.save(bookingCaptor.capture())).thenAnswer(invocation -> {
            BookingEntity b = bookingCaptor.getValue();
            b.setId(1L);
            return b;
        });

        // Act
        BookingResponse response = bookingService.createBooking(TEST_USER_ID, request);

        // Assert
        BookingEntity savedBookingEntity = bookingCaptor.getValue();
        assertThat(savedBookingEntity.getEndOfBufferTime()).isEqualTo(
                request.startTime().plusMinutes(SessionType.MEDIUM.getDurationMinutes())
                        .plusMinutes(SessionType.MEDIUM.getBufferAfterMinutes()));
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
        UserEntity userEntity = TestData.createTestUser1();
        userEntity.setId(TEST_USER_ID);

        ArtistEntity artistEntity = TestData.createTestArtist();
        artistEntity.setId(TEST_ARTIST_ID);

        CreateBookingRequest request = TestRequestFactory.request()
                .ofType(SessionType.MEDIUM).at(TEST_START_TIME).build();

        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(userEntity));
        when(artistRepository.getReferenceById(TEST_ARTIST_ID)).thenReturn(artistEntity);
        // Mock hasOverlap to return false (no conflict)
        when(bookingRepository.hasOverlap(anyLong(), any(), any(), any()))
                .thenReturn(false);
        // Mock save to return the booking
        when(bookingRepository.save(any(BookingEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        bookingService.createBooking(TEST_USER_ID, request);

        // Assert - capture and verify arguments
        verify(bookingRepository).hasOverlap(
                artistIdCaptor.capture(),
                startTimeCaptor.capture(),
                endOfBufferTimeCaptor.capture(),
                statusCaptor.capture()
        );

        assertThat(artistIdCaptor.getValue()).isEqualTo(TEST_ARTIST_ID);
        assertThat(startTimeCaptor.getValue()).isEqualTo(TEST_START_TIME);
        // endOfBufferTime = session time(4h) + 2h buffer
        assertThat(endOfBufferTimeCaptor.getValue()).isEqualTo(TEST_START_TIME.plusHours(6));
        assertThat(statusCaptor.getValue()).isEqualTo(BookingStatus.CANCELLED);
    }
    @Test
    @DisplayName("Should throw BookingConflictException when time slot is taken")
    void conflictTest() {
        // Arrange
        UserEntity userEntity = TestData.createTestUser1();
        userEntity.setId(TEST_USER_ID);
        ArtistEntity artistEntity = TestData.createTestArtist();
        artistEntity.setId(TEST_ARTIST_ID);

        CreateBookingRequest request = TestRequestFactory.request()
                .ofType(SessionType.MEDIUM).at(TEST_START_TIME).build();

        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(userEntity));
        when(artistRepository.getReferenceById(TEST_ARTIST_ID)).thenReturn(artistEntity);
        when(bookingRepository.hasOverlap(anyLong(), any(), any(), any())).thenReturn(true); // Conflict!

        // Act & Assert
        assertThatThrownBy(() -> bookingService.createBooking(TEST_USER_ID, request))
                .isInstanceOf(BookingConflictException.class);
    }
}

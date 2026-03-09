package com.tattoo.scheduler.service;

import com.tattoo.scheduler.model.Artist;
import com.tattoo.scheduler.model.Booking;
import com.tattoo.scheduler.model.SessionType;
import com.tattoo.scheduler.model.User;
import com.tattoo.scheduler.repository.BookingRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BookingServiceUnitTest {
    @Mock
    private BookingRepository bookingRepository;
    @InjectMocks
    private BookingService bookingService;

    @Test
    @DisplayName("Should auto-calculate endTime when not provided")
    void autoCalculateEndTimeTest() {
        // Arrange
        Booking booking = new Booking();
        booking.setUser(new User()); // to pass validation
        booking.setArtist(new Artist());
        booking.setStartTime(LocalDateTime.of(2026,3,10,12,0));
        booking.setSessionType(SessionType.LARGE);

        when(bookingRepository.save(any(Booking.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Booking result = bookingService.createBooking(booking);

        // Assert
        assertThat(result.getEndTime()).isEqualTo(LocalDateTime.of(2026,3,10,20,0));
    }
    @Test
    @DisplayName("Should not override existing endTime")
    void statedEndTimeTest(){
        // Arrange
        Booking booking = new Booking();
        booking.setUser(new User());
        booking.setArtist(new Artist());
        booking.setStartTime(LocalDateTime.of(2026,3,10,12,0));
        booking.setSessionType(SessionType.LARGE);
        booking.setEndTime(LocalDateTime.of(2026,3,10,19,0));

        when(bookingRepository.save(any(Booking.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Booking result = bookingService.createBooking(booking);

        // Assert
        assertThat(result.getEndTime()).isEqualTo(LocalDateTime.of(2026,3,10,19,0));
    }
    @Test
    @DisplayName("Should throw exception when artist is missing")
    void artistMissingTest() {
        // Arrange
        Booking booking = new Booking();
        booking.setUser(new User());
        booking.setStartTime(LocalDateTime.of(2026,3,10,12,0));
        booking.setSessionType(SessionType.LARGE);

        // Act and Assert
        assertThatThrownBy(()->bookingService.createBooking(booking))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Artist and User are required");
    }
    @Test
    @DisplayName("Should throw exception when user is missing")
    void userMissingTest() {
        // Arrange
        Booking booking = new Booking();
        booking.setArtist(new Artist());
        booking.setStartTime(LocalDateTime.of(2026,3,10,12,0));
        booking.setSessionType(SessionType.LARGE);

        // Act and Assert
        assertThatThrownBy(()->bookingService.createBooking(booking))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Artist and User are required");
    }

}

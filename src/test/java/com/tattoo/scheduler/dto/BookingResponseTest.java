package com.tattoo.scheduler.dto;

import com.tattoo.scheduler.model.Booking;
import com.tattoo.scheduler.model.BookingStatus;
import com.tattoo.scheduler.model.SessionType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static com.tattoo.scheduler.util.TestData.createTestBooking;
import static org.assertj.core.api.Assertions.assertThat;

public class BookingResponseTest {
    @Test
    public void from_ShouldMapAllFieldsTest() {
        // Arrange
        Booking booking = createTestBooking();
        // Act
        BookingResponse response = BookingResponse.from(booking);
        // Assert
        assertThat(response.id()).isEqualTo(99L);
        assertThat(response.userId()).isEqualTo(42L);
        assertThat(response.artistId()).isEqualTo(1L);
        assertThat(response.sessionType()).isEqualTo(SessionType.MEDIUM);
        assertThat(response.startTime()).isEqualTo(LocalDateTime.of(2026, 4, 15, 10, 0));
        assertThat(response.endTime()).isEqualTo(LocalDateTime.of(2026, 4, 15, 14, 0));
        assertThat(response.status()).isEqualTo(BookingStatus.PENDING);
        assertThat(response.notes()).isEqualTo("Test notes");
        assertThat(response.imagePath()).isEqualTo("/images/test.png");
        assertThat(response.createdAt()).isEqualTo(LocalDateTime.of(2026,3,10,17,0));
    }
}

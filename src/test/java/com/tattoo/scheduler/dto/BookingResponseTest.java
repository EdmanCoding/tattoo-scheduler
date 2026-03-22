package com.tattoo.scheduler.dto;

import com.tattoo.scheduler.model.BookingEntity;
import com.tattoo.scheduler.model.BookingStatus;
import com.tattoo.scheduler.model.SessionType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static com.tattoo.scheduler.util.TestData.*;
import static org.assertj.core.api.Assertions.assertThat;

public class BookingResponseTest {
    @Test
    public void from_ShouldMapAllFieldsTest() {
        // Arrange
        BookingEntity bookingEntity = createTestBooking();
        // Act
        BookingResponse response = BookingResponse.from(bookingEntity);
        // Assert
        assertThat(response.id()).isEqualTo(99L);
        assertThat(response.userId()).isEqualTo(42L);
        assertThat(response.artistId()).isEqualTo(1L);
        assertThat(response.sessionType()).isEqualTo(SessionType.MEDIUM);
        assertThat(response.startTime()).isEqualTo(DEFAULT_START_TIME);
        assertThat(response.endTime()).isEqualTo(DEFAULT_END_TIME);
        assertThat(response.endOfBufferTime()).isEqualTo(DEFAULT_END_OF_BUFFER_TIME);
        assertThat(response.status()).isEqualTo(BookingStatus.PENDING);
        assertThat(response.notes()).isEqualTo("Test notes");
        assertThat(response.imagePath()).isEqualTo("/images/test.png");
        assertThat(response.createdAt()).isEqualTo(LocalDateTime.of(2026,3,10,17,0));
    }
}

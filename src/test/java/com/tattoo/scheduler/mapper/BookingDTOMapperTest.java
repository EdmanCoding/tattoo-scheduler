package com.tattoo.scheduler.mapper;

import com.tattoo.scheduler.controller.mapper.BookingDTOMapper;
import com.tattoo.scheduler.domain.Booking;
import com.tattoo.scheduler.dto.BookingResponse;
import com.tattoo.scheduler.dto.CreateBookingRequest;
import com.tattoo.scheduler.model.SessionType;
import com.tattoo.scheduler.util.TestData;
import com.tattoo.scheduler.util.TestRequestFactory;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static com.tattoo.scheduler.util.TestData.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class BookingDTOMapperTest {
    private final BookingDTOMapper bookingDTOMapper = Mappers.getMapper(BookingDTOMapper.class);

    @Test
    void toDomain_shouldMapRequestToDomain() {
        CreateBookingRequest request = TestRequestFactory.request()
                .ofType(SessionType.MEDIUM)
                .at(DEFAULT_START_TIME)
                .withNotes("Test notes")
                .withImage("image/test.png").build();
        Long userId = TEST_USER_ID;
        Long artistId = TEST_ARTIST_ID;

        Booking domain = bookingDTOMapper.toDomain(request, userId, artistId);

        assertThat(domain).isNotNull();
        assertThat(domain.getSessionType()).isEqualTo(request.sessionType());
        assertThat(domain.getStartTime()).isEqualTo(request.startTime());
        assertThat(domain.getNotes()).isEqualTo(request.notes());
        assertThat(domain.getImagePath()).isEqualTo(request.imagePath());
        assertThat(domain.getUserId()).isEqualTo(userId);
        assertThat(domain.getArtistId()).isEqualTo(artistId);
        // These fields are calculated by the service, not mapped from the request
        assertThat(domain.getEndTime()).isNull();
        assertThat(domain.getEndOfBufferTime()).isNull();
        assertThat(domain.getStatus()).isNull();
        assertThat(domain.getCreatedAt()).isNull();
        assertThat(domain.getUpdatedAt()).isNull();
    }

    @Test
    void toResponse_shouldMapDomainToResponse() {
        Booking domain = TestData.createTestBookingDomain();

        BookingResponse response = bookingDTOMapper.toResponse(domain);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(domain.getId());
        assertThat(response.userId()).isEqualTo(domain.getUserId());
        assertThat(response.artistId()).isEqualTo(domain.getArtistId());
        assertThat(response.sessionType()).isEqualTo(domain.getSessionType());
        assertThat(response.startTime()).isEqualTo(domain.getStartTime());
        assertThat(response.endTime()).isEqualTo(domain.getEndTime());
        assertThat(response.endOfBufferTime()).isEqualTo(domain.getEndOfBufferTime());
        assertThat(response.status()).isEqualTo(domain.getStatus());
        assertThat(response.notes()).isEqualTo(domain.getNotes());
        assertThat(response.imagePath()).isEqualTo(domain.getImagePath());
        assertThat(response.createdAt()).isEqualTo(domain.getCreatedAt());
    }
}

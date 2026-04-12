package com.tattoo.scheduler.mapper;

import com.tattoo.scheduler.domain.Booking;
import com.tattoo.scheduler.model.BookingEntity;
import com.tattoo.scheduler.util.TestData;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class BookingMapperTest {
    private final BookingMapper mapper = Mappers.getMapper(BookingMapper.class);

    @Test
    void toDomain_shouldMapEntityToDomain() {

        BookingEntity entity = TestData.createTestBookingEntity();

        Booking domain = mapper.toDomain(entity);

        assertThat(domain).isNotNull();
        assertThat(domain.getId()).isEqualTo(entity.getId());
        assertThat(domain.getUserId()).isEqualTo(entity.getUserEntity().getId());
        assertThat(domain.getArtistId()).isEqualTo(entity.getArtistEntity().getId());
        assertThat(domain.getSessionType()).isEqualTo(entity.getSessionType());
        assertThat(domain.getStartTime()).isEqualTo(entity.getStartTime());
        assertThat(domain.getEndTime()).isEqualTo(entity.getEndTime());
        assertThat(domain.getEndOfBufferTime()).isEqualTo(entity.getEndOfBufferTime());
        assertThat(domain.getStatus()).isEqualTo(entity.getStatus());
        assertThat(domain.getNotes()).isEqualTo(entity.getNotes());
        assertThat(domain.getImagePath()).isEqualTo(entity.getImagePath());
        assertThat(domain.getCreatedAt()).isEqualTo(entity.getCreatedAt());
        assertThat(domain.getUpdatedAt()).isEqualTo(entity.getUpdatedAt());
    }

    @Test
    void toEntity_shouldMapDomainToEntity() {

        Booking domain = TestData.createTestBookingDomain();

        BookingEntity entity = mapper.toEntity(domain);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(domain.getId());
        assertThat(entity.getUserEntity()).isNull();
        assertThat(entity.getArtistEntity()).isNull();
        assertThat(entity.getSessionType()).isEqualTo(domain.getSessionType());
        assertThat(entity.getStartTime()).isEqualTo(domain.getStartTime());
        assertThat(entity.getEndTime()).isEqualTo(domain.getEndTime());
        assertThat(entity.getEndOfBufferTime()).isEqualTo(domain.getEndOfBufferTime());
        assertThat(entity.getStatus()).isEqualTo(domain.getStatus());
        assertThat(entity.getNotes()).isEqualTo(domain.getNotes());
        assertThat(entity.getImagePath()).isEqualTo(domain.getImagePath());
        assertThat(entity.getCreatedAt()).isEqualTo(domain.getCreatedAt());
        assertThat(entity.getUpdatedAt()).isEqualTo(domain.getUpdatedAt());
    }
}

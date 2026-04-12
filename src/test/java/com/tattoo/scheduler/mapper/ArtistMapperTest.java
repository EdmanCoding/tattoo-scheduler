package com.tattoo.scheduler.mapper;

import com.tattoo.scheduler.domain.Artist;
import com.tattoo.scheduler.model.ArtistEntity;
import com.tattoo.scheduler.util.TestData;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class ArtistMapperTest {
    private final ArtistMapper mapper = Mappers.getMapper(ArtistMapper.class);

    @Test
    void toDomain_shouldMapEntityToDomain() {
        ArtistEntity entity = TestData.createTestArtistEntity();

        Artist domain = mapper.toDomain(entity);

        assertThat(domain).isNotNull();
        assertThat(domain.getId()).isEqualTo(entity.getId());
        assertThat(domain.getName()).isEqualTo(entity.getName());
        assertThat(domain.getEmail()).isEqualTo(entity.getEmail());
        assertThat(domain.getPassword()).isEqualTo(entity.getPassword());
        assertThat(domain.getCreatedAt()).isEqualTo(entity.getCreatedAt());
    }

    @Test
    void toEntity_shouldMapDomainToEntity() {
        Artist domain = TestData.createTestArtistDomain();

        ArtistEntity entity = mapper.toEntity(domain);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(domain.getId());
        assertThat(entity.getName()).isEqualTo(domain.getName());
        assertThat(entity.getEmail()).isEqualTo(domain.getEmail());
        assertThat(entity.getPassword()).isEqualTo(domain.getPassword());
        assertThat(entity.getCreatedAt()).isEqualTo(domain.getCreatedAt());
        assertThat(entity.getBookingEntities()).isNull();
    }
}

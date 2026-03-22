package com.tattoo.scheduler.mapper;

import com.tattoo.scheduler.domain.Artist;
import com.tattoo.scheduler.model.ArtistEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ArtistMapper {

    @Mapping(target = "bookingEntities", ignore = true)
    ArtistEntity toEntity(Artist artist);
    Artist toDomain(ArtistEntity entity);
}

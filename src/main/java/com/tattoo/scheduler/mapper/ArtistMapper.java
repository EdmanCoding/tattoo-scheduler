package com.tattoo.scheduler.mapper;

import com.tattoo.scheduler.domain.Artist;
import com.tattoo.scheduler.model.ArtistEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ArtistMapper {
    ArtistMapper INSTANCE = Mappers.getMapper(ArtistMapper.class);

    ArtistEntity toEntity(Artist artist);
    Artist toDomain(ArtistEntity entity);
}

package com.tattoo.scheduler.mapper;

import com.tattoo.scheduler.domain.Booking;
import com.tattoo.scheduler.model.BookingEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface BookingMapper {
    BookingMapper INSTANCE = Mappers.getMapper(BookingMapper.class);

    @Mapping(target="user", ignore = true)
    @Mapping(target="artist", ignore = true)
    BookingEntity toEntity(Booking booking);

    @Mapping(target="userId", source="user.id")
    @Mapping(target="artistId", source = "artist.id")
    Booking toDomain(BookingEntity entity);
}

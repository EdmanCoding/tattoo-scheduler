package com.tattoo.scheduler.mapper;

import com.tattoo.scheduler.domain.Booking;
import com.tattoo.scheduler.model.BookingEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    @Mapping(target = "userEntity", ignore = true)
    @Mapping(target = "artistEntity", ignore = true)
    BookingEntity toEntity(Booking booking);

    @Mapping(target = "userId", source = "userEntity.id")
    @Mapping(target = "artistId", source = "artistEntity.id")
    Booking toDomain(BookingEntity entity);
}

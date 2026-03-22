package com.tattoo.scheduler.controller.mapper;

import com.tattoo.scheduler.domain.Booking;
import com.tattoo.scheduler.dto.BookingResponse;
import com.tattoo.scheduler.dto.CreateBookingRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookingDTOMapper {
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "artistId", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "endTime", ignore = true)
    @Mapping(target = "endOfBufferTime", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Booking toDomain(CreateBookingRequest request);

    BookingResponse toResponse(Booking booking);
}

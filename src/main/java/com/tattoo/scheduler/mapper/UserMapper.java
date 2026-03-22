package com.tattoo.scheduler.mapper;

import com.tattoo.scheduler.domain.User;
import com.tattoo.scheduler.model.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "bookingEntities", ignore = true)
    UserEntity ToEntity(User user);
    User toDomain(UserEntity user);
}

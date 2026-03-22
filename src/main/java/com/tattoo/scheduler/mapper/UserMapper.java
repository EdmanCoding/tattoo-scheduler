package com.tattoo.scheduler.mapper;

import com.tattoo.scheduler.domain.User;
import com.tattoo.scheduler.model.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    UserEntity ToEntity(User user);
    User toDomain(UserEntity user);
}

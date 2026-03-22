package com.tattoo.scheduler.service.resolver.impl;

import com.tattoo.scheduler.domain.User;
import com.tattoo.scheduler.mapper.UserMapper;
import com.tattoo.scheduler.model.UserEntity;
import com.tattoo.scheduler.repository.UserRepository;
import com.tattoo.scheduler.service.exception.UserNotFoundException;
import com.tattoo.scheduler.service.resolver.UserResolver;
import org.springframework.stereotype.Component;

@Component
public class DefaultUserResolver implements UserResolver {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public DefaultUserResolver(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    public User getUser(Long userId){
        UserEntity entity = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        return userMapper.toDomain(entity);
    }
}

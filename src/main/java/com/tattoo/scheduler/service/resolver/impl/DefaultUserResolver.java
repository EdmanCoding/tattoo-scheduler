package com.tattoo.scheduler.service.resolver.impl;

import com.tattoo.scheduler.model.User;
import com.tattoo.scheduler.repository.UserRepository;
import com.tattoo.scheduler.service.exception.UserNotFoundException;
import com.tattoo.scheduler.service.resolver.UserResolver;
import org.springframework.stereotype.Component;

@Component
public class DefaultUserResolver implements UserResolver {
    private final UserRepository userRepository;

    public DefaultUserResolver(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User getUser(Long userId){
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

}

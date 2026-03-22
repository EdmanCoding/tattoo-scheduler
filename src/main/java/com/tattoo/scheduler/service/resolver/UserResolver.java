package com.tattoo.scheduler.service.resolver;

import com.tattoo.scheduler.domain.User;

public interface UserResolver {
    User getUser(Long userId);
}

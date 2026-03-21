package com.tattoo.scheduler.service.resolver;

import com.tattoo.scheduler.model.User;

public interface UserResolver {
    User getUser(Long userId);
}

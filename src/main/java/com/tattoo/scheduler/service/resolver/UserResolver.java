package com.tattoo.scheduler.service.resolver;

import com.tattoo.scheduler.domain.User;

/**
 * Resolves user entities by ID.
 * <p>
 * Reserved for future features like user profile management.
 */
public interface UserResolver {
    /**
     * Retrieves a user by ID.
     *
     * @param userId the user ID
     * @return the user domain object
     * @throws com.tattoo.scheduler.service.exception.UserNotFoundException if not found
     */
    User getUser(Long userId);
}

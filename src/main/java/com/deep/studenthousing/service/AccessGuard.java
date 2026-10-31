package com.deep.studenthousing.service;

import com.deep.studenthousing.entity.Role;
import com.deep.studenthousing.entity.User;
import com.deep.studenthousing.exception.UnauthorizedActionException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Central place for "is the logged-in user really allowed to touch this?" checks.
 *
 * The URLs carry an {ownerId}, but a URL is user-controlled input: it must never
 * be trusted as proof of who the caller is. Every owner-side endpoint therefore
 * compares that id with the account that is actually logged in.
 */
@Component
public class AccessGuard {

    private final UserService userService;

    public AccessGuard(UserService userService) {
        this.userService = userService;
    }

    /** The logged-in user, or null when nobody is logged in. */
    public User currentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return userService.findByEmail(authentication.getName());
    }

    /** Caller must be an OWNER and must be the owner identified by {@code ownerId}. */
    public User requireSelfOwner(Long ownerId, Authentication authentication) {
        User me = currentUser(authentication);
        if (me == null || me.getRole() != Role.OWNER || !me.getId().equals(ownerId)) {
            throw new UnauthorizedActionException("You can only manage your own account and listings.");
        }
        return me;
    }

    /** Caller must be the user identified by {@code userId}, or an admin. */
    public User requireSelfOrAdmin(Long userId, Authentication authentication) {
        User me = currentUser(authentication);
        if (me == null || (me.getRole() != Role.ADMIN && !me.getId().equals(userId))) {
            throw new UnauthorizedActionException("You can only view your own profile.");
        }
        return me;
    }
}

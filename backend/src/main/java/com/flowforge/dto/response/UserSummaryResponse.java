package com.flowforge.dto.response;

import com.flowforge.entity.User;

import java.io.Serializable;

public record UserSummaryResponse(Long id, String username, String email, String role) implements Serializable {
    public static UserSummaryResponse from(User user) {
        return new UserSummaryResponse(user.getId(), user.getUsername(), user.getEmail(), user.getRole().name());
    }
}

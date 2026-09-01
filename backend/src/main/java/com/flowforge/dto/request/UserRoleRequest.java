package com.flowforge.dto.request;

import com.flowforge.entity.User;
import jakarta.validation.constraints.NotNull;

public class UserRoleRequest {
    @NotNull(message = "Role is required")
    private User.Role role;

    public User.Role getRole() {
        return role;
    }

    public void setRole(User.Role role) {
        this.role = role;
    }
}

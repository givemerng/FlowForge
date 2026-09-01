package com.flowforge.dto.request;

import jakarta.validation.constraints.NotNull;

public class TaskAssignRequest {
    @NotNull
    private Long userId;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}

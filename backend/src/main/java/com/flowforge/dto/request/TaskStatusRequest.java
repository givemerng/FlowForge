package com.flowforge.dto.request;

import com.flowforge.entity.Task;
import jakarta.validation.constraints.NotNull;

public class TaskStatusRequest {
    @NotNull
    private Task.Status status;

    public Task.Status getStatus() { return status; }
    public void setStatus(Task.Status status) { this.status = status; }
}

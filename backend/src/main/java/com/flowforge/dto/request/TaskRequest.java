package com.flowforge.dto.request;

import com.flowforge.entity.Task;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public class TaskRequest {
    @NotBlank
    private String title;
    private String description;
    private Task.Priority priority = Task.Priority.MEDIUM;
    private Long assignedToId;
    private LocalDateTime deadline;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Task.Priority getPriority() { return priority; }
    public void setPriority(Task.Priority priority) { this.priority = priority; }
    public Long getAssignedToId() { return assignedToId; }
    public void setAssignedToId(Long assignedToId) { this.assignedToId = assignedToId; }
    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }
}

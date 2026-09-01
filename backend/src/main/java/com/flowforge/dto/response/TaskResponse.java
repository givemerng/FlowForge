package com.flowforge.dto.response;

import com.flowforge.entity.Task;

import java.io.Serializable;
import java.time.LocalDateTime;

public record TaskResponse(Long id, String title, String description, String status, String priority, Long projectId, UserSummaryResponse assignedTo, LocalDateTime deadline, LocalDateTime createdAt, LocalDateTime updatedAt) implements Serializable {
    public static TaskResponse from(Task task) {
        UserSummaryResponse assignee = task.getAssignedTo() == null ? null : UserSummaryResponse.from(task.getAssignedTo());
        return new TaskResponse(task.getId(), task.getTitle(), task.getDescription(), task.getStatus().name(), task.getPriority().name(), task.getProject().getId(), assignee, task.getDeadline(), task.getCreatedAt(), task.getUpdatedAt());
    }
}

package com.flowforge.dto.response;

import com.flowforge.entity.Task;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record TaskResponse(Long id, String title, String description, String status, String priority, Long projectId, UserSummaryResponse assignedTo, LocalDateTime deadline, LocalDateTime createdAt, LocalDateTime updatedAt, List<LabelResponse> labels) implements Serializable {
    public static TaskResponse from(Task task) {
        UserSummaryResponse assignee = task.getAssignedTo() == null ? null : UserSummaryResponse.from(task.getAssignedTo());
        List<LabelResponse> labels = task.getTaskLabels() != null 
            ? task.getTaskLabels().stream().map(tl -> LabelResponse.from(tl.getLabel())).collect(Collectors.toList())
            : List.of();
        return new TaskResponse(task.getId(), task.getTitle(), task.getDescription(), task.getStatus().name(), task.getPriority().name(), task.getProject().getId(), assignee, task.getDeadline(), task.getCreatedAt(), task.getUpdatedAt(), labels);
    }
}

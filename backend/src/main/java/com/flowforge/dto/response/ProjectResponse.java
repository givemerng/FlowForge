package com.flowforge.dto.response;

import com.flowforge.entity.Project;

import java.io.Serializable;
import java.time.LocalDateTime;

public record ProjectResponse(Long id, String name, String description, UserSummaryResponse owner, LocalDateTime createdAt, LocalDateTime updatedAt) implements Serializable {
    public static ProjectResponse from(Project project) {
        UserSummaryResponse owner = project.getOwner() == null ? null : UserSummaryResponse.from(project.getOwner());
        return new ProjectResponse(project.getId(), project.getName(), project.getDescription(), owner, project.getCreatedAt(), project.getUpdatedAt());
    }
}

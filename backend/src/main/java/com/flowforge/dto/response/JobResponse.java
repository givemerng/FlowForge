package com.flowforge.dto.response;

import com.flowforge.entity.Job;

import java.io.Serializable;
import java.time.LocalDateTime;

public record JobResponse(Long id, String type, String status, int attemptCount, String lastError, LocalDateTime createdAt, LocalDateTime updatedAt) implements Serializable {
    public static JobResponse from(Job job) {
        return new JobResponse(job.getId(), job.getType(), job.getStatus().name(), job.getAttemptCount(), job.getLastError(), job.getCreatedAt(), job.getUpdatedAt());
    }
}

package com.flowforge.dto.response;

import com.flowforge.entity.Report;

import java.io.Serializable;
import java.time.LocalDateTime;

public record ReportResponse(
        Long id,
        ProjectResponse project,
        UserSummaryResponse createdBy,
        JobResponse job,
        String type,
        String content,
        LocalDateTime createdAt
) implements Serializable {
    public static ReportResponse from(Report report) {
        return new ReportResponse(
                report.getId(),
                ProjectResponse.from(report.getProject()),
                UserSummaryResponse.from(report.getCreatedBy()),
                report.getJob() == null ? null : JobResponse.from(report.getJob()),
                report.getType(),
                report.getContent(),
                report.getCreatedAt()
        );
    }
}

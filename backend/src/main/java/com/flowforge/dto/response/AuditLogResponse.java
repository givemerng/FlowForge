package com.flowforge.dto.response;

import com.flowforge.entity.AuditLog;

import java.time.LocalDateTime;

public record AuditLogResponse(
        Long id,
        UserSummaryResponse user,
        String action,
        String resource,
        Long resourceId,
        String metadata,
        LocalDateTime createdAt
) {
    public static AuditLogResponse from(AuditLog auditLog) {
        return new AuditLogResponse(
                auditLog.getId(),
                auditLog.getUser() == null ? null : UserSummaryResponse.from(auditLog.getUser()),
                auditLog.getAction(),
                auditLog.getResource(),
                auditLog.getResourceId(),
                auditLog.getMetadata(),
                auditLog.getCreatedAt()
        );
    }
}

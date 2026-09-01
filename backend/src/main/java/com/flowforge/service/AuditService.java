package com.flowforge.service;

import com.flowforge.entity.AuditLog;
import com.flowforge.entity.User;
import com.flowforge.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

@Service
public class AuditService {
    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void record(User user, String action, String resource, Long resourceId, String metadata) {
        AuditLog auditLog = new AuditLog();
        auditLog.setUser(user);
        auditLog.setAction(action);
        auditLog.setResource(resource);
        auditLog.setResourceId(resourceId);
        auditLog.setMetadata(metadata);
        auditLogRepository.save(auditLog);
    }
}

package com.flowforge.controller;

import com.flowforge.dto.response.AuditLogResponse;
import com.flowforge.repository.AuditLogRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
@Tag(name = "AuditLog", description = "API endpoints for AuditLog")
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;

    public AuditLogController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get Audit Logs")
    public ResponseEntity<List<AuditLogResponse>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        PageRequest pageRequest = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 200));
        return ResponseEntity.ok(auditLogRepository.findAllByOrderByCreatedAtDesc(pageRequest).stream()
                .map(AuditLogResponse::from)
                .toList());
    }
}

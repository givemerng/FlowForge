package com.flowforge.repository;

import com.flowforge.entity.AuditLog;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    @EntityGraph(attributePaths = "user")
    java.util.List<AuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
}

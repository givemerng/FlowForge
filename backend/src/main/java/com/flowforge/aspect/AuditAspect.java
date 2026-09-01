package com.flowforge.aspect;

import com.flowforge.entity.User;
import com.flowforge.service.AuditService;
import com.flowforge.service.CurrentUserService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AuditAspect {

    private final AuditService auditService;
    private final CurrentUserService currentUserService;

    public AuditAspect(AuditService auditService, CurrentUserService currentUserService) {
        this.auditService = auditService;
        this.currentUserService = currentUserService;
    }

    @Around("@annotation(auditable)")
    public Object auditAction(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        Object result = joinPoint.proceed();

        try {
            User currentUser = currentUserService.requireCurrentUser();
            Long resourceId = null;

            // Attempt to extract resourceId from ResponseEntity body if possible
            if (result instanceof ResponseEntity<?> response) {
                Object body = response.getBody();
                if (body != null) {
                    try {
                        // Use reflection to try to get ID
                        java.lang.reflect.Method getIdMethod = body.getClass().getMethod("getId");
                        Object idObj = getIdMethod.invoke(body);
                        if (idObj instanceof Long id) {
                            resourceId = id;
                        }
                    } catch (Exception e) {
                        // Ignore if no getId method
                    }
                }
            }

            auditService.record(
                    currentUser,
                    auditable.action(),
                    auditable.resource(),
                    resourceId,
                    "Method: " + joinPoint.getSignature().getName()
            );
        } catch (Exception e) {
            // Log audit failure but do not break the transaction
            System.err.println("Failed to create audit log: " + e.getMessage());
        }

        return result;
    }
}

package com.flowforge.controller;

import com.flowforge.entity.Notification;
import com.flowforge.dto.response.NotificationResponse;
import com.flowforge.repository.NotificationRepository;
import com.flowforge.security.UserDetailsImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notification", description = "API endpoints for Notification")
public class NotificationController {

    private final NotificationRepository notificationRepository;

    public NotificationController(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @GetMapping
    @Operation(summary = "Get My Notifications")
    public ResponseEntity<List<NotificationResponse>> getMyNotifications(@AuthenticationPrincipal UserDetailsImpl principal) {
        List<NotificationResponse> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(principal.getId())
                .stream()
                .map(NotificationResponse::from)
                .toList();
        return ResponseEntity.ok(notifications);
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Mark Read")
    public ResponseEntity<?> markRead(@PathVariable Long id, @AuthenticationPrincipal UserDetailsImpl principal) {
        return notificationRepository.findById(id).map(n -> {
            if (!n.getUser().getId().equals(principal.getId())) {
                return ResponseEntity.status(403).build();
            }
            n.setRead(true);
            notificationRepository.save(n);
            return ResponseEntity.ok(Map.of("message", "Marked as read"));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/read-all")
    @Operation(summary = "Mark All Read")
    public ResponseEntity<?> markAllRead(@AuthenticationPrincipal UserDetailsImpl principal) {
        List<Notification> unread = notificationRepository.findByUserIdAndReadFalse(principal.getId());
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
        return ResponseEntity.ok(Map.of("message", "All notifications marked as read", "count", unread.size()));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get Unread Count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@AuthenticationPrincipal UserDetailsImpl principal) {
        long count = notificationRepository.countByUserIdAndReadFalse(principal.getId());
        return ResponseEntity.ok(Map.of("count", count));
    }
}

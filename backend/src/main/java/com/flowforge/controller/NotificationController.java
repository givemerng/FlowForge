package com.flowforge.controller;

import com.flowforge.entity.Notification;
import com.flowforge.entity.User;
import com.flowforge.repository.NotificationRepository;
import com.flowforge.security.UserDetailsImpl;
import com.flowforge.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationController(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<Notification>> getMyNotifications(@AuthenticationPrincipal UserDetailsImpl principal) {
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(principal.getId());
        return ResponseEntity.ok(notifications);
    }

    @PutMapping("/{id}/read")
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
    public ResponseEntity<?> markAllRead(@AuthenticationPrincipal UserDetailsImpl principal) {
        List<Notification> unread = notificationRepository.findByUserIdAndReadFalse(principal.getId());
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
        return ResponseEntity.ok(Map.of("message", "All notifications marked as read", "count", unread.size()));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@AuthenticationPrincipal UserDetailsImpl principal) {
        long count = notificationRepository.countByUserIdAndReadFalse(principal.getId());
        return ResponseEntity.ok(Map.of("count", count));
    }
}

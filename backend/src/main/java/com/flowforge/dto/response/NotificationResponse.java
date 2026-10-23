package com.flowforge.dto.response;

import com.flowforge.entity.Notification;

import java.time.LocalDateTime;

public record NotificationResponse(Long id, String title, String body, boolean read, LocalDateTime createdAt) {
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getTitle(),
                notification.getBody(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}

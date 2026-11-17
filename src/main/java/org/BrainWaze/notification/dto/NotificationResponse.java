package org.BrainWaze.notification.dto;


import org.BrainWaze.notification.entity.Notification;
import org.BrainWaze.notification.entity.NotificationType;
import org.BrainWaze.notification.entity.RecipientRole;

import java.time.Instant;

public record NotificationResponse(
        Long id,
        RecipientRole recipientRole,
        NotificationType type,
        String title,
        String message,
        String referenceId,
        String referenceType,
        String link,
        boolean read,
        Instant createdAt,
        Instant readAt
) {

    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getRecipientRole(),
                notification.getType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getReferenceId(),
                notification.getReferenceType(),
                notification.getLink(),
                notification.isRead(),
                notification.getCreatedAt(),
                notification.getReadAt()
        );
    }
}
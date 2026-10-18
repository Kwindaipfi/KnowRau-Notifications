package org.BrainWaze.notification.dto;

import org.BrainWaze.notification.domain.Notification;
import org.BrainWaze.notification.domain.NotificationType;
import org.BrainWaze.notification.domain.RecipientRole;

import java.time.Instant;

/**
 * Public view of a notification returned to the frontend.
 */
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

    public static NotificationResponse from(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getRecipientRole(),
                n.getType(),
                n.getTitle(),
                n.getMessage(),
                n.getReferenceId(),
                n.getReferenceType(),
                n.getLink(),
                n.isRead(),
                n.getCreatedAt(),
                n.getReadAt()
        );
    }
}

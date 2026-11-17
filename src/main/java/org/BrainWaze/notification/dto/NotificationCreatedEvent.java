package org.BrainWaze.notification.dto;


public record NotificationCreatedEvent(
        String recipientUserId,
        NotificationResponse notification,
        String recipientEmail,
        String recipientPhone
) {
}
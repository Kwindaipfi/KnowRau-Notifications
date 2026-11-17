package org.BrainWaze.notification.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.BrainWaze.notification.channel.NotificationDispatcher;
import org.BrainWaze.notification.dto.NotificationCreatedEvent;
import org.BrainWaze.notification.dto.NotificationResponse;
import org.BrainWaze.notification.entity.Notification;
import org.BrainWaze.notification.repository.NotificationRepository;
import org.BrainWaze.notification.service.NotificationWebSocketService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationDeliveryListener {

    private final NotificationRepository repository;
    private final NotificationDispatcher dispatcher;
    private final NotificationWebSocketService webSocketService;

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handleNotificationCreated(
            NotificationCreatedEvent event
    ) {

        String userId = event.recipientUserId();

        NotificationResponse notification = event.notification();

        log.info(
                "Processing delivery for notification {} and user {}",
                notification.id(),
                userId
        );

        // 1. Real-time WebSocket delivery
        try {

            webSocketService.sendNotification(
                    userId,
                    notification
            );

            long unreadCount = repository
                    .countByRecipientUserIdAndReadFalse(userId);

            webSocketService.sendUnreadCount(
                    userId,
                    unreadCount
            );

        } catch (Exception exception) {

            log.error(
                    "WebSocket delivery failed for notification {}",
                    notification.id(),
                    exception
            );
        }

        // 2. Email and SMS delivery
        try {

            Notification savedNotification = repository
                    .findById(notification.id())
                    .orElseThrow(() ->
                            new IllegalStateException(
                                    "Notification not found: "
                                            + notification.id()
                            )
                    );

            dispatcher.dispatch(
                    savedNotification,
                    event.recipientEmail(),
                    event.recipientPhone()
            );

        } catch (Exception exception) {

            log.error(
                    "External notification dispatch failed for notification {}",
                    notification.id(),
                    exception
            );
        }
    }
}
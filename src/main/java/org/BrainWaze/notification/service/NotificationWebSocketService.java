package org.BrainWaze.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.BrainWaze.notification.dto.NotificationResponse;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendNotification(
            String recipientUserId,
            NotificationResponse notification
    ) {

        try {

            messagingTemplate.convertAndSendToUser(
                    recipientUserId,
                    "/queue/notifications",
                    notification
            );

            log.info(
                    "Notification {} sent to user {}",
                    notification.id(),
                    recipientUserId
            );

        } catch (Exception exception) {

            log.error(
                    "Failed to deliver notification to user {}",
                    recipientUserId,
                    exception
            );
        }
    }

    public void sendUnreadCount(
            String recipientUserId,
            long unreadCount
    ) {

        try {

            messagingTemplate.convertAndSendToUser(
                    recipientUserId,
                    "/queue/unread-count",
                    unreadCount
            );

        } catch (Exception exception) {

            log.error(
                    "Failed to deliver unread count to user {}",
                    recipientUserId,
                    exception
            );
        }
    }
}
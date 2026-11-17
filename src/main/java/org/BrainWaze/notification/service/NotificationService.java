package org.BrainWaze.notification.service;

import lombok.RequiredArgsConstructor;
import org.BrainWaze.notification.dto.CreateNotificationRequest;
import org.BrainWaze.notification.dto.NotificationCreatedEvent;
import org.BrainWaze.notification.dto.NotificationResponse;
import org.BrainWaze.notification.entity.Notification;
import org.BrainWaze.notification.repository.NotificationRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository repository;
    private final ApplicationEventPublisher eventPublisher;
    private final NotificationWebSocketService webSocketService;

    @Transactional
    public NotificationResponse create(CreateNotificationRequest request) {

        var existing = repository
                .findByRecipientUserIdAndTypeAndReferenceTypeAndReferenceId(
                        request.recipientUserId(),
                        request.type(),
                        request.referenceType(),
                        request.referenceId()
                );

        if (existing.isPresent()) {
            return NotificationResponse.from(existing.get());
        }

        Notification notification = Notification.builder()
                .recipientUserId(request.recipientUserId())
                .recipientRole(request.recipientRole())
                .type(request.type())
                .title(request.title())
                .message(request.message())
                .referenceId(request.referenceId())
                .referenceType(request.referenceType())
                .link(request.link())
                .read(false)
                .createdAt(Instant.now())
                .build();

        Notification saved = repository.saveAndFlush(notification);

        NotificationResponse response = NotificationResponse.from(saved);

        eventPublisher.publishEvent(
                new NotificationCreatedEvent(
                        saved.getRecipientUserId(),
                        response,
                        request.recipientEmail(),
                        request.recipientPhone()
                )
        );

        return response;
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> list(
            String userId,
            boolean unreadOnly,
            int page,
            int size
    ) {

        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                clampSize(size),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<Notification> notifications = unreadOnly
                ? repository.findByRecipientUserIdAndReadFalseOrderByCreatedAtDesc(
                userId, pageable)
                : repository.findByRecipientUserIdOrderByCreatedAtDesc(
                userId, pageable);

        return notifications.map(NotificationResponse::from);
    }

    @Transactional(readOnly = true)
    public long unreadCount(String userId) {

        return repository.countByRecipientUserIdAndReadFalse(userId);
    }

    @Transactional
    public NotificationResponse markRead(
            String userId,
            Long notificationId
    ) {

        Notification notification = getOwnedNotification(
                userId,
                notificationId
        );

        if (!notification.isRead()) {

            notification.setRead(true);
            notification.setReadAt(Instant.now());

            repository.save(notification);

            long unreadCount = repository
                    .countByRecipientUserIdAndReadFalse(userId);

            webSocketService.sendUnreadCount(
                    userId,
                    unreadCount
            );
        }

        return NotificationResponse.from(notification);
    }

    @Transactional
    public int markAllRead(String userId) {

        int updated = repository.markAllRead(
                userId,
                Instant.now()
        );

        long unreadCount = repository
                .countByRecipientUserIdAndReadFalse(userId);

        webSocketService.sendUnreadCount(
                userId,
                unreadCount
        );

        return updated;
    }

    @Transactional
    public void deleteNotification(
            String userId,
            Long notificationId
    ) {

        Notification notification = getOwnedNotification(
                userId,
                notificationId
        );

        boolean wasUnread = !notification.isRead();

        repository.delete(notification);

        if (wasUnread) {

            long unreadCount = repository
                    .countByRecipientUserIdAndReadFalse(userId);

            webSocketService.sendUnreadCount(
                    userId,
                    unreadCount
            );
        }
    }

    private Notification getOwnedNotification(
            String userId,
            Long notificationId
    ) {

        Notification notification = repository.findById(notificationId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Notification not found"
                ));

        if (!notification.getRecipientUserId().equals(userId)) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Notification not found"
            );
        }

        return notification;
    }

    private int clampSize(int size) {
        if (size <= 0) {
            return 20;
        }
        return Math.min(size, 100);
    }
}
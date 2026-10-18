package org.BrainWaze.notification.service;

import lombok.RequiredArgsConstructor;
import org.BrainWaze.notification.channel.NotificationDispatcher;
import org.BrainWaze.notification.domain.Notification;
import org.BrainWaze.notification.dto.CreateNotificationRequest;
import org.BrainWaze.notification.dto.NotificationResponse;
import org.BrainWaze.notification.repository.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository repository;
    private final NotificationDispatcher dispatcher;

    /**
     * Create and persist a notification, then fan it out to the recipient's
     * enabled external channels (email/SMS) asynchronously. Called from the
     * internal API by trusted services (matching/booking) when an offer is
     * accepted, a request is selected, etc.
     */
    @Transactional
    public NotificationResponse create(CreateNotificationRequest request) {
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

        Notification saved = repository.save(notification);

        // In-app row is now stored; fan out to email/SMS off the request thread.
        dispatcher.dispatch(saved, request.recipientEmail(), request.recipientPhone());

        return NotificationResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> list(String userId, boolean unreadOnly, int page, int size) {
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                clampSize(size),
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Notification> result = unreadOnly
                ? repository.findByRecipientUserIdAndReadFalseOrderByCreatedAtDesc(userId, pageable)
                : repository.findByRecipientUserIdOrderByCreatedAtDesc(userId, pageable);

        return result.map(NotificationResponse::from);
    }

    @Transactional(readOnly = true)
    public long unreadCount(String userId) {
        return repository.countByRecipientUserIdAndReadFalse(userId);
    }

    /**
     * Mark a single notification read. Enforces ownership: a user can only
     * mark their own notifications.
     */
    @Transactional
    public NotificationResponse markRead(String userId, Long notificationId) {
        Notification notification = repository.findById(notificationId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Notification not found"));

        if (!notification.getRecipientUserId().equals(userId)) {
            // Do not reveal existence of other users' notifications.
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found");
        }

        if (!notification.isRead()) {
            notification.setRead(true);
            notification.setReadAt(Instant.now());
            repository.save(notification);
        }

        return NotificationResponse.from(notification);
    }

    @Transactional
    public int markAllRead(String userId) {
        return repository.markAllRead(userId, Instant.now());
    }

    private int clampSize(int size) {
        if (size <= 0) {
            return 20;
        }
        return Math.min(size, 100);
    }
}

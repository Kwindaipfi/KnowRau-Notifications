package org.BrainWaze.notification.repository;

import org.BrainWaze.notification.entity.Notification;
import org.BrainWaze.notification.entity.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByRecipientUserIdOrderByCreatedAtDesc(
            String recipientUserId,
            Pageable pageable
    );

    Page<Notification> findByRecipientUserIdAndReadFalseOrderByCreatedAtDesc(
            String recipientUserId,
            Pageable pageable
    );

    long countByRecipientUserIdAndReadFalse(String recipientUserId);

    boolean existsByRecipientUserIdAndTypeAndReferenceTypeAndReferenceId(
            String recipientUserId,
            NotificationType type,
            String referenceType,
            String referenceId
    );

    @Modifying
    @Query("""
        UPDATE Notification n
           SET n.read = true,
               n.readAt = :readAt
         WHERE n.recipientUserId = :userId
           AND n.read = false
        """)
    int markAllRead(
            @Param("userId") String userId,
            @Param("readAt") Instant readAt
    );

    Optional<Notification> findByRecipientUserIdAndTypeAndReferenceTypeAndReferenceId(
            String recipientUserId,
            NotificationType type,
            String referenceType,
            String referenceId
    );
}
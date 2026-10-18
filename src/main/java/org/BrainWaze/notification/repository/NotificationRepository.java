package org.BrainWaze.notification.repository;

import org.BrainWaze.notification.domain.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

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

    @Modifying
    @Query("""
            update Notification n
               set n.read = true, n.readAt = :readAt
             where n.recipientUserId = :userId
            and n.read = false
            """)
    int markAllRead(@Param("userId") String userId, @Param("readAt") Instant readAt);
}

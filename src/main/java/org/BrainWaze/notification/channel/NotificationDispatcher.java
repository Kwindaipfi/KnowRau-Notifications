package org.BrainWaze.notification.channel;

import org.BrainWaze.notification.domain.Notification;
import org.BrainWaze.notification.domain.NotificationPreference;
import org.BrainWaze.notification.repository.NotificationPreferenceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Fans a stored notification out to the external channels (email, SMS)
 * according to the recipient's preferences. Runs asynchronously so channel
 * delivery never blocks the create request. The in-app row is already saved
 * by the time this runs.
 */
@Component
public class NotificationDispatcher {

    private static final Logger log = LoggerFactory.getLogger(NotificationDispatcher.class);

    private final EmailNotificationChannel emailChannel;
    private final SmsNotificationChannel smsChannel;
    private final NotificationPreferenceRepository preferenceRepository;

    public NotificationDispatcher(
            EmailNotificationChannel emailChannel,
            SmsNotificationChannel smsChannel,
            NotificationPreferenceRepository preferenceRepository) {
        this.emailChannel = emailChannel;
        this.smsChannel = smsChannel;
        this.preferenceRepository = preferenceRepository;
    }

    /**
     * @param notification the already-persisted notification
     * @param email        recipient email (may be null)
     * @param phone        recipient phone (may be null)
     */
    @Async
    public void dispatch(Notification notification, String email, String phone) {
        NotificationPreference prefs = preferenceRepository
                .findById(notification.getRecipientUserId())
                .orElseGet(() -> NotificationPreference.defaultsFor(notification.getRecipientUserId()));

        try {
            if (prefs.isEmailEnabled()) {
                emailChannel.send(notification, email);
            }
            if (prefs.isSmsEnabled()) {
                smsChannel.send(notification, phone);
            }
        } catch (Exception ex) {
            // Defensive: channels already swallow their own errors, but guard
            // the fan-out loop too so one bad channel never kills the thread.
            log.warn("Dispatch error for notification {}: {}", notification.getId(), ex.getMessage());
        }
    }
}

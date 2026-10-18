package org.BrainWaze.notification.channel;

import org.BrainWaze.notification.domain.Notification;

/**
 * A delivery channel (email, SMS, push, ...). The in-app channel is the DB
 * row itself and is not modeled here. Implementations must be safe to call
 * asynchronously and must never throw out to the caller — they log and
 * swallow their own failures so one channel can't break another.
 */
public interface NotificationChannel {

    /** Human-readable channel name, e.g. "email". */
    String name();

    /** Whether this channel is enabled by configuration. */
    boolean isEnabled();

    /**
     * Attempt delivery. Implementations decide whether they have what they
     * need (e.g. an email address) and no-op otherwise.
     *
     * @param notification the stored notification
     * @param recipientAddress channel-specific address (email or phone), may be null
     */
    void send(Notification notification, String recipientAddress);
}

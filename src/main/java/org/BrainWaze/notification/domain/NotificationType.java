package org.BrainWaze.notification.domain;

/**
 * The kind of event a notification represents.
 * Extend this enum as new notifiable events are added.
 */
public enum NotificationType {

    /** A student's request was selected / matched. */
    REQUEST_SELECTED,

    /** A tutor's offer on a request was accepted. */
    OFFER_ACCEPTED,

    /** A tutor's offer on a request was declined. */
    OFFER_DECLINED,

    /** A new offer was received on a student's request. */
    OFFER_RECEIVED,
    /** A session was booked. */
    SESSION_BOOKED,

    /** A new chat message was received. */
    MESSAGE_RECEIVED,

    /** Generic / system notification. */
    SYSTEM
}

package org.BrainWaze.notification.entity;

public enum NotificationType {

    // Matching
    REQUEST_SELECTED,
    OFFER_RECEIVED,
    OFFER_ACCEPTED,
    OFFER_DECLINED,

    // Sessions
    SESSION_BOOKED,
    SESSION_RESCHEDULED,
    SESSION_CANCELLED,
    SESSION_REMINDER,
    SESSION_COMPLETED,

    // Payments
    PAYMENT_SUCCESSFUL,
    PAYMENT_FAILED,
    PAYMENT_REFUNDED,

    // Communication
    MESSAGE_RECEIVED,

    // Reviews
    REVIEW_RECEIVED,

    // Account
    ACCOUNT_UPDATE,
    SYSTEM
}
CREATE TABLE notification_preferences (
    user_id       VARCHAR(255) NOT NULL PRIMARY KEY,
    email_enabled BOOLEAN      NOT NULL DEFAULT TRUE,
    sms_enabled   BOOLEAN      NOT NULL DEFAULT FALSE
);

package org.BrainWaze.notification.channel;

import org.BrainWaze.notification.config.NotificationChannelProperties;
import org.BrainWaze.notification.entity.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class SmsNotificationChannel implements NotificationChannel {

    private static final Logger log =
            LoggerFactory.getLogger(SmsNotificationChannel.class);

    private final NotificationChannelProperties properties;

    public SmsNotificationChannel(
            NotificationChannelProperties properties) {
        this.properties = properties;
    }

    @Override
    public String name() {
        return "sms";
    }

    @Override
    public boolean isEnabled() {
        return properties.getSms().isEnabled();
    }

    @Override
    public void send(
            Notification notification,
            String recipientAddress) {

        if (!isEnabled()) {
            return;
        }

        if (!StringUtils.hasText(recipientAddress)) {
            throw new IllegalArgumentException(
                    "SMS recipient phone number is missing for notification "
                            + notification.getId()
            );
        }

        throw new UnsupportedOperationException(
                "SMS provider has not been configured yet."
        );
    }
}
package org.BrainWaze.notification.channel;

import org.BrainWaze.notification.config.NotificationChannelProperties;
import org.BrainWaze.notification.domain.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * SMS channel STUB. Logs what it would send. Replace the body of send() with
 * a real provider call (e.g. Twilio) and add the provider SDK + credentials
 * when SMS goes live. Toggle on with NOTIFICATION_SMS_ENABLED=true.
 */
@Component
public class SmsNotificationChannel implements NotificationChannel {

    private static final Logger log = LoggerFactory.getLogger(SmsNotificationChannel.class);

    private final NotificationChannelProperties properties;

    public SmsNotificationChannel(NotificationChannelProperties properties) {
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
    public void send(Notification notification, String recipientAddress) {
        if (!isEnabled()) {
            return;
        }
        if (!StringUtils.hasText(recipientAddress)) {
            log.debug("SMS channel skipped for notification {}: no recipient phone", notification.getId());
            return;
        }

        // TODO: integrate a real SMS provider here (Twilio/Clickatell/etc.).
        // For now this is a stub so the architecture is complete.
        log.info("[SMS STUB] Would send to {}: {} — {}",
                recipientAddress, notification.getTitle(), notification.getMessage());
    }
}

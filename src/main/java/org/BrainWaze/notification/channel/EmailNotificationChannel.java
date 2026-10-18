package org.BrainWaze.notification.channel;

import org.BrainWaze.notification.config.NotificationChannelProperties;
import org.BrainWaze.notification.domain.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Sends notifications by email over SMTP. Disabled automatically when no
 * MAIL_HOST is configured, so the service still runs (in-app only) in dev.
 */
@Component
public class EmailNotificationChannel implements NotificationChannel {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationChannel.class);

    private final JavaMailSender mailSender;
    private final NotificationChannelProperties properties;
    private final String mailHost;

    public EmailNotificationChannel(
            JavaMailSender mailSender,
            NotificationChannelProperties properties,
            @Value("${spring.mail.host:}") String mailHost) {
        this.mailSender = mailSender;
        this.properties = properties;
        this.mailHost = mailHost;
    }

    @Override
    public String name() {
        return "email";
    }

    @Override
    public boolean isEnabled() {
        // Enabled only if configured on AND an SMTP host is present.
        return properties.getEmail().isEnabled() && StringUtils.hasText(mailHost);
    }

    @Override
    public void send(Notification notification, String recipientAddress) {
        if (!isEnabled()) {
            return;
        }
        if (!StringUtils.hasText(recipientAddress)) {
            log.debug("Email channel skipped for notification {}: no recipient email", notification.getId());
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(formatFrom());
            message.setTo(recipientAddress);
            message.setSubject(notification.getTitle());
            message.setText(buildBody(notification));
            mailSender.send(message);
            log.info("Email sent for notification {} to {}", notification.getId(), recipientAddress);
        } catch (Exception ex) {
            // Never let a mail failure bubble up and break other channels.
            log.warn("Failed to send email for notification {}: {}", notification.getId(), ex.getMessage());
        }
    }

    private String formatFrom() {
        NotificationChannelProperties.Email email = properties.getEmail();
        if (StringUtils.hasText(email.getFromName())) {
            return email.getFromName() + " <" + email.getFrom() + ">";
        }
        return email.getFrom();
    }

    private String buildBody(Notification notification) {
        StringBuilder sb = new StringBuilder();
        sb.append(notification.getMessage());
        if (StringUtils.hasText(notification.getLink())) {
            sb.append("\n\nOpen KnowRau: ").append(notification.getLink());
        }
        sb.append("\n\n— KnowRau");
        return sb.toString();
    }
}

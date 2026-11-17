package org.BrainWaze.notification.channel;

import org.BrainWaze.notification.config.NotificationChannelProperties;
import org.BrainWaze.notification.entity.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class EmailNotificationChannel implements NotificationChannel {

    private static final Logger log =
            LoggerFactory.getLogger(EmailNotificationChannel.class);

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
        return properties.getEmail().isEnabled()
                && StringUtils.hasText(mailHost);
    }

    @Override
    public void send(Notification notification, String recipientAddress) {

        if (!isEnabled()) {
            return;
        }

        if (!StringUtils.hasText(recipientAddress)) {
            throw new IllegalArgumentException(
                    "Email recipient address is missing for notification "
                            + notification.getId()
            );
        }

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(formatFrom());
        message.setTo(recipientAddress);
        message.setSubject(notification.getTitle());
        message.setText(buildBody(notification));

        mailSender.send(message);

        log.info(
                "Email successfully sent for notification {}",
                notification.getId()
        );
    }

    private String formatFrom() {

        NotificationChannelProperties.Email email =
                properties.getEmail();

        if (StringUtils.hasText(email.getFromName())) {
            return email.getFromName()
                    + " <"
                    + email.getFrom()
                    + ">";
        }

        return email.getFrom();
    }

    private String buildBody(Notification notification) {

        StringBuilder body = new StringBuilder();

        body.append(notification.getMessage());

        if (StringUtils.hasText(notification.getLink())) {
            body.append("\n\nOpen BrainWaze: ")
                    .append(notification.getLink());
        }

        body.append("\n\n— BrainWaze");

        return body.toString();
    }
}
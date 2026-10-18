package org.BrainWaze.notification.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.BrainWaze.notification.dto.FinalMatchConfirmedEvent;
import org.BrainWaze.notification.service.MatchingNotificationHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MatchingEventConsumer {

    private final MatchingNotificationHandler notificationHandler;

    @KafkaListener(topics = "final-match-confirmed-notification", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(FinalMatchConfirmedEvent event) {
        log.info("Received final match confirmation. Event ID: {}, Booking ID: {}", event.eventId(), event.bookingId());
        notificationHandler.handleFinalMatchConfirmed(event);
    }
}
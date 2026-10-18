package org.BrainWaze.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.BrainWaze.notification.domain.NotificationType;
import org.BrainWaze.notification.domain.RecipientRole;
import org.BrainWaze.notification.dto.CreateNotificationRequest;
import org.BrainWaze.notification.dto.FinalMatchConfirmedEvent;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchingNotificationHandler {

    private final NotificationService notificationService;

    public void handleFinalMatchConfirmed(FinalMatchConfirmedEvent event) {

        log.info("Processing final-match event for booking {}", event.bookingId());

        String lessonDetails = String.format("%s lesson on %s, from %s to %s.", event.subject(), event.lessonDate(), event.startTime(), event.endTime());
        // Notify the student.
        CreateNotificationRequest studentNotification =
                new CreateNotificationRequest(
                        event.studentUserId(), RecipientRole.STUDENT, NotificationType.REQUEST_SELECTED,
                        "Your tutor has been confirmed", "Your tutor has been selected and your booking is confirmed. " + lessonDetails, null, null, event.bookingId().toString(), "BOOKING", "/student/lessons");
        notificationService.create(studentNotification);

        // Notify the selected tutor.
        CreateNotificationRequest tutorNotification =
                new CreateNotificationRequest(
                        event.tutorUserId(), RecipientRole.TUTOR, NotificationType.SESSION_BOOKED, "You have a confirmed lesson", "A student has selected your offer. Your booking is confirmed. " + lessonDetails,
                        null, null, event.bookingId().toString(), "BOOKING", "/tutor/bookings");

        notificationService.create(tutorNotification);

        log.info("Created student and tutor notifications for booking {}", event.bookingId()
        );
    }
}
package org.BrainWaze.notification.event;

import java.time.Instant;

public record TutorOfferAcceptedEvent(
        String eventId,
        Long offerId,
        String studentId,
        String studentName,
        String studentEmail,
        String tutorId,
        String tutorName,
        String tutorEmail,
        String subject,
        Instant occurredAt
) {
}
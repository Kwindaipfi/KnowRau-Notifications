package org.BrainWaze.notification.event;

import java.time.Instant;

public record LessonScheduledEvent(
        String eventId,
        Long lessonId,
        String studentId,
        String studentName,
        String studentEmail,
        String tutorId,
        String tutorName,
        String tutorEmail,
        String subject,
        Instant scheduledAt,
        String timeZone,
        Instant occurredAt
) {
}
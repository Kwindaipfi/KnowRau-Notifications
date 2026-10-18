package org.BrainWaze.notification.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

public record FinalMatchConfirmedEvent(
        String eventId,
        Long bookingId,
        Long requestId,
        String studentUserId,
        String tutorUserId,
        String subject,
        LocalDate lessonDate,
        LocalTime startTime,
        LocalTime endTime,
        String bookingStatus,
        LocalDateTime occurredAt
) {
}
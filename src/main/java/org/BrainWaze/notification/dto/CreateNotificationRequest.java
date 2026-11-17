package org.BrainWaze.notification.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.BrainWaze.notification.entity.NotificationType;
import org.BrainWaze.notification.entity.RecipientRole;

public record CreateNotificationRequest(

        @NotBlank
        String recipientUserId,

        @NotNull
        RecipientRole recipientRole,

        @NotNull
        NotificationType type,

        @NotBlank
        @Size(max = 160)
        String title,

        @NotBlank
        @Size(max = 1000)
        String message,

        @Email
        @Size(max = 255)
        String recipientEmail,

        @Size(max = 32)
        String recipientPhone,

        @Size(max = 255)
        String referenceId,

        @Size(max = 40)
        String referenceType,

        @Size(max = 255)
        String link
) {
}
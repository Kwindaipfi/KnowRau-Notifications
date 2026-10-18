package org.BrainWaze.notification.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.BrainWaze.notification.domain.NotificationType;
import org.BrainWaze.notification.domain.RecipientRole;

/**
 * Payload trusted backend services send to the internal API to create a
 * notification for a user.
 */
public record CreateNotificationRequest(

                @NotBlank String recipientUserId,

                @NotNull RecipientRole recipientRole,

                @NotNull NotificationType type,

                @NotBlank @Size(max = 160) String title,

                @NotBlank @Size(max = 1000) String message,

                /** Optional recipient email; if present the email channel may send to it. */
                @Email @Size(max = 255) String recipientEmail,

                /**
                 * Optional recipient phone (E.164); if present the SMS channel may send to it.
                 */
                @Size(max = 32) String recipientPhone,

                @Size(max = 255) String referenceId,

                @Size(max = 40) String referenceType,

                @Size(max = 255) String link) {
}

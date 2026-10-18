package org.BrainWaze.notification.dto;

import jakarta.validation.constraints.NotNull;

public record PreferenceRequest(
        @NotNull Boolean emailEnabled,
        @NotNull Boolean smsEnabled
) {
}

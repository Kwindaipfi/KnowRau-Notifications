package org.BrainWaze.notification.dto;

import org.BrainWaze.notification.entity.NotificationPreference;

public record PreferenceResponse(
        boolean emailEnabled,
        boolean smsEnabled
) {
    public static PreferenceResponse from(NotificationPreference p) {
        return new PreferenceResponse(p.isEmailEnabled(), p.isSmsEnabled());
    }
}

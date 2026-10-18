package org.BrainWaze.notification.repository;

import org.BrainWaze.notification.domain.NotificationPreference;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationPreferenceRepository
        extends JpaRepository<NotificationPreference, String> {
}

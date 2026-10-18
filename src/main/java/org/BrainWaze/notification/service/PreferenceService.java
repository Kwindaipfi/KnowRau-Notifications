package org.BrainWaze.notification.service;

import lombok.RequiredArgsConstructor;
import org.BrainWaze.notification.domain.NotificationPreference;
import org.BrainWaze.notification.dto.PreferenceRequest;
import org.BrainWaze.notification.dto.PreferenceResponse;
import org.BrainWaze.notification.repository.NotificationPreferenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PreferenceService {

    private final NotificationPreferenceRepository repository;

    @Transactional(readOnly = true)
    public PreferenceResponse get(String userId) {
        return PreferenceResponse.from(
                repository.findById(userId)
                        .orElseGet(() -> NotificationPreference.defaultsFor(userId)));
    }

    @Transactional
    public PreferenceResponse update(String userId, PreferenceRequest request) {
        NotificationPreference prefs = repository.findById(userId)
                .orElseGet(() -> NotificationPreference.defaultsFor(userId));
        prefs.setEmailEnabled(Boolean.TRUE.equals(request.emailEnabled()));
        prefs.setSmsEnabled(Boolean.TRUE.equals(request.smsEnabled()));
        return PreferenceResponse.from(repository.save(prefs));
    }
}

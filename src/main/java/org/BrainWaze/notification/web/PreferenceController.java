package org.BrainWaze.notification.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.BrainWaze.notification.dto.PreferenceRequest;
import org.BrainWaze.notification.dto.PreferenceResponse;
import org.BrainWaze.notification.service.PreferenceService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Lets an authenticated user read and update their own channel preferences
 * (email / SMS toggles). In-app notifications are always on.
 */
@RestController
@RequestMapping("/api/notifications/preferences")
@RequiredArgsConstructor
public class PreferenceController {

    private final PreferenceService service;

    @GetMapping
    public PreferenceResponse get(Authentication authentication) {
        return service.get(authentication.getName());
    }

    @PutMapping
    public PreferenceResponse update(
            Authentication authentication,
            @Valid @RequestBody PreferenceRequest request
    ) {
        return service.update(authentication.getName(), request);
    }
}

package org.BrainWaze.notification.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.BrainWaze.notification.dto.CreateNotificationRequest;
import org.BrainWaze.notification.dto.NotificationResponse;
import org.BrainWaze.notification.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Internal API for trusted backend services (matching, booking, chat).
 * Secured by the service token (ROLE_NOTIFICATION_SERVICE). Services call
 * this when, e.g., an offer is accepted or a student request is selected.
 */
@RestController
@RequestMapping("/api/notifications/internal")
@RequiredArgsConstructor
public class InternalNotificationController {

    private final NotificationService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NotificationResponse create(@Valid @RequestBody CreateNotificationRequest request) {
        return service.create(request);
    }
}

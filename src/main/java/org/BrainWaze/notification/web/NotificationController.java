package org.BrainWaze.notification.web;

import lombok.RequiredArgsConstructor;
import org.BrainWaze.notification.dto.NotificationResponse;
import org.BrainWaze.notification.dto.UnreadCountResponse;
import org.BrainWaze.notification.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Public API consumed by the Angular app. The authenticated user is derived
 * from the JWT; a user can only see and mutate their own notifications.
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService service;

    @GetMapping
    public Page<NotificationResponse> list(
            Authentication authentication,
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return service.list(currentUserId(authentication), unreadOnly, page, size);
    }

    @GetMapping("/unread-count")
    public UnreadCountResponse unreadCount(Authentication authentication) {
        return new UnreadCountResponse(service.unreadCount(currentUserId(authentication)));
    }

    @PatchMapping("/{id}/read")
    public NotificationResponse markRead(
            Authentication authentication,
            @PathVariable Long id
    ) {
        return service.markRead(currentUserId(authentication), id);
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Map<String, Integer>> markAllRead(Authentication authentication) {
        int updated = service.markAllRead(currentUserId(authentication));
        return ResponseEntity.ok(Map.of("updated", updated));
    }

    private String currentUserId(Authentication authentication) {
        return authentication.getName();
    }
}

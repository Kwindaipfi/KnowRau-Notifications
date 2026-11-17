package org.BrainWaze.notification.controller;

import lombok.RequiredArgsConstructor;
import org.BrainWaze.notification.dto.NotificationResponse;
import org.BrainWaze.notification.dto.UnreadCountResponse;
import org.BrainWaze.notification.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
            @RequestParam(defaultValue = "20") int size) {

        return service.list(currentUserId(authentication), unreadOnly, page, size);
    }

    @GetMapping("/unread-count")
    public UnreadCountResponse unreadCount(Authentication authentication) {
        return new UnreadCountResponse(service.unreadCount(currentUserId(authentication)));
    }

    @PatchMapping("/{id}/read")
    public NotificationResponse markRead(Authentication authentication, @PathVariable Long id) {
        return service.markRead(currentUserId(authentication), id);
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Map<String, Integer>> markAllRead(
            Authentication authentication
    ) {

        int updated = service.markAllRead(
                currentUserId(authentication)
        );

        return ResponseEntity.ok(
                Map.of("updated", updated)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(
            Authentication authentication,
            @PathVariable Long id
    ) {

        service.deleteNotification(
                currentUserId(authentication),
                id
        );

        return ResponseEntity.noContent().build();
    }

    private String currentUserId(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Unauthenticated user");
        }

        return authentication.getName();
    }
}
package org.BrainWaze.notification.security;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;

    private static final List<String> ALLOWED_DESTINATIONS = List.of(
            "/user/queue/notifications",
            "/user/queue/unread-count"
    );

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        StompCommand command = accessor.getCommand();

        if (StompCommand.CONNECT.equals(command)) {
            authenticate(accessor);
        }

        if (StompCommand.SUBSCRIBE.equals(command)) {

            if (accessor.getUser() == null) {
                throw new IllegalArgumentException(
                        "Unauthenticated WebSocket subscription");
            }

            String destination = accessor.getDestination();

            if (!ALLOWED_DESTINATIONS.contains(destination)) {
                throw new IllegalArgumentException("Subscription to destination not allowed");
            }
        }

        // This service only pushes notifications.
        // Clients cannot publish arbitrary STOMP messages.
        if (StompCommand.SEND.equals(command)) {

            throw new IllegalArgumentException(
                    "Client message publishing is disabled"
            );
        }

        return message;
    }

    private void authenticate(StompHeaderAccessor accessor) {

        String authorization = accessor.getFirstNativeHeader(
                "Authorization"
        );

        if (!StringUtils.hasText(authorization) || !authorization.startsWith("Bearer ")) {

            throw new IllegalArgumentException("Missing or invalid WebSocket authorization header");
        }

        String token = authorization.substring(7);

        try {

            Claims claims = jwtService.parse(token);

            String userId = jwtService.extractUserId(claims);

            String role = jwtService.extractRole(claims);

            if (!StringUtils.hasText(userId)) {

                throw new IllegalArgumentException(
                        "Invalid user identity"
                );
            }

            List<SimpleGrantedAuthority> authorities =
                    new java.util.ArrayList<>();

            authorities.add(
                    new SimpleGrantedAuthority("ROLE_USER")
            );

            if (StringUtils.hasText(role)) {

                authorities.add(
                        new SimpleGrantedAuthority(
                                "ROLE_" + role
                        )
                );
            }

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userId,
                            null,
                            authorities
                    );

            accessor.setUser(authentication);

            log.info(
                    "WebSocket authenticated for user {}",
                    userId
            );

        } catch (Exception exception) {

            log.warn(
                    "WebSocket authentication failed: {}",
                    exception.getMessage()
            );

            throw new IllegalArgumentException(
                    "WebSocket authentication failed"
            );
        }
    }
}
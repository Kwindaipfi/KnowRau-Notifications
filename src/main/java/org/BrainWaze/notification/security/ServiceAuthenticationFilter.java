package org.BrainWaze.notification.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Authenticates trusted backend services via a shared service token header,
 * mirroring the DocumentService pattern. A valid token grants
 * ROLE_NOTIFICATION_SERVICE, which is required to call the internal API.
 */
@Component
public class ServiceAuthenticationFilter extends OncePerRequestFilter {

    public static final String SERVICE_TOKEN_HEADER = "X-Service-Token";
    public static final String SERVICE_AUTHORITY = "ROLE_NOTIFICATION_SERVICE";

    private final String serviceToken;

    public ServiceAuthenticationFilter(
            @Value("${security.service.token:}") String serviceToken) {
        this.serviceToken = serviceToken;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String token = request.getHeader(SERVICE_TOKEN_HEADER);

        if (StringUtils.hasText(serviceToken)
                && StringUtils.hasText(token)
                && constantTimeEquals(serviceToken, token)) {

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            "notification-service-caller",
                            null,
                            List.of(new SimpleGrantedAuthority(SERVICE_AUTHORITY)));

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    /** Length-constant comparison to avoid timing side channels on the token. */
    private boolean constantTimeEquals(String expected, String provided) {
        byte[] a = expected.getBytes();
        byte[] b = provided.getBytes();
        if (a.length != b.length) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length; i++) {
            result |= a[i] ^ b[i];
        }
        return result == 0;
    }
}

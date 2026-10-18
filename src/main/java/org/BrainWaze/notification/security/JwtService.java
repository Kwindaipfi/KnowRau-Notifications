package org.BrainWaze.notification.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * Parses and validates JWTs issued by the auth service.
 * The signing secret must match the auth service (JWT_SECRET env var).
 */
@Component
public class JwtService {

    private final SecretKey key;

    public JwtService(@Value("${security.jwt.secret:}") String secret) {
        if (!StringUtils.hasText(secret)) {
            // Fail fast with a clear message rather than a cryptic key error later.
            throw new IllegalStateException(
                    "security.jwt.secret (JWT_SECRET) is not configured. "
                            + "Set it to the same secret the auth service signs tokens with.");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extract the user id from a validated token. Prefer a "userId" claim,
     * fall back to the subject.
     */
    public String extractUserId(Claims claims) {
        Object userId = claims.get("userId");
        if (userId != null) {
            return String.valueOf(userId);
        }
        return claims.getSubject();
    }

    public String extractRole(Claims claims) {
        Object role = claims.get("role");
        return role != null ? String.valueOf(role) : null;
    }
}

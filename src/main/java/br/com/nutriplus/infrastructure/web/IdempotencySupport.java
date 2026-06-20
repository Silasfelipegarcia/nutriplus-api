package br.com.nutriplus.infrastructure.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Set;

public final class IdempotencySupport {

    public static final String HEADER = "Idempotency-Key";
    public static final String REPLAYED_HEADER = "Idempotency-Replayed";
    public static final String MDC_KEY = "idempotencyKey";

    private static final Set<String> EXCLUDED_AUTH_PATHS = Set.of("/auth/login", "/auth/refresh");
    private static final int MAX_KEY_LENGTH = 128;

    private IdempotencySupport() {
    }

    public static boolean isMutatingMethod(String method) {
        return switch (method) {
            case "POST", "PUT", "PATCH", "DELETE" -> true;
            default -> false;
        };
    }

    public static boolean isExcludedPath(String path) {
        if (path.startsWith("/health")
                || path.startsWith("/actuator/")
                || path.startsWith("/webhooks/")) {
            return true;
        }
        return EXCLUDED_AUTH_PATHS.contains(path);
    }

    public static boolean isValidKey(String key) {
        return key != null && !key.isBlank() && key.length() <= MAX_KEY_LENGTH;
    }

    public static long resolveScopeUserId(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
            String sub = jwt.getSubject();
            if (sub != null && !sub.isBlank()) {
                try {
                    return Long.parseLong(sub);
                } catch (NumberFormatException ignored) {
                    return Math.abs(sub.hashCode());
                }
            }
        }
        String remote = request.getRemoteAddr();
        if (remote == null || remote.isBlank()) {
            remote = "unknown";
        }
        return Math.abs((long) remote.hashCode());
    }

    public static String hashRequestBody(byte[] body) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(body != null ? body : new byte[0]);
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível", e);
        }
    }

    public static String bodyAsString(byte[] body) {
        if (body == null || body.length == 0) {
            return "";
        }
        return new String(body, StandardCharsets.UTF_8);
    }
}

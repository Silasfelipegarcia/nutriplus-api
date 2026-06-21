package br.com.nutriplus.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public final class ClientIpResolver {

    private ClientIpResolver() {
    }

    public static String resolve(HttpServletRequest request, List<String> trustedProxyIps) {
        String remote = request.getRemoteAddr();
        if (trustedProxyIps != null && trustedProxyIps.contains(remote)) {
            String forwarded = request.getHeader("X-Forwarded-For");
            if (forwarded != null && !forwarded.isBlank()) {
                return forwarded.split(",")[0].trim();
            }
        }
        return remote;
    }
}

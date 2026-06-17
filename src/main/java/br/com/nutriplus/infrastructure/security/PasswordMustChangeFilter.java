package br.com.nutriplus.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Component
public class PasswordMustChangeFilter extends OncePerRequestFilter {

    private static final String CHANGE_PASSWORD_PATH = "/users/me/password";

    private final ObjectMapper objectMapper;

    public PasswordMustChangeFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            if (Boolean.TRUE.equals(jwt.getClaim("passwordMustChange"))) {
                boolean allow = "PUT".equalsIgnoreCase(request.getMethod())
                        && CHANGE_PASSWORD_PATH.equals(request.getRequestURI());
                if (!allow) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.getWriter().write(objectMapper.writeValueAsString(Map.of(
                            "message", "Defina uma nova senha para continuar a usar a aplicação.",
                            "code", "PASSWORD_MUST_CHANGE"
                    )));
                    return;
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}

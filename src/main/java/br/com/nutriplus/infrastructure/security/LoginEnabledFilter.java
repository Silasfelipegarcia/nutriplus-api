package br.com.nutriplus.infrastructure.security;

import br.com.nutriplus.application.auth.LoginAccessPolicy;
import br.com.nutriplus.application.port.UserQueryPort;
import br.com.nutriplus.domain.enums.UserRole;
import br.com.nutriplus.domain.model.User;
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
public class LoginEnabledFilter extends OncePerRequestFilter {

    private final UserQueryPort userQueryPort;
    private final ObjectMapper objectMapper;

    public LoginEnabledFilter(UserQueryPort userQueryPort, ObjectMapper objectMapper) {
        this.userQueryPort = userQueryPort;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            String subject = jwt.getSubject();
            if (subject != null && !subject.isBlank()) {
                Long userId = Long.parseLong(subject);
                User user = userQueryPort.findById(userId).orElse(null);
                if (user != null && user.role() != UserRole.ADMIN && !user.loginEnabled()) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.getWriter().write(objectMapper.writeValueAsString(Map.of(
                            "message", LoginAccessPolicy.PENDING_MESSAGE,
                            "code", "LOGIN_PENDING_APPROVAL"
                    )));
                    return;
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}

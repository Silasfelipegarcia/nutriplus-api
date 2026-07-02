package br.com.nutriplus.infrastructure.security;

import br.com.nutriplus.exception.WebPortalOnlyException;
import br.com.nutriplus.infrastructure.config.CorsProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Component
public class WebPortalClientVerifier {

    public static final String CLIENT_HEADER = "X-Nutri-Client";
    public static final String CLIENT_WEB = "web";

    private static final List<String> DEFAULT_ALLOWED_ORIGIN_PATTERNS = List.of(
            "https://nutriplus-web-ten.vercel.app",
            "https://nutriplus-web.vercel.app",
            "https://nutriplus.com.br",
            "https://www.nutriplus.com.br",
            "https://nutriplus.app.br",
            "https://www.nutriplus.app.br",
            "http://localhost:4200",
            "http://127.0.0.1:4200"
    );

    private final CorsProperties corsProperties;

    public WebPortalClientVerifier(CorsProperties corsProperties) {
        this.corsProperties = corsProperties;
    }

    public void requireWebPortal(HttpServletRequest request) {
        String client = request.getHeader(CLIENT_HEADER);
        if (!CLIENT_WEB.equalsIgnoreCase(client)) {
            throw new WebPortalOnlyException(
                    "Exclusão de conta disponível apenas no portal web (nutriplus.app.br).");
        }

        String origin = request.getHeader("Origin");
        if (StringUtils.hasText(origin)) {
            if (!isAllowedOrigin(origin)) {
                throw new WebPortalOnlyException("Origem não autorizada para exclusão de conta.");
            }
            return;
        }

        String referer = request.getHeader("Referer");
        if (StringUtils.hasText(referer) && isAllowedReferer(referer)) {
            return;
        }

        throw new WebPortalOnlyException(
                "Exclusão de conta disponível apenas no portal web (nutriplus.app.br).");
    }

    private boolean isAllowedOrigin(String origin) {
        return allowedOriginPatterns().stream().anyMatch(pattern -> matchesOriginPattern(origin, pattern));
    }

    private boolean isAllowedReferer(String referer) {
        try {
            String origin = URI.create(referer).getScheme() + "://" + URI.create(referer).getAuthority();
            return isAllowedOrigin(origin);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private List<String> allowedOriginPatterns() {
        List<String> patterns = new ArrayList<>();
        if (corsProperties.allowedOrigins() != null) {
            corsProperties.allowedOrigins().stream()
                    .filter(origin -> origin != null && !origin.isBlank())
                    .forEach(patterns::add);
        }
        if (patterns.isEmpty()) {
            patterns.addAll(DEFAULT_ALLOWED_ORIGIN_PATTERNS);
        }
        patterns.add("https://*.vercel.app");
        return patterns;
    }

    private static boolean matchesOriginPattern(String origin, String pattern) {
        if (pattern.contains("*")) {
            String regex = pattern
                    .replace(".", "\\.")
                    .replace("*", ".*");
            return origin.matches(regex);
        }
        return origin.equalsIgnoreCase(pattern);
    }
}

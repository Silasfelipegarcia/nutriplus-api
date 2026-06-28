package br.com.nutriplus.infrastructure.security;

import br.com.nutriplus.infrastructure.web.CorrelationIdFilter;
import br.com.nutriplus.infrastructure.web.MdcUserFilter;
import br.com.nutriplus.infrastructure.web.RequestPerformanceFilter;
import br.com.nutriplus.infrastructure.config.CorsProperties;
import br.com.nutriplus.infrastructure.config.EmailProperties;
import br.com.nutriplus.infrastructure.config.IdempotencyProperties;
import br.com.nutriplus.infrastructure.web.IdempotencyFilter;
import br.com.nutriplus.infrastructure.web.IdempotencySupport;
import br.com.nutriplus.infrastructure.web.MetricsTokenFilter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import br.com.nutriplus.security.CustomUserDetailsService;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.SecretKey;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableMethodSecurity
@EnableConfigurationProperties({RateLimitProperties.class, CorsProperties.class, IdempotencyProperties.class, EmailProperties.class})
public class SecurityConfig {

    @Bean
    DaoAuthenticationProvider authenticationProvider(CustomUserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    JwtDecoder jwtDecoder(JwtService jwtService) {
        SecretKey key = jwtService.getSecretKey();
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        OAuth2TokenValidator<Jwt> rejectRefresh = jwt -> {
            if ("refresh".equals(jwt.getClaims().get("typ"))) {
                return OAuth2TokenValidatorResult.failure(new OAuth2Error(
                        "invalid_token", "Refresh token não pode ser usado como access token.", null));
            }
            return OAuth2TokenValidatorResult.success();
        };
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefault(), rejectRefresh));
        return decoder;
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            List<String> roles = jwt.getClaimAsStringList("roles");
            if (roles == null) {
                return List.of();
            }
            return roles.stream()
                    .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                    .map(SimpleGrantedAuthority::new)
                    .map(GrantedAuthority.class::cast)
                    .collect(Collectors.toList());
        });
        return converter;
    }

    @Bean
    @Order(1)
    SecurityFilterChain publicChain(
            HttpSecurity http,
            CorrelationIdFilter correlationIdFilter,
            RequestPerformanceFilter requestPerformanceFilter,
            RateLimitFilter rateLimitFilter,
            IdempotencyFilter idempotencyFilter,
            MetricsTokenFilter metricsTokenFilter,
            JsonSecurityHandlers jsonSecurityHandlers
    ) throws Exception {
        http
                .securityMatcher(
                        "/auth/**",
                        "/health",
                        "/legal/**",
                        "/pricing/guidelines",
                        "/nutritionists",
                        "/nutritionists/**",
                        "/training/sports",
                        "/feature-flags",
                        "/plans",
                        "/payments/mercadopago/webhook",
                        "/webhooks/**",
                        "/actuator/health",
                        "/actuator/health/**",
                        "/actuator/info",
                        "/actuator/prometheus",
                        "/actuator/metrics")
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .anyRequest().permitAll())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jsonSecurityHandlers)
                        .accessDeniedHandler(jsonSecurityHandlers))
                .addFilterBefore(correlationIdFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(requestPerformanceFilter, CorrelationIdFilter.class)
                .addFilterAfter(metricsTokenFilter, RequestPerformanceFilter.class)
                .addFilterAfter(idempotencyFilter, RequestPerformanceFilter.class)
                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain protectedChain(
            HttpSecurity http,
            JwtAuthenticationConverter jwtAuthenticationConverter,
            CorrelationIdFilter correlationIdFilter,
            RequestPerformanceFilter requestPerformanceFilter,
            RateLimitFilter rateLimitFilter,
            UserRateLimitFilter userRateLimitFilter,
            RiskEvaluationFilter riskEvaluationFilter,
            MdcUserFilter mdcUserFilter,
            PasswordMustChangeFilter passwordMustChangeFilter,
            LoginEnabledFilter loginEnabledFilter,
            IdempotencyFilter idempotencyFilter,
            JsonSecurityHandlers jsonSecurityHandlers
    ) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jsonSecurityHandlers)
                        .accessDeniedHandler(jsonSecurityHandlers))
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)))
                .addFilterBefore(correlationIdFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(requestPerformanceFilter, CorrelationIdFilter.class)
                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(mdcUserFilter, BearerTokenAuthenticationFilter.class)
                .addFilterAfter(userRateLimitFilter, MdcUserFilter.class)
                .addFilterAfter(passwordMustChangeFilter, BearerTokenAuthenticationFilter.class)
                .addFilterAfter(loginEnabledFilter, PasswordMustChangeFilter.class)
                .addFilterAfter(idempotencyFilter, LoginEnabledFilter.class)
                .addFilterAfter(riskEvaluationFilter, IdempotencyFilter.class);
        return http.build();
    }

    private static final List<String> DEFAULT_PRODUCTION_ORIGIN_PATTERNS = List.of(
            "https://nutriplus-web-ten.vercel.app",
            "https://nutriplus-web.vercel.app",
            "https://nutriplus.com.br",
            "https://www.nutriplus.com.br",
            "https://nutriplus.app.br",
            "https://www.nutriplus.app.br",
            "https://*.vercel.app"
    );

    @Bean
    CorsConfigurationSource corsConfigurationSource(CorsProperties corsProperties) {
        CorsConfiguration configuration = new CorsConfiguration();
        List<String> originPatterns = new ArrayList<>();
        if (corsProperties.allowedOrigins() != null) {
            corsProperties.allowedOrigins().stream()
                    .filter(origin -> origin != null && !origin.isBlank())
                    .forEach(originPatterns::add);
        }
        if (originPatterns.isEmpty()) {
            originPatterns.addAll(DEFAULT_PRODUCTION_ORIGIN_PATTERNS);
        }
        configuration.setAllowedOriginPatterns(originPatterns);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of(
                CorrelationIdFilter.HEADER,
                CorrelationIdFilter.TRACE_HEADER,
                CorrelationIdFilter.FLOW_HEADER,
                IdempotencySupport.REPLAYED_HEADER));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

package br.com.nutriplus.infrastructure.web;

import br.com.nutriplus.application.port.IdempotencyStore;
import br.com.nutriplus.domain.enums.IdempotencyStatus;
import br.com.nutriplus.infrastructure.config.IdempotencyProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class IdempotencyFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(IdempotencyFilter.class);

    private final IdempotencyProperties properties;
    private final IdempotencyStore idempotencyStore;
    private final ObjectMapper objectMapper;

    public IdempotencyFilter(IdempotencyProperties properties,
                             IdempotencyStore idempotencyStore,
                             ObjectMapper objectMapper) {
        this.properties = properties;
        this.idempotencyStore = idempotencyStore;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!properties.enabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        String method = request.getMethod();
        String path = request.getRequestURI();
        if (!IdempotencySupport.isMutatingMethod(method) || IdempotencySupport.isExcludedPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String idempotencyKey = request.getHeader(IdempotencySupport.HEADER);
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            if (properties.requireKey()) {
                writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                        "Idempotency-Key é obrigatório para esta operação.", "IDEMPOTENCY_KEY_REQUIRED");
                return;
            }
            filterChain.doFilter(request, response);
            return;
        }

        idempotencyKey = idempotencyKey.strip();
        if (!IdempotencySupport.isValidKey(idempotencyKey)) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Idempotency-Key inválida.", "IDEMPOTENCY_KEY_INVALID");
            return;
        }

        MDC.put(IdempotencySupport.MDC_KEY, idempotencyKey);
        try {
            processIdempotentRequest(request, response, filterChain, method, path, idempotencyKey);
        } finally {
            MDC.remove(IdempotencySupport.MDC_KEY);
        }
    }

    private void processIdempotentRequest(HttpServletRequest request,
                                          HttpServletResponse response,
                                          FilterChain filterChain,
                                          String method,
                                          String path,
                                          String idempotencyKey) throws ServletException, IOException {
        long scopeUserId = IdempotencySupport.resolveScopeUserId(request);
        ContentCachingRequestWrapper cachedRequest = new ContentCachingRequestWrapper(request);
        byte[] requestBody = StreamUtils.copyToByteArray(cachedRequest.getInputStream());
        String requestHash = IdempotencySupport.hashRequestBody(requestBody);

        Optional<IdempotencyStore.StoredRecord> existing = idempotencyStore.find(
                idempotencyKey, scopeUserId, method, path);

        if (existing.isPresent()) {
            IdempotencyStore.StoredRecord stored = existing.get();
            if (!stored.requestHash().equals(requestHash)) {
                writeError(response, 422,
                        "Idempotency-Key reutilizada com corpo de requisição diferente.",
                        "IDEMPOTENCY_KEY_BODY_MISMATCH");
                return;
            }
            if (stored.status() == IdempotencyStatus.COMPLETED) {
                log.info("IDEMPOTENCY_REPLAY key={} path={}", idempotencyKey, path);
                replayResponse(response, stored);
                return;
            }
            if (stored.status() == IdempotencyStatus.IN_PROGRESS && !isStale(stored)) {
                log.info("IDEMPOTENCY_CONFLICT key={} path={}", idempotencyKey, path);
                writeError(response, HttpServletResponse.SC_CONFLICT,
                        "Requisição idempotente ainda em processamento.", "IDEMPOTENCY_IN_PROGRESS");
                return;
            }
            idempotencyStore.delete(idempotencyKey, scopeUserId, method, path);
        }

        LocalDateTime expiresAt = LocalDateTime.now().plusHours(properties.ttlHours());
        try {
            idempotencyStore.saveInProgress(idempotencyKey, scopeUserId, method, path, requestHash, expiresAt);
        } catch (DataIntegrityViolationException ex) {
            Optional<IdempotencyStore.StoredRecord> raced = idempotencyStore.find(
                    idempotencyKey, scopeUserId, method, path);
            if (raced.isPresent() && raced.get().status() == IdempotencyStatus.COMPLETED) {
                replayResponse(response, raced.get());
                return;
            }
            writeError(response, HttpServletResponse.SC_CONFLICT,
                    "Requisição idempotente ainda em processamento.", "IDEMPOTENCY_IN_PROGRESS");
            return;
        }

        ContentCachingResponseWrapper cachedResponse = new ContentCachingResponseWrapper(response);
        try {
            filterChain.doFilter(cachedRequest, cachedResponse);
            finalizeRecord(idempotencyKey, scopeUserId, method, path, cachedResponse);
            cachedResponse.copyBodyToResponse();
        } catch (Exception ex) {
            idempotencyStore.delete(idempotencyKey, scopeUserId, method, path);
            throw ex;
        }
    }

    private void finalizeRecord(String idempotencyKey,
                                long scopeUserId,
                                String method,
                                String path,
                                ContentCachingResponseWrapper cachedResponse) {
        int status = cachedResponse.getStatus();
        byte[] responseBody = cachedResponse.getContentAsByteArray();
        String body = IdempotencySupport.bodyAsString(responseBody);
        String contentType = cachedResponse.getContentType();

        if (status >= 500) {
            idempotencyStore.delete(idempotencyKey, scopeUserId, method, path);
            return;
        }

        idempotencyStore.markCompleted(
                idempotencyKey,
                scopeUserId,
                method,
                path,
                status,
                body,
                contentType
        );
    }

    private boolean isStale(IdempotencyStore.StoredRecord stored) {
        return stored.createdAt().plusSeconds(properties.inProgressTimeoutSeconds()).isBefore(LocalDateTime.now());
    }

    private void replayResponse(HttpServletResponse response, IdempotencyStore.StoredRecord stored) throws IOException {
        response.setStatus(stored.responseStatus());
        response.setHeader(IdempotencySupport.REPLAYED_HEADER, "true");
        if (stored.responseContentType() != null && !stored.responseContentType().isBlank()) {
            response.setContentType(stored.responseContentType());
        } else {
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        }
        if (stored.responseBody() != null) {
            response.getOutputStream().write(stored.responseBody().getBytes(StandardCharsets.UTF_8));
        }
    }

    private void writeError(HttpServletResponse response, int status, String message, String code) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        Map<String, String> body = new LinkedHashMap<>();
        body.put("message", message);
        body.put("code", code);
        String cid = MDC.get(CorrelationIdFilter.MDC_KEY);
        if (cid != null && !cid.isBlank()) {
            body.put("correlationId", cid);
        }
        String trace = MDC.get(CorrelationIdFilter.MDC_TRACE);
        if (trace != null && !trace.isBlank()) {
            body.put("traceId", trace);
        }
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}

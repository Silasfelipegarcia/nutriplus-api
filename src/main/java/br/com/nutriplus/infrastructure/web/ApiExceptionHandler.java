package br.com.nutriplus.infrastructure.web;

import br.com.nutriplus.application.port.TokenPort;
import br.com.nutriplus.exception.AccountLockedException;
import br.com.nutriplus.exception.AiAgentException;
import br.com.nutriplus.exception.BusinessException;
import br.com.nutriplus.exception.InvalidCredentialsException;
import br.com.nutriplus.exception.LoginDisabledException;
import br.com.nutriplus.exception.ResourceNotFoundException;
import br.com.nutriplus.exception.SubscriptionRequiredException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> notFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), null, request);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> business(BusinessException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), null, request);
    }

    @ExceptionHandler(SubscriptionRequiredException.class)
    public ResponseEntity<Map<String, Object>> subscriptionRequired(SubscriptionRequiredException ex, HttpServletRequest request) {
        return build(HttpStatus.PAYMENT_REQUIRED, ex.getMessage(), "SUBSCRIPTION_REQUIRED", request);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> illegalState(IllegalStateException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), "INVALID_STATE", request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> illegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), "INVALID_ARGUMENT", request);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, Object>> invalidCredentials(InvalidCredentialsException ex, HttpServletRequest request) {
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), null, request);
    }

    @ExceptionHandler(TokenPort.InvalidTokenException.class)
    public ResponseEntity<Map<String, Object>> invalidToken(TokenPort.InvalidTokenException ex, HttpServletRequest request) {
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), "INVALID_TOKEN", request);
    }

    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<Map<String, Object>> accountLocked(AccountLockedException ex, HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage(), "ACCOUNT_LOCKED", request);
    }

    @ExceptionHandler(LoginDisabledException.class)
    public ResponseEntity<Map<String, Object>> loginDisabled(LoginDisabledException ex, HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage(), "LOGIN_PENDING_APPROVAL", request);
    }

    @ExceptionHandler(AiAgentException.class)
    public ResponseEntity<Map<String, Object>> aiAgent(AiAgentException ex, HttpServletRequest request) {
        log.error("[AI_AGENT] {}", ex.getMessage(), ex);
        return build(HttpStatus.BAD_GATEWAY, ex.getMessage(), "AI_AGENT_ERROR", request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> accessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, "Acesso negado.", "FORBIDDEN", request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> unreadableBody(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Corpo da requisição inválido ou ausente.", "INVALID_REQUEST_BODY", request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        List<Map<String, String>> fields = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> Map.of("field", fe.getField(), "message", fe.getDefaultMessage() != null ? fe.getDefaultMessage() : ""))
                .toList();
        Map<String, Object> body = baseBody(message, "VALIDATION_ERROR", request);
        body.put("fields", fields);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> dataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
        String details = ex.getMessage() != null ? ex.getMessage().toLowerCase() : "";
        if (details.contains("cpf_hash") || details.contains("uk_users_cpf")) {
            return build(HttpStatus.BAD_REQUEST, "CPF já cadastrado", "DUPLICATE_CPF", request);
        }
        if (details.contains("email") || details.contains("users.email")) {
            return build(HttpStatus.BAD_REQUEST, "E-mail já cadastrado", "DUPLICATE_EMAIL", request);
        }
        log.warn("[DATA_INTEGRITY] {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, "Dados já cadastrados.", "DUPLICATE_DATA", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> fallback(Exception ex, HttpServletRequest request) {
        log.error("[UNHANDLED] {}", ex.getMessage(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno. Tente novamente.", "INTERNAL_ERROR", request);
    }

    private ResponseEntity<Map<String, Object>> build(HttpStatus status, String message, String code, HttpServletRequest request) {
        return ResponseEntity.status(status).body(baseBody(message, code, request));
    }

    private Map<String, Object> baseBody(String message, String code, HttpServletRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", message);
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("path", request.getRequestURI());
        if (code != null) {
            body.put("code", code);
        }
        String cid = MDC.get(CorrelationIdFilter.MDC_KEY);
        if (cid != null && !cid.isBlank()) {
            body.put("correlationId", cid);
        }
        String trace = MDC.get(CorrelationIdFilter.MDC_TRACE);
        if (trace != null && !trace.isBlank()) {
            body.put("traceId", trace);
        }
        return body;
    }
}

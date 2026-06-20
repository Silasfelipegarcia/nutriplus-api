package br.com.nutriplus.domain.entity;

import br.com.nutriplus.domain.enums.IdempotencyStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "idempotency_keys")
public class IdempotencyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "idempotency_key", nullable = false, length = 128)
    private String idempotencyKey;

    @Column(name = "scope_user_id", nullable = false)
    private Long scopeUserId;

    @Column(name = "http_method", nullable = false, length = 10)
    private String httpMethod;

    @Column(name = "request_path", nullable = false, length = 255)
    private String requestPath;

    @Column(name = "request_hash", nullable = false, columnDefinition = "CHAR(64) NOT NULL")
    private String requestHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IdempotencyStatus status;

    @Column(name = "response_status")
    private Integer responseStatus;

    @Column(name = "response_body", columnDefinition = "MEDIUMTEXT")
    private String responseBody;

    @Column(name = "response_content_type", length = 128)
    private String responseContentType;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    protected IdempotencyRecord() {
    }

    private IdempotencyRecord(Builder builder) {
        this.idempotencyKey = builder.idempotencyKey;
        this.scopeUserId = builder.scopeUserId;
        this.httpMethod = builder.httpMethod;
        this.requestPath = builder.requestPath;
        this.requestHash = builder.requestHash;
        this.status = builder.status;
        this.responseStatus = builder.responseStatus;
        this.responseBody = builder.responseBody;
        this.responseContentType = builder.responseContentType;
        this.expiresAt = builder.expiresAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getId() {
        return id;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public Long getScopeUserId() {
        return scopeUserId;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public String getRequestPath() {
        return requestPath;
    }

    public String getRequestHash() {
        return requestHash;
    }

    public IdempotencyStatus getStatus() {
        return status;
    }

    public Integer getResponseStatus() {
        return responseStatus;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public String getResponseContentType() {
        return responseContentType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setStatus(IdempotencyStatus status) {
        this.status = status;
    }

    public void setResponseStatus(Integer responseStatus) {
        this.responseStatus = responseStatus;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    public void setResponseContentType(String responseContentType) {
        this.responseContentType = responseContentType;
    }

    public static final class Builder {
        private String idempotencyKey;
        private Long scopeUserId;
        private String httpMethod;
        private String requestPath;
        private String requestHash;
        private IdempotencyStatus status;
        private Integer responseStatus;
        private String responseBody;
        private String responseContentType;
        private LocalDateTime expiresAt;

        public Builder idempotencyKey(String idempotencyKey) {
            this.idempotencyKey = idempotencyKey;
            return this;
        }

        public Builder scopeUserId(Long scopeUserId) {
            this.scopeUserId = scopeUserId;
            return this;
        }

        public Builder httpMethod(String httpMethod) {
            this.httpMethod = httpMethod;
            return this;
        }

        public Builder requestPath(String requestPath) {
            this.requestPath = requestPath;
            return this;
        }

        public Builder requestHash(String requestHash) {
            this.requestHash = requestHash;
            return this;
        }

        public Builder status(IdempotencyStatus status) {
            this.status = status;
            return this;
        }

        public Builder responseStatus(Integer responseStatus) {
            this.responseStatus = responseStatus;
            return this;
        }

        public Builder responseBody(String responseBody) {
            this.responseBody = responseBody;
            return this;
        }

        public Builder responseContentType(String responseContentType) {
            this.responseContentType = responseContentType;
            return this;
        }

        public Builder expiresAt(LocalDateTime expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        public IdempotencyRecord build() {
            return new IdempotencyRecord(this);
        }
    }
}

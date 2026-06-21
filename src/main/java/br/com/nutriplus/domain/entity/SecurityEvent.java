package br.com.nutriplus.domain.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "security_events")
public class SecurityEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "client_ip", nullable = false, length = 45)
    private String clientIp;

    @Column(nullable = false, length = 64)
    private String action;

    @Column(nullable = false)
    private int score;

    @Column(nullable = false)
    private boolean blocked;

    @Column(length = 2000)
    private String details;

    @Column(name = "correlation_id", length = 64)
    private String correlationId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected SecurityEvent() {
    }

    private SecurityEvent(Builder builder) {
        this.userId = builder.userId;
        this.clientIp = builder.clientIp;
        this.action = builder.action;
        this.score = builder.score;
        this.blocked = builder.blocked;
        this.details = builder.details;
        this.correlationId = builder.correlationId;
        this.createdAt = builder.createdAt != null ? builder.createdAt : LocalDateTime.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getId() {
        return id;
    }

    public static final class Builder {
        private Long userId;
        private String clientIp;
        private String action;
        private int score;
        private boolean blocked;
        private String details;
        private String correlationId;
        private LocalDateTime createdAt;

        public Builder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public Builder clientIp(String clientIp) {
            this.clientIp = clientIp;
            return this;
        }

        public Builder action(String action) {
            this.action = action;
            return this;
        }

        public Builder score(int score) {
            this.score = score;
            return this;
        }

        public Builder blocked(boolean blocked) {
            this.blocked = blocked;
            return this;
        }

        public Builder details(String details) {
            this.details = details;
            return this;
        }

        public Builder correlationId(String correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public SecurityEvent build() {
            return new SecurityEvent(this);
        }
    }
}

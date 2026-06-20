package br.com.nutriplus.domain.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_events")
public class ProductEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "session_id", nullable = false, length = 64)
    private String sessionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "event_name", nullable = false, length = 128)
    private String eventName;

    @Column(length = 128)
    private String step;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "properties_json", columnDefinition = "json")
    private String propertiesJson;

    @Column(name = "correlation_id", length = 64)
    private String correlationId;

    protected ProductEvent() {
    }

    private ProductEvent(Builder builder) {
        this.sessionId = builder.sessionId;
        this.user = builder.user;
        this.eventName = builder.eventName;
        this.step = builder.step;
        this.propertiesJson = builder.propertiesJson;
        this.correlationId = builder.correlationId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String sessionId;
        private User user;
        private String eventName;
        private String step;
        private String propertiesJson;
        private String correlationId;

        public Builder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public Builder user(User user) {
            this.user = user;
            return this;
        }

        public Builder eventName(String eventName) {
            this.eventName = eventName;
            return this;
        }

        public Builder step(String step) {
            this.step = step;
            return this;
        }

        public Builder propertiesJson(String propertiesJson) {
            this.propertiesJson = propertiesJson;
            return this;
        }

        public Builder correlationId(String correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public ProductEvent build() {
            return new ProductEvent(this);
        }
    }
}

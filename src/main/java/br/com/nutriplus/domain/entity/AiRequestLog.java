package br.com.nutriplus.domain.entity;

import br.com.nutriplus.domain.enums.AiRequestStatus;
import br.com.nutriplus.domain.enums.AiRequestType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_requests_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiRequestLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_type", nullable = false)
    private AiRequestType requestType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "request_payload", nullable = false, columnDefinition = "json")
    private String requestPayload;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "response_payload", columnDefinition = "json")
    private String responsePayload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AiRequestStatus status;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "duration_ms")
    private Integer durationMs;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

package br.com.nutriplus.domain.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "conversation_threads")
public class ConversationThread {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "care_relationship_id", nullable = false, unique = true)
    private CareRelationship careRelationship;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected ConversationThread() {
    }

    public static ConversationThread forCare(CareRelationship care) {
        ConversationThread t = new ConversationThread();
        t.careRelationship = care;
        return t;
    }

    public Long getId() {
        return id;
    }

    public CareRelationship getCareRelationship() {
        return careRelationship;
    }
}

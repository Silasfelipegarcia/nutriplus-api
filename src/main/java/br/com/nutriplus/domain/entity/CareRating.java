package br.com.nutriplus.domain.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "care_ratings")
public class CareRating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "care_relationship_id", nullable = false, unique = true)
    private CareRelationship careRelationship;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "nutritionist_id", nullable = false)
    private Nutritionist nutritionist;

    @Column(nullable = false, columnDefinition = "TINYINT")
    private int stars;

    @Column(length = 2000)
    private String comment;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected CareRating() {
    }

    public static CareRating create(CareRelationship care, int stars, String comment) {
        CareRating rating = new CareRating();
        rating.careRelationship = care;
        rating.patient = care.getPatient();
        rating.nutritionist = care.getNutritionist();
        rating.stars = stars;
        rating.comment = comment != null && !comment.isBlank() ? comment.trim() : null;
        return rating;
    }

    public Long getId() {
        return id;
    }

    public CareRelationship getCareRelationship() {
        return careRelationship;
    }

    public User getPatient() {
        return patient;
    }

    public Nutritionist getNutritionist() {
        return nutritionist;
    }

    public int getStars() {
        return stars;
    }

    public String getComment() {
        return comment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}

package br.com.nutriplus.domain.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "plan_revisions")
public class PlanRevision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meal_plan_id", nullable = false)
    private MealPlan mealPlan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nutritionist_id", nullable = false)
    private Nutritionist nutritionist;

    @Column(name = "changes_json", columnDefinition = "JSON")
    private String changesJson;

    @CreationTimestamp
    @Column(name = "published_at", nullable = false, updatable = false)
    private LocalDateTime publishedAt;

    protected PlanRevision() {
    }

    public static PlanRevision publish(MealPlan plan, Nutritionist nutritionist, String changesJson) {
        PlanRevision r = new PlanRevision();
        r.mealPlan = plan;
        r.nutritionist = nutritionist;
        r.changesJson = changesJson;
        return r;
    }

    public Long getId() {
        return id;
    }

    public MealPlan getMealPlan() {
        return mealPlan;
    }

    public Nutritionist getNutritionist() {
        return nutritionist;
    }

    public String getChangesJson() {
        return changesJson;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }
}

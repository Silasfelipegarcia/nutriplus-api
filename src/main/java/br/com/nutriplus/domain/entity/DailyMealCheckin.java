package br.com.nutriplus.domain.entity;

import br.com.nutriplus.domain.enums.CheckinStatus;
import br.com.nutriplus.domain.enums.MealType;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "daily_meal_checkins")
public class DailyMealCheckin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "checkin_date", nullable = false)
    private LocalDate checkinDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meal_id")
    private Meal meal;

    @Enumerated(EnumType.STRING)
    @Column(name = "meal_type", nullable = false)
    private MealType mealType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CheckinStatus status;

    @Column(length = 500)
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected DailyMealCheckin() {
    }

    private DailyMealCheckin(Builder builder) {
        this.id = builder.id;
        this.user = builder.user;
        this.checkinDate = builder.checkinDate;
        this.meal = builder.meal;
        this.mealType = builder.mealType;
        this.status = builder.status;
        this.notes = builder.notes;
        this.createdAt = builder.createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public LocalDate getCheckinDate() {
        return checkinDate;
    }

    public Meal getMeal() {
        return meal;
    }

    public MealType getMealType() {
        return mealType;
    }

    public CheckinStatus getStatus() {
        return status;
    }

    public void setStatus(CheckinStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public static final class Builder {
        private Long id;
        private User user;
        private LocalDate checkinDate;
        private Meal meal;
        private MealType mealType;
        private CheckinStatus status;
        private String notes;
        private LocalDateTime createdAt;

        public Builder user(User user) {
            this.user = user;
            return this;
        }

        public Builder checkinDate(LocalDate checkinDate) {
            this.checkinDate = checkinDate;
            return this;
        }

        public Builder meal(Meal meal) {
            this.meal = meal;
            return this;
        }

        public Builder mealType(MealType mealType) {
            this.mealType = mealType;
            return this;
        }

        public Builder status(CheckinStatus status) {
            this.status = status;
            return this;
        }

        public Builder notes(String notes) {
            this.notes = notes;
            return this;
        }

        public DailyMealCheckin build() {
            return new DailyMealCheckin(this);
        }
    }
}

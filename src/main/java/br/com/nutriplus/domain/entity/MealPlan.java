package br.com.nutriplus.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "meal_plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MealPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nutrition_profile_id", nullable = false)
    private NutritionProfile nutritionProfile;

    @Column(name = "plan_date", nullable = false)
    private LocalDate planDate;

    @Column(name = "total_calories", precision = 8, scale = 2)
    private BigDecimal totalCalories;

    @Column(name = "total_protein_g", precision = 8, scale = 2)
    private BigDecimal totalProteinG;

    @Column(name = "total_carbs_g", precision = 8, scale = 2)
    private BigDecimal totalCarbsG;

    @Column(name = "total_fat_g", precision = 8, scale = 2)
    private BigDecimal totalFatG;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String disclaimer;

    @Column(name = "ai_model", length = 100)
    private String aiModel;

    @OneToMany(mappedBy = "mealPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    @Builder.Default
    private List<Meal> meals = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

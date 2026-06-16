package br.com.nutriplus.domain.entity;

import br.com.nutriplus.domain.enums.*;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "nutrition_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NutritionProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private Integer age;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Sex sex;

    @Column(name = "height_cm", nullable = false, precision = 5, scale = 2)
    private BigDecimal heightCm;

    @Column(name = "current_weight_kg", nullable = false, precision = 5, scale = 2)
    private BigDecimal currentWeightKg;

    @Column(name = "target_weight_kg", nullable = false, precision = 5, scale = 2)
    private BigDecimal targetWeightKg;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Goal goal;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_level", nullable = false)
    private ActivityLevel activityLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "dietary_preference", nullable = false)
    private DietaryPreference dietaryPreference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Restriction restriction;

    @Column(name = "bmr_kcal", precision = 8, scale = 2)
    private BigDecimal bmrKcal;

    @Column(name = "tdee_kcal", precision = 8, scale = 2)
    private BigDecimal tdeeKcal;

    @Column(name = "target_calories", precision = 8, scale = 2)
    private BigDecimal targetCalories;

    @Column(name = "target_protein_g", precision = 8, scale = 2)
    private BigDecimal targetProteinG;

    @Column(name = "target_carbs_g", precision = 8, scale = 2)
    private BigDecimal targetCarbsG;

    @Column(name = "target_fat_g", precision = 8, scale = 2)
    private BigDecimal targetFatG;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}

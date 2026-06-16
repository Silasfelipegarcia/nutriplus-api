package br.com.nutriplus.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "meal_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MealItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meal_id", nullable = false)
    private Meal meal;

    @Column(name = "food_name", nullable = false, length = 200)
    private String foodName;

    @Column(name = "quantity_g", nullable = false, precision = 8, scale = 2)
    private BigDecimal quantityG;

    @Column(precision = 8, scale = 2)
    private BigDecimal calories;

    @Column(name = "protein_g", precision = 8, scale = 2)
    private BigDecimal proteinG;

    @Column(name = "carbs_g", precision = 8, scale = 2)
    private BigDecimal carbsG;

    @Column(name = "fat_g", precision = 8, scale = 2)
    private BigDecimal fatG;
}

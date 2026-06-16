package br.com.nutriplus.repository;

import br.com.nutriplus.domain.entity.MealPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface MealPlanRepository extends JpaRepository<MealPlan, Long> {

    @Query("SELECT DISTINCT mp FROM MealPlan mp LEFT JOIN FETCH mp.meals m LEFT JOIN FETCH m.items WHERE mp.user.id = :userId ORDER BY mp.createdAt DESC")
    java.util.List<MealPlan> findByUserIdWithMealsOrderByCreatedAtDesc(Long userId);
}

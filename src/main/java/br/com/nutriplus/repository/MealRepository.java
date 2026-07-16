package br.com.nutriplus.repository;

import br.com.nutriplus.domain.entity.Meal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface MealRepository extends JpaRepository<Meal, Long> {

    List<Meal> findByMealPlanIdOrderBySortOrderAsc(Long mealPlanId);

    boolean existsByIdAndMealPlan_User_Id(Long id, Long userId);

    @Query("""
            SELECT m FROM Meal m
            JOIN FETCH m.mealPlan p
            WHERE p.id IN :mealPlanIds
            ORDER BY p.id ASC, m.sortOrder ASC
            """)
    List<Meal> findByMealPlanIdInOrderByMealPlanIdAscSortOrderAsc(@Param("mealPlanIds") Collection<Long> mealPlanIds);
}

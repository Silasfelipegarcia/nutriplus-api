package br.com.nutriplus.repository;

import br.com.nutriplus.domain.entity.Meal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MealRepository extends JpaRepository<Meal, Long> {

    List<Meal> findByMealPlanIdOrderBySortOrderAsc(Long mealPlanId);
}

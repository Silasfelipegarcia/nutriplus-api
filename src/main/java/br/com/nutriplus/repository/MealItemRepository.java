package br.com.nutriplus.repository;

import br.com.nutriplus.domain.entity.MealItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface MealItemRepository extends JpaRepository<MealItem, Long> {

    List<MealItem> findByMealIdOrderByIdAsc(Long mealId);

    @Query("SELECT mi FROM MealItem mi WHERE mi.meal.id IN :mealIds ORDER BY mi.meal.id ASC, mi.id ASC")
    List<MealItem> findByMealIds(@Param("mealIds") Collection<Long> mealIds);
}

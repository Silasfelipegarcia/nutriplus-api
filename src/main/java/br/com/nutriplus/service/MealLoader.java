package br.com.nutriplus.service;

import br.com.nutriplus.domain.entity.Meal;
import br.com.nutriplus.domain.entity.MealItem;
import br.com.nutriplus.repository.MealItemRepository;
import br.com.nutriplus.repository.MealRepository;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class MealLoader {

    private final MealRepository mealRepository;
    private final MealItemRepository mealItemRepository;

    public MealLoader(MealRepository mealRepository, MealItemRepository mealItemRepository) {
        this.mealRepository = mealRepository;
        this.mealItemRepository = mealItemRepository;
    }

    public List<Meal> mealsForPlan(Long mealPlanId) {
        return mealRepository.findByMealPlanIdOrderBySortOrderAsc(mealPlanId);
    }

    public Map<Long, List<Meal>> mealsByPlanIds(Collection<Long> mealPlanIds) {
        if (mealPlanIds == null || mealPlanIds.isEmpty()) {
            return Map.of();
        }
        return mealRepository.findByMealPlanIdInOrderByMealPlanIdAscSortOrderAsc(mealPlanIds).stream()
                .collect(Collectors.groupingBy(meal -> meal.getMealPlan().getId()));
    }

    public Map<Long, List<MealItem>> itemsByMealId(List<Meal> meals) {
        if (meals.isEmpty()) {
            return Map.of();
        }
        List<Long> mealIds = meals.stream().map(Meal::getId).toList();
        return mealItemRepository.findByMealIds(mealIds).stream()
                .collect(Collectors.groupingBy(item -> item.getMeal().getId()));
    }

    public List<MealItem> itemsForMeal(Long mealId) {
        if (mealId == null) {
            return List.of();
        }
        return mealItemRepository.findByMealIdOrderByIdAsc(mealId);
    }

    public Map<Long, List<MealItem>> emptyItemsMap() {
        return Collections.emptyMap();
    }
}

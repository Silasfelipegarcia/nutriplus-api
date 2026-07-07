package br.com.nutriplus.service;

import br.com.nutriplus.domain.entity.Meal;
import br.com.nutriplus.domain.entity.MealItem;
import br.com.nutriplus.domain.entity.MealPlan;
import br.com.nutriplus.domain.entity.User;
import br.com.nutriplus.exception.ResourceNotFoundException;
import br.com.nutriplus.repository.MealPlanRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SharedPlanSnapshotBuilder {

    private final MealPlanRepository mealPlanRepository;
    private final MealLoader mealLoader;

    public SharedPlanSnapshotBuilder(MealPlanRepository mealPlanRepository, MealLoader mealLoader) {
        this.mealPlanRepository = mealPlanRepository;
        this.mealLoader = mealLoader;
    }

    public Map<String, Object> buildSnapshot(Long mealPlanId) {
        MealPlan plan = mealPlanRepository.findById(mealPlanId)
                .orElseThrow(() -> new ResourceNotFoundException("Plano base não encontrado"));
        User owner = plan.getUser();
        List<Meal> meals = mealLoader.mealsForPlan(plan.getId());
        Map<Long, List<MealItem>> itemsByMeal = mealLoader.itemsByMealId(meals);

        List<Map<String, Object>> mealMaps = new ArrayList<>();
        for (Meal meal : meals) {
            Map<String, Object> mealMap = new HashMap<>();
            mealMap.put("mealType", meal.getMealType().name());
            mealMap.put("name", meal.getName());
            mealMap.put("sortOrder", meal.getSortOrder());
            if (meal.getScheduledTime() != null) {
                mealMap.put("scheduledTime", meal.getScheduledTime().toString().substring(0, 5));
            }

            List<Map<String, Object>> itemMaps = new ArrayList<>();
            for (MealItem item : itemsByMeal.getOrDefault(meal.getId(), List.of())) {
                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("foodName", item.getFoodName());
                itemMap.put("quantityG", item.getQuantityG());
                if (item.getQuantityDisplay() != null) {
                    itemMap.put("quantityDisplay", item.getQuantityDisplay());
                }
                itemMaps.add(itemMap);
            }
            mealMap.put("items", itemMaps);
            mealMaps.add(mealMap);
        }

        Map<String, Object> snapshot = new HashMap<>();
        snapshot.put("planId", plan.getId());
        if (owner != null) {
            snapshot.put("ownerLabel", owner.getName());
        }
        snapshot.put("meals", mealMaps);
        return snapshot;
    }
}

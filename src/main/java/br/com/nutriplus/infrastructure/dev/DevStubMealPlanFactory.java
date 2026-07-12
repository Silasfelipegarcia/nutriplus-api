package br.com.nutriplus.infrastructure.dev;

import br.com.nutriplus.domain.entity.Meal;
import br.com.nutriplus.domain.entity.MealItem;
import br.com.nutriplus.domain.entity.MealPlan;
import br.com.nutriplus.domain.entity.NutritionProfile;
import br.com.nutriplus.domain.entity.User;
import br.com.nutriplus.domain.enums.MealType;
import br.com.nutriplus.repository.MealItemRepository;
import br.com.nutriplus.repository.MealPlanRepository;
import br.com.nutriplus.repository.MealRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Plano alimentar mínimo para testes funcionais locais (sem chamar o agente de IA).
 */
final class DevStubMealPlanFactory {

    private DevStubMealPlanFactory() {
    }

    static MealPlan createStubPlan(MealPlanRepository mealPlanRepository,
                                   MealRepository mealRepository,
                                   MealItemRepository mealItemRepository,
                                   User user,
                                   NutritionProfile profile,
                                   LocalDateTime createdAt) {
        MealPlan plan = mealPlanRepository.save(MealPlan.builder()
                .user(user)
                .nutritionProfile(profile)
                .planDate(LocalDate.now())
                .totalCalories(new BigDecimal("1900.00"))
                .totalProteinG(new BigDecimal("150.00"))
                .totalCarbsG(new BigDecimal("190.00"))
                .totalFatG(new BigDecimal("63.00"))
                .disclaimer("Plano de teste gerado automaticamente no ambiente local.")
                .aiModel("dev-stub")
                .createdAt(createdAt)
                .build());

        createMeal(mealRepository, mealItemRepository, plan, MealType.BREAKFAST, "Café da manhã", 1,
                LocalTime.of(7, 30), "Ovos mexidos", new BigDecimal("120.00"));
        createMeal(mealRepository, mealItemRepository, plan, MealType.LUNCH, "Almoço", 2,
                LocalTime.of(12, 30), "Frango grelhado", new BigDecimal("150.00"));
        createMeal(mealRepository, mealItemRepository, plan, MealType.AFTERNOON_SNACK, "Lanche", 3,
                LocalTime.of(16, 0), "Iogurte natural", new BigDecimal("170.00"));
        createMeal(mealRepository, mealItemRepository, plan, MealType.DINNER, "Jantar", 4,
                LocalTime.of(19, 30), "Salada com atum", new BigDecimal("130.00"));

        return plan;
    }

    private static void createMeal(MealRepository mealRepository,
                                   MealItemRepository mealItemRepository,
                                   MealPlan plan,
                                   MealType type,
                                   String name,
                                   int sortOrder,
                                   LocalTime time,
                                   String foodName,
                                   BigDecimal quantityG) {
        Meal meal = mealRepository.save(Meal.builder()
                .mealPlan(plan)
                .mealType(type)
                .name(name)
                .sortOrder(sortOrder)
                .scheduledTime(time)
                .build());

        mealItemRepository.save(MealItem.builder()
                .meal(meal)
                .foodName(foodName)
                .quantityG(quantityG)
                .quantityDisplay(quantityG.intValue() + " g")
                .unitKind("G")
                .calories(new BigDecimal("180.00"))
                .proteinG(new BigDecimal("25.00"))
                .carbsG(new BigDecimal("12.00"))
                .fatG(new BigDecimal("6.00"))
                .build());
    }
}

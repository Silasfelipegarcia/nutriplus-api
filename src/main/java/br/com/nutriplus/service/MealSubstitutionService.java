package br.com.nutriplus.service;

import br.com.nutriplus.client.AiAgentClient;
import br.com.nutriplus.client.dto.AiMealItemDto;
import br.com.nutriplus.client.dto.AiSubstitutionResponse;
import br.com.nutriplus.domain.entity.Meal;
import br.com.nutriplus.domain.entity.MealItem;
import br.com.nutriplus.domain.entity.MealItemSwapEvent;
import br.com.nutriplus.domain.entity.MealPlan;
import br.com.nutriplus.domain.entity.NutritionProfile;
import br.com.nutriplus.domain.entity.User;
import br.com.nutriplus.dto.request.ApplyMealItemSubstitutionRequest;
import br.com.nutriplus.dto.response.MealItemResponse;
import br.com.nutriplus.dto.response.MealItemSubstitutionOptionsResponse;
import br.com.nutriplus.dto.response.MealPlanResponse;
import br.com.nutriplus.exception.BusinessException;
import br.com.nutriplus.exception.ResourceNotFoundException;
import br.com.nutriplus.infrastructure.config.NutriCacheEvictionService;
import br.com.nutriplus.mapper.ResponseMapper;
import br.com.nutriplus.repository.MealItemRepository;
import br.com.nutriplus.repository.MealItemSwapEventRepository;
import br.com.nutriplus.repository.MealPlanRepository;
import br.com.nutriplus.repository.MealRepository;
import br.com.nutriplus.repository.NutritionProfileRepository;
import br.com.nutriplus.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class MealSubstitutionService {

    private static final BigDecimal CALORIE_TOLERANCE = new BigDecimal("0.10");

    private final CurrentUser currentUser;
    private final NutritionProfileRepository nutritionProfileRepository;
    private final MealRepository mealRepository;
    private final MealItemRepository mealItemRepository;
    private final MealPlanRepository mealPlanRepository;
    private final MealItemSwapEventRepository swapEventRepository;
    private final MealLoader mealLoader;
    private final ResponseMapper responseMapper;
    private final AiAgentClient aiAgentClient;
    private final NutriCacheEvictionService cacheEvictionService;

    public MealSubstitutionService(CurrentUser currentUser,
                                   NutritionProfileRepository nutritionProfileRepository,
                                   MealRepository mealRepository,
                                   MealItemRepository mealItemRepository,
                                   MealPlanRepository mealPlanRepository,
                                   MealItemSwapEventRepository swapEventRepository,
                                   MealLoader mealLoader,
                                   ResponseMapper responseMapper,
                                   AiAgentClient aiAgentClient,
                                   NutriCacheEvictionService cacheEvictionService) {
        this.currentUser = currentUser;
        this.nutritionProfileRepository = nutritionProfileRepository;
        this.mealRepository = mealRepository;
        this.mealItemRepository = mealItemRepository;
        this.mealPlanRepository = mealPlanRepository;
        this.swapEventRepository = swapEventRepository;
        this.mealLoader = mealLoader;
        this.responseMapper = responseMapper;
        this.aiAgentClient = aiAgentClient;
        this.cacheEvictionService = cacheEvictionService;
    }

    public MealItemSubstitutionOptionsResponse suggestSubstitutions(Long mealId, Long mealItemId) {
        User user = currentUser.get();
        NutritionProfile profile = nutritionProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Perfil nutricional não encontrado"));
        MealItem item = requireOwnedMealItem(user.getId(), mealId, mealItemId);

        AiSubstitutionResponse ai = aiAgentClient.generateSubstitutions(
                profile,
                item.getFoodName(),
                item.getQuantityG(),
                item.getCalories(),
                item.getProteinG());

        List<MealItemResponse> options = new ArrayList<>();
        if (ai.substitutions() != null) {
            for (AiMealItemDto sub : ai.substitutions()) {
                if (sub == null || sub.foodName() == null || sub.foodName().isBlank()) {
                    continue;
                }
                if (!withinCalorieTolerance(item.getCalories(), sub.calories())) {
                    continue;
                }
                options.add(toResponse(null, sub));
            }
        }

        return new MealItemSubstitutionOptionsResponse(
                mealId,
                mealItemId,
                toResponse(item),
                options);
    }

    @Transactional
    public MealPlanResponse applySubstitution(Long mealId,
                                              Long mealItemId,
                                              ApplyMealItemSubstitutionRequest request) {
        User user = currentUser.get();
        MealItem item = requireOwnedMealItem(user.getId(), mealId, mealItemId);
        if (!withinCalorieTolerance(item.getCalories(), request.calories())) {
            throw new BusinessException("A troca precisa manter calorias equivalentes (±10%).");
        }

        String fromFood = item.getFoodName();
        BigDecimal fromCalories = item.getCalories();

        item.setFoodName(request.foodName().trim());
        item.setQuantityG(request.quantityG());
        item.setQuantityDisplay(resolveQuantityDisplay(request));
        item.setUnitKind(resolveUnitKind(request));
        item.setCalories(request.calories());
        item.setProteinG(request.proteinG());
        item.setCarbsG(request.carbsG());
        item.setFatG(request.fatG());
        mealItemRepository.save(item);

        MealPlan mealPlan = item.getMeal().getMealPlan();
        List<Meal> meals = mealRepository.findByMealPlanIdOrderBySortOrderAsc(mealPlan.getId());
        List<Long> mealIds = meals.stream().map(Meal::getId).toList();
        List<MealItem> allItems = mealIds.isEmpty() ? List.of() : mealItemRepository.findByMealIds(mealIds);
        recalculateMealPlanTotals(mealPlan, allItems);
        mealPlanRepository.save(mealPlan);

        swapEventRepository.save(new MealItemSwapEvent(
                user,
                mealId,
                mealItemId,
                fromFood,
                item.getFoodName(),
                fromCalories,
                item.getCalories()));

        cacheEvictionService.evictMealPlanCaches(user.getId());
        cacheEvictionService.evictCheckinCaches(user.getId());

        Map<Long, List<MealItem>> itemsByMealId = mealLoader.itemsByMealId(meals);
        return responseMapper.toMealPlanResponse(mealPlan, meals, itemsByMealId);
    }

    private MealItem requireOwnedMealItem(Long userId, Long mealId, Long mealItemId) {
        if (!mealRepository.existsByIdAndMealPlan_User_Id(mealId, userId)) {
            throw new ResourceNotFoundException("Refeição não encontrada");
        }
        return mealItemRepository.findByIdAndMeal_Id(mealItemId, mealId)
                .orElseThrow(() -> new ResourceNotFoundException("Alimento não encontrado"));
    }

    private String resolveQuantityDisplay(ApplyMealItemSubstitutionRequest request) {
        if (request.quantityDisplay() != null && !request.quantityDisplay().isBlank()) {
            return request.quantityDisplay().trim();
        }
        return request.quantityG().stripTrailingZeros().toPlainString() + " g";
    }

    private String resolveUnitKind(ApplyMealItemSubstitutionRequest request) {
        if (request.unitKind() != null && !request.unitKind().isBlank()) {
            return request.unitKind().trim();
        }
        return "gram";
    }

    private boolean withinCalorieTolerance(BigDecimal original, BigDecimal candidate) {
        if (original == null || candidate == null || original.compareTo(BigDecimal.ZERO) <= 0) {
            return true;
        }
        BigDecimal delta = candidate.subtract(original).abs();
        BigDecimal allowed = original.multiply(CALORIE_TOLERANCE);
        return delta.compareTo(allowed) <= 0;
    }

    private void recalculateMealPlanTotals(MealPlan mealPlan, List<MealItem> mealItems) {
        BigDecimal calories = BigDecimal.ZERO;
        BigDecimal protein = BigDecimal.ZERO;
        BigDecimal carbs = BigDecimal.ZERO;
        BigDecimal fat = BigDecimal.ZERO;
        for (MealItem item : mealItems) {
            if (item.getCalories() != null) {
                calories = calories.add(item.getCalories());
            }
            if (item.getProteinG() != null) {
                protein = protein.add(item.getProteinG());
            }
            if (item.getCarbsG() != null) {
                carbs = carbs.add(item.getCarbsG());
            }
            if (item.getFatG() != null) {
                fat = fat.add(item.getFatG());
            }
        }
        mealPlan.setTotalCalories(calories.setScale(2, RoundingMode.HALF_UP));
        mealPlan.setTotalProteinG(protein.setScale(2, RoundingMode.HALF_UP));
        mealPlan.setTotalCarbsG(carbs.setScale(2, RoundingMode.HALF_UP));
        mealPlan.setTotalFatG(fat.setScale(2, RoundingMode.HALF_UP));
    }

    private MealItemResponse toResponse(MealItem item) {
        return new MealItemResponse(
                item.getId(),
                item.getFoodName(),
                item.getQuantityG(),
                item.getQuantityDisplay(),
                item.getUnitKind(),
                item.getCalories(),
                item.getProteinG(),
                item.getCarbsG(),
                item.getFatG());
    }

    private MealItemResponse toResponse(Long id, AiMealItemDto dto) {
        return new MealItemResponse(
                id,
                dto.foodName(),
                dto.quantityG(),
                dto.quantityDisplay(),
                dto.unitKind(),
                dto.calories(),
                dto.proteinG(),
                dto.carbsG(),
                dto.fatG());
    }
}

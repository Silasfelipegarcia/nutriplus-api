package br.com.nutriplus.service;

import br.com.nutriplus.client.dto.AiShoppingSwapOptionDto;
import br.com.nutriplus.domain.entity.Meal;
import br.com.nutriplus.domain.entity.MealItem;
import br.com.nutriplus.domain.entity.MealPlan;
import br.com.nutriplus.domain.entity.ShoppingList;
import br.com.nutriplus.domain.entity.ShoppingListItem;
import br.com.nutriplus.domain.entity.User;
import br.com.nutriplus.dto.request.ApplyShoppingSwapsRequest;
import br.com.nutriplus.dto.request.ShoppingSwapSelectionRequest;
import br.com.nutriplus.dto.response.ApplyShoppingSwapsResponse;
import br.com.nutriplus.dto.response.ShoppingListResponse;
import br.com.nutriplus.exception.ResourceNotFoundException;
import br.com.nutriplus.mapper.ResponseMapper;
import br.com.nutriplus.repository.MealItemRepository;
import br.com.nutriplus.repository.MealPlanRepository;
import br.com.nutriplus.repository.MealRepository;
import br.com.nutriplus.repository.ShoppingListRepository;
import br.com.nutriplus.security.CurrentUser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
public class ShoppingSwapService {

    private final CurrentUser currentUser;
    private final ShoppingListRepository shoppingListRepository;
    private final MealRepository mealRepository;
    private final MealItemRepository mealItemRepository;
    private final MealPlanRepository mealPlanRepository;
    private final ResponseMapper responseMapper;
    private final ObjectMapper objectMapper;

    public ShoppingSwapService(CurrentUser currentUser,
                               ShoppingListRepository shoppingListRepository,
                               MealRepository mealRepository,
                               MealItemRepository mealItemRepository,
                               MealPlanRepository mealPlanRepository,
                               ResponseMapper responseMapper,
                               ObjectMapper objectMapper) {
        this.currentUser = currentUser;
        this.shoppingListRepository = shoppingListRepository;
        this.mealRepository = mealRepository;
        this.mealItemRepository = mealItemRepository;
        this.mealPlanRepository = mealPlanRepository;
        this.responseMapper = responseMapper;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ApplyShoppingSwapsResponse applySwaps(ApplyShoppingSwapsRequest request) {
        User user = currentUser.get();
        ShoppingList list = latestListForUser(user.getId());
        Map<Long, ShoppingListItem> itemsById = new HashMap<>();
        for (ShoppingListItem item : list.getItems()) {
            itemsById.put(item.getId(), item);
        }

        MealPlan mealPlan = list.getMealPlan();
        List<Meal> meals = mealRepository.findByMealPlanIdOrderBySortOrderAsc(mealPlan.getId());
        List<Long> mealIds = meals.stream().map(Meal::getId).toList();
        List<MealItem> mealItems = mealIds.isEmpty() ? List.of() : mealItemRepository.findByMealIds(mealIds);

        for (ShoppingSwapSelectionRequest selection : request.selections()) {
            ShoppingListItem item = itemsById.get(selection.shoppingListItemId());
            if (item == null) {
                throw new ResponseStatusException(BAD_REQUEST, "Item da lista inválido");
            }
            AiShoppingSwapOptionDto option = findSwapOption(item, selection.swapOptionId());
            if (option == null) {
                throw new ResponseStatusException(BAD_REQUEST, "Opção de troca inválida");
            }

            Integer oldKcalRef = item.getKcalEstimate();
            item.setItemName(option.label());
            item.setSelectedSwapId(option.id());
            if (option.proteinLeanness() != null) {
                item.setProteinLeanness(option.proteinLeanness());
            }
            if (option.kcalEstimate() != null) {
                item.setKcalEstimate(option.kcalEstimate());
            }

            applyMealItemSwaps(mealItems, option, oldKcalRef, option.kcalEstimate());
        }

        mealItemRepository.saveAll(mealItems);
        shoppingListRepository.save(list);
        recalculateMealPlanTotals(mealPlan, mealItems);
        mealPlanRepository.save(mealPlan);

        ShoppingListResponse response = responseMapper.toShoppingListResponse(list);
        return new ApplyShoppingSwapsResponse(response, mealPlan.getId());
    }

    private ShoppingList latestListForUser(Long userId) {
        List<ShoppingList> lists = shoppingListRepository.findByUserIdWithItemsOrderByCreatedAtDesc(userId);
        if (lists.isEmpty()) {
            throw new ResourceNotFoundException("Nenhuma lista de compras encontrada");
        }
        return lists.getFirst();
    }

    private AiShoppingSwapOptionDto findSwapOption(ShoppingListItem item, String swapOptionId) {
        List<AiShoppingSwapOptionDto> options = parseSwapOptions(item.getSwapOptionsJson());
        return options.stream()
                .filter(o -> Objects.equals(o.id(), swapOptionId))
                .findFirst()
                .orElse(null);
    }

    private List<AiShoppingSwapOptionDto> parseSwapOptions(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<AiShoppingSwapOptionDto>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }

    private void applyMealItemSwaps(List<MealItem> mealItems,
                                    AiShoppingSwapOptionDto option,
                                    Integer oldKcalRef,
                                    Integer newKcalRef) {
        List<String> targets = option.matchesMealFoods() != null ? option.matchesMealFoods() : List.of();
        for (MealItem mealItem : mealItems) {
            if (!matchesAnyFood(mealItem.getFoodName(), targets)) {
                continue;
            }
            mealItem.setFoodName(option.label());
            scaleMacros(mealItem, oldKcalRef, newKcalRef);
        }
    }

    private boolean matchesAnyFood(String foodName, List<String> targets) {
        if (targets == null || targets.isEmpty()) {
            return false;
        }
        String normalizedFood = normalize(foodName);
        for (String target : targets) {
            String normalizedTarget = normalize(target);
            if (normalizedFood.equals(normalizedTarget)
                    || normalizedFood.contains(normalizedTarget)
                    || normalizedTarget.contains(normalizedFood)) {
                return true;
            }
        }
        return false;
    }

    private void scaleMacros(MealItem mealItem, Integer oldKcalRef, Integer newKcalRef) {
        if (oldKcalRef == null || newKcalRef == null || oldKcalRef <= 0) {
            return;
        }
        BigDecimal ratio = BigDecimal.valueOf(newKcalRef)
                .divide(BigDecimal.valueOf(oldKcalRef), 4, RoundingMode.HALF_UP);
        if (mealItem.getCalories() != null) {
            mealItem.setCalories(mealItem.getCalories().multiply(ratio).setScale(2, RoundingMode.HALF_UP));
        }
        if (mealItem.getProteinG() != null) {
            mealItem.setProteinG(mealItem.getProteinG().multiply(ratio).setScale(2, RoundingMode.HALF_UP));
        }
        if (mealItem.getCarbsG() != null) {
            mealItem.setCarbsG(mealItem.getCarbsG().multiply(ratio).setScale(2, RoundingMode.HALF_UP));
        }
        if (mealItem.getFatG() != null) {
            mealItem.setFatG(mealItem.getFatG().multiply(ratio).setScale(2, RoundingMode.HALF_UP));
        }
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
        mealPlan.setTotalCalories(calories);
        mealPlan.setTotalProteinG(protein);
        mealPlan.setTotalCarbsG(carbs);
        mealPlan.setTotalFatG(fat);
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        String lowered = value.toLowerCase(Locale.ROOT).trim();
        return Normalizer.normalize(lowered, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    }
}

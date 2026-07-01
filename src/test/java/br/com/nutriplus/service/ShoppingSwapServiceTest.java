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
import br.com.nutriplus.dto.response.MealPlanResponse;
import br.com.nutriplus.dto.response.ShoppingListResponse;
import br.com.nutriplus.infrastructure.config.NutriCacheEvictionService;
import br.com.nutriplus.mapper.ResponseMapper;
import br.com.nutriplus.repository.MealItemRepository;
import br.com.nutriplus.repository.MealPlanRepository;
import br.com.nutriplus.repository.MealRepository;
import br.com.nutriplus.repository.ShoppingListRepository;
import br.com.nutriplus.security.CurrentUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShoppingSwapServiceTest {

    @Mock
    private CurrentUser currentUser;
    @Mock
    private ShoppingListRepository shoppingListRepository;
    @Mock
    private MealRepository mealRepository;
    @Mock
    private MealItemRepository mealItemRepository;
    @Mock
    private MealPlanRepository mealPlanRepository;
    @Mock
    private MealLoader mealLoader;
    @Mock
    private ResponseMapper responseMapper;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private NutriCacheEvictionService cacheEvictionService;

    @InjectMocks
    private ShoppingSwapService shoppingSwapService;

    @Test
    void applySwapsUpdatesShoppingItemAndMealFoodName() throws Exception {
        User user = User.builder().id(1L).email("u@test.com").name("U").passwordHash("x").build();
        MealPlan mealPlan = MealPlan.builder().id(10L).user(user).planDate(LocalDate.now()).disclaimer("d").build();
        ShoppingList list = ShoppingList.builder().id(5L).user(user).mealPlan(mealPlan)
                .weekStart(LocalDate.now()).weekEnd(LocalDate.now().plusDays(6)).items(new ArrayList<>()).build();

        String swapJson = """
                [{"id":"tilapia_file","label":"Tilápia filé","costTier":"ECONOMIC","kcalEstimate":128,"matchesMealFoods":["Salmão grelhado"]}]
                """;
        ShoppingListItem shoppingItem = ShoppingListItem.builder()
                .id(99L)
                .shoppingList(list)
                .itemName("Filé de salmão")
                .quantity("400g")
                .category("Proteínas")
                .kcalEstimate(208)
                .swapOptionsJson(swapJson)
                .build();
        list.getItems().add(shoppingItem);

        Meal meal = Meal.builder().id(20L).mealPlan(mealPlan).mealType(br.com.nutriplus.domain.enums.MealType.LUNCH)
                .name("Almoço").sortOrder(1).build();
        MealItem mealItem = MealItem.builder()
                .id(30L)
                .meal(meal)
                .foodName("Salmão grelhado")
                .quantityG(new BigDecimal("150"))
                .calories(new BigDecimal("300"))
                .proteinG(new BigDecimal("30"))
                .carbsG(BigDecimal.ZERO)
                .fatG(new BigDecimal("18"))
                .build();

        when(currentUser.get()).thenReturn(user);
        when(shoppingListRepository.findByUserIdWithItemsOrderByCreatedAtDesc(1L)).thenReturn(List.of(list));
        when(mealRepository.findByMealPlanIdOrderBySortOrderAsc(10L)).thenReturn(List.of(meal));
        when(mealItemRepository.findByMealIds(List.of(20L))).thenReturn(List.of(mealItem));
        when(mealLoader.itemsByMealId(List.of(meal))).thenReturn(Map.of(20L, List.of(mealItem)));
        when(responseMapper.toShoppingListResponse(list))
                .thenReturn(new ShoppingListResponse(5L, 10L, LocalDate.now(), LocalDate.now().plusDays(6),
                        List.of(), null, false, null));
        when(responseMapper.toMealPlanResponse(eq(mealPlan), eq(List.of(meal)), any()))
                .thenReturn(new MealPlanResponse(10L, LocalDate.now(), new BigDecimal("188"), null, null, null,
                        "d", List.of(), null, null, null, null, null, null, null, null));
        when(objectMapper.readValue(any(String.class), any(com.fasterxml.jackson.core.type.TypeReference.class)))
                .thenReturn(List.of(new AiShoppingSwapOptionDto(
                        "tilapia_file", "Tilápia filé", "ECONOMIC", null, "magro", 128, List.of("Salmão grelhado")
                )));

        var response = shoppingSwapService.applySwaps(
                new ApplyShoppingSwapsRequest(List.of(new ShoppingSwapSelectionRequest(99L, "tilapia_file")))
        );

        assertEquals("Tilápia filé", shoppingItem.getItemName());
        assertEquals("tilapia_file", shoppingItem.getSelectedSwapId());
        assertEquals("Tilápia filé", mealItem.getFoodName());
        assertEquals(10L, response.mealPlanId());
        assertEquals(new BigDecimal("184.62"), mealItem.getCalories());
    }

    @Test
    void applySwapsMatchesShoppingItemNameWhenMealTargetsMissing() throws Exception {
        User user = User.builder().id(1L).email("u@test.com").name("U").passwordHash("x").build();
        MealPlan mealPlan = MealPlan.builder().id(10L).user(user).planDate(LocalDate.now()).disclaimer("d").build();
        ShoppingList list = ShoppingList.builder().id(5L).user(user).mealPlan(mealPlan)
                .weekStart(LocalDate.now()).weekEnd(LocalDate.now().plusDays(6)).items(new ArrayList<>()).build();

        String swapJson = """
                [{"id":"ovo","label":"Ovos","costTier":"ECONOMIC","kcalEstimate":155}]
                """;
        ShoppingListItem shoppingItem = ShoppingListItem.builder()
                .id(101L)
                .shoppingList(list)
                .itemName("Peito de frango")
                .quantity("800g")
                .category("Proteínas")
                .kcalEstimate(165)
                .swapOptionsJson(swapJson)
                .build();
        list.getItems().add(shoppingItem);

        Meal meal = Meal.builder().id(22L).mealPlan(mealPlan)
                .mealType(br.com.nutriplus.domain.enums.MealType.LUNCH).name("Almoço").sortOrder(1).build();
        MealItem mealItem = MealItem.builder()
                .id(32L)
                .meal(meal)
                .foodName("Peito de frango grelhado")
                .quantityG(new BigDecimal("150"))
                .calories(new BigDecimal("248"))
                .proteinG(new BigDecimal("46"))
                .carbsG(BigDecimal.ZERO)
                .fatG(new BigDecimal("5"))
                .build();

        when(currentUser.get()).thenReturn(user);
        when(shoppingListRepository.findByUserIdWithItemsOrderByCreatedAtDesc(1L)).thenReturn(List.of(list));
        when(mealRepository.findByMealPlanIdOrderBySortOrderAsc(10L)).thenReturn(List.of(meal));
        when(mealItemRepository.findByMealIds(List.of(22L))).thenReturn(List.of(mealItem));
        when(mealLoader.itemsByMealId(List.of(meal))).thenReturn(Map.of(22L, List.of(mealItem)));
        when(responseMapper.toShoppingListResponse(list))
                .thenReturn(new ShoppingListResponse(5L, 10L, LocalDate.now(), LocalDate.now().plusDays(6),
                        List.of(), null, false, null));
        when(responseMapper.toMealPlanResponse(eq(mealPlan), eq(List.of(meal)), any()))
                .thenReturn(new MealPlanResponse(10L, LocalDate.now(), new BigDecimal("233"), null, null, null,
                        "d", List.of(), null, null, null, null, null, null, null, null));
        when(objectMapper.readValue(any(String.class), any(com.fasterxml.jackson.core.type.TypeReference.class)))
                .thenReturn(List.of(new AiShoppingSwapOptionDto(
                        "ovo", "Ovos", "ECONOMIC", null, "moderado", 155, List.of()
                )));

        shoppingSwapService.applySwaps(
                new ApplyShoppingSwapsRequest(List.of(new ShoppingSwapSelectionRequest(101L, "ovo")))
        );

        assertEquals("Ovos", shoppingItem.getItemName());
        assertEquals("Ovos", mealItem.getFoodName());
        assertEquals(0, mealItem.getCalories().compareTo(new BigDecimal("232.97")));
    }

    @Test
    void applySwapsUpdatesYogurtMealItem() throws Exception {
        User user = User.builder().id(1L).email("u@test.com").name("U").passwordHash("x").build();
        MealPlan mealPlan = MealPlan.builder().id(10L).user(user).planDate(LocalDate.now()).disclaimer("d").build();
        ShoppingList list = ShoppingList.builder().id(5L).user(user).mealPlan(mealPlan)
                .weekStart(LocalDate.now()).weekEnd(LocalDate.now().plusDays(6)).items(new ArrayList<>()).build();

        String swapJson = """
                [{"id":"iogurte_zero","label":"Iogurte zero açúcar","costTier":"MODERATE","kcalEstimate":35,"matchesMealFoods":["Iogurte natural"]}]
                """;
        ShoppingListItem shoppingItem = ShoppingListItem.builder()
                .id(100L)
                .shoppingList(list)
                .itemName("Iogurte natural")
                .quantity("7 potes")
                .category("Bebidas")
                .kcalEstimate(63)
                .swapOptionsJson(swapJson)
                .build();
        list.getItems().add(shoppingItem);

        Meal meal = Meal.builder().id(21L).mealPlan(mealPlan)
                .mealType(br.com.nutriplus.domain.enums.MealType.AFTERNOON_SNACK)
                .name("Lanche").sortOrder(2).build();
        MealItem mealItem = MealItem.builder()
                .id(31L)
                .meal(meal)
                .foodName("Iogurte natural")
                .quantityG(new BigDecimal("170"))
                .calories(new BigDecimal("90"))
                .proteinG(new BigDecimal("8"))
                .carbsG(new BigDecimal("12"))
                .fatG(new BigDecimal("2"))
                .build();

        when(currentUser.get()).thenReturn(user);
        when(shoppingListRepository.findByUserIdWithItemsOrderByCreatedAtDesc(1L)).thenReturn(List.of(list));
        when(mealRepository.findByMealPlanIdOrderBySortOrderAsc(10L)).thenReturn(List.of(meal));
        when(mealItemRepository.findByMealIds(List.of(21L))).thenReturn(List.of(mealItem));
        when(mealLoader.itemsByMealId(List.of(meal))).thenReturn(Map.of(21L, List.of(mealItem)));
        when(responseMapper.toShoppingListResponse(list))
                .thenReturn(new ShoppingListResponse(5L, 10L, LocalDate.now(), LocalDate.now().plusDays(6),
                        List.of(), null, false, null));
        when(responseMapper.toMealPlanResponse(eq(mealPlan), eq(List.of(meal)), any()))
                .thenReturn(new MealPlanResponse(10L, LocalDate.now(), new BigDecimal("50"), null, null, null,
                        "d", List.of(), null, null, null, null, null, null, null, null));
        when(objectMapper.readValue(any(String.class), any(com.fasterxml.jackson.core.type.TypeReference.class)))
                .thenReturn(List.of(new AiShoppingSwapOptionDto(
                        "iogurte_zero", "Iogurte zero açúcar", "MODERATE", null, null, 35,
                        List.of("Iogurte natural")
                )));

        shoppingSwapService.applySwaps(
                new ApplyShoppingSwapsRequest(List.of(new ShoppingSwapSelectionRequest(100L, "iogurte_zero")))
        );

        assertEquals("Iogurte zero açúcar", shoppingItem.getItemName());
        assertEquals("Iogurte zero açúcar", mealItem.getFoodName());
        assertEquals(new BigDecimal("50.00"), mealItem.getCalories());
    }
}

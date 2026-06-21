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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
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
    private ResponseMapper responseMapper;
    @Mock
    private ObjectMapper objectMapper;

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
    }
}

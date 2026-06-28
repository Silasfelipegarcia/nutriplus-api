package br.com.nutriplus.service;

import br.com.nutriplus.dto.response.AppBootstrapResponse;
import br.com.nutriplus.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppBootstrapService {

    private final UserService userService;
    private final NutritionProfileService nutritionProfileService;
    private final MealPlanService mealPlanService;
    private final ShoppingListService shoppingListService;
    private final CheckinService checkinService;
    private final ProgressService progressService;

    public AppBootstrapService(UserService userService,
                               NutritionProfileService nutritionProfileService,
                               MealPlanService mealPlanService,
                               ShoppingListService shoppingListService,
                               CheckinService checkinService,
                               ProgressService progressService) {
        this.userService = userService;
        this.nutritionProfileService = nutritionProfileService;
        this.mealPlanService = mealPlanService;
        this.shoppingListService = shoppingListService;
        this.checkinService = checkinService;
        this.progressService = progressService;
    }

    @Transactional(readOnly = true)
    public AppBootstrapResponse bootstrap() {
        return new AppBootstrapResponse(
                userService.getMe(),
                findOptional(() -> nutritionProfileService.get()),
                findOptional(() -> mealPlanService.getLatest()),
                findOptional(() -> shoppingListService.getLatest()),
                checkinService.getToday(),
                checkinService.getStats(),
                findOptional(() -> progressService.getSchedule()),
                mealPlanService.getGenerationStatus()
        );
    }

    private static <T> T findOptional(java.util.function.Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (ResourceNotFoundException ignored) {
            return null;
        }
    }
}

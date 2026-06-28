package br.com.nutriplus.service;

import br.com.nutriplus.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CacheWarmService {

    private static final Logger log = LoggerFactory.getLogger(CacheWarmService.class);

    private final JwtDecoder jwtDecoder;
    private final UserService userService;
    private final NutritionProfileService nutritionProfileService;
    private final MealPlanService mealPlanService;
    private final ShoppingListService shoppingListService;
    private final CheckinService checkinService;
    private final ProgressService progressService;

    public CacheWarmService(JwtDecoder jwtDecoder,
                            UserService userService,
                            NutritionProfileService nutritionProfileService,
                            MealPlanService mealPlanService,
                            ShoppingListService shoppingListService,
                            CheckinService checkinService,
                            ProgressService progressService) {
        this.jwtDecoder = jwtDecoder;
        this.userService = userService;
        this.nutritionProfileService = nutritionProfileService;
        this.mealPlanService = mealPlanService;
        this.shoppingListService = shoppingListService;
        this.checkinService = checkinService;
        this.progressService = progressService;
    }

    @Async("cacheWarmExecutor")
    public void warmTierSCaches(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            return;
        }
        try {
            Jwt jwt = jwtDecoder.decode(stripBearer(accessToken));
            var context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(new JwtAuthenticationToken(jwt));
            SecurityContextHolder.setContext(context);
            warmCaches();
        } catch (Exception ex) {
            log.debug("Cache warm skipped: {}", ex.getMessage());
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private void warmCaches() {
        userService.getMe();
        warmOptional(nutritionProfileService::get);
        warmOptional(mealPlanService::getLatest);
        warmOptional(shoppingListService::getLatest);
        checkinService.getToday();
        checkinService.getStats();
        warmOptional(progressService::getSchedule);
        mealPlanService.getGenerationStatus();
    }

    private static void warmOptional(java.util.function.Supplier<?> loader) {
        try {
            loader.get();
        } catch (ResourceNotFoundException ignored) {
            // optional Tier S resources
        }
    }

    private static String stripBearer(String token) {
        if (token.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return token.substring(7).trim();
        }
        return token.trim();
    }
}

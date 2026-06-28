package br.com.nutriplus.service;

import br.com.nutriplus.domain.enums.MealPlanGenerationStatus;
import br.com.nutriplus.domain.enums.UserRole;
import br.com.nutriplus.dto.response.AdminPerformanceSummaryResponse;
import br.com.nutriplus.dto.response.AppBootstrapResponse;
import br.com.nutriplus.dto.response.CheckinStatsResponse;
import br.com.nutriplus.dto.response.MealPlanGenerationStatusResponse;
import br.com.nutriplus.dto.response.TodayCheckinsResponse;
import br.com.nutriplus.dto.response.UserResponse;
import br.com.nutriplus.infrastructure.config.RedisCacheConfig;
import br.com.nutriplus.security.AuthorizationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminPerformanceServiceTest {

    @Mock private AuthorizationService authorizationService;
    @Mock private RedisCacheConfig cacheConfig;
    @Mock private Environment environment;
    @Mock private AppBootstrapService appBootstrapService;
    @Mock private UserService userService;
    @Mock private NutritionProfileService nutritionProfileService;
    @Mock private MealPlanService mealPlanService;
    @Mock private ShoppingListService shoppingListService;
    @Mock private CheckinService checkinService;
    @Mock private ProgressService progressService;
    @Mock private FeatureFlagService featureFlagService;

    private AdminPerformanceService service;

    @BeforeEach
    void setUp() {
        service = new AdminPerformanceService(
                authorizationService,
                cacheConfig,
                environment,
                new ObjectMapper(),
                appBootstrapService,
                userService,
                nutritionProfileService,
                mealPlanService,
                shoppingListService,
                checkinService,
                progressService,
                featureFlagService);
    }

    @Test
    void summaryRequiresAdminAndReturnsProbes() {
        when(authorizationService.hasRole(UserRole.ADMIN)).thenReturn(true);
        when(cacheConfig.isEnabled()).thenReturn(true);
        when(environment.getActiveProfiles()).thenReturn(new String[]{"test"});
        when(environment.getProperty("spring.data.redis.host")).thenReturn(null);
        when(featureFlagService.listPublic()).thenReturn(List.of());
        when(userService.getMe()).thenReturn(new UserResponse(
                1L, "Admin", "admin@test.local", null, false, null, null, null, null, null, null, null, null));
        when(appBootstrapService.bootstrap()).thenReturn(new AppBootstrapResponse(
                null, null, null, null, null, null, null, null));
        when(mealPlanService.getGenerationStatus()).thenReturn(
                new MealPlanGenerationStatusResponse(null, MealPlanGenerationStatus.NONE, null, null, null, null, 5));
        when(checkinService.getToday()).thenReturn(new TodayCheckinsResponse(
                List.of(), 0, 0, null, 0, 0, 0, 0, "MAINTAIN", List.of()));
        when(checkinService.getStats()).thenReturn(new CheckinStatsResponse(0, 0));

        AdminPerformanceSummaryResponse summary = service.summary();

        assertThat(summary.cacheEnabled()).isTrue();
        assertThat(summary.endpoints()).isNotEmpty();
        assertThat(summary.criticalFailures()).isZero();
    }
}

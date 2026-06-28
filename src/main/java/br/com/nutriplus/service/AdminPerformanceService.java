package br.com.nutriplus.service;

import br.com.nutriplus.domain.enums.UserRole;
import br.com.nutriplus.dto.response.AdminPerformanceSummaryResponse;
import br.com.nutriplus.dto.response.AdminPerformanceSummaryResponse.EndpointProbe;
import br.com.nutriplus.exception.BusinessException;
import br.com.nutriplus.exception.ResourceNotFoundException;
import br.com.nutriplus.infrastructure.config.RedisCacheConfig;
import br.com.nutriplus.security.AuthorizationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@Service
public class AdminPerformanceService {

    private static final List<String> TIER_S_PATHS = List.of(
            "/health",
            "/feature-flags",
            "/users/me",
            "/app/bootstrap",
            "/nutrition-profile",
            "/meal-plans/generation-status",
            "/meal-plans/latest",
            "/shopping-list/latest",
            "/checkins/today",
            "/checkins/stats",
            "/progress/schedule"
    );

    private final AuthorizationService authorizationService;
    private final RedisCacheConfig cacheConfig;
    private final Environment environment;
    private final ObjectMapper objectMapper;
    private final AppBootstrapService appBootstrapService;
    private final UserService userService;
    private final NutritionProfileService nutritionProfileService;
    private final MealPlanService mealPlanService;
    private final ShoppingListService shoppingListService;
    private final CheckinService checkinService;
    private final ProgressService progressService;
    private final FeatureFlagService featureFlagService;

    public AdminPerformanceService(AuthorizationService authorizationService,
                                   RedisCacheConfig cacheConfig,
                                   Environment environment,
                                   ObjectMapper objectMapper,
                                   AppBootstrapService appBootstrapService,
                                   UserService userService,
                                   NutritionProfileService nutritionProfileService,
                                   MealPlanService mealPlanService,
                                   ShoppingListService shoppingListService,
                                   CheckinService checkinService,
                                   ProgressService progressService,
                                   FeatureFlagService featureFlagService) {
        this.authorizationService = authorizationService;
        this.cacheConfig = cacheConfig;
        this.environment = environment;
        this.objectMapper = objectMapper;
        this.appBootstrapService = appBootstrapService;
        this.userService = userService;
        this.nutritionProfileService = nutritionProfileService;
        this.mealPlanService = mealPlanService;
        this.shoppingListService = shoppingListService;
        this.checkinService = checkinService;
        this.progressService = progressService;
        this.featureFlagService = featureFlagService;
    }

    public AdminPerformanceSummaryResponse summary() {
        requireAdmin();

        int tierSThreshold = readThreshold("tier_s_p95_ms", 200);
        int dashboardThreshold = readThreshold("dashboard_flow_p95_ms", 800);

        List<EndpointProbe> probes = new ArrayList<>();
        probes.add(probe("GET", "/health", "S", "Health check", this::healthProbe));
        probes.add(probe("GET", "/feature-flags", "S", "Feature flags", featureFlagService::listPublic));
        probes.add(probe("GET", "/users/me", "S", "Perfil usuário", userService::getMe));
        probes.add(probe("GET", "/app/bootstrap", "S", "Bootstrap dashboard", appBootstrapService::bootstrap));
        probes.add(probeOptional("GET", "/nutrition-profile", "S", "Perfil nutricional",
                nutritionProfileService::get));
        probes.add(probe("GET", "/meal-plans/generation-status", "S", "Status geração plano",
                mealPlanService::getGenerationStatus));
        probes.add(probeOptional("GET", "/meal-plans/latest", "S", "Plano alimentar",
                mealPlanService::getLatest));
        probes.add(probeOptional("GET", "/shopping-list/latest", "S", "Lista de compras",
                shoppingListService::getLatest));
        probes.add(probe("GET", "/checkins/today", "S", "Check-ins hoje", checkinService::getToday));
        probes.add(probe("GET", "/checkins/stats", "S", "Estatísticas check-in", checkinService::getStats));
        probes.add(probeOptional("GET", "/progress/schedule", "S", "Agenda progresso",
                progressService::getSchedule));

        List<EndpointProbe> tierS = probes.stream().filter(p -> "S".equals(p.tier())).toList();
        double tierSAvg = tierS.stream().mapToLong(EndpointProbe::p95Ms).average().orElse(0);
        double tierSP95 = tierS.stream().mapToLong(EndpointProbe::p95Ms).max().orElse(0);
        double dashboardSum = dashboardFlowSumP95(probes);
        int criticalFailures = (int) probes.stream().filter(EndpointProbe::failed).count();

        String env = environment.getActiveProfiles().length > 0
                ? String.join(",", environment.getActiveProfiles())
                : "default";

        return new AdminPerformanceSummaryResponse(
                Instant.now(),
                env,
                cacheConfig.isEnabled(),
                environment.getProperty("spring.data.redis.host") != null,
                tierSThreshold,
                dashboardThreshold,
                round1(tierSAvg),
                tierSP95,
                dashboardSum,
                criticalFailures,
                probes,
                "docs/PERFORMANCE_BASELINE.md",
                "perf/run-baseline.sh prod"
        );
    }

    private double dashboardFlowSumP95(List<EndpointProbe> probes) {
        return probes.stream()
                .filter(p -> TIER_S_PATHS.contains(p.path()) && !"/health".equals(p.path())
                        && !"/feature-flags".equals(p.path()))
                .mapToLong(EndpointProbe::p95Ms)
                .sum();
    }

    private EndpointProbe probe(String method, String path, String tier, String description,
                                Supplier<?> action) {
        long[] samples = sample(action, 3);
        long p95 = percentile(samples, 0.95);
        int status = samples.length > 0 && samples[0] >= 0 ? 200 : 500;
        String grade = status >= 500 ? "FALHA SERVER" : "OK";
        boolean failed = status >= 500;
        boolean slow = p95 > readThreshold("tier_s_p95_ms", 200);
        return new EndpointProbe(method, path, tier, description, status, p95, grade, slow, failed);
    }

    private EndpointProbe probeOptional(String method, String path, String tier, String description,
                                          Supplier<?> action) {
        long[] samples = sampleOptional(action, 3);
        long p95 = percentile(samples, 0.95);
        int status = samples.length == 0 ? 404 : (samples[0] >= 0 ? 200 : 500);
        String grade = status == 404 ? "OK (404 esperado)" : status >= 500 ? "FALHA SERVER" : "OK";
        boolean failed = status >= 500;
        boolean slow = status == 200 && p95 > readThreshold("tier_s_p95_ms", 200);
        return new EndpointProbe(method, path, tier, description, status, p95, grade, slow, failed);
    }

    private long[] sample(Supplier<?> action, int count) {
        long[] latencies = new long[count];
        for (int i = 0; i < count; i++) {
            latencies[i] = timed(action);
        }
        return latencies;
    }

    private long[] sampleOptional(Supplier<?> action, int count) {
        long[] latencies = new long[count];
        for (int i = 0; i < count; i++) {
            try {
                latencies[i] = timed(action);
            } catch (ResourceNotFoundException e) {
                return new long[]{404};
            }
        }
        return latencies;
    }

    private long timed(Supplier<?> action) {
        long start = System.nanoTime();
        try {
            action.get();
            return (System.nanoTime() - start) / 1_000_000;
        } catch (Exception e) {
            return -1;
        }
    }

    private long percentile(long[] values, double pct) {
        if (values.length == 0) {
            return 0;
        }
        long[] sorted = java.util.Arrays.stream(values).filter(v -> v >= 0).sorted().toArray();
        if (sorted.length == 0) {
            return 0;
        }
        int idx = Math.min(sorted.length - 1, (int) (sorted.length * pct));
        return sorted[idx];
    }

    private int readThreshold(String key, int fallback) {
        try {
            JsonNode root = objectMapper.readTree(new ClassPathResource("perf/baseline-gates.json").getInputStream());
            JsonNode thresholds = root.path("thresholds");
            if (thresholds.has(key)) {
                return thresholds.get(key).asInt(fallback);
            }
        } catch (Exception ignored) {
        }
        return fallback;
    }

    private Object healthProbe() {
        return "ok";
    }

    private double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private void requireAdmin() {
        if (!authorizationService.hasRole(UserRole.ADMIN)) {
            throw new BusinessException("Acesso restrito a administradores.");
        }
    }
}

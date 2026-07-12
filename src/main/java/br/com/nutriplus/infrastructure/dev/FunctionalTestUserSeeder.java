package br.com.nutriplus.infrastructure.dev;

import br.com.nutriplus.domain.entity.Meal;
import br.com.nutriplus.domain.entity.MealPlan;
import br.com.nutriplus.domain.entity.NutritionProfile;
import br.com.nutriplus.domain.entity.User;
import br.com.nutriplus.domain.entity.UserTrainingActivity;
import br.com.nutriplus.domain.enums.*;
import br.com.nutriplus.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Cria usuários do catálogo {@link DevTestUserSpec} — usado no boot local/dev e nos testes de integração.
 */
@Component
public class FunctionalTestUserSeeder {

    private final UserRepository userRepository;
    private final NutritionProfileRepository nutritionProfileRepository;
    private final MealPlanRepository mealPlanRepository;
    private final MealRepository mealRepository;
    private final MealItemRepository mealItemRepository;
    private final UserTrainingActivityRepository trainingActivityRepository;
    private final PasswordEncoder passwordEncoder;

    public FunctionalTestUserSeeder(UserRepository userRepository,
                                    NutritionProfileRepository nutritionProfileRepository,
                                    MealPlanRepository mealPlanRepository,
                                    MealRepository mealRepository,
                                    MealItemRepository mealItemRepository,
                                    UserTrainingActivityRepository trainingActivityRepository,
                                    PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.nutritionProfileRepository = nutritionProfileRepository;
        this.mealPlanRepository = mealPlanRepository;
        this.mealRepository = mealRepository;
        this.mealItemRepository = mealItemRepository;
        this.trainingActivityRepository = trainingActivityRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void seedAllDevUsers() {
        for (DevTestUserSpec spec : DevTestUserSpec.values()) {
            if (userRepository.findByEmail(spec.email()).isEmpty()) {
                seedSpec(spec, spec.email());
            }
        }
    }

    public User seedSpec(DevTestUserSpec spec) {
        return seedSpec(spec, spec.email());
    }

    @Transactional
    public User seedSpec(DevTestUserSpec spec, String email) {
        var existing = userRepository.findByEmail(email);
        if (existing.isPresent()) {
            if (email.endsWith("@func.nutriplus.test")) {
                User user = existing.get();
                clearNutritionData(user);
                applyUserFields(user, spec);
                user = userRepository.save(user);
                if (spec.withProfile()) {
                    attachProfileAndPlan(user, spec);
                }
                return user;
            }
            return existing.get();
        }

        User user = User.builder()
                .name(spec.displayName())
                .email(email)
                .role(spec.adminRole() ? UserRole.ADMIN : UserRole.PATIENT)
                .passwordHash(passwordEncoder.encode(DevDataLoader.TEST_PASSWORD))
                .loginEnabled(true)
                .loginEnabledAt(LocalDateTime.now())
                .build();
        applyUserFields(user, spec);
        user = userRepository.save(user);

        if (spec.withProfile()) {
            attachProfileAndPlan(user, spec);
        }

        return user;
    }

    private void applyUserFields(User user, DevTestUserSpec spec) {
        user.setName(spec.displayName());
        user.setRole(spec.adminRole() ? UserRole.ADMIN : UserRole.PATIENT);
        user.setSubscriptionPlan(spec.subscriptionPlan());
        user.setPlanValidUntil(spec.planValidUntil());
        user.setTrialAte(spec.trialUntil());
        user.setTrialUtilizado(spec.trialUntil() != null);
        user.setAutoRenew(spec.autoRenew());
    }

    private void clearNutritionData(User user) {
        Long userId = user.getId();
        trainingActivityRepository.deleteByUserId(userId);
        for (MealPlan plan : mealPlanRepository.findByUserIdOrderByCreatedAtDesc(userId)) {
            for (Meal meal : mealRepository.findByMealPlanIdOrderBySortOrderAsc(plan.getId())) {
                mealItemRepository.deleteAll(mealItemRepository.findByMealIdOrderByIdAsc(meal.getId()));
                mealRepository.delete(meal);
            }
            mealPlanRepository.delete(plan);
        }
        nutritionProfileRepository.deleteByUserId(userId);
    }

    private void attachProfileAndPlan(User user, DevTestUserSpec spec) {
        LocalDateTime profileCreatedAt = spec.profileAgeDays() > 0
                ? LocalDateTime.now().minusDays(spec.profileAgeDays())
                : LocalDateTime.now();

        NutritionProfile profile = nutritionProfileRepository.save(buildProfile(user, spec, profileCreatedAt));

        if (spec.profileAgeDays() > 0) {
            nutritionProfileRepository.backdateCreatedAt(profile.getId(), profileCreatedAt);
        }

        if (spec.athleteMode()) {
            trainingActivityRepository.save(new UserTrainingActivity(
                    user, SportType.RUNNING, 3, 45));
        }

        if (spec.withStubPlan()) {
            LocalDateTime planCreatedAt = spec.planAgeDays() > 0
                    ? LocalDateTime.now().minusDays(spec.planAgeDays())
                    : LocalDateTime.now();
            DevStubMealPlanFactory.createStubPlan(
                    mealPlanRepository, mealRepository, mealItemRepository,
                    user, profile, planCreatedAt);

            if (spec.planRegenLockedUntil() != null) {
                profile.setPlanRegenLockedUntil(spec.planRegenLockedUntil());
            }
            if (spec.oneTimeCorrectionUsed()) {
                profile.setOneTimeCorrectionUsedAt(LocalDateTime.now().minusDays(5));
            }
            if (spec.athleteRegenEligible()) {
                profile.setAthleteRegenEligible(true);
            }
            nutritionProfileRepository.save(profile);
        }
    }

    private NutritionProfile buildProfile(User user, DevTestUserSpec spec, LocalDateTime createdAt) {
        return NutritionProfile.builder()
                .user(user)
                .age(spec.age())
                .sex(spec.sex())
                .heightCm(new BigDecimal("175.00"))
                .currentWeightKg(new BigDecimal("80.00"))
                .targetWeightKg(new BigDecimal("75.00"))
                .goal(Goal.LOSE_WEIGHT)
                .activityLevel(spec.athleteMode() ? ActivityLevel.INTENSE : ActivityLevel.MODERATE)
                .dietaryPreference(spec.dietaryPreference())
                .restriction(Restriction.NONE)
                .agentPersona(spec.agentPersona())
                .foodLikes(spec.foodLikes())
                .foodDislikes("fígado, jiló")
                .mealNotes(spec.mealNotes())
                .foodBudgetLevel(FoodBudgetLevel.MODERATE)
                .bmrKcal(new BigDecimal("1750.00"))
                .tdeeKcal(new BigDecimal("2400.00"))
                .targetCalories(new BigDecimal("1900.00"))
                .targetProteinG(new BigDecimal("150.00"))
                .targetCarbsG(new BigDecimal("190.00"))
                .targetFatG(new BigDecimal("63.00"))
                .hungerPattern(HungerPattern.BALANCED)
                .athleteModeEnabled(spec.athleteMode())
                .trainingDailyExtraKcal(spec.athleteMode() ? new BigDecimal("350.00") : null)
                .createdAt(createdAt)
                .build();
    }
}

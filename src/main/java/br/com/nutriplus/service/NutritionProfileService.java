package br.com.nutriplus.service;

import br.com.nutriplus.client.AiAgentClient;
import br.com.nutriplus.client.dto.AiNutritionCalculateResponse;
import br.com.nutriplus.domain.entity.NutritionProfile;
import br.com.nutriplus.domain.entity.User;
import br.com.nutriplus.domain.enums.AiRequestStatus;
import br.com.nutriplus.domain.enums.AiRequestType;
import br.com.nutriplus.domain.enums.CalculationMethod;
import br.com.nutriplus.dto.request.NutritionProfileRequest;
import br.com.nutriplus.dto.response.NutritionProfileResponse;
import br.com.nutriplus.domain.util.AgePolicy;
import br.com.nutriplus.exception.BusinessException;
import br.com.nutriplus.exception.ResourceNotFoundException;
import br.com.nutriplus.mapper.ResponseMapper;
import br.com.nutriplus.repository.NutritionProfileRepository;
import br.com.nutriplus.security.CurrentUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import br.com.nutriplus.infrastructure.config.NutriCacheNames;

import java.time.LocalTime;
import java.time.LocalDate;
import java.time.Period;
import java.math.BigDecimal;
import br.com.nutriplus.util.TimeInputNormalizer;

@Service
public class NutritionProfileService {

    private final CurrentUser currentUser;
    private final NutritionProfileRepository nutritionProfileRepository;
    private final TrainingService trainingService;
    private final AiAgentClient aiAgentClient;
    private final AiRequestLogService aiRequestLogService;
    private final ResponseMapper responseMapper;
    private final ObjectMapper objectMapper;
    private final SubscriptionService subscriptionService;
    private final HealthEligibilityService healthEligibilityService;
    private final ProgressService progressService;
    private final HydrationTargetService hydrationTargetService;

    public NutritionProfileService(CurrentUser currentUser,
                                   NutritionProfileRepository nutritionProfileRepository,
                                   TrainingService trainingService,
                                   AiAgentClient aiAgentClient,
                                   AiRequestLogService aiRequestLogService,
                                   ResponseMapper responseMapper,
                                   ObjectMapper objectMapper,
                                   SubscriptionService subscriptionService,
                                   HealthEligibilityService healthEligibilityService,
                                   @Lazy ProgressService progressService,
                                   HydrationTargetService hydrationTargetService) {
        this.currentUser = currentUser;
        this.nutritionProfileRepository = nutritionProfileRepository;
        this.trainingService = trainingService;
        this.aiAgentClient = aiAgentClient;
        this.aiRequestLogService = aiRequestLogService;
        this.responseMapper = responseMapper;
        this.objectMapper = objectMapper;
        this.subscriptionService = subscriptionService;
        this.healthEligibilityService = healthEligibilityService;
        this.progressService = progressService;
        this.hydrationTargetService = hydrationTargetService;
    }

    @Transactional
    @CacheEvict(value = {NutriCacheNames.NUTRITION_PROFILE, NutriCacheNames.USER_ME}, keyGenerator = "userIdCacheKeyGenerator")
    public NutritionProfileResponse saveOrUpdate(NutritionProfileRequest request) {
        User user = currentUser.get();
        long start = System.currentTimeMillis();

        NutritionProfile profile = nutritionProfileRepository.findByUserId(user.getId())
                .orElse(NutritionProfile.builder().user(user).build());

        BigDecimal previousWeight = profile.getCurrentWeightKg();
        applyRequest(profile, request);
        trainingService.syncTrainingDailyExtra(profile);

        String requestJson = toJson(request);
        try {
            AiNutritionCalculateResponse macros = aiAgentClient.calculateMacros(profile);
            profile.setBmrKcal(macros.bmrKcal());
            profile.setTdeeKcal(macros.tdeeKcal());
            profile.setTargetCalories(macros.targetCalories());
            profile.setTargetProteinG(macros.targetProteinG());
            profile.setTargetCarbsG(macros.targetCarbsG());
            profile.setTargetFatG(macros.targetFatG());
            if (macros.leanMassKg() != null) {
                profile.setLeanMassKg(macros.leanMassKg());
            }
            profile.setPaceWarning(macros.paceWarning());
            profile.setEstimatedWeeklyRateKg(macros.estimatedWeeklyRateKg());
            hydrationTargetService.syncHydrationTarget(profile);

            profile = nutritionProfileRepository.save(profile);

            BigDecimal newWeight = profile.getCurrentWeightKg();
            if (newWeight != null && (previousWeight == null || previousWeight.compareTo(newWeight) != 0)) {
                progressService.syncProfileWeightFromEdit(user.getId(), newWeight);
            }

            aiRequestLogService.log(user, AiRequestType.CALCULATE_MACROS, requestJson,
                    toJson(macros), AiRequestStatus.SUCCESS, null,
                    (int) (System.currentTimeMillis() - start));

            return toProfileResponse(profile, user);
        } catch (Exception e) {
            aiRequestLogService.log(user, AiRequestType.CALCULATE_MACROS, requestJson,
                    null, AiRequestStatus.ERROR, e.getMessage(),
                    (int) (System.currentTimeMillis() - start));
            throw e;
        }
    }

    @Cacheable(value = NutriCacheNames.NUTRITION_PROFILE, keyGenerator = "userIdCacheKeyGenerator")
    public NutritionProfileResponse get() {
        User user = currentUser.get();
        NutritionProfile profile = nutritionProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Perfil nutricional não encontrado"));
        profile = ensureHydrationTargetSynced(profile);
        return toProfileResponse(profile, user);
    }

    private NutritionProfile ensureHydrationTargetSynced(NutritionProfile profile) {
        Integer computed = hydrationTargetService.computeDailyWaterTargetMl(profile);
        if (java.util.Objects.equals(profile.getDailyWaterTargetMl(), computed)) {
            return profile;
        }
        profile.setDailyWaterTargetMl(computed);
        return nutritionProfileRepository.save(profile);
    }

    private NutritionProfileResponse toProfileResponse(NutritionProfile profile, User user) {
        return responseMapper.toNutritionProfileResponse(profile, subscriptionService.montarStatus(user));
    }

    public NutritionProfile getEntityForUser(User user) {
        return nutritionProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Complete o onboarding nutricional antes de continuar"));
    }

    private void applyRequest(NutritionProfile profile, NutritionProfileRequest request) {
        int resolvedAge = resolveAge(request);
        if (request.birthDate() != null) {
            AgePolicy.requireAdult(request.birthDate());
        } else if (resolvedAge < AgePolicy.MIN_AGE || resolvedAge > AgePolicy.MAX_AGE) {
            if (resolvedAge < AgePolicy.MIN_AGE) {
                throw new BusinessException("Você precisa ter pelo menos 18 anos.");
            }
            throw new BusinessException("Informe uma data de nascimento válida.");
        }
        if (request.birthDate() != null) {
            profile.setBirthDate(request.birthDate());
            profile.setAge(resolvedAge);
        } else {
            profile.setAge(request.age());
        }
        profile.setStateCode(request.stateCode());
        profile.setCity(request.city());
        if (request.chewingDifficulty() != null) {
            profile.setChewingDifficulty(request.chewingDifficulty());
        }
        if (request.seniorWeightLossAck() != null) {
            profile.setSeniorWeightLossAck(request.seniorWeightLossAck());
        }
        profile.setSex(request.sex());
        profile.setHeightCm(request.heightCm());
        profile.setCurrentWeightKg(request.currentWeightKg());
        profile.setTargetWeightKg(request.targetWeightKg());
        profile.setGoalTargetWeeks(request.goalTargetWeeks());
        profile.setGoal(request.goal());
        profile.setActivityLevel(request.activityLevel());
        profile.setDietaryPreference(request.dietaryPreference());
        profile.setRestriction(request.restriction());
        profile.setAgentPersona(request.agentPersona());
        profile.setFoodLikes(request.foodLikes());
        profile.setFoodDislikes(request.foodDislikes());
        profile.setMealNotes(request.mealNotes());
        if (request.eatsBreakfast() != null) {
            profile.setEatsBreakfast(request.eatsBreakfast());
        }
        if (request.eatsLunch() != null) {
            profile.setEatsLunch(request.eatsLunch());
        }
        if (request.eatsAfternoonSnack() != null) {
            profile.setEatsAfternoonSnack(request.eatsAfternoonSnack());
        }
        if (request.eatsDinner() != null) {
            profile.setEatsDinner(request.eatsDinner());
        }
        if (request.openToRoutineAdjustment() != null) {
            profile.setOpenToRoutineAdjustment(request.openToRoutineAdjustment());
        }
        if (request.freeExtras() != null) {
            profile.setFreeExtrasJson(toJson(request.freeExtras()));
        }
        if (request.hungerPattern() != null) {
            profile.setHungerPattern(request.hungerPattern());
        }
        if (request.nutritionMode() != null) {
            profile.setNutritionMode(request.nutritionMode());
        }
        profile.setFoodBudgetLevel(request.resolvedFoodBudgetLevel());
        applyCalculationInputs(profile, request);
        profile.setMuscleMassKg(request.muscleMassKg());
        profile.setLeanMassKg(null);
        profile.setWakeTime(parseTime(request.wakeTime()));
        profile.setSleepTime(parseTime(request.sleepTime()));
        profile.setHealthConditions(request.healthConditions());
        profile.setMedications(request.medications());
        profile.setAllergies(request.allergies());
        profile.setHealthNotes(request.healthNotes());
        healthEligibilityService.evaluateAndApply(
                profile,
                request.pregnancyStatus(),
                request.eatingDisorderRisk(),
                request.severeRenalRestriction()
        );
    }

    private int resolveAge(NutritionProfileRequest request) {
        if (request.birthDate() != null) {
            return Period.between(request.birthDate(), LocalDate.now()).getYears();
        }
        return request.age();
    }

    private void applyCalculationInputs(NutritionProfile profile, NutritionProfileRequest request) {
        CalculationMethod method = request.resolvedCalculationMethod();
        profile.setCalculationMethod(method);
        profile.setLeanMassKg(null);

        switch (method) {
            case MANUAL_BMR -> {
                if (request.manualBmrKcal() == null) {
                    throw new BusinessException("Informe a taxa metabólica basal (kcal) da bioimpedância.");
                }
                profile.setManualBmrKcal(request.manualBmrKcal());
                profile.setBodyFatPercent(request.bodyFatPercent());
            }
            case BIOIMPEDANCE -> {
                profile.setManualBmrKcal(null);
                if (request.bodyFatPercent() == null) {
                    throw new BusinessException("Informe o % de gordura da bioimpedância.");
                }
                profile.setBodyFatPercent(request.bodyFatPercent());
            }
            default -> {
                profile.setManualBmrKcal(null);
                profile.setBodyFatPercent(request.bodyFatPercent());
            }
        }
    }

    private LocalTime parseTime(String value) {
        return TimeInputNormalizer.parseFlexible(value);
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "{}";
        }
    }
}

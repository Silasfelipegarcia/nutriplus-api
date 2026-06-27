package br.com.nutriplus.service;

import br.com.nutriplus.client.AiAgentClient;
import br.com.nutriplus.client.dto.AiNutritionCalculateResponse;
import br.com.nutriplus.domain.entity.NutritionProfile;
import br.com.nutriplus.domain.entity.User;
import br.com.nutriplus.domain.enums.AiRequestStatus;
import br.com.nutriplus.domain.enums.AiRequestType;
import br.com.nutriplus.dto.request.NutritionProfileRequest;
import br.com.nutriplus.dto.response.NutritionProfileResponse;
import br.com.nutriplus.domain.util.AgePolicy;
import br.com.nutriplus.exception.BusinessException;
import br.com.nutriplus.exception.ResourceNotFoundException;
import br.com.nutriplus.mapper.ResponseMapper;
import br.com.nutriplus.repository.NutritionProfileRepository;
import br.com.nutriplus.security.CurrentUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import br.com.nutriplus.infrastructure.config.NutriCacheNames;

import java.time.LocalTime;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;

@Service
public class NutritionProfileService {

    private final CurrentUser currentUser;
    private final NutritionProfileRepository nutritionProfileRepository;
    private final AiAgentClient aiAgentClient;
    private final AiRequestLogService aiRequestLogService;
    private final ResponseMapper responseMapper;
    private final ObjectMapper objectMapper;

    public NutritionProfileService(CurrentUser currentUser,
                                   NutritionProfileRepository nutritionProfileRepository,
                                   AiAgentClient aiAgentClient,
                                   AiRequestLogService aiRequestLogService,
                                   ResponseMapper responseMapper,
                                   ObjectMapper objectMapper) {
        this.currentUser = currentUser;
        this.nutritionProfileRepository = nutritionProfileRepository;
        this.aiAgentClient = aiAgentClient;
        this.aiRequestLogService = aiRequestLogService;
        this.responseMapper = responseMapper;
        this.objectMapper = objectMapper;
    }

    @Transactional
    @CacheEvict(value = {NutriCacheNames.NUTRITION_PROFILE, NutriCacheNames.USER_ME}, keyGenerator = "userIdCacheKeyGenerator")
    public NutritionProfileResponse saveOrUpdate(NutritionProfileRequest request) {
        User user = currentUser.get();
        long start = System.currentTimeMillis();

        NutritionProfile profile = nutritionProfileRepository.findByUserId(user.getId())
                .orElse(NutritionProfile.builder().user(user).build());

        applyRequest(profile, request);

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

            profile = nutritionProfileRepository.save(profile);

            aiRequestLogService.log(user, AiRequestType.CALCULATE_MACROS, requestJson,
                    toJson(macros), AiRequestStatus.SUCCESS, null,
                    (int) (System.currentTimeMillis() - start));

            return responseMapper.toNutritionProfileResponse(profile);
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
        return responseMapper.toNutritionProfileResponse(profile);
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
        profile.setFoodBudgetLevel(request.resolvedFoodBudgetLevel());
        profile.setCalculationMethod(request.resolvedCalculationMethod());
        profile.setBodyFatPercent(request.bodyFatPercent());
        profile.setMuscleMassKg(request.muscleMassKg());
        profile.setLeanMassKg(null);
        profile.setWakeTime(parseTime(request.wakeTime()));
        profile.setSleepTime(parseTime(request.sleepTime()));
        profile.setHealthConditions(request.healthConditions());
        profile.setMedications(request.medications());
        profile.setAllergies(request.allergies());
        profile.setHealthNotes(request.healthNotes());
    }

    private int resolveAge(NutritionProfileRequest request) {
        if (request.birthDate() != null) {
            return Period.between(request.birthDate(), LocalDate.now()).getYears();
        }
        return request.age();
    }

    private LocalTime parseTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalTime.parse(value, DateTimeFormatter.ofPattern("HH:mm"));
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "{}";
        }
    }
}

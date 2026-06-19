package br.com.nutriplus.service;

import br.com.nutriplus.client.AiAgentClient;
import br.com.nutriplus.client.dto.AiNutritionCalculateResponse;
import br.com.nutriplus.domain.entity.NutritionProfile;
import br.com.nutriplus.domain.entity.User;
import br.com.nutriplus.domain.enums.AiRequestStatus;
import br.com.nutriplus.domain.enums.AiRequestType;
import br.com.nutriplus.dto.request.NutritionProfileRequest;
import br.com.nutriplus.dto.response.NutritionProfileResponse;
import br.com.nutriplus.exception.ResourceNotFoundException;
import br.com.nutriplus.mapper.ResponseMapper;
import br.com.nutriplus.repository.NutritionProfileRepository;
import br.com.nutriplus.security.CurrentUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        profile.setAge(request.age());
        profile.setSex(request.sex());
        profile.setHeightCm(request.heightCm());
        profile.setCurrentWeightKg(request.currentWeightKg());
        profile.setTargetWeightKg(request.targetWeightKg());
        profile.setGoal(request.goal());
        profile.setActivityLevel(request.activityLevel());
        profile.setDietaryPreference(request.dietaryPreference());
        profile.setRestriction(request.restriction());
        profile.setAgentPersona(request.agentPersona());
        profile.setFoodLikes(request.foodLikes());
        profile.setFoodDislikes(request.foodDislikes());
        profile.setMealNotes(request.mealNotes());
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "{}";
        }
    }
}

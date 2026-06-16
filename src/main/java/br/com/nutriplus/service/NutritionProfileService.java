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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NutritionProfileService {

    private final CurrentUser currentUser;
    private final NutritionProfileRepository nutritionProfileRepository;
    private final AiAgentClient aiAgentClient;
    private final AiRequestLogService aiRequestLogService;
    private final ResponseMapper responseMapper;
    private final ObjectMapper objectMapper;

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
            profile.setBmrKcal(macros.getBmrKcal());
            profile.setTdeeKcal(macros.getTdeeKcal());
            profile.setTargetCalories(macros.getTargetCalories());
            profile.setTargetProteinG(macros.getTargetProteinG());
            profile.setTargetCarbsG(macros.getTargetCarbsG());
            profile.setTargetFatG(macros.getTargetFatG());

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
        profile.setAge(request.getAge());
        profile.setSex(request.getSex());
        profile.setHeightCm(request.getHeightCm());
        profile.setCurrentWeightKg(request.getCurrentWeightKg());
        profile.setTargetWeightKg(request.getTargetWeightKg());
        profile.setGoal(request.getGoal());
        profile.setActivityLevel(request.getActivityLevel());
        profile.setDietaryPreference(request.getDietaryPreference());
        profile.setRestriction(request.getRestriction());
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "{}";
        }
    }
}

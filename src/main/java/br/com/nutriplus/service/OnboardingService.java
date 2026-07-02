package br.com.nutriplus.service;

import br.com.nutriplus.dto.request.OnboardingCompleteRequest;
import br.com.nutriplus.dto.request.TrainingProfileRequest;
import br.com.nutriplus.dto.response.NutritionProfileResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OnboardingService {

    private final NutritionProfileService nutritionProfileService;
    private final TrainingService trainingService;

    public OnboardingService(NutritionProfileService nutritionProfileService,
                             TrainingService trainingService) {
        this.nutritionProfileService = nutritionProfileService;
        this.trainingService = trainingService;
    }

    /**
     * Orquestra onboarding: salva perfil nutricional, depois treino (se atleta) e recalcula macros uma vez com treino.
     * Sequência obrigatória para modo atleta — ver docs/ONBOARDING.md.
     */
    @Transactional
    public NutritionProfileResponse complete(OnboardingCompleteRequest request) {
        NutritionProfileResponse profile = nutritionProfileService.saveOrUpdate(request.nutritionProfile());

        if (Boolean.TRUE.equals(request.athleteModeEnabled())) {
            List<br.com.nutriplus.dto.request.TrainingActivityRequest> activities =
                    request.activities() != null ? request.activities() : List.of();
            trainingService.saveProfile(new TrainingProfileRequest(true, activities, null, null));
            return trainingService.applyToPlan();
        }

        trainingService.saveProfile(new TrainingProfileRequest(false, List.of(), null, null));
        return profile;
    }
}

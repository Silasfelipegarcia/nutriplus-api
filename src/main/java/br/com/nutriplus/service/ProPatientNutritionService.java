package br.com.nutriplus.service;

import br.com.nutriplus.domain.entity.Nutritionist;
import br.com.nutriplus.domain.entity.NutritionProfile;
import br.com.nutriplus.domain.entity.User;
import br.com.nutriplus.dto.request.ProPatientNutritionUpdateRequest;
import br.com.nutriplus.dto.response.NutritionProfileResponse;
import br.com.nutriplus.exception.ResourceNotFoundException;
import br.com.nutriplus.infrastructure.config.NutriCacheNames;
import br.com.nutriplus.mapper.ResponseMapper;
import br.com.nutriplus.repository.NutritionProfileRepository;
import br.com.nutriplus.repository.UserRepository;
import br.com.nutriplus.security.AuthorizationService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProPatientNutritionService {

    private final AuthorizationService authorizationService;
    private final NutritionProfileRepository nutritionProfileRepository;
    private final UserRepository userRepository;
    private final ResponseMapper responseMapper;
    private final AuditLogService auditLogService;

    public ProPatientNutritionService(AuthorizationService authorizationService,
                                      NutritionProfileRepository nutritionProfileRepository,
                                      UserRepository userRepository,
                                      ResponseMapper responseMapper,
                                      AuditLogService auditLogService) {
        this.authorizationService = authorizationService;
        this.nutritionProfileRepository = nutritionProfileRepository;
        this.userRepository = userRepository;
        this.responseMapper = responseMapper;
        this.auditLogService = auditLogService;
    }

    @Transactional
    @CacheEvict(value = NutriCacheNames.NUTRITION_PROFILE, key = "#patientId")
    public NutritionProfileResponse updateForPatient(Long patientId, ProPatientNutritionUpdateRequest request) {
        authorizationService.requireCareAccessForNutritionistByPatientId(patientId);
        NutritionProfile profile = nutritionProfileRepository.findByUserId(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil nutricional não encontrado."));
        if (request.goal() != null) {
            profile.setGoal(request.goal());
        }
        if (request.dietaryPreference() != null) {
            profile.setDietaryPreference(request.dietaryPreference());
        }
        if (request.restriction() != null) {
            profile.setRestriction(request.restriction());
        }
        if (request.agentPersona() != null) {
            profile.setAgentPersona(request.agentPersona());
        }
        if (request.mealNotes() != null) {
            profile.setMealNotes(request.mealNotes());
        }
        if (request.healthNotes() != null) {
            profile.setHealthNotes(request.healthNotes());
        }
        profile = nutritionProfileRepository.save(profile);
        User patient = userRepository.findById(patientId).orElseThrow();
        auditLogService.log("PRO_UPDATE_NUTRITION_PROFILE", "NUTRITION_PROFILE", patient);
        return responseMapper.toNutritionProfileResponse(profile);
    }
}

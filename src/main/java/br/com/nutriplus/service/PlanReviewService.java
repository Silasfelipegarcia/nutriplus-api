package br.com.nutriplus.service;

import br.com.nutriplus.domain.entity.Meal;
import br.com.nutriplus.domain.entity.MealPlan;
import br.com.nutriplus.domain.entity.Nutritionist;
import br.com.nutriplus.domain.entity.PlanRevision;
import br.com.nutriplus.domain.enums.PlanSource;
import br.com.nutriplus.dto.request.PublishMealPlanRequest;
import br.com.nutriplus.dto.response.MealPlanResponse;
import br.com.nutriplus.exception.ResourceNotFoundException;
import br.com.nutriplus.mapper.ResponseMapper;
import br.com.nutriplus.repository.MealPlanRepository;
import br.com.nutriplus.repository.PlanRevisionRepository;
import br.com.nutriplus.security.AuthorizationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class PlanReviewService {

    private final AuthorizationService authorizationService;
    private final MealPlanRepository mealPlanRepository;
    private final PlanRevisionRepository planRevisionRepository;
    private final MealLoader mealLoader;
    private final ResponseMapper responseMapper;
    private final ObjectMapper objectMapper;

    public PlanReviewService(AuthorizationService authorizationService,
                             MealPlanRepository mealPlanRepository,
                             PlanRevisionRepository planRevisionRepository,
                             MealLoader mealLoader,
                             ResponseMapper responseMapper,
                             ObjectMapper objectMapper) {
        this.authorizationService = authorizationService;
        this.mealPlanRepository = mealPlanRepository;
        this.planRevisionRepository = planRevisionRepository;
        this.mealLoader = mealLoader;
        this.responseMapper = responseMapper;
        this.objectMapper = objectMapper;
    }

    public List<MealPlanResponse> listPatientMealPlans(Long patientId) {
        authorizationService.requireCareAccessForNutritionistByPatientId(patientId);
        List<MealPlan> plans = mealPlanRepository.findByUserIdOrderByCreatedAtDesc(patientId);
        if (plans.isEmpty()) {
            return List.of();
        }
        Map<Long, List<Meal>> mealsByPlanId =
                mealLoader.mealsByPlanIds(plans.stream().map(MealPlan::getId).toList());
        return plans.stream()
                .map(plan -> {
                    var meals = mealsByPlanId.getOrDefault(plan.getId(), List.of());
                    var items = mealLoader.itemsByMealId(meals);
                    return responseMapper.toMealPlanResponse(plan, meals, items);
                })
                .toList();
    }

    @Transactional
    public MealPlanResponse publishMealPlan(Long patientId, Long mealPlanId, PublishMealPlanRequest request) {
        authorizationService.requireCareAccessForNutritionistByPatientId(patientId);
        Nutritionist nutritionist = authorizationService.requireNutritionist();

        MealPlan plan = mealPlanRepository.findById(mealPlanId)
                .orElseThrow(() -> new ResourceNotFoundException("Plano não encontrado"));
        if (!plan.getUser().getId().equals(patientId)) {
            throw new ResourceNotFoundException("Plano não pertence ao paciente.");
        }

        plan.setPlanSource(PlanSource.NUTRITIONIST_APPROVED);
        plan.setNutritionist(nutritionist);
        if (request.reviewNotes() != null) {
            plan.setMedicalReviewNotes(request.reviewNotes());
        }
        plan.setMedicalReviewStatus("APPROVED");
        mealPlanRepository.save(plan);

        planRevisionRepository.save(PlanRevision.publish(plan, nutritionist, buildChangesJson(request)));

        var meals = mealLoader.mealsForPlan(plan.getId());
        var items = mealLoader.itemsByMealId(meals);
        return responseMapper.toMealPlanResponse(plan, meals, items);
    }

    private String buildChangesJson(PublishMealPlanRequest request) {
        try {
            Map<String, String> payload = new java.util.LinkedHashMap<>();
            if (request.reviewNotes() != null && !request.reviewNotes().isBlank()) {
                payload.put("notes", request.reviewNotes().trim());
            }
            if (request.changesSummary() != null && !request.changesSummary().isBlank()) {
                payload.put("changesSummary", request.changesSummary().trim());
            }
            return payload.isEmpty() ? "{}" : objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
}

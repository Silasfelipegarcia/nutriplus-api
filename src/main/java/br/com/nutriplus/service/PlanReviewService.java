package br.com.nutriplus.service;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PlanReviewService {

    private final AuthorizationService authorizationService;
    private final MealPlanRepository mealPlanRepository;
    private final PlanRevisionRepository planRevisionRepository;
    private final MealLoader mealLoader;
    private final ResponseMapper responseMapper;

    public PlanReviewService(AuthorizationService authorizationService,
                             MealPlanRepository mealPlanRepository,
                             PlanRevisionRepository planRevisionRepository,
                             MealLoader mealLoader,
                             ResponseMapper responseMapper) {
        this.authorizationService = authorizationService;
        this.mealPlanRepository = mealPlanRepository;
        this.planRevisionRepository = planRevisionRepository;
        this.mealLoader = mealLoader;
        this.responseMapper = responseMapper;
    }

    public List<MealPlanResponse> listPatientMealPlans(Long patientId) {
        authorizationService.requireCareAccessForNutritionistByPatientId(patientId);
        return mealPlanRepository.findByUserIdOrderByCreatedAtDesc(patientId).stream()
                .map(plan -> {
                    var meals = mealLoader.mealsForPlan(plan.getId());
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

        planRevisionRepository.save(PlanRevision.publish(plan, nutritionist,
                request.reviewNotes() != null ? "{\"notes\":\"" + request.reviewNotes().replace("\"", "\\\"") + "\"}" : "{}"));

        var meals = mealLoader.mealsForPlan(plan.getId());
        var items = mealLoader.itemsByMealId(meals);
        return responseMapper.toMealPlanResponse(plan, meals, items);
    }
}

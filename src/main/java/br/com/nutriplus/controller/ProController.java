package br.com.nutriplus.controller;

import br.com.nutriplus.dto.request.ProBodyMeasurementRequest;
import br.com.nutriplus.dto.request.CreateInviteRequest;
import br.com.nutriplus.dto.request.ProPricingUpdateRequest;
import br.com.nutriplus.dto.request.ProProfileUpdateRequest;
import br.com.nutriplus.dto.request.ProGenerateMealPlanRequest;
import br.com.nutriplus.dto.request.ProPatientNutritionUpdateRequest;
import br.com.nutriplus.dto.request.ProPortfolioUpdateRequest;
import br.com.nutriplus.dto.request.PublishMealPlanRequest;
import br.com.nutriplus.dto.response.*;
import br.com.nutriplus.service.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pro")
public class ProController {

    private final NutritionistProService nutritionistProService;
    private final CareService careService;
    private final ProDashboardService dashboardService;
    private final PatientDossierService dossierService;
    private final PlanReviewService planReviewService;
    private final StripePaymentService stripePaymentService;
    private final ProPatientNutritionService proPatientNutritionService;
    private final MealPlanService mealPlanService;
    private final CareRatingService careRatingService;
    private final NutritionistPortfolioService portfolioService;

    public ProController(NutritionistProService nutritionistProService,
                           CareService careService,
                           ProDashboardService dashboardService,
                           PatientDossierService dossierService,
                           PlanReviewService planReviewService,
                           StripePaymentService stripePaymentService,
                           ProPatientNutritionService proPatientNutritionService,
                           MealPlanService mealPlanService,
                           CareRatingService careRatingService,
                           NutritionistPortfolioService portfolioService) {
        this.nutritionistProService = nutritionistProService;
        this.careService = careService;
        this.dashboardService = dashboardService;
        this.dossierService = dossierService;
        this.planReviewService = planReviewService;
        this.stripePaymentService = stripePaymentService;
        this.proPatientNutritionService = proPatientNutritionService;
        this.mealPlanService = mealPlanService;
        this.careRatingService = careRatingService;
        this.portfolioService = portfolioService;
    }

    @GetMapping("/profile")
    public NutritionistPublicResponse profile() {
        return nutritionistProService.getMyProfile();
    }

    @PutMapping("/profile")
    public NutritionistPublicResponse updateProfile(@Valid @RequestBody ProProfileUpdateRequest request) {
        return nutritionistProService.updateProfile(request);
    }

    @PutMapping("/pricing")
    public NutritionistPublicResponse updatePricing(@Valid @RequestBody ProPricingUpdateRequest request) {
        return nutritionistProService.updatePricing(request);
    }

    @GetMapping("/portfolio")
    public List<NutritionistPortfolioItemResponse> portfolio() {
        var nutritionist = nutritionistProService.getMyProfile();
        return portfolioService.listForNutritionist(nutritionist.id());
    }

    @PutMapping("/portfolio")
    public List<NutritionistPortfolioItemResponse> updatePortfolio(@Valid @RequestBody ProPortfolioUpdateRequest request) {
        return portfolioService.replacePortfolio(request);
    }

    @GetMapping("/dashboard")
    public ProDashboardResponse dashboard() {
        return dashboardService.getDashboard();
    }

    @GetMapping("/reports/revenue")
    public RevenueReportResponse revenue(@RequestParam int year, @RequestParam int month) {
        return dashboardService.getRevenueReport(year, month);
    }

    @GetMapping("/patients")
    public List<CareRelationshipResponse> patients() {
        return careService.listCaseload();
    }

    @GetMapping("/patients/{patientId}/dossier")
    public PatientDossierResponse dossier(@PathVariable Long patientId) {
        return dossierService.getDossier(patientId);
    }

    @PostMapping("/patients/{patientId}/measurements")
    public BodyMeasurementResponse recordMeasurement(@PathVariable Long patientId,
                                                     @Valid @RequestBody ProBodyMeasurementRequest request) {
        return dossierService.recordMeasurementForPatient(patientId, request);
    }

    @GetMapping("/patients/{patientId}/meal-plans")
    public List<MealPlanResponse> mealPlans(@PathVariable Long patientId) {
        return planReviewService.listPatientMealPlans(patientId);
    }

    @PutMapping("/patients/{patientId}/nutrition-profile")
    public NutritionProfileResponse updatePatientNutrition(@PathVariable Long patientId,
                                                           @Valid @RequestBody ProPatientNutritionUpdateRequest request) {
        return proPatientNutritionService.updateForPatient(patientId, request);
    }

    @PostMapping("/patients/{patientId}/meal-plans/generate")
    public MealPlanGenerationStatusResponse generatePlanForPatient(
            @PathVariable Long patientId,
            @RequestBody(required = false) @Valid ProGenerateMealPlanRequest request) {
        return mealPlanService.enqueueGenerationForUser(patientId, request);
    }

    @GetMapping("/ratings")
    public NutritionistRatingsSummaryResponse myRatings() {
        var nutritionist = nutritionistProService.getMyProfile();
        return careRatingService.myRatings(nutritionist.id());
    }

    @PutMapping("/patients/{patientId}/meal-plans/{mealPlanId}/publish")
    public MealPlanResponse publishPlan(@PathVariable Long patientId,
                                        @PathVariable Long mealPlanId,
                                        @Valid @RequestBody PublishMealPlanRequest request) {
        return planReviewService.publishMealPlan(patientId, mealPlanId, request);
    }

    @PostMapping("/invites")
    public InviteResponse createInvite(@Valid @RequestBody CreateInviteRequest request) {
        return careService.createInvite(request);
    }

    @PostMapping("/stripe/connect")
    public StripeConnectResponse connectStripe() {
        return stripePaymentService.startConnectOnboarding();
    }
}

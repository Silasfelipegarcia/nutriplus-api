package br.com.nutriplus.controller;

import br.com.nutriplus.dto.request.BodyMeasurementRequest;
import br.com.nutriplus.dto.request.CreateInviteRequest;
import br.com.nutriplus.dto.request.ProPricingUpdateRequest;
import br.com.nutriplus.dto.request.ProProfileUpdateRequest;
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

    public ProController(NutritionistProService nutritionistProService,
                           CareService careService,
                           ProDashboardService dashboardService,
                           PatientDossierService dossierService,
                           PlanReviewService planReviewService,
                           StripePaymentService stripePaymentService) {
        this.nutritionistProService = nutritionistProService;
        this.careService = careService;
        this.dashboardService = dashboardService;
        this.dossierService = dossierService;
        this.planReviewService = planReviewService;
        this.stripePaymentService = stripePaymentService;
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
                                                     @Valid @RequestBody BodyMeasurementRequest request) {
        return dossierService.recordMeasurementForPatient(patientId, request);
    }

    @GetMapping("/patients/{patientId}/meal-plans")
    public List<MealPlanResponse> mealPlans(@PathVariable Long patientId) {
        return planReviewService.listPatientMealPlans(patientId);
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

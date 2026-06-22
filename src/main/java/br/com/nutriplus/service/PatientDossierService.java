package br.com.nutriplus.service;

import br.com.nutriplus.domain.entity.*;
import br.com.nutriplus.domain.enums.CheckinStatus;
import br.com.nutriplus.domain.enums.ProgressReviewStatus;
import br.com.nutriplus.dto.request.ProBodyMeasurementRequest;
import br.com.nutriplus.dto.response.*;
import br.com.nutriplus.exception.ResourceNotFoundException;
import br.com.nutriplus.mapper.ProMapper;
import br.com.nutriplus.mapper.ResponseMapper;
import br.com.nutriplus.repository.*;
import br.com.nutriplus.security.AuthorizationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class PatientDossierService {

    private final AuthorizationService authorizationService;
    private final NutritionProfileRepository nutritionProfileRepository;
    private final BodyMeasurementSessionRepository measurementRepository;
    private final ProgressReviewRepository reviewRepository;
    private final MealPlanRepository mealPlanRepository;
    private final MealLoader mealLoader;
    private final DailyMealCheckinRepository checkinRepository;
    private final ResponseMapper responseMapper;
    private final ProMapper proMapper;
    private final EvolutionReportBuilder evolutionReportBuilder;
    private final ProgressService progressService;
    private final HealthReferenceService healthReferenceService;
    private final ProMeasurementValidator proMeasurementValidator;

    public PatientDossierService(AuthorizationService authorizationService,
                                 NutritionProfileRepository nutritionProfileRepository,
                                 BodyMeasurementSessionRepository measurementRepository,
                                 ProgressReviewRepository reviewRepository,
                                 MealPlanRepository mealPlanRepository,
                                 MealLoader mealLoader,
                                 DailyMealCheckinRepository checkinRepository,
                                 ResponseMapper responseMapper,
                                 ProMapper proMapper,
                                 EvolutionReportBuilder evolutionReportBuilder,
                                 ProgressService progressService,
                                 HealthReferenceService healthReferenceService,
                                 ProMeasurementValidator proMeasurementValidator) {
        this.authorizationService = authorizationService;
        this.nutritionProfileRepository = nutritionProfileRepository;
        this.measurementRepository = measurementRepository;
        this.reviewRepository = reviewRepository;
        this.mealPlanRepository = mealPlanRepository;
        this.mealLoader = mealLoader;
        this.checkinRepository = checkinRepository;
        this.responseMapper = responseMapper;
        this.proMapper = proMapper;
        this.evolutionReportBuilder = evolutionReportBuilder;
        this.progressService = progressService;
        this.healthReferenceService = healthReferenceService;
        this.proMeasurementValidator = proMeasurementValidator;
    }

    public PatientDossierResponse getDossier(Long patientId) {
        CareRelationship care = authorizationService.requireCareAccessForNutritionistByPatientId(patientId);
        User patient = care.getPatient();
        Long uid = patient.getId();

        NutritionProfile profile = nutritionProfileRepository.findByUserId(uid).orElse(null);
        List<BodyMeasurementResponse> measurements = measurementRepository
                .findTop12ByUserIdOrderByMeasuredOnAscIdAsc(uid).stream()
                .map(this::toMeasurement).toList();

        CheckinStatsResponse stats = checkinStatsFor(uid);
        EvolutionReportResponse evolution = profile != null && !measurements.isEmpty()
                ? buildEvolution(profile, measurements, stats)
                : null;

        MealPlanResponse latestPlan = mealPlanRepository.findByUserIdOrderByCreatedAtDesc(uid).stream()
                .findFirst()
                .map(plan -> {
                    var meals = mealLoader.mealsForPlan(plan.getId());
                    var items = mealLoader.itemsByMealId(meals);
                    return responseMapper.toMealPlanResponse(plan, meals, items);
                })
                .orElse(null);

        ProgressReviewResponse latestReview = reviewRepository.findFirstByUserIdOrderByCreatedAtDesc(uid)
                .filter(r -> r.getStatus() == ProgressReviewStatus.COMPLETED)
                .map(this::toReviewResponse)
                .orElse(null);

        return new PatientDossierResponse(
                uid,
                patient.getName(),
                proMapper.toCare(care),
                profile != null ? responseMapper.toNutritionProfileResponse(profile) : null,
                measurements,
                evolution,
                latestPlan,
                latestReview,
                stats
        );
    }

    @Transactional
    public BodyMeasurementResponse recordMeasurementForPatient(Long patientId, ProBodyMeasurementRequest request) {
        var nutritionist = authorizationService.requireNutritionist();
        authorizationService.requireCareAccessForNutritionistByPatientId(patientId);
        proMeasurementValidator.validate(request);
        return progressService.saveMeasurementForUser(
                patientId,
                request.toBodyMeasurementRequest(),
                request.calculationMethod(),
                nutritionist
        );
    }

    private CheckinStatsResponse checkinStatsFor(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(6);
        var weekCheckins = checkinRepository.findByUserIdAndDateRange(userId, weekStart, today);
        int done = (int) weekCheckins.stream().filter(c -> c.getStatus() == CheckinStatus.DONE).count();
        int total = Math.max(weekCheckins.size(), 1);
        int adherence = (int) Math.round((done * 100.0) / total);
        return new CheckinStatsResponse(0, adherence);
    }

    private EvolutionReportResponse buildEvolution(NutritionProfile profile,
                                                   List<BodyMeasurementResponse> measurements,
                                                   CheckinStatsResponse stats) {
        BodyMeasurementResponse baseline = measurements.getFirst();
        BodyMeasurementResponse latest = measurements.getLast();
        var metrics = evolutionReportBuilder.buildMetrics(profile, baseline, latest, stats.weekAdherencePercent());
        return new EvolutionReportResponse(
                true,
                profile.getGoal().name(),
                profile.getTargetWeightKg(),
                baseline,
                latest,
                measurements,
                metrics,
                0, 0, 0, 0,
                "Evolução do paciente",
                stats.weekAdherencePercent(),
                stats.streakDays(),
                null,
                profile.getHeightCm(),
                healthReferenceService.buildHealthSnapshot(profile, baseline, latest),
                HealthReferenceService.HEALTH_DISCLAIMER
        );
    }

    private BodyMeasurementResponse toMeasurement(BodyMeasurementSession s) {
        return new BodyMeasurementResponse(
                s.getId(), s.getMeasuredOn(), s.getWeightKg(), s.getBodyFatPercent(), s.getMuscleMassKg(),
                s.getWaistCm(), s.getHipCm(), s.getChestCm(), s.getNeckCm(),
                s.getArmRightCm(), s.getArmLeftCm(), s.getThighRightCm(), s.getThighLeftCm(), s.getNotes()
        );
    }

    private ProgressReviewResponse toReviewResponse(ProgressReview review) {
        return new ProgressReviewResponse(
                review.getId(),
                review.getStatus().name(),
                review.getTrend() != null ? review.getTrend().name() : null,
                review.getSummary(),
                List.of(),
                review.getWeekAdherencePercent(),
                toMeasurement(review.getCurrentSession()),
                review.getPreviousSession() != null ? toMeasurement(review.getPreviousSession()) : null,
                review.getCompletedAt()
        );
    }
}

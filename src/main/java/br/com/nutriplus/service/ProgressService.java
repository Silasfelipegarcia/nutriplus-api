package br.com.nutriplus.service;

import br.com.nutriplus.client.AiAgentClient;
import br.com.nutriplus.client.dto.AiProgressAnalyzeResponse;
import br.com.nutriplus.domain.entity.BodyMeasurementSession;
import br.com.nutriplus.domain.entity.Nutritionist;
import br.com.nutriplus.domain.entity.NutritionProfile;
import br.com.nutriplus.domain.entity.ProgressReview;
import br.com.nutriplus.domain.entity.User;
import br.com.nutriplus.domain.enums.CalculationMethod;
import br.com.nutriplus.domain.enums.ProgressReviewStatus;
import br.com.nutriplus.domain.enums.EvolutionMetricStatus;
import br.com.nutriplus.domain.enums.ProgressTrend;
import br.com.nutriplus.dto.request.BodyMeasurementRequest;
import br.com.nutriplus.dto.request.ProgressReviewRequest;
import br.com.nutriplus.dto.response.BodyMeasurementResponse;
import br.com.nutriplus.dto.response.EvolutionReportResponse;
import br.com.nutriplus.dto.response.EvolutionMetricResponse;
import br.com.nutriplus.dto.response.ProgressReviewResponse;
import br.com.nutriplus.dto.response.ProgressScheduleResponse;
import br.com.nutriplus.exception.BusinessException;
import br.com.nutriplus.exception.ResourceNotFoundException;
import br.com.nutriplus.repository.BodyMeasurementSessionRepository;
import br.com.nutriplus.repository.MealPlanRepository;
import br.com.nutriplus.repository.NutritionProfileRepository;
import br.com.nutriplus.repository.ProgressReviewRepository;
import br.com.nutriplus.repository.UserRepository;
import br.com.nutriplus.security.CurrentUser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import br.com.nutriplus.infrastructure.config.NutriCacheNames;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class ProgressService {

    private final CurrentUser currentUser;
    private final NutritionProfileRepository nutritionProfileRepository;
    private final BodyMeasurementSessionRepository measurementRepository;
    private final MealPlanRepository mealPlanRepository;
    private final ProgressReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final CheckinService checkinService;
    private final AiAgentClient aiAgentClient;
    private final ObjectMapper objectMapper;
    private final EvolutionReportBuilder evolutionReportBuilder;
    private final HealthReferenceService healthReferenceService;
    private final ProgressScheduleService progressScheduleService;

    public ProgressService(CurrentUser currentUser,
                           NutritionProfileRepository nutritionProfileRepository,
                           BodyMeasurementSessionRepository measurementRepository,
                           MealPlanRepository mealPlanRepository,
                           ProgressReviewRepository reviewRepository,
                           UserRepository userRepository,
                           CheckinService checkinService,
                           AiAgentClient aiAgentClient,
                           ObjectMapper objectMapper,
                           EvolutionReportBuilder evolutionReportBuilder,
                           HealthReferenceService healthReferenceService,
                           ProgressScheduleService progressScheduleService) {
        this.currentUser = currentUser;
        this.nutritionProfileRepository = nutritionProfileRepository;
        this.measurementRepository = measurementRepository;
        this.mealPlanRepository = mealPlanRepository;
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.checkinService = checkinService;
        this.aiAgentClient = aiAgentClient;
        this.objectMapper = objectMapper;
        this.evolutionReportBuilder = evolutionReportBuilder;
        this.healthReferenceService = healthReferenceService;
        this.progressScheduleService = progressScheduleService;
    }

    @Cacheable(value = NutriCacheNames.PROGRESS_SCHEDULE, keyGenerator = "userIdCacheKeyGenerator")
    public ProgressScheduleResponse getSchedule() {
        User user = currentUser.get();
        NutritionProfile profile = requireProfile(user.getId());
        return getScheduleForUser(user.getId(), profile);
    }

    public ProgressScheduleResponse getScheduleForUser(Long userId, NutritionProfile profile) {
        return progressScheduleService.getScheduleForUser(userId, profile);
    }

    @Transactional
    @CacheEvict(value = {NutriCacheNames.PROGRESS_SCHEDULE, NutriCacheNames.PROGRESS_MEASUREMENT_LATEST},
            keyGenerator = "userIdCacheKeyGenerator")
    public BodyMeasurementResponse saveMeasurement(BodyMeasurementRequest request) {
        User user = currentUser.get();
        return saveMeasurementForUser(user.getId(), request);
    }

    @Transactional
    public BodyMeasurementResponse saveMeasurementForUser(Long userId, BodyMeasurementRequest request) {
        return saveMeasurementForUser(userId, request, null, null);
    }

    @Transactional
    public BodyMeasurementResponse saveMeasurementForUser(Long userId,
                                                        BodyMeasurementRequest request,
                                                        CalculationMethod calculationMethod,
                                                        Nutritionist recordedByNutritionist) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        NutritionProfile profile = requireProfile(user.getId());

        LocalDate measuredOn = request.measuredOn();
        LocalDate today = LocalDate.now();
        ProgressScheduleResponse schedule = getScheduleForUser(userId, profile);
        Optional<BodyMeasurementSession> existingOnDate = measurementRepository
                .findFirstByUserIdAndMeasuredOnOrderByIdDesc(userId, measuredOn);

        if (!canSaveMeasurement(userId, measuredOn, today, existingOnDate, schedule)) {
            int days = schedule.daysUntilDue();
            throw new BusinessException(
                    days > 0
                            ? "Aguarde " + days + " dias para registrar a próxima medição."
                            : "Aguarde o prazo da próxima medição.");
        }

        BodyMeasurementSession session = existingOnDate.orElseGet(() -> new BodyMeasurementSession(user));
        if (session.getId() == null) {
            session.setUser(user);
        }
        session.setMeasuredOn(measuredOn);
        session.setWeightKg(request.weightKg());
        session.setBodyFatPercent(request.bodyFatPercent());
        session.setMuscleMassKg(request.muscleMassKg());
        session.setWaistCm(request.waistCm());
        session.setHipCm(request.hipCm());
        session.setChestCm(request.chestCm());
        session.setNeckCm(request.neckCm());
        session.setArmRightCm(request.armRightCm());
        session.setArmLeftCm(request.armLeftCm());
        session.setThighRightCm(request.thighRightCm());
        session.setThighLeftCm(request.thighLeftCm());
        session.setNotes(request.notes());
        if (recordedByNutritionist != null) {
            session.setRecordedByNutritionist(recordedByNutritionist);
        }
        session = measurementRepository.save(session);

        profile.setCurrentWeightKg(request.weightKg());
        if (request.bodyFatPercent() != null) {
            profile.setBodyFatPercent(request.bodyFatPercent());
        }
        if (request.muscleMassKg() != null) {
            profile.setMuscleMassKg(request.muscleMassKg());
        }
        if (calculationMethod != null) {
            profile.setCalculationMethod(calculationMethod);
        }
        nutritionProfileRepository.save(profile);

        return toResponse(session);
    }

    /**
     * Mantém a medição de hoje alinhada ao peso do perfil após edição no app/web.
     * Não exige janela de progresso — só atualiza ou cria registro do dia.
     */
    @Transactional
    @CacheEvict(value = {NutriCacheNames.PROGRESS_SCHEDULE, NutriCacheNames.PROGRESS_MEASUREMENT_LATEST},
            keyGenerator = "userIdCacheKeyGenerator")
    public void syncProfileWeightFromEdit(Long userId, java.math.BigDecimal weightKg) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        LocalDate today = br.com.nutriplus.util.NutriTime.today();
        BodyMeasurementSession session = measurementRepository
                .findFirstByUserIdAndMeasuredOnOrderByIdDesc(userId, today)
                .orElseGet(() -> new BodyMeasurementSession(user));
        if (session.getId() == null) {
            session.setUser(user);
            session.setMeasuredOn(today);
        }
        session.setWeightKg(weightKg);
        measurementRepository.save(session);
    }

    @Cacheable(value = NutriCacheNames.PROGRESS_MEASUREMENT_LATEST, keyGenerator = "userIdCacheKeyGenerator")
    public BodyMeasurementResponse getLatestMeasurement() {
        User user = currentUser.get();
        return measurementRepository.findFirstByUserIdOrderByMeasuredOnDescIdDesc(user.getId())
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Nenhuma medição registrada"));
    }

    @Transactional
    public ProgressReviewResponse generateReview(ProgressReviewRequest request) {
        User user = currentUser.get();
        NutritionProfile profile = requireProfile(user.getId());
        ProgressScheduleResponse schedule = getSchedule();
        if (!schedule.due()) {
            throw new BusinessException(
                    "Sua próxima reavaliação será em " + schedule.daysUntilDue() + " dias.");
        }

        List<BodyMeasurementSession> sessions = measurementRepository
                .findTop2ByUserIdOrderByMeasuredOnDescIdDesc(user.getId());
        if (sessions.isEmpty()) {
            throw new BusinessException("Registre suas medidas antes de pedir a análise.");
        }

        BodyMeasurementSession current = sessions.getFirst();
        BodyMeasurementSession previous = sessions.size() > 1 ? sessions.get(1) : null;

        ProgressReview review = new ProgressReview(user);
        review.setCurrentSession(current);
        review.setPreviousSession(previous);
        review.setStatus(ProgressReviewStatus.RUNNING);
        review.setWeekAdherencePercent(checkinService.getStats().weekAdherencePercent());
        if (request != null) {
            review.setPhysicalDiscomforts(trimToNull(request.physicalDiscomforts()));
            review.setPositiveChanges(trimToNull(request.positiveChanges()));
            review.setGeneralNotes(trimToNull(request.generalNotes()));
        }
        review = reviewRepository.save(review);

        try {
            AiProgressAnalyzeResponse ai = aiAgentClient.analyzeProgress(
                    profile,
                    current,
                    previous,
                    review.getWeekAdherencePercent(),
                    review.getPhysicalDiscomforts(),
                    review.getPositiveChanges(),
                    review.getGeneralNotes()
            );
            review.setTrend(ProgressTrend.valueOf(ai.trend()));
            review.setSummary(ai.summary());
            try {
                review.setRecommendations(objectMapper.writeValueAsString(ai.recommendations()));
            } catch (com.fasterxml.jackson.core.JsonProcessingException ex) {
                review.setRecommendations(String.join("\n", ai.recommendations()));
            }
            review.setPlanChangeSuggested(ai.planChangeSuggested());
            review.setPlanChangeRationale(ai.planChangeRationale());
            review.setKeepPlanMessage(ai.keepPlanMessage());
            review.setConfidence(ai.confidence());
            review.setStatus(ProgressReviewStatus.COMPLETED);
            review.setCompletedAt(LocalDateTime.now());
        } catch (Exception e) {
            review.setStatus(ProgressReviewStatus.FAILED);
            review.setErrorMessage(e.getMessage());
            reviewRepository.save(review);
            throw e;
        }

        review = reviewRepository.save(review);
        return toReviewResponse(review);
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public ProgressReviewResponse getLatestReview() {
        User user = currentUser.get();
        return reviewRepository.findFirstByUserIdOrderByCreatedAtDesc(user.getId())
                .filter(r -> r.getStatus() == ProgressReviewStatus.COMPLETED)
                .map(this::toReviewResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Nenhuma análise de progresso encontrada"));
    }

    public EvolutionReportResponse getEvolutionReport() {
        User user = currentUser.get();
        NutritionProfile profile = requireProfile(user.getId());
        var stats = checkinService.getStats();

        List<BodyMeasurementSession> historyEntities =
                measurementRepository.findTop12ByUserIdOrderByMeasuredOnAscIdAsc(user.getId());
        boolean hasMeasurements = !historyEntities.isEmpty();

        BodyMeasurementResponse baseline;
        BodyMeasurementResponse latest;
        List<BodyMeasurementResponse> history;

        if (hasMeasurements) {
            history = historyEntities.stream().map(this::toResponse).toList();
            baseline = history.getFirst();
            latest = history.getLast();
        } else {
            baseline = profileSnapshot(profile, profileCreatedOn(profile));
            latest = baseline;
            history = List.of(baseline);
        }

        List<EvolutionMetricResponse> metrics = evolutionReportBuilder.buildMetrics(
                profile, baseline, latest, stats.weekAdherencePercent());

        int excellent = 0;
        int good = 0;
        int ok = 0;
        int below = 0;
        for (EvolutionMetricResponse m : metrics) {
            switch (EvolutionMetricStatus.valueOf(m.status())) {
                case EXCELLENT -> excellent++;
                case GOOD -> good++;
                case OK -> ok++;
                case BELOW -> below++;
            }
        }

        ProgressReviewResponse latestReview = reviewRepository
                .findFirstByUserIdOrderByCreatedAtDesc(user.getId())
                .filter(r -> r.getStatus() == ProgressReviewStatus.COMPLETED)
                .map(this::toReviewResponse)
                .orElse(null);

        return new EvolutionReportResponse(
                hasMeasurements,
                profile.getGoal().name(),
                profile.getTargetWeightKg(),
                baseline,
                latest,
                history,
                metrics,
                excellent,
                good,
                ok,
                below,
                buildHeadline(excellent, good, below, profile.getGoal().name(), profile.isAthleteModeEnabled()),
                stats.weekAdherencePercent(),
                stats.streakDays(),
                latestReview,
                profile.getHeightCm(),
                healthReferenceService.buildHealthSnapshot(profile, baseline, latest),
                HealthReferenceService.HEALTH_DISCLAIMER
        );
    }

    private BodyMeasurementResponse profileSnapshot(NutritionProfile profile, LocalDate measuredOn) {
        return new BodyMeasurementResponse(
                null,
                measuredOn,
                profile.getCurrentWeightKg(),
                profile.getBodyFatPercent(),
                profile.getMuscleMassKg(),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "Baseline do onboarding"
        );
    }

    private String buildHeadline(int excellent, int good, int below, String goal, boolean athleteMode) {
        if (athleteMode) {
            if (excellent >= 4) {
                return "Evolução excelente nos grupos medidos — padrão de sucesso!";
            }
            if (below >= 2) {
                return "Alguns grupos musculares pedem reajuste — revise treino, proteína e recuperação.";
            }
            if (good + excellent >= 4) {
                return "Boa evolução corporal — continue medindo peito, braços e coxas.";
            }
        }
        if (excellent >= 3) {
            return "Evolução excelente — você está no caminho certo!";
        }
        if (good + excellent >= 3 && below == 0) {
            return "Boa evolução! Vários indicadores melhoraram.";
        }
        if (below >= 2) {
            return "Alguns pontos pedem atenção — vamos ajustar juntos.";
        }
        return switch (goal) {
            case "LOSE_WEIGHT" -> "Acompanhe peso e cintura para ver a gordura ceder.";
            case "GAIN_MASS" -> "Acompanhe braços e coxas para ver o músculo crescer.";
            default -> "Sua evolução no período, ponto a ponto.";
        };
    }

    private String buildHeadline(int excellent, int good, int below, String goal) {
        return buildHeadline(excellent, good, below, goal, false);
    }

    private NutritionProfile requireProfile(Long userId) {
        return nutritionProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil nutricional não encontrado"));
    }

    private boolean canSaveMeasurement(Long userId,
                                       LocalDate measuredOn,
                                       LocalDate today,
                                       Optional<BodyMeasurementSession> existingOnDate,
                                       ProgressScheduleResponse schedule) {
        if (measurementRepository.findFirstByUserIdOrderByMeasuredOnAscIdAsc(userId).isEmpty()) {
            return true;
        }
        if (mealPlanRepository.findByUserIdOrderByCreatedAtDesc(userId).isEmpty()) {
            return true;
        }
        if (schedule.due()) {
            return true;
        }
        return measuredOn.equals(today) && existingOnDate.isPresent();
    }

    private LocalDate resolveAnchorDate(Long userId, NutritionProfile profile) {
        return reviewRepository.findFirstByUserIdOrderByCreatedAtDesc(userId)
                .filter(r -> r.getStatus() == ProgressReviewStatus.COMPLETED && r.getCompletedAt() != null)
                .map(r -> r.getCompletedAt().toLocalDate())
                .orElseGet(() -> measurementRepository
                        .findFirstByUserIdOrderByMeasuredOnDescIdDesc(userId)
                        .map(BodyMeasurementSession::getMeasuredOn)
                        .orElseGet(() -> profileCreatedOn(profile)));
    }

    private LocalDate profileCreatedOn(NutritionProfile profile) {
        if (profile.getCreatedAt() != null) {
            return profile.getCreatedAt().toLocalDate();
        }
        return LocalDate.now();
    }

    private BodyMeasurementResponse toResponse(BodyMeasurementSession s) {
        return new BodyMeasurementResponse(
                s.getId(),
                s.getMeasuredOn(),
                s.getWeightKg(),
                s.getBodyFatPercent(),
                s.getMuscleMassKg(),
                s.getWaistCm(),
                s.getHipCm(),
                s.getChestCm(),
                s.getNeckCm(),
                s.getArmRightCm(),
                s.getArmLeftCm(),
                s.getThighRightCm(),
                s.getThighLeftCm(),
                s.getNotes()
        );
    }

    private ProgressReviewResponse toReviewResponse(ProgressReview review) {
        List<String> recommendations = List.of();
        if (review.getRecommendations() != null && !review.getRecommendations().isBlank()) {
            try {
                recommendations = objectMapper.readValue(
                        review.getRecommendations(), new TypeReference<List<String>>() {});
            } catch (Exception ignored) {
                recommendations = Arrays.asList(review.getRecommendations().split("\n"));
            }
        }
        return new ProgressReviewResponse(
                review.getId(),
                review.getStatus().name(),
                review.getTrend() != null ? review.getTrend().name() : null,
                review.getSummary(),
                recommendations,
                review.getWeekAdherencePercent(),
                toResponse(review.getCurrentSession()),
                review.getPreviousSession() != null ? toResponse(review.getPreviousSession()) : null,
                review.getCompletedAt(),
                review.getPlanChangeSuggested(),
                review.getPlanChangeRationale(),
                review.getKeepPlanMessage(),
                review.getConfidence()
        );
    }
}

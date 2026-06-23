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
import br.com.nutriplus.dto.response.BodyMeasurementResponse;
import br.com.nutriplus.dto.response.EvolutionReportResponse;
import br.com.nutriplus.dto.response.EvolutionMetricResponse;
import br.com.nutriplus.dto.response.ProgressReviewResponse;
import br.com.nutriplus.dto.response.ProgressScheduleResponse;
import br.com.nutriplus.exception.BusinessException;
import br.com.nutriplus.exception.ResourceNotFoundException;
import br.com.nutriplus.repository.BodyMeasurementSessionRepository;
import br.com.nutriplus.repository.NutritionProfileRepository;
import br.com.nutriplus.repository.ProgressReviewRepository;
import br.com.nutriplus.repository.UserRepository;
import br.com.nutriplus.security.CurrentUser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final ProgressReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final CheckinService checkinService;
    private final AiAgentClient aiAgentClient;
    private final ObjectMapper objectMapper;
    private final EvolutionReportBuilder evolutionReportBuilder;
    private final HealthReferenceService healthReferenceService;

    public ProgressService(CurrentUser currentUser,
                           NutritionProfileRepository nutritionProfileRepository,
                           BodyMeasurementSessionRepository measurementRepository,
                           ProgressReviewRepository reviewRepository,
                           UserRepository userRepository,
                           CheckinService checkinService,
                           AiAgentClient aiAgentClient,
                           ObjectMapper objectMapper,
                           EvolutionReportBuilder evolutionReportBuilder,
                           HealthReferenceService healthReferenceService) {
        this.currentUser = currentUser;
        this.nutritionProfileRepository = nutritionProfileRepository;
        this.measurementRepository = measurementRepository;
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.checkinService = checkinService;
        this.aiAgentClient = aiAgentClient;
        this.objectMapper = objectMapper;
        this.evolutionReportBuilder = evolutionReportBuilder;
        this.healthReferenceService = healthReferenceService;
    }

    public ProgressScheduleResponse getSchedule() {
        User user = currentUser.get();
        NutritionProfile profile = requireProfile(user.getId());
        return getScheduleForUser(user.getId(), profile);
    }

    private ProgressScheduleResponse getScheduleForUser(Long userId, NutritionProfile profile) {
        int intervalDays = profile.getProgressReviewIntervalDays();

        LocalDate anchor = resolveAnchorDate(userId, profile);
        LocalDate nextDueOn = anchor.plusDays(intervalDays);
        LocalDate today = LocalDate.now();
        long daysUntil = ChronoUnit.DAYS.between(today, nextDueOn);
        boolean due = !today.isBefore(nextDueOn);

        LocalDateTime lastReviewAt = reviewRepository.findFirstByUserIdOrderByCreatedAtDesc(userId)
                .filter(r -> r.getStatus() == ProgressReviewStatus.COMPLETED)
                .map(ProgressReview::getCompletedAt)
                .orElse(null);

        LocalDate lastMeasurementOn = measurementRepository
                .findFirstByUserIdOrderByMeasuredOnDescIdDesc(userId)
                .map(BodyMeasurementSession::getMeasuredOn)
                .orElse(null);

        return new ProgressScheduleResponse(
                intervalDays,
                due,
                due ? 0 : (int) daysUntil,
                nextDueOn,
                lastReviewAt,
                lastMeasurementOn
        );
    }

    @Transactional
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

        if (!schedule.due()) {
            if (!measuredOn.equals(today) || existingOnDate.isEmpty()) {
                int days = schedule.daysUntilDue();
                throw new BusinessException(
                        days > 0
                                ? "Aguarde " + days + " dias para registrar a próxima medição."
                                : "Aguarde o prazo da próxima medição.");
            }
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

    public BodyMeasurementResponse getLatestMeasurement() {
        User user = currentUser.get();
        return measurementRepository.findFirstByUserIdOrderByMeasuredOnDescIdDesc(user.getId())
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Nenhuma medição registrada"));
    }

    @Transactional
    public ProgressReviewResponse generateReview() {
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
        review = reviewRepository.save(review);

        try {
            AiProgressAnalyzeResponse ai = aiAgentClient.analyzeProgress(
                    profile,
                    current,
                    previous,
                    review.getWeekAdherencePercent()
            );
            review.setTrend(ProgressTrend.valueOf(ai.trend()));
            review.setSummary(ai.summary());
            try {
                review.setRecommendations(objectMapper.writeValueAsString(ai.recommendations()));
            } catch (com.fasterxml.jackson.core.JsonProcessingException ex) {
                review.setRecommendations(String.join("\n", ai.recommendations()));
            }
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
                review.getCompletedAt()
        );
    }
}

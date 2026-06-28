package br.com.nutriplus.service;

import br.com.nutriplus.client.AiAgentClient;
import br.com.nutriplus.client.dto.AiNutritionCalculateResponse;
import br.com.nutriplus.domain.entity.NutritionProfile;
import br.com.nutriplus.domain.entity.User;
import br.com.nutriplus.domain.entity.UserTrainingActivity;
import br.com.nutriplus.domain.enums.SportType;
import br.com.nutriplus.dto.request.TrainingActivityRequest;
import br.com.nutriplus.dto.request.TrainingProfileRequest;
import br.com.nutriplus.dto.response.CoachInsightResponse;
import br.com.nutriplus.dto.response.SportCatalogItemResponse;
import br.com.nutriplus.dto.response.TrainingActivityResponse;
import br.com.nutriplus.dto.response.TrainingProfileResponse;
import br.com.nutriplus.exception.BusinessException;
import br.com.nutriplus.exception.SubscriptionRequiredException;
import br.com.nutriplus.mapper.ResponseMapper;
import br.com.nutriplus.repository.NutritionProfileRepository;
import br.com.nutriplus.repository.UserTrainingActivityRepository;
import br.com.nutriplus.security.CurrentUser;
import br.com.nutriplus.dto.response.NutritionProfileResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;
import br.com.nutriplus.infrastructure.config.NutriCacheNames;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Service
public class TrainingService {

    private final CurrentUser currentUser;
    private final NutritionProfileRepository nutritionProfileRepository;
    private final UserTrainingActivityRepository activityRepository;
    private final AiAgentClient aiAgentClient;
    private final ResponseMapper responseMapper;
    private final CoachInsightService coachInsightService;
    private final AthleteAccessService athleteAccessService;
    private final SubscriptionService subscriptionService;
    private final PlanRegenerationPolicyService regenerationPolicyService;

    public TrainingService(CurrentUser currentUser,
                           NutritionProfileRepository nutritionProfileRepository,
                           UserTrainingActivityRepository activityRepository,
                           AiAgentClient aiAgentClient,
                           ResponseMapper responseMapper,
                           CoachInsightService coachInsightService,
                           AthleteAccessService athleteAccessService,
                           SubscriptionService subscriptionService,
                           PlanRegenerationPolicyService regenerationPolicyService) {
        this.currentUser = currentUser;
        this.nutritionProfileRepository = nutritionProfileRepository;
        this.activityRepository = activityRepository;
        this.aiAgentClient = aiAgentClient;
        this.responseMapper = responseMapper;
        this.coachInsightService = coachInsightService;
        this.athleteAccessService = athleteAccessService;
        this.subscriptionService = subscriptionService;
        this.regenerationPolicyService = regenerationPolicyService;
    }

    @Cacheable(value = NutriCacheNames.SPORT_CATALOG, key = "'catalog'")
    public List<SportCatalogItemResponse> getSportCatalog() {
        return Arrays.stream(SportType.values())
                .sorted(Comparator.comparing(SportType::labelPt))
                .map(s -> new SportCatalogItemResponse(
                        s.name(),
                        s.labelPt(),
                        s.met(),
                        intensityHint(s)
                ))
                .toList();
    }

    public TrainingProfileResponse getProfile() {
        User user = currentUser.get();
        NutritionProfile profile = requireProfile(user.getId());
        List<UserTrainingActivity> activities = activityRepository.findByUserIdOrderByIdAsc(user.getId());
        return buildResponse(profile, activities, null);
    }

    @Transactional
    public TrainingProfileResponse saveProfile(TrainingProfileRequest request) {
        User user = currentUser.get();
        NutritionProfile profile = requireProfile(user.getId());
        boolean wasAthlete = profile.isAthleteModeEnabled();

        if (Boolean.TRUE.equals(request.athleteModeEnabled()) && !athleteAccessService.hasAthleteAccess(user)) {
            throw new SubscriptionRequiredException("Assine o plano Atleta para ativar o modo atleta.");
        }

        profile.setAthleteModeEnabled(Boolean.TRUE.equals(request.athleteModeEnabled()));
        if (!wasAthlete && profile.isAthleteModeEnabled()) {
            regenerationPolicyService.markAthleteRegenEligible(profile);
        }
        if (!profile.isAthleteModeEnabled()) {
            profile.setTrainingDailyExtraKcal(null);
        }
        nutritionProfileRepository.save(profile);

        activityRepository.deleteByUserId(user.getId());

        if (Boolean.TRUE.equals(request.athleteModeEnabled()) && request.activities() != null) {
            for (TrainingActivityRequest item : request.activities()) {
                SportType sport = parseSport(item.sportType());
                String customLabel = normalizeCustomLabel(item.customLabel());
                if (sport == SportType.OTHER && (customLabel == null || customLabel.isBlank())) {
                    throw new BusinessException("Informe o nome da prática ao escolher Outro.");
                }
                if (sport != SportType.OTHER) {
                    customLabel = null;
                }
                activityRepository.save(new UserTrainingActivity(
                        user,
                        sport,
                        item.daysPerWeek(),
                        item.minutesPerSession(),
                        customLabel
                ));
            }
        }

        List<UserTrainingActivity> saved = activityRepository.findByUserIdOrderByIdAsc(user.getId());
        if (profile.isAthleteModeEnabled() && saved.isEmpty()) {
            throw new BusinessException("Adicione pelo menos um treino no modo atleta.");
        }

        profile = recalculateMacrosForProfile(profile, saved);
        TrainingProfileResponse response = buildResponse(profile, saved, null);
        CoachInsightResponse coachInsight = coachInsightService.trainingReview(profile, response);
        return buildResponse(profile, saved, coachInsight);
    }

    @Transactional
    public NutritionProfileResponse applyToPlan() {
        User user = currentUser.get();
        if (!athleteAccessService.hasAthleteAccess(user)) {
            throw new SubscriptionRequiredException("Assine o plano Atleta para aplicar treinos ao plano alimentar.");
        }
        NutritionProfile profile = requireProfile(user.getId());
        if (!profile.isAthleteModeEnabled()) {
            throw new BusinessException("Ative o modo atleta antes de aplicar ao plano.");
        }

        List<UserTrainingActivity> activities = activityRepository.findByUserIdOrderByIdAsc(user.getId());
        if (activities.isEmpty()) {
            throw new BusinessException("Cadastre seus treinos para ajustar o plano.");
        }

        profile = recalculateMacrosForProfile(profile, activities);
        return responseMapper.toNutritionProfileResponse(profile, subscriptionService.montarStatus(user));
    }

    public void syncTrainingDailyExtra(NutritionProfile profile) {
        if (!profile.isAthleteModeEnabled()) {
            return;
        }
        Long userId = profile.getUser().getId();
        List<UserTrainingActivity> activities = activityRepository.findByUserIdOrderByIdAsc(userId);
        if (activities.isEmpty()) {
            return;
        }
        profile.setTrainingDailyExtraKcal(computeDailyExtra(profile.getCurrentWeightKg(), activities));
    }

    private NutritionProfile recalculateMacrosForProfile(NutritionProfile profile,
                                                         List<UserTrainingActivity> activities) {
        if (profile.isAthleteModeEnabled() && !activities.isEmpty()) {
            profile.setTrainingDailyExtraKcal(computeDailyExtra(profile.getCurrentWeightKg(), activities));
        } else {
            profile.setTrainingDailyExtraKcal(null);
        }

        AiNutritionCalculateResponse macros = aiAgentClient.calculateMacros(profile);
        profile.setBmrKcal(macros.bmrKcal());
        profile.setTdeeKcal(macros.tdeeKcal());
        profile.setTargetCalories(macros.targetCalories());
        profile.setTargetProteinG(macros.targetProteinG());
        profile.setTargetCarbsG(macros.targetCarbsG());
        profile.setTargetFatG(macros.targetFatG());
        if (macros.leanMassKg() != null) {
            profile.setLeanMassKg(macros.leanMassKg());
        }

        return nutritionProfileRepository.save(profile);
    }

    private TrainingProfileResponse buildResponse(NutritionProfile profile,
                                                  List<UserTrainingActivity> activities,
                                                  CoachInsightResponse coachInsight) {
        BigDecimal weight = profile.getCurrentWeightKg();
        List<TrainingActivityResponse> items = activities.stream()
                .map(a -> toActivityResponse(a, weight))
                .toList();

        BigDecimal weekly = items.stream()
                .map(TrainingActivityResponse::caloriesPerWeek)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal dailyExtra = weekly.divide(BigDecimal.valueOf(7), 0, RoundingMode.HALF_UP);

        BigDecimal baseTarget = profile.getTargetCalories();
        BigDecimal previewExtra = profile.isAthleteModeEnabled() ? dailyExtra : BigDecimal.ZERO;
        BigDecimal adjusted = baseTarget != null
                ? baseTarget.add(previewExtra).setScale(0, RoundingMode.HALF_UP)
                : null;

        boolean applied = profile.getTrainingDailyExtraKcal() != null
                && profile.getTrainingDailyExtraKcal().compareTo(BigDecimal.ZERO) > 0
                && profile.isAthleteModeEnabled();

        if (applied && baseTarget != null && profile.getTrainingDailyExtraKcal() != null) {
            adjusted = baseTarget;
        }

        boolean weightRequired = profile.isAthleteModeEnabled()
                && !activities.isEmpty()
                && (weight == null || weight.compareTo(BigDecimal.ZERO) <= 0);

        return new TrainingProfileResponse(
                profile.isAthleteModeEnabled(),
                items,
                weekly,
                profile.isAthleteModeEnabled() ? dailyExtra : BigDecimal.ZERO,
                baseTarget != null && applied && profile.getTrainingDailyExtraKcal() != null
                        ? baseTarget.subtract(profile.getTrainingDailyExtraKcal()).max(BigDecimal.ZERO)
                        : baseTarget,
                adjusted,
                applied,
                weightRequired,
                coachInsight
        );
    }

    private TrainingActivityResponse toActivityResponse(UserTrainingActivity activity, BigDecimal weightKg) {
        BigDecimal perSession = activity.getSportType().caloriesPerSession(
                weightKg, activity.getMinutesPerSession());
        BigDecimal perWeek = perSession.multiply(BigDecimal.valueOf(activity.getDaysPerWeek()));
        String label = resolveActivityLabel(activity);
        return new TrainingActivityResponse(
                activity.getId(),
                activity.getSportType().name(),
                label,
                activity.getDaysPerWeek(),
                activity.getMinutesPerSession(),
                perSession,
                perWeek
        );
    }

    private String resolveActivityLabel(UserTrainingActivity activity) {
        if (activity.getCustomLabel() != null && !activity.getCustomLabel().isBlank()) {
            return activity.getCustomLabel().trim();
        }
        return activity.getSportType().labelPt();
    }

    private String normalizeCustomLabel(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private BigDecimal computeDailyExtra(BigDecimal weightKg, List<UserTrainingActivity> activities) {
        BigDecimal weekly = activities.stream()
                .map(a -> toActivityResponse(a, weightKg).caloriesPerWeek())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return weekly.divide(BigDecimal.valueOf(7), 0, RoundingMode.HALF_UP);
    }

    private SportType parseSport(String raw) {
        try {
            return SportType.valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Esporte inválido: " + raw);
        }
    }

    private NutritionProfile requireProfile(Long userId) {
        return nutritionProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(
                        "Complete o onboarding nutricional antes de configurar treinos."));
    }

    private String intensityHint(SportType sport) {
        return switch (sport) {
            case YOGA, PILATES, WALKING, STRETCHING -> "Baixa intensidade";
            case DANCE, WEIGHT_TRAINING, FOOTBALL, VOLLEYBALL, BASKETBALL, FUNCTIONAL,
                 BEACH_TENNIS, HIKING -> "Moderada";
            case RUNNING, CYCLING, SWIMMING, CROSSFIT, HIIT, MARTIAL_ARTS, TENNIS, SPINNING,
                 BOXING, ROWING -> "Alta intensidade";
            case OTHER -> "Intensidade variável";
        };
    }
}

package br.com.nutriplus.service;

import br.com.nutriplus.domain.entity.Meal;
import br.com.nutriplus.domain.entity.MealPlan;
import br.com.nutriplus.domain.entity.MealPlanGenerationJob;
import br.com.nutriplus.domain.entity.User;
import br.com.nutriplus.domain.enums.MealPlanGenerationStatus;
import br.com.nutriplus.dto.response.MealPlanGenerationStatusResponse;
import br.com.nutriplus.dto.response.MealPlanResponse;
import br.com.nutriplus.exception.ResourceNotFoundException;
import br.com.nutriplus.mapper.ResponseMapper;
import br.com.nutriplus.repository.MealPlanGenerationJobRepository;
import br.com.nutriplus.repository.MealPlanRepository;
import br.com.nutriplus.repository.UserRepository;
import br.com.nutriplus.security.AuthorizationService;
import br.com.nutriplus.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;
import br.com.nutriplus.infrastructure.config.NutriCacheNames;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MealPlanService {

    private static final List<MealPlanGenerationStatus> ACTIVE_STATUSES = List.of(
            MealPlanGenerationStatus.PENDING,
            MealPlanGenerationStatus.RUNNING
    );

    private static final String[] PROGRESS_HINTS = {
            "Analisando suas metas nutricionais…",
            "Montando refeições personalizadas…",
            "Ajustando macros e porções…",
            "Preparando lista de compras…",
            "Quase lá — finalizando seu plano…"
    };

    private static final Duration STALE_JOB_MAX_AGE = Duration.ofMinutes(15);

    private final CurrentUser currentUser;
    private final NutritionProfileService nutritionProfileService;
    private final MealPlanRepository mealPlanRepository;
    private final MealLoader mealLoader;
    private final MealPlanGenerationJobRepository jobRepository;
    private final MealPlanGenerationWorker generationWorker;
    private final ResponseMapper responseMapper;
    private final AuthorizationService authorizationService;
    private final UserRepository userRepository;
    private final MealPlanGenerationQuotaService generationQuotaService;

    public MealPlanService(CurrentUser currentUser,
                           NutritionProfileService nutritionProfileService,
                           MealPlanRepository mealPlanRepository,
                           MealLoader mealLoader,
                           MealPlanGenerationJobRepository jobRepository,
                           MealPlanGenerationWorker generationWorker,
                           ResponseMapper responseMapper,
                           AuthorizationService authorizationService,
                           UserRepository userRepository,
                           MealPlanGenerationQuotaService generationQuotaService) {
        this.currentUser = currentUser;
        this.nutritionProfileService = nutritionProfileService;
        this.mealPlanRepository = mealPlanRepository;
        this.mealLoader = mealLoader;
        this.jobRepository = jobRepository;
        this.generationWorker = generationWorker;
        this.responseMapper = responseMapper;
        this.authorizationService = authorizationService;
        this.userRepository = userRepository;
        this.generationQuotaService = generationQuotaService;
    }

    @Transactional
    public MealPlanGenerationStatusResponse enqueueGenerationForUser(Long userId) {
        authorizationService.requireCareAccessForNutritionistByPatientId(userId);
        return enqueueGenerationInternal(userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado.")));
    }

    @Transactional
    public MealPlanGenerationStatusResponse enqueueGeneration() {
        return enqueueGenerationInternal(currentUser.get());
    }

    private MealPlanGenerationStatusResponse enqueueGenerationInternal(User user) {
        nutritionProfileService.getEntityForUser(user);
        generationQuotaService.assertCanGenerate(user);

        List<MealPlanGenerationJob> active = jobRepository.findByUserIdAndStatusIn(user.getId(), ACTIVE_STATUSES);
        failStaleJobs(active);
        active = jobRepository.findByUserIdAndStatusIn(user.getId(), ACTIVE_STATUSES);
        if (!active.isEmpty()) {
            return toStatusResponse(active.getFirst());
        }

        MealPlanGenerationJob job = MealPlanGenerationJob.builder()
                .user(user)
                .status(MealPlanGenerationStatus.PENDING)
                .build();
        job = jobRepository.save(job);

        final Long jobId = job.getId();
        final Long userId = user.getId();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                generationWorker.processAsync(jobId, userId);
            }
        });
        return toStatusResponse(job);
    }

    public MealPlanGenerationStatusResponse getGenerationStatus() {
        User user = currentUser.get();
        failStaleJobs(jobRepository.findByUserIdAndStatusIn(user.getId(), ACTIVE_STATUSES));
        return jobRepository.findTopByUserIdOrderByCreatedAtDesc(user.getId())
                .map(this::toStatusResponse)
                .orElse(new MealPlanGenerationStatusResponse(
                        null,
                        MealPlanGenerationStatus.NONE,
                        null,
                        null,
                        null
                ));
    }

    @Cacheable(value = NutriCacheNames.MEAL_PLAN_LATEST, keyGenerator = "userIdCacheKeyGenerator")
    public MealPlanResponse getLatest() {
        User user = currentUser.get();
        List<MealPlan> plans = mealPlanRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        if (plans.isEmpty()) {
            throw new ResourceNotFoundException("Nenhum plano alimentar encontrado");
        }
        MealPlan plan = plans.getFirst();
        List<Meal> meals = mealLoader.mealsForPlan(plan.getId());
        return responseMapper.toMealPlanResponse(plan, meals, mealLoader.itemsByMealId(meals));
    }

    private MealPlanGenerationStatusResponse toStatusResponse(MealPlanGenerationJob job) {
        Long mealPlanId = job.getMealPlan() != null ? job.getMealPlan().getId() : null;
        return new MealPlanGenerationStatusResponse(
                job.getId(),
                job.getStatus(),
                mealPlanId,
                job.getErrorMessage(),
                progressHint(job)
        );
    }

    private String progressHint(MealPlanGenerationJob job) {
        if (job.getStatus() == MealPlanGenerationStatus.COMPLETED) {
            return "Seu plano está pronto!";
        }
        if (job.getStatus() == MealPlanGenerationStatus.FAILED) {
            return null;
        }
        LocalDateTime ref = job.getStartedAt() != null ? job.getStartedAt() : job.getCreatedAt();
        long seconds = Duration.between(ref, LocalDateTime.now()).getSeconds();
        int index = (int) Math.min(PROGRESS_HINTS.length - 1, seconds / 8);
        return PROGRESS_HINTS[index];
    }

    private void failStaleJobs(List<MealPlanGenerationJob> jobs) {
        LocalDateTime now = LocalDateTime.now();
        for (MealPlanGenerationJob job : jobs) {
            LocalDateTime ref = job.getStartedAt() != null ? job.getStartedAt() : job.getCreatedAt();
            if (ref == null) {
                continue;
            }
            Duration maxAge = job.getStatus() == MealPlanGenerationStatus.PENDING && job.getStartedAt() == null
                    ? Duration.ofMinutes(3)
                    : STALE_JOB_MAX_AGE;
            if (ref.isBefore(now.minus(maxAge))) {
                job.setStatus(MealPlanGenerationStatus.FAILED);
                job.setErrorMessage("A geração demorou demais. Tente novamente.");
                job.setCompletedAt(now);
                jobRepository.save(job);
            }
        }
    }
}

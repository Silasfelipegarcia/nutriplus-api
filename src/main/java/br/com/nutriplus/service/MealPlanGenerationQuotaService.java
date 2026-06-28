package br.com.nutriplus.service;

import br.com.nutriplus.domain.entity.User;
import br.com.nutriplus.domain.enums.MealPlanGenerationStatus;
import br.com.nutriplus.domain.enums.SubscriptionPlan;
import br.com.nutriplus.domain.enums.SubscriptionPlans;
import br.com.nutriplus.exception.BusinessException;
import br.com.nutriplus.exception.SubscriptionRequiredException;
import br.com.nutriplus.infrastructure.config.MealPlanGenerationQuotaProperties;
import br.com.nutriplus.repository.MealPlanGenerationJobRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Limites de geração de plano alimentar por tier e por janela (dia/mês).
 * Grátis/Essencial: 1/dia e 1/mês. Atleta/Trial: 3/dia. Beta (billing off): 2/dia.
 */
@Service
public class MealPlanGenerationQuotaService {

    private static final List<MealPlanGenerationStatus> QUOTA_STATUSES = List.of(
            MealPlanGenerationStatus.COMPLETED,
            MealPlanGenerationStatus.PENDING,
            MealPlanGenerationStatus.RUNNING
    );

    private final BillingEnforcementService billingEnforcementService;
    private final SubscriptionService subscriptionService;
    private final MealPlanGenerationJobRepository jobRepository;
    private final MealPlanGenerationQuotaProperties properties;

    public MealPlanGenerationQuotaService(BillingEnforcementService billingEnforcementService,
                                          SubscriptionService subscriptionService,
                                          MealPlanGenerationJobRepository jobRepository,
                                          MealPlanGenerationQuotaProperties properties) {
        this.billingEnforcementService = billingEnforcementService;
        this.subscriptionService = subscriptionService;
        this.jobRepository = jobRepository;
        this.properties = properties;
    }

    public void assertCanGenerate(User user) {
        subscriptionService.expirarSeNecessario(user);

        int dailyLimit = resolveDailyLimit(user);
        int usedToday = countGenerationsSince(user.getId(), startOfToday());
        if (usedToday >= dailyLimit) {
            throw dailyQuotaExceeded(user, dailyLimit);
        }

        if (!billingEnforcementService.isBillingEnabled()) {
            return;
        }
        if (hasUnlimitedMonthlyGenerations(user)) {
            return;
        }

        int usedThisMonth = countGenerationsSince(user.getId(), startOfMonth());
        if (usedThisMonth >= properties.limitedTierMonthlyQuota()) {
            throw monthlyQuotaExceeded(user);
        }
    }

    public int remainingGenerationsThisMonth(User user) {
        if (!billingEnforcementService.isBillingEnabled() || hasUnlimitedMonthlyGenerations(user)) {
            return -1;
        }
        int used = countGenerationsSince(user.getId(), startOfMonth());
        return Math.max(0, properties.limitedTierMonthlyQuota() - used);
    }

    public int remainingGenerationsToday(User user) {
        int used = countGenerationsSince(user.getId(), startOfToday());
        return Math.max(0, resolveDailyLimit(user) - used);
    }

    private int resolveDailyLimit(User user) {
        if (!billingEnforcementService.isBillingEnabled()) {
            return properties.betaDailyQuota();
        }
        if (hasUnlimitedMonthlyGenerations(user)) {
            return properties.unlimitedTierDailyQuota();
        }
        return properties.limitedTierDailyQuota();
    }

    private boolean hasUnlimitedMonthlyGenerations(User user) {
        if (subscriptionService.emTrial(user)) {
            return true;
        }
        if (!subscriptionService.temAssinaturaPaga(user)) {
            return false;
        }
        SubscriptionPlan plan = subscriptionService.resolverPlanoEfetivo(user);
        return SubscriptionPlans.isAthletePlan(plan);
    }

    private int countGenerationsSince(Long userId, LocalDateTime since) {
        return (int) jobRepository.countByUserIdAndStatusInAndCreatedAtGreaterThanEqual(
                userId, QUOTA_STATUSES, since);
    }

    private static LocalDateTime startOfToday() {
        return LocalDate.now().atStartOfDay();
    }

    private static LocalDateTime startOfMonth() {
        return LocalDate.now().withDayOfMonth(1).atStartOfDay();
    }

    private BusinessException dailyQuotaExceeded(User user, int dailyLimit) {
        if (!billingEnforcementService.isBillingEnabled()) {
            return new BusinessException(
                    "Você atingiu o limite de " + dailyLimit + " gerações de plano por dia. "
                            + "Tente novamente amanhã.");
        }
        if (hasUnlimitedMonthlyGenerations(user)) {
            return new BusinessException(
                    "Você já gerou " + dailyLimit + " planos hoje. "
                            + "Amanhã você pode gerar de novo — ou ajuste preferências sem regerar o plano inteiro.");
        }
        return new BusinessException(
                "Você já gerou seu plano de hoje. "
                        + "Tente novamente amanhã ou faça upgrade para o plano Atleta para mais regenerações diárias.");
    }

    private SubscriptionRequiredException monthlyQuotaExceeded(User user) {
        SubscriptionPlan plan = subscriptionService.resolverPlanoEfetivo(user);
        if (plan == SubscriptionPlan.FREE) {
            return new SubscriptionRequiredException(
                    "Você atingiu o limite de 1 plano por mês no plano gratuito. "
                            + "Assine o Nutri+ Essencial ou inicie o trial de 7 dias.");
        }
        if (SubscriptionPlans.isEssentialPlan(plan)) {
            return new SubscriptionRequiredException(
                    "Você já gerou seu plano deste mês no Essencial. "
                            + "Faça upgrade para Atleta para regenerações diárias extras.");
        }
        return new SubscriptionRequiredException(
                "Limite de gerações de plano atingido. Assine um plano para continuar.");
    }
}

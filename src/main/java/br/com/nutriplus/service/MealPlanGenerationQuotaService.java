package br.com.nutriplus.service;

import br.com.nutriplus.domain.entity.User;
import br.com.nutriplus.domain.enums.MealPlanGenerationStatus;
import br.com.nutriplus.domain.enums.SubscriptionPlan;
import br.com.nutriplus.domain.enums.SubscriptionPlans;
import br.com.nutriplus.exception.SubscriptionRequiredException;
import br.com.nutriplus.repository.MealPlanGenerationJobRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Limites de geração de plano alimentar por tier (Opção A).
 * Grátis/Essencial: 1/mês. Atleta/Trial: ilimitado. Beta (billing off): ilimitado.
 */
@Service
public class MealPlanGenerationQuotaService {

    private static final int LIMITED_TIER_MONTHLY_QUOTA = 1;

    private static final List<MealPlanGenerationStatus> QUOTA_STATUSES = List.of(
            MealPlanGenerationStatus.COMPLETED,
            MealPlanGenerationStatus.PENDING,
            MealPlanGenerationStatus.RUNNING
    );

    private final BillingEnforcementService billingEnforcementService;
    private final SubscriptionService subscriptionService;
    private final MealPlanGenerationJobRepository jobRepository;

    public MealPlanGenerationQuotaService(BillingEnforcementService billingEnforcementService,
                                          SubscriptionService subscriptionService,
                                          MealPlanGenerationJobRepository jobRepository) {
        this.billingEnforcementService = billingEnforcementService;
        this.subscriptionService = subscriptionService;
        this.jobRepository = jobRepository;
    }

    public void assertCanGenerate(User user) {
        if (!billingEnforcementService.isBillingEnabled()) {
            return;
        }
        subscriptionService.expirarSeNecessario(user);
        if (hasUnlimitedGenerations(user)) {
            return;
        }
        int used = countGenerationsThisMonth(user.getId());
        if (used >= LIMITED_TIER_MONTHLY_QUOTA) {
            throw quotaExceeded(user);
        }
    }

    public int remainingGenerationsThisMonth(User user) {
        if (!billingEnforcementService.isBillingEnabled() || hasUnlimitedGenerations(user)) {
            return -1;
        }
        int used = countGenerationsThisMonth(user.getId());
        return Math.max(0, LIMITED_TIER_MONTHLY_QUOTA - used);
    }

    private boolean hasUnlimitedGenerations(User user) {
        if (subscriptionService.emTrial(user)) {
            return true;
        }
        if (!subscriptionService.temAssinaturaPaga(user)) {
            return false;
        }
        SubscriptionPlan plan = subscriptionService.resolverPlanoEfetivo(user);
        return SubscriptionPlans.isAthletePlan(plan);
    }

    private int countGenerationsThisMonth(Long userId) {
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        return (int) jobRepository.countByUserIdAndStatusInAndCreatedAtGreaterThanEqual(
                userId, QUOTA_STATUSES, startOfMonth);
    }

    private SubscriptionRequiredException quotaExceeded(User user) {
        SubscriptionPlan plan = subscriptionService.resolverPlanoEfetivo(user);
        if (plan == SubscriptionPlan.FREE) {
            return new SubscriptionRequiredException(
                    "Você atingiu o limite de 1 plano por mês no plano gratuito. "
                            + "Assine o Nutri+ Essencial ou inicie o trial de 7 dias.");
        }
        if (SubscriptionPlans.isEssentialPlan(plan)) {
            return new SubscriptionRequiredException(
                    "Você já gerou seu plano deste mês no Essencial. "
                            + "Faça upgrade para Atleta para regenerações ilimitadas.");
        }
        return new SubscriptionRequiredException(
                "Limite de gerações de plano atingido. Assine um plano para continuar.");
    }
}

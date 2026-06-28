package br.com.nutriplus.service;

import br.com.nutriplus.domain.entity.NutritionProfile;
import br.com.nutriplus.domain.entity.User;
import br.com.nutriplus.domain.enums.SubscriptionPlan;
import br.com.nutriplus.domain.enums.SubscriptionStatus;
import br.com.nutriplus.dto.response.PlanQuoteResponse;
import br.com.nutriplus.dto.response.SubscriptionStatusResponse;
import br.com.nutriplus.repository.NutritionProfileRepository;
import br.com.nutriplus.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

@Service
public class SubscriptionService {

    private static final int PERIODO_MENSAL_DIAS = 30;
    private static final int PERIODO_ANUAL_DIAS = 365;

    private final UserRepository userRepository;
    private final NutritionProfileRepository nutritionProfileRepository;
    private final SubscriptionPlanCatalogService planCatalogService;
    private final BillingEnforcementService billingEnforcementService;

    public SubscriptionService(UserRepository userRepository,
                               NutritionProfileRepository nutritionProfileRepository,
                               SubscriptionPlanCatalogService planCatalogService,
                               BillingEnforcementService billingEnforcementService) {
        this.userRepository = userRepository;
        this.nutritionProfileRepository = nutritionProfileRepository;
        this.planCatalogService = planCatalogService;
        this.billingEnforcementService = billingEnforcementService;
    }

    @Transactional
    public void ativarPeriodoPago(User user, SubscriptionPlan plan, Instant paidAt, boolean renewal) {
        Instant base = user.getPlanValidUntil() != null && user.getPlanValidUntil().isAfter(paidAt)
                ? user.getPlanValidUntil()
                : paidAt;

        user.setSubscriptionPlan(plan);
        user.setPlanValidUntil(base.plus(periodoDias(plan), ChronoUnit.DAYS));
        user.setAutoRenew(true);
        user.setPlanCancelledAt(null);
        user.setTrialAte(null);
        userRepository.save(user);
        syncAthleteModeOnActivation(user);
    }

    @Transactional
    public void aplicarUpgrade(User user, SubscriptionPlan novoPlano) {
        user.setSubscriptionPlan(novoPlano);
        user.setAutoRenew(true);
        user.setPlanCancelledAt(null);
        user.setTrialAte(null);
        userRepository.save(user);
        syncAthleteModeOnActivation(user);
    }

    public boolean ehUpgradeProporcional(User user, SubscriptionPlan targetPlan) {
        return targetPlan == SubscriptionPlan.ATHLETE_YEARLY
                && resolverPlanoEfetivo(user) == SubscriptionPlan.ATHLETE_MONTHLY
                && temAssinaturaPaga(user);
    }

    public int calcularValorCobranca(User user, SubscriptionPlan targetPlan) {
        int precoCheio = precoPlano(targetPlan);
        if (ehUpgradeProporcional(user, targetPlan)) {
            int diff = precoPlano(SubscriptionPlan.ATHLETE_YEARLY) - precoPlano(SubscriptionPlan.ATHLETE_MONTHLY);
            long dias = ChronoUnit.DAYS.between(Instant.now(), user.getPlanValidUntil());
            dias = Math.max(1, Math.min(dias, planCatalogService.monthlyPeriodDays()));
            int proporcional = (int) Math.round(diff * (dias / (double) PERIODO_MENSAL_DIAS));
            return Math.max(100, proporcional);
        }
        return precoCheio;
    }

    public PlanQuoteResponse montarCotacao(User user, SubscriptionPlan targetPlan) {
        validarPlanoPago(targetPlan);
        int fullPrice = precoPlano(targetPlan);
        int amount = calcularValorCobranca(user, targetPlan);
        boolean upgrade = ehUpgradeProporcional(user, targetPlan);

        PlanQuoteResponse quote = new PlanQuoteResponse();
        quote.setPlan(targetPlan);
        quote.setAmountCents(amount);
        quote.setAmountLabel(formatarValor(amount));
        quote.setFullPriceCents(fullPrice);
        quote.setFullPriceLabel(formatarValor(fullPrice));
        quote.setUpgrade(upgrade);
        if (upgrade && user.getPlanValidUntil() != null) {
            long dias = ChronoUnit.DAYS.between(Instant.now(), user.getPlanValidUntil());
            quote.setDescription("Diferença proporcional aos " + Math.max(1, dias) + " dias restantes do período");
        }
        return quote;
    }

    public void definirCartaoPadrao(User user, String cardId) {
        if (cardId != null && !cardId.isBlank()) {
            user.setDefaultCardId(cardId);
            userRepository.save(user);
        }
    }

    @Transactional
    public User cancelar(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        if (!temAssinaturaPaga(user) && !emTrial(user)) {
            throw new IllegalStateException("Você não possui uma assinatura ativa para cancelar.");
        }
        if (user.getPlanCancelledAt() != null) {
            throw new IllegalStateException("A renovação automática já está cancelada.");
        }

        user.setAutoRenew(false);
        user.setPlanCancelledAt(Instant.now());
        return userRepository.save(user);
    }

    @Transactional
    public User reativar(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        if (user.getPlanCancelledAt() == null) {
            throw new IllegalStateException("Não há cancelamento pendente para reativar.");
        }
        if (!periodoAindaValido(user) && !emTrial(user)) {
            throw new IllegalStateException("O período da assinatura já expirou. Assine um plano novamente.");
        }

        user.setAutoRenew(true);
        user.setPlanCancelledAt(null);
        return userRepository.save(user);
    }

    @Transactional
    public void expirarSeNecessario(User user) {
        if (user.getPlanValidUntil() == null) {
            return;
        }
        if (Instant.now().isAfter(user.getPlanValidUntil()) && isPlanoPago(user.getSubscriptionPlan())) {
            user.setSubscriptionPlan(SubscriptionPlan.FREE);
            user.setPlanValidUntil(null);
            user.setPlanCancelledAt(null);
            user.setAutoRenew(false);
            user.setDefaultCardId(null);
            userRepository.save(user);
            syncAthleteModeOnExpiry(user);
        }
    }

    public SubscriptionPlan resolverPlanoEfetivo(User user) {
        if (!isPlanoPago(user.getSubscriptionPlan())) {
            return user.getSubscriptionPlan();
        }
        if (user.getPlanValidUntil() != null && Instant.now().isAfter(user.getPlanValidUntil())) {
            return SubscriptionPlan.FREE;
        }
        return user.getSubscriptionPlan();
    }

    public SubscriptionStatus statusAssinatura(User user) {
        if (emTrial(user)) {
            return SubscriptionStatus.TRIAL;
        }
        if (!isPlanoPago(user.getSubscriptionPlan()) || user.getPlanValidUntil() == null) {
            if (user.getPlanValidUntil() != null
                    && Instant.now().isAfter(user.getPlanValidUntil())
                    && isPlanoPago(user.getSubscriptionPlan())) {
                return SubscriptionStatus.EXPIRED;
            }
            return SubscriptionStatus.NONE;
        }
        if (Instant.now().isAfter(user.getPlanValidUntil())) {
            return SubscriptionStatus.EXPIRED;
        }
        if (user.getPlanCancelledAt() != null || !user.isAutoRenew()) {
            return SubscriptionStatus.CANCELLED_PENDING;
        }
        return SubscriptionStatus.ACTIVE;
    }

    public SubscriptionStatusResponse montarStatus(User user) {
        SubscriptionStatusResponse response = new SubscriptionStatusResponse();
        response.setBillingEnforced(billingEnforcementService.isBillingEnabled());
        SubscriptionStatus status = statusAssinatura(user);
        response.setStatus(status.name());
        response.setAutoRenew(user.isAutoRenew());
        response.setValidUntil(user.getPlanValidUntil());
        response.setCancelledAt(user.getPlanCancelledAt());
        response.setDefaultCardId(user.getDefaultCardId());
        response.setPlan(user.getSubscriptionPlan());
        response.setPlanNome(planoNome(user.getSubscriptionPlan()));
        response.setTrialDisponivel(!user.isTrialUtilizado() && user.getTrialAte() == null);
        response.setEmTrial(emTrial(user));

        if (user.getPlanValidUntil() != null && periodoAindaValido(user)) {
            long dias = ChronoUnit.DAYS.between(Instant.now(), user.getPlanValidUntil());
            response.setDaysRemaining(Math.max(0, dias));
        } else if (emTrial(user) && user.getTrialAte() != null) {
            long dias = ChronoUnit.DAYS.between(Instant.now(), user.getTrialAte());
            response.setDaysRemaining(Math.max(0, dias));
        } else {
            response.setDaysRemaining(0);
        }

        response.setPodeCancelar(status == SubscriptionStatus.ACTIVE || status == SubscriptionStatus.TRIAL);
        response.setPodeReativar(status == SubscriptionStatus.CANCELLED_PENDING);
        return response;
    }

    public boolean temAssinaturaPaga(User user) {
        return isPlanoPago(user.getSubscriptionPlan()) && periodoAindaValido(user);
    }

    public boolean temAcessoAtleta(User user) {
        return temAssinaturaPaga(user) || emTrial(user);
    }

    public boolean emTrial(User user) {
        return user.getTrialAte() != null && Instant.now().isBefore(user.getTrialAte())
                && (user.getPlanValidUntil() == null || !Instant.now().isBefore(user.getPlanValidUntil()));
    }

    public boolean periodoAindaValido(User user) {
        return user.getPlanValidUntil() != null && !Instant.now().isAfter(user.getPlanValidUntil());
    }

    public int precoPlano(SubscriptionPlan plan) {
        return planCatalogService.priceCents(plan);
    }

    public String planoNome(SubscriptionPlan plan) {
        return planCatalogService.planName(plan);
    }

    private int periodoDias(SubscriptionPlan plan) {
        int dias = planCatalogService.periodDays(plan);
        if (dias > 0) {
            return dias;
        }
        return plan == SubscriptionPlan.ATHLETE_YEARLY ? PERIODO_ANUAL_DIAS : PERIODO_MENSAL_DIAS;
    }

    private void validarPlanoPago(SubscriptionPlan plan) {
        if (plan != SubscriptionPlan.ATHLETE_MONTHLY && plan != SubscriptionPlan.ATHLETE_YEARLY) {
            throw new IllegalArgumentException("Plano inválido para cotação");
        }
    }

    private boolean isPlanoPago(SubscriptionPlan plan) {
        return plan == SubscriptionPlan.ATHLETE_MONTHLY || plan == SubscriptionPlan.ATHLETE_YEARLY;
    }

    private String formatarValor(int amountCents) {
        double valor = amountCents / 100.0;
        return String.format(Locale.forLanguageTag("pt-BR"), "R$ %.2f", valor);
    }

    private void syncAthleteModeOnActivation(User user) {
        nutritionProfileRepository.findByUserId(user.getId()).ifPresent(profile -> {
            if (!profile.isAthleteModeEnabled()) {
                profile.setAthleteModeEnabled(true);
                nutritionProfileRepository.save(profile);
            }
        });
    }

    private void syncAthleteModeOnExpiry(User user) {
        if (temAcessoAtleta(user)) {
            return;
        }
        if (user.getAthleteGraceUntil() != null && Instant.now().isBefore(user.getAthleteGraceUntil())) {
            return;
        }
        nutritionProfileRepository.findByUserId(user.getId()).ifPresent(profile -> {
            if (profile.isAthleteModeEnabled()) {
                profile.setAthleteModeEnabled(false);
                profile.setTrainingDailyExtraKcal(null);
                nutritionProfileRepository.save(profile);
            }
        });
    }
}

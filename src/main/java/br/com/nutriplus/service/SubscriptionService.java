package br.com.nutriplus.service;

import br.com.nutriplus.domain.entity.NutritionProfile;
import br.com.nutriplus.domain.entity.User;
import br.com.nutriplus.domain.enums.SubscriptionPlan;
import br.com.nutriplus.domain.enums.SubscriptionPlans;
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
        user.setTrialUtilizado(true);
        userRepository.save(user);
        syncTierFeatures(user, plan);
    }

    @Transactional
    public void aplicarUpgrade(User user, SubscriptionPlan novoPlano) {
        user.setSubscriptionPlan(novoPlano);
        user.setAutoRenew(true);
        user.setPlanCancelledAt(null);
        user.setTrialAte(null);
        userRepository.save(user);
        syncTierFeatures(user, novoPlano);
    }

    public boolean ehUpgradeProporcional(User user, SubscriptionPlan targetPlan) {
        if (!temAssinaturaPaga(user)) {
            return false;
        }
        SubscriptionPlan current = resolverPlanoEfetivo(user);
        if (targetPlan == SubscriptionPlan.ATHLETE_YEARLY && current == SubscriptionPlan.ATHLETE_MONTHLY) {
            return true;
        }
        if (targetPlan == SubscriptionPlan.ESSENTIAL_YEARLY && current == SubscriptionPlan.ESSENTIAL_MONTHLY) {
            return true;
        }
        if (SubscriptionPlans.isAthletePlan(targetPlan) && SubscriptionPlans.isEssentialPlan(current)) {
            return SubscriptionPlans.isMonthlyPlan(current) == SubscriptionPlans.isMonthlyPlan(targetPlan)
                    || SubscriptionPlans.isYearlyPlan(current) && SubscriptionPlans.isYearlyPlan(targetPlan);
        }
        return false;
    }

    public int calcularValorCobranca(User user, SubscriptionPlan targetPlan) {
        int precoCheio = precoPlano(targetPlan);
        if (!ehUpgradeProporcional(user, targetPlan)) {
            return precoCheio;
        }
        SubscriptionPlan current = resolverPlanoEfetivo(user);
        int diff = precoPlano(targetPlan) - precoPlano(current);
        long dias = ChronoUnit.DAYS.between(Instant.now(), user.getPlanValidUntil());
        int periodoAtual = periodoDias(current);
        dias = Math.max(1, Math.min(dias, periodoAtual));
        int proporcional = (int) Math.round(diff * (dias / (double) periodoAtual));
        return Math.max(100, proporcional);
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
        if (Instant.now().isAfter(user.getPlanValidUntil()) && SubscriptionPlans.isPaidB2cPlan(user.getSubscriptionPlan())) {
            user.setSubscriptionPlan(SubscriptionPlan.FREE);
            user.setPlanValidUntil(null);
            user.setPlanCancelledAt(null);
            user.setAutoRenew(false);
            user.setDefaultCardId(null);
            userRepository.save(user);
            syncTierFeatures(user, SubscriptionPlan.FREE);
        }
    }

    public SubscriptionPlan resolverPlanoEfetivo(User user) {
        if (!SubscriptionPlans.isPaidB2cPlan(user.getSubscriptionPlan())) {
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
        if (!SubscriptionPlans.isPaidB2cPlan(user.getSubscriptionPlan()) || user.getPlanValidUntil() == null) {
            if (user.getPlanValidUntil() != null
                    && Instant.now().isAfter(user.getPlanValidUntil())
                    && SubscriptionPlans.isPaidB2cPlan(user.getSubscriptionPlan())) {
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
        response.setStatusLabel(rotuloStatusAssinatura(status));
        response.setAutoRenew(user.isAutoRenew());
        if (emTrial(user) && user.getTrialAte() != null) {
            response.setValidUntil(user.getTrialAte());
        } else {
            response.setValidUntil(user.getPlanValidUntil());
        }
        response.setCancelledAt(user.getPlanCancelledAt());
        response.setDefaultCardId(user.getDefaultCardId());
        response.setPlan(user.getSubscriptionPlan());
        response.setPlanNome(planoNome(user.getSubscriptionPlan()));
        response.setTrialDisponivel(trialDisponivel(user, status));
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
        return SubscriptionPlans.isPaidB2cPlan(user.getSubscriptionPlan()) && periodoAindaValido(user);
    }

    /** Modo atleta: plano Atleta pago ou trial (acesso total durante trial). */
    public boolean temAcessoAtleta(User user) {
        if (emTrial(user)) {
            return true;
        }
        if (!temAssinaturaPaga(user)) {
            return false;
        }
        return SubscriptionPlans.isAthletePlan(resolverPlanoEfetivo(user));
    }

    /** Essencial ou superior (pago ou trial). */
    public boolean temAcessoEssencial(User user) {
        if (emTrial(user)) {
            return true;
        }
        if (!temAssinaturaPaga(user)) {
            return false;
        }
        SubscriptionPlan plan = resolverPlanoEfetivo(user);
        return SubscriptionPlans.isEssentialTier(plan) || SubscriptionPlans.isAthletePlan(plan);
    }

    public boolean emTrial(User user) {
        return user.getTrialAte() != null && Instant.now().isBefore(user.getTrialAte())
                && (user.getPlanValidUntil() == null || !Instant.now().isBefore(user.getPlanValidUntil()));
    }

    public boolean periodoAindaValido(User user) {
        return user.getPlanValidUntil() != null && !Instant.now().isAfter(user.getPlanValidUntil());
    }

    /** Trial só para quem nunca usou e ainda não tem assinatura paga ativa. */
    public boolean trialDisponivel(User user, SubscriptionStatus status) {
        if (user.isTrialUtilizado()) {
            return false;
        }
        if (emTrial(user)) {
            return false;
        }
        if (status == SubscriptionStatus.ACTIVE || status == SubscriptionStatus.CANCELLED_PENDING) {
            return false;
        }
        if (temAssinaturaPaga(user)) {
            return false;
        }
        return true;
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
        return SubscriptionPlans.isYearlyPlan(plan) ? PERIODO_ANUAL_DIAS : PERIODO_MENSAL_DIAS;
    }

    private void validarPlanoPago(SubscriptionPlan plan) {
        if (!SubscriptionPlans.isPaidB2cPlan(plan)) {
            throw new IllegalArgumentException("Plano inválido para cotação");
        }
    }

    private void syncTierFeatures(User user, SubscriptionPlan plan) {
        if (SubscriptionPlans.isAthletePlan(plan)) {
            syncAthleteModeOnActivation(user);
        } else {
            syncAthleteModeOnExpiry(user);
        }
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

    private String rotuloStatusAssinatura(SubscriptionStatus status) {
        if (status == null) {
            return "—";
        }
        return switch (status) {
            case ACTIVE -> "Ativo";
            case TRIAL -> "Período de teste";
            case CANCELLED_PENDING -> "Cancelamento agendado";
            case EXPIRED -> "Expirado";
            case NONE -> "Gratuito";
        };
    }
}

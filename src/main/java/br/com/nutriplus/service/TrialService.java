package br.com.nutriplus.service;

import br.com.nutriplus.domain.entity.User;
import br.com.nutriplus.domain.enums.SubscriptionPlan;
import br.com.nutriplus.domain.enums.SubscriptionPlans;
import br.com.nutriplus.dto.request.ChargePlanRequest;
import br.com.nutriplus.dto.response.ChargePlanResponse;
import br.com.nutriplus.payment.MercadoPagoPaymentService;
import br.com.nutriplus.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class TrialService {

    private static final Logger log = LoggerFactory.getLogger(TrialService.class);

    private final UserRepository userRepository;
    private final MercadoPagoPaymentService paymentService;
    private final AthleteAccessService athleteAccessService;
    private final BillingEnforcementService billingEnforcementService;

    public TrialService(UserRepository userRepository,
                        @Lazy MercadoPagoPaymentService paymentService,
                        AthleteAccessService athleteAccessService,
                        BillingEnforcementService billingEnforcementService) {
        this.userRepository = userRepository;
        this.paymentService = paymentService;
        this.athleteAccessService = athleteAccessService;
        this.billingEnforcementService = billingEnforcementService;
    }

    @Transactional
    public User iniciarTrial(Long userId) {
        billingEnforcementService.requireBillingEnabled();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        if (user.isTrialUtilizado()) {
            throw new IllegalStateException("Você já utilizou o período de teste gratuito.");
        }

        if (user.getDefaultCardId() == null || user.getDefaultCardId().isBlank()) {
            throw new IllegalStateException(
                    "Cadastre um cartão em Cobrança antes de iniciar o trial de 7 dias.");
        }

        user.setTrialUtilizado(true);
        user.setTrialAte(Instant.now().plus(7, ChronoUnit.DAYS));
        user.setSubscriptionPlan(SubscriptionPlan.ESSENTIAL_MONTHLY);
        user.setAutoRenew(true);
        user = userRepository.save(user);
        return user;
    }

    @Transactional
    public void expirarTrialSeNecessario(User user) {
        if (user.getTrialAte() == null || !Instant.now().isAfter(user.getTrialAte())) {
            return;
        }
        if (user.getPlanValidUntil() != null && Instant.now().isBefore(user.getPlanValidUntil())) {
            user.setTrialAte(null);
            userRepository.save(user);
            return;
        }

        if (tentarCobrarAposTrial(user)) {
            user.setTrialAte(null);
            userRepository.save(user);
            athleteAccessService.syncAthleteModeOnExpiry(user);
            return;
        }

        user.setSubscriptionPlan(SubscriptionPlan.FREE);
        user.setTrialAte(null);
        user.setAutoRenew(false);
        userRepository.save(user);
        athleteAccessService.syncAthleteModeOnExpiry(user);
        log.info("Trial expirado sem cobrança para {}", user.getEmail());
    }

    public boolean trialDisponivel(User user) {
        return !user.isTrialUtilizado() && user.getTrialAte() == null;
    }

    private boolean tentarCobrarAposTrial(User user) {
        if (!user.isAutoRenew()
                || user.getDefaultCardId() == null
                || user.getDefaultCardId().isBlank()) {
            return false;
        }
        try {
            ChargePlanRequest request = new ChargePlanRequest();
            request.setPlan(SubscriptionPlan.ESSENTIAL_MONTHLY);
            request.setCardId(user.getDefaultCardId());
            request.setRenewal(true);
            ChargePlanResponse response = paymentService.cobrarPlano(user.getId(), request);
            boolean aprovado = "APPROVED".equalsIgnoreCase(response.getStatus());
            if (aprovado) {
                log.info("Trial convertido em Essencial para {}", user.getEmail());
            }
            return aprovado;
        } catch (Exception e) {
            log.warn("Cobrança pós-trial falhou para {}: {}", user.getEmail(), e.getMessage());
            return false;
        }
    }
}

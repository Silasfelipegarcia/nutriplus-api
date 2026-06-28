package br.com.nutriplus.service;

import br.com.nutriplus.domain.entity.NutritionProfile;
import br.com.nutriplus.domain.entity.User;
import br.com.nutriplus.domain.enums.SubscriptionPlan;
import br.com.nutriplus.domain.enums.SubscriptionStatus;
import br.com.nutriplus.infrastructure.config.MercadoPagoProperties;
import br.com.nutriplus.repository.NutritionProfileRepository;
import br.com.nutriplus.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class AthleteAccessService {

    private final UserRepository userRepository;
    private final NutritionProfileRepository nutritionProfileRepository;
    private final SubscriptionService subscriptionService;
    private final BillingEnforcementService billingEnforcementService;

    public AthleteAccessService(UserRepository userRepository,
                                NutritionProfileRepository nutritionProfileRepository,
                                SubscriptionService subscriptionService,
                                BillingEnforcementService billingEnforcementService) {
        this.userRepository = userRepository;
        this.nutritionProfileRepository = nutritionProfileRepository;
        this.subscriptionService = subscriptionService;
        this.billingEnforcementService = billingEnforcementService;
    }

    public boolean hasAthleteAccess(User user) {
        if (!billingEnforcementService.isBillingEnabled()) {
            return true;
        }
        subscriptionService.expirarSeNecessario(user);
        if (subscriptionService.temAcessoAtleta(user)) {
            return true;
        }
        return user.getAthleteGraceUntil() != null && Instant.now().isBefore(user.getAthleteGraceUntil());
    }

    @Transactional
    public void syncAthleteModeOnActivation(User user) {
        nutritionProfileRepository.findByUserId(user.getId()).ifPresent(profile -> {
            if (!profile.isAthleteModeEnabled()) {
                profile.setAthleteModeEnabled(true);
                nutritionProfileRepository.save(profile);
            }
        });
    }

    @Transactional
    public void syncAthleteModeOnExpiry(User user) {
        if (hasAthleteAccess(user)) {
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

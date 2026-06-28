package br.com.nutriplus.service;

import br.com.nutriplus.domain.entity.NutritionProfile;
import br.com.nutriplus.domain.enums.AiPlanIneligibleReason;
import br.com.nutriplus.domain.enums.PregnancyStatus;
import br.com.nutriplus.exception.BusinessException;
import br.com.nutriplus.infrastructure.config.LegalProperties;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class HealthEligibilityService {

    private final LegalProperties legalProperties;

    public HealthEligibilityService(LegalProperties legalProperties) {
        this.legalProperties = legalProperties;
    }

    public record EligibilityInfo(
            boolean aiPlanEligible,
            String aiPlanIneligibleReason,
            String aiPlanIneligibleMessagePt
    ) {
    }

    public void evaluateAndApply(NutritionProfile profile,
                                 PregnancyStatus pregnancyStatus,
                                 Boolean eatingDisorderRisk,
                                 Boolean severeRenalRestriction) {
        PregnancyStatus status = pregnancyStatus != null ? pregnancyStatus : PregnancyStatus.NONE;
        profile.setPregnancyStatus(status);
        profile.setEatingDisorderRisk(Boolean.TRUE.equals(eatingDisorderRisk));
        profile.setSevereRenalRestriction(Boolean.TRUE.equals(severeRenalRestriction));

        EligibilityInfo info = computeEligibility(
                status,
                profile.isEatingDisorderRisk(),
                profile.isSevereRenalRestriction()
        );
        profile.setAiPlanEligible(info.aiPlanEligible());
        profile.setAiPlanIneligibleReason(info.aiPlanIneligibleReason());
        profile.setHealthEligibilityAckAt(LocalDateTime.now());
        profile.setHealthEligibilityVersion(legalProperties.healthEligibilityVersion());
    }

    public EligibilityInfo getEligibilityInfo(NutritionProfile profile) {
        return computeEligibility(
                profile.getPregnancyStatus() != null ? profile.getPregnancyStatus() : PregnancyStatus.NONE,
                profile.isEatingDisorderRisk(),
                profile.isSevereRenalRestriction()
        );
    }

    public void assertAiPlanAllowed(NutritionProfile profile) {
        EligibilityInfo info = getEligibilityInfo(profile);
        if (!info.aiPlanEligible()) {
            throw new BusinessException(info.aiPlanIneligibleMessagePt());
        }
    }

    private EligibilityInfo computeEligibility(PregnancyStatus pregnancyStatus,
                                               boolean eatingDisorderRisk,
                                               boolean severeRenalRestriction) {
        List<AiPlanIneligibleReason> reasons = new ArrayList<>();
        if (pregnancyStatus == PregnancyStatus.PREGNANT) {
            reasons.add(AiPlanIneligibleReason.PREGNANCY);
        }
        if (pregnancyStatus == PregnancyStatus.BREASTFEEDING) {
            reasons.add(AiPlanIneligibleReason.BREASTFEEDING);
        }
        if (eatingDisorderRisk) {
            reasons.add(AiPlanIneligibleReason.EATING_DISORDER);
        }
        if (severeRenalRestriction) {
            reasons.add(AiPlanIneligibleReason.SEVERE_RENAL);
        }

        if (reasons.isEmpty()) {
            return new EligibilityInfo(true, null, null);
        }

        AiPlanIneligibleReason code = reasons.size() > 1
                ? AiPlanIneligibleReason.MULTIPLE
                : reasons.getFirst();
        return new EligibilityInfo(false, code.name(), messageFor(code));
    }

    private String messageFor(AiPlanIneligibleReason reason) {
        return switch (reason) {
            case PREGNANCY -> "Planos automáticos por IA não estão disponíveis durante a gravidez. "
                    + "Consulte um nutricionista pelo NutriPlus para acompanhamento personalizado.";
            case BREASTFEEDING -> "Planos automáticos por IA não estão disponíveis durante a amamentação. "
                    + "Consulte um nutricionista pelo NutriPlus para acompanhamento personalizado.";
            case EATING_DISORDER -> "Planos automáticos por IA não estão disponíveis para quem tem transtorno alimentar "
                    + "diagnosticado ou em tratamento. Consulte um nutricionista pelo NutriPlus.";
            case SEVERE_RENAL -> "Planos automáticos por IA não estão disponíveis para restrições renais graves "
                    + "prescritas. Consulte um nutricionista ou médico pelo NutriPlus.";
            case MULTIPLE -> "Seu perfil indica condições que exigem acompanhamento profissional. "
                    + "Planos automáticos por IA não estão disponíveis — consulte um nutricionista pelo NutriPlus.";
        };
    }
}

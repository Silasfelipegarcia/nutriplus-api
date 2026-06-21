package br.com.nutriplus.service;

import br.com.nutriplus.domain.entity.NutritionProfile;
import br.com.nutriplus.domain.enums.Goal;
import br.com.nutriplus.domain.enums.LifeStage;
import br.com.nutriplus.domain.enums.Sex;
import br.com.nutriplus.domain.util.LifeStageUtil;
import br.com.nutriplus.dto.response.BodyMeasurementResponse;
import br.com.nutriplus.dto.response.HealthIndicatorResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Component
public class HealthReferenceService {

    public static final String HEALTH_DISCLAIMER =
            "Referências baseadas em critérios da OMS e do Ministério da Saúde. "
                    + "Não substituem avaliação de nutricionista ou médico.";

    private static final BigDecimal BMI_UNDERWEIGHT = new BigDecimal("18.5");
    private static final BigDecimal BMI_NORMAL_MAX = new BigDecimal("24.9");
    private static final BigDecimal BMI_OVERWEIGHT = new BigDecimal("25");
    private static final BigDecimal BMI_OBESE_I = new BigDecimal("30");
    private static final BigDecimal BMI_OBESE_II = new BigDecimal("35");
    private static final BigDecimal BMI_OBESE_III = new BigDecimal("40");

    private static final BigDecimal MALE_WAIST_ATTENTION = new BigDecimal("94");
    private static final BigDecimal MALE_WAIST_HIGH = new BigDecimal("102");
    private static final BigDecimal FEMALE_WAIST_ATTENTION = new BigDecimal("80");
    private static final BigDecimal FEMALE_WAIST_HIGH = new BigDecimal("88");

    private static final BigDecimal MALE_WH_RATIO_LIMIT = new BigDecimal("0.90");
    private static final BigDecimal FEMALE_WH_RATIO_LIMIT = new BigDecimal("0.85");

    public BigDecimal calculateBmi(BigDecimal weightKg, BigDecimal heightCm) {
        if (weightKg == null || heightCm == null || heightCm.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        BigDecimal heightM = heightCm.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
        BigDecimal heightSquared = heightM.multiply(heightM);
        if (heightSquared.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        return weightKg.divide(heightSquared, 1, RoundingMode.HALF_UP);
    }

    public HealthIndicatorResponse buildBmiSnapshot(NutritionProfile profile) {
        BigDecimal bmi = calculateBmi(profile.getCurrentWeightKg(), profile.getHeightCm());
        if (bmi == null) {
            return null;
        }
        return buildBmiIndicator(profile, bmi, bmi, profile.getGoal());
    }

    public List<HealthIndicatorResponse> buildHealthSnapshot(
            NutritionProfile profile,
            BodyMeasurementResponse baseline,
            BodyMeasurementResponse latest
    ) {
        List<HealthIndicatorResponse> indicators = new ArrayList<>();
        Goal goal = profile.getGoal();
        BigDecimal heightCm = profile.getHeightCm();

        BigDecimal latestBmi = calculateBmi(latest.weightKg(), heightCm);
        BigDecimal baselineBmi = calculateBmi(baseline.weightKg(), heightCm);
        if (latestBmi != null) {
            indicators.add(buildBmiIndicator(profile, latestBmi, baselineBmi, goal));
        }

        if (latest.bodyFatPercent() != null) {
            BigDecimal baselineFat = baseline.bodyFatPercent();
            indicators.add(buildBodyFatIndicator(
                    profile.getSex(),
                    profile.getAge(),
                    latest.bodyFatPercent(),
                    baselineFat,
                    goal
            ));
        }

        if (latest.waistCm() != null) {
            indicators.add(buildWaistIndicator(
                    profile.getSex(),
                    latest.waistCm(),
                    baseline.waistCm(),
                    goal
            ));
        }

        if (latest.waistCm() != null && latest.hipCm() != null
                && latest.hipCm().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal latestRatio = latest.waistCm().divide(latest.hipCm(), 2, RoundingMode.HALF_UP);
            BigDecimal baselineRatio = null;
            if (baseline.waistCm() != null && baseline.hipCm() != null
                    && baseline.hipCm().compareTo(BigDecimal.ZERO) > 0) {
                baselineRatio = baseline.waistCm().divide(baseline.hipCm(), 2, RoundingMode.HALF_UP);
            }
            indicators.add(buildWaistHipRatioIndicator(
                    profile.getSex(),
                    latestRatio,
                    baselineRatio,
                    goal
            ));
        }

        return indicators;
    }

    private HealthIndicatorResponse buildBmiIndicator(
            NutritionProfile profile,
            BigDecimal currentBmi,
            BigDecimal baselineBmi,
            Goal goal
    ) {
        BmiClassification classification = classifyBmi(currentBmi);
        LifeStage lifeStage = resolveLifeStage(profile);
        String healthNote = buildBmiHealthNote(classification, profile.isAthleteModeEnabled(), lifeStage);
        BigDecimal delta = delta(baselineBmi, currentBmi);

        return new HealthIndicatorResponse(
                "bmi",
                "IMC",
                currentBmi,
                "",
                classification.label(),
                classification.riskLevel(),
                BMI_UNDERWEIGHT,
                BMI_NORMAL_MAX,
                "OMS",
                healthNote,
                baselineBmi,
                delta,
                deltaDirection(goal, "bmi", baselineBmi, currentBmi, classification.riskLevel())
        );
    }

    private HealthIndicatorResponse buildBodyFatIndicator(
            Sex sex,
            Integer age,
            BigDecimal current,
            BigDecimal baseline,
            Goal goal
    ) {
        BodyFatRange range = bodyFatHealthyRange(sex);
        String classification;
        String riskLevel;
        if (current.compareTo(range.min()) < 0) {
            classification = "Abaixo do saudável";
            riskLevel = "ATTENTION";
        } else if (current.compareTo(range.max()) <= 0) {
            classification = "Saudável";
            riskLevel = "OK";
        } else if (current.compareTo(range.max().add(new BigDecimal("5"))) <= 0) {
            classification = "Acima do saudável";
            riskLevel = "ATTENTION";
        } else {
            classification = "Elevado";
            riskLevel = "HIGH_RISK";
        }

        String healthNote = "Faixa saudável de referência (ACE): "
                + range.min().stripTrailingZeros().toPlainString()
                + "–"
                + range.max().stripTrailingZeros().toPlainString()
                + "% para "
                + (sex == Sex.MALE ? "homens" : "mulheres")
                + ". Percentual elevado pode estar associado a maior risco metabólico.";

        return new HealthIndicatorResponse(
                "bodyFat",
                "% Gordura",
                current.setScale(1, RoundingMode.HALF_UP),
                "%",
                classification,
                riskLevel,
                range.min(),
                range.max(),
                "ACE",
                healthNote,
                baseline,
                delta(baseline, current),
                deltaDirection(goal, "bodyFat", baseline, current, riskLevel)
        );
    }

    private HealthIndicatorResponse buildWaistIndicator(
            Sex sex,
            BigDecimal current,
            BigDecimal baseline,
            Goal goal
    ) {
        WaistClassification classification = classifyWaist(current, sex);
        return new HealthIndicatorResponse(
                "waist",
                "Cintura",
                current.setScale(1, RoundingMode.HALF_UP),
                "cm",
                classification.label(),
                classification.riskLevel(),
                BigDecimal.ZERO,
                sex == Sex.MALE ? MALE_WAIST_ATTENTION : FEMALE_WAIST_ATTENTION,
                "OMS / MS",
                classification.healthNote(),
                baseline,
                delta(baseline, current),
                deltaDirection(goal, "waist", baseline, current, classification.riskLevel())
        );
    }

    private HealthIndicatorResponse buildWaistHipRatioIndicator(
            Sex sex,
            BigDecimal current,
            BigDecimal baseline,
            Goal goal
    ) {
        BigDecimal limit = sex == Sex.MALE ? MALE_WH_RATIO_LIMIT : FEMALE_WH_RATIO_LIMIT;
        String classification;
        String riskLevel;
        String healthNote;
        if (current.compareTo(limit) <= 0) {
            classification = "Dentro do esperado";
            riskLevel = "OK";
            healthNote = "Relação cintura-quadril dentro do limite considerado saudável pela OMS (≤"
                    + limit.stripTrailingZeros().toPlainString() + ").";
        } else {
            classification = "Risco aumentado";
            riskLevel = "HIGH_RISK";
            healthNote = "Relação cintura-quadril acima de "
                    + limit.stripTrailingZeros().toPlainString()
                    + " está associada a maior risco cardiovascular segundo a OMS.";
        }

        return new HealthIndicatorResponse(
                "waistHipRatio",
                "Relação cintura-quadril",
                current,
                "",
                classification,
                riskLevel,
                BigDecimal.ZERO,
                limit,
                "OMS",
                healthNote,
                baseline,
                delta(baseline, current),
                deltaDirection(goal, "waistHipRatio", baseline, current, riskLevel)
        );
    }

    private BmiClassification classifyBmi(BigDecimal bmi) {
        if (bmi.compareTo(BMI_UNDERWEIGHT) < 0) {
            return new BmiClassification("Baixo peso", "ATTENTION");
        }
        if (bmi.compareTo(BMI_NORMAL_MAX) <= 0) {
            return new BmiClassification("Normal", "OK");
        }
        if (bmi.compareTo(BMI_OVERWEIGHT) >= 0 && bmi.compareTo(BMI_OBESE_I) < 0) {
            return new BmiClassification("Sobrepeso", "ATTENTION");
        }
        if (bmi.compareTo(BMI_OBESE_I) >= 0 && bmi.compareTo(BMI_OBESE_II) < 0) {
            return new BmiClassification("Obesidade grau I", "HIGH_RISK");
        }
        if (bmi.compareTo(BMI_OBESE_II) >= 0 && bmi.compareTo(BMI_OBESE_III) < 0) {
            return new BmiClassification("Obesidade grau II", "HIGH_RISK");
        }
        return new BmiClassification("Obesidade grau III", "HIGH_RISK");
    }

    private String buildBmiHealthNote(BmiClassification classification, boolean athleteMode, LifeStage lifeStage) {
        String base = switch (classification.label()) {
            case "Baixo peso" -> "IMC abaixo de 18,5 indica baixo peso segundo a OMS.";
            case "Normal" -> "IMC dentro da faixa considerada normal pela OMS (18,5–24,9).";
            case "Sobrepeso" -> "IMC entre 25 e 29,9 indica sobrepeso. Pode estar associado a maior risco metabólico.";
            case "Obesidade grau I" -> "IMC entre 30 e 34,9 indica obesidade grau I segundo a OMS.";
            case "Obesidade grau II" -> "IMC entre 35 e 39,9 indica obesidade grau II segundo a OMS.";
            default -> "IMC a partir de 40 indica obesidade grau III segundo a OMS.";
        };
        if (athleteMode) {
            base += " Em pessoas muito musculosas, o IMC pode superestimar o risco — considere também % gordura e cintura.";
        }
        if (lifeStage == LifeStage.SENIOR) {
            base += " Em idosos, faixas de IMC ligeiramente acima do normal podem ser aceitáveis — consulte um profissional.";
        }
        return base + " Isso não é diagnóstico.";
    }

    private WaistClassification classifyWaist(BigDecimal waistCm, Sex sex) {
        if (sex == Sex.MALE) {
            if (waistCm.compareTo(MALE_WAIST_HIGH) >= 0) {
                return new WaistClassification(
                        "Risco alto",
                        "HIGH_RISK",
                        "Cintura ≥102 cm indica risco metabólico substancialmente aumentado (OMS)."
                );
            }
            if (waistCm.compareTo(MALE_WAIST_ATTENTION) >= 0) {
                return new WaistClassification(
                        "Risco aumentado",
                        "ATTENTION",
                        "Cintura ≥94 cm indica risco metabólico aumentado (OMS). Você está nessa faixa ou próximo."
                );
            }
            if (waistCm.compareTo(MALE_WAIST_ATTENTION.subtract(new BigDecimal("4"))) >= 0) {
                return new WaistClassification(
                        "Próximo ao limite",
                        "ATTENTION",
                        "Sua cintura está próxima de 94 cm, limite de risco aumentado para homens (OMS)."
                );
            }
            return new WaistClassification(
                    "Dentro do esperado",
                    "OK",
                    "Cintura abaixo de 94 cm, dentro do limite de risco aumentado para homens (OMS)."
            );
        }
        if (waistCm.compareTo(FEMALE_WAIST_HIGH) >= 0) {
            return new WaistClassification(
                    "Risco alto",
                    "HIGH_RISK",
                    "Cintura ≥88 cm indica risco metabólico substancialmente aumentado (OMS)."
            );
        }
        if (waistCm.compareTo(FEMALE_WAIST_ATTENTION) >= 0) {
            return new WaistClassification(
                    "Risco aumentado",
                    "ATTENTION",
                    "Cintura ≥80 cm indica risco metabólico aumentado (OMS). Você está nessa faixa ou próximo."
            );
        }
        if (waistCm.compareTo(FEMALE_WAIST_ATTENTION.subtract(new BigDecimal("4"))) >= 0) {
            return new WaistClassification(
                    "Próximo ao limite",
                    "ATTENTION",
                    "Sua cintura está próxima de 80 cm, limite de risco aumentado para mulheres (OMS)."
            );
        }
        return new WaistClassification(
                "Dentro do esperado",
                "OK",
                "Cintura abaixo de 80 cm, dentro do limite de risco aumentado para mulheres (OMS)."
        );
    }

    private BodyFatRange bodyFatHealthyRange(Sex sex) {
        if (sex == Sex.MALE) {
            return new BodyFatRange(new BigDecimal("10"), new BigDecimal("20"));
        }
        return new BodyFatRange(new BigDecimal("18"), new BigDecimal("28"));
    }

    private LifeStage resolveLifeStage(NutritionProfile profile) {
        if (profile.getBirthDate() != null) {
            return LifeStageUtil.resolveFromBirthDate(profile.getBirthDate());
        }
        if (profile.getAge() != null) {
            return LifeStageUtil.resolve(profile.getAge());
        }
        return LifeStage.ADULT;
    }

    private BigDecimal delta(BigDecimal baseline, BigDecimal current) {
        if (baseline == null || current == null) {
            return null;
        }
        return current.subtract(baseline).setScale(2, RoundingMode.HALF_UP);
    }

    private String deltaDirection(Goal goal, String key, BigDecimal baseline, BigDecimal current, String riskLevel) {
        if (baseline == null || current == null) {
            return "UNKNOWN";
        }
        int cmp = current.compareTo(baseline);
        if (cmp == 0) {
            return "STABLE";
        }
        boolean decreased = cmp < 0;
        boolean lowerIsBetter = isLowerBetter(key, goal);
        if (goal == Goal.MAINTAIN_WEIGHT) {
            BigDecimal change = current.subtract(baseline).abs();
            BigDecimal threshold = switch (key) {
                case "bmi" -> new BigDecimal("0.5");
                case "bodyFat" -> new BigDecimal("1");
                case "waist" -> new BigDecimal("2");
                case "waistHipRatio" -> new BigDecimal("0.03");
                default -> new BigDecimal("1");
            };
            if (change.compareTo(threshold) <= 0) {
                return "STABLE";
            }
            if ("OK".equals(riskLevel) && decreased && lowerIsBetter) {
                return "IMPROVED";
            }
            if ("OK".equals(riskLevel) && !decreased && !lowerIsBetter) {
                return "IMPROVED";
            }
            return decreased ? "IMPROVED" : "WORSENED";
        }
        boolean improved = (decreased && lowerIsBetter) || (!decreased && !lowerIsBetter);
        return improved ? "IMPROVED" : "WORSENED";
    }

    private boolean isLowerBetter(String key, Goal goal) {
        return switch (key) {
            case "bmi", "bodyFat", "waist", "waistHipRatio" -> goal != Goal.GAIN_MASS;
            default -> goal == Goal.LOSE_WEIGHT;
        };
    }

    private record BmiClassification(String label, String riskLevel) {}

    private record WaistClassification(String label, String riskLevel, String healthNote) {}

    private record BodyFatRange(BigDecimal min, BigDecimal max) {}
}

package br.com.nutriplus.service;

import br.com.nutriplus.domain.entity.BodyMeasurementSession;
import br.com.nutriplus.domain.entity.NutritionProfile;
import br.com.nutriplus.domain.enums.EvolutionMetricStatus;
import br.com.nutriplus.domain.enums.Goal;
import br.com.nutriplus.domain.enums.ProgressReviewStatus;
import br.com.nutriplus.dto.response.BodyMeasurementResponse;
import br.com.nutriplus.dto.response.EvolutionMetricResponse;
import br.com.nutriplus.dto.response.EvolutionReportResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Component
public class EvolutionReportBuilder {

    public List<EvolutionMetricResponse> buildMetrics(
            NutritionProfile profile,
            BodyMeasurementResponse baseline,
            BodyMeasurementResponse latest,
            int weekAdherencePercent
    ) {
        List<EvolutionMetricResponse> metrics = new ArrayList<>();
        Goal goal = profile.getGoal();

        addDecimalMetric(metrics, "weight", "Peso", "kg", baseline.weightKg(), latest.weightKg(),
                profile.getTargetWeightKg(), goal, this::rateWeight);
        addDecimalMetric(metrics, "bodyFat", "% Gordura", "%", baseline.bodyFatPercent(), latest.bodyFatPercent(),
                null, goal, this::rateBodyFat);
        addDecimalMetric(metrics, "muscleMass", "Massa muscular", "kg", baseline.muscleMassKg(), latest.muscleMassKg(),
                null, goal, this::rateMuscleMass);
        addDecimalMetric(metrics, "waist", "Cintura", "cm", baseline.waistCm(), latest.waistCm(),
                null, goal, this::rateWaist);
        addDecimalMetric(metrics, "hip", "Quadril", "cm", baseline.hipCm(), latest.hipCm(),
                null, goal, this::rateHip);
        addDecimalMetric(metrics, "chest", "Peito", "cm", baseline.chestCm(), latest.chestCm(),
                null, goal, this::rateChest);
        addDecimalMetric(metrics, "arm", "Braço (médio)", "cm", avg(baseline.armRightCm(), baseline.armLeftCm()),
                avg(latest.armRightCm(), latest.armLeftCm()), null, goal, this::rateArm);
        addDecimalMetric(metrics, "thigh", "Coxa (média)", "cm", avg(baseline.thighRightCm(), baseline.thighLeftCm()),
                avg(latest.thighRightCm(), latest.thighLeftCm()), null, goal, this::rateThigh);

        metrics.add(rateAdherence(weekAdherencePercent));
        return metrics;
    }

    private void addDecimalMetric(
            List<EvolutionMetricResponse> metrics,
            String key,
            String label,
            String unit,
            BigDecimal baseline,
            BigDecimal current,
            BigDecimal target,
            Goal goal,
            MetricRater rater
    ) {
        if (current == null && baseline == null) {
            return;
        }
        BigDecimal base = baseline != null ? baseline : current;
        BigDecimal cur = current != null ? current : baseline;
        if (base == null || cur == null) {
            return;
        }
        BigDecimal delta = cur.subtract(base).setScale(2, RoundingMode.HALF_UP);
        EvolutionMetricStatus status = rater.rate(goal, base, cur, target, delta);
        metrics.add(new EvolutionMetricResponse(
                key,
                label,
                unit,
                base,
                cur,
                target,
                delta,
                direction(delta),
                status.name(),
                status.labelPt(),
                insight(key, goal, delta, status)
        ));
    }

    private EvolutionMetricResponse rateAdherence(int adherence) {
        EvolutionMetricStatus status;
        if (adherence >= 85) {
            status = EvolutionMetricStatus.EXCELLENT;
        } else if (adherence >= 70) {
            status = EvolutionMetricStatus.GOOD;
        } else if (adherence >= 50) {
            status = EvolutionMetricStatus.OK;
        } else {
            status = EvolutionMetricStatus.BELOW;
        }
        return new EvolutionMetricResponse(
                "adherence",
                "Aderência ao plano",
                "%",
                BigDecimal.valueOf(70),
                BigDecimal.valueOf(adherence),
                BigDecimal.valueOf(80),
                BigDecimal.valueOf(adherence - 70),
                adherence >= 70 ? "UP" : "DOWN",
                status.name(),
                status.labelPt(),
                "Refeições marcadas como feitas na última semana."
        );
    }

    private EvolutionMetricStatus rateWeight(Goal goal, BigDecimal base, BigDecimal cur, BigDecimal target, BigDecimal delta) {
        if (goal == Goal.LOSE_WEIGHT) {
            if (target != null && cur.compareTo(target) <= 0) {
                return EvolutionMetricStatus.EXCELLENT;
            }
            if (target != null && base.compareTo(target) > 0) {
                BigDecimal totalGap = base.subtract(target);
                BigDecimal progress = base.subtract(cur);
                if (totalGap.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal ratio = progress.divide(totalGap, 2, RoundingMode.HALF_UP);
                    if (ratio.compareTo(new BigDecimal("0.8")) >= 0) {
                        return EvolutionMetricStatus.EXCELLENT;
                    }
                    if (ratio.compareTo(new BigDecimal("0.4")) >= 0) {
                        return EvolutionMetricStatus.GOOD;
                    }
                }
            }
            if (delta.compareTo(new BigDecimal("-1")) <= 0) {
                return EvolutionMetricStatus.GOOD;
            }
            if (delta.compareTo(new BigDecimal("0.3")) <= 0) {
                return EvolutionMetricStatus.OK;
            }
            return EvolutionMetricStatus.BELOW;
        }
        if (goal == Goal.GAIN_MASS) {
            if (target != null && cur.compareTo(target) >= 0) {
                return EvolutionMetricStatus.EXCELLENT;
            }
            if (delta.compareTo(new BigDecimal("1.5")) >= 0) {
                return EvolutionMetricStatus.EXCELLENT;
            }
            if (delta.compareTo(new BigDecimal("0.5")) >= 0) {
                return EvolutionMetricStatus.GOOD;
            }
            if (delta.compareTo(new BigDecimal("-0.3")) >= 0) {
                return EvolutionMetricStatus.OK;
            }
            return EvolutionMetricStatus.BELOW;
        }
        if (target != null) {
            BigDecimal diff = cur.subtract(target).abs();
            if (diff.compareTo(new BigDecimal("1")) <= 0) {
                return EvolutionMetricStatus.EXCELLENT;
            }
            if (diff.compareTo(new BigDecimal("2")) <= 0) {
                return EvolutionMetricStatus.GOOD;
            }
            if (diff.compareTo(new BigDecimal("3")) <= 0) {
                return EvolutionMetricStatus.OK;
            }
        }
        return EvolutionMetricStatus.BELOW;
    }

    private EvolutionMetricStatus rateBodyFat(Goal goal, BigDecimal base, BigDecimal cur, BigDecimal target, BigDecimal delta) {
        if (goal == Goal.GAIN_MASS) {
            if (delta.compareTo(new BigDecimal("2")) > 0) {
                return EvolutionMetricStatus.BELOW;
            }
            if (delta.compareTo(new BigDecimal("-0.5")) <= 0) {
                return EvolutionMetricStatus.EXCELLENT;
            }
            return EvolutionMetricStatus.OK;
        }
        if (delta.compareTo(new BigDecimal("-2")) <= 0) {
            return EvolutionMetricStatus.EXCELLENT;
        }
        if (delta.compareTo(new BigDecimal("-0.5")) <= 0) {
            return EvolutionMetricStatus.GOOD;
        }
        if (delta.compareTo(new BigDecimal("0.5")) < 0) {
            return EvolutionMetricStatus.OK;
        }
        return EvolutionMetricStatus.BELOW;
    }

    private EvolutionMetricStatus rateMuscleMass(Goal goal, BigDecimal base, BigDecimal cur, BigDecimal target, BigDecimal delta) {
        if (delta.compareTo(new BigDecimal("0.8")) >= 0) {
            return EvolutionMetricStatus.EXCELLENT;
        }
        if (delta.compareTo(new BigDecimal("0.3")) >= 0) {
            return EvolutionMetricStatus.GOOD;
        }
        if (delta.compareTo(new BigDecimal("-0.2")) >= 0) {
            return EvolutionMetricStatus.OK;
        }
        return goal == Goal.GAIN_MASS ? EvolutionMetricStatus.BELOW : EvolutionMetricStatus.OK;
    }

    private EvolutionMetricStatus rateWaist(Goal goal, BigDecimal base, BigDecimal cur, BigDecimal target, BigDecimal delta) {
        if (goal == Goal.GAIN_MASS) {
            if (delta.compareTo(new BigDecimal("2")) > 0) {
                return EvolutionMetricStatus.BELOW;
            }
            if (delta.abs().compareTo(new BigDecimal("1")) <= 0) {
                return EvolutionMetricStatus.OK;
            }
            return EvolutionMetricStatus.GOOD;
        }
        if (delta.compareTo(new BigDecimal("-2")) <= 0) {
            return EvolutionMetricStatus.EXCELLENT;
        }
        if (delta.compareTo(new BigDecimal("-1")) <= 0) {
            return EvolutionMetricStatus.GOOD;
        }
        if (delta.compareTo(new BigDecimal("0.5")) < 0) {
            return EvolutionMetricStatus.OK;
        }
        return EvolutionMetricStatus.BELOW;
    }

    private EvolutionMetricStatus rateHip(Goal goal, BigDecimal base, BigDecimal cur, BigDecimal target, BigDecimal delta) {
        return rateWaist(goal, base, cur, target, delta);
    }

    private EvolutionMetricStatus rateChest(Goal goal, BigDecimal base, BigDecimal cur, BigDecimal target, BigDecimal delta) {
        if (goal == Goal.GAIN_MASS && delta.compareTo(new BigDecimal("1")) >= 0) {
            return EvolutionMetricStatus.EXCELLENT;
        }
        if (delta.compareTo(new BigDecimal("0.5")) >= 0) {
            return EvolutionMetricStatus.GOOD;
        }
        if (delta.abs().compareTo(new BigDecimal("0.5")) <= 0) {
            return EvolutionMetricStatus.OK;
        }
        return EvolutionMetricStatus.BELOW;
    }

    private EvolutionMetricStatus rateArm(Goal goal, BigDecimal base, BigDecimal cur, BigDecimal target, BigDecimal delta) {
        if (delta.compareTo(new BigDecimal("1")) >= 0) {
            return EvolutionMetricStatus.EXCELLENT;
        }
        if (delta.compareTo(new BigDecimal("0.5")) >= 0) {
            return EvolutionMetricStatus.GOOD;
        }
        if (delta.abs().compareTo(new BigDecimal("0.3")) <= 0) {
            return EvolutionMetricStatus.OK;
        }
        return goal == Goal.GAIN_MASS ? EvolutionMetricStatus.BELOW : EvolutionMetricStatus.OK;
    }

    private EvolutionMetricStatus rateThigh(Goal goal, BigDecimal base, BigDecimal cur, BigDecimal target, BigDecimal delta) {
        return rateArm(goal, base, cur, target, delta);
    }

    private String direction(BigDecimal delta) {
        int cmp = delta.compareTo(BigDecimal.ZERO);
        if (cmp > 0) {
            return "UP";
        }
        if (cmp < 0) {
            return "DOWN";
        }
        return "FLAT";
    }

    private String insight(String key, Goal goal, BigDecimal delta, EvolutionMetricStatus status) {
        if ("weight".equals(key) && goal == Goal.LOSE_WEIGHT && status == EvolutionMetricStatus.EXCELLENT) {
            return "Bom progresso em direção ao peso alvo — estimativa com base nas medidas.";
        }
        if ("waist".equals(key) && delta.compareTo(BigDecimal.ZERO) < 0) {
            return "Cintura menor — pode indicar mudança na região abdominal (estimativa).";
        }
        if ("arm".equals(key) && delta.compareTo(new BigDecimal("0.5")) >= 0) {
            return "Braços maiores — pode indicar aumento de perímetro muscular.";
        }
        if ("bodyFat".equals(key) && delta.compareTo(BigDecimal.ZERO) < 0) {
            return "% de gordura em queda — pode sugerir recomposição (estimativa).";
        }
        return switch (status) {
            case EXCELLENT -> "Resultado acima do esperado para sua meta.";
            case GOOD -> "Evolução positiva — continue assim.";
            case OK -> "Dentro do esperado; consistência faz diferença.";
            case BELOW -> "Vale conversar com um profissional de saúde se tiver dúvidas sobre sua rotina.";
        };
    }

    private BigDecimal avg(BigDecimal a, BigDecimal b) {
        if (a == null && b == null) {
            return null;
        }
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        return a.add(b).divide(new BigDecimal("2"), 2, RoundingMode.HALF_UP);
    }

    @FunctionalInterface
    private interface MetricRater {
        EvolutionMetricStatus rate(Goal goal, BigDecimal base, BigDecimal cur, BigDecimal target, BigDecimal delta);
    }
}

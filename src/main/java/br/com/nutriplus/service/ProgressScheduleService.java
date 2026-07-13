package br.com.nutriplus.service;

import br.com.nutriplus.domain.entity.BodyMeasurementSession;
import br.com.nutriplus.domain.entity.NutritionProfile;
import br.com.nutriplus.domain.entity.ProgressReview;
import br.com.nutriplus.domain.enums.ProgressReviewStatus;
import br.com.nutriplus.dto.response.ProgressScheduleResponse;
import br.com.nutriplus.repository.BodyMeasurementSessionRepository;
import br.com.nutriplus.repository.ProgressReviewRepository;
import br.com.nutriplus.util.NutriTime;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class ProgressScheduleService {

    private final BodyMeasurementSessionRepository measurementRepository;
    private final ProgressReviewRepository reviewRepository;

    public ProgressScheduleService(BodyMeasurementSessionRepository measurementRepository,
                                   ProgressReviewRepository reviewRepository) {
        this.measurementRepository = measurementRepository;
        this.reviewRepository = reviewRepository;
    }

    public ProgressScheduleResponse getScheduleForUser(Long userId, NutritionProfile profile) {
        int intervalDays = profile.getProgressReviewIntervalDays();

        LocalDate anchor = resolveAnchorDate(userId, profile);
        LocalDate nextDueOn = anchor.plusDays(intervalDays);
        LocalDate today = NutriTime.today();
        long daysUntil = ChronoUnit.DAYS.between(today, nextDueOn);
        boolean due = !today.isBefore(nextDueOn);

        LocalDateTime lastReviewAt = reviewRepository
                .findFirstByUserIdAndStatusOrderByCompletedAtDesc(userId, ProgressReviewStatus.COMPLETED)
                .map(ProgressReview::getCompletedAt)
                .orElse(null);

        LocalDate lastMeasurementOn = measurementRepository
                .findFirstByUserIdOrderByMeasuredOnDescIdDesc(userId)
                .map(BodyMeasurementSession::getMeasuredOn)
                .orElse(null);

        return new ProgressScheduleResponse(
                intervalDays,
                due,
                due ? 0 : (int) Math.max(daysUntil, 0),
                nextDueOn,
                lastReviewAt,
                lastMeasurementOn
        );
    }

    /**
     * Ciclo de reavaliação:
     * 1) última análise COMPLETED, ou
     * 2) baseline = primeira medição (não a última — senão salvar medidas no dia "due" zera o ciclo), ou
     * 3) criação do perfil.
     */
    private LocalDate resolveAnchorDate(Long userId, NutritionProfile profile) {
        return reviewRepository
                .findFirstByUserIdAndStatusOrderByCompletedAtDesc(userId, ProgressReviewStatus.COMPLETED)
                .filter(r -> r.getCompletedAt() != null)
                .map(r -> r.getCompletedAt().toLocalDate())
                .orElseGet(() -> measurementRepository
                        .findFirstByUserIdOrderByMeasuredOnAscIdAsc(userId)
                        .map(BodyMeasurementSession::getMeasuredOn)
                        .orElseGet(() -> profileCreatedOn(profile)));
    }

    private LocalDate profileCreatedOn(NutritionProfile profile) {
        if (profile.getCreatedAt() != null) {
            return profile.getCreatedAt().toLocalDate();
        }
        return NutriTime.today();
    }
}

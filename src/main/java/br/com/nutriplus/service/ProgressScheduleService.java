package br.com.nutriplus.service;

import br.com.nutriplus.domain.entity.BodyMeasurementSession;
import br.com.nutriplus.domain.entity.NutritionProfile;
import br.com.nutriplus.domain.entity.ProgressReview;
import br.com.nutriplus.domain.enums.ProgressReviewStatus;
import br.com.nutriplus.dto.response.ProgressScheduleResponse;
import br.com.nutriplus.repository.BodyMeasurementSessionRepository;
import br.com.nutriplus.repository.ProgressReviewRepository;
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
        LocalDate today = LocalDate.now();
        long daysUntil = ChronoUnit.DAYS.between(today, nextDueOn);
        boolean due = !today.isBefore(nextDueOn);

        LocalDateTime lastReviewAt = reviewRepository.findFirstByUserIdOrderByCreatedAtDesc(userId)
                .filter(r -> r.getStatus() == ProgressReviewStatus.COMPLETED)
                .map(ProgressReview::getCompletedAt)
                .orElse(null);

        LocalDate lastMeasurementOn = measurementRepository
                .findFirstByUserIdOrderByMeasuredOnDescIdDesc(userId)
                .map(BodyMeasurementSession::getMeasuredOn)
                .orElse(null);

        return new ProgressScheduleResponse(
                intervalDays,
                due,
                due ? 0 : (int) daysUntil,
                nextDueOn,
                lastReviewAt,
                lastMeasurementOn
        );
    }

    private LocalDate resolveAnchorDate(Long userId, NutritionProfile profile) {
        return reviewRepository.findFirstByUserIdOrderByCreatedAtDesc(userId)
                .filter(r -> r.getStatus() == ProgressReviewStatus.COMPLETED && r.getCompletedAt() != null)
                .map(r -> r.getCompletedAt().toLocalDate())
                .orElseGet(() -> measurementRepository
                        .findFirstByUserIdOrderByMeasuredOnDescIdDesc(userId)
                        .map(BodyMeasurementSession::getMeasuredOn)
                        .orElseGet(() -> profileCreatedOn(profile)));
    }

    private LocalDate profileCreatedOn(NutritionProfile profile) {
        if (profile.getCreatedAt() != null) {
            return profile.getCreatedAt().toLocalDate();
        }
        return LocalDate.now();
    }
}

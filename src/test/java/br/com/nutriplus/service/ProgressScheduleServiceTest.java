package br.com.nutriplus.service;

import br.com.nutriplus.domain.entity.BodyMeasurementSession;
import br.com.nutriplus.domain.entity.NutritionProfile;
import br.com.nutriplus.domain.entity.ProgressReview;
import br.com.nutriplus.domain.entity.User;
import br.com.nutriplus.domain.enums.ProgressReviewStatus;
import br.com.nutriplus.dto.response.ProgressScheduleResponse;
import br.com.nutriplus.repository.BodyMeasurementSessionRepository;
import br.com.nutriplus.repository.ProgressReviewRepository;
import br.com.nutriplus.util.NutriTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProgressScheduleServiceTest {

    @Mock
    private BodyMeasurementSessionRepository measurementRepository;
    @Mock
    private ProgressReviewRepository reviewRepository;

    private ProgressScheduleService service;
    private NutritionProfile profile;

    @BeforeEach
    void setUp() {
        service = new ProgressScheduleService(measurementRepository, reviewRepository);
        User user = mock(User.class);
        profile = NutritionProfile.builder()
                .user(user)
                .progressReviewIntervalDays(15)
                .build();
    }

    @Test
    void savingFreshMeasurementOnDueDayDoesNotResetCycle() {
        LocalDate baseline = NutriTime.today().minusDays(15);
        LocalDate today = NutriTime.today();

        when(reviewRepository.findFirstByUserIdAndStatusOrderByCompletedAtDesc(1L, ProgressReviewStatus.COMPLETED))
                .thenReturn(Optional.empty());

        BodyMeasurementSession first = mock(BodyMeasurementSession.class);
        when(first.getMeasuredOn()).thenReturn(baseline);
        when(measurementRepository.findFirstByUserIdOrderByMeasuredOnAscIdAsc(1L))
                .thenReturn(Optional.of(first));

        BodyMeasurementSession latest = mock(BodyMeasurementSession.class);
        when(latest.getMeasuredOn()).thenReturn(today);
        when(measurementRepository.findFirstByUserIdOrderByMeasuredOnDescIdDesc(1L))
                .thenReturn(Optional.of(latest));

        ProgressScheduleResponse schedule = service.getScheduleForUser(1L, profile);

        assertTrue(schedule.due(), "salvar medidas no dia da reavaliação não pode zerar o ciclo");
        assertEquals(0, schedule.daysUntilDue());
        assertEquals(today, schedule.lastMeasurementOn());
    }

    @Test
    void midCycleLatestMeasurementDoesNotAffectDueDate() {
        LocalDate baseline = NutriTime.today().minusDays(5);

        when(reviewRepository.findFirstByUserIdAndStatusOrderByCompletedAtDesc(1L, ProgressReviewStatus.COMPLETED))
                .thenReturn(Optional.empty());

        BodyMeasurementSession first = mock(BodyMeasurementSession.class);
        when(first.getMeasuredOn()).thenReturn(baseline);
        when(measurementRepository.findFirstByUserIdOrderByMeasuredOnAscIdAsc(1L))
                .thenReturn(Optional.of(first));
        when(measurementRepository.findFirstByUserIdOrderByMeasuredOnDescIdDesc(1L))
                .thenReturn(Optional.of(first));

        ProgressScheduleResponse schedule = service.getScheduleForUser(1L, profile);

        assertFalse(schedule.due());
        assertEquals(10, schedule.daysUntilDue());
    }

    @Test
    void completedReviewAnchorsNextCycleEvenIfEarlierFailedAttemptExists() {
        LocalDateTime completedAt = NutriTime.today().minusDays(2).atStartOfDay();
        ProgressReview completed = mock(ProgressReview.class);
        when(completed.getCompletedAt()).thenReturn(completedAt);

        when(reviewRepository.findFirstByUserIdAndStatusOrderByCompletedAtDesc(1L, ProgressReviewStatus.COMPLETED))
                .thenReturn(Optional.of(completed));
        when(measurementRepository.findFirstByUserIdOrderByMeasuredOnDescIdDesc(1L))
                .thenReturn(Optional.empty());

        ProgressScheduleResponse schedule = service.getScheduleForUser(1L, profile);

        assertFalse(schedule.due());
        assertEquals(13, schedule.daysUntilDue());
        assertEquals(completedAt, schedule.lastReviewAt());
    }
}

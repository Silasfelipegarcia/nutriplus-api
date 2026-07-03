package br.com.nutriplus.service;

import br.com.nutriplus.domain.enums.Goal;
import br.com.nutriplus.dto.response.GoalTimelineChartPoint;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GoalTimelineServiceTest {

    @Test
    void normalizeActualRate_caps_unrealistic_loss() {
        var capped = GoalTimelineService.normalizeActualRate(
                new BigDecimal("3.50"),
                new BigDecimal("0.20"),
                Goal.LOSE_WEIGHT
        );
        assertTrue(capped.doubleValue() <= 1.15);
        assertTrue(capped.doubleValue() >= 0.15);
    }

    @Test
    void resolveChartEndDate_extends_when_behind_schedule() {
        LocalDate target = LocalDate.of(2026, 9, 22);
        LocalDate projected = LocalDate.of(2026, 11, 1);
        LocalDate end = GoalTimelineService.resolveChartEndDate(
                LocalDate.of(2026, 6, 30),
                target,
                projected,
                40
        );
        assertTrue(end.isAfter(target));
    }

    @Test
    void resolveChartEndDate_uses_target_when_on_track() {
        LocalDate target = LocalDate.of(2026, 9, 22);
        LocalDate end = GoalTimelineService.resolveChartEndDate(
                LocalDate.of(2026, 6, 30),
                target,
                LocalDate.of(2026, 9, 10),
                -12
        );
        assertEquals(target, end);
    }

    @Test
    void buildPaceLine_has_weekly_points() {
        List<GoalTimelineChartPoint> line = GoalTimelineService.buildPaceLine(
                LocalDate.of(2026, 6, 1),
                new BigDecimal("80.0"),
                LocalDate.of(2026, 9, 1),
                new BigDecimal("74.0"),
                Goal.LOSE_WEIGHT
        );
        assertTrue(line.size() >= 5);
        assertEquals(LocalDate.of(2026, 6, 1), line.getFirst().date());
        assertEquals(LocalDate.of(2026, 9, 1), line.getLast().date());
    }

    @Test
    void buildProjectionLine_extends_to_chart_end_when_behind() {
        List<GoalTimelineChartPoint> line = GoalTimelineService.buildProjectionLine(
                LocalDate.of(2026, 7, 1),
                new BigDecimal("78.0"),
                -0.30,
                new BigDecimal("74.0"),
                Goal.LOSE_WEIGHT,
                LocalDate.of(2026, 9, 22)
        );
        assertEquals(LocalDate.of(2026, 7, 1), line.getFirst().date());
        assertEquals(LocalDate.of(2026, 9, 22), line.getLast().date());
        assertTrue(line.getLast().weightKg().doubleValue() > 74.0);
    }

    @Test
    void buildProjectionLine_reaches_goal_and_stays_flat_until_deadline() {
        List<GoalTimelineChartPoint> line = GoalTimelineService.buildProjectionLine(
                LocalDate.of(2026, 7, 1),
                new BigDecimal("76.0"),
                -0.80,
                new BigDecimal("74.0"),
                Goal.LOSE_WEIGHT,
                LocalDate.of(2026, 9, 22)
        );
        assertTrue(line.stream().anyMatch(p -> p.weightKg().doubleValue() <= 74.01));
        assertEquals(74.0, line.getLast().weightKg().doubleValue());
    }

    @Test
    void buildProjectionLine_stays_flat_when_adverse_trend_suppressed() {
        Double forChart = GoalTimelineService.signedTrendForProjection(0.15, Goal.LOSE_WEIGHT);
        List<GoalTimelineChartPoint> line = GoalTimelineService.buildProjectionLine(
                LocalDate.of(2026, 7, 1),
                new BigDecimal("78.0"),
                forChart,
                new BigDecimal("74.0"),
                Goal.LOSE_WEIGHT,
                LocalDate.of(2026, 9, 22)
        );
        assertEquals(78.0, line.getLast().weightKg().doubleValue());
    }

    @Test
    void signedTrendForProjection_keeps_favorable_loss_direction() {
        assertEquals(-0.30, GoalTimelineService.signedTrendForProjection(-0.30, Goal.LOSE_WEIGHT));
        assertNull(GoalTimelineService.signedTrendForProjection(0.15, Goal.LOSE_WEIGHT));
    }

    @Test
    void hasMinimumTrendData_false_on_first_plan_days() {
        LocalDate planStart = LocalDate.of(2026, 7, 1);
        LocalDate today = LocalDate.of(2026, 7, 2);
        assertFalse(GoalTimelineService.hasMinimumTrendData(
                planStart, today, List.of(), null));
    }

    @Test
    void blendSignedTrend_weights_calories_when_no_recent_weigh_in() {
        Double blended = GoalTimelineService.blendSignedTrend(-0.4, 0.2, 0.15);
        assertEquals(0.11, blended, 0.001);
    }

    @Test
    void magnitudeTowardGoal_ignores_adverse_calorie_direction() {
        assertEquals(0.0, GoalTimelineService.magnitudeTowardGoal(0.2, Goal.LOSE_WEIGHT).doubleValue());
        assertEquals(0.2, GoalTimelineService.magnitudeTowardGoal(-0.2, Goal.LOSE_WEIGHT).doubleValue());
    }
}

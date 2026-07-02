package br.com.nutriplus.service;

import br.com.nutriplus.domain.enums.Goal;
import br.com.nutriplus.dto.response.GoalTimelineChartPoint;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
                new BigDecimal("0.30"),
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
                new BigDecimal("0.80"),
                new BigDecimal("74.0"),
                Goal.LOSE_WEIGHT,
                LocalDate.of(2026, 9, 22)
        );
        assertTrue(line.stream().anyMatch(p -> p.weightKg().doubleValue() <= 74.01));
        assertEquals(74.0, line.getLast().weightKg().doubleValue());
    }
}

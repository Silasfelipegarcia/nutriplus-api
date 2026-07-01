package br.com.nutriplus.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CheckinAdherenceLogicTest {

    @Test
    void resolveDayStatus_noDataWhenBeforePlan() {
        assertEquals("NO_DATA", CheckinService.resolveDayStatus(
                false, true, 0, 0, 0, 0, 0, 0, 0, 2000, false));
    }

    @Test
    void resolveDayStatus_missedWhenPastDayWithoutCheckins() {
        assertEquals("MISSED", CheckinService.resolveDayStatus(
                true, false, 5, 0, 0, 5, 0, 0, 0, 2000, false));
    }

    @Test
    void resolveDayStatus_overWhenAboveTarget() {
        assertEquals("OVER", CheckinService.resolveDayStatus(
                true, false, 5, 3, 0, 0, 0, 60, 2300, 2000, false));
    }

    @Test
    void resolveDayStatus_onTrackWhenBalanced() {
        assertEquals("ON_TRACK", CheckinService.resolveDayStatus(
                true, false, 5, 5, 0, 0, 0, 100, 1900, 2000, false));
    }

    @Test
    void resolveDayStatus_partialWhenLowAdherence() {
        assertEquals("PARTIAL", CheckinService.resolveDayStatus(
                true, false, 5, 2, 1, 2, 0, 40, 900, 2000, false));
    }

    @Test
    void resolveDayStatus_partialWhenOnlyExtras() {
        assertEquals("OVER", CheckinService.resolveDayStatus(
                false, false, 0, 0, 0, 0, 1, 0, 2500, 2000, false));
    }
}

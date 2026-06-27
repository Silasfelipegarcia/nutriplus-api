package br.com.nutriplus.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CheckinAdherenceLogicTest {

    @Test
    void resolveDayStatus_noDataWhenEmpty() {
        assertEquals("NO_DATA", CheckinService.resolveDayStatus(0, 0, 0, 0, 2000));
    }

    @Test
    void resolveDayStatus_overWhenAboveTarget() {
        assertEquals("OVER", CheckinService.resolveDayStatus(3, 0, 100, 2300, 2000));
    }

    @Test
    void resolveDayStatus_onTrackWhenBalanced() {
        assertEquals("ON_TRACK", CheckinService.resolveDayStatus(4, 0, 100, 1900, 2000));
    }

    @Test
    void resolveDayStatus_partialWhenLowAdherence() {
        assertEquals("PARTIAL", CheckinService.resolveDayStatus(2, 1, 50, 900, 2000));
    }

    @Test
    void resolveDayStatus_partialWhenOnlyExtras() {
        assertEquals("OVER", CheckinService.resolveDayStatus(0, 1, 0, 2500, 2000));
    }
}

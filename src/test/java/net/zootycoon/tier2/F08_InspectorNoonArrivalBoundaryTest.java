package net.zootycoon.tier2;

import net.zootycoon.tier1.F08_InspectorNoonArrivalTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tier 2 - F8: Inspector Noon Arrival Scheduling Boundary Cases")
public class F08_InspectorNoonArrivalBoundaryTest {

    @Test
    @DisplayName("F8-B1: Request at exact dawn tick 0 of Day 0 arrives at tick 30000 (Day 1 Noon)")
    void testRequestAtExactDawnTickZero() {
        long target = F08_InspectorNoonArrivalTest.calculateTargetArrivalTick(0L);
        assertEquals(30000L, target);
    }

    @Test
    @DisplayName("F8-B2: Request at exact noon tick 6000 of Day 0 arrives at tick 30000 (Day 1 Noon)")
    void testRequestAtExactNoonTodayArrivesNextDayNoon() {
        long target = F08_InspectorNoonArrivalTest.calculateTargetArrivalTick(6000L);
        assertEquals(30000L, target, "Summoning at noon today must schedule arrival for noon the following day");
    }

    @Test
    @DisplayName("F8-B3: Boundary tick 5999 (1 tick before noon) vs tick 6001 (1 tick after noon)")
    void testTicksAroundNoonBoundary() {
        long beforeNoon = F08_InspectorNoonArrivalTest.calculateTargetArrivalTick(5999L);
        long afterNoon = F08_InspectorNoonArrivalTest.calculateTargetArrivalTick(6001L);

        assertEquals(30000L, beforeNoon);
        assertEquals(30000L, afterNoon);
    }

    @Test
    @DisplayName("F8-B4: Exact last tick of day (23999) vs first tick of next day (24000)")
    void testDayTransitionBoundary() {
        long lastTickDay0 = F08_InspectorNoonArrivalTest.calculateTargetArrivalTick(23999L);
        long firstTickDay1 = F08_InspectorNoonArrivalTest.calculateTargetArrivalTick(24000L);

        assertEquals(30000L, lastTickDay0, "Last tick of Day 0 arrives Day 1 Noon (30000)");
        assertEquals(54000L, firstTickDay1, "First tick of Day 1 arrives Day 2 Noon (54000)");
    }

    @Test
    @DisplayName("F8-B5: Long-lived world tick simulation (Day 10000)")
    void testLongLivedWorldHighTicks() {
        long day10000Time = 10000L * 24000L + 15000L;
        long target = F08_InspectorNoonArrivalTest.calculateTargetArrivalTick(day10000Time);

        long expected = 10001L * 24000L + 6000L;
        assertEquals(expected, target);
        assertEquals(6000L, target % 24000L, "Must always land on tick 6000 of that day");
    }
}

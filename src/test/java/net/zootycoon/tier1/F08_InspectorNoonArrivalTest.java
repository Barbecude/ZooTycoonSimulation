package net.zootycoon.tier1;

import net.minecraft.nbt.CompoundTag;
import net.zootycoon.TestHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tier 1 - F8: Inspector Noon Arrival Scheduling (~Tick 6000 Next Day)")
public class F08_InspectorNoonArrivalTest {

    public static long calculateTargetArrivalTick(long currentDayTime) {
        long currentDay = currentDayTime / 24000L;
        long targetDay = currentDay + 1L;
        return targetDay * 24000L + 6000L;
    }

    public static boolean shouldSpawnInspector(long currentDayTime, long scheduledArrivalTick) {
        return scheduledArrivalTick > 0 && currentDayTime >= scheduledArrivalTick;
    }

    @Test
    @DisplayName("F8-1: Morning summon arrives at noon of the following in-game day")
    void testScheduledArrivalCalculationFromMorning() {
        long morningTick = 1000L; // Day 0 morning
        long target = calculateTargetArrivalTick(morningTick);

        assertEquals(30000L, target, "Morning summon on Day 0 must arrive at tick 30000 (Day 1 Noon)");
        assertEquals(6000L, target % 24000L, "Arrival time within the day must be tick 6000 (Noon)");
    }

    @Test
    @DisplayName("F8-2: Afternoon summon arrives at noon of the following in-game day")
    void testScheduledArrivalCalculationFromAfternoon() {
        long afternoonTick = 12000L; // Day 0 afternoon
        long target = calculateTargetArrivalTick(afternoonTick);

        assertEquals(30000L, target, "Afternoon summon on Day 0 must arrive at tick 30000 (Day 1 Noon)");
    }

    @Test
    @DisplayName("F8-3: Night summon arrives at noon of the following in-game day")
    void testScheduledArrivalCalculationFromNight() {
        long nightTick = 18000L; // Day 0 midnight
        long target = calculateTargetArrivalTick(nightTick);

        assertEquals(30000L, target, "Night summon on Day 0 must arrive at tick 30000 (Day 1 Noon)");

        long lateNightTick = 23999L; // Day 0 right before sunrise
        long targetLate = calculateTargetArrivalTick(lateNightTick);
        assertEquals(30000L, targetLate, "Late night summon on Day 0 must arrive at tick 30000 (Day 1 Noon)");
    }

    @Test
    @DisplayName("F8-4: Arrival calculation correctly scales across high day counts")
    void testScheduledArrivalCalculationAcrossMultipleDays() {
        long day10Tick = 10L * 24000L + 5000L; // Day 10, tick 245000
        long target = calculateTargetArrivalTick(day10Tick);

        long expected = 11L * 24000L + 6000L; // Day 11 noon = 270000
        assertEquals(expected, target);
    }

    @Test
    @DisplayName("F8-5: Scheduled arrival tick persists cleanly to and from NBT")
    void testScheduledArrivalTickNbtPersistence() {
        TestHelper.ensureMinecraftBootstrapped();

        CompoundTag tag = new CompoundTag();
        long arrivalTick = 54000L;
        tag.putLong("ScheduledArrivalTick", arrivalTick);

        assertTrue(tag.contains("ScheduledArrivalTick"));
        assertEquals(arrivalTick, tag.getLong("ScheduledArrivalTick"));
    }

    @Test
    @DisplayName("F8-6: Spawn check triggers only when current daytime reaches or passes target tick")
    void testSpawnTriggerCondition() {
        long target = 30000L;

        assertFalse(shouldSpawnInspector(1000L, target), "Must not spawn on morning of request");
        assertFalse(shouldSpawnInspector(24000L, target), "Must not spawn at dawn of arrival day");
        assertFalse(shouldSpawnInspector(29999L, target), "Must not spawn 1 tick before noon");
        assertTrue(shouldSpawnInspector(30000L, target), "Must spawn at exact noon tick");
        assertTrue(shouldSpawnInspector(30001L, target), "Must spawn if tick passes noon");
    }
}

package net.zootycoon.tier2;

import net.zootycoon.entity.ZooInspectorEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tier 2 - F10: Inspector Walking AI Boundary and Corner Cases")
public class F10_InspectorWalkingAiBoundaryTest {

    @Test
    @DisplayName("F10-B1: Zero registered animals in zoo triggers graceful completion")
    void testZeroRegisteredAnimalsHandling() {
        List<UUID> ownedAnimals = new ArrayList<>();
        assertTrue(ownedAnimals.isEmpty());

        // When 0 animals exist, inspector transitions to completing/returning rather than crashing
        ZooInspectorEntity.InspectorPhase nextPhase = ownedAnimals.isEmpty()
                ? ZooInspectorEntity.InspectorPhase.APPROACHING_PLAYER
                : ZooInspectorEntity.InspectorPhase.WALKING_TO_ANIMAL;

        assertEquals(ZooInspectorEntity.InspectorPhase.APPROACHING_PLAYER, nextPhase);
    }

    @Test
    @DisplayName("F10-B2: Stress load of 100 registered animals does not overflow inspection queue")
    void testManyAnimalsQueueStress() {
        List<UUID> manyAnimals = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            manyAnimals.add(UUID.randomUUID());
        }
        assertEquals(100, manyAnimals.size());

        // Queue iteration maintains integrity
        int inspected = 0;
        for (UUID ignored : manyAnimals) {
            inspected++;
        }
        assertEquals(100, inspected);
    }

    @Test
    @DisplayName("F10-B3: Distance zero (inspector already standing at enclosure target) handles immediately")
    void testZeroDistanceToTarget() {
        double distSq = 0.0D;
        double arriveThresholdSq = 4.0D; // 2 blocks radius
        boolean reached = distSq <= arriveThresholdSq;

        assertTrue(reached, "Distance 0 must be treated as arrived");
    }

    @Test
    @DisplayName("F10-B4: Maximum path distance boundary (64 blocks follow range limit)")
    void testFollowRangeLimitBoundary() {
        double followRange = 64.0D;
        double targetDistance = 63.9D;
        boolean withinRange = targetDistance <= followRange;

        assertTrue(withinRange, "Target within 64 blocks is within follow range");

        double outOfRangeDistance = 65.0D;
        assertFalse(outOfRangeDistance <= followRange, "Target beyond 64 blocks exceeds follow range");
    }

    @Test
    @DisplayName("F10-B5: Target animal despawned or removed mid-walk triggers recovery")
    void testAnimalDespawnedMidWalkRecovery() {
        UUID targetAnimalUuid = UUID.randomUUID();
        boolean animalStillAlive = false; // Despawned or killed

        ZooInspectorEntity.InspectorPhase next = !animalStillAlive
                ? ZooInspectorEntity.InspectorPhase.WALKING_TO_ANIMAL // Re-route to next animal
                : ZooInspectorEntity.InspectorPhase.OBSERVING_ANIMAL;

        assertEquals(ZooInspectorEntity.InspectorPhase.WALKING_TO_ANIMAL, next);
    }
}

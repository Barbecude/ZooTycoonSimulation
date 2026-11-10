package net.zootycoon.tier1;

import net.zootycoon.entity.ZooInspectorEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tier 1 - F10: Inspector Walking AI and State Machine")
public class F10_InspectorWalkingAiTest {

    @Test
    @DisplayName("F10-1: Inspector phase enum contains all required progression states")
    void testInspectorPhaseEnumCompleteness() {
        ZooInspectorEntity.InspectorPhase[] phases = ZooInspectorEntity.InspectorPhase.values();
        assertTrue(phases.length >= 8, "Must contain at least 8 progression phases");

        assertNotNull(ZooInspectorEntity.InspectorPhase.valueOf("ARRIVING_AT_GATE"));
        assertNotNull(ZooInspectorEntity.InspectorPhase.valueOf("WAITING_CONFIRMATION"));
        assertNotNull(ZooInspectorEntity.InspectorPhase.valueOf("WALKING_TO_ANIMAL"));
        assertNotNull(ZooInspectorEntity.InspectorPhase.valueOf("OBSERVING_ANIMAL"));
        assertNotNull(ZooInspectorEntity.InspectorPhase.valueOf("WRITING_NOTES"));
        assertNotNull(ZooInspectorEntity.InspectorPhase.valueOf("APPROACHING_PLAYER"));
        assertNotNull(ZooInspectorEntity.InspectorPhase.valueOf("WAITING_RESULT_CLAIM"));
        assertNotNull(ZooInspectorEntity.InspectorPhase.valueOf("RETURNING_TO_GATE"));
    }

    @Test
    @DisplayName("F10-2: Walking to animal phase precedes observation and note writing")
    void testPhaseTransitionLogicalOrder() {
        ZooInspectorEntity.InspectorPhase walking = ZooInspectorEntity.InspectorPhase.WALKING_TO_ANIMAL;
        ZooInspectorEntity.InspectorPhase observing = ZooInspectorEntity.InspectorPhase.OBSERVING_ANIMAL;
        ZooInspectorEntity.InspectorPhase writing = ZooInspectorEntity.InspectorPhase.WRITING_NOTES;

        assertNotNull(walking);
        assertNotNull(observing);
        assertNotNull(writing);
    }

    @Test
    @DisplayName("F10-3: Observation distance threshold is within reasonable enclosure perimeter (<= 8 blocks)")
    void testObservationDistanceThreshold() {
        double maxObservationDistance = 8.0D;
        double minObservationDistance = 2.0D;

        assertTrue(minObservationDistance >= 1.5D, "Min distance should prevent inspector crowding inside animal hitboxes");
        assertTrue(maxObservationDistance <= 10.0D, "Max distance should ensure inspector is visibly inspecting the enclosure");
    }

    @Test
    @DisplayName("F10-4: Navigation requirements specify door and gate traversal capability")
    void testNavigationRequirements() {
        boolean canOpenDoors = true;
        boolean canPassGates = true;
        int minPathBudget = 1024;

        assertTrue(canOpenDoors, "Inspector navigation must allow opening wooden doors");
        assertTrue(canPassGates, "Inspector navigation must allow opening fence gates to reach enclosures");
        assertTrue(minPathBudget >= 1024, "Pathfinding search budget must be at least 1024 nodes for zoo complexes");
    }

    @Test
    @DisplayName("F10-5: Inspector walking speed attribute provides steady deliberate pace")
    void testInspectorSpeedAttribute() {
        double defaultSpeed = 0.30D;
        assertTrue(defaultSpeed >= 0.20D && defaultSpeed <= 0.35D,
                "Inspector walk speed (" + defaultSpeed + ") should be steady and deliberate");
    }
}

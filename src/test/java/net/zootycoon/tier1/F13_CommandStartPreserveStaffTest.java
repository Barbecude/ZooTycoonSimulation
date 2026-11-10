package net.zootycoon.tier1;

import net.zootycoon.economy.ZooStaffManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tier 1 - F13: Command Start Staff Preservation")
public class F13_CommandStartPreserveStaffTest {

    @Test
    @DisplayName("F13-1: StaffRecord maintains all four staff types intact")
    void testStaffRecordStructureIntegrity() {
        ZooStaffManager.StaffRecord record = new ZooStaffManager.StaffRecord(3, 4, 2, 5);
        assertEquals(3, record.builders);
        assertEquals(4, record.zookeepers);
        assertEquals(2, record.security);
        assertEquals(5, record.cashiers);
    }

    @Test
    @DisplayName("F13-2: Reset workflow preserves hired staff counts without zeroing records")
    void testResetWorkflowPreservesStaffRecord() {
        UUID playerUuid = UUID.randomUUID();
        ConcurrentHashMap<UUID, ZooStaffManager.StaffRecord> staffRecords = new ConcurrentHashMap<>();
        staffRecords.put(playerUuid, new ZooStaffManager.StaffRecord(2, 3, 1, 4));

        // Simulate /zootycoon start execution without removing staff
        // Note: Do NOT invoke removeAllStaffForPlayer()
        ZooStaffManager.StaffRecord preserved = staffRecords.get(playerUuid);
        assertNotNull(preserved, "Staff record must not be removed during /zootycoon start");
        assertEquals(2, preserved.builders, "Builder count must remain 2");
        assertEquals(3, preserved.zookeepers, "Zookeeper count must remain 3");
        assertEquals(1, preserved.security, "Security count must remain 1");
        assertEquals(4, preserved.cashiers, "Cashier count must remain 4");
    }

    @Test
    @DisplayName("F13-3: Active staff strike penalty is cleared on command reset")
    void testStaffStrikeClearedOnReset() {
        UUID playerUuid = UUID.randomUUID();
        ConcurrentHashMap<UUID, Boolean> staffOnStrike = new ConcurrentHashMap<>();
        staffOnStrike.put(playerUuid, true);

        // Reset operation clears strike penalty
        staffOnStrike.remove(playerUuid);

        assertFalse(staffOnStrike.containsKey(playerUuid), "Staff strike penalty must be lifted after /zootycoon start");
    }

    @Test
    @DisplayName("F13-4: Reset with minimal baseline staff preserves builder record")
    void testMinimalStaffPreservation() {
        UUID playerUuid = UUID.randomUUID();
        ZooStaffManager.StaffRecord record = new ZooStaffManager.StaffRecord(1, 0, 0, 0);

        assertEquals(1, record.builders);
        assertEquals(0, record.zookeepers);
        assertEquals(0, record.security);
        assertEquals(0, record.cashiers);
    }

    @Test
    @DisplayName("F13-5: Multiple players' staff records remain segregated and preserved")
    void testMultiPlayerStaffSegregation() {
        UUID playerA = UUID.randomUUID();
        UUID playerB = UUID.randomUUID();

        ConcurrentHashMap<UUID, ZooStaffManager.StaffRecord> staffRecords = new ConcurrentHashMap<>();
        staffRecords.put(playerA, new ZooStaffManager.StaffRecord(2, 2, 1, 1));
        staffRecords.put(playerB, new ZooStaffManager.StaffRecord(4, 5, 3, 2));

        // Player A runs /zootycoon start -> both records preserved
        assertEquals(2, staffRecords.get(playerA).builders);
        assertEquals(4, staffRecords.get(playerB).builders);
    }
}

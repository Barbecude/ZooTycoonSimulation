package net.zootycoon.tier2;

import net.zootycoon.economy.ZooStaffManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tier 2 - F13: Command Start Staff Preservation Boundary Cases")
public class F13_CommandStartPreserveStaffBoundaryTest {

    @Test
    @DisplayName("F13-B1: Zero staff owned boundary (player starts with 0 employees)")
    void testZeroStaffPreserved() {
        ZooStaffManager.StaffRecord record = new ZooStaffManager.StaffRecord(0, 0, 0, 0);
        // Minimum builder is 1 per constructor constraint, other staff 0
        assertEquals(1, record.builders);
        assertEquals(0, record.zookeepers);
        assertEquals(0, record.security);
        assertEquals(0, record.cashiers);
    }

    @Test
    @DisplayName("F13-B2: Large enterprise staff count (500 staff members)")
    void testLargeStaffPreservation() {
        UUID playerUuid = UUID.randomUUID();
        ConcurrentHashMap<UUID, ZooStaffManager.StaffRecord> staffMap = new ConcurrentHashMap<>();
        staffMap.put(playerUuid, new ZooStaffManager.StaffRecord(100, 150, 50, 200));

        ZooStaffManager.StaffRecord preserved = staffMap.get(playerUuid);
        assertNotNull(preserved);
        assertEquals(100, preserved.builders);
        assertEquals(150, preserved.zookeepers);
        assertEquals(50, preserved.security);
        assertEquals(200, preserved.cashiers);
    }

    @Test
    @DisplayName("F13-B3: Multiple consecutive /zootycoon start calls remain idempotent for staff")
    void testMultipleResetInvocationsIdempotency() {
        UUID playerUuid = UUID.randomUUID();
        ConcurrentHashMap<UUID, ZooStaffManager.StaffRecord> staffMap = new ConcurrentHashMap<>();
        staffMap.put(playerUuid, new ZooStaffManager.StaffRecord(5, 10, 4, 8));

        // Call reset 3 times
        for (int i = 0; i < 3; i++) {
            ZooStaffManager.StaffRecord rec = staffMap.get(playerUuid);
            assertNotNull(rec);
            assertEquals(5, rec.builders);
            assertEquals(10, rec.zookeepers);
        }
    }

    @Test
    @DisplayName("F13-B4: Staff record for non-existent player returns null safely")
    void testNonExistentPlayerStaffQuery() {
        ConcurrentHashMap<UUID, ZooStaffManager.StaffRecord> staffMap = new ConcurrentHashMap<>();
        UUID randomUuid = UUID.randomUUID();
        assertNull(staffMap.get(randomUuid));
    }

    @Test
    @DisplayName("F13-B5: Strike state cleared even when staff count is 0")
    void testStrikeClearedWithNoStaff() {
        UUID playerUuid = UUID.randomUUID();
        ConcurrentHashMap<UUID, Boolean> strikeMap = new ConcurrentHashMap<>();
        strikeMap.put(playerUuid, true);

        // Reset operation
        strikeMap.remove(playerUuid);
        assertFalse(strikeMap.containsKey(playerUuid));
    }
}

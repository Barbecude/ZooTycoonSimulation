package net.zootycoon.tier1;

import net.minecraft.world.item.Items;
import net.zootycoon.TestHelper;
import net.zootycoon.client.ui.StaffRecruitmentTab;
import net.zootycoon.economy.ZooEconomyEngine;
import net.zootycoon.economy.ZooStaffManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tier 1 - F16: Per-Entity Staff Strike Payment & Salary Icon Display")
public class F16_StaffStrikeAndSalaryDisplayTest {

    @BeforeEach
    void setUp() {
        ZooStaffManager.reset();
    }

    @AfterEach
    void tearDown() {
        ZooStaffManager.reset();
    }

    @Test
    @DisplayName("Resolving one striking staff member leaves other staff entities of the same owner on strike")
    void testPerEntityStrikeResolutionIsolatesIndividualStaff() {
        ZooStaffManager manager = new ZooStaffManager();
        UUID ownerA = UUID.randomUUID();
        UUID ownerB = UUID.randomUUID();
        UUID keeper1 = UUID.randomUUID();
        UUID keeper2 = UUID.randomUUID();
        UUID cashier1 = UUID.randomUUID();
        UUID ownerBKeeper = UUID.randomUUID();

        manager.setStaffOnStrike(ownerA, true);
        manager.setStaffOnStrike(ownerB, true);

        assertTrue(manager.isStaffEntityOnStrike(ownerA, keeper1));
        assertTrue(manager.isStaffEntityOnStrike(ownerA, keeper2));
        assertTrue(manager.isStaffEntityOnStrike(ownerA, cashier1));
        assertTrue(manager.isStaffEntityOnStrike(ownerB, ownerBKeeper));

        // Player pays only keeper1 via dialog
        manager.resolveStaffEntityStrike(ownerA, keeper1);

        assertFalse(manager.isStaffEntityOnStrike(ownerA, keeper1), "Paid staff entity must resume work immediately");
        assertTrue(manager.isStaffEntityOnStrike(ownerA, keeper2), "Unpaid sibling zookeeper must stay on strike");
        assertTrue(manager.isStaffEntityOnStrike(ownerA, cashier1), "Unpaid cashier must stay on strike");
        assertTrue(manager.isStaffEntityOnStrike(ownerB, ownerBKeeper), "Other owner's staff must stay on strike");

        // Player visits cashier1 next
        manager.resolveStaffEntityStrike(ownerA, cashier1);
        assertFalse(manager.isStaffEntityOnStrike(ownerA, cashier1));
        assertTrue(manager.isStaffEntityOnStrike(ownerA, keeper2), "Last unpaid staff must still be on strike until visited");

        // Player visits last remaining staff keeper2
        manager.resolveStaffEntityStrike(ownerA, keeper2);
        assertFalse(manager.isStaffEntityOnStrike(ownerA, keeper2));

        // Subsequent missed wage puts everyone on strike again (new strike epoch)
        manager.setStaffOnStrike(ownerA, true);
        assertTrue(manager.isStaffEntityOnStrike(ownerA, keeper1));
        assertTrue(manager.isStaffEntityOnStrike(ownerA, keeper2));
        assertTrue(manager.isStaffEntityOnStrike(ownerA, cashier1));

        // Explicit full clear lifts strike for all entities of that owner
        manager.setStaffOnStrike(ownerA, false);
        assertFalse(manager.isStaffEntityOnStrike(ownerA, keeper1));
        assertFalse(manager.isStaffEntityOnStrike(ownerA, keeper2));
        assertFalse(manager.isStaffEntityOnStrike(ownerA, cashier1));
        assertTrue(manager.isStaffEntityOnStrike(ownerB, ownerBKeeper));
    }

    @Test
    @DisplayName("checkStrikeResolutionOnDeposit never auto-clears active strikes")
    void testDepositDoesNotAutoClearStrike() {
        ZooStaffManager manager = new ZooStaffManager();
        UUID owner = UUID.randomUUID();
        UUID staff = UUID.randomUUID();

        manager.setStaffOnStrike(owner, true);
        manager.checkStrikeResolutionOnDeposit(owner, null);

        assertTrue(manager.isStaffEntityOnStrike(owner, staff),
                "Strike must remain active until player visits each staff via dialog");
    }

    @Test
    @DisplayName("Daily wage lookup supports both staff keys and dialog role keys")
    void testDailyWageLookup() {
        assertEquals(ZooEconomyEngine.ZOOKEEPER_DAILY_WAGE, ZooEconomyEngine.getStaffDailyWage("zookeeper"));
        assertEquals(ZooEconomyEngine.ZOOKEEPER_DAILY_WAGE, ZooEconomyEngine.getStaffDailyWage("zootycoon.dialog.zookeeper.role"));
        assertEquals(ZooEconomyEngine.SECURITY_DAILY_WAGE, ZooEconomyEngine.getStaffDailyWage("security"));
        assertEquals(ZooEconomyEngine.SECURITY_DAILY_WAGE, ZooEconomyEngine.getStaffDailyWage("zootycoon.dialog.security.role"));
        assertEquals(ZooEconomyEngine.CASHIER_DAILY_WAGE, ZooEconomyEngine.getStaffDailyWage("cashier"));
        assertEquals(ZooEconomyEngine.CASHIER_DAILY_WAGE, ZooEconomyEngine.getStaffDailyWage("zootycoon.dialog.cashier.role"));
        assertEquals(ZooEconomyEngine.BUILDER_DAILY_WAGE, ZooEconomyEngine.getStaffDailyWage("builder"));
        assertEquals(ZooEconomyEngine.BUILDER_DAILY_WAGE, ZooEconomyEngine.getStaffDailyWage("zootycoon.dialog.builder.role"));
    }

    @Test
    @DisplayName("Staff salary UI keys exist in both languages and omit the literal word Emerald")
    void testStaffSalaryUiUsesEmeraldIconWithoutEmeraldText() {
        TestHelper.ensureMinecraftBootstrapped();
        assertTrue(StaffRecruitmentTab.VANILLA_EMERALD_ITEM.is(Items.EMERALD));

        assertEquals(String.valueOf(ZooEconomyEngine.ZOOKEEPER_DAILY_WAGE), StaffRecruitmentTab.getWageValueText("zookeeper"));
        assertEquals(String.valueOf(ZooEconomyEngine.SECURITY_DAILY_WAGE), StaffRecruitmentTab.getWageValueText("security"));
        assertEquals(String.valueOf(ZooEconomyEngine.CASHIER_DAILY_WAGE), StaffRecruitmentTab.getWageValueText("cashier"));

        Map<String, String> en = TestHelper.getEnLang();
        Map<String, String> id = TestHelper.getIdLang();

        String[] keys = {
                "zootycoon.ui.staff.wage_daily_label",
                "zootycoon.ui.staff.wage_label",
                "zootycoon.ui.staff.wage_per_project"
        };
        for (String key : keys) {
            assertTrue(en.containsKey(key), "Missing en_us key: " + key);
            assertTrue(id.containsKey(key), "Missing id_id key: " + key);
            assertFalse(en.get(key).toLowerCase().contains("emerald"),
                    "en_us key must not contain 'emerald' word: " + key);
            assertFalse(id.get(key).toLowerCase().contains("emerald"),
                    "id_id key must not contain 'emerald' word: " + key);
        }
    }
}

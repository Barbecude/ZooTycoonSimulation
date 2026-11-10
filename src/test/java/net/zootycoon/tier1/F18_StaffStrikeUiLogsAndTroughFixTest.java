package net.zootycoon.tier1;

import net.minecraft.world.item.Items;
import net.zootycoon.TestHelper;
import net.zootycoon.animal.AnimalDietRegistry;
import net.zootycoon.client.ui.StaffRecruitmentTab;
import net.zootycoon.entity.ZooZookeeperEntity;
import net.zootycoon.log.ZooLogManager;
import net.zootycoon.network.ZooNetworkPayloads;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tier 1 - F18: Strike Logs, Kelola Staff Exclamation Icons, Calling & Trough Refill Fix")
public class F18_StaffStrikeUiLogsAndTroughFixTest {

    @Test
    @DisplayName("ZooLogManager records exact striking staff names/roles and builds live active strike summary")
    void testStrikeStaffAppearsInLogs() {
        UUID owner = UUID.randomUUID();
        ZooLogManager.resetLogsAndStats(null, owner);

        List<String> strikers = List.of("Zookeeper: Budi", "Kasir: Sari", "Security: Eko");
        ZooLogManager.recordStaffStrikeDetails(owner, 200, strikers);

        List<ZooNetworkPayloads.ZooLogEntryDto> stored = ZooLogManager.getLogsForOwner(owner);
        assertFalse(stored.isEmpty(), "Strike log entry must be recorded");
        assertEquals("SALARY", stored.get(0).category());
        assertEquals(3, stored.get(0).urgency());
        assertTrue(stored.get(0).description().contains("Zookeeper: Budi"));
        assertTrue(stored.get(0).description().contains("Kasir: Sari"));

        List<ZooNetworkPayloads.ZooLogEntryDto> syncedWithActive = ZooLogManager.withActiveStrikeSummary(stored, List.of("Kasir: Sari"));
        assertEquals(stored.size() + 1, syncedWithActive.size());
        assertTrue(syncedWithActive.get(0).title().contains("Mogok"));
        assertTrue(syncedWithActive.get(0).description().contains("Kasir: Sari"));

        List<ZooNetworkPayloads.ZooLogEntryDto> syncedResolved = ZooLogManager.withActiveStrikeSummary(stored, List.of());
        assertEquals(stored.size(), syncedResolved.size());

        ZooLogManager.recordIndividualSalaryPayment(owner, "Kasir", "Sari", 40);
        List<ZooNetworkPayloads.ZooLogEntryDto> afterPay = ZooLogManager.getLogsForOwner(owner);
        assertTrue(afterPay.get(0).description().contains("Kasir Sari"));
        assertEquals(-40, afterPay.get(0).emeraldDelta());
    }

    @Test
    @DisplayName("Exclamation mark icon resource and strike UI translation keys exist")
    void testExclamationIconAndStrikeUiKeys() throws Exception {
        TestHelper.ensureMinecraftBootstrapped();
        assertEquals("textures/gui/icons/exclamation_point.png", StaffRecruitmentTab.EXCLAMATION_ICON.getPath());

        File iconFile = TestHelper.getAssetFile("textures/gui/icons/exclamation_point.png");
        assertTrue(iconFile.exists(), "Exclamation point icon file must exist");
        TestHelper.PngDimensions dims = TestHelper.getPngDimensions(iconFile);
        assertTrue(dims.validPng());

        Map<String, String> en = TestHelper.getEnLang();
        Map<String, String> id = TestHelper.getIdLang();
        assertTrue(en.containsKey("zootycoon.ui.staff.manage.on_strike"));
        assertTrue(id.containsKey("zootycoon.ui.staff.manage.on_strike"));
        assertTrue(en.containsKey("zootycoon.ui.staff.strike_tooltip"));
        assertTrue(id.containsKey("zootycoon.ui.staff.strike_tooltip"));
    }

    @Test
    @DisplayName("All 4 staff sync DTOs expose isOnStrike flag and staff entities allow call ticks while on strike")
    void testSyncDtosAndCallingStrikingStaffSupport() throws Exception {
        UUID id = UUID.randomUUID();
        var zk = new ZooNetworkPayloads.ZookeeperSyncData(id, "Budi", 10, true, 99, false, true);
        var b = new ZooNetworkPayloads.BuilderSyncData(id, "Joko", 11, true, "Pavilion", false, false, true);
        var sec = new ZooNetworkPayloads.SecuritySyncData(id, "Eko", 12, false, false, true);
        var cash = new ZooNetworkPayloads.CashierSyncData(id, "Sari", 13, "ticket", 0, false, true);

        assertTrue(zk.isOnStrike());
        assertTrue(b.isOnStrike());
        assertTrue(sec.isOnStrike());
        assertTrue(cash.isOnStrike());

        String staffMgr = Files.readString(Path.of("src/main/java/net/zootycoon/economy/ZooStaffManager.java"));
        assertTrue(staffMgr.contains("!zk.isOnStrike()"), "Striking zookeepers must still be callable");
        assertTrue(staffMgr.contains("!b.isOnStrike()"), "Striking builders must still be callable");
    }

    @Test
    @DisplayName("Zookeepers only refill assigned feeding troughs for trough-eating diets")
    void testZookeeperNeverFillsUnlinkedTroughsOrNonTroughAnimalDiets() throws Exception {
        TestHelper.ensureMinecraftBootstrapped();

        var grazing = new AnimalDietRegistry.AnimalDiet(AnimalDietRegistry.FeedingType.GRAZING, "Grazing", false, false, Items.WHEAT);
        var thrownMeat = new AnimalDietRegistry.AnimalDiet(AnimalDietRegistry.FeedingType.THROWN_MEAT, "Karnivora", true, false, Items.BEEF);
        var trough = new AnimalDietRegistry.AnimalDiet(AnimalDietRegistry.FeedingType.TROUGH, "Herbivora", false, false, Items.WHEAT);
        var specialTrough = new AnimalDietRegistry.AnimalDiet(AnimalDietRegistry.FeedingType.SPECIAL_TROUGH, "Insektivora", false, false, Items.SPIDER_EYE);

        assertFalse(ZooZookeeperEntity.requiresFeedingTrough(grazing), "GRAZING animals must not trigger feeding trough refill");
        assertFalse(ZooZookeeperEntity.requiresFeedingTrough(thrownMeat), "THROWN_MEAT animals must not trigger feeding trough refill");
        assertTrue(ZooZookeeperEntity.requiresFeedingTrough(trough), "TROUGH animals require feeding trough refill");
        assertTrue(ZooZookeeperEntity.requiresFeedingTrough(specialTrough), "SPECIAL_TROUGH animals require feeding trough refill");

        assertFalse(ZooZookeeperEntity.isTroughEligibleForFamilyRefill(null, List.of()));

        String zkSource = Files.readString(Path.of("src/main/java/net/zootycoon/entity/ZooZookeeperEntity.java"));
        assertFalse(zkSource.contains("fallbackTrough"), "Unassigned fallback trough logic must be completely removed");
        assertTrue(zkSource.contains("else if (requiresFeedingTrough(diet))"), "Grazing animals must never fall through to trough refilling");
    }
}

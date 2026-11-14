package net.zootycoon.license;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.zootycoon.animal.ZooAnimalHungerManager;
import net.zootycoon.claim.ZooClaim;
import net.zootycoon.claim.ZooClaimManager;
import net.zootycoon.entity.ZooCashierEntity;
import net.zootycoon.entity.ZooEntities;
import net.zootycoon.entity.ZooInspectorEntity;
import net.zootycoon.entity.ZooVisitorEntity;
import net.zootycoon.item.InspectionLetterItem;
import net.zootycoon.item.LicenseBannerItem;
import net.zootycoon.network.ZooNetworkPayloads;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ZooLicenseManager {

    public static class TierProgressData {
        public LicenseProgressionState state;
        public boolean inspectionPassed;
        public boolean letterClaimed;
        public boolean licenseClaimed;
        public boolean bannerClaimed;
        public boolean licenseUnlocked;
        public final List<String> inspectedSpecies = new ArrayList<>();
        public final List<String> improvementNotes = new ArrayList<>();

        public TierProgressData(int tierLevel) {
            if (tierLevel == 1) {
                this.state = LicenseProgressionState.COLLECTING;
                this.licenseUnlocked = true;
            } else {
                this.state = LicenseProgressionState.LOCKED;
                this.licenseUnlocked = false;
            }
            this.inspectionPassed = false;
            this.letterClaimed = false;
            this.licenseClaimed = false;
            this.bannerClaimed = false;
        }

        public CompoundTag toNbt() {
            CompoundTag tag = new CompoundTag();
            tag.putString("State", state.name());
            tag.putBoolean("InspectionPassed", inspectionPassed);
            tag.putBoolean("LetterClaimed", letterClaimed);
            tag.putBoolean("LicenseClaimed", licenseClaimed);
            tag.putBoolean("BannerClaimed", bannerClaimed);
            tag.putBoolean("LicenseUnlocked", licenseUnlocked);

            ListTag spList = new ListTag();
            for (String s : inspectedSpecies) spList.add(StringTag.valueOf(s));
            tag.put("InspectedSpecies", spList);

            ListTag noteList = new ListTag();
            for (String n : improvementNotes) noteList.add(StringTag.valueOf(n));
            tag.put("ImprovementNotes", noteList);
            return tag;
        }

        public void fromNbt(CompoundTag tag, int tierLevel) {
            LicenseProgressionState defaultState = tierLevel == 1 ? LicenseProgressionState.COLLECTING : LicenseProgressionState.LOCKED;
            this.state = LicenseProgressionState.fromName(tag.getString("State"), defaultState);
            this.inspectionPassed = tag.getBoolean("InspectionPassed");
            this.letterClaimed = tag.getBoolean("LetterClaimed");
            this.licenseClaimed = tag.getBoolean("LicenseClaimed");
            this.bannerClaimed = tag.getBoolean("BannerClaimed");
            this.licenseUnlocked = tierLevel == 1 || tag.getBoolean("LicenseUnlocked");

            this.inspectedSpecies.clear();
            if (tag.contains("InspectedSpecies", Tag.TAG_LIST)) {
                ListTag l = tag.getList("InspectedSpecies", Tag.TAG_STRING);
                for (int i = 0; i < l.size(); i++) this.inspectedSpecies.add(l.getString(i));
            }
            this.improvementNotes.clear();
            if (tag.contains("ImprovementNotes", Tag.TAG_LIST)) {
                ListTag l = tag.getList("ImprovementNotes", Tag.TAG_STRING);
                for (int i = 0; i < l.size(); i++) this.improvementNotes.add(l.getString(i));
            }
        }
    }

    public static class PlayerLicenseProfile {
        public int activeTierLevel = 1;
        public final Set<String> ownedSpeciesKeys = ConcurrentHashMap.newKeySet();
        public final Map<Integer, TierProgressData> tierMap = new ConcurrentHashMap<>();
        public UUID activeInspectorUuid = null;
        public long scheduledInspectorArrivalTick = -1L;

        public PlayerLicenseProfile() {
            for (LicenseTier t : LicenseTier.values()) {
                tierMap.put(t.getTierLevel(), new TierProgressData(t.getTierLevel()));
            }
        }

        public TierProgressData getTierData(int tierLevel) {
            return tierMap.computeIfAbsent(tierLevel, TierProgressData::new);
        }

        public CompoundTag toNbt() {
            CompoundTag tag = new CompoundTag();
            tag.putInt("ActiveTierLevel", activeTierLevel);
            tag.putLong("ScheduledInspectorArrivalTick", scheduledInspectorArrivalTick);
            if (activeInspectorUuid != null) {
                tag.putString("ActiveInspectorUuid", activeInspectorUuid.toString());
            }

            ListTag ownedList = new ListTag();
            for (String key : ownedSpeciesKeys) {
                ownedList.add(StringTag.valueOf(key));
            }
            tag.put("OwnedSpeciesKeys", ownedList);

            CompoundTag tiersTag = new CompoundTag();
            for (var entry : tierMap.entrySet()) {
                tiersTag.put(String.valueOf(entry.getKey()), entry.getValue().toNbt());
            }
            tag.put("Tiers", tiersTag);
            return tag;
        }

        public static PlayerLicenseProfile fromNbt(CompoundTag tag) {
            PlayerLicenseProfile p = new PlayerLicenseProfile();
            p.activeTierLevel = Math.clamp(tag.contains("ActiveTierLevel") ? tag.getInt("ActiveTierLevel") : 1, 1, 5);
            p.scheduledInspectorArrivalTick = tag.contains("ScheduledInspectorArrivalTick")
                    ? tag.getLong("ScheduledInspectorArrivalTick")
                    : -1L;
            if (tag.contains("ActiveInspectorUuid")) {
                try {
                    p.activeInspectorUuid = UUID.fromString(tag.getString("ActiveInspectorUuid"));
                } catch (IllegalArgumentException ignored) {}
            }
            if (tag.contains("OwnedSpeciesKeys", Tag.TAG_LIST)) {
                ListTag l = tag.getList("OwnedSpeciesKeys", Tag.TAG_STRING);
                for (int i = 0; i < l.size(); i++) {
                    p.ownedSpeciesKeys.add(l.getString(i));
                }
            }
            if (tag.contains("Tiers", Tag.TAG_COMPOUND)) {
                CompoundTag tiersTag = tag.getCompound("Tiers");
                for (LicenseTier t : LicenseTier.values()) {
                    String k = String.valueOf(t.getTierLevel());
                    if (tiersTag.contains(k, Tag.TAG_COMPOUND)) {
                        p.getTierData(t.getTierLevel()).fromNbt(tiersTag.getCompound(k), t.getTierLevel());
                    }
                }
            }
            return p;
        }
    }

    private static final Map<UUID, PlayerLicenseProfile> PROFILES = new ConcurrentHashMap<>();

    // Client-side cache for UI rendering
    private static int clientActiveTierLevel = 1;
    private static final Set<String> clientOwnedSpeciesKeys = ConcurrentHashMap.newKeySet();
    private static final Map<Integer, TierProgressData> clientTierMap = new ConcurrentHashMap<>();

    static {
        for (LicenseTier t : LicenseTier.values()) {
            clientTierMap.put(t.getTierLevel(), new TierProgressData(t.getTierLevel()));
        }
    }

    public static ZooLicenseManager get(ServerLevel level) {
        // Ensure ZooClaimManager is loaded so our NBT data is initialized
        ZooClaimManager.get(level);
        return INSTANCE;
    }

    private static final ZooLicenseManager INSTANCE = new ZooLicenseManager();

    public static void reset() {
        PROFILES.clear();
    }

    public static CompoundTag saveAllToNbt() {
        CompoundTag root = new CompoundTag();
        for (var entry : PROFILES.entrySet()) {
            root.put(entry.getKey().toString(), entry.getValue().toNbt());
        }
        return root;
    }

    public static void loadAllFromNbt(CompoundTag root) {
        PROFILES.clear();
        if (root == null) return;
        for (String key : root.getAllKeys()) {
            try {
                UUID uuid = UUID.fromString(key);
                PROFILES.put(uuid, PlayerLicenseProfile.fromNbt(root.getCompound(key)));
            } catch (Exception ignored) {}
        }
    }

    public PlayerLicenseProfile getProfile(UUID playerUuid) {
        return PROFILES.computeIfAbsent(playerUuid, k -> new PlayerLicenseProfile());
    }

    public void resetProfileForPlayer(ServerLevel level, ServerPlayer player) {
        if (player == null) return;
        UUID uuid = player.getUUID();
        PlayerLicenseProfile profile = getProfile(uuid);
        if (profile.activeInspectorUuid != null && level != null) {
            var entity = level.getEntity(profile.activeInspectorUuid);
            if (entity != null) {
                entity.discard();
            }
        }
        PlayerLicenseProfile freshProfile = new PlayerLicenseProfile();
        PROFILES.put(uuid, freshProfile);
        save(level);
        net.zootycoon.network.ZooNetworkPayloads.sendLicenseSync(player);
    }

    private void save(ServerLevel level) {
        if (level != null) {
            ZooClaimManager.get(level).saveToFile();
        }
    }

    public static boolean isSpeciesUnlockedForPlayer(ServerLevel level, ServerPlayer player, EntityType<?> entityType) {
        if (player == null || entityType == null) return true;
        if (player.isCreative()) return true;
        return get(level).isSpeciesUnlocked(player.getUUID(), entityType);
    }

    public boolean isSpeciesUnlocked(UUID playerUuid, EntityType<?> entityType) {
        if (playerUuid == null || entityType == null) return true;
        LicenseTier requiredTier = LicenseTier.findTierForEntity(entityType);
        if (requiredTier == null) {
            // Non-restricted animal (e.g., vanilla cow, pig, chicken) is always allowed
            return true;
        }
        PlayerLicenseProfile profile = getProfile(playerUuid);
        TierProgressData tierData = profile.getTierData(requiredTier.getTierLevel());
        return tierData.licenseUnlocked;
    }

    public static LicenseTier getRequiredTierForEntity(EntityType<?> entityType) {
        return LicenseTier.findTierForEntity(entityType);
    }

    public void onAnimalRegistered(ServerLevel level, ServerPlayer player, LivingEntity animal) {
        if (player == null || animal == null) return;
        PlayerLicenseProfile profile = getProfile(player.getUUID());
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(animal.getType());
        if (id != null) {
            LicenseTier.SpeciesEntry entry = LicenseTier.findSpeciesEntry(id.toString());
            if (entry != null) {
                profile.ownedSpeciesKeys.add(entry.speciesKey());
                recalculateActiveTierEligibility(profile);
                save(level);
                ZooNetworkPayloads.sendLicenseSync(player);
            }
        }
    }

    /** Price in emeralds of one animal information board sold from the collection modal. */
    public static final int INFO_BOARD_PRICE = 10;

    public void buyAnimalInfoBanner(ServerPlayer player, String speciesKey) {
        if (player == null || !(player.level() instanceof ServerLevel level)) return;
        LicenseTier.SpeciesEntry entry = LicenseTier.findSpeciesEntry(speciesKey);
        if (entry == null) return;

        PlayerLicenseProfile profile = getProfile(player.getUUID());
        syncOwnedSpeciesFromWorld(level, player);
        if (!profile.ownedSpeciesKeys.contains(entry.speciesKey())) {
            ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.inspector",
                    "zootycoon.license.toast.info_board_not_owned", ZooNetworkPayloads.TOAST_WARNING, entry.getDisplayName());
            return;
        }

        int tierLevel = 1;
        for (LicenseTier tier : LicenseTier.values()) {
            if (tier.getRequiredSpecies().contains(entry)) {
                tierLevel = tier.getTierLevel();
                break;
            }
        }

        if (!player.isCreative()) {
            if (!net.zootycoon.economy.TransactionHelper.hasEnoughEmeralds(player, INFO_BOARD_PRICE)
                    || !net.zootycoon.economy.TransactionHelper.consumeEmeralds(player, INFO_BOARD_PRICE)) {
                ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.inspector",
                        "zootycoon.license.toast.info_board_no_funds", ZooNetworkPayloads.TOAST_WARNING, INFO_BOARD_PRICE);
                return;
            }
        }

        var stack = LicenseBannerItem.createAnimalInfoBannerStack(tierLevel, entry.speciesKey(), player.getName().getString() + "'s Zoo");
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
        ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.inspector",
                "zootycoon.license.toast.info_board_bought", ZooNetworkPayloads.TOAST_SUCCESS, entry.getDisplayName());
    }

    public void syncOwnedSpeciesFromWorld(ServerLevel level, ServerPlayer player) {
        if (level == null || player == null) return;
        PlayerLicenseProfile profile = getProfile(player.getUUID());
        boolean changed = false;

        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof LivingEntity le && le.isAlive()
                    && ZooAnimalHungerManager.isTagged(le)
                    && ZooAnimalHungerManager.isOwnedBy(le, player.getUUID())) {
                ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(le.getType());
                if (id != null) {
                    LicenseTier.SpeciesEntry entry = LicenseTier.findSpeciesEntry(id.toString());
                    if (entry != null && profile.ownedSpeciesKeys.add(entry.speciesKey())) {
                        changed = true;
                    }
                }
            }
        }

        if (recalculateActiveTierEligibility(profile)) {
            changed = true;
        }
        if (changed) {
            save(level);
        }
    }

    public void onAnimalRemovedOrUnowned(ServerLevel level, UUID playerUuid) {
        if (level == null || playerUuid == null) return;
        PlayerLicenseProfile profile = getProfile(playerUuid);
        Set<String> stillOwned = new HashSet<>();
        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof LivingEntity le && le.isAlive()
                    && ZooAnimalHungerManager.isTagged(le)
                    && ZooAnimalHungerManager.isOwnedBy(le, playerUuid)) {
                ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(le.getType());
                if (id != null) {
                    LicenseTier.SpeciesEntry entry = LicenseTier.findSpeciesEntry(id.toString());
                    if (entry != null) {
                        stillOwned.add(entry.speciesKey());
                    }
                }
            }
        }
        profile.ownedSpeciesKeys.clear();
        profile.ownedSpeciesKeys.addAll(stillOwned);
        recalculateActiveTierEligibility(profile);
        save(level);

        ServerPlayer sp = level.getServer().getPlayerList().getPlayer(playerUuid);
        if (sp != null) {
            ZooNetworkPayloads.sendLicenseSync(sp);
        }
    }

    private boolean recalculateActiveTierEligibility(PlayerLicenseProfile profile) {
        LicenseTier activeTier = LicenseTier.byLevel(profile.activeTierLevel);
        TierProgressData tierData = profile.getTierData(activeTier.getTierLevel());

        // Only auto-switch between COLLECTING and ELIGIBLE_FOR_INSPECTION
        if (tierData.state == LicenseProgressionState.COLLECTING
                || tierData.state == LicenseProgressionState.ELIGIBLE_FOR_INSPECTION) {
            boolean allOwned = hasAllSpeciesForTier(profile, activeTier);
            LicenseProgressionState target = allOwned
                    ? LicenseProgressionState.ELIGIBLE_FOR_INSPECTION
                    : LicenseProgressionState.COLLECTING;
            if (tierData.state != target) {
                tierData.state = target;
                return true;
            }
        }
        return false;
    }

    public boolean hasAllSpeciesForTier(PlayerLicenseProfile profile, LicenseTier tier) {
        for (LicenseTier.SpeciesEntry entry : tier.getRequiredSpecies()) {
            if (!profile.ownedSpeciesKeys.contains(entry.speciesKey())) {
                return false;
            }
        }
        return true;
    }

    public int getMissingSpeciesCountForActiveTier(UUID playerUuid) {
        PlayerLicenseProfile profile = getProfile(playerUuid);
        LicenseTier tier = LicenseTier.byLevel(profile.activeTierLevel);
        int missing = 0;
        for (LicenseTier.SpeciesEntry entry : tier.getRequiredSpecies()) {
            if (!profile.ownedSpeciesKeys.contains(entry.speciesKey())) {
                missing++;
            }
        }
        return missing;
    }

    public void requestInspection(ServerPlayer player) {
        if (player == null || !(player.level() instanceof ServerLevel level)) return;
        syncOwnedSpeciesFromWorld(level, player);

        PlayerLicenseProfile profile = getProfile(player.getUUID());
        LicenseTier activeTier = LicenseTier.byLevel(profile.activeTierLevel);
        TierProgressData tierData = profile.getTierData(activeTier.getTierLevel());

        boolean creativeBypass = player.isCreative();

        // Check if an inspector already exists in the world for this player
        ZooInspectorEntity existing = findActiveInspector(level, player.getUUID(), profile.activeInspectorUuid);
        if (existing != null && existing.isAlive()) {
            boolean canRelocateIdleCreative = creativeBypass
                    && (existing.getPhase() == ZooInspectorEntity.InspectorPhase.WAITING_CONFIRMATION
                            || existing.getPhase() == ZooInspectorEntity.InspectorPhase.WAITING_RESULT_CLAIM)
                    && existing.distanceToSqr(player) > 100.0D;
            if (canRelocateIdleCreative) {
                BlockPos nearPos = findInspectorCreativeSpawnPos(level, player);
                existing.setGatePos(nearPos);
                existing.moveTo(nearPos.getX() + 0.5, nearPos.getY(), nearPos.getZ() + 0.5, player.getYRot() + 180.0F, 0.0F);
                profile.scheduledInspectorArrivalTick = -1L;
                save(level);
                ZooNetworkPayloads.sendLicenseSync(player);
                ZooNetworkPayloads.sendToast(
                        player,
                        "zootycoon.toast.title.inspector",
                        "zootycoon.license.toast.inspector_arrived",
                        ZooNetworkPayloads.TOAST_INFO
                );
                return;
            }
            ZooNetworkPayloads.sendToast(
                    player,
                    "zootycoon.toast.title.inspector",
                    "zootycoon.license.toast.already_active",
                    ZooNetworkPayloads.TOAST_WARNING
            );
            return;
        }

        if (!creativeBypass && profile.scheduledInspectorArrivalTick > 0) {
            ZooNetworkPayloads.sendToast(
                    player,
                    "zootycoon.toast.title.inspector",
                    "zootycoon.license.toast.already_scheduled",
                    ZooNetworkPayloads.TOAST_WARNING
            );
            return;
        }

        // If player already passed inspection and hasn't claimed license yet, spawn inspector right near gate/player to claim
        boolean alreadyPassedPendingClaim = tierData.inspectionPassed && (!tierData.licenseClaimed || !tierData.letterClaimed);

        int missingCount = getMissingSpeciesCountForActiveTier(player.getUUID());
        if (!creativeBypass && !alreadyPassedPendingClaim && missingCount > 0) {
            ZooNetworkPayloads.sendToast(
                    player,
                    "zootycoon.toast.title.inspector",
                    "zootycoon.license.toast.missing_species",
                    ZooNetworkPayloads.TOAST_WARNING,
                    missingCount,
                    activeTier.getDisplayName()
            );
            return;
        }

        // F7: Inspector star rating guard
        double currentStars = net.zootycoon.economy.ZooRatingManager.get(level.getServer()).getStarRating(player.getUUID());
        double requiredStars = activeTier.getRequiredStars();
        if (!creativeBypass && !alreadyPassedPendingClaim && currentStars < requiredStars) {
            ZooNetworkPayloads.sendToast(
                    player,
                    "zootycoon.toast.title.inspector",
                    "zootycoon.license.toast.rating_insufficient",
                    ZooNetworkPayloads.TOAST_WARNING,
                    String.format(java.util.Locale.ROOT, "%.1f", requiredStars),
                    String.format(java.util.Locale.ROOT, "%.1f", currentStars)
            );
            return;
        }

        ZooClaimManager claimManager = ZooClaimManager.get(level);
        List<ZooClaim> claims = claimManager.getPlayerClaims(player.getUUID());
        ZooCashierEntity ticketCashier = findTicketCashierInZoo(level, claims);

        if (!creativeBypass && (claims.isEmpty() || ticketCashier == null)) {
            ZooNetworkPayloads.sendToast(
                    player,
                    "zootycoon.toast.title.inspector",
                    "zootycoon.license.toast.need_ticket_gate",
                    ZooNetworkPayloads.TOAST_WARNING
            );
            return;
        }

        if (alreadyPassedPendingClaim || creativeBypass) {
            BlockPos spawnAndWaitPos = creativeBypass
                    ? findInspectorCreativeSpawnPos(level, player)
                    : findInspectorGateSpawnPos(level, player, ticketCashier);
            ZooInspectorEntity inspector = ZooEntities.INSPECTOR.create(level);
            if (inspector == null) return;

            inspector.moveTo(spawnAndWaitPos.getX() + 0.5, spawnAndWaitPos.getY(), spawnAndWaitPos.getZ() + 0.5, player.getYRot() + 180.0F, 0.0F);
            inspector.setupInitialArrival(player.getUUID(), activeTier.getTierLevel(), spawnAndWaitPos);
            if (alreadyPassedPendingClaim) {
                inspector.setPhase(ZooInspectorEntity.InspectorPhase.WAITING_RESULT_CLAIM);
            } else {
                tierData.state = LicenseProgressionState.WAITING_FOR_CONFIRMATION;
                inspector.setPhase(ZooInspectorEntity.InspectorPhase.WAITING_CONFIRMATION);
            }

            level.addFreshEntity(inspector);
            profile.activeInspectorUuid = inspector.getUUID();
            profile.scheduledInspectorArrivalTick = -1L;
            save(level);
            ZooNetworkPayloads.sendLicenseSync(player);

            ZooNetworkPayloads.sendToast(
                    player,
                    "zootycoon.toast.title.inspector",
                    "zootycoon.license.toast.inspector_arrived",
                    ZooNetworkPayloads.TOAST_INFO
            );
        } else {
            // F8: Inspector noon arrival scheduling for next day
            long currentDay = level.getDayTime() / 24000L;
            long targetTick = (currentDay + 1L) * 24000L + 6000L;
            profile.scheduledInspectorArrivalTick = targetTick;
            tierData.state = LicenseProgressionState.INSPECTOR_REQUESTED;
            save(level);
            ZooNetworkPayloads.sendLicenseSync(player);

            ZooNetworkPayloads.sendToast(
                    player,
                    "zootycoon.toast.title.inspector",
                    "zootycoon.license.toast.inspector_scheduled",
                    ZooNetworkPayloads.TOAST_INFO
            );
        }
    }

    public void tickServer(ServerLevel level) {
        if (level == null || level.getServer() == null) return;
        long currentDayTime = level.getDayTime();

        for (Map.Entry<UUID, PlayerLicenseProfile> entry : PROFILES.entrySet()) {
            UUID playerUuid = entry.getKey();
            PlayerLicenseProfile profile = entry.getValue();

            if (profile.scheduledInspectorArrivalTick > 0 && currentDayTime >= profile.scheduledInspectorArrivalTick) {
                ServerPlayer player = level.getServer().getPlayerList().getPlayer(playerUuid);
                if (player != null) {
                    spawnScheduledInspector(level, player, profile);
                }
            }
        }
    }

    private void spawnScheduledInspector(ServerLevel level, ServerPlayer player, PlayerLicenseProfile profile) {
        ZooInspectorEntity existing = findActiveInspector(level, player.getUUID(), profile.activeInspectorUuid);
        if (existing != null && existing.isAlive()) {
            profile.scheduledInspectorArrivalTick = -1L;
            save(level);
            return;
        }

        LicenseTier activeTier = LicenseTier.byLevel(profile.activeTierLevel);
        TierProgressData tierData = profile.getTierData(activeTier.getTierLevel());

        ZooClaimManager claimManager = ZooClaimManager.get(level);
        List<ZooClaim> claims = claimManager.getPlayerClaims(player.getUUID());
        ZooCashierEntity ticketCashier = findTicketCashierInZoo(level, claims);

        BlockPos spawnAndWaitPos = findInspectorGateSpawnPos(level, player, ticketCashier);
        ZooInspectorEntity inspector = ZooEntities.INSPECTOR.create(level);
        if (inspector == null) return;

        inspector.moveTo(
                spawnAndWaitPos.getX() + 0.5,
                spawnAndWaitPos.getY(),
                spawnAndWaitPos.getZ() + 0.5,
                player.getYRot() + 180.0F,
                0.0F
        );
        inspector.setupInitialArrival(player.getUUID(), activeTier.getTierLevel(), spawnAndWaitPos);

        tierData.state = LicenseProgressionState.WAITING_FOR_CONFIRMATION;
        inspector.setPhase(ZooInspectorEntity.InspectorPhase.WAITING_CONFIRMATION);

        level.addFreshEntity(inspector);
        profile.activeInspectorUuid = inspector.getUUID();
        profile.scheduledInspectorArrivalTick = -1L;
        save(level);
        ZooNetworkPayloads.sendLicenseSync(player);

        ZooNetworkPayloads.sendToast(
                player,
                "zootycoon.toast.title.inspector",
                "zootycoon.license.toast.inspector_arrived",
                ZooNetworkPayloads.TOAST_INFO
        );
    }

    public void onInspectorWaitingAtGate(UUID ownerUuid) {
        if (ownerUuid == null) return;
        PlayerLicenseProfile profile = getProfile(ownerUuid);
        TierProgressData tierData = profile.getTierData(profile.activeTierLevel);
        if (tierData.state == LicenseProgressionState.INSPECTOR_REQUESTED) {
            tierData.state = LicenseProgressionState.WAITING_FOR_CONFIRMATION;
        }
    }

    public void openInspectorDialog(ServerPlayer player, ZooInspectorEntity inspector) {
        if (player == null || inspector == null) return;
        ZooInspectorEntity.InspectorPhase phase = inspector.getPhase();
        if (phase == ZooInspectorEntity.InspectorPhase.APPROACHING_PLAYER
                || phase == ZooInspectorEntity.InspectorPhase.WAITING_RESULT_CLAIM) {
            if (phase == ZooInspectorEntity.InspectorPhase.APPROACHING_PLAYER) {
                inspector.setPhase(ZooInspectorEntity.InspectorPhase.WAITING_RESULT_CLAIM);
            }
            inspector.markResultDialogOpened();
        }
        PlayerLicenseProfile profile = getProfile(player.getUUID());
        int tierLevel = inspector.getTierLevel();
        TierProgressData tierData = profile.getTierData(tierLevel);

        ZooNetworkPayloads.sendOpenInspectorDialog(
                player,
                inspector.getId(),
                inspector.getPhase().ordinal(),
                tierLevel,
                inspector.getCurrentSpeciesName(),
                inspector.getInspectedCount(),
                inspector.getTotalCount(),
                tierData.inspectionPassed,
                tierData.letterClaimed,
                tierData.licenseClaimed,
                tierData.inspectedSpecies,
                tierData.improvementNotes
        );
    }

    public void handleInspectorAction(ServerPlayer player, int inspectorEntityId, String action) {
        if (player == null || !(player.level() instanceof ServerLevel level)) return;
        Entity e = level.getEntity(inspectorEntityId);
        ZooInspectorEntity inspector = (e instanceof ZooInspectorEntity ins && ins.isAlive()) ? ins : null;
        PlayerLicenseProfile profile = getProfile(player.getUUID());
        int tierLevel = inspector != null ? inspector.getTierLevel() : profile.activeTierLevel;
        TierProgressData tierData = profile.getTierData(tierLevel);

        switch (action) {
            case "START_INSPECTION" -> {
                if (inspector == null) return;
                if (inspector.getPhase() != ZooInspectorEntity.InspectorPhase.WAITING_CONFIRMATION
                        && inspector.getPhase() != ZooInspectorEntity.InspectorPhase.ARRIVING_AT_GATE) {
                    return;
                }
                tierData.state = LicenseProgressionState.INSPECTING;
                tierData.inspectionPassed = false;
                tierData.inspectedSpecies.clear();
                tierData.improvementNotes.clear();
                inspector.beginInspection(level);
                save(level);
                ZooNetworkPayloads.sendLicenseSync(player);
            }
            case "CANCEL_INSPECTION" -> {
                profile.scheduledInspectorArrivalTick = -1L;
                if (inspector != null) {
                    clearActiveInspector(player.getUUID(), inspector.getUUID());
                    inspector.discard();
                }
                recalculateActiveTierEligibility(profile);
                save(level);
                ZooNetworkPayloads.sendLicenseSync(player);
            }
            case "CLAIM_LETTER" -> {
                if (tierData.state != LicenseProgressionState.INSPECTION_PASSED
                        && tierData.state != LicenseProgressionState.INSPECTION_FAILED
                        && tierData.state != LicenseProgressionState.INSPECTION_COMPLETED
                        && tierData.state != LicenseProgressionState.LETTER_CLAIMED
                        && tierData.state != LicenseProgressionState.LICENSE_CLAIMED
                        && tierData.state != LicenseProgressionState.BANNER_CLAIMED
                        && tierData.state != LicenseProgressionState.LICENSE_UNLOCKED) {
                    return;
                }
                if (!tierData.letterClaimed) {
                    String zooName = player.getName().getString() + "'s Zoo";
                    var stack = InspectionLetterItem.createLetterStack(
                            zooName,
                            tierLevel,
                            tierData.inspectionPassed,
                            tierData.inspectedSpecies,
                            tierData.improvementNotes
                    );
                    if (!player.getInventory().add(stack)) {
                        player.drop(stack, false);
                    }
                    tierData.letterClaimed = true;
                    if (tierData.inspectionPassed && !tierData.licenseClaimed) {
                        tierData.state = LicenseProgressionState.LETTER_CLAIMED;
                    }
                    save(level);
                }
                ZooNetworkPayloads.sendLicenseSync(player);
                if (inspector != null) {
                    openInspectorDialog(player, inspector);
                }
            }
            case "CLAIM_LICENSE" -> {
                if (!tierData.inspectionPassed) {
                    return;
                }
                if (!tierData.licenseClaimed && !tierData.bannerClaimed) {
                    String zooName = player.getName().getString() + "'s Zoo";
                    var bannerStack = LicenseBannerItem.createStackForTier(tierLevel, zooName);
                    if (!player.getInventory().add(bannerStack)) {
                        player.drop(bannerStack, false);
                    }

                    // F12: Also award Animal Information Banner
                    String awardedSpecies = getFeaturedSpeciesForTier(tierLevel, tierData.inspectedSpecies);
                    var infoBannerStack = LicenseBannerItem.createAnimalInfoBannerStack(tierLevel, awardedSpecies, zooName);
                    if (!player.getInventory().add(infoBannerStack)) {
                        player.drop(infoBannerStack, false);
                    }

                    tierData.licenseClaimed = true;
                    tierData.bannerClaimed = true;
                    tierData.state = LicenseProgressionState.LICENSE_UNLOCKED;
                    net.zootycoon.economy.TransactionHelper.giveEmeralds(player, 500);
                    net.zootycoon.advancement.ZooAdvancements.grantForLicenseTier(player, tierLevel);

                    // Unlock next tier
                    LicenseTier currentTier = LicenseTier.byLevel(tierLevel);
                    LicenseTier nextTier = currentTier.nextTier();
                    if (nextTier != null) {
                        TierProgressData nextData = profile.getTierData(nextTier.getTierLevel());
                        nextData.licenseUnlocked = true;
                        if (nextData.state == LicenseProgressionState.LOCKED) {
                            nextData.state = LicenseProgressionState.COLLECTING;
                        }
                        profile.activeTierLevel = nextTier.getTierLevel();
                        recalculateActiveTierEligibility(profile);
                    }

                    save(level);
                    ZooNetworkPayloads.sendToast(
                            player,
                            "zootycoon.toast.title.inspector",
                            "zootycoon.license.toast.license_unlocked",
                            ZooNetworkPayloads.TOAST_SUCCESS,
                            currentTier.getDisplayName()
                    );
                }
                ZooNetworkPayloads.sendLicenseSync(player);
                if (inspector != null) {
                    openInspectorDialog(player, inspector);
                }
            }
            case "THANK_YOU_FINISH" -> {
                // Must have claimed letter, and if passed must have claimed license too
                if (!tierData.letterClaimed) {
                    return;
                }
                if (tierData.inspectionPassed && !tierData.licenseClaimed) {
                    return;
                }
                if (!tierData.inspectionPassed) {
                    recalculateActiveTierEligibility(profile);
                }
                if (inspector != null) {
                    inspector.startReturnAndDespawn();
                }
                save(level);
                ZooNetworkPayloads.sendLicenseSync(player);
            }
        }
    }

    public void recordInspectionCompletion(
            ServerLevel level,
            UUID ownerUuid,
            int tierLevel,
            boolean passed,
            List<String> inspectedSpecies,
            List<String> issues
    ) {
        if (ownerUuid == null) return;
        PlayerLicenseProfile profile = getProfile(ownerUuid);
        TierProgressData tierData = profile.getTierData(tierLevel);
        tierData.state = LicenseProgressionState.INSPECTION_COMPLETED;
        tierData.inspectedSpecies.clear();
        tierData.inspectedSpecies.addAll(inspectedSpecies);
        tierData.improvementNotes.clear();
        tierData.improvementNotes.addAll(issues);

        tierData.letterClaimed = false;
        if (passed) {
            tierData.inspectionPassed = true;
            tierData.state = LicenseProgressionState.INSPECTION_PASSED;
        } else {
            tierData.inspectionPassed = false;
            tierData.state = LicenseProgressionState.INSPECTION_FAILED;
        }

        save(level);
        ServerPlayer sp = level.getServer().getPlayerList().getPlayer(ownerUuid);
        if (sp != null) {
            ZooNetworkPayloads.sendLicenseSync(sp);
        }
    }

    public void clearActiveInspector(UUID ownerUuid, UUID inspectorUuid) {
        if (ownerUuid == null) return;
        PlayerLicenseProfile profile = getProfile(ownerUuid);
        if (profile.activeInspectorUuid == null || profile.activeInspectorUuid.equals(inspectorUuid)) {
            profile.activeInspectorUuid = null;
        }
    }

    private ZooInspectorEntity findActiveInspector(ServerLevel level, UUID ownerUuid, UUID recordedUuid) {
        if (recordedUuid != null) {
            Entity e = level.getEntity(recordedUuid);
            if (e instanceof ZooInspectorEntity ins && ins.isAlive()) {
                return ins;
            }
        }
        for (Entity e : level.getAllEntities()) {
            if (e instanceof ZooInspectorEntity ins && ins.isAlive() && ownerUuid.equals(ins.getOwnerUuid())) {
                return ins;
            }
        }
        return null;
    }

    private ZooCashierEntity findTicketCashierInZoo(ServerLevel level, List<ZooClaim> claims) {
        if (claims == null || claims.isEmpty()) return null;
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
        for (ZooClaim c : claims) {
            var b = c.getBox();
            minX = Math.min(minX, b.minX());
            maxX = Math.max(maxX, b.maxX());
            minZ = Math.min(minZ, b.minZ());
            maxZ = Math.max(maxZ, b.maxZ());
        }
        AABB zooArea = new AABB(minX - 16, level.getMinBuildHeight(), minZ - 16, maxX + 16, level.getMaxBuildHeight(), maxZ + 16);
        List<ZooCashierEntity> cashiers = level.getEntitiesOfClass(ZooCashierEntity.class, zooArea, Entity::isAlive);
        for (ZooCashierEntity c : cashiers) {
            if ("TICKET".equalsIgnoreCase(c.getCashierRole())) {
                return c;
            }
        }
        return null;
    }

    /**
     * Finds a clear spawn position ~5 blocks away from the ticket booth queue / gate area,
     * with a 5-block clear space around it away from visitor queues and obstacles.
     */
    private BlockPos findInspectorGateSpawnPos(ServerLevel level, ServerPlayer player, ZooCashierEntity ticketCashier) {
        if (ticketCashier != null) {
            BlockPos cPos = ticketCashier.blockPosition();
            Direction facing = ticketCashier.getFacingDirection();
            Direction side = facing.getClockWise();

            // Position 5 blocks laterally from the queue line and 4-6 blocks in front of the gate
            int[] forwardOffsets = {4, 5, 6, 3, 7};
            int[] lateralOffsets = {5, -5, 6, -6, 7, -7};

            for (int lat : lateralOffsets) {
                for (int fwd : forwardOffsets) {
                    BlockPos base = cPos.relative(facing, fwd).relative(side, lat);
                    for (int dy : new int[]{0, 1, -1, 2, -2, 3, -3}) {
                        BlockPos candidate = base.offset(0, dy, 0);
                        if (ZooInspectorEntity.isSafeWalkableSpot(level, candidate) && isClearOfVisitorQueue(level, candidate)) {
                            return candidate;
                        }
                    }
                }
            }

            // Fallback: any safe spot 5-8 blocks around cashier
            for (int r = 5; r <= 8; r++) {
                for (Direction dir : Direction.Plane.HORIZONTAL) {
                    BlockPos candidate = cPos.relative(dir, r);
                    for (int dy : new int[]{0, 1, -1, 2, -2}) {
                        BlockPos check = candidate.offset(0, dy, 0);
                        if (ZooInspectorEntity.isSafeWalkableSpot(level, check)) {
                            return check;
                        }
                    }
                }
            }
        }

        return findInspectorCreativeSpawnPos(level, player);
    }

    /**
     * Spawns the Inspector directly in front of the player in Creative mode (or when no ticket gate exists),
     * searching vertically across a wider Y range so hovering players still spawn the Inspector on solid ground.
     */
    private BlockPos findInspectorCreativeSpawnPos(ServerLevel level, ServerPlayer player) {
        BlockPos pPos = player.blockPosition();
        Direction lookDir = player.getDirection();
        int[] dyOffsets = {0, -1, 1, -2, 2, -3, 3, -4, 4, -5, 5, -6, 6};
        for (int dist = 3; dist <= 6; dist++) {
            BlockPos candidate = pPos.relative(lookDir, dist);
            for (int dy : dyOffsets) {
                BlockPos check = candidate.offset(0, dy, 0);
                if (ZooInspectorEntity.isSafeWalkableSpot(level, check)) {
                    return check;
                }
            }
        }
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            for (int dist = 2; dist <= 4; dist++) {
                BlockPos candidate = pPos.relative(dir, dist);
                for (int dy : dyOffsets) {
                    BlockPos check = candidate.offset(0, dy, 0);
                    if (ZooInspectorEntity.isSafeWalkableSpot(level, check)) {
                        return check;
                    }
                }
            }
        }
        return pPos;
    }

    private boolean isClearOfVisitorQueue(ServerLevel level, BlockPos pos) {
        AABB checkBox = new AABB(pos).inflate(3.5D, 2.0D, 3.5D);
        List<ZooVisitorEntity> nearbyVisitors = level.getEntitiesOfClass(ZooVisitorEntity.class, checkBox, Entity::isAlive);
        return nearbyVisitors.isEmpty();
    }

    // =========================================================================
    // Client-side Cache Methods
    // =========================================================================

    public static void updateClientCache(int activeTier, List<String> ownedSpecies, CompoundTag tiersNbt) {
        clientActiveTierLevel = Math.clamp(activeTier, 1, 5);
        clientOwnedSpeciesKeys.clear();
        if (ownedSpecies != null) {
            clientOwnedSpeciesKeys.addAll(ownedSpecies);
        }
        if (tiersNbt != null) {
            for (LicenseTier t : LicenseTier.values()) {
                String k = String.valueOf(t.getTierLevel());
                if (tiersNbt.contains(k, Tag.TAG_COMPOUND)) {
                    clientTierMap.computeIfAbsent(t.getTierLevel(), TierProgressData::new)
                            .fromNbt(tiersNbt.getCompound(k), t.getTierLevel());
                }
            }
        }
    }

    public static int getClientActiveTierLevel() {
        return clientActiveTierLevel;
    }

    public static boolean isClientSpeciesOwned(String speciesKey) {
        return speciesKey != null && clientOwnedSpeciesKeys.contains(speciesKey);
    }

    public static boolean isClientTierUnlocked(int tierLevel) {
        if (tierLevel <= 1) return true;
        TierProgressData d = clientTierMap.get(tierLevel);
        return d != null && d.licenseUnlocked;
    }

    public static TierProgressData getClientTierData(int tierLevel) {
        return clientTierMap.computeIfAbsent(tierLevel, TierProgressData::new);
    }

    public static int getClientMissingCountForActiveTier() {
        LicenseTier tier = LicenseTier.byLevel(clientActiveTierLevel);
        int missing = 0;
        for (LicenseTier.SpeciesEntry entry : tier.getRequiredSpecies()) {
            if (!clientOwnedSpeciesKeys.contains(entry.speciesKey())) {
                missing++;
            }
        }
        return missing;
    }

    public static String getFeaturedSpeciesForTier(int tierLevel, List<String> inspectedNames) {
        LicenseTier tier = LicenseTier.byLevel(tierLevel);
        if (inspectedNames != null && !inspectedNames.isEmpty()) {
            for (LicenseTier.SpeciesEntry entry : tier.getRequiredSpecies()) {
                for (String inspectedName : inspectedNames) {
                    if (entry.getDisplayName().equalsIgnoreCase(inspectedName) || entry.speciesKey().equalsIgnoreCase(inspectedName)) {
                        return entry.speciesKey();
                    }
                }
            }
        }
        return switch (tierLevel) {
            case 1 -> "capybara";
            case 2 -> "zebra";
            case 3 -> "elephant";
            case 4 -> "lion";
            default -> "orca";
        };
    }
}

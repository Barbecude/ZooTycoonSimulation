package net.zootycoon.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.zootycoon.animal.ZooAnimalHungerManager;
import net.zootycoon.animal.habitat.HabitatEvaluator;
import net.zootycoon.license.LicenseTier;
import net.zootycoon.license.ZooLicenseManager;
import net.zootycoon.network.ZooNetworkPayloads;
import net.zootycoon.util.ZooTranslations;

import java.util.*;

public class ZooInspectorEntity extends PathfinderMob {

    public enum InspectorPhase {
        ARRIVING_AT_GATE,
        WAITING_CONFIRMATION,
        WALKING_TO_ANIMAL,
        WRITING_NOTES,
        OBSERVING_ANIMAL,
        APPROACHING_PLAYER,
        WAITING_RESULT_CLAIM,
        RETURNING_TO_GATE
    }

    private static final EntityDataAccessor<String> DATA_OWNER_UUID =
            SynchedEntityData.defineId(ZooInspectorEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> DATA_PHASE =
            SynchedEntityData.defineId(ZooInspectorEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_TIER_LEVEL =
            SynchedEntityData.defineId(ZooInspectorEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> DATA_CURRENT_SPECIES =
            SynchedEntityData.defineId(ZooInspectorEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> DATA_INSPECTED_COUNT =
            SynchedEntityData.defineId(ZooInspectorEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_TOTAL_COUNT =
            SynchedEntityData.defineId(ZooInspectorEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ANIM_TICK =
            SynchedEntityData.defineId(ZooInspectorEntity.class, EntityDataSerializers.INT);

    private BlockPos gatePos = BlockPos.ZERO;
    private final List<String> pendingSpeciesKeys = new ArrayList<>();
    private final List<String> inspectedSpeciesDisplayNames = new ArrayList<>();
    private final List<String> improvementNotes = new ArrayList<>();
    private UUID currentTargetAnimalUuid = null;
    private int phaseTimer = 0;
    private int stuckTicks = 0;
    private Vec3 lastProgressPos = Vec3.ZERO;

    public ZooInspectorEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.setPersistenceRequired();
        this.addTag("zootycoon_staff");
        this.setCustomName(Component.literal("Zoo Inspector"));
        this.setCustomNameVisible(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.30D)
                .add(Attributes.FOLLOW_RANGE, 64.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_OWNER_UUID, "");
        builder.define(DATA_PHASE, InspectorPhase.WAITING_CONFIRMATION.ordinal());
        builder.define(DATA_TIER_LEVEL, 1);
        builder.define(DATA_CURRENT_SPECIES, "");
        builder.define(DATA_INSPECTED_COUNT, 0);
        builder.define(DATA_TOTAL_COUNT, 8);
        builder.define(DATA_ANIM_TICK, 0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.isCreativePlayer()) {
            return super.hurt(source, amount);
        }
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(Entity entity) {
        // Avoid colliding or pushing visitors and other NPCs
    }

    public UUID getOwnerUuid() {
        String s = this.entityData.get(DATA_OWNER_UUID);
        if (s == null || s.isEmpty()) return null;
        try {
            return UUID.fromString(s);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public void setOwnerUuid(UUID uuid) {
        this.entityData.set(DATA_OWNER_UUID, uuid != null ? uuid.toString() : "");
    }

    public InspectorPhase getPhase() {
        int ord = this.entityData.get(DATA_PHASE);
        InspectorPhase[] vals = InspectorPhase.values();
        return (ord >= 0 && ord < vals.length) ? vals[ord] : InspectorPhase.WAITING_CONFIRMATION;
    }

    public void setPhase(InspectorPhase phase) {
        this.entityData.set(DATA_PHASE, phase.ordinal());
        this.phaseTimer = 0;
        this.stuckTicks = 0;
        this.lastProgressPos = this.position();
        updateHeldItemsForPhase(phase);
    }

    private void updateHeldItemsForPhase(InspectorPhase phase) {
        if (phase == InspectorPhase.WRITING_NOTES || phase == InspectorPhase.OBSERVING_ANIMAL) {
            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.FEATHER));
            this.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.PAPER));
        } else if (phase == InspectorPhase.WAITING_RESULT_CLAIM) {
            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.PAPER));
            this.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        } else {
            this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            this.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.BOOK));
        }
    }

    public int getTierLevel() {
        return Math.clamp(this.entityData.get(DATA_TIER_LEVEL), 1, 5);
    }

    public void setTierLevel(int tierLevel) {
        this.entityData.set(DATA_TIER_LEVEL, Math.clamp(tierLevel, 1, 5));
    }

    public String getCurrentSpeciesName() {
        return this.entityData.get(DATA_CURRENT_SPECIES);
    }

    public void setCurrentSpeciesName(String name) {
        this.entityData.set(DATA_CURRENT_SPECIES, name != null ? name : "");
    }

    public int getInspectedCount() {
        return this.entityData.get(DATA_INSPECTED_COUNT);
    }

    public void setInspectedCount(int count) {
        this.entityData.set(DATA_INSPECTED_COUNT, count);
    }

    public int getTotalCount() {
        return Math.max(1, this.entityData.get(DATA_TOTAL_COUNT));
    }

    public void setTotalCount(int total) {
        this.entityData.set(DATA_TOTAL_COUNT, Math.max(1, total));
    }

    public int getAnimTick() {
        return this.entityData.get(DATA_ANIM_TICK);
    }

    public BlockPos getGatePos() {
        return gatePos;
    }

    public void setGatePos(BlockPos pos) {
        this.gatePos = pos != null ? pos.immutable() : BlockPos.ZERO;
    }

    public void setupInitialArrival(UUID ownerUuid, int tierLevel, BlockPos gateTarget) {
        setOwnerUuid(ownerUuid);
        setTierLevel(tierLevel);
        setGatePos(gateTarget);
        LicenseTier tier = LicenseTier.byLevel(tierLevel);
        setTotalCount(tier.getRequiredSpecies().size());
        setInspectedCount(0);
        setPhase(InspectorPhase.WAITING_CONFIRMATION);
    }

    public void beginInspection(ServerLevel level) {
        if (getPhase() != InspectorPhase.WAITING_CONFIRMATION && getPhase() != InspectorPhase.ARRIVING_AT_GATE) {
            return;
        }
        LicenseTier tier = LicenseTier.byLevel(getTierLevel());
        pendingSpeciesKeys.clear();
        inspectedSpeciesDisplayNames.clear();
        improvementNotes.clear();
        for (LicenseTier.SpeciesEntry entry : tier.getRequiredSpecies()) {
            pendingSpeciesKeys.add(entry.speciesKey());
        }
        setTotalCount(pendingSpeciesKeys.size());
        setInspectedCount(0);
        advanceToNextSpecies(level);
    }

    public void startReturnAndDespawn() {
        setPhase(InspectorPhase.RETURNING_TO_GATE);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel) this.level();
        phaseTimer++;
        this.entityData.set(DATA_ANIM_TICK, phaseTimer);

        InspectorPhase phase = getPhase();
        switch (phase) {
            case ARRIVING_AT_GATE -> tickArrivingAtGate(serverLevel);
            case WAITING_CONFIRMATION -> tickWaitingConfirmation(serverLevel);
            case WALKING_TO_ANIMAL -> tickWalkingToAnimal(serverLevel);
            case WRITING_NOTES -> tickWritingNotes(serverLevel);
            case OBSERVING_ANIMAL -> tickObservingAnimal(serverLevel);
            case APPROACHING_PLAYER -> tickApproachingPlayer(serverLevel);
            case WAITING_RESULT_CLAIM -> tickWaitingResultClaim(serverLevel);
            case RETURNING_TO_GATE -> tickReturningToGate(serverLevel);
        }
    }

    private void tickArrivingAtGate(ServerLevel level) {
        if (gatePos == null || gatePos.equals(BlockPos.ZERO) || this.blockPosition().distSqr(gatePos) <= 6.25D) {
            this.getNavigation().stop();
            setPhase(InspectorPhase.WAITING_CONFIRMATION);
            ZooLicenseManager.get(level).onInspectorWaitingAtGate(getOwnerUuid());
            return;
        }
        if (phaseTimer % 20 == 1) {
            this.getNavigation().moveTo(gatePos.getX() + 0.5, gatePos.getY(), gatePos.getZ() + 0.5, 1.0D);
        }
        if (phaseTimer > 160) {
            safeTeleportNear(level, gatePos, 2);
            setPhase(InspectorPhase.WAITING_CONFIRMATION);
            ZooLicenseManager.get(level).onInspectorWaitingAtGate(getOwnerUuid());
        }
    }

    private void tickWaitingConfirmation(ServerLevel level) {
        this.getNavigation().stop();
        UUID ownerUuid = getOwnerUuid();
        if (ownerUuid != null) {
            Player player = level.getPlayerByUUID(ownerUuid);
            if (player != null && player.distanceToSqr(this) <= 100.0D) {
                this.getLookControl().setLookAt(player, 30.0F, 30.0F);
            }
        }
    }

    private void advanceToNextSpecies(ServerLevel level) {
        if (pendingSpeciesKeys.isEmpty()) {
            // All required species in this tier have been inspected!
            boolean passed = improvementNotes.isEmpty();
            ZooLicenseManager.get(level).recordInspectionCompletion(
                    level,
                    getOwnerUuid(),
                    getTierLevel(),
                    passed,
                    new ArrayList<>(inspectedSpeciesDisplayNames),
                    new ArrayList<>(improvementNotes)
            );
            setCurrentSpeciesName("");
            setPhase(InspectorPhase.APPROACHING_PLAYER);
            return;
        }

        String nextSpeciesKey = pendingSpeciesKeys.get(0);
        LicenseTier.SpeciesEntry entry = LicenseTier.findSpeciesEntry(nextSpeciesKey);
        String displayName = entry != null ? entry.getDisplayName() : nextSpeciesKey;
        setCurrentSpeciesName(displayName);

        LivingEntity targetAnimal = findOwnedAnimalForSpecies(level, getOwnerUuid(), entry);
        if (targetAnimal != null) {
            currentTargetAnimalUuid = targetAnimal.getUUID();
            setPhase(InspectorPhase.WALKING_TO_ANIMAL);
            BlockPos standPos = findInspectionStandPosNearAnimal(level, targetAnimal);
            this.getNavigation().moveTo(standPos.getX() + 0.5, standPos.getY(), standPos.getZ() + 0.5, 1.05D);
        } else {
            // Animal not found in loaded chunks (e.g., Creative mode bypass or missing animal)
            currentTargetAnimalUuid = null;
            setPhase(InspectorPhase.WRITING_NOTES);
        }
    }

    private void tickWalkingToAnimal(ServerLevel level) {
        LivingEntity target = findCurrentTargetEntity(level);
        if (target == null || !target.isAlive()) {
            setPhase(InspectorPhase.WRITING_NOTES);
            return;
        }

        double distSq = this.distanceToSqr(target);
        if (distSq <= 25.0D) { // Within 5 blocks of the animal/enclosure
            this.getNavigation().stop();
            this.getLookControl().setLookAt(target, 30.0F, 30.0F);
            setPhase(InspectorPhase.WRITING_NOTES);
            return;
        }

        if (phaseTimer % 20 == 0) {
            BlockPos standPos = findInspectionStandPosNearAnimal(level, target);
            this.getNavigation().moveTo(standPos.getX() + 0.5, standPos.getY(), standPos.getZ() + 0.5, 1.05D);
        }

        if (this.position().distanceToSqr(lastProgressPos) < 0.08D) {
            stuckTicks++;
        } else {
            stuckTicks = 0;
            lastProgressPos = this.position();
        }

        // Safe fallback if pathfinding gets stuck or enclosure is far
        if (stuckTicks >= 80 || phaseTimer >= 240) {
            BlockPos standPos = findInspectionStandPosNearAnimal(level, target);
            safeTeleportNear(level, standPos, 2);
            this.getNavigation().stop();
            setPhase(InspectorPhase.WRITING_NOTES);
        }
    }

    private void tickWritingNotes(ServerLevel level) {
        this.getNavigation().stop();
        LivingEntity target = findCurrentTargetEntity(level);
        if (target != null) {
            this.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }

        if (phaseTimer % 12 == 0) {
            this.swing(InteractionHand.MAIN_HAND);
            level.playSound(null, this.blockPosition(), SoundEvents.BOOK_PAGE_TURN, SoundSource.NEUTRAL, 0.6F, 1.1F);
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 1.5, this.getZ(), 2, 0.25, 0.25, 0.25, 0.02);
        }

        if (phaseTimer >= 60) { // 3 seconds of writing notes
            setPhase(InspectorPhase.OBSERVING_ANIMAL);
        }
    }

    private void tickObservingAnimal(ServerLevel level) {
        this.getNavigation().stop();
        LivingEntity target = findCurrentTargetEntity(level);
        if (target != null) {
            this.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }

        if (phaseTimer >= 40) { // 2 seconds of observing animal
            evaluateAndRecordCurrentSpecies(level, target);
            if (!pendingSpeciesKeys.isEmpty()) {
                pendingSpeciesKeys.remove(0);
            }
            setInspectedCount(getTotalCount() - pendingSpeciesKeys.size());
            advanceToNextSpecies(level);
        }
    }

    private void evaluateAndRecordCurrentSpecies(ServerLevel level, LivingEntity animal) {
        String speciesName = getCurrentSpeciesName();
        if (speciesName.isEmpty()) {
            speciesName = "Satwa";
        }
        inspectedSpeciesDisplayNames.add(speciesName);

        UUID ownerUuid = getOwnerUuid();
        ServerPlayer ownerPlayer = ownerUuid != null ? level.getServer().getPlayerList().getPlayer(ownerUuid) : null;
        boolean isCreativeBypass = ownerPlayer != null && ownerPlayer.isCreative();

        if (animal == null || !animal.isAlive()) {
            if (!isCreativeBypass) {
                improvementNotes.add(speciesName + ": " + ZooTranslations.get("zootycoon.license.issue.missing_animal"));
            }
            return;
        }

        int hunger = ZooAnimalHungerManager.getHunger(animal);
        int thirst = ZooAnimalHungerManager.getThirst(animal);
        int clean = ZooAnimalHungerManager.getCleanliness(animal);
        float healthRatio = animal.getMaxHealth() > 0 ? (animal.getHealth() / animal.getMaxHealth()) : 1.0f;

        if (hunger <= 30) {
            improvementNotes.add(speciesName + ": " + ZooTranslations.get("zootycoon.license.issue.hungry", hunger));
        }
        if (thirst <= 30) {
            improvementNotes.add(speciesName + ": " + ZooTranslations.get("zootycoon.license.issue.thirsty", thirst));
        }
        if (!ZooAnimalHungerManager.isPureAquatic(animal) && clean <= 30) {
            improvementNotes.add(speciesName + ": " + ZooTranslations.get("zootycoon.license.issue.dirty", clean));
        }
        if (healthRatio <= 0.30f) {
            improvementNotes.add(speciesName + ": " + ZooTranslations.get("zootycoon.license.issue.low_health"));
        }

        HabitatEvaluator.HabitatResult hab = HabitatEvaluator.evaluate(level, animal);
        if (hab != null && hab.status() == HabitatEvaluator.HabitatStatus.UNSUITABLE) {
            String hint = hab.formatHint();
            if (hint == null || hint.isEmpty()) {
                hint = ZooTranslations.get("zootycoon.license.issue.habitat_generic");
            }
            improvementNotes.add(speciesName + ": " + ZooTranslations.get("zootycoon.license.issue.habitat_detail", hint));
        }
    }

    private void tickApproachingPlayer(ServerLevel level) {
        UUID ownerUuid = getOwnerUuid();
        ServerPlayer player = ownerUuid != null ? level.getServer().getPlayerList().getPlayer(ownerUuid) : null;
        if (player == null || player.level() != level) {
            setPhase(InspectorPhase.WAITING_RESULT_CLAIM);
            return;
        }

        double distSq = this.distanceToSqr(player);
        if (distSq <= 16.0D) { // Within 4 blocks of player
            this.getNavigation().stop();
            this.getLookControl().setLookAt(player, 30.0F, 30.0F);
            setPhase(InspectorPhase.WAITING_RESULT_CLAIM);
            ZooNetworkPayloads.sendToast(
                    player,
                    "zootycoon.toast.title.inspector",
                    "zootycoon.license.toast.inspection_done",
                    ZooNetworkPayloads.TOAST_SUCCESS
            );
            return;
        }

        if (phaseTimer % 20 == 1) {
            this.getNavigation().moveTo(player, 1.1D);
        }

        if (this.position().distanceToSqr(lastProgressPos) < 0.08D) {
            stuckTicks++;
        } else {
            stuckTicks = 0;
            lastProgressPos = this.position();
        }

        if (stuckTicks >= 60 || phaseTimer >= 180) {
            // Teleport fallback ~5 blocks from player in a safe spot outside blocks/cages
            safeTeleportNear(level, player.blockPosition(), 5);
            this.getNavigation().stop();
            this.getLookControl().setLookAt(player, 30.0F, 30.0F);
            setPhase(InspectorPhase.WAITING_RESULT_CLAIM);
            ZooNetworkPayloads.sendToast(
                    player,
                    "zootycoon.toast.title.inspector",
                    "zootycoon.license.toast.inspection_done",
                    ZooNetworkPayloads.TOAST_SUCCESS
            );
        }
    }

    private void tickWaitingResultClaim(ServerLevel level) {
        this.getNavigation().stop();
        UUID ownerUuid = getOwnerUuid();
        if (ownerUuid != null) {
            Player player = level.getPlayerByUUID(ownerUuid);
            if (player != null && player.distanceToSqr(this) <= 100.0D) {
                this.getLookControl().setLookAt(player, 30.0F, 30.0F);
            }
        }
    }

    private void tickReturningToGate(ServerLevel level) {
        if (gatePos == null || gatePos.equals(BlockPos.ZERO) || this.blockPosition().distSqr(gatePos) <= 9.0D) {
            this.getNavigation().stop();
            if (phaseTimer >= 30) {
                ZooLicenseManager.get(level).clearActiveInspector(getOwnerUuid(), this.getUUID());
                this.discard();
            }
            return;
        }

        if (phaseTimer % 20 == 1) {
            this.getNavigation().moveTo(gatePos.getX() + 0.5, gatePos.getY(), gatePos.getZ() + 0.5, 1.05D);
        }

        if (this.position().distanceToSqr(lastProgressPos) < 0.08D) {
            stuckTicks++;
        } else {
            stuckTicks = 0;
            lastProgressPos = this.position();
        }

        if (stuckTicks >= 60 || phaseTimer >= 160) {
            safeTeleportNear(level, gatePos, 2);
            ZooLicenseManager.get(level).clearActiveInspector(getOwnerUuid(), this.getUUID());
            this.discard();
        }
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        if (this.level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }
        UUID owner = getOwnerUuid();
        if (owner != null && !owner.equals(serverPlayer.getUUID())) {
            return InteractionResult.FAIL;
        }

        ZooLicenseManager.get((ServerLevel) this.level()).openInspectorDialog(serverPlayer, this);
        return InteractionResult.SUCCESS;
    }

    private LivingEntity findCurrentTargetEntity(ServerLevel level) {
        if (currentTargetAnimalUuid == null) return null;
        Entity e = level.getEntity(currentTargetAnimalUuid);
        return (e instanceof LivingEntity le && le.isAlive()) ? le : null;
    }

    private LivingEntity findOwnedAnimalForSpecies(ServerLevel level, UUID ownerUuid, LicenseTier.SpeciesEntry entry) {
        if (ownerUuid == null || entry == null) return null;
        LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;
        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof LivingEntity le && le.isAlive()
                    && ZooAnimalHungerManager.isTagged(le)
                    && ZooAnimalHungerManager.isOwnedBy(le, ownerUuid)
                    && entry.matches(le.getType())) {
                double d = this.distanceToSqr(le);
                if (d < bestDist) {
                    bestDist = d;
                    best = le;
                }
            }
        }
        return best;
    }

    private BlockPos findInspectionStandPosNearAnimal(ServerLevel level, LivingEntity animal) {
        BlockPos center = animal.blockPosition();
        for (int radius = 3; radius <= 6; radius++) {
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                BlockPos candidate = center.relative(dir, radius);
                for (int dy : new int[]{0, 1, -1, 2, -2}) {
                    BlockPos check = candidate.offset(0, dy, 0);
                    if (isSafeWalkableSpot(level, check)) {
                        return check;
                    }
                }
            }
        }
        return center;
    }

    public void safeTeleportNear(ServerLevel level, BlockPos targetCenter, int preferredRadius) {
        for (int r = preferredRadius; r <= preferredRadius + 4; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (Math.abs(dx) != r && Math.abs(dz) != r) continue;
                    for (int dy : new int[]{0, 1, -1, 2, -2, 3, -3}) {
                        BlockPos pos = targetCenter.offset(dx, dy, dz);
                        if (isSafeWalkableSpot(level, pos)) {
                            this.teleportTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                            return;
                        }
                    }
                }
            }
        }
        this.teleportTo(targetCenter.getX() + 0.5, targetCenter.getY(), targetCenter.getZ() + 0.5);
    }

    public static boolean isSafeWalkableSpot(ServerLevel level, BlockPos pos) {
        BlockState ground = level.getBlockState(pos.below());
        BlockState feet = level.getBlockState(pos);
        BlockState head = level.getBlockState(pos.above());
        if (!ground.isSolid() || ground.getBlock() instanceof net.minecraft.world.level.block.FenceBlock
                || ground.getBlock() instanceof net.minecraft.world.level.block.WallBlock) {
            return false;
        }
        return feet.getCollisionShape(level, pos).isEmpty()
                && head.getCollisionShape(level, pos.above()).isEmpty()
                && feet.getFluidState().isEmpty();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("OwnerUuid", this.entityData.get(DATA_OWNER_UUID));
        tag.putInt("InspectorPhase", getPhase().ordinal());
        tag.putInt("TierLevel", getTierLevel());
        tag.putString("CurrentSpecies", getCurrentSpeciesName());
        tag.putInt("InspectedCount", getInspectedCount());
        tag.putInt("TotalCount", getTotalCount());
        tag.putLong("GatePos", gatePos.asLong());

        ListTag pendingTag = new ListTag();
        for (String s : pendingSpeciesKeys) pendingTag.add(StringTag.valueOf(s));
        tag.put("PendingSpecies", pendingTag);

        ListTag inspectedTag = new ListTag();
        for (String s : inspectedSpeciesDisplayNames) inspectedTag.add(StringTag.valueOf(s));
        tag.put("InspectedSpecies", inspectedTag);

        ListTag notesTag = new ListTag();
        for (String s : improvementNotes) notesTag.add(StringTag.valueOf(s));
        tag.put("ImprovementNotes", notesTag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("OwnerUuid")) this.entityData.set(DATA_OWNER_UUID, tag.getString("OwnerUuid"));
        if (tag.contains("TierLevel")) setTierLevel(tag.getInt("TierLevel"));
        if (tag.contains("CurrentSpecies")) setCurrentSpeciesName(tag.getString("CurrentSpecies"));
        if (tag.contains("InspectedCount")) setInspectedCount(tag.getInt("InspectedCount"));
        if (tag.contains("TotalCount")) setTotalCount(tag.getInt("TotalCount"));
        if (tag.contains("GatePos")) gatePos = BlockPos.of(tag.getLong("GatePos"));

        pendingSpeciesKeys.clear();
        if (tag.contains("PendingSpecies", Tag.TAG_LIST)) {
            ListTag l = tag.getList("PendingSpecies", Tag.TAG_STRING);
            for (int i = 0; i < l.size(); i++) pendingSpeciesKeys.add(l.getString(i));
        }
        inspectedSpeciesDisplayNames.clear();
        if (tag.contains("InspectedSpecies", Tag.TAG_LIST)) {
            ListTag l = tag.getList("InspectedSpecies", Tag.TAG_STRING);
            for (int i = 0; i < l.size(); i++) inspectedSpeciesDisplayNames.add(l.getString(i));
        }
        improvementNotes.clear();
        if (tag.contains("ImprovementNotes", Tag.TAG_LIST)) {
            ListTag l = tag.getList("ImprovementNotes", Tag.TAG_STRING);
            for (int i = 0; i < l.size(); i++) improvementNotes.add(l.getString(i));
        }
        if (tag.contains("InspectorPhase")) {
            int ord = tag.getInt("InspectorPhase");
            InspectorPhase[] vals = InspectorPhase.values();
            if (ord >= 0 && ord < vals.length) {
                setPhase(vals[ord]);
            }
        }
    }
}

package net.zootycoon.entity;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.nbt.CompoundTag;
import net.zootycoon.ZooTycoonMod;
import net.zootycoon.block.ToiletBlockEntity;

import java.util.EnumSet;
import java.util.List;

public class ZooVisitorEntity extends PathfinderMob {

    // Variants: 0..22 (corresponds to visitor_0.png .. visitor_22.png)
    private static final EntityDataAccessor<Integer> VARIANT =
            SynchedEntityData.defineId(ZooVisitorEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Boolean> IS_CHILD =
            SynchedEntityData.defineId(ZooVisitorEntity.class, EntityDataSerializers.BOOLEAN);

    // Needs (0..100)
    private static final EntityDataAccessor<Integer> HUNGER =
            SynchedEntityData.defineId(ZooVisitorEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Integer> THIRST =
            SynchedEntityData.defineId(ZooVisitorEntity.class, EntityDataSerializers.INT);

    // Mood
    private static final EntityDataAccessor<Integer> MOOD =
            SynchedEntityData.defineId(ZooVisitorEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Boolean> HAS_TRASH =
            SynchedEntityData.defineId(ZooVisitorEntity.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<Boolean> NEEDS_TOILET =
            SynchedEntityData.defineId(ZooVisitorEntity.class, EntityDataSerializers.BOOLEAN);

    public static final int MAX_VARIANTS = 23;
    private static final int MAX_STAY = 2400; // 2 minutes (2400 ticks)

    public enum VisitorMood {
        NEUTRAL("zootycoon.visitor.mood.neutral", 0xFFFFFF),
        TOILET("zootycoon.visitor.mood.toilet", 0xFFA500),
        AMAZED("zootycoon.visitor.mood.amazed", 0x00FFFF),
        ADORED("zootycoon.visitor.mood.adored", 0xFF69B4),
        HUNGRY("zootycoon.visitor.mood.hungry", 0xFF0000),
        THIRSTY("zootycoon.visitor.mood.thirsty", 0x0000FF);

        public final String labelKey;
        public final int color;

        VisitorMood(String labelKey, int color) {
            this.labelKey = labelKey;
            this.color = color;
        }

        public String getLabel() {
            return net.zootycoon.util.ZooTranslations.get(this.labelKey);
        }

        public static VisitorMood fromOrdinal(int ordinal) {
            VisitorMood[] values = values();
            if (ordinal < 0 || ordinal >= values.length) {
                return NEUTRAL;
            }
            return values[ordinal];
        }
    }

    private BlockPos gatePos = null;
    private int stayTime = 0;
    private int toiletTimer = 0;
    private int watchingAnimalId = -1;
    // 0 = small/baby, 1 = normal, 2 = big
    private int watchingAnimalSize = -1;
    private int eatTimer = 0;

    public ZooVisitorEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(VARIANT, (int) (Math.random() * MAX_VARIANTS));
        builder.define(IS_CHILD, Math.random() < 0.20); // 20% child chance
        builder.define(HUNGER, 0);
        builder.define(THIRST, 0);
        builder.define(MOOD, VisitorMood.NEUTRAL.ordinal());
        builder.define(HAS_TRASH, false);
        builder.define(NEEDS_TOILET, false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new LeaveZooGoal(this));
        this.goalSelector.addGoal(2, new FindToiletGoal(this));
        this.goalSelector.addGoal(2, new FindTrashCanGoal(this));
        this.goalSelector.addGoal(3, new OpenDoorGoal(this, true));
        this.goalSelector.addGoal(4, new WatchAnimalsGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.7D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    public VisitorMood getMood() {
        return VisitorMood.fromOrdinal(this.entityData.get(MOOD));
    }

    public void setMood(VisitorMood mood) {
        this.entityData.set(MOOD, mood.ordinal());
    }

    public int getVariant() {
        return this.entityData.get(VARIANT);
    }

    public void setVariant(int variant) {
        this.entityData.set(VARIANT, Math.max(0, Math.min(MAX_VARIANTS - 1, variant)));
    }

    public boolean isChildVisitor() {
        return this.entityData.get(IS_CHILD);
    }

    public void setChildVisitor(boolean child) {
        this.entityData.set(IS_CHILD, child);
    }

    public int getHunger() {
        return this.entityData.get(HUNGER);
    }

    public void setHunger(int hunger) {
        this.entityData.set(HUNGER, Math.clamp(hunger, 0, 100));
    }

    public int getThirst() {
        return this.entityData.get(THIRST);
    }

    public void setThirst(int thirst) {
        this.entityData.set(THIRST, Math.clamp(thirst, 0, 100));
    }

    public boolean hasTrash() {
        return this.entityData.get(HAS_TRASH);
    }

    public void setHasTrash(boolean trash) {
        this.entityData.set(HAS_TRASH, trash);
    }

    public boolean needsToilet() {
        return this.entityData.get(NEEDS_TOILET);
    }

    public void setNeedsToilet(boolean needsToilet) {
        this.entityData.set(NEEDS_TOILET, needsToilet);
    }

    public void setGatePos(BlockPos pos) {
        this.gatePos = pos;
    }

    public BlockPos getGatePos() {
        return gatePos;
    }

    public boolean isTimeToLeave() {
        return stayTime > MAX_STAY;
    }

    /**
     * Authoritative mood updater based on priority:
     * TOILET > HUNGRY (>=50) > THIRSTY (>=50) > Animal Watching (AMAZED / ADORED / HAPPY) > NEUTRAL
     */
    public void updateMood() {
        if (needsToilet()) {
            setMood(VisitorMood.TOILET);
            return;
        }
        if (getHunger() >= 50) {
            setMood(VisitorMood.HUNGRY);
            return;
        }
        if (getThirst() >= 50) {
            setMood(VisitorMood.THIRSTY);
            return;
        }
        if (watchingAnimalId != -1) {
            if (watchingAnimalSize == 2) {
                setMood(VisitorMood.AMAZED);
            } else if (watchingAnimalSize == 0) {
                setMood(VisitorMood.ADORED);
            } else {
                setMood(VisitorMood.NEUTRAL);
            }
            return;
        }
        setMood(VisitorMood.NEUTRAL);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide()) {
            stayTime++;

            // Every 10 seconds (200 ticks): hunger and thirst build up
            if (this.tickCount % 200 == 0) {
                setHunger(getHunger() + 7);
                setThirst(getThirst() + 10);
            }

            // Every 20 seconds (400 ticks): small chance to need toilet
            if (this.tickCount % 400 == 0 && !needsToilet()) {
                if (this.random.nextFloat() < 0.035F) {
                    setNeedsToilet(true);
                }
            }

            // Toilet timer check: if toilet need isn't met for 30s (600 ticks), visitor leaves
            if (needsToilet()) {
                toiletTimer++;
                if (toiletTimer > 600) {
                    forceLeave();
                }
            } else {
                toiletTimer = 0;
            }

            // Occasional trash generation
            if (this.random.nextFloat() < 0.0003F && !hasTrash()) {
                setHasTrash(true);
            }

            // Eating / drinking finish logic
            if (eatTimer > 0) {
                eatTimer--;
                if (eatTimer == 0) {
                    this.setItemInHand(InteractionHand.OFF_HAND, net.minecraft.world.item.ItemStack.EMPTY);
                    this.playSound(SoundEvents.GENERIC_EAT, 1.0F, 1.0F);
                }
            }

            // Update mood every 5 ticks
            if (this.tickCount % 5 == 0) {
                updateMood();
            }
        }
    }

    public void forceLeave() {
        if (gatePos != null) {
            this.getNavigation().stop();
            this.getNavigation().moveTo(gatePos.getX() + 0.5, gatePos.getY(), gatePos.getZ() + 0.5, 1.2D);
            if (this.distanceToSqr(gatePos.getX() + 0.5, gatePos.getY(), gatePos.getZ() + 0.5) < 9.0D) {
                this.discard();
            }
        } else {
            this.discard();
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.level().isClientSide() && hand == InteractionHand.MAIN_HAND && player instanceof ServerPlayer sPlayer) {
            VisitorMood currentMood = getMood();
            net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(sPlayer, new net.zootycoon.network.ZooNetworkPayloads.OpenVisitorScreenPayload(
                    this.getId(),
                    this.getUUID(),
                    getVariant(),
                    isChildVisitor(),
                    currentMood.name(),
                    currentMood.getLabel(),
                    getHunger(),
                    getThirst(),
                    needsToilet(),
                    hasTrash()
            ));
            this.playSound(SoundEvents.VILLAGER_YES, 1.0F, 1.0F);
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (gatePos != null) {
            tag.putInt("GateX", gatePos.getX());
            tag.putInt("GateY", gatePos.getY());
            tag.putInt("GateZ", gatePos.getZ());
        }
        tag.putInt("StayTime", stayTime);
        tag.putInt("Variant", getVariant());
        tag.putBoolean("IsChild", isChildVisitor());
        tag.putInt("Hunger", getHunger());
        tag.putInt("Thirst", getThirst());
        tag.putInt("Mood", getMood().ordinal());
        tag.putBoolean("HasTrash", hasTrash());
        tag.putBoolean("NeedsToilet", needsToilet());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("GateX") && tag.contains("GateY") && tag.contains("GateZ")) {
            gatePos = new BlockPos(
                    tag.getInt("GateX"),
                    tag.getInt("GateY"),
                    tag.getInt("GateZ")
            );
        }
        stayTime = tag.getInt("StayTime");
        setVariant(tag.getInt("Variant"));
        setChildVisitor(tag.getBoolean("IsChild"));
        setHunger(tag.getInt("Hunger"));
        setThirst(tag.getInt("Thirst"));
        setMood(VisitorMood.fromOrdinal(tag.contains("Mood") ? tag.getInt("Mood") : VisitorMood.NEUTRAL.ordinal()));
        setHasTrash(tag.getBoolean("HasTrash"));
        setNeedsToilet(tag.getBoolean("NeedsToilet"));
    }

    // =========================================================================
    // GOALS
    // =========================================================================

    /**
     * Goal for watching animals in the zoo.
     * Searches for nearby living Animal entities, approaches them, and observes them.
     */
    public static class WatchAnimalsGoal extends Goal {
        private final ZooVisitorEntity visitor;
        private Animal targetAnimal;
        private int watchTimer;
        private BlockPos watchPos;

        public WatchAnimalsGoal(ZooVisitorEntity visitor) {
            this.visitor = visitor;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (visitor.isTimeToLeave()) return false;
            if (visitor.tickCount % 40 != 0) return false;

            // Search for nearby animals within 24 blocks
            List<Animal> nearbyAnimals = visitor.level().getEntitiesOfClass(
                    Animal.class,
                    visitor.getBoundingBox().inflate(24.0D),
                    a -> a.isAlive()
            );

            if (nearbyAnimals.isEmpty()) return false;

            // Pick nearest animal
            Animal best = null;
            double bestDist = Double.MAX_VALUE;
            for (Animal a : nearbyAnimals) {
                double d = visitor.distanceToSqr(a);
                if (d < bestDist) {
                    bestDist = d;
                    best = a;
                }
            }

            if (best != null) {
                this.targetAnimal = best;
                return true;
            }
            return false;
        }

        @Override
        public void start() {
            int base = 140 + visitor.random.nextInt(100); // ~7-12 seconds
            watchTimer = base;
            if (targetAnimal != null) {
                visitor.watchingAnimalId = targetAnimal.getId();
                visitor.watchingAnimalSize = determineAnimalSize(targetAnimal);
                watchPos = computeWatchPos(targetAnimal);
                visitor.updateMood();
            }
        }

        @Override
        public void stop() {
            visitor.watchingAnimalId = -1;
            visitor.watchingAnimalSize = -1;
            targetAnimal = null;
            watchPos = null;
            visitor.updateMood();
        }

        @Override
        public void tick() {
            if (targetAnimal == null || !targetAnimal.isAlive()) {
                watchTimer = 0;
                return;
            }

            if (watchPos == null || visitor.distanceToSqr(watchPos.getX() + 0.5, watchPos.getY(), watchPos.getZ() + 0.5) < 2.0D) {
                watchPos = computeWatchPos(targetAnimal);
            }

            if (watchPos != null) {
                double dist = visitor.distanceToSqr(watchPos.getX() + 0.5, watchPos.getY(), watchPos.getZ() + 0.5);
                if (dist > 4.0D || visitor.getNavigation().isDone()) {
                    visitor.getNavigation().moveTo(watchPos.getX() + 0.5, watchPos.getY(), watchPos.getZ() + 0.5, 1.0D);
                }
            }

            visitor.getLookControl().setLookAt(targetAnimal, 30.0F, 30.0F);
            visitor.watchingAnimalSize = determineAnimalSize(targetAnimal);

            if (watchTimer > 0) {
                watchTimer--;
            }
        }

        @Override
        public boolean canContinueToUse() {
            return watchTimer > 0 && targetAnimal != null && targetAnimal.isAlive();
        }

        private int determineAnimalSize(Animal animal) {
            if (animal.isBaby() || animal.getBbWidth() < 0.8F) {
                return 0; // Small / baby (ADORED / Gemas)
            }
            if (animal.getBbWidth() > 1.4F || animal.getBbHeight() > 1.8F) {
                return 2; // Big / imposing (AMAZED / Takjub)
            }
            return 1; // Normal (NEUTRAL / Biasa)
        }

        private BlockPos computeWatchPos(Animal target) {
            double radius = 4.0D;
            double angle = ((visitor.getId() * 37) + (target.getId() * 19)) % 360;
            double rad = Math.toRadians(angle);
            int dx = (int) Math.round(Math.cos(rad) * radius);
            int dz = (int) Math.round(Math.sin(rad) * radius);
            return target.blockPosition().offset(dx, 0, dz);
        }
    }

    /**
     * Goal for leaving the zoo once stay time is over or visitor is forced to leave.
     */
    public static class LeaveZooGoal extends Goal {
        private final ZooVisitorEntity visitor;

        public LeaveZooGoal(ZooVisitorEntity visitor) {
            this.visitor = visitor;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return visitor.isTimeToLeave() && visitor.getGatePos() != null;
        }

        @Override
        public void start() {
            BlockPos gate = visitor.getGatePos();
            if (gate != null) {
                visitor.getNavigation().moveTo(gate.getX() + 0.5, gate.getY(), gate.getZ() + 0.5, 1.0D);
            }
        }

        @Override
        public void tick() {
            BlockPos gate = visitor.getGatePos();
            if (gate == null) return;

            if (visitor.getNavigation().isDone() || visitor.tickCount % 20 == 0) {
                visitor.getNavigation().moveTo(gate.getX() + 0.5, gate.getY(), gate.getZ() + 0.5, 1.0D);
            }

            if (visitor.distanceToSqr(gate.getX() + 0.5, gate.getY(), gate.getZ() + 0.5) < 9.0D) {
                visitor.discard();
            }
        }
    }

    /**
     * Goal for finding a trash can to dispose of trash, or dropping it on the ground (littering) if none found.
     */
    public static class FindTrashCanGoal extends Goal {
        private final ZooVisitorEntity visitor;
        private BlockPos targetPos;
        private int timer;
        private int searchTimer;

        public FindTrashCanGoal(ZooVisitorEntity visitor) {
            this.visitor = visitor;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (!visitor.hasTrash()) return false;

            BlockPos p = visitor.blockPosition();
            for (BlockPos pos : BlockPos.betweenClosed(p.offset(-16, -3, -16), p.offset(16, 3, 16))) {
                if (visitor.level().getBlockState(pos).is(net.zootycoon.block.ZooBlocks.TRASH_CAN)) {
                    this.targetPos = pos.immutable();
                    this.searchTimer = 0;
                    return true;
                }
            }

            this.searchTimer++;
            if (this.searchTimer > 300) { // ~15 seconds without finding trash can
                this.targetPos = null;
                return true; // triggers littering
            }

            return false;
        }

        @Override
        public void start() {
            if (targetPos != null) {
                visitor.getNavigation().moveTo(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5, 1.0D);
            }
            timer = 0;
        }

        @Override
        public void tick() {
            if (targetPos != null) {
                double distSq = visitor.distanceToSqr(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5);
                if (distSq < 4.0D) {
                    visitor.getNavigation().stop();
                    timer++;

                    if (timer == 2) {
                        net.minecraft.world.level.block.state.BlockState state = visitor.level().getBlockState(targetPos);
                        if (state.is(net.zootycoon.block.ZooBlocks.TRASH_CAN)) {
                            visitor.level().setBlockAndUpdate(targetPos, state.setValue(net.zootycoon.block.TrashCanBlock.OPEN, true));
                            visitor.playSound(SoundEvents.CHEST_OPEN, 0.5F, 0.8F);
                        }
                    }

                    if (timer > 20) {
                        visitor.setHasTrash(false);
                        visitor.playSound(SoundEvents.ITEM_PICKUP, 1.0F, 1.0F);

                        net.minecraft.world.level.block.state.BlockState state = visitor.level().getBlockState(targetPos);
                        if (state.is(net.zootycoon.block.ZooBlocks.TRASH_CAN)) {
                            visitor.level().setBlockAndUpdate(targetPos, state.setValue(net.zootycoon.block.TrashCanBlock.OPEN, false));
                            visitor.playSound(SoundEvents.CHEST_CLOSE, 0.5F, 0.8F);
                        }

                        targetPos = null;
                        searchTimer = 0;
                    }
                } else if (visitor.getNavigation().isDone() || visitor.tickCount % 20 == 0) {
                    visitor.getNavigation().moveTo(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5, 1.0D);
                }
            } else {
                // Littering on ground
                BlockPos feet = visitor.blockPosition();
                if (visitor.level().isEmptyBlock(feet)) {
                    visitor.level().setBlockAndUpdate(feet, net.zootycoon.block.ZooBlocks.TRASH.defaultBlockState()
                            .setValue(net.zootycoon.block.TrashBlock.VARIANT, visitor.getRandom().nextInt(4)));
                    visitor.setHasTrash(false);
                    this.searchTimer = 0;
                    visitor.playSound(SoundEvents.CHICKEN_STEP, 1.0F, 0.8F);
                }
            }
        }

        @Override
        public boolean canContinueToUse() {
            if (targetPos == null && visitor.hasTrash()) return true;
            return targetPos != null && visitor.hasTrash() && visitor.level().getBlockState(targetPos).is(net.zootycoon.block.ZooBlocks.TRASH_CAN);
        }
    }

    /**
     * Goal for finding a toilet, queuing if occupied, using the toilet, and flushing.
     */
    public static class FindToiletGoal extends Goal {
        private final ZooVisitorEntity visitor;
        private BlockPos targetPos;
        private BlockPos doorPos;
        private Stage stage;
        private int timer;
        private int usingTicks;
        private int queueTicks;
        private int repathCooldown;

        private enum Stage {
            TO_DOOR,
            TO_TARGET,
            USING,
            QUEUING
        }

        public FindToiletGoal(ZooVisitorEntity visitor) {
            this.visitor = visitor;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (!visitor.needsToilet()) return false;

            BlockPos p = visitor.blockPosition();
            BlockPos bestPos = null;
            int bestQueue = Integer.MAX_VALUE;

            for (BlockPos pos : BlockPos.betweenClosed(p.offset(-32, -5, -32), p.offset(32, 5, 32))) {
                if (!visitor.level().getBlockState(pos).is(net.zootycoon.block.ZooBlocks.TOILET)) continue;
                int queueLen = 0;
                if (visitor.level().getBlockEntity(pos) instanceof ToiletBlockEntity toilet) {
                    if (toilet.isVisitorOccupant(visitor.getUUID()) || toilet.isInVisitorQueue(visitor.getUUID())) {
                        this.targetPos = pos.immutable();
                        this.doorPos = findNearestDoorNear(this.targetPos, 5);
                        this.stage = this.doorPos != null ? Stage.TO_DOOR : Stage.TO_TARGET;
                        return true;
                    }
                    queueLen = toilet.getQueueSize();
                }
                if (queueLen < bestQueue) {
                    bestQueue = queueLen;
                    bestPos = pos.immutable();
                }
            }

            if (bestPos != null) {
                this.targetPos = bestPos;
                this.doorPos = findNearestDoorNear(this.targetPos, 5);
                this.stage = this.doorPos != null ? Stage.TO_DOOR : Stage.TO_TARGET;
                return true;
            }

            return false;
        }

        @Override
        public void start() {
            timer = 0;
            usingTicks = 0;
            queueTicks = 0;
            repathCooldown = 0;
            if (stage == Stage.TO_DOOR && doorPos != null) {
                moveTo(doorPos);
            } else if (targetPos != null) {
                moveTo(targetPos);
            }
        }

        @Override
        public void stop() {
            if ((stage == Stage.QUEUING || stage == Stage.USING) && targetPos != null && !visitor.level().isClientSide()) {
                if (visitor.level().getBlockEntity(targetPos) instanceof ToiletBlockEntity toilet) {
                    toilet.releaseVisitorOccupant(visitor.getUUID());
                }
            }
            if (visitor.isPassenger()) {
                visitor.stopRiding();
            }
            if (visitor.getPose() == net.minecraft.world.entity.Pose.CROUCHING) {
                visitor.setPose(net.minecraft.world.entity.Pose.STANDING);
            }
            targetPos = null;
            doorPos = null;
            stage = null;
            usingTicks = 0;
            queueTicks = 0;
        }

        @Override
        public void tick() {
            if (targetPos == null) return;

            if (stage == Stage.TO_DOOR && doorPos != null) {
                if (visitor.distanceToSqr(doorPos.getX() + 0.5, doorPos.getY(), doorPos.getZ() + 0.5) < 4.0D) {
                    openDoorAt(doorPos);
                    stage = Stage.TO_TARGET;
                    moveTo(targetPos);
                    timer = 0;
                } else {
                    repathIfNeeded(doorPos);
                }
                return;
            }

            if (stage == Stage.USING) {
                visitor.getNavigation().stop();
                if (usingTicks > 0) {
                    usingTicks--;
                }
                if (usingTicks <= 0) {
                    if (visitor.isPassenger()) {
                        visitor.stopRiding();
                    }
                    visitor.setPose(net.minecraft.world.entity.Pose.STANDING);
                    visitor.toiletTimer = 0;
                    visitor.setNeedsToilet(false);

                    if (targetPos != null && visitor.level() instanceof net.minecraft.server.level.ServerLevel sl) {
                        if (sl.getBlockEntity(targetPos) instanceof ToiletBlockEntity toilet) {
                            toilet.flush(sl);
                            toilet.releaseVisitorOccupant(visitor.getUUID());
                        }
                    }

                    visitor.updateMood();
                    targetPos = null;
                    stage = null;
                }
                return;
            }

            if (stage == Stage.QUEUING) {
                if (visitor.level().isClientSide()) return;
                if (!(visitor.level().getBlockEntity(targetPos) instanceof ToiletBlockEntity toilet)) {
                    targetPos = null; stage = null; return;
                }

                if (toilet.isVisitorOccupant(visitor.getUUID())) {
                    if (doorPos != null) openDoorAt(doorPos);
                    double distSq = visitor.distanceToSqr(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5);
                    if (distSq > 3.0D) {
                        repathIfNeeded(targetPos);
                    } else {
                        visitor.getNavigation().stop();
                        stage = Stage.USING;
                        usingTicks = 80;
                        if (visitor.level() instanceof net.minecraft.server.level.ServerLevel sl) {
                            toilet.seatVisitor(sl, visitor);
                        }
                    }
                    return;
                }

                queueTicks++;
                repathIfNeeded(targetPos);
                if (queueTicks > 200) { // 10s timeout
                    toilet.releaseVisitorOccupant(visitor.getUUID());
                    visitor.toiletTimer = 0;
                    visitor.setNeedsToilet(false);
                    targetPos = null;
                    stage = null;
                }
                return;
            }

            // Moving toward target toilet
            if (visitor.distanceToSqr(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5) < 5.0D) {
                if (doorPos != null) openDoorAt(doorPos);
                visitor.getNavigation().stop();
                timer++;
                if (timer > 15) {
                    if (visitor.level().isClientSide()) return;
                    if (visitor.level().getBlockEntity(targetPos) instanceof ToiletBlockEntity toilet) {
                        if (doorPos != null) openDoorAt(doorPos);
                        if (toilet.isVisitorOccupant(visitor.getUUID())) {
                            stage = Stage.USING;
                            usingTicks = 80;
                            if (visitor.level() instanceof net.minecraft.server.level.ServerLevel sl) {
                                toilet.seatVisitor(sl, visitor);
                            }
                            return;
                        }

                        boolean granted = toilet.requestVisitorUse(visitor.getUUID());
                        if (granted) {
                            stage = Stage.USING;
                            usingTicks = 80;
                            if (visitor.level() instanceof net.minecraft.server.level.ServerLevel sl) {
                                toilet.seatVisitor(sl, visitor);
                            }
                        } else {
                            stage = Stage.QUEUING;
                            queueTicks = 0;
                            repathIfNeeded(targetPos);
                        }
                    } else {
                        targetPos = null;
                        stage = null;
                    }
                }
            } else {
                repathIfNeeded(targetPos);
            }
        }

        @Override
        public boolean canContinueToUse() {
            if (stage == Stage.QUEUING && targetPos != null) return true;
            if (stage == Stage.USING && targetPos != null) return true;
            return targetPos != null && visitor.level().getBlockState(targetPos).is(net.zootycoon.block.ZooBlocks.TOILET);
        }

        private void moveTo(BlockPos pos) {
            if (pos == null) return;
            visitor.getNavigation().moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 1.0D);
        }

        private void repathIfNeeded(BlockPos pos) {
            if (pos == null) return;
            if (repathCooldown > 0) {
                repathCooldown--;
                return;
            }
            if (visitor.getNavigation().isDone() || visitor.tickCount % 20 == 0) {
                moveTo(pos);
                repathCooldown = 10;
            }
        }

        private void openDoorAt(BlockPos pos) {
            var state = visitor.level().getBlockState(pos);
            if (state.getBlock() instanceof net.minecraft.world.level.block.DoorBlock door) {
                if (!state.getValue(net.minecraft.world.level.block.DoorBlock.OPEN)) {
                    door.setOpen(visitor, visitor.level(), state, pos, true);
                }
                return;
            }
            if (state.getBlock() instanceof net.minecraft.world.level.block.FenceGateBlock) {
                if (!state.getValue(net.minecraft.world.level.block.FenceGateBlock.OPEN)) {
                    visitor.level().setBlock(pos, state.setValue(net.minecraft.world.level.block.FenceGateBlock.OPEN, true), 3);
                }
            }
        }

        private BlockPos findNearestDoorNear(BlockPos center, int radius) {
            BlockPos best = null;
            double bestDist = Double.MAX_VALUE;
            for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -2, -radius), center.offset(radius, 2, radius))) {
                var st = visitor.level().getBlockState(pos);
                if (!(st.getBlock() instanceof net.minecraft.world.level.block.DoorBlock)
                        && !(st.getBlock() instanceof net.minecraft.world.level.block.FenceGateBlock)) {
                    continue;
                }
                double d = visitor.distanceToSqr(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                if (d < bestDist) {
                    bestDist = d;
                    best = pos.immutable();
                }
            }
            return best;
        }
    }
}

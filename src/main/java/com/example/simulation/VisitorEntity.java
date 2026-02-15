package com.example.simulation;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;

public class VisitorEntity extends PathfinderMob {
    // 0 = Default, 1-19 = Variasi dari assetzone
    private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(VisitorEntity.class,
            EntityDataSerializers.INT);
    // 0 = Adult, 1 = Child
    private static final EntityDataAccessor<Boolean> IS_CHILD = SynchedEntityData.defineId(VisitorEntity.class,
            EntityDataSerializers.BOOLEAN);
    // Needs
    private static final EntityDataAccessor<Integer> HUNGER = SynchedEntityData.defineId(VisitorEntity.class,
            EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> THIRST = SynchedEntityData.defineId(VisitorEntity.class,
            EntityDataSerializers.INT);

    // Fields
    private BlockPos gatePos = null;
    private int stayTime = 0; // Seberapa lama sudah di zoo
    private static final int MAX_STAY = 2400; // 2 menit

    // Moods
    public enum Mood {
        HAPPY("Senang", 0x00FF00),
        NEUTRAL("Biasa", 0xFFFF00),
        TOILET("Kebelet Pipis", 0xFFA500),
        AMAZED("Takjub", 0x00FFFF),
        ADORED("Gemas", 0xFF69B4);

        public final String label;
        public final int color;

        Mood(String label, int color) {
            this.label = label;
            this.color = color;
        }
    }

    private Mood currentMood = Mood.HAPPY;
    private int moodTimer = 0;

    public Mood getMood() {
        return currentMood;
    }

    public void setMood(Mood mood, int duration) {
        this.currentMood = mood;
        this.moodTimer = duration;
    }

    public VisitorEntity(EntityType<? extends VisitorEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(VARIANT, 0);
        this.entityData.define(IS_CHILD, false);
        this.entityData.define(HUNGER, 0);
        this.entityData.define(THIRST, 0);
    }

    public int getVariant() {
        return this.entityData.get(VARIANT);
    }

    public void setVariant(int variant) {
        this.entityData.set(VARIANT, variant);
    }

    public boolean isChildVisitor() {
        return this.entityData.get(IS_CHILD);
    }

    public void setChildVisitor(boolean isChild) {
        this.entityData.set(IS_CHILD, isChild);
    }

    public int getHunger() {
        return this.entityData.get(HUNGER);
    }

    public void setHunger(int val) {
        this.entityData.set(HUNGER, val);
    }

    public int getThirst() {
        return this.entityData.get(THIRST);
    }

    public void setThirst(int val) {
        this.entityData.set(THIRST, val);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
            @Nullable SpawnGroupData spawnData, @Nullable CompoundTag dataTag) {
        // Randomize Variant (0 to 19 based on asset count)
        this.setVariant(this.getRandom().nextInt(20));

        // Randomize Age/Size (20% chance to be child)
        if (this.getRandom().nextFloat() < 0.2F) {
            this.setChildVisitor(true);
            applySize(0.6F); // 60% size
        } else {
            this.setChildVisitor(false);
            applySize(1.0F); // Normal size
        }

        return super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
    }

    private void applySize(float scale) {
        // Use Pehkui if installed
        if (PehkuiIntegration.isPehkuiLoaded()) {
            PehkuiIntegration.resizeEntity(this, scale);
        } else {
            // Fallback? Vanilla doesn't support easy dynamic resize without restart or ugly
            // hacks.
            // Just ignore if Pehkui missing.
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide) {
            // Failsafe: If variant is still 0 after spawn, randomize it
            if (this.tickCount == 5 && getVariant() == 0) {
                this.setVariant(this.getRandom().nextInt(20));
                if (this.getRandom().nextFloat() < 0.2F) {
                    this.setChildVisitor(true);
                    applySize(0.6F); // 60% size
                }
            }

            stayTime++;

            // Needs Increase over time
            if (this.tickCount % 600 == 0) { // Every 30 seconds
                setHunger(Math.min(100, getHunger() + 5));
                setThirst(Math.min(100, getThirst() + 8));

                // Mood Logic Check - Toilet
                if (this.getRandom().nextFloat() < 0.1F) { // 10% chance every 30s
                    setMood(Mood.TOILET, 600);
                }
            }

            if (moodTimer > 0) {
                moodTimer--;
                if (moodTimer == 0) {
                    setMood(Mood.HAPPY, 0); // Revert to happy
                }
            }

            // Logic: Drop Trash (Random Chance: 0.05% per tick)
            if (this.getRandom().nextFloat() < 0.0005F) {
                BlockPos pos = this.blockPosition();
                if (this.level().getBlockState(pos).isAir()) {
                    this.level().setBlock(pos, IndoZooTycoon.TRASH_BLOCK.get().defaultBlockState(), 3);
                }
            }
        }
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new LeaveZooGoal(this));

        // Critical Needs
        this.goalSelector.addGoal(2, new FindFacilityGoal(this, "food"));
        this.goalSelector.addGoal(2, new FindFacilityGoal(this, "drink"));

        this.goalSelector.addGoal(3, new OpenDoorGoal(this, true));
        this.goalSelector.addGoal(4, new WatchAnimalsGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.7D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
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

    public void forceLeave() {
        if (gatePos != null) {
            this.getNavigation().stop();
            this.getNavigation().moveTo(gatePos.getX(), gatePos.getY(), gatePos.getZ(), 1.2D);
            if (this.distanceToSqr(gatePos.getX(), gatePos.getY(), gatePos.getZ()) < 9) {
                this.discard();
            }
        } else {
            this.discard();
        }
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
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("GateX")) {
            gatePos = new BlockPos(tag.getInt("GateX"), tag.getInt("GateY"), tag.getInt("GateZ"));
        }
        stayTime = tag.getInt("StayTime");
        if (tag.contains("Variant"))
            setVariant(tag.getInt("Variant"));
        if (tag.contains("IsChild")) {
            setChildVisitor(tag.getBoolean("IsChild"));
            // Re-apply size on load
            applySize(isChildVisitor() ? 0.6F : 1.0F);
        }
        if (tag.contains("Hunger"))
            setHunger(tag.getInt("Hunger"));
        if (tag.contains("Thirst"))
            setThirst(tag.getInt("Thirst"));
    }

    // --- INNER GOALS ---

    @Override
    protected net.minecraft.world.InteractionResult mobInteract(Player player,
            net.minecraft.world.InteractionHand hand) {
        if (!this.level().isClientSide) {
            String status = "Visitor [" + getVariant() + "]\n" +
                    "Mood: " + getMood().label + "\n" +
                    "Hunger: " + getHunger() + "% | Thirst: " + getThirst() + "%";
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(status)
                    .withStyle(net.minecraft.ChatFormatting.YELLOW));
        }
        return net.minecraft.world.InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    // ...

    static class FindFacilityGoal extends Goal {
        private final VisitorEntity visitor;
        private final String type; // "food" or "drink"
        private BlockPos targetPos;
        private int timer;

        public FindFacilityGoal(VisitorEntity v, String type) {
            this.visitor = v;
            this.type = type;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (type.equals("food") && visitor.getHunger() < 50)
                return false;
            if (type.equals("drink") && visitor.getThirst() < 50)
                return false;
            if (visitor.getRandom().nextFloat() > 0.05F)
                return false;

            // Scan for facility
            BlockPos p = visitor.blockPosition();
            net.minecraft.world.level.block.Block targetBlock = type.equals("food")
                    ? IndoZooTycoon.FOOD_STALL_BLOCK.get()
                    : IndoZooTycoon.DRINK_STALL_BLOCK.get();

            for (BlockPos pos : BlockPos.betweenClosed(p.offset(-30, -5, -30), p.offset(30, 5, 30))) {
                if (visitor.level().getBlockState(pos).is(targetBlock)) {
                    this.targetPos = pos.immutable();
                    return true;
                }
            }
            return false;
        }

        @Override
        public void start() {
            visitor.getNavigation().moveTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 1.0D);
            timer = 0;
        }

        @Override
        public void stop() {
            targetPos = null;
        }

        @Override
        public void tick() {
            if (targetPos == null)
                return;
            if (visitor.distanceToSqr(targetPos.getX(), targetPos.getY(), targetPos.getZ()) < 6.0D) {
                // Arrived
                visitor.getNavigation().stop();
                timer++;
                if (timer > 40) { // 2 seconds to eat/drink
                    if (type.equals("food"))
                        visitor.setHunger(0);
                    else
                        visitor.setThirst(0);

                    // NEW: Pay for service
                    if (!visitor.level().isClientSide) {
                        ZooData.get(visitor.level()).addBalance(15);
                        visitor.playSound(net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
                    }

                    targetPos = null; // Done
                }
            } else {
                // Keep moving if path lost
                if (visitor.getNavigation().isDone()) {
                    visitor.getNavigation().moveTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 1.0D);
                }
            }
        }

        @Override
        public boolean canContinueToUse() {
            return targetPos != null && visitor.level().getBlockState(targetPos).is(
                    type.equals("food") ? IndoZooTycoon.FOOD_STALL_BLOCK.get() : IndoZooTycoon.DRINK_STALL_BLOCK.get());
        }
    }

    static class WatchAnimalsGoal extends Goal {
        private final VisitorEntity visitor;
        private Animal target = null;
        private int watchTimer = 0;

        public WatchAnimalsGoal(VisitorEntity v) {
            this.visitor = v;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (visitor.isTimeToLeave())
                return false;
            if (visitor.getRandom().nextFloat() < 0.05F) {
                List<Animal> animals = visitor.level().getEntitiesOfClass(Animal.class,
                        visitor.getBoundingBox().inflate(15));
                if (!animals.isEmpty()) {
                    target = animals.get(visitor.getRandom().nextInt(animals.size()));
                    return true;
                }
            }
            return false;
        }

        @Override
        public void start() {
            watchTimer = 100 + visitor.getRandom().nextInt(200);
        }

        @Override
        public void tick() {
            if (target != null) {
                double dist = visitor.distanceToSqr(target);
                if (dist > 25) {
                    visitor.getNavigation().moveTo(target, 1.0D);
                }
                visitor.getLookControl().setLookAt(target, 30, 30);
                if (watchTimer > 0) {
                    watchTimer--;
                    // Mood Trigger Logic
                    if (watchTimer % 20 == 0) {
                        float width = target.getBbWidth();
                        if (width > 1.5F) {
                            visitor.setMood(Mood.AMAZED, 100);
                        } else if (width < 0.8F || target.isBaby()) {
                            visitor.setMood(Mood.ADORED, 100);
                        }

                        net.minecraft.resources.ResourceLocation id = net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES
                                .getKey(target.getType());
                        if (id != null && (id.getPath().contains("dragon") || id.getPath().contains("wither")
                                || id.getPath().contains("serpent"))) {
                            visitor.setMood(Mood.AMAZED, 200);
                        }
                    }
                }
            }
        }

        @Override
        public boolean canContinueToUse() {
            return watchTimer > 0 && target != null && target.isAlive();
        }
    }

    static class LeaveZooGoal extends Goal {
        private final VisitorEntity visitor;

        public LeaveZooGoal(VisitorEntity v) {
            this.visitor = v;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return visitor.isTimeToLeave() && visitor.getGatePos() != null;
        }

        @Override
        public void start() {
            // Recalculate path to gate
            BlockPos gate = visitor.getGatePos();
            visitor.getNavigation().moveTo(gate.getX(), gate.getY(), gate.getZ(), 1.0D);
        }

        @Override
        public void tick() {
            BlockPos gate = visitor.getGatePos();
            // Keep moving
            if (visitor.getNavigation().isDone()) {
                visitor.getNavigation().moveTo(gate.getX(), gate.getY(), gate.getZ(), 1.0D);
            }

            if (visitor.distanceToSqr(gate.getX(), gate.getY(), gate.getZ()) < 4) {
                visitor.discard();
            }
        }
    }
}

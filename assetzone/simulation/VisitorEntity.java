package com.example.simulation;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.core.Direction;
import net.minecraftforge.registries.ForgeRegistries;

public class VisitorEntity extends PathfinderMob implements RangedAttackMob {
    private static final Logger LOGGER = LogManager.getLogger(IndoZooTycoon.MODID);
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
    // Mood Sync
    private static final EntityDataAccessor<Integer> MOOD = SynchedEntityData.defineId(VisitorEntity.class,
            EntityDataSerializers.INT);
    // Hunter Variant (Very Rare)
    private static final EntityDataAccessor<Boolean> IS_HUNTER = SynchedEntityData.defineId(VisitorEntity.class,
            EntityDataSerializers.BOOLEAN);
    // Hunter Mode: 0 = Kidnapper, 1 = Archer
    private static final EntityDataAccessor<Integer> HUNTER_MODE = SynchedEntityData.defineId(VisitorEntity.class,
            EntityDataSerializers.INT);
    // Kidnapping
    private static final EntityDataAccessor<Boolean> IS_CARRYING = SynchedEntityData.defineId(VisitorEntity.class,
            EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<CompoundTag> CARRIED_ANIMAL_DATA = SynchedEntityData.defineId(VisitorEntity.class,
            EntityDataSerializers.COMPOUND_TAG);
    // Escape Mode
    private static final EntityDataAccessor<Boolean> IS_ESCAPE_MODE = SynchedEntityData.defineId(VisitorEntity.class,
            EntityDataSerializers.BOOLEAN);
    // Trash
    private static final EntityDataAccessor<Boolean> HAS_TRASH = SynchedEntityData.defineId(VisitorEntity.class,
            EntityDataSerializers.BOOLEAN);

    // Fields
    private BlockPos gatePos = null;
    private int stayTime = 0; // Seberapa lama sudah di zoo
    private static final int MAX_STAY = 2400; // 2 menit
    private static final int MAX_VIEWERS_PER_ANIMAL = 4;
    private static final double VISITOR_BASE_SPEED = 0.28D;
    private static final double HUNTER_BASE_SPEED = 0.22D;
    private static final double HUNTER_RUN_SPEED = 0.45D;

    public enum HunterMode {
        KIDNAPPER(0),
        ARCHER(1);

        public final int id;

        HunterMode(int id) {
            this.id = id;
        }

        public static HunterMode fromId(int id) {
            for (HunterMode m : values()) {
                if (m.id == id) return m;
            }
            return KIDNAPPER;
        }
    }

    // Moods
    public enum Mood {
        HAPPY("Senang", 0x00FF00),
        NEUTRAL("Biasa", 0xFFFFFF),
        TOILET("Kebelet Pipis", 0xFFA500),
        AMAZED("Takjub", 0x00FFFF),
        ADORED("Gemas", 0xFF69B4),
        HUNGRY("Lapar", 0xFF0000),
        THIRSTY("Haus", 0x0000FF);

        public final String label;
        public final int color;

        Mood(String label, int color) {
            this.label = label;
            this.color = color;
        }
    }

    private int moodTimer = 0;
    private int toiletTimer = 0; // Tracks duration of TOILET mood
    private int watchingAnimalId = -1;
    private boolean lastCarryState = false;
    private final java.util.HashMap<Long, Integer> tempPlacedBlocks = new java.util.HashMap<>();
    private long hunterCrimeUntilTick = 0L;

    public Mood getMood() {
        int ordinal = this.entityData.get(MOOD);
        if (ordinal < 0 || ordinal >= Mood.values().length) {
            return Mood.NEUTRAL;
        }
        return Mood.values()[ordinal];
    }

    public void setMood(Mood mood, int duration) {
        Mood current = getMood();
        if (current == mood && this.moodTimer > duration)
            return; // Keep longer duration

        this.entityData.set(MOOD, mood.ordinal());
        this.moodTimer = duration;
    }

    public VisitorEntity(EntityType<? extends VisitorEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        net.minecraft.world.entity.projectile.AbstractArrow arrow = net.minecraft.world.entity.projectile.ProjectileUtil.getMobArrow(this, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.ARROW), distanceFactor);
        double d0 = target.getX() - this.getX();
        double d1 = target.getY(0.3333333333333333D) - arrow.getY();
        double d2 = target.getZ() - this.getZ();
        double d3 = Math.sqrt(d0 * d0 + d2 * d2);
        arrow.shoot(d0, d1 + d3 * 0.20000000298023224D, d2, 1.6F, (float)(14 - this.level().getDifficulty().getId() * 4));
        this.playSound(net.minecraft.sounds.SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
        this.level().addFreshEntity(arrow);
        if (isHunter() && target != null && !(target instanceof StaffEntity) && !(target instanceof VisitorEntity)) {
            markHunterCrime("attack");
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(VARIANT, 0);
        this.entityData.define(IS_CHILD, false);
        this.entityData.define(HUNGER, 0);
        this.entityData.define(THIRST, 0);
        this.entityData.define(MOOD, Mood.NEUTRAL.ordinal());
        this.entityData.define(IS_HUNTER, false);
        this.entityData.define(HUNTER_MODE, HunterMode.KIDNAPPER.id);
        this.entityData.define(IS_CARRYING, false);
        this.entityData.define(CARRIED_ANIMAL_DATA, new CompoundTag());
        this.entityData.define(IS_ESCAPE_MODE, false);
        this.entityData.define(HAS_TRASH, false);
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

    public boolean isHunter() {
        return this.entityData.get(IS_HUNTER);
    }

    public void setHunter(boolean hunter) {
        this.entityData.set(IS_HUNTER, hunter);
        if (hunter) {
            this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(HUNTER_BASE_SPEED);
            this.setChildVisitor(false);
            applySize(1.0F);
        }
    }

    public HunterMode getHunterMode() {
        return HunterMode.fromId(this.entityData.get(HUNTER_MODE));
    }

    public void setHunterMode(HunterMode mode) {
        this.entityData.set(HUNTER_MODE, mode.id);
    }

    public boolean isCarrying() {
        return this.isHunter() && (this.entityData.get(IS_CARRYING) || !this.getPassengers().isEmpty());
    }

    private void setCarrying(boolean carrying) {
        this.entityData.set(IS_CARRYING, carrying);
    }

    public CompoundTag getCarriedAnimalData() {
        return this.entityData.get(CARRIED_ANIMAL_DATA);
    }

    public void setCarriedAnimalData(CompoundTag data) {
        this.entityData.set(CARRIED_ANIMAL_DATA, data);
    }

    public boolean isEscapeMode() {
        return this.entityData.get(IS_ESCAPE_MODE);
    }

    public void setEscapeMode(boolean escape) {
        this.entityData.set(IS_ESCAPE_MODE, escape);
    }

    public boolean hasTrash() {
        return this.entityData.get(HAS_TRASH);
    }

    public void setHasTrash(boolean trash) {
        this.entityData.set(HAS_TRASH, trash);
    }

    public void markHunterCrime(String reason) {
        if (!isHunter()) return;
        this.hunterCrimeUntilTick = this.level().getGameTime() + 20L * 60L; // 60s threat window
        this.setEscapeMode(true);
        this.getPersistentData().putLong("HunterCrimeUntil", this.hunterCrimeUntilTick);
        if (reason != null && !reason.isEmpty()) {
            this.getPersistentData().putString("HunterCrimeReason", reason);
        }
    }

    public boolean hasActiveHunterCrime() {
        if (!isHunter()) return false;
        long now = this.level().getGameTime();
        if (hunterCrimeUntilTick > now) return true;
        long fromTag = this.getPersistentData().getLong("HunterCrimeUntil");
        return fromTag > now;
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

        boolean hunterFromTag = false;
        if (dataTag != null && dataTag.contains("IsHunter")) {
            this.setHunter(dataTag.getBoolean("IsHunter"));
            hunterFromTag = true;
        }
        if (!hunterFromTag && this.getRandom().nextFloat() < 0.5F) {
            this.setHunter(true);
        }

        if (this.isHunter()) {
            if (dataTag != null && dataTag.contains("HunterMode")) {
                this.setHunterMode(HunterMode.fromId(dataTag.getInt("HunterMode")));
            } else {
                this.setHunterMode(this.getRandom().nextFloat() < 0.15F ? HunterMode.ARCHER : HunterMode.KIDNAPPER);
            }
        }

        if (this.isHunter()) {
            var follow = this.getAttribute(Attributes.FOLLOW_RANGE);
            if (follow != null) {
                follow.setBaseValue(64.0D);
            }
        }

        return super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
    }

    private void applySize(float scale) {
        float finalScale = this.isHunter() ? 1.0F : scale;
        if (PehkuiIntegration.isPehkuiLoaded()) {
            PehkuiIntegration.resizeEntity(this, finalScale);
        } else {
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, VISITOR_BASE_SPEED)
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

            if (isHunter()) {
                if (getHunger() != 0) setHunger(0);
                if (getThirst() != 0) setThirst(0);
                if (getMood() != Mood.NEUTRAL) setMood(Mood.NEUTRAL, 0);
                if (hasTrash()) setHasTrash(false);
            } else {
                if (this.tickCount % 600 == 0) {
                    setHunger(Math.min(100, getHunger() + 5));
                    setThirst(Math.min(100, getThirst() + 8));
                }

                if (this.tickCount % 100 == 0) {
                    if (getHunger() > 70) {
                        setMood(Mood.HUNGRY, 120);
                    } else if (getThirst() > 70) {
                        setMood(Mood.THIRSTY, 120);
                    } else if (this.getRandom().nextFloat() < 0.15F) {
                        if (this.getRandom().nextBoolean())
                            setMood(Mood.HAPPY, 100);
                        else
                            setMood(Mood.NEUTRAL, 100);
                    }

                    if (this.getRandom().nextFloat() < 0.05F) {
                        setMood(Mood.TOILET, 1200);
                    }
                }

                if (getMood() == Mood.TOILET) {
                    toiletTimer++;
                    if (toiletTimer > 600) {
                        ZooData data = ZooData.get(this.level());
                        data.setRating(data.getRating() - 5);
                        this.forceLeave();
                    }
                } else {
                    toiletTimer = 0;
                }

                if (moodTimer > 0) {
                    moodTimer--;
                    if (moodTimer == 0) {
                        if (getMood() != Mood.TOILET) {
                            setMood(Mood.NEUTRAL, 0);
                        } else {
                        }
                    }
                }

                if (this.getRandom().nextFloat() < 0.0005F) {
                    BlockPos pos = this.blockPosition();
                    if (this.level().getBlockState(pos).isAir()) {
                        int variant = this.getRandom().nextInt(4);
                        this.level().setBlock(pos,
                                IndoZooTycoon.TRASH_BLOCK.get().defaultBlockState().setValue(TrashBlock.VARIANT, variant),
                                3);
                    }
                }
            }

            // Hunter logic: equip bow
            if (isHunter() && getHunterMode() == HunterMode.ARCHER && this.getItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND).isEmpty()) {
                this.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,
                        new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.BOW));
            }

            boolean carrying = isHunter() && isCarrying();
            if (carrying != lastCarryState) {
                LOGGER.info("Hunter {} carrying={} pos={}", this.getId(), carrying, this.blockPosition());
                lastCarryState = carrying;
            }
            if (isHunter()) {
                boolean runMode = (carrying || isEscapeMode()) && getHunterMode() == HunterMode.KIDNAPPER;
                this.setSprinting(runMode);
                tickTempBlocks();
            }
            updateMovementSpeed();

            if (isHunter() && getHunterMode() == HunterMode.KIDNAPPER && this.entityData.get(IS_CARRYING) && this.getPassengers().isEmpty()) {
                CompoundTag carried = getCarriedAnimalData();
                if (carried != null && carried.contains("ZooTaggedId")) {
                    ZooData.get(this.level()).setAnimalCarried(carried.getInt("ZooTaggedId"), false);
                }
                setCarrying(false);
                setCarriedAnimalData(new CompoundTag());
            }

            if (isHunter() && getHunterMode() == HunterMode.KIDNAPPER && isCarrying() && !isEscapeMode()) {
                setEscapeMode(true);
            }
        }
    }

    private void tickTempBlocks() {
        if (this.level().isClientSide) return;
        if (tempPlacedBlocks.isEmpty()) return;
        java.util.Iterator<java.util.Map.Entry<Long, Integer>> it = tempPlacedBlocks.entrySet().iterator();
        while (it.hasNext()) {
            java.util.Map.Entry<Long, Integer> e = it.next();
            int t = e.getValue() - 1;
            if (t > 0) {
                e.setValue(t);
                continue;
            }
            BlockPos pos = BlockPos.of(e.getKey());
            if (this.level().getBlockState(pos).is(net.minecraft.world.level.block.Blocks.DIRT)) {
                if (this.level().getEntities(null, new AABB(pos)).isEmpty()) {
                    this.level().setBlock(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
                }
            }
            it.remove();
        }
    }

    private void updateMovementSpeed() {
        double desired = isHunter() ? (isCarrying() ? HUNTER_RUN_SPEED : HUNTER_BASE_SPEED) : VISITOR_BASE_SPEED;
        double current = this.getAttribute(Attributes.MOVEMENT_SPEED).getBaseValue();
        double diff = desired - current;
        if (Math.abs(diff) > 0.001D) {
            double step = Math.signum(diff) * 0.005D;
            if (Math.abs(step) > Math.abs(diff)) {
                step = diff;
            }
            this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(current + step);
        }
    }

    @Override
    protected void positionRider(Entity passenger, MoveFunction moveFunction) {
        if (this.isHunter() && this.isCarrying()) {
            float yaw = this.getYRot();
            double rad = Math.toRadians(yaw);
            double sideX = -Math.sin(rad) * 0.35;
            double sideZ = Math.cos(rad) * 0.35;
            double forwardX = Math.cos(rad) * 0.15;
            double forwardZ = Math.sin(rad) * 0.15;

            double px = this.getX() + sideX + forwardX;
            double py = this.getY() + 0.45;
            double pz = this.getZ() + sideZ + forwardZ;
            moveFunction.accept(passenger, px, py, pz);
            passenger.setYRot(this.getYRot());
            passenger.setXRot(0.0F);
            return;
        }
        super.positionRider(passenger, moveFunction);
    }

    @Override
    public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float amount) {
        if (isCarrying() && !this.level().isClientSide) {
            dropCarriedAnimal();
            this.setEscapeMode(true);
            this.playSound(net.minecraft.sounds.SoundEvents.CHICKEN_HURT, 1.0F, 0.5F);
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && isHunter() && target instanceof LivingEntity && !(target instanceof StaffEntity) && !(target instanceof VisitorEntity)) {
            markHunterCrime("melee");
        }
        return hit;
    }

    private void dropCarriedAnimal() {
        if (this.level().isClientSide) return;
        if (!this.getPassengers().isEmpty()) {
            Entity passenger = this.getPassengers().get(0);
            passenger.stopRiding();
            passenger.setPos(this.getX(), this.getY(), this.getZ());
            if (passenger instanceof net.minecraft.world.entity.Mob mob) {
                mob.setNoAi(false);
            }
            ZooData data = ZooData.get(this.level());
            data.setAnimalCarried(passenger.getId(), false);
            setCarrying(false);
            setCarriedAnimalData(new CompoundTag());
            setEscapeMode(false);
            return;
        }
        CompoundTag carried = getCarriedAnimalData();
        if (carried != null && carried.contains("ZooTaggedId")) {
            ZooData.get(this.level()).setAnimalCarried(carried.getInt("ZooTaggedId"), false);
        }
        setCarrying(false);
        setCarriedAnimalData(new CompoundTag());
        setEscapeMode(false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new LeaveZooGoal(this));

        // Critical Needs
        this.goalSelector.addGoal(2, new FindFacilityGoal(this, "food"));
        this.goalSelector.addGoal(2, new FindFacilityGoal(this, "drink"));
        this.goalSelector.addGoal(2, new FindFacilityGoal(this, "toilet"));
        this.goalSelector.addGoal(2, new FindTrashCanGoal(this));

        this.goalSelector.addGoal(3, new OpenDoorGoal(this, true));
        this.goalSelector.addGoal(4, new WatchAnimalsGoal(this));

        // Hunter Goals
        this.goalSelector.addGoal(0, new AvoidEntityGoal<>(this, StaffEntity.class, 16.0F, 1.4D, 1.5D, (staff) -> {
            return staff instanceof StaffEntity s && s.getRole() == 2;
        }) {
            @Override
            public boolean canUse() {
                return super.canUse() && VisitorEntity.this.isHunter();
            }
        });

        this.goalSelector.addGoal(1, new KidnapAnimalGoal(this));
        this.goalSelector.addGoal(1, new EscapeWithAnimalGoal(this));

        this.goalSelector.addGoal(2, new RangedBowAttackGoal<>(this, 1.0D, 20, 15.0F) {
            @Override
            public boolean canUse() {
                return super.canUse() && VisitorEntity.this.isHunter() && VisitorEntity.this.getHunterMode() == HunterMode.ARCHER;
            }
        });
        this.targetSelector.addGoal(1, new net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal<>(this, net.minecraft.world.entity.Mob.class, 10, true, false, (target) -> {
            if (target == null) return false;
            if (target instanceof Player) return false;
            if (target instanceof StaffEntity) return false;
            if (target instanceof VisitorEntity) return false;
            ZooData data = ZooData.get(VisitorEntity.this.level());
            for (int i = 0; i < data.getTaggedAnimals().size(); i++) {
                CompoundTag t = data.getTaggedAnimals().getCompound(i);
                if (t.getInt("id") == target.getId() && !t.getBoolean("carried")) {
                    return true;
                }
            }
            return false;
        }) {
            @Override
            public boolean canUse() {
                return super.canUse() && VisitorEntity.this.isHunter() && VisitorEntity.this.getHunterMode() == HunterMode.ARCHER;
            }
        });

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

    int getWatchingAnimalId() {
        return watchingAnimalId;
    }

    public void forceLeave() {
        if (this.isHunter()) {
            if (!this.level().isClientSide) {
                removeVisitorFromData();
            }
            if (gatePos != null) {
                this.getNavigation().stop();
                BlockPos out = gatePos.offset(this.getX() > gatePos.getX() ? 20 : -20, 0,
                        this.getZ() > gatePos.getZ() ? 20 : -20);
                this.getNavigation().moveTo(out.getX(), out.getY(), out.getZ(), 1.3D);
            }
            return;
        }
        if (gatePos != null) {
            this.getNavigation().stop();
            this.getNavigation().moveTo(gatePos.getX(), gatePos.getY(), gatePos.getZ(), 1.2D);
            if (this.distanceToSqr(gatePos.getX(), gatePos.getY(), gatePos.getZ()) < 9) {
                removeVisitorFromData();
                this.discard();
            }
        } else {
            removeVisitorFromData();
            this.discard();
        }
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        if (!this.level().isClientSide && watchingAnimalId != -1) {
            ZooData.get(this.level()).decrementViewer(watchingAnimalId);
            watchingAnimalId = -1;
        }
        super.remove(reason);
    }

    private void removeVisitorFromData() {
        if (!this.level().isClientSide) {
            ZooData data = ZooData.get(this.level());
            if (data.getVisitorCount() > 0) {
                data.setVisitorCount(data.getVisitorCount() - 1);
                SyncBalancePacket packet = new SyncBalancePacket(data.getBalance(), data.getTaggedAnimals(),
                        data.getAnimalCount(), data.getStaffCount(), data.getVisitorCount(), data.getRating());
                PacketHandler.INSTANCE.send(net.minecraftforge.network.PacketDistributor.ALL.noArg(), packet);
            }
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
        tag.putInt("Mood", getMood().ordinal());
        tag.putInt("MoodTimer", moodTimer);
        tag.putBoolean("IsHunter", isHunter());
        tag.putInt("HunterMode", getHunterMode().id);
        tag.putBoolean("IsCarrying", isCarrying());
        tag.put("CarriedAnimalData", getCarriedAnimalData());
        tag.putBoolean("IsEscapeMode", isEscapeMode());
        tag.putBoolean("HasTrash", hasTrash());
        tag.putLong("HunterCrimeUntil", hunterCrimeUntilTick);
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
        if (tag.contains("Mood")) {
            int moodIdx = tag.getInt("Mood");
            if (moodIdx >= 0 && moodIdx < Mood.values().length) {
                this.entityData.set(MOOD, moodIdx);
            }
        }
        if (tag.contains("MoodTimer"))
            moodTimer = tag.getInt("MoodTimer");
        if (tag.contains("IsHunter"))
            setHunter(tag.getBoolean("IsHunter"));
        if (tag.contains("HunterMode"))
            setHunterMode(HunterMode.fromId(tag.getInt("HunterMode")));
        if (tag.contains("IsCarrying"))
            setCarrying(tag.getBoolean("IsCarrying"));
        if (tag.contains("CarriedAnimalData"))
            setCarriedAnimalData(tag.getCompound("CarriedAnimalData"));
        if (tag.contains("IsEscapeMode"))
            setEscapeMode(tag.getBoolean("IsEscapeMode"));
        if (tag.contains("HasTrash"))
            setHasTrash(tag.getBoolean("HasTrash"));
        if (tag.contains("HunterCrimeUntil")) {
            hunterCrimeUntilTick = tag.getLong("HunterCrimeUntil");
            this.getPersistentData().putLong("HunterCrimeUntil", hunterCrimeUntilTick);
        }
    }

    // --- INNER GOALS ---

    private class KidnapAnimalGoal extends Goal {
        private final VisitorEntity visitor;
        private net.minecraft.world.entity.Mob targetAnimal;
        private int kidnapTimer = 0;
        private int placeCooldown = 0;

        public KidnapAnimalGoal(VisitorEntity visitor) {
            this.visitor = visitor;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (!visitor.isHunter() || visitor.getHunterMode() != HunterMode.KIDNAPPER || visitor.isCarrying()) return false;
            
            List<net.minecraft.world.entity.Mob> animals = visitor.level().getEntitiesOfClass(net.minecraft.world.entity.Mob.class, visitor.getBoundingBox().inflate(64.0D),
                    (m) -> !(m instanceof StaffEntity) && !(m instanceof VisitorEntity) && m.getVehicle() == null);
            ZooData data = ZooData.get(visitor.level());
            net.minecraft.world.entity.Mob best = null;
            double bestDist = Double.MAX_VALUE;
            for (net.minecraft.world.entity.Mob animal : animals) {
                if (!animal.isAlive()) continue;
                if (animal.getVehicle() != null) continue;
                if (!isTagged(data, animal.getId())) continue;
                if (isTaggedCarried(data, animal.getId())) continue;
                double d = visitor.distanceToSqr(animal);
                if (d < bestDist) {
                    bestDist = d;
                    best = animal;
                }
            }
            this.targetAnimal = best;
            return best != null;
        }

        @Override
        public void start() {
            this.kidnapTimer = 0;
            this.placeCooldown = 0;
            visitor.getNavigation().moveTo(targetAnimal, 1.1D);
        }

        @Override
        public boolean canContinueToUse() {
            return visitor.isHunter() && visitor.getHunterMode() == HunterMode.KIDNAPPER && !visitor.isCarrying()
                    && targetAnimal != null && targetAnimal.isAlive() && isTagged(ZooData.get(visitor.level()), targetAnimal.getId());
        }

        @Override
        public void tick() {
            if (targetAnimal == null || !targetAnimal.isAlive()) {
                targetAnimal = null;
                return;
            }
            visitor.getLookControl().setLookAt(targetAnimal, 30.0F, 30.0F);
            double dist = visitor.distanceToSqr(targetAnimal);
            if (dist < 4.5D) {
                kidnapTimer++;
                if (kidnapTimer >= 10) {
                    if (!visitor.level().isClientSide) {
                        ZooData data = ZooData.get(visitor.level());
                        if (isTaggedCarried(data, targetAnimal.getId()) || targetAnimal.getVehicle() != null) {
                            kidnapTimer = 0;
                            targetAnimal = null;
                            return;
                        }

                        targetAnimal.getNavigation().stop();
                        data.setAnimalCarried(targetAnimal.getId(), true);

                        CompoundTag carried = new CompoundTag();
                        carried.putInt("ZooTaggedId", targetAnimal.getId());
                        ResourceLocation typeId = ForgeRegistries.ENTITY_TYPES.getKey(targetAnimal.getType());
                        if (typeId != null) {
                            carried.putString("Type", typeId.toString());
                        }
                        CompoundTag nbt = new CompoundTag();
                        if (targetAnimal.save(nbt)) {
                            if (nbt.contains("UUID")) nbt.remove("UUID");
                            carried.put("Nbt", nbt);
                        }

                        boolean mounted = targetAnimal.startRiding(visitor, true);
                        if (mounted) {
                            targetAnimal.setNoAi(true);
                        } else {
                            targetAnimal.discard();
                        }

                        visitor.setCarriedAnimalData(carried);
                        visitor.setCarrying(true);
                        visitor.setEscapeMode(true);
                        visitor.markHunterCrime("kidnap");

                        LOGGER.info("Hunter {} kidnapped animal {}", visitor.getId(), targetAnimal.getId());
                        visitor.playSound(net.minecraft.sounds.SoundEvents.CHICKEN_EGG, 1.0F, 1.0F);
                        targetAnimal = null;
                    }
                }
            } else {
                if (!visitor.level().isClientSide) {
                    if (placeCooldown > 0) placeCooldown--;
                    if (placeCooldown == 0 && (visitor.getNavigation().isStuck() || visitor.horizontalCollision)) {
                        if (attemptClimbPlacement(targetAnimal)) {
                            placeCooldown = 20;
                            return;
                        }
                    }
                }
                if (visitor.getNavigation().isDone() || visitor.tickCount % 20 == 0) {
                    visitor.getNavigation().moveTo(targetAnimal, 1.2D);
                }
            }
        }

        private boolean attemptClimbPlacement(net.minecraft.world.entity.Mob target) {
            if (visitor.tempPlacedBlocks.size() >= 12) return false;
            BlockPos feetPos = visitor.blockPosition();
            BlockPos placePos = null;
            if (visitor.level().getBlockState(feetPos).isAir()) {
                placePos = feetPos;
            } else {
                BlockPos below = feetPos.below();
                if (visitor.level().getBlockState(below).isAir()) {
                    placePos = below;
                }
            }

            if (placePos == null) return false;

            visitor.level().setBlock(placePos, net.minecraft.world.level.block.Blocks.DIRT.defaultBlockState(), 3);
            visitor.tempPlacedBlocks.put(placePos.asLong(), 60);
            visitor.getNavigation().moveTo(placePos.getX() + 0.5, placePos.getY() + 1.0, placePos.getZ() + 0.5, 1.25D);
            return true;
        }

        private boolean isTaggedCarried(ZooData data, int id) {
            net.minecraft.nbt.ListTag tagged = data.getTaggedAnimals();
            for (int i = 0; i < tagged.size(); i++) {
                CompoundTag t = tagged.getCompound(i);
                if (t.getInt("id") == id) {
                    return t.getBoolean("carried");
                }
            }
            return false;
        }

        private boolean isTagged(ZooData data, int id) {
            net.minecraft.nbt.ListTag tagged = data.getTaggedAnimals();
            for (int i = 0; i < tagged.size(); i++) {
                if (tagged.getCompound(i).getInt("id") == id) {
                    return true;
                }
            }
            return false;
        }
    }


    private class EscapeWithAnimalGoal extends Goal {
        private final VisitorEntity visitor;
        private BlockPos bannerPos;

        public EscapeWithAnimalGoal(VisitorEntity visitor) {
            this.visitor = visitor;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return visitor.isHunter() && visitor.getHunterMode() == HunterMode.KIDNAPPER && visitor.isCarrying();
        }

        @Override
        public void start() {
            this.bannerPos = findNearestBanner();
            if (bannerPos != null) {
                visitor.getNavigation().moveTo(bannerPos.getX(), bannerPos.getY(), bannerPos.getZ(), 1.3D);
            }
        }

        @Override
        public void tick() {
            if (!visitor.isCarrying()) return;

            if (bannerPos != null) {
                double dist = visitor.distanceToSqr(bannerPos.getX(), bannerPos.getY(), bannerPos.getZ());
                if (dist < 9.0D) {
                    bannerPos = null;
                    // Setelah sampai banner, segera tinggalkan kebun binatang
                    visitor.forceLeave();
                    return;
                } else {
                    if (visitor.getNavigation().isDone() || visitor.tickCount % 20 == 0) {
                        visitor.getNavigation().moveTo(bannerPos.getX(), bannerPos.getY(), bannerPos.getZ(), 1.3D);
                    }
                    return;
                }
            }
            // Bila tidak menemukan banner, tetap coba keluar lewat gerbang jika diketahui
            if (visitor.getGatePos() != null) {
                visitor.forceLeave();
            }
        }

        @Override
        public boolean canContinueToUse() {
            return visitor.isHunter() && visitor.getHunterMode() == HunterMode.KIDNAPPER && visitor.isCarrying();
        }

        @Override
        public void stop() {
            bannerPos = null;
            visitor.getNavigation().stop();
        }

        private BlockPos findNearestBanner() {
            BlockPos origin = visitor.blockPosition();
            int radius = 64;
            BlockPos nearest = null;
            double bestDist = Double.MAX_VALUE;

            for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-radius, -8, -radius),
                    origin.offset(radius, 8, radius))) {
                if (visitor.level().getBlockState(pos).is(IndoZooTycoon.ZOO_BANNER_BLOCK.get())) {
                    double d = origin.distSqr(pos);
                    if (d < bestDist) {
                        bestDist = d;
                        nearest = pos.immutable();
                    }
                }
            }
            return nearest;
        }
    }

    private class BlockBreakGoal extends Goal {
        private final VisitorEntity visitor;
        private BlockPos breakingPos;
        private int breakTick;

        public BlockBreakGoal(VisitorEntity visitor) {
            this.visitor = visitor;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (!visitor.isHunter() || visitor.getHunterMode() != HunterMode.KIDNAPPER) return false;
            if (!visitor.isCarrying()) return false;
            
            if (visitor.getNavigation().isStuck() || visitor.horizontalCollision) {
                Direction forward = visitor.getMotionDirection();
                if (forward == Direction.UP || forward == Direction.DOWN) return false;

                BlockPos front = visitor.blockPosition().relative(forward);
                if (isObstructing(front)) {
                    this.breakingPos = front;
                    return true;
                }
                BlockPos frontHead = front.above();
                if (isObstructing(frontHead)) {
                    this.breakingPos = frontHead;
                    return true;
                }
            }
            return false;
        }

        private boolean isObstructing(BlockPos pos) {
            BlockState state = visitor.level().getBlockState(pos);
            if (state.isAir()) return false;
            if (!state.getFluidState().isEmpty()) return false;
            boolean isFenceLike = state.is(net.minecraft.tags.BlockTags.FENCES)
                    || state.is(net.minecraft.tags.BlockTags.WALLS)
                    || state.is(net.minecraft.tags.BlockTags.FENCE_GATES)
                    || state.is(net.minecraft.tags.BlockTags.DOORS);
            return (state.blocksMotion() || isFenceLike) && state.getDestroySpeed(visitor.level(), pos) >= 0;
        }

        @Override
        public void start() {
            this.breakTick = 0;
        }

        @Override
        public void tick() {
            breakTick++;
            if (breakTick % 5 == 0) {
                visitor.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
                visitor.level().levelEvent(2001, breakingPos, net.minecraft.world.level.block.Block.getId(visitor.level().getBlockState(breakingPos)));
            }
            
            if (breakTick >= 25) { // 1.25 detik hancur (lebih cepat)
                visitor.level().destroyBlock(breakingPos, false);
                this.breakingPos = null;
                // Keep moving immediately
                visitor.getNavigation().recomputePath();
            }
        }

        @Override
        public boolean canContinueToUse() {
            return breakingPos != null && visitor.isAlive();
        }
    }

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
        private BlockPos doorPos;
        private Stage stage;
        private int timer;
        private int usingTicks;
        private int repathCooldown;

        private enum Stage {
            TO_DOOR,
            TO_TARGET,
            USING
        }

        public FindFacilityGoal(VisitorEntity v, String type) {
            this.visitor = v;
            this.type = type;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (visitor.isHunter()) return false;
            if (type.equals("food") && visitor.getHunger() < 50)
                return false;
            if (type.equals("drink") && visitor.getThirst() < 50)
                return false;
            if (type.equals("toilet") && visitor.getMood() != Mood.TOILET)
                return false;

            if (!type.equals("toilet") && visitor.getRandom().nextFloat() > 0.05F)
                return false;

            // Scan for facility
            BlockPos p = visitor.blockPosition();
            net.minecraft.world.level.block.Block targetBlock;

            if (type.equals("food"))
                targetBlock = IndoZooTycoon.FOOD_STALL_BLOCK.get();
            else if (type.equals("drink"))
                targetBlock = IndoZooTycoon.DRINK_STALL_BLOCK.get();
            else
                targetBlock = IndoZooTycoon.RESTROOM_BLOCK.get();

            for (BlockPos pos : BlockPos.betweenClosed(p.offset(-30, -5, -30), p.offset(30, 5, 30))) {
                if (visitor.level().getBlockState(pos).is(targetBlock)) {
                    this.targetPos = pos.immutable();
                    if (type.equals("toilet")) {
                        this.doorPos = findNearestDoorNear(this.targetPos, 6);
                        this.stage = this.doorPos != null ? Stage.TO_DOOR : Stage.TO_TARGET;
                    } else {
                        this.doorPos = null;
                        this.stage = Stage.TO_TARGET;
                    }
                    return true;
                }
            }
            return false;
        }

        // Keep start() / stop() as is if matching signature

        @Override
        public void start() {
            timer = 0;
            usingTicks = 0;
            repathCooldown = 0;
            if (stage == Stage.TO_DOOR && doorPos != null) {
                moveTo(doorPos);
            } else {
                moveTo(targetPos);
            }
            timer = 0;
        }

        @Override
        public void stop() {
            targetPos = null;
            doorPos = null;
            stage = null;
            usingTicks = 0;
            repathCooldown = 0;
        }

        @Override
        public void tick() {
            if (targetPos == null)
                return;

            if (stage == Stage.TO_DOOR && doorPos != null) {
                boolean blocked = !visitor.level().getEntitiesOfClass(VisitorEntity.class,
                        new net.minecraft.world.phys.AABB(doorPos).inflate(0.8D),
                        v -> v != visitor && !v.isHunter()).isEmpty();
                if (blocked && visitor.tickCount % 20 == 0) {
                    BlockPos side = doorPos.offset(visitor.getRandom().nextBoolean() ? 1 : -1, 0, visitor.getRandom().nextBoolean() ? 0 : 1);
                    moveTo(side);
                    return;
                }
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
                    visitor.setMood(Mood.HAPPY, 100);
                    visitor.toiletTimer = 0;
                    if (visitor.getRandom().nextFloat() < 0.45F || visitor.isTimeToLeave()) {
                        visitor.forceLeave();
                    }
                    targetPos = null;
                }
                return;
            }

            if (visitor.distanceToSqr(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5) < 6.0D) {
                // Arrived
                visitor.getNavigation().stop();
                timer++;
                if (timer > 20) {
                    if (type.equals("food"))
                        visitor.setHunger(0);
                    else if (type.equals("drink"))
                        visitor.setThirst(0);
                    else if (type.equals("toilet")) {
                        boolean occupied = false;
                        double cx = targetPos.getX() + 0.5;
                        double cy = targetPos.getY();
                        double cz = targetPos.getZ() + 0.5;
                        for (VisitorEntity other : visitor.level().getEntitiesOfClass(VisitorEntity.class,
                                new net.minecraft.world.phys.AABB(targetPos).inflate(1.5D))) {
                            if (other != visitor && other.getMood() == Mood.TOILET
                                    && other.distanceToSqr(cx, cy, cz) < 2.25D
                                    && other.getNavigation().isDone()) {
                                occupied = true;
                                break;
                            }
                        }
                        if (!occupied) {
                            stage = Stage.USING;
                            usingTicks = 80;
                            return;
                        } else {
                            timer = 20;
                            return;
                        }
                    }

                    if (type.equals("food") || type.equals("drink")) {
                        visitor.setHasTrash(true);
                    }
                    
                    // Pay for service (food/drink only) via FoodTransactionManager
                    if (!type.equals("toilet") && !visitor.level().isClientSide) {
                        net.minecraft.world.level.block.entity.BlockEntity be = visitor.level().getBlockEntity(targetPos);
                        if (be instanceof ShelfBlockEntity shelf) {
                            if (type.equals("food")) {
                                FoodTransactionManager.processFoodPurchase(visitor, shelf);
                            } else if (type.equals("drink")) {
                                FoodTransactionManager.processDrinkPurchase(visitor, shelf);
                            }
                        } else {
                            // Fallback: basic transaction if shelf not found
                            ZooData.get(visitor.level()).addBalance(15);
                            visitor.playSound(net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
                        }
                    }

                    targetPos = null; // Done
                }
            } else {
                // Keep moving if path lost
                repathIfNeeded(targetPos);
            }
        }

        @Override
        public boolean canContinueToUse() {
            net.minecraft.world.level.block.Block targetBlock;
            if (type.equals("food"))
                targetBlock = IndoZooTycoon.FOOD_STALL_BLOCK.get();
            else if (type.equals("drink"))
                targetBlock = IndoZooTycoon.DRINK_STALL_BLOCK.get();
            else
                targetBlock = IndoZooTycoon.RESTROOM_BLOCK.get();

            return targetPos != null && visitor.level().getBlockState(targetPos).is(targetBlock);
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
            if (visitor.getNavigation().isDone() || visitor.getNavigation().isStuck() || visitor.tickCount % 20 == 0) {
                moveTo(pos);
                repathCooldown = 10;
            }
        }

        private void openDoorAt(BlockPos pos) {
            net.minecraft.world.level.block.state.BlockState state = visitor.level().getBlockState(pos);
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
            BlockPos start = visitor.blockPosition();
            for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -2, -radius), center.offset(radius, 2, radius))) {
                net.minecraft.world.level.block.state.BlockState st = visitor.level().getBlockState(pos);
                if (!(st.getBlock() instanceof net.minecraft.world.level.block.DoorBlock)
                        && !(st.getBlock() instanceof net.minecraft.world.level.block.FenceGateBlock)) {
                    continue;
                }
                net.minecraft.world.level.pathfinder.Path path = visitor.getNavigation().createPath(pos, 0);
                if (path == null) continue;
                double d = pos.distSqr(start);
                if (d < bestDist) {
                    bestDist = d;
                    best = pos.immutable();
                }
            }
            return best;
        }
    }

    static class WatchAnimalsGoal extends Goal {
        private final VisitorEntity visitor;
        private Animal target;
        private int watchTimer;
        private BlockPos watchPos;

        public WatchAnimalsGoal(VisitorEntity v) {
            this.visitor = v;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (visitor.isHunter()) return false;
            if (visitor.isTimeToLeave()) return false;
            if (visitor.tickCount % 40 != 0) return false;
            ZooData data = ZooData.get(visitor.level());
            if (data.getTaggedAnimals().isEmpty()) return false;

            Animal candidate = findTarget(data);
            if (candidate != null) {
                target = candidate;
                return true;
            }
            return false;
        }

        @Override
        public void start() {
            int base = 160 + visitor.getRandom().nextInt(120);
            if (target != null) {
                double d = Math.sqrt(visitor.distanceToSqr(target));
                watchTimer = base + Math.min(600, (int) (d * 8));
                if (d > 35) {
                    visitor.setMood(Mood.AMAZED, 120);
                }
            } else {
                watchTimer = base;
            }
            if (target != null) {
                ZooData data = ZooData.get(visitor.level());
                if (visitor.watchingAnimalId != -1 && visitor.watchingAnimalId != target.getId()) {
                    data.decrementViewer(visitor.watchingAnimalId);
                }
                visitor.watchingAnimalId = target.getId();
                data.incrementViewer(target.getId());
                watchPos = computeWatchPos(target, false);
                LOGGER.info("Visitor {} watching target {}", visitor.getId(), target.getId());
            }
        }

        @Override
        public void stop() {
            if (visitor.watchingAnimalId != -1) {
                ZooData.get(visitor.level()).decrementViewer(visitor.watchingAnimalId);
            }
            visitor.watchingAnimalId = -1;
            target = null;
            watchPos = null;
        }

        @Override
        public void tick() {
            if (target == null || !target.isAlive() || !isTaggedAnimal(target.getId())) {
                watchTimer = 0;
                return;
            }

            boolean crowded = visitor.level().getEntitiesOfClass(VisitorEntity.class,
                    target.getBoundingBox().inflate(2.0D), v -> !v.isHunter()).size() > MAX_VIEWERS_PER_ANIMAL;
            if (watchPos == null || crowded || visitor.distanceToSqr(watchPos.getX() + 0.5, watchPos.getY(), watchPos.getZ() + 0.5) < 2.0D) {
                watchPos = computeWatchPos(target, crowded);
            }

            if (watchPos != null) {
                double dist = visitor.distanceToSqr(watchPos.getX() + 0.5, watchPos.getY(), watchPos.getZ() + 0.5);
                if (dist > 4.0D || visitor.getNavigation().isDone()) {
                    visitor.getNavigation().moveTo(watchPos.getX() + 0.5, watchPos.getY(), watchPos.getZ() + 0.5, 1.0D);
                }
            }
            visitor.getLookControl().setLookAt(target, 30, 30);
            if (watchTimer > 0) {
                watchTimer--;
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

        @Override
        public boolean canContinueToUse() {
            return watchTimer > 0 && target != null && target.isAlive() && isTaggedAnimal(target.getId());
        }

        private Animal findTarget(ZooData data) {
            net.minecraft.nbt.ListTag tagged = data.getTaggedAnimals();
            java.util.List<Animal> candidates = new java.util.ArrayList<>();
            int minViewers = Integer.MAX_VALUE;
            int minBelowMax = Integer.MAX_VALUE;
            boolean foundBelowMax = false;

            for (int i = 0; i < tagged.size(); i++) {
                int id = tagged.getCompound(i).getInt("id");
                net.minecraft.world.entity.Entity entity = visitor.level().getEntity(id);
                if (entity instanceof Animal animal && animal.isAlive()) {
                    int viewers = data.getViewerCount(id);
                    if (viewers < MAX_VIEWERS_PER_ANIMAL) {
                        if (!foundBelowMax || viewers < minBelowMax) {
                            foundBelowMax = true;
                            minBelowMax = viewers;
                            candidates.clear();
                            candidates.add(animal);
                        } else if (viewers == minBelowMax) {
                            candidates.add(animal);
                        }
                    } else if (!foundBelowMax) {
                        if (viewers < minViewers) {
                            minViewers = viewers;
                            candidates.clear();
                            candidates.add(animal);
                        } else if (viewers == minViewers) {
                            candidates.add(animal);
                        }
                    }
                }
            }

            if (candidates.isEmpty()) return null;
            Animal nearest = null;
            double best = Double.MAX_VALUE;
            for (Animal animal : candidates) {
                double dist = visitor.distanceToSqr(animal);
                if (dist < best) {
                    best = dist;
                    nearest = animal;
                }
            }
            return nearest;
        }

        private boolean isTaggedAnimal(int id) {
            net.minecraft.nbt.ListTag tagged = ZooData.get(visitor.level()).getTaggedAnimals();
            for (int i = 0; i < tagged.size(); i++) {
                if (tagged.getCompound(i).getInt("id") == id) {
                    return true;
                }
            }
            return false;
        }

        private BlockPos computeWatchPos(Animal target, boolean crowded) {
            double radius = crowded ? 4.5D : 3.5D;
            double angle = ((visitor.getId() * 31) + (target.getId() * 17)) % 360;
            double rad = Math.toRadians(angle);
            int dx = (int) Math.round(Math.cos(rad) * radius);
            int dz = (int) Math.round(Math.sin(rad) * radius);
            return target.blockPosition().offset(dx, 0, dz);
        }
    }

    private class FindTrashCanGoal extends Goal {
        private final VisitorEntity visitor;
        private BlockPos targetPos;
        private int timer;
        private int searchTimer;

        public FindTrashCanGoal(VisitorEntity v) {
            this.visitor = v;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (visitor.isHunter()) return false;
            if (!visitor.hasTrash()) return false;
            
            // Scan for trash can
            BlockPos p = visitor.blockPosition();
            for (BlockPos pos : BlockPos.betweenClosed(p.offset(-15, -3, -15), p.offset(15, 3, 15))) {
                if (visitor.level().getBlockState(pos).is(IndoZooTycoon.TRASH_CAN_BLOCK.get())) {
                    this.targetPos = pos.immutable();
                    this.searchTimer = 0;
                    return true;
                }
            }
            
            // If no trash can found, increment search timer
            this.searchTimer++;
            if (this.searchTimer > 300) { // Approx 15 seconds
                this.targetPos = null;
                return true; // Return true to trigger littering in start() or tick()
            }
            
            return false;
        }

        @Override
        public void start() {
            if (targetPos != null) {
                visitor.getNavigation().moveTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 1.0D);
            }
            timer = 0;
        }

        @Override
        public void tick() {
            if (targetPos != null) {
                if (visitor.distanceToSqr(targetPos.getX(), targetPos.getY(), targetPos.getZ()) < 4.0D) {
                    visitor.getNavigation().stop();
                    timer++;
                    
                    // Interaction Animation
                    if (timer == 2) {
                        BlockState state = visitor.level().getBlockState(targetPos);
                        if (state.is(IndoZooTycoon.TRASH_CAN_BLOCK.get())) {
                            visitor.level().setBlockAndUpdate(targetPos, state.setValue(TrashCanBlock.OPEN, true));
                            visitor.playSound(net.minecraft.sounds.SoundEvents.CHEST_OPEN, 0.5F, 0.8F);
                        }
                    }

                    if (timer > 20) {
                        visitor.setHasTrash(false);
                        visitor.playSound(net.minecraft.sounds.SoundEvents.ITEM_PICKUP, 1.0F, 1.0F);
                        
                        // Close it
                        BlockState state = visitor.level().getBlockState(targetPos);
                        if (state.is(IndoZooTycoon.TRASH_CAN_BLOCK.get())) {
                            visitor.level().setBlockAndUpdate(targetPos, state.setValue(TrashCanBlock.OPEN, false));
                            visitor.playSound(net.minecraft.sounds.SoundEvents.CHEST_CLOSE, 0.5F, 0.8F);
                        }

                        targetPos = null;
                        searchTimer = 0;
                    }
                } else if (visitor.getNavigation().isDone()) {
                    visitor.getNavigation().moveTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 1.0D);
                }
            } else {
                // No target can found -> Litter!
                BlockPos feet = visitor.blockPosition();
                if (visitor.level().isEmptyBlock(feet)) {
                    visitor.level().setBlockAndUpdate(feet, IndoZooTycoon.TRASH_BLOCK.get().defaultBlockState()
                        .setValue(TrashBlock.VARIANT, visitor.getRandom().nextInt(4)));
                    visitor.setHasTrash(false);
                    this.searchTimer = 0;
                    
                    // Rating Penalty
                    if (!visitor.level().isClientSide) {
                        ZooData data = ZooData.get(visitor.level());
                        if (data.getRating() > 0) {
                            data.setRating(data.getRating() - 1);
                        }
                    }
                    visitor.playSound(net.minecraft.sounds.SoundEvents.CHICKEN_STEP, 1.0F, 0.8F);
                }
            }
        }

        @Override
        public boolean canContinueToUse() {
            if (visitor.isHunter()) return false;
            if (targetPos == null && visitor.hasTrash()) return true; // Finish littering
            return targetPos != null && visitor.hasTrash() && visitor.level().getBlockState(targetPos).is(IndoZooTycoon.TRASH_CAN_BLOCK.get());
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
            return !visitor.isHunter() && visitor.isTimeToLeave() && visitor.getGatePos() != null;
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
            double speed = visitor.isCarrying() ? 1.4D : 1.0D;
            
            if (visitor.getNavigation().isDone() || visitor.tickCount % 20 == 0) {
                visitor.getNavigation().moveTo(gate.getX(), gate.getY(), gate.getZ(), speed);
            }

            if (visitor.distanceToSqr(gate.getX(), gate.getY(), gate.getZ()) < 12) {
                if (!visitor.level().isClientSide) {
                    ZooData data = ZooData.get(visitor.level());
                    if (data.getVisitorCount() > 0) {
                        data.setVisitorCount(data.getVisitorCount() - 1);

                        // Rating Adjustment
                        if (!visitor.isCarrying()) {
                            Mood mood = visitor.getMood();
                            if (mood == Mood.HAPPY || mood == Mood.AMAZED || mood == Mood.ADORED) {
                                if (visitor.getRandom().nextFloat() < 0.5f) { // 50% chance to increase
                                    data.setRating(data.getRating() + 1);
                                }
                            }
                        } else {
                            // Heist! Decreases rating
                            data.setRating(Math.max(0, data.getRating() - 2));
                            for (net.minecraft.world.entity.Entity passenger : visitor.getPassengers()) {
                                passenger.discard(); // Despawn stolen animal
                            }
                        }

                        SyncBalancePacket packet = new SyncBalancePacket(data.getBalance(), data.getTaggedAnimals(),
                                data.getAnimalCount(), data.getStaffCount(), data.getVisitorCount(), data.getRating());
                        PacketHandler.INSTANCE.send(net.minecraftforge.network.PacketDistributor.ALL.noArg(), packet);
                    }
                }
                visitor.discard();
            }
        }
    }
}

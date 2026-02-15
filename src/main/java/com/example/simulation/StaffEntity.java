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
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;

public class StaffEntity extends PathfinderMob {
    // 0 = Janitor, 1 = Zookeeper
    private static final EntityDataAccessor<Integer> ROLE = SynchedEntityData.defineId(StaffEntity.class,
            EntityDataSerializers.INT);
    // 0 = Idle, 1 = Sweeping, 2 = Greeting, 3 = Explaining
    private static final EntityDataAccessor<Integer> ANIM_STATE = SynchedEntityData.defineId(StaffEntity.class,
            EntityDataSerializers.INT);

    private int foodStock = 0;
    private BlockPos homePos = BlockPos.ZERO;
    private static final int MAX_HOME_DISTANCE = 30;

    public StaffEntity(EntityType<? extends StaffEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ROLE, 0);
        this.entityData.define(ANIM_STATE, 0);
    }

    public int getRole() {
        return this.entityData.get(ROLE);
    }

    public void setRole(int role) {
        this.entityData.set(ROLE, role);
    }

    public int getAnimState() {
        return this.entityData.get(ANIM_STATE);
    }

    public void setAnimState(int state) {
        this.entityData.set(ANIM_STATE, state);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
            @Nullable SpawnGroupData spawnData, @Nullable CompoundTag dataTag) {
        // Randomly assign role (50/50 for now)
        this.setRole(this.getRandom().nextBoolean() ? 1 : 0);
        return super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
    }

    // ... rest of logic ...

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.35D)
                .add(Attributes.FOLLOW_RANGE, 48.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new OpenDoorGoal(this, true));

        // Role 0 (Janitor)
        this.goalSelector.addGoal(2, new RideVehicleGoal(this));
        this.goalSelector.addGoal(3, new CleanTrashGoal(this));

        // Role 1 (Zookeeper)
        this.goalSelector.addGoal(2, new InteractVisitorGoal(this));
        // Feeding removed as per user request "hanya menjelaskan"

        this.goalSelector.addGoal(5, new RandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    // --- DRIVING LOGIC ---
    @Override
    public void tick() {
        super.tick();

        // Driving Logic for Janitors
        if (this.isPassenger() && this.getVehicle() != null && getRole() == 0) {
            net.minecraft.world.entity.Entity vehicle = this.getVehicle();
            if (isDriveable(vehicle)) {
                // Force rotation to match staff
                vehicle.setYRot(this.getYRot());

                // Simulate Player Input (Pressing W)
                this.setZza(1.0F);
                this.yBodyRot = this.getYRot();

                // If vehicle is LivingEntity, this might be enough.
                // If not, apply manual velocity.

                // Simple cruise control
                if (this.horizontalCollision) {
                    this.setYRot(this.getYRot() + 180);
                } else if (this.random.nextInt(40) == 0) {
                    this.setYRot(this.getYRot() + (this.random.nextInt(60) - 30));
                }

                // Manual fallback for non-living vehicles
                float speed = 0.3F;
                var look = this.calculateViewVector(0, this.getYRot());
                vehicle.setDeltaMovement(look.x * speed, vehicle.getDeltaMovement().y, look.z * speed);
            }
        }
    }

    private boolean isDriveable(net.minecraft.world.entity.Entity e) {
        if (e == null)
            return false;
        String id = net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES.getKey(e.getType()).toString()
                .toLowerCase();
        String name = e.getDisplayName().getString().toLowerCase();
        // Expanded list
        return (id.contains("cart") || id.contains("vehicle") || id.contains("jeep") || id.contains("atv")
                || id.contains("car") || id.contains("rover") || id.contains("scooter")
                || name.contains("cart") || name.contains("mobil") || name.contains("vehicle")
                || name.contains("motor"));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("FoodStock", foodStock);
        tag.putLong("HomePos", homePos.asLong());
        tag.putInt("Role", getRole());
        tag.putInt("AnimState", getAnimState());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        foodStock = tag.getInt("FoodStock");
        if (tag.contains("HomePos")) {
            homePos = BlockPos.of(tag.getLong("HomePos"));
        }
        if (tag.contains("Role")) {
            setRole(tag.getInt("Role"));
        }
        if (tag.contains("AnimState")) {
            setAnimState(tag.getInt("AnimState"));
        }
    }

    // --- INNER GOALS ---

    static class RideVehicleGoal extends Goal {
        private final StaffEntity staff;
        private net.minecraft.world.entity.Entity targetVehicle;

        public RideVehicleGoal(StaffEntity staff) {
            this.staff = staff;
            this.setFlags(java.util.EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            // Only Janitor drives
            if (staff.getRole() != 0)
                return false;
            // Don't switch if already riding
            if (staff.isPassenger())
                return false;
            // Chance to look for car
            if (staff.getRandom().nextFloat() > 0.02F)
                return false;

            // Find nearby vehicle
            java.util.List<net.minecraft.world.entity.Entity> vehicles = staff.level().getEntities(staff,
                    staff.getBoundingBox().inflate(10), e -> staff.isDriveable(e) && !e.isVehicle());

            if (!vehicles.isEmpty()) {
                // Pick closest
                vehicles.sort(java.util.Comparator.comparingDouble(staff::distanceToSqr));
                this.targetVehicle = vehicles.get(0);
                return true;
            }
            return false;
        }

        @Override
        public void start() {
            staff.getNavigation().moveTo(targetVehicle, 1.2D);
        }

        @Override
        public void tick() {
            if (targetVehicle != null && !targetVehicle.isRemoved()) {
                if (staff.distanceToSqr(targetVehicle) < 4.0D) {
                    staff.startRiding(targetVehicle);
                    staff.getNavigation().stop();
                } else {
                    staff.getNavigation().moveTo(targetVehicle, 1.2D);
                }
            }
        }

        @Override
        public boolean canContinueToUse() {
            return targetVehicle != null && !targetVehicle.isRemoved() && !targetVehicle.isVehicle()
                    && !staff.isPassenger();
        }
    }

    static class CleanTrashGoal extends Goal {
        private final StaffEntity staff;
        private BlockPos targetPos;
        private int workTimer;

        public CleanTrashGoal(StaffEntity staff) {
            this.staff = staff;
            this.setFlags(java.util.EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            // Only Janitor (Role 0) cleans, check every few ticks
            if (staff.getRole() != 0 || staff.getRandom().nextFloat() > 0.02F)
                return false;

            // Simple scan for trash nearby (radius 10)
            BlockPos p = staff.blockPosition();
            for (BlockPos pos : BlockPos.betweenClosed(p.offset(-10, -2, -10), p.offset(10, 2, 10))) {
                if (staff.level().getBlockState(pos).is(IndoZooTycoon.TRASH_BLOCK.get())) {
                    this.targetPos = pos.immutable();
                    return true;
                }
            }
            return false;
        }

        @Override
        public void start() {
            // Updated: Dismount if riding to clean
            if (staff.isPassenger()) {
                staff.stopRiding();
            }

            staff.getNavigation().moveTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 1.0D);
            workTimer = 0;
            staff.setAnimState(0);
        }

        @Override
        public void stop() {
            staff.setAnimState(0);
            targetPos = null;
        }

        @Override
        public void tick() {
            if (targetPos == null)
                return;

            double dist = staff.distanceToSqr(targetPos.getX(), targetPos.getY(), targetPos.getZ());
            if (dist < 5.0D) {
                // Arrived at trash
                staff.getNavigation().stop();
                staff.setAnimState(1); // Sweeping animation

                workTimer++;
                if (workTimer > 60) { // 3 seconds to clean
                    staff.level().destroyBlock(targetPos, false);
                    staff.setAnimState(0);
                    targetPos = null; // Done
                }
            } else {
                // Keep moving to trash
                if (staff.getNavigation().isDone()) {
                    staff.getNavigation().moveTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 1.0D);
                }
            }
        }

        @Override
        public boolean canContinueToUse() {
            return targetPos != null && staff.level().getBlockState(targetPos).is(IndoZooTycoon.TRASH_BLOCK.get());
        }
    }

    static class RefillFeederGoal extends Goal {
        private final StaffEntity staff;
        private BlockPos targetPos;
        private int workTimer;

        public RefillFeederGoal(StaffEntity staff) {
            this.staff = staff;
            this.setFlags(java.util.EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            // Only Zookeeper (Role 1) refills feeders
            if (staff.getRole() != 1 || staff.getRandom().nextFloat() > 0.05F)
                return false;

            BlockPos p = staff.blockPosition();
            for (BlockPos pos : BlockPos.betweenClosed(p.offset(-15, -3, -15), p.offset(15, 3, 15))) {
                if (staff.level().getBlockState(pos).is(IndoZooTycoon.ANIMAL_FEEDER_BLOCK.get())) {
                    net.minecraft.world.level.block.entity.BlockEntity be = staff.level().getBlockEntity(pos);
                    if (be instanceof AnimalFeederBlockEntity feeder && !feeder.isFull()) {
                        this.targetPos = pos.immutable();
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public void start() {
            staff.getNavigation().moveTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 1.0D);
            workTimer = 0;
            staff.setAnimState(0);
            // Visual: Hold "Food" (Hay Block)
            staff.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,
                    new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.HAY_BLOCK));
        }

        @Override
        public void stop() {
            staff.setAnimState(0);
            staff.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,
                    net.minecraft.world.item.ItemStack.EMPTY);
            targetPos = null;
        }

        @Override
        public void tick() {
            if (targetPos == null)
                return;

            if (staff.distanceToSqr(targetPos.getX(), targetPos.getY(), targetPos.getZ()) < 5.0D) {
                staff.getNavigation().stop();
                // Change: Don't use Explaining animation (3) for refill. Just swing arm.
                staff.setAnimState(0);

                if (staff.tickCount % 10 == 0) {
                    staff.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
                }

                workTimer++;
                if (workTimer > 40) { // 2 seconds to refill
                    net.minecraft.world.level.block.entity.BlockEntity be = staff.level().getBlockEntity(targetPos);
                    if (be instanceof AnimalFeederBlockEntity feeder) {
                        feeder.addFood(50); // Add 50% food
                    }
                    // Stop will be called by canContinueToUse returning false?
                    // No, we need to clear TargetPos to stop continuing.
                    targetPos = null;
                }
            }
        }

        @Override
        public boolean canContinueToUse() {
            return targetPos != null;
        }
    }

    static class InteractVisitorGoal extends Goal {
        private final StaffEntity staff;
        private VisitorEntity targetVisitor;
        private int timer;

        public InteractVisitorGoal(StaffEntity staff) {
            this.staff = staff;
            this.setFlags(java.util.EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            // Only Zookeeper (Role 1) explains
            if (staff.getRole() != 1)
                return false;

            if (staff.getRandom().nextFloat() < 0.01F) { // Check 1% per tick
                VisitorEntity nearby = staff.level().getNearestEntity(VisitorEntity.class,
                        TargetingConditions.forNonCombat().range(4.0D).selector(entity -> {
                            return entity.hasLineOfSight(staff);
                        }), staff,
                        staff.getX(), staff.getY(), staff.getZ(),
                        staff.getBoundingBox().inflate(4.0D));

                if (nearby != null) {
                    this.targetVisitor = nearby;
                    return true;
                }
            }
            return false;
        }

        @Override
        public void start() {
            timer = 60; // 3 seconds interaction
            staff.getNavigation().stop();
            if (staff.getRole() == 0) {
                staff.setAnimState(2); // Janitor -> Greet (2)
            } else {
                staff.setAnimState(3); // Zookeeper -> Explain (3)
            }
        }

        @Override
        public void stop() {
            staff.setAnimState(0);
            targetVisitor = null;
        }

        @Override
        public void tick() {
            if (targetVisitor != null) {
                staff.getLookControl().setLookAt(targetVisitor, 30.0F, 30.0F);
                // Visitor also looks at staff
                targetVisitor.getLookControl().setLookAt(staff, 30.0F, 30.0F);
            }
            timer--;
        }

        @Override
        public boolean canContinueToUse() {
            return timer > 0 && targetVisitor != null && targetVisitor.isAlive()
                    && staff.distanceToSqr(targetVisitor) < 25.0D; // 5 blocks
        }
    }

    public int getFoodStock() {
        return foodStock;
    }

    public void setFoodStock(int amount) {
        this.foodStock = amount;
    }

    public boolean hasFood() {
        return foodStock > 0;
    }

    public void consumeOneFood() {
        if (foodStock > 0)
            foodStock--;
    }

    public void setHomePos(BlockPos pos, int radius) {
        this.homePos = pos;
        this.restrictTo(pos, radius);
    }

    // Overload for backward compatibility or default
    public void setHomePos(BlockPos pos) {
        setHomePos(pos, MAX_HOME_DISTANCE);
    }
}

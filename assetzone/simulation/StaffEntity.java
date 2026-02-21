package com.example.simulation;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public class StaffEntity extends PathfinderMob {
    // 0 = Janitor, 1 = Zookeeper, 2 = Security
    private static final EntityDataAccessor<Integer> ROLE = SynchedEntityData.defineId(StaffEntity.class,
            EntityDataSerializers.INT);
    // 0 = Idle, 1 = Sweeping, 2 = Greeting, 3 = Explaining
    private static final EntityDataAccessor<Integer> ANIM_STATE = SynchedEntityData.defineId(StaffEntity.class,
            EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> STAFF_NAME = SynchedEntityData.defineId(StaffEntity.class,
            EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> ASSIGNED_ANIMAL_ID = SynchedEntityData.defineId(StaffEntity.class,
            EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> ASSIGNED_FOOD_ID = SynchedEntityData.defineId(StaffEntity.class,
            EntityDataSerializers.STRING);

    // AI Coordination: Claimed trash positions to prevent Janitors from clumping
    private static final java.util.Set<BlockPos> CLAIMED_TRASH = java.util.Collections.synchronizedSet(new java.util.HashSet<>());

    private int foodStock = 0;
    private BlockPos homePos = BlockPos.ZERO;
    private BlockPos gatePos = null;
    private boolean isPermanent = true;
    private int lifeTicks = 0;
    private static final int MAX_HOME_DISTANCE = 30;

    public StaffEntity(EntityType<? extends StaffEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ROLE, 0);
        this.entityData.define(ANIM_STATE, 0);
        this.entityData.define(STAFF_NAME, "Staff");
        this.entityData.define(ASSIGNED_ANIMAL_ID, "");
        this.entityData.define(ASSIGNED_FOOD_ID, "");
    }

    public int getRole() {
        return this.entityData.get(ROLE);
    }

    public void setRole(int role) {
        this.entityData.set(ROLE, role);
        if (role == 2) {
            this.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_SWORD));
        } else {
            this.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, net.minecraft.world.item.ItemStack.EMPTY);
        }
        if ("Staff".equals(getStaffName()) || getStaffName().isEmpty()) {
            this.setStaffName(role == 1 ? randomZookeeperName() : (role == 2 ? randomSecurityName() : randomJanitorName()));
        }
    }

    public int getAnimState() {
        return this.entityData.get(ANIM_STATE);
    }

    public void setAnimState(int state) {
        this.entityData.set(ANIM_STATE, state);
    }

    public String getStaffName() {
        return this.entityData.get(STAFF_NAME);
    }

    public void setStaffName(String name) {
        this.entityData.set(STAFF_NAME, name);
    }

    public String getAssignedAnimalId() {
        return this.entityData.get(ASSIGNED_ANIMAL_ID);
    }

    public void setAssignedAnimalId(String animalId) {
        this.entityData.set(ASSIGNED_ANIMAL_ID, animalId == null ? "" : animalId);
    }

    public String getAssignedFoodId() {
        return this.entityData.get(ASSIGNED_FOOD_ID);
    }

    public void setAssignedFoodId(String foodId) {
        this.entityData.set(ASSIGNED_FOOD_ID, foodId == null ? "" : foodId);
    }

    public void setGatePos(BlockPos pos) {
        this.gatePos = pos;
    }

    public void setPermanent(boolean permanent) {
        this.isPermanent = permanent;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
            @Nullable SpawnGroupData spawnData, @Nullable CompoundTag dataTag) {
        // Jika spawn alami/telur, acak role. Jika command, biasanya role di-set setelah spawn.
        if (dataTag != null && dataTag.contains("Role")) {
            this.setRole(dataTag.getInt("Role"));
        } else if (reason == MobSpawnType.NATURAL || reason == MobSpawnType.SPAWN_EGG) {
            this.setRole(this.getRandom().nextInt(3));
        }
        if (getStaffName().isEmpty() || "Staff".equals(getStaffName())) {
            this.setStaffName(getRole() == 1 ? randomZookeeperName() : (getRole() == 2 ? randomSecurityName() : randomJanitorName()));
        }
        return super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.35D)
                .add(Attributes.FOLLOW_RANGE, 48.0D)
                .add(Attributes.ATTACK_DAMAGE, 2.0D);
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
        this.goalSelector.addGoal(3, new ThrowFoodToHungryAnimalGoal(this));
        this.goalSelector.addGoal(4, new RefillFeederGoal(this));

        // Role 2 (Security) - Melee Attack
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2D, false) {
            @Override
            public boolean canUse() {
                return super.canUse() && StaffEntity.this.getRole() == 2;
            }
        });

        // Target Goal: Security only targets Monsters & Hunters
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, net.minecraft.world.entity.monster.Monster.class, 10, true, false, null) {
            @Override
            public boolean canUse() {
                return super.canUse() && StaffEntity.this.getRole() == 2;
            }
        });

        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, VisitorEntity.class, 10, true, false, (target) -> {
            return target instanceof VisitorEntity v && v.isHunter() && v.hasActiveHunterCrime();
        }) {
            @Override
            public boolean canUse() {
                return super.canUse() && StaffEntity.this.getRole() == 2;
            }
        });

        this.goalSelector.addGoal(5, new RandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    // --- DRIVING LOGIC ---
    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            lifeTicks++;
            if (getRole() == 1 && this.tickCount % 40 == 0) {
                ensureZookeeperSpecialization();
            }
            // Non-permanent staff (like spawned security) leave after 5 minutes (6000 ticks)
            if (!isPermanent && gatePos != null && lifeTicks > 6000) {
                this.getNavigation().moveTo(gatePos.getX(), gatePos.getY(), gatePos.getZ(), 1.0D);
                if (this.distanceToSqr(gatePos.getX(), gatePos.getY(), gatePos.getZ()) < 10) {
                    this.discard();
                }
            }

            if (getRole() == 2 && gatePos != null && this.getTarget() == null) {
                double dist = this.distanceToSqr(gatePos.getX(), gatePos.getY(), gatePos.getZ());
                if (dist > 36 && this.tickCount % 40 == 0 && this.getNavigation().isDone()) {
                    this.getNavigation().moveTo(gatePos.getX(), gatePos.getY(), gatePos.getZ(), 1.0D);
                }
            }
        }

        // Driving Logic for Janitors
        if (this.isPassenger() && this.getVehicle() != null && getRole() == 0) {
            net.minecraft.world.entity.Entity vehicle = this.getVehicle();
            if (isDriveable(vehicle)) {
                vehicle.setYRot(this.getYRot());
                this.setZza(1.0F);
                this.yBodyRot = this.getYRot();

                if (this.horizontalCollision) {
                    this.setYRot(this.getYRot() + 180);
                } else if (this.random.nextInt(40) == 0) {
                    this.setYRot(this.getYRot() + (this.random.nextInt(60) - 30));
                }

                float speed = 0.3F;
                var look = this.calculateViewVector(0, this.getYRot());
                vehicle.setDeltaMovement(look.x * speed, vehicle.getDeltaMovement().y, look.z * speed);
            }
        }
    }

    private boolean isDriveable(net.minecraft.world.entity.Entity e) {
        if (e == null) return false;
        String id = net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES.getKey(e.getType()).toString().toLowerCase();
        String name = e.getDisplayName().getString().toLowerCase();
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
        if (gatePos != null) tag.putLong("GatePos", gatePos.asLong());
        tag.putBoolean("IsPermanent", isPermanent);
        tag.putInt("LifeTicks", lifeTicks);
        tag.putInt("Role", getRole());
        tag.putInt("AnimState", getAnimState());
        tag.putString("StaffName", getStaffName());
        tag.putString("AssignedAnimalId", getAssignedAnimalId());
        tag.putString("AssignedFoodId", getAssignedFoodId());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        foodStock = tag.getInt("FoodStock");
        if (tag.contains("HomePos")) {
            homePos = BlockPos.of(tag.getLong("HomePos"));
        }
        if (tag.contains("GatePos")) {
            gatePos = BlockPos.of(tag.getLong("GatePos"));
        }
        if (tag.contains("IsPermanent")) {
            isPermanent = tag.getBoolean("IsPermanent");
        }
        if (tag.contains("LifeTicks")) {
            lifeTicks = tag.getInt("LifeTicks");
        }
        if (tag.contains("Role")) {
            setRole(tag.getInt("Role"));
        }
        if (tag.contains("AnimState")) {
            setAnimState(tag.getInt("AnimState"));
        }
        if (tag.contains("StaffName")) {
            setStaffName(tag.getString("StaffName"));
        }
        if (tag.contains("AssignedAnimalId")) {
            setAssignedAnimalId(tag.getString("AssignedAnimalId"));
        }
        if (tag.contains("AssignedFoodId")) {
            setAssignedFoodId(tag.getString("AssignedFoodId"));
        }
    }

    private void ensureZookeeperSpecialization() {
        if (getRole() != 1 || this.level().isClientSide) return;
        if (!getAssignedAnimalId().isEmpty() && !getAssignedFoodId().isEmpty()) return;

        ZooData data = ZooData.get(this.level());
        net.minecraft.nbt.ListTag tagged = data.getTaggedAnimals();
        String pickedAnimal = "";

        java.util.Set<String> alreadyAssigned = new java.util.HashSet<>();
        for (StaffEntity other : this.level().getEntitiesOfClass(StaffEntity.class, this.getBoundingBox().inflate(128.0D))) {
            if (other == this) continue;
            if (other.getRole() != 1) continue;
            if (!other.getAssignedAnimalId().isEmpty()) {
                alreadyAssigned.add(other.getAssignedAnimalId());
            }
        }

        for (int i = 0; i < tagged.size(); i++) {
            CompoundTag t = tagged.getCompound(i);
            String type = t.getString("type");
            if (type == null || type.isEmpty()) continue;
            if (!alreadyAssigned.contains(type) && !FoodAnimalRegistry.getFoodsForAnimal(type).isEmpty()) {
                pickedAnimal = type;
                break;
            }
        }

        if (pickedAnimal.isEmpty()) return;
        setAssignedAnimalId(pickedAnimal);
        List<net.minecraft.world.item.Item> foods = FoodAnimalRegistry.getFoodsForAnimal(pickedAnimal);
        if (!foods.isEmpty()) {
            net.minecraft.resources.ResourceLocation foodId = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(foods.get(0));
            if (foodId != null) setAssignedFoodId(foodId.toString());
        }
    }

    private static String randomJanitorName() {
        String[] names = {"Rudi", "Fajar", "Tono", "Asep", "Lukman", "Yanto"};
        return names[(int) (Math.random() * names.length)];
    }

    private static String randomZookeeperName() {
        String[] names = {"Budi", "Siti", "Ahmad", "Dewi", "Hendra", "Ratna", "Anto", "Rina"};
        return names[(int) (Math.random() * names.length)];
    }

    private static String randomSecurityName() {
        String[] names = {"Agus", "Tejo", "Bram", "Dika", "Eko", "Joko", "Ridho", "Wahyu"};
        return names[(int) (Math.random() * names.length)];
    }

    // --- INNER GOALS ---
    static class RideVehicleGoal extends Goal {
        private final StaffEntity staff;
        private net.minecraft.world.entity.Entity targetVehicle;
        public RideVehicleGoal(StaffEntity staff) { this.staff = staff; this.setFlags(java.util.EnumSet.of(Goal.Flag.MOVE)); }
        @Override public boolean canUse() {
            if (staff.getRole() != 0) return false;
            if (staff.isPassenger() || staff.getRandom().nextFloat() > 0.02F) return false;
            java.util.List<net.minecraft.world.entity.Entity> vehicles = staff.level().getEntities(staff, staff.getBoundingBox().inflate(10), e -> staff.isDriveable(e) && !e.isVehicle());
            if (!vehicles.isEmpty()) { vehicles.sort(java.util.Comparator.comparingDouble(staff::distanceToSqr)); this.targetVehicle = vehicles.get(0); return true; }
            return false;
        }
        @Override public void start() { staff.getNavigation().moveTo(targetVehicle, 1.2D); }
        @Override public void tick() {
            if (targetVehicle != null && !targetVehicle.isRemoved()) {
                if (staff.distanceToSqr(targetVehicle) < 4.0D) { staff.startRiding(targetVehicle); staff.getNavigation().stop(); } 
                else { staff.getNavigation().moveTo(targetVehicle, 1.2D); }
            }
        }
        @Override public boolean canContinueToUse() { return targetVehicle != null && !targetVehicle.isRemoved() && !targetVehicle.isVehicle() && !staff.isPassenger(); }
    }

    static class CleanTrashGoal extends Goal {
        private final StaffEntity staff;
        private BlockPos targetPos;
        private int workTimer;
        public CleanTrashGoal(StaffEntity staff) { this.staff = staff; this.setFlags(java.util.EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK)); }
        @Override public boolean canUse() {
            if (staff.getRole() != 0 || staff.getRandom().nextFloat() > 0.05F) return false;
            BlockPos p = staff.blockPosition();
            for (BlockPos pos : BlockPos.betweenClosed(p.offset(-15, -3, -15), p.offset(15, 3, 15))) {
                if (staff.level().getBlockState(pos).is(IndoZooTycoon.TRASH_BLOCK.get())) { 
                    BlockPos immutablePos = pos.immutable();
                    if (!CLAIMED_TRASH.contains(immutablePos)) {
                        this.targetPos = immutablePos;
                        CLAIMED_TRASH.add(this.targetPos); // Claim it!
                        return true; 
                    }
                }
            }
            return false;
        }
        @Override public void start() { if (staff.isPassenger()) staff.stopRiding(); staff.getNavigation().moveTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 1.0D); workTimer = 0; staff.setAnimState(0); }
        @Override public void stop() { 
            if (targetPos != null) CLAIMED_TRASH.remove(targetPos); // Release claim
            staff.setAnimState(0); 
            targetPos = null; 
        }
        @Override public void tick() {
            if (targetPos == null) return;
            if (staff.distanceToSqr(targetPos.getX(), targetPos.getY(), targetPos.getZ()) < 4.0D) {
                staff.getNavigation().stop(); staff.setAnimState(1); workTimer++;
                if (workTimer > 60) { 
                    staff.level().destroyBlock(targetPos, false); 
                    staff.setAnimState(0); 
                    CLAIMED_TRASH.remove(targetPos);
                    targetPos = null; 
                }
            } else if (staff.getNavigation().isDone()) { staff.getNavigation().moveTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 1.0D); }
        }
        @Override public boolean canContinueToUse() { 
            return targetPos != null && staff.level().getBlockState(targetPos).is(IndoZooTycoon.TRASH_BLOCK.get()); 
        }
    }

    static class RefillFeederGoal extends Goal {
        private final StaffEntity staff;
        private BlockPos targetPos;
        private int workTimer;
        private static final int SPECIALIZATION_RADIUS = 10;
        public RefillFeederGoal(StaffEntity staff) { this.staff = staff; this.setFlags(java.util.EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK)); }
        @Override public boolean canUse() {
            if (staff.getRole() != 1 || staff.getRandom().nextFloat() > 0.05F) return false;
            if (staff.getAssignedAnimalId().isEmpty() || staff.getAssignedFoodId().isEmpty()) return false;
            net.minecraft.world.entity.Entity specializedAnimal = findSpecializedAnimal();
            if (specializedAnimal == null) return false;
            net.minecraft.resources.ResourceLocation foodId = net.minecraft.resources.ResourceLocation.tryParse(staff.getAssignedFoodId());
            net.minecraft.world.item.Item foodItem = foodId != null ? net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(foodId) : null;
            if (foodItem == null) return false;
            BlockPos p = staff.blockPosition();
            for (BlockPos pos : BlockPos.betweenClosed(p.offset(-15, -3, -15), p.offset(15, 3, 15))) {
                if (staff.level().getBlockState(pos).is(IndoZooTycoon.ANIMAL_FEEDER_BLOCK.get())) {
                    net.minecraft.world.level.block.entity.BlockEntity be = staff.level().getBlockEntity(pos);
                    if (!(be instanceof AnimalFeederBlockEntity feeder) || feeder.isFull()) continue;
                    if (specializedAnimal.blockPosition().distSqr(pos) > (SPECIALIZATION_RADIUS * SPECIALIZATION_RADIUS)) continue;
                    if (!feeder.canAcceptFood(foodItem, "", 1)) continue;
                    this.targetPos = pos.immutable();
                    return true;
                }
            }
            return false;
        }
        @Override public void start() {
            staff.getNavigation().moveTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 1.0D);
            workTimer = 0;
            staff.setAnimState(0);
            net.minecraft.resources.ResourceLocation foodId = net.minecraft.resources.ResourceLocation.tryParse(staff.getAssignedFoodId());
            net.minecraft.world.item.Item foodItem = foodId != null ? net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(foodId) : null;
            if (foodItem == null) foodItem = net.minecraft.world.item.Items.HAY_BLOCK;
            staff.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new net.minecraft.world.item.ItemStack(foodItem));
        }
        @Override public void stop() { staff.setAnimState(0); staff.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, net.minecraft.world.item.ItemStack.EMPTY); targetPos = null; }
        @Override public void tick() {
            if (targetPos == null) return;
            if (staff.distanceToSqr(targetPos.getX(), targetPos.getY(), targetPos.getZ()) < 5.0D) {
                staff.getNavigation().stop(); staff.setAnimState(0);
                if (staff.tickCount % 10 == 0) staff.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
                workTimer++;
                if (workTimer > 40) {
                    net.minecraft.world.level.block.entity.BlockEntity be = staff.level().getBlockEntity(targetPos);
                    if (be instanceof AnimalFeederBlockEntity feeder) {
                        net.minecraft.resources.ResourceLocation foodId = net.minecraft.resources.ResourceLocation.tryParse(staff.getAssignedFoodId());
                        net.minecraft.world.item.Item foodItem = foodId != null ? net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(foodId) : null;
                        if (foodItem != null && FoodAnimalRegistry.isValidFoodForAnimal(staff.getAssignedAnimalId(), foodItem)) {
                            feeder.addFood(new net.minecraft.world.item.ItemStack(foodItem), 8, "");
                        }
                    }
                    targetPos = null;
                }
            }
        }
        @Override public boolean canContinueToUse() { return targetPos != null; }

        private net.minecraft.world.entity.Entity findSpecializedAnimal() {
            ZooData data = ZooData.get(staff.level());
            net.minecraft.nbt.ListTag tagged = data.getTaggedAnimals();
            for (int i = 0; i < tagged.size(); i++) {
                net.minecraft.nbt.CompoundTag t = tagged.getCompound(i);
                if (!staff.getAssignedAnimalId().equals(t.getString("type"))) continue;
                net.minecraft.world.entity.Entity e = staff.level().getEntity(t.getInt("id"));
                if (e != null && e.isAlive()) return e;
            }
            return null;
        }
    }

    static class ThrowFoodToHungryAnimalGoal extends Goal {
        private final StaffEntity staff;
        private LivingEntity targetAnimal;
        private int actionTicks;
        private int cooldownTicks;

        public ThrowFoodToHungryAnimalGoal(StaffEntity staff) {
            this.staff = staff;
            this.setFlags(java.util.EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (staff.getRole() != 1) return false;
            if (cooldownTicks > 0) {
                cooldownTicks--;
                return false;
            }
            if (staff.getAssignedAnimalId().isEmpty() || staff.getAssignedFoodId().isEmpty()) return false;
            if (!(staff.level() instanceof net.minecraft.server.level.ServerLevel serverLevel)) return false;
            if (staff.getRandom().nextFloat() > 0.08F) return false;

            targetAnimal = findHungrySpecializedAnimal(serverLevel);
            return targetAnimal != null;
        }

        @Override
        public void start() {
            actionTicks = 0;
            staff.setAnimState(3);
            net.minecraft.resources.ResourceLocation foodId = net.minecraft.resources.ResourceLocation.tryParse(staff.getAssignedFoodId());
            net.minecraft.world.item.Item foodItem = foodId != null ? net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(foodId) : null;
            if (foodItem != null) {
                staff.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new net.minecraft.world.item.ItemStack(foodItem));
            }
        }

        @Override
        public void stop() {
            staff.setAnimState(0);
            staff.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, net.minecraft.world.item.ItemStack.EMPTY);
            targetAnimal = null;
            actionTicks = 0;
        }

        @Override
        public void tick() {
            if (!(staff.level() instanceof net.minecraft.server.level.ServerLevel serverLevel)) return;
            if (targetAnimal == null || !targetAnimal.isAlive()) {
                targetAnimal = null;
                return;
            }

            net.minecraft.resources.ResourceLocation foodId = net.minecraft.resources.ResourceLocation.tryParse(staff.getAssignedFoodId());
            net.minecraft.world.item.Item foodItem = foodId != null ? net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(foodId) : null;
            if (foodItem == null) {
                targetAnimal = null;
                return;
            }

            staff.getLookControl().setLookAt(targetAnimal, 30.0F, 30.0F);
            double dist = staff.distanceToSqr(targetAnimal);
            if (dist > 64.0D) {
                staff.getNavigation().moveTo(targetAnimal, 1.05D);
                return;
            }

            staff.getNavigation().stop();
            actionTicks++;
            if (actionTicks >= 16) {
                ZooAnimalHungerSystem.throwFoodAtTarget(serverLevel, staff, targetAnimal, foodItem);
                cooldownTicks = 80;
                targetAnimal = null;
            }
        }

        @Override
        public boolean canContinueToUse() {
            return targetAnimal != null;
        }

        private LivingEntity findHungrySpecializedAnimal(net.minecraft.server.level.ServerLevel level) {
            ZooData data = ZooData.get(level);
            net.minecraft.nbt.ListTag tagged = data.getTaggedAnimals();
            LivingEntity best = null;
            double bestDistance = Double.MAX_VALUE;

            for (int i = 0; i < tagged.size(); i++) {
                net.minecraft.nbt.CompoundTag tag = tagged.getCompound(i);
                if (!staff.getAssignedAnimalId().equals(tag.getString("type"))) continue;

                net.minecraft.world.entity.Entity entity = ZooAnimalHungerSystem.resolveTaggedEntity(level, tag);
                if (!(entity instanceof LivingEntity living) || !living.isAlive()) continue;
                if (!ZooAnimalHungerSystem.isHungry(level, living)) continue;

                double distance = staff.distanceToSqr(living);
                if (distance > 18.0D * 18.0D) continue;
                if (distance < bestDistance) {
                    bestDistance = distance;
                    best = living;
                }
            }

            return best;
        }
    }

    static class InteractVisitorGoal extends Goal {
        private final StaffEntity staff;
        private VisitorEntity targetVisitor;
        private int timer;
        public InteractVisitorGoal(StaffEntity staff) { this.staff = staff; this.setFlags(java.util.EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK)); }
        @Override public boolean canUse() {
            if (staff.getRole() != 1) return false;
            if (staff.getRandom().nextFloat() < 0.01F) {
                VisitorEntity nearby = staff.level().getNearestEntity(VisitorEntity.class, TargetingConditions.forNonCombat().range(4.0D).selector(e -> e.hasLineOfSight(staff)), staff, staff.getX(), staff.getY(), staff.getZ(), staff.getBoundingBox().inflate(4.0D));
                if (nearby != null) { this.targetVisitor = nearby; return true; }
            }
            return false;
        }
        @Override public void start() { timer = 60; staff.getNavigation().stop(); staff.setAnimState(staff.getRole() == 0 ? 2 : 3); }
        @Override public void stop() { staff.setAnimState(0); targetVisitor = null; }
        @Override public void tick() { if (targetVisitor != null) { staff.getLookControl().setLookAt(targetVisitor, 30.0F, 30.0F); targetVisitor.getLookControl().setLookAt(staff, 30.0F, 30.0F); } timer--; }
        @Override public boolean canContinueToUse() { return timer > 0 && targetVisitor != null && targetVisitor.isAlive() && staff.distanceToSqr(targetVisitor) < 25.0D; }
    }

    public int getFoodStock() { return foodStock; }
    public void setFoodStock(int amount) { this.foodStock = amount; }
    public boolean hasFood() { return foodStock > 0; }
    public void consumeOneFood() { if (foodStock > 0) foodStock--; }
    public void setHomePos(BlockPos pos, int radius) { this.homePos = pos; this.restrictTo(pos, radius); }
    public void setHomePos(BlockPos pos) { setHomePos(pos, MAX_HOME_DISTANCE); }
}

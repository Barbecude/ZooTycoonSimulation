package net.zootycoon.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.Heightmap;
import net.zootycoon.animal.AnimalDietRegistry;
import net.zootycoon.block.ZooBlocks;
import net.zootycoon.block.ZooFeedingTroughBlock;
import net.zootycoon.block.ZooFeedingTroughBlockEntity;
import net.zootycoon.claim.ZooClaim;
import net.zootycoon.claim.ZooClaimManager;
import net.zootycoon.item.ZooItems;
import net.zootycoon.network.ZooNetworkPayloads;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.particles.BlockParticleOption;
import net.zootycoon.animal.ZooAnimalHungerManager;
import net.zootycoon.util.ZooTranslations;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ZooZookeeperEntity extends PathfinderMob {

    static {
        net.zootycoon.network.ZookeeperBathingPayloads.register();
    }

    private static final EntityDataAccessor<Integer> FOOD_STOCK_DATA =
            SynchedEntityData.defineId(ZooZookeeperEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> SWEEPING =
            SynchedEntityData.defineId(ZooZookeeperEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> HAS_BATHING_TOOLS =
            SynchedEntityData.defineId(ZooZookeeperEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> BATHING_ACTION =
            SynchedEntityData.defineId(ZooZookeeperEntity.class, EntityDataSerializers.INT);

    private static final String[] NAMES = new String[]{
            "Siti Rahma", "Agus Setiawan", "Rudi Hartono", "Dewi Lestari",
            "Bambang Pamungkas", "Nurul Hidayah", "Hendra Wijaya", "Indah Permata",
            "Joko Supriyanto", "Rina Marlina"
    };

    private UUID ownerUuid = null;
    private UUID assignedAnimalUuid = null;
    private String currentDutyState = "IDLE"; // "REFILL", "NEED_FOOD", "SWEEPING", "BATHING", "IDLE"
    private int actionCooldown = 0;
    private int bathingStepTicks = 0;

    private boolean hasWateringCan = false;
    private ItemStack wateringCanStack = ItemStack.EMPTY;
    private ItemStack brushStack = ItemStack.EMPTY;

    private BlockPos currentTrashTarget = null;
    private int trashStuckTicks = 0;
    private BlockPos tempDirtPos = null;
    private int tempDirtTimer = 0;
    private BlockState previousBlockState = null;

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(FOOD_STOCK_DATA, 0); // Starts with 0 food units (player supplies food)
        builder.define(SWEEPING, false);
        builder.define(HAS_BATHING_TOOLS, false);
        builder.define(BATHING_ACTION, 0);
    }

    public ZooZookeeperEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.setPersistenceRequired();

        if (!level.isClientSide()) {
            int nameIdx = this.random.nextInt(NAMES.length);
            this.setCustomName(Component.literal(NAMES[nameIdx]));
            this.setCustomNameVisible(true);
            this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            this.addTag("zootycoon_staff");
            this.addTag("zootycoon_zookeeper");
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new net.minecraft.world.entity.ai.goal.AvoidEntityGoal<>(this, ZooHunterEntity.class, 14.0F, 1.1D, 1.35D));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(3, new ZookeeperClaimStrollGoal(this, 0.4));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
    }

    public static class ZookeeperClaimStrollGoal extends WaterAvoidingRandomStrollGoal {
        private final ZooZookeeperEntity zookeeper;

        public ZookeeperClaimStrollGoal(ZooZookeeperEntity mob, double speedModifier) {
            super(mob, speedModifier);
            this.zookeeper = mob;
        }

        @Override
        public boolean canUse() {
            if (this.zookeeper.hasActiveAssignedAnimal()) {
                return false;
            }
            return super.canUse();
        }

        @Override
        protected net.minecraft.world.phys.Vec3 getPosition() {
            net.minecraft.world.phys.Vec3 pos = super.getPosition();
            if (pos != null && this.zookeeper.level() instanceof ServerLevel sLevel) {
                ZooClaimManager cm = ZooClaimManager.get(sLevel);
                BlockPos bp = BlockPos.containing(pos.x, pos.y, pos.z);
                boolean inside = (this.zookeeper.ownerUuid != null)
                        ? cm.isClaimedBy(bp, this.zookeeper.ownerUuid)
                        : cm.isInsideAnyClaim(bp);
                if (!inside) {
                    ZooClaim claim = this.zookeeper.findClosestClaim(cm);
                    if (claim != null) {
                        var box = claim.getBox();
                        int minX = Math.min(box.minX() + 2, box.maxX());
                        int maxX = Math.max(box.maxX() - 2, minX);
                        int minZ = Math.min(box.minZ() + 2, box.maxZ());
                        int maxZ = Math.max(box.maxZ() - 2, minZ);
                        int tx = Math.clamp((int) Math.floor(pos.x), minX, maxX);
                        int tz = Math.clamp((int) Math.floor(pos.z), minZ, maxZ);
                        int ty = sLevel.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, tx, tz);
                        return new net.minecraft.world.phys.Vec3(tx + 0.5D, ty, tz + 0.5D);
                    }
                }
            }
            return pos;
        }
    }

    public boolean hasActiveAssignedAnimal() {
        if (this.assignedAnimalUuid == null) return false;
        if (this.level() instanceof ServerLevel sLevel) {
            Entity ent = sLevel.getEntity(this.assignedAnimalUuid);
            return ent instanceof LivingEntity living && living.isAlive();
        }
        return false;
    }

    public ZooClaim findClosestClaim(ZooClaimManager claimManager) {
        java.util.Collection<ZooClaim> claims = (ownerUuid != null)
                ? claimManager.getPlayerClaims(ownerUuid)
                : claimManager.getAllClaims();
        if (claims.isEmpty() && ownerUuid != null) {
            claims = claimManager.getAllClaims();
        }
        ZooClaim closest = null;
        double minDistanceSq = Double.MAX_VALUE;
        BlockPos currentPos = this.blockPosition();
        for (ZooClaim claim : claims) {
            var box = claim.getBox();
            int cx = (box.minX() + box.maxX()) / 2;
            int cz = (box.minZ() + box.maxZ()) / 2;
            double d = currentPos.distSqr(new net.minecraft.core.Vec3i(cx, currentPos.getY(), cz));
            if (d < minDistanceSq) {
                minDistanceSq = d;
                closest = claim;
            }
        }
        return closest;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) return;
        ServerLevel sLevel = (ServerLevel) this.level();

        if (actionCooldown > 0) actionCooldown--;

        // Temporary dirt expiration countdown
        if (tempDirtPos != null) {
            tempDirtTimer--;
            if (tempDirtTimer <= 0) {
                removeTempDirt(sLevel);
            }
        }

        // Active litter cleaning and stuck detection for unassigned zookeepers
        if (this.assignedAnimalUuid == null && currentTrashTarget != null) {
            if (!sLevel.getBlockState(currentTrashTarget).is(ZooBlocks.TRASH)) {
                currentTrashTarget = null;
                trashStuckTicks = 0;
            } else {
                double distSq = this.distanceToSqr(currentTrashTarget.getX() + 0.5, currentTrashTarget.getY() + 0.5, currentTrashTarget.getZ() + 0.5);
                double reach = (tempDirtPos != null) ? 4.8 : 2.4;
                if (distSq <= reach * reach) {
                    sweepLitterAt(sLevel, currentTrashTarget);
                } else if (tempDirtPos == null && distSq <= 4.8 * 4.8) {
                    boolean navStuck = this.getNavigation().isDone() || this.getDeltaMovement().horizontalDistanceSqr() < 0.0008;
                    if (navStuck) {
                        trashStuckTicks++;
                        if (trashStuckTicks >= 25) {
                            tryPlaceTemporaryDirt(sLevel);
                        }
                    } else if (trashStuckTicks > 0) {
                        trashStuckTicks--;
                    }
                }
            }
        } else if (this.assignedAnimalUuid != null && tempDirtPos != null) {
            removeTempDirt(sLevel);
            currentTrashTarget = null;
            trashStuckTicks = 0;
        }

        // If elevated on temporary dirt, keep mob centered on dirt and look towards trash
        if (tempDirtPos != null) {
            this.getNavigation().stop();
            if (currentTrashTarget != null) {
                this.getLookControl().setLookAt(currentTrashTarget.getX() + 0.5, currentTrashTarget.getY() + 0.5, currentTrashTarget.getZ() + 0.5);
            }
        }

        // Sanitize legacy custom name if it contains " - Zookeeper" or "Zookeeper"
        Component cn = this.getCustomName();
        if (cn != null) {
            String str = cn.getString();
            if (str.contains(" - Zookeeper") || str.contains("Zookeeper - ") || str.equalsIgnoreCase("Zookeeper")) {
                String cleaned = str.replace(" - Zookeeper", "").replace("Zookeeper - ", "").trim();
                if (cleaned.isEmpty() || cleaned.equalsIgnoreCase("Zookeeper")) {
                    cleaned = NAMES[Math.abs(this.getUUID().hashCode()) % NAMES.length];
                }
                this.setCustomName(Component.literal(cleaned));
            }
        }

        ZooClaimManager claimManager = ZooClaimManager.get(sLevel);
        if (ownerUuid == null && this.tickCount % 20 == 0) {
            ZooClaim nearest = findClosestClaim(claimManager);
            if (nearest != null) {
                ownerUuid = nearest.getOwnerUuid();
            }
        }

        // Restrict strictly to claimed zoo area
        BlockPos currentPos = this.blockPosition();
        boolean inside = (ownerUuid != null)
                ? claimManager.isClaimedBy(currentPos, ownerUuid)
                : claimManager.isInsideAnyClaim(currentPos);
        if (!inside) {
            if (this.tickCount % 10 == 0) {
                returnToClaimedLand(sLevel, claimManager);
            }
        } else if (this.getNavigation().isInProgress()) {
            BlockPos targetPos = this.getNavigation().getTargetPos();
            if (targetPos != null) {
                boolean targetInside = (ownerUuid != null)
                        ? claimManager.isClaimedBy(targetPos, ownerUuid)
                        : claimManager.isInsideAnyClaim(targetPos);
                if (!targetInside) {
                    this.getNavigation().stop();
                }
            }
        }

        // Run duties every 25 ticks
        if (this.tickCount % 25 == 0 && actionCooldown <= 0) {
            handleStaffDuties(sLevel);
        }
    }

    private void returnToClaimedLand(ServerLevel sLevel, ZooClaimManager claimManager) {
        ZooClaim closestClaim = findClosestClaim(claimManager);
        if (closestClaim == null) return;

        var box = closestClaim.getBox();
        BlockPos currentPos = this.blockPosition();
        int minX = Math.min(box.minX() + 2, box.maxX());
        int maxX = Math.max(box.maxX() - 2, minX);
        int minZ = Math.min(box.minZ() + 2, box.maxZ());
        int maxZ = Math.max(box.maxZ() - 2, minZ);
        int targetX = Math.clamp(currentPos.getX(), minX, maxX);
        int targetZ = Math.clamp(currentPos.getZ(), minZ, maxZ);
        int targetY = sLevel.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, targetX, targetZ);
        BlockPos safePos = new BlockPos(targetX, targetY, targetZ);

        double distToSafe = this.distanceToSqr(safePos.getX() + 0.5, safePos.getY(), safePos.getZ() + 0.5);
        if (distToSafe > 10.0 * 10.0) {
            this.teleportTo(safePos.getX() + 0.5, safePos.getY(), safePos.getZ() + 0.5);
            this.getNavigation().stop();
        } else {
            this.getNavigation().moveTo(safePos.getX() + 0.5, safePos.getY(), safePos.getZ() + 0.5, 1.25);
        }
    }

    private void handleStaffDuties(ServerLevel sLevel) {
        ZooClaimManager claimManager = ZooClaimManager.get(sLevel);
        BlockPos myPos = this.blockPosition();

        // Check if assigned animal is valid and alive
        LivingEntity assignedAnimal = null;
        if (this.assignedAnimalUuid != null) {
            Entity ent = sLevel.getEntity(this.assignedAnimalUuid);
            if (ent instanceof LivingEntity living && living.isAlive()) {
                assignedAnimal = living;
            } else {
                // If animal is dead or gone, clear assignment
                this.assignedAnimalUuid = null;
            }
        }

        // ========================================================
        // 1. Zookeepers with an assigned animal will NEVER sweep trash!
        // Only unassigned zookeepers sweep and clean visitor litter.
        // ========================================================
        if (assignedAnimal == null) {
            BlockPos nearestTrash = null;
            double minTrashDistSq = Double.MAX_VALUE;

            for (int dx = -14; dx <= 14; dx++) {
                for (int dz = -14; dz <= 14; dz++) {
                    for (int dy = -3; dy <= 3; dy++) {
                        BlockPos p = myPos.offset(dx, dy, dz);
                        if (sLevel.getBlockState(p).is(ZooBlocks.TRASH)) {
                            if (ownerUuid != null && !claimManager.isClaimedBy(p, ownerUuid)) continue;
                            double d = myPos.distSqr(p);
                            if (d < minTrashDistSq) {
                                minTrashDistSq = d;
                                nearestTrash = p.immutable();
                            }
                        }
                    }
                }
            }

            if (nearestTrash != null) {
                currentTrashTarget = nearestTrash;
                currentDutyState = "SWEEPING";
                this.entityData.set(SWEEPING, true);
                // Hold broom in hand
                if (!this.getMainHandItem().is(ZooItems.BROOM)) {
                    this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ZooItems.BROOM));
                }

                double distSq = this.distanceToSqr(nearestTrash.getX() + 0.5, nearestTrash.getY() + 0.5, nearestTrash.getZ() + 0.5);
                double reach = (tempDirtPos != null) ? 4.8 : 2.4;
                if (distSq <= reach * reach) {
                    sweepLitterAt(sLevel, nearestTrash);
                    return;
                }

                if (tempDirtPos == null) {
                    this.getNavigation().moveTo(nearestTrash.getX() + 0.5, nearestTrash.getY() + 0.5, nearestTrash.getZ() + 0.5, 1.15);
                } else {
                    this.getNavigation().stop();
                }
                return;
            }

            // Cleaned or no trash: remove broom and stay idle/strolling
            currentTrashTarget = null;
            trashStuckTicks = 0;
            if (tempDirtPos != null) {
                removeTempDirt(sLevel);
            }
            if (this.getMainHandItem().is(ZooItems.BROOM)) {
                this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            }
            this.entityData.set(SWEEPING, false);
            currentDutyState = "IDLE";
            return;
        }

        // Assigned animal is present: NEVER sweep trash! Put away broom if held
        if (this.getMainHandItem().is(ZooItems.BROOM)) {
            this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }
        this.entityData.set(SWEEPING, false);

        // ========================================================
        // 2a. PRIORITY 2a: Bathe assigned animal if dirty (cleanliness <= 50%) and tools are available
        // ========================================================
        if (assignedAnimal != null && this.hasBathingTools()) {
            int cleanliness = ZooAnimalHungerManager.getCleanliness(assignedAnimal);
            if (cleanliness <= 50) {
                currentDutyState = "BATHING";
                double distSq = this.distanceToSqr(assignedAnimal);

                boolean isWet = net.zootycoon.animal.AnimalBathingManager.isWet(assignedAnimal);
                ItemStack targetTool = isWet ? (!brushStack.isEmpty() ? brushStack : new ItemStack(Items.BRUSH))
                        : (!wateringCanStack.isEmpty() ? wateringCanStack : new ItemStack(Items.WATER_BUCKET));
                if (!this.getMainHandItem().is(targetTool.getItem())) {
                    this.setItemSlot(EquipmentSlot.MAINHAND, targetTool.copy());
                }

                this.getLookControl().setLookAt(assignedAnimal, 30.0F, 30.0F);

                if (distSq > 2.8 * 2.8) {
                    this.getNavigation().moveTo(assignedAnimal, 1.25);
                    this.setBathingAction(0);
                    return;
                } else {
                    this.getNavigation().stop();
                    if (!isWet) {
                        // Step 1: Water the animal to make it wet with continuous animation
                        this.setBathingAction(1);
                        bathingStepTicks++;

                        if (bathingStepTicks % 4 == 0) {
                            sLevel.sendParticles(ParticleTypes.SPLASH, assignedAnimal.getX(), assignedAnimal.getY() + assignedAnimal.getBbHeight() * 0.5, assignedAnimal.getZ(),
                                    12, assignedAnimal.getBbWidth() * 0.35, assignedAnimal.getBbHeight() * 0.25, assignedAnimal.getBbWidth() * 0.35, 0.08);
                            sLevel.sendParticles(ParticleTypes.DRIPPING_WATER, assignedAnimal.getX(), assignedAnimal.getY() + assignedAnimal.getBbHeight() * 0.8, assignedAnimal.getZ(),
                                    6, assignedAnimal.getBbWidth() * 0.25, 0.1, assignedAnimal.getBbWidth() * 0.25, 0.04);
                            sLevel.playSound(null, this.blockPosition(), SoundEvents.WATER_AMBIENT, SoundSource.NEUTRAL, 0.5f, 1.2f);
                        }

                        if (bathingStepTicks >= 30) {
                            bathingStepTicks = 0;
                            net.zootycoon.animal.AnimalBathingManager.setWet(assignedAnimal, net.zootycoon.animal.AnimalBathingManager.WET_DURATION_TICKS);

                            sLevel.playSound(null, assignedAnimal.blockPosition(), SoundEvents.BUCKET_EMPTY, SoundSource.NEUTRAL, 1.0f, 1.2f);
                            sLevel.playSound(null, assignedAnimal.blockPosition(), SoundEvents.GENERIC_SPLASH, SoundSource.NEUTRAL, 0.8f, 1.0f);

                            net.zootycoon.animal.ZooInspectionService.notifyAnimalUpdated(assignedAnimal);
                            this.setBathingAction(0);
                            ItemStack brushItem = !brushStack.isEmpty() ? brushStack : new ItemStack(Items.BRUSH);
                            this.setItemSlot(EquipmentSlot.MAINHAND, brushItem.copy());
                            actionCooldown = 15;
                        }
                        return;
                    } else {
                        // Step 2: Brush the wet animal with continuous scrubbing animation
                        this.setBathingAction(2);
                        bathingStepTicks++;

                        if (bathingStepTicks % 6 == 0) {
                            sLevel.playSound(null, assignedAnimal.blockPosition(), SoundEvents.BRUSH_GENERIC, SoundSource.NEUTRAL, 0.8f, 1.0f + this.getRandom().nextFloat() * 0.2f);
                            sLevel.sendParticles(ParticleTypes.BUBBLE_POP, assignedAnimal.getX(), assignedAnimal.getY() + assignedAnimal.getBbHeight() * 0.5, assignedAnimal.getZ(),
                                    8, assignedAnimal.getBbWidth() * 0.35, assignedAnimal.getBbHeight() * 0.25, assignedAnimal.getBbWidth() * 0.35, 0.04);
                            sLevel.sendParticles(ParticleTypes.WAX_OFF, assignedAnimal.getX(), assignedAnimal.getY() + assignedAnimal.getBbHeight() * 0.5, assignedAnimal.getZ(),
                                    5, assignedAnimal.getBbWidth() * 0.35, assignedAnimal.getBbHeight() * 0.25, assignedAnimal.getBbWidth() * 0.35, 0.02);
                        }

                        if (bathingStepTicks >= 40) {
                            bathingStepTicks = 0;
                            ZooAnimalHungerManager.setCleanliness(assignedAnimal, ZooAnimalHungerManager.MAX_CLEANLINESS);

                            hurtBrush(1, sLevel);

                            sLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, assignedAnimal.getX(), assignedAnimal.getY() + assignedAnimal.getBbHeight() * 0.8, assignedAnimal.getZ(),
                                    8, 0.3, 0.3, 0.3, 0.02);

                            this.setBathingAction(0);
                            this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                            net.zootycoon.animal.ZooInspectionService.notifyAnimalUpdated(assignedAnimal);
                            actionCooldown = 40;
                        }
                        return;
                    }
                }
            }
        }

        // If not bathing, clear bathing tools from hand if held
        if (!"BATHING".equals(currentDutyState)) {
            if (this.getBathingAction() != 0) {
                this.setBathingAction(0);
            }
            bathingStepTicks = 0;
            if (this.getMainHandItem().is(Items.BRUSH) || net.zootycoon.animal.AnimalBathingManager.isWateringCan(this.getMainHandItem())) {
                this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            }
        }

        // ========================================================
        // 2b. PRIORITY 2b: Feeding Duty for assigned animal
        // Handles both Thrown Food (Carnivores/Predators) and Trough (Herbivores/Omnivores)
        // ========================================================
        if (assignedAnimal != null) {
            var diet = AnimalDietRegistry.getDiet(assignedAnimal);
            boolean isThrownMeat = (diet != null && diet.feedingType == AnimalDietRegistry.FeedingType.THROWN_MEAT);

            if (isThrownMeat) {
                // Carnivores / Thrown food: Zookeeper throws food directly to the animal
                int hunger = ZooAnimalHungerManager.getHunger(assignedAnimal);
                if (hunger <= 75) {
                    if (this.getFoodStock() > 0) {
                        currentDutyState = "FEEDING_ANIMAL";
                        Item targetFoodItem = (diet != null && !diet.acceptedFoods.isEmpty()) ? diet.acceptedFoods.get(0) : Items.BEEF;
                        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(targetFoodItem));

                        double distSq = this.distanceToSqr(assignedAnimal);
                        if (distSq > 4.5 * 4.5) {
                            this.getNavigation().moveTo(assignedAnimal, 1.25);
                            return;
                        } else {
                            this.getNavigation().stop();
                            this.getLookControl().setLookAt(assignedAnimal, 30.0F, 30.0F);
                            this.swing(InteractionHand.MAIN_HAND);

                            ItemStack throwStack = new ItemStack(targetFoodItem, 1);
                            ItemEntity itemEntity = new ItemEntity(sLevel, this.getX(), this.getEyeY() - 0.2, this.getZ(), throwStack);
                            Vec3 targetPos = assignedAnimal.position().add(0, assignedAnimal.getBbHeight() * 0.25, 0);
                            Vec3 throwVec = targetPos.subtract(itemEntity.position()).normalize().scale(0.38);
                            itemEntity.setDeltaMovement(throwVec.x, throwVec.y + 0.12, throwVec.z);
                            itemEntity.setDefaultPickUpDelay();
                            sLevel.addFreshEntity(itemEntity);

                            sLevel.playSound(null, this.blockPosition(), SoundEvents.SNOWBALL_THROW, SoundSource.NEUTRAL, 0.6f, 0.9f + this.getRandom().nextFloat() * 0.2f);
                            this.setFoodStock(this.getFoodStock() - 1);
                            actionCooldown = 40; // 2 seconds cooldown between throws
                            return;
                        }
                    } else {
                        currentDutyState = "NEED_FOOD";
                    }
                }
            } else {
                // Herbivores / Trough eaters: Refill feeding trough
                Item targetFoodItem = Items.WHEAT;
                if (diet != null && !diet.acceptedFoods.isEmpty()) {
                    targetFoodItem = diet.acceptedFoods.get(0);
                } else if (assignedAnimal instanceof net.minecraft.world.entity.animal.Animal a) {
                    if (a.isFood(new ItemStack(Items.CARROT))) targetFoodItem = Items.CARROT;
                    else if (a.isFood(new ItemStack(Items.WHEAT_SEEDS))) targetFoodItem = Items.WHEAT_SEEDS;
                    else if (a.isFood(new ItemStack(Items.BAMBOO))) targetFoodItem = Items.BAMBOO;
                    else if (a.isFood(new ItemStack(Items.APPLE))) targetFoodItem = Items.APPLE;
                }

                BlockPos designatedTrough = null;
                double minAssignedDistSq = Double.MAX_VALUE;

                BlockPos fallbackTrough = null;
                double minFallbackDistSq = Double.MAX_VALUE;

                for (BlockPos troughPos : ZooFeedingTroughBlock.KNOWN_TROUGHS) {
                    if (ownerUuid != null && !claimManager.isClaimedBy(troughPos, ownerUuid)) continue;
                    double distSq = myPos.distSqr(troughPos);
                    if (distSq > 48 * 48) continue;

                    BlockEntity be = sLevel.getBlockEntity(troughPos);
                    if (be instanceof ZooFeedingTroughBlockEntity troughBe) {
                        if (troughBe.hasWater()) continue;
                        ItemStack foodInTrough = troughBe.getFoodStack();
                        boolean needsFood = foodInTrough.isEmpty() || (foodInTrough.is(targetFoodItem) && foodInTrough.getCount() < 32);

                        if (needsFood) {
                            if (troughBe.isAssignedTo(assignedAnimal)) {
                                if (distSq < minAssignedDistSq) {
                                    minAssignedDistSq = distSq;
                                    designatedTrough = troughPos;
                                }
                            } else if (!troughBe.isAssigned() && assignedAnimal.blockPosition().distSqr(troughPos) <= 24 * 24) {
                                if (distSq < minFallbackDistSq) {
                                    minFallbackDistSq = distSq;
                                    fallbackTrough = troughPos;
                                }
                            }
                        }
                    }
                }

                BlockPos needRefillTrough = (designatedTrough != null) ? designatedTrough : fallbackTrough;

                if (needRefillTrough != null) {
                    if (this.getFoodStock() > 0) {
                        currentDutyState = "REFILL";
                        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(targetFoodItem));

                        double distSq = this.distanceToSqr(needRefillTrough.getX() + 0.5, needRefillTrough.getY(), needRefillTrough.getZ() + 0.5);
                        if (distSq > 2.5 * 2.5) {
                            this.getNavigation().moveTo(needRefillTrough.getX() + 0.5, needRefillTrough.getY(), needRefillTrough.getZ() + 0.5, 1.2);
                        } else {
                            // Refill the trough with the assigned animal's food!
                            BlockEntity be = sLevel.getBlockEntity(needRefillTrough);
                            if (be instanceof ZooFeedingTroughBlockEntity troughBe) {
                                int toInsert = Math.min(8, this.getFoodStock());
                                boolean inserted = troughBe.insertFood(new ItemStack(targetFoodItem, toInsert), toInsert);
                                if (inserted) {
                                    this.setFoodStock(this.getFoodStock() - toInsert);
                                    sLevel.playSound(null, needRefillTrough, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.9f, 1.2f);
                                    sLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, needRefillTrough.getX() + 0.5, needRefillTrough.getY() + 0.8, needRefillTrough.getZ() + 0.5, 6, 0.2, 0.2, 0.2, 0.02);
                                    actionCooldown = 40; // 2 seconds cooldown between refills
                                }
                            }
                        }
                        return;
                    } else {
                        currentDutyState = "NEED_FOOD";
                    }
                }
            }
        }

        // If not feeding/refilling, clear food from hand if held
        if ("REFILL".equals(currentDutyState) || "FEEDING_ANIMAL".equals(currentDutyState)) {
            currentDutyState = "IDLE";
            this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }

        // ========================================================
        // 3. PRIORITY 3: Follow Assigned Animal (Natural 5-block distance)
        // When animal is full (> 25% hunger), zookeeper follows its animal
        // ========================================================
        if (assignedAnimal != null) {
            currentDutyState = "FOLLOW_ANIMAL";
            if (!this.getMainHandItem().isEmpty() && !this.getMainHandItem().is(ZooItems.BROOM)) {
                this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            }

            this.getLookControl().setLookAt(assignedAnimal, 30.0F, 30.0F);

            double distSq = this.distanceToSqr(assignedAnimal);
            // Natural 5-block following distance:
            // Move closer if further than 6 blocks, stop when at or within 5 blocks
            if (distSq > 6.0 * 6.0) {
                this.getNavigation().moveTo(assignedAnimal, 1.05D);
            } else if (distSq <= 5.0 * 5.0) {
                this.getNavigation().stop();
            }
            return;
        }

        // ========================================================
        // 4. PRIORITY 4: No assigned animal, idle patrol
        // ========================================================
        currentDutyState = "IDLE";
        this.entityData.set(SWEEPING, false);
        if (this.getMainHandItem().is(ZooItems.BROOM)) {
            this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.level().isClientSide() && hand == InteractionHand.MAIN_HAND && player instanceof ServerPlayer sp) {
            if (this.ownerUuid == null) {
                this.ownerUuid = player.getUUID();
            }

            openDialogScreen(sp, null);
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    public void openDialogScreen(ServerPlayer player, String dialogOverride) {
        LivingEntity animal = null;
        if (assignedAnimalUuid != null && this.level() instanceof ServerLevel sLevel) {
            Entity ent = sLevel.getEntity(assignedAnimalUuid);
            if (ent instanceof LivingEntity l) animal = l;
        }

        String keeperName = this.getName().getString();
        boolean hasAnimal = animal != null;
        String animalName = hasAnimal ? (animal.hasCustomName() ? animal.getCustomName().getString() : animal.getType().getDescription().getString()) : "-";
        int hunger = hasAnimal ? ZooAnimalHungerManager.getHunger(animal) : 0;

        List<String> foods = new ArrayList<>();
        if (hasAnimal) {
            var diet = AnimalDietRegistry.getDiet(animal);
            for (Item item : diet.acceptedFoods) {
                foods.add(BuiltInRegistries.ITEM.getKey(item).toString());
            }
        }

        String dialogText = "";
        if (dialogOverride != null) {
            dialogText = dialogOverride;
        } else if (!hasAnimal) {
            dialogText = ZooTranslations.get("zootycoon.dialog.zookeeper.speech_no_animal");
        } else {
            int cleanlinessVal = ZooAnimalHungerManager.getCleanliness(animal);
            if (cleanlinessVal <= 50 && !this.hasBathingTools()) {
                dialogText = ZooTranslations.get("zootycoon.dialog.zookeeper.need_bathing_tools", animalName);
            } else {
                ChatFormatting hungerColor = hunger <= 25 ? ChatFormatting.RED : (hunger <= 60 ? ChatFormatting.YELLOW : ChatFormatting.GREEN);
                String hungerColoredStr = hungerColor + String.valueOf(hunger) + "%" + ChatFormatting.RESET;
                var diet = AnimalDietRegistry.getDiet(animal);
                boolean isThrownDiet = (diet != null && diet.feedingType == AnimalDietRegistry.FeedingType.THROWN_MEAT);

                if (this.getFoodStock() <= 0) {
                    dialogText = isThrownDiet
                            ? ZooTranslations.get("zootycoon.dialog.zookeeper.empty_stock_throw", animalName, hungerColoredStr)
                            : ZooTranslations.get("zootycoon.dialog.zookeeper.empty_stock", animalName, hungerColoredStr);
                } else {
                    dialogText = isThrownDiet
                            ? ZooTranslations.get("zootycoon.dialog.zookeeper.speech_with_animal_throw", animalName, hungerColoredStr, String.valueOf(this.getFoodStock()))
                            : ZooTranslations.get("zootycoon.dialog.zookeeper.speech_with_animal", animalName, hungerColoredStr, String.valueOf(this.getFoodStock()));
                }
            }
        }

        ServerPlayNetworking.send(player, new ZooNetworkPayloads.OpenZookeeperScreenPayload(
                this.getUUID(), keeperName, hasAnimal, animalName, hunger, this.getFoodStock(), dialogText, foods
        ));
    }

    public void handleFoodTransfer(ServerPlayer player) {
        LivingEntity animal = null;
        if (assignedAnimalUuid != null && this.level() instanceof ServerLevel sLevel) {
            Entity ent = sLevel.getEntity(assignedAnimalUuid);
            if (ent instanceof LivingEntity l) animal = l;
        }

        int totalGiven = 0;
        boolean hasAnyFoodItem = false;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack st = player.getInventory().getItem(i);
            if (!st.isEmpty()) {
                if (AnimalDietRegistry.isAnyFood(st) || AnimalDietRegistry.isAnyTroughFood(st)) {
                    hasAnyFoodItem = true;
                }
                boolean valid = (animal != null) ? AnimalDietRegistry.isFoodValid(animal, st) : (AnimalDietRegistry.isAnyFood(st) || AnimalDietRegistry.isAnyTroughFood(st));
                if (valid) {
                    int toTake = Math.min(st.getCount(), 32 - totalGiven);
                    st.shrink(toTake);
                    this.addFoodStock(toTake);
                    totalGiven += toTake;
                    if (totalGiven >= 32) break;
                }
            }
        }
        player.getInventory().setChanged();

        String anDesc = (animal != null) ? (animal.hasCustomName() ? animal.getCustomName().getString() : animal.getType().getDescription().getString()) : "satwa";

        if (totalGiven > 0) {
            this.playSound(SoundEvents.VILLAGER_YES, 1.0f, 1.0f);
            ((ServerLevel) this.level()).sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 1.2, this.getZ(), 6, 0.2, 0.2, 0.2, 0.02);
            ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.zookeeper", "zootycoon.zookeeper.food_given_toast", 1, totalGiven, anDesc);
        } else if (!hasAnyFoodItem) {
            this.playSound(SoundEvents.VILLAGER_NO, 1.0f, 1.0f);
            ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.zookeeper", "zootycoon.zookeeper.no_food_in_inv", 2);
        } else {
            this.playSound(SoundEvents.VILLAGER_NO, 1.0f, 1.0f);
            ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.zookeeper", "zootycoon.zookeeper.wrong_food_toast", 2, anDesc);
        }
    }

    public void handleBathingToolsTransfer(ServerPlayer player) {
        ItemStack canFound = ItemStack.EMPTY;
        int canSlot = -1;
        ItemStack brushFound = ItemStack.EMPTY;
        int brushSlot = -1;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack st = player.getInventory().getItem(i);
            if (!st.isEmpty()) {
                if (canSlot == -1 && net.zootycoon.animal.AnimalBathingManager.isWateringCan(st)) {
                    canFound = st;
                    canSlot = i;
                } else if (brushSlot == -1 && st.is(Items.BRUSH)) {
                    brushFound = st;
                    brushSlot = i;
                }
            }
        }

        if (canSlot != -1 && brushSlot != -1) {
            ItemStack takenCan = canFound.split(1);
            ItemStack takenBrush = brushFound.split(1);
            this.hasWateringCan = true;
            this.wateringCanStack = takenCan;
            this.brushStack = takenBrush;
            this.entityData.set(HAS_BATHING_TOOLS, true);
            player.getInventory().setChanged();

            this.playSound(SoundEvents.VILLAGER_YES, 1.0f, 1.0f);
            if (this.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 1.2, this.getZ(), 8, 0.2, 0.2, 0.2, 0.02);
            }
            ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.success", "zootycoon.zookeeper.bathing_tools_given_toast", 1, this.getName().getString());
        } else {
            this.playSound(SoundEvents.VILLAGER_NO, 1.0f, 1.0f);
            ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.warning", "zootycoon.zookeeper.no_bathing_tools_toast", 2);
        }
    }

    public void hurtBrush(int amount, ServerLevel sLevel) {
        if (!brushStack.isEmpty()) {
            brushStack.hurtAndBreak(amount, this, EquipmentSlot.MAINHAND);
            if (brushStack.isEmpty()) {
                sLevel.playSound(null, this.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0f, 1.0f);
            }
            this.entityData.set(HAS_BATHING_TOOLS, hasBathingTools());
        }
    }

    public boolean hasBathingTools() {
        if (this.level() != null && this.level().isClientSide()) {
            return this.entityData.get(HAS_BATHING_TOOLS);
        }
        return this.hasWateringCan && !this.brushStack.isEmpty();
    }

    public boolean hasWateringCan() {
        return this.hasWateringCan;
    }

    public boolean hasBrush() {
        return !this.brushStack.isEmpty();
    }

    public int getBathingAction() {
        return this.entityData.get(BATHING_ACTION);
    }

    public void setBathingAction(int action) {
        this.entityData.set(BATHING_ACTION, action);
    }

    public ItemStack getBrushStack() {
        return this.brushStack;
    }

    public ItemStack getWateringCanStack() {
        return this.wateringCanStack;
    }

    public boolean isSweeping() {
        return this.entityData.get(SWEEPING) || this.getMainHandItem().is(ZooItems.BROOM);
    }

    public void setOwnerUuid(UUID uuid) {
        this.ownerUuid = uuid;
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    public UUID getAssignedAnimalUuid() {
        return assignedAnimalUuid;
    }

    public void setAssignedAnimalUuid(UUID uuid) {
        this.assignedAnimalUuid = uuid;
    }

    public int getFoodStock() {
        return this.entityData.get(FOOD_STOCK_DATA);
    }

    public void setFoodStock(int amount) {
        this.entityData.set(FOOD_STOCK_DATA, amount);
    }

    public void addFoodStock(int amount) {
        this.setFoodStock(this.getFoodStock() + amount);
    }

    private void sweepLitterAt(ServerLevel sLevel, BlockPos trashPos) {
        this.getLookControl().setLookAt(trashPos.getX() + 0.5, trashPos.getY() + 0.5, trashPos.getZ() + 0.5);
        this.swing(InteractionHand.MAIN_HAND);
        sLevel.playSound(null, trashPos, SoundEvents.BRUSH_GENERIC, SoundSource.BLOCKS, 1.0f, 1.0f);
        sLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, trashPos.getX() + 0.5, trashPos.getY() + 0.3, trashPos.getZ() + 0.5, 6, 0.2, 0.1, 0.2, 0.02);
        sLevel.destroyBlock(trashPos, false, this);
        this.actionCooldown = 30;
        this.trashStuckTicks = 0;
        this.currentTrashTarget = null;
        if (this.tempDirtPos != null && this.tempDirtTimer > 30) {
            this.tempDirtTimer = 30;
        }
    }

    private boolean tryPlaceTemporaryDirt(ServerLevel sLevel) {
        if (this.tempDirtPos != null) return false;

        BlockPos groundPos = BlockPos.containing(this.getX(), this.getY() - 0.1, this.getZ());
        BlockPos feetPos = groundPos.above();

        BlockState stateAtFeet = sLevel.getBlockState(feetPos);
        boolean feetOk = stateAtFeet.isAir() || stateAtFeet.canBeReplaced();
        if (!feetOk) return false;

        BlockPos headPos = feetPos.above();
        BlockState stateAtHead = sLevel.getBlockState(headPos);
        boolean headOk = stateAtHead.isAir() || stateAtHead.canBeReplaced();
        if (!headOk) return false;

        BlockPos newHeadPos = feetPos.above(2);
        BlockState stateAbove = sLevel.getBlockState(newHeadPos);
        boolean aboveOk = stateAbove.isAir() || stateAbove.canBeReplaced() || !stateAbove.isSolid();
        if (!aboveOk) return false;

        this.previousBlockState = stateAtFeet;
        sLevel.setBlockAndUpdate(feetPos, net.minecraft.world.level.block.Blocks.DIRT.defaultBlockState());
        this.teleportTo(feetPos.getX() + 0.5, feetPos.getY() + 1.0, feetPos.getZ() + 0.5);
        this.setDeltaMovement(0, 0.1, 0);
        this.getNavigation().stop();

        sLevel.playSound(null, feetPos, SoundEvents.GRAVEL_PLACE, SoundSource.BLOCKS, 1.0f, 0.8f);
        sLevel.sendParticles(new net.minecraft.core.particles.BlockParticleOption(ParticleTypes.BLOCK, net.minecraft.world.level.block.Blocks.DIRT.defaultBlockState()),
                feetPos.getX() + 0.5, feetPos.getY() + 0.5, feetPos.getZ() + 0.5, 8, 0.2, 0.2, 0.2, 0.05);

        this.tempDirtPos = feetPos.immutable();
        this.tempDirtTimer = 80;
        this.trashStuckTicks = 0;
        return true;
    }

    private void removeTempDirt(ServerLevel sLevel) {
        if (tempDirtPos != null) {
            if (sLevel.getBlockState(tempDirtPos).is(net.minecraft.world.level.block.Blocks.DIRT)) {
                BlockState toRestore = (previousBlockState != null && !previousBlockState.is(net.minecraft.world.level.block.Blocks.DIRT))
                        ? previousBlockState
                        : net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
                sLevel.setBlockAndUpdate(tempDirtPos, toRestore);
                sLevel.playSound(null, tempDirtPos, SoundEvents.GRASS_BREAK, SoundSource.BLOCKS, 0.8f, 1.2f);
                sLevel.sendParticles(ParticleTypes.POOF, tempDirtPos.getX() + 0.5, tempDirtPos.getY() + 0.5, tempDirtPos.getZ() + 0.5, 6, 0.2, 0.2, 0.2, 0.03);
            }
            tempDirtPos = null;
            tempDirtTimer = 0;
            previousBlockState = null;
        }
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        if (tempDirtPos != null && this.level() instanceof ServerLevel sLevel) {
            removeTempDirt(sLevel);
        }
        super.remove(reason);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (ownerUuid != null) tag.putString("OwnerUuid", ownerUuid.toString());
        if (assignedAnimalUuid != null) tag.putString("AssignedAnimalUuid", assignedAnimalUuid.toString());
        tag.putInt("FoodStock", getFoodStock());
        tag.putString("DutyState", currentDutyState);
        tag.putBoolean("HasWateringCan", hasWateringCan);
        if (!wateringCanStack.isEmpty()) {
            tag.put("WateringCanStack", wateringCanStack.save(this.registryAccess()));
        }
        if (!brushStack.isEmpty()) {
            tag.put("BrushStack", brushStack.save(this.registryAccess()));
        }
        if (tempDirtPos != null) {
            tag.putInt("TempDirtX", tempDirtPos.getX());
            tag.putInt("TempDirtY", tempDirtPos.getY());
            tag.putInt("TempDirtZ", tempDirtPos.getZ());
            tag.putInt("TempDirtTimer", tempDirtTimer);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        String oStr = tag.getString("OwnerUuid");
        if (!oStr.isEmpty()) {
            try { this.ownerUuid = UUID.fromString(oStr); } catch (Exception ignored) {}
        }
        String aStr = tag.getString("AssignedAnimalUuid");
        if (!aStr.isEmpty()) {
            try { this.assignedAnimalUuid = UUID.fromString(aStr); } catch (Exception ignored) {}
        }
        this.setFoodStock(tag.getInt("FoodStock"));
        if (tag.contains("DutyState")) {
            this.currentDutyState = tag.getString("DutyState");
        }
        if (tag.contains("HasWateringCan")) {
            this.hasWateringCan = tag.getBoolean("HasWateringCan");
        }
        if (tag.contains("WateringCanStack")) {
            this.wateringCanStack = ItemStack.parseOptional(this.registryAccess(), tag.getCompound("WateringCanStack"));
            if (!this.wateringCanStack.isEmpty()) {
                this.hasWateringCan = true;
            }
        }
        if (tag.contains("BrushStack")) {
            this.brushStack = ItemStack.parseOptional(this.registryAccess(), tag.getCompound("BrushStack"));
        }
        this.entityData.set(HAS_BATHING_TOOLS, hasBathingTools());

        if (tag.contains("TempDirtTimer")) {
            this.tempDirtTimer = tag.getInt("TempDirtTimer");
            this.tempDirtPos = new BlockPos(tag.getInt("TempDirtX"), tag.getInt("TempDirtY"), tag.getInt("TempDirtZ"));
        }
    }
}

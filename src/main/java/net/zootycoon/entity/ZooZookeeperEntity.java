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
import net.zootycoon.animal.ZooAnimalHungerManager;
import net.zootycoon.util.ZooTranslations;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ZooZookeeperEntity extends PathfinderMob {

    private static final EntityDataAccessor<Integer> FOOD_STOCK_DATA =
            SynchedEntityData.defineId(ZooZookeeperEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> SWEEPING =
            SynchedEntityData.defineId(ZooZookeeperEntity.class, EntityDataSerializers.BOOLEAN);

    private static final String[] NAMES = new String[]{
            "Siti Rahma", "Agus Setiawan", "Rudi Hartono", "Dewi Lestari",
            "Bambang Pamungkas", "Nurul Hidayah", "Hendra Wijaya", "Indah Permata",
            "Joko Supriyanto", "Rina Marlina"
    };

    private UUID ownerUuid = null;
    private UUID assignedAnimalUuid = null;
    private String currentDutyState = "IDLE"; // "REFILL", "NEED_FOOD", "SWEEPING", "IDLE"
    private int actionCooldown = 0;

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(FOOD_STOCK_DATA, 16); // Starts with 16 food units
        builder.define(SWEEPING, false);
    }

    public ZooZookeeperEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.setPersistenceRequired();

        if (!level.isClientSide()) {
            int nameIdx = this.random.nextInt(NAMES.length);
            this.setCustomName(Component.literal(NAMES[nameIdx] + " - Zookeeper"));
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
        // 1. PRIORITY 1: Check Visitor Litter (TrashBlock) & Sweep
        // As requested: clean trash first, then return to following animal
        // ========================================================
        BlockPos nearestTrash = null;
        double minTrashDistSq = Double.MAX_VALUE;

        for (int dx = -14; dx <= 14; dx += 2) {
            for (int dz = -14; dz <= 14; dz += 2) {
                for (int dy = -2; dy <= 2; dy++) {
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
            currentDutyState = "SWEEPING";
            this.entityData.set(SWEEPING, true);
            // Hold broom in hand
            if (!this.getMainHandItem().is(ZooItems.BROOM)) {
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ZooItems.BROOM));
            }

            double distSq = this.distanceToSqr(nearestTrash.getX() + 0.5, nearestTrash.getY(), nearestTrash.getZ() + 0.5);
            if (distSq > 2.2 * 2.2) {
                this.getNavigation().moveTo(nearestTrash.getX() + 0.5, nearestTrash.getY(), nearestTrash.getZ() + 0.5, 1.15);
            } else {
                // Sweep litter!
                this.swing(InteractionHand.MAIN_HAND);
                sLevel.playSound(null, nearestTrash, SoundEvents.BRUSH_GENERIC, SoundSource.BLOCKS, 1.0f, 1.0f);
                sLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, nearestTrash.getX() + 0.5, nearestTrash.getY() + 0.3, nearestTrash.getZ() + 0.5, 6, 0.2, 0.1, 0.2, 0.02);
                sLevel.destroyBlock(nearestTrash, false, this);
                actionCooldown = 30;
            }
            return;
        }

        // Cleaned or no trash: remove broom
        if (this.getMainHandItem().is(ZooItems.BROOM)) {
            this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }
        this.entityData.set(SWEEPING, false);

        // ========================================================
        // 2. PRIORITY 2: Check Feeding Trough if assigned animal is hungry (<= 25% hunger)
        // ========================================================
        if (assignedAnimal != null) {
            int animalHunger = ZooAnimalHungerManager.getHunger(assignedAnimal);
            if (animalHunger <= 25) {
                var diet = AnimalDietRegistry.getDiet(assignedAnimal);
                Item targetFoodItem = Items.WHEAT;
                if (diet != null && !diet.acceptedFoods.isEmpty()) {
                    targetFoodItem = diet.acceptedFoods.get(0);
                } else if (assignedAnimal instanceof net.minecraft.world.entity.animal.Animal a) {
                    if (a.isFood(new ItemStack(Items.CARROT))) targetFoodItem = Items.CARROT;
                    else if (a.isFood(new ItemStack(Items.WHEAT_SEEDS))) targetFoodItem = Items.WHEAT_SEEDS;
                    else if (a.isFood(new ItemStack(Items.BAMBOO))) targetFoodItem = Items.BAMBOO;
                    else if (a.isFood(new ItemStack(Items.APPLE))) targetFoodItem = Items.APPLE;
                }

                BlockPos needRefillTrough = null;
                double minTroughDistSq = Double.MAX_VALUE;

                for (BlockPos troughPos : ZooFeedingTroughBlock.KNOWN_TROUGHS) {
                    if (ownerUuid != null && !claimManager.isClaimedBy(troughPos, ownerUuid)) continue;
                    double distSq = myPos.distSqr(troughPos);
                    if (distSq > 32 * 32) continue;

                    BlockEntity be = sLevel.getBlockEntity(troughPos);
                    if (be instanceof ZooFeedingTroughBlockEntity troughBe) {
                        if (troughBe.hasWater()) continue;
                        ItemStack foodInTrough = troughBe.getFoodStack();
                        boolean matchingFood = foodInTrough.is(targetFoodItem) && foodInTrough.getCount() < 24;
                        boolean emptyForMyAnimal = foodInTrough.isEmpty()
                                && !troughBe.isPlacementCooldown(sLevel.getGameTime())
                                && assignedAnimal.blockPosition().distSqr(troughPos) <= 24 * 24;

                        if (matchingFood || emptyForMyAnimal) {
                            if (distSq < minTroughDistSq) {
                                minTroughDistSq = distSq;
                                needRefillTrough = troughPos;
                            }
                        }
                    }
                }

                if (needRefillTrough != null) {
                    currentDutyState = "REFILL";
                    this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(targetFoodItem));

                    double distSq = this.distanceToSqr(needRefillTrough.getX() + 0.5, needRefillTrough.getY(), needRefillTrough.getZ() + 0.5);
                    if (distSq > 2.5 * 2.5) {
                        this.getNavigation().moveTo(needRefillTrough.getX() + 0.5, needRefillTrough.getY(), needRefillTrough.getZ() + 0.5, 1.2);
                    } else {
                        // Refill the trough with the assigned animal's food!
                        BlockEntity be = sLevel.getBlockEntity(needRefillTrough);
                        if (be instanceof ZooFeedingTroughBlockEntity troughBe) {
                            troughBe.insertFood(new ItemStack(targetFoodItem, 8), 8);
                            sLevel.playSound(null, needRefillTrough, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.9f, 1.2f);
                            sLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, needRefillTrough.getX() + 0.5, needRefillTrough.getY() + 0.8, needRefillTrough.getZ() + 0.5, 6, 0.2, 0.2, 0.2, 0.02);
                            actionCooldown = 60; // 3 seconds cooldown between refills
                        }
                    }
                    return;
                }
            }
        }

        // If not refilling a trough, clear food from hand if held
        if ("REFILL".equals(currentDutyState)) {
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
        } else if ("SWEEPING".equals(currentDutyState)) {
            dialogText = ZooTranslations.get("zootycoon.dialog.zookeeper.sweeping");
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
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack st = player.getInventory().getItem(i);
            if (!st.isEmpty()) {
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
            ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.success", "zootycoon.zookeeper.food_given_toast", 1, totalGiven, anDesc);
        } else {
            this.playSound(SoundEvents.VILLAGER_NO, 1.0f, 1.0f);
            ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.warning", "zootycoon.zookeeper.wrong_food_toast", 2, anDesc);
        }
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

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (ownerUuid != null) tag.putString("OwnerUuid", ownerUuid.toString());
        if (assignedAnimalUuid != null) tag.putString("AssignedAnimalUuid", assignedAnimalUuid.toString());
        tag.putInt("FoodStock", getFoodStock());
        tag.putString("DutyState", currentDutyState);
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
    }
}

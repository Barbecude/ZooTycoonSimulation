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

import java.util.List;
import java.util.UUID;

public class ZooZookeeperEntity extends PathfinderMob {

    private static final EntityDataAccessor<Integer> FOOD_STOCK_DATA =
            SynchedEntityData.defineId(ZooZookeeperEntity.class, EntityDataSerializers.INT);

    private static final String[] NAMES = new String[]{
            "Siti Rahma", "Agus Setiawan", "Rudi Hartono", "Dewi Lestari",
            "Bambang Pamungkas", "Nurul Hidayah", "Hendra Wijaya", "Indah Permata",
            "Joko Supriyanto", "Rina Marlina"
    };

    private UUID ownerUuid = null;
    private String currentDutyState = "IDLE"; // "REFILL", "NEED_FOOD", "SWEEPING", "IDLE"
    private int actionCooldown = 0;

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(FOOD_STOCK_DATA, 16); // Starts with 16 food units
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
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(2, new ZookeeperClaimStrollGoal(this, 0.4));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
    }

    public static class ZookeeperClaimStrollGoal extends WaterAvoidingRandomStrollGoal {
        private final ZooZookeeperEntity zookeeper;

        public ZookeeperClaimStrollGoal(ZooZookeeperEntity mob, double speedModifier) {
            super(mob, speedModifier);
            this.zookeeper = mob;
        }

        @Override
        public boolean canUse() {
            if (this.zookeeper.ownerUuid == null) return super.canUse();
            return super.canUse();
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) return;
        ServerLevel sLevel = (ServerLevel) this.level();

        if (actionCooldown > 0) actionCooldown--;

        // Restrict to owner's claimed land
        if (ownerUuid != null) {
            ZooClaimManager claimManager = ZooClaimManager.get(sLevel);
            BlockPos currentPos = this.blockPosition();

            if (!claimManager.isClaimedBy(currentPos, ownerUuid)) {
                returnToClaimedLand(sLevel, claimManager);
            } else if (this.getNavigation().isInProgress()) {
                BlockPos targetPos = this.getNavigation().getTargetPos();
                if (targetPos != null && !claimManager.isClaimedBy(targetPos, ownerUuid)) {
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
        List<ZooClaim> playerClaims = claimManager.getPlayerClaims(ownerUuid);
        if (playerClaims.isEmpty()) return;

        ZooClaim closestClaim = null;
        double minDistanceSq = Double.MAX_VALUE;
        BlockPos currentPos = this.blockPosition();

        for (ZooClaim claim : playerClaims) {
            var box = claim.getBox();
            int cx = (box.minX() + box.maxX()) / 2;
            int cz = (box.minZ() + box.maxZ()) / 2;
            double d = currentPos.distSqr(new net.minecraft.core.Vec3i(cx, currentPos.getY(), cz));
            if (d < minDistanceSq) {
                minDistanceSq = d;
                closestClaim = claim;
            }
        }

        if (closestClaim == null) return;

        var box = closestClaim.getBox();
        int targetX = Math.clamp(currentPos.getX(), box.minX() + 1, box.maxX() - 1);
        int targetZ = Math.clamp(currentPos.getZ(), box.minZ() + 1, box.maxZ() - 1);
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

        // ========================================================
        // 1. PRIORITY 1: Check Feeding Troughs in Zoo
        // ========================================================
        BlockPos needRefillTrough = null;
        for (BlockPos troughPos : ZooFeedingTroughBlock.KNOWN_TROUGHS) {
            if (ownerUuid != null && !claimManager.isClaimedBy(troughPos, ownerUuid)) continue;
            if (myPos.distSqr(troughPos) > 32 * 32) continue;

            BlockEntity be = sLevel.getBlockEntity(troughPos);
            if (be instanceof ZooFeedingTroughBlockEntity troughBe) {
                if (!troughBe.hasWater() && (troughBe.isEmpty() || troughBe.getFoodStack().getCount() < 24)) {
                    needRefillTrough = troughPos;
                    break;
                }
            }
        }

        if (needRefillTrough != null) {
            if (getFoodStock() > 0) {
                currentDutyState = "REFILL";
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.WHEAT));

                double distSq = this.distanceToSqr(needRefillTrough.getX() + 0.5, needRefillTrough.getY(), needRefillTrough.getZ() + 0.5);
                if (distSq > 2.5 * 2.5) {
                    this.getNavigation().moveTo(needRefillTrough.getX() + 0.5, needRefillTrough.getY(), needRefillTrough.getZ() + 0.5, 1.2);
                } else {
                    // Refill the trough!
                    BlockEntity be = sLevel.getBlockEntity(needRefillTrough);
                    if (be instanceof ZooFeedingTroughBlockEntity troughBe) {
                        troughBe.insertFood(new ItemStack(Items.WHEAT, 8), 8);
                        setFoodStock(Math.max(0, getFoodStock() - 1));
                        sLevel.playSound(null, needRefillTrough, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.9f, 1.2f);
                        sLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, needRefillTrough.getX() + 0.5, needRefillTrough.getY() + 0.8, needRefillTrough.getZ() + 0.5, 6, 0.2, 0.2, 0.2, 0.02);
                        actionCooldown = 40;
                    }
                }
                return;
            } else {
                currentDutyState = "NEED_FOOD";
                this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                // Look for nearby player to ask for food
                Player nearestPlayer = sLevel.getNearestPlayer(this, 12.0);
                if (nearestPlayer != null) {
                    this.getLookControl().setLookAt(nearestPlayer, 30.0f, 30.0f);
                }
                return;
            }
        }

        // ========================================================
        // 2. PRIORITY 2: Check Visitor Litter (TrashBlock) & Sweep
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

        // ========================================================
        // 3. PRIORITY 3: All Good, Patrol
        // ========================================================
        currentDutyState = "IDLE";
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

            ItemStack held = player.getItemInHand(hand);
            boolean isFood = AnimalDietRegistry.isAnyTroughFood(held)
                    || held.is(Items.WHEAT) || held.is(Items.CARROT) || held.is(Items.APPLE)
                    || held.is(Items.POTATO) || held.is(Items.BEETROOT) || held.is(Items.SWEET_BERRIES);

            // If player hands food to Zookeeper
            if (isFood && !held.isEmpty()) {
                int take = Math.min(held.getCount(), 16);
                held.shrink(take);
                this.addFoodStock(take);
                this.playSound(SoundEvents.VILLAGER_YES, 1.0f, 1.0f);
                ((ServerLevel) this.level()).sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 1.2, this.getZ(), 6, 0.2, 0.2, 0.2, 0.02);
                ZooNetworkPayloads.sendToast(sp, "zootycoon.toast.title.success", "zootycoon.zookeeper.received_food_toast", 1, take, getFoodStock());
                return InteractionResult.SUCCESS;
            }

            // Dynamic conversational dialogue
            if ("NEED_FOOD".equals(currentDutyState) || getFoodStock() <= 0) {
                this.playSound(SoundEvents.VILLAGER_NO, 1.0f, 1.0f);
                ZooNetworkPayloads.sendToast(sp, "zootycoon.toast.title.warning", "zootycoon.zookeeper.ask_food_toast", 2);
            } else if ("SWEEPING".equals(currentDutyState)) {
                this.playSound(SoundEvents.VILLAGER_YES, 1.0f, 1.2f);
                ZooNetworkPayloads.sendToast(sp, "zootycoon.toast.title.info", "zootycoon.zookeeper.sweeping_toast", 0);
            } else {
                this.playSound(SoundEvents.VILLAGER_YES, 1.0f, 1.0f);
                ZooNetworkPayloads.sendToast(sp, "zootycoon.toast.title.info", "zootycoon.zookeeper.all_good_toast", 1, getFoodStock());
            }

            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    public void handleFoodTransfer(ServerPlayer player) {
        // Player gives food from inventory
        int totalGiven = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack st = player.getInventory().getItem(i);
            if (!st.isEmpty() && AnimalDietRegistry.isAnyTroughFood(st)) {
                int toTake = Math.min(st.getCount(), 16);
                st.shrink(toTake);
                this.addFoodStock(toTake);
                totalGiven += toTake;
                break;
            }
        }
        if (totalGiven > 0) {
            player.getInventory().setChanged();
            this.playSound(SoundEvents.VILLAGER_YES, 1.0f, 1.0f);
            ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.success", "zootycoon.zookeeper.received_food_toast", 1, totalGiven, getFoodStock());
        }
    }

    public void setOwnerUuid(UUID uuid) {
        this.ownerUuid = uuid;
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
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
        this.setFoodStock(tag.getInt("FoodStock"));
        if (tag.contains("DutyState")) {
            this.currentDutyState = tag.getString("DutyState");
        }
    }
}

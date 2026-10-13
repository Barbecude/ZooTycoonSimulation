package net.zootycoon.entity;

import net.minecraft.world.level.Level;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.levelgen.Heightmap;
import net.zootycoon.animal.AnimalDietRegistry;
import net.zootycoon.animal.ZooAnimalHungerManager;
import net.zootycoon.claim.ZooClaim;
import net.zootycoon.claim.ZooClaimManager;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.zootycoon.network.ZooNetworkPayloads;

import java.util.ArrayList;
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
    private UUID assignedAnimalUuid = null;
    private int feedCooldown = 0;
    private int reminderCooldown = 0;

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(FOOD_STOCK_DATA, 0);
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
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(2, new ZookeeperClaimStrollGoal(this, 0.4));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
    }

    public static class ZookeeperClaimStrollGoal extends WaterAvoidingRandomStrollGoal {
        private final ZooZookeeperEntity zookeeper;

        public ZookeeperClaimStrollGoal(ZooZookeeperEntity mob, double speed) {
            super(mob, speed);
            this.zookeeper = mob;
        }

        @Override
        protected Vec3 getPosition() {
            if (this.zookeeper.ownerUuid == null || !(this.mob.level() instanceof ServerLevel sl)) {
                return super.getPosition();
            }
            ZooClaimManager cm = ZooClaimManager.get(sl);
            for (int i = 0; i < 10; i++) {
                Vec3 candidate = LandRandomPos.getPos(this.mob, 10, 4);
                if (candidate != null && cm.isClaimedBy(BlockPos.containing(candidate), this.zookeeper.ownerUuid)) {
                    return candidate;
                }
            }
            return null; // Don't stroll if outside owner's claimed land
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) return;
        ServerLevel sLevel = (ServerLevel) this.level();

        if (feedCooldown > 0) feedCooldown--;
        if (reminderCooldown > 0) reminderCooldown--;

        // Zookeeper TIDAK BOLEH keluar dari claimed land pemilik!
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

        // Every 30 ticks, check animal pairing and feeding
        if (this.tickCount % 30 == 0) {
            handleAnimalCare(sLevel);
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
            // Jika terlempar/berada jauh di luar lahan, teleport aman kembali ke dalam
            this.teleportTo(safePos.getX() + 0.5, safePos.getY(), safePos.getZ() + 0.5);
            this.getNavigation().stop();
            sLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 1.0, this.getZ(), 8, 0.2, 0.2, 0.2, 0.02);
        } else {
            // Berjalan kembali ke dalam perbatasan lahan
            this.getNavigation().moveTo(safePos.getX() + 0.5, safePos.getY(), safePos.getZ() + 0.5, 1.25);
        }
    }

    private void handleAnimalCare(ServerLevel sLevel) {
        LivingEntity assignedAnimal = null;

        // 1. Resolve currently assigned animal
        if (assignedAnimalUuid != null) {
            Entity ent = sLevel.getEntity(assignedAnimalUuid);
            if (ent instanceof LivingEntity living && living.isAlive()) {
                assignedAnimal = living;
            } else {
                assignedAnimalUuid = null;
            }
        }

        // Satwa HANYA mendapatkan/mempertahankan zookeeper jika berada di claimed land!
        if (assignedAnimal != null && ownerUuid != null) {
            ZooClaimManager claimManager = ZooClaimManager.get(sLevel);
            if (!claimManager.isClaimedBy(assignedAnimal.blockPosition(), ownerUuid)) {
                // Satwa keluar dari claimed land -> lepas penugasan zookeeper
                ZooAnimalHungerManager.setAssignedZookeeper(assignedAnimal, null);
                this.assignedAnimalUuid = null;
                assignedAnimal = null;
                this.getNavigation().stop();
            }
        }

        // 2. If no animal assigned, find an untended TAGGED animal inside owner's claims
        if (assignedAnimal == null && ownerUuid != null) {
            assignedAnimal = findUntendedAnimal(sLevel);
            if (assignedAnimal != null) {
                assignedAnimalUuid = assignedAnimal.getUUID();
                ZooAnimalHungerManager.setAssignedZookeeper(assignedAnimal, this.getUUID());
                reminderCooldown = 300;
            }
        }

        // 3. Care routine for assigned animal
        if (assignedAnimal != null) {
            double distSqr = this.distanceToSqr(assignedAnimal);

            // Move closer if too far
            if (distSqr > 6.0 * 6.0) {
                this.getNavigation().moveTo(assignedAnimal, 1.0);
            }

            // If out of food, try picking up from nearby animal feeder / trough container
            if (getFoodStock() <= 0 && this.tickCount % 60 == 0) {
                searchAndTakeFoodFromFeeders(sLevel, assignedAnimal);
            }

            // Check hunger
            int hunger = ZooAnimalHungerManager.getHunger(assignedAnimal);
            if (hunger <= 85 && feedCooldown <= 0) {
                performFeeding(sLevel, assignedAnimal);
            }

            // Update hand item visual based on diet and stock
            var diet = AnimalDietRegistry.getDiet(assignedAnimal);
            if (getFoodStock() > 0 && !diet.acceptedFoods.isEmpty()) {
                Item preferred = diet.acceptedFoods.get(0);
                if (!this.getMainHandItem().is(preferred)) {
                    this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(preferred));
                }
            } else if (getFoodStock() <= 0) {
                this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            }
        }
    }

    private void searchAndTakeFoodFromFeeders(ServerLevel sLevel, LivingEntity animal) {
        BlockPos center = this.blockPosition();
        for (BlockPos bp : BlockPos.betweenClosed(center.offset(-6, -2, -6), center.offset(6, 2, 6))) {
            var be = sLevel.getBlockEntity(bp);
            if (be instanceof net.minecraft.world.Container container) {
                for (int i = 0; i < container.getContainerSize(); i++) {
                    ItemStack st = container.getItem(i);
                    if (!st.isEmpty() && AnimalDietRegistry.isFoodValid(animal, st)) {
                        int take = Math.min(st.getCount(), 8);
                        this.addFoodStock(take);
                        st.shrink(take);
                        container.setChanged();
                        sLevel.playSound(null, bp, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.8f, 1.0f);
                        sLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 1.0, this.getZ(), 4, 0.2, 0.2, 0.2, 0.02);
                        return;
                    }
                }
            }
        }
    }

    private LivingEntity findUntendedAnimal(ServerLevel sLevel) {
        if (ownerUuid == null) return null;
        ZooClaimManager claimManager = ZooClaimManager.get(sLevel);
        var aabb = this.getBoundingBox().inflate(48.0);
        List<LivingEntity> animals = sLevel.getEntitiesOfClass(LivingEntity.class, aabb,
                ZooAnimalHungerManager::isZooAnimalCandidate
        );

        for (LivingEntity a : animals) {
            // Zookeeper HANYA merawat satwa yang terdaftar, milik owner, DAN berada di claimed land owner!
            if (!ZooAnimalHungerManager.isTagged(a)) continue;
            if (!ZooAnimalHungerManager.isOwnedBy(a, ownerUuid)) continue;
            if (!claimManager.isClaimedBy(a.blockPosition(), ownerUuid)) continue;

            UUID existingKeeper = ZooAnimalHungerManager.getAssignedZookeeper(a);
            if (existingKeeper == null || existingKeeper.equals(this.getUUID())) {
                return a;
            }
        }
        return null;
    }

    private void performFeeding(ServerLevel sLevel, LivingEntity animal) {
        if (getFoodStock() <= 0) {
            return;
        }

        var diet = AnimalDietRegistry.getDiet(animal);
        Item foodItem = !diet.acceptedFoods.isEmpty() ? diet.acceptedFoods.get(0) : Items.WHEAT;
        ItemStack foodStack = new ItemStack(foodItem, 2);

        if (diet.feedingType == AnimalDietRegistry.FeedingType.THROWN_MEAT) {
            this.getLookControl().setLookAt(animal);
            ZooAnimalHungerManager.throwFoodAtAnimal(sLevel, this, animal, foodStack.copyWithCount(1));
            ZooAnimalHungerManager.feedAnimal(animal, foodStack, null);
        } else {
            ZooAnimalHungerManager.feedAnimal(animal, foodStack, null);
        }

        setFoodStock(Math.max(0, getFoodStock() - 1));
        feedCooldown = 300; // 15 seconds cooldown between feeds
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        Level lvl = this.level();
        if (lvl.isClientSide()) return InteractionResult.SUCCESS;
        ServerPlayer sPlayer = (ServerPlayer) player;

        // If no animal assigned yet, try finding one
        if (assignedAnimalUuid == null) {
            this.ownerUuid = player.getUUID();
            LivingEntity found = findUntendedAnimal((ServerLevel) lvl);
            if (found != null) {
                this.assignedAnimalUuid = found.getUUID();
                ZooAnimalHungerManager.setAssignedZookeeper(found, this.getUUID());
            }
        }

        // Open Dialog Screen (zero chat spam)
        openDialogScreen(sPlayer, null);
        return InteractionResult.SUCCESS;
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

        String dialogText;
        if (dialogOverride != null) {
            dialogText = dialogOverride;
        } else if (!hasAnimal) {
            dialogText = net.zootycoon.util.ZooTranslations.get("zootycoon.dialog.zookeeper.no_animal");
        } else if (getFoodStock() <= 0) {
            dialogText = net.zootycoon.util.ZooTranslations.get("zootycoon.dialog.zookeeper.empty_stock", animalName);
        } else {
            dialogText = net.zootycoon.util.ZooTranslations.get("zootycoon.dialog.zookeeper.has_stock", animalName, getFoodStock());
        }

        ServerPlayNetworking.send(player, new ZooNetworkPayloads.OpenZookeeperScreenPayload(
                this.getUUID(), keeperName, hasAnimal, animalName, hunger, this.getFoodStock(), dialogText, foods
        ));
    }

    public void handleFoodTransfer(ServerPlayer player) {
        if (assignedAnimalUuid == null) {
            ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.warning", "zootycoon.zookeeper.no_animal_toast", 2);
            openDialogScreen(player, net.zootycoon.util.ZooTranslations.get("zootycoon.dialog.zookeeper.no_animal"));
            return;
        }
        Entity aEnt = ((ServerLevel) this.level()).getEntity(assignedAnimalUuid);
        if (!(aEnt instanceof LivingEntity animal)) {
            ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.warning", "zootycoon.zookeeper.animal_missing_toast", 2);
            openDialogScreen(player, net.zootycoon.util.ZooTranslations.get("zootycoon.dialog.zookeeper.animal_missing"));
            return;
        }

        int totalGiven = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack st = player.getInventory().getItem(i);
            if (!st.isEmpty() && AnimalDietRegistry.isFoodValid(animal, st)) {
                int toTake = Math.min(st.getCount(), 32 - totalGiven);
                st.shrink(toTake);
                this.addFoodStock(toTake);
                totalGiven += toTake;
                if (totalGiven >= 32) break;
            }
        }
        player.getInventory().setChanged();

        String aName = animal.hasCustomName() ? animal.getCustomName().getString() : animal.getType().getDescription().getString();
        if (totalGiven > 0) {
            ServerLevel sLevel = (ServerLevel) this.level();
            sLevel.playSound(null, this.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 1.0f, 1.0f);
            sLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 1.0, this.getZ(), 5, 0.2, 0.2, 0.2, 0.02);
            // Immediately feed the assigned animal so love particles appear right away!
            this.feedCooldown = 0;
            this.performFeeding(sLevel, animal);
            ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.success", "zootycoon.zookeeper.food_given_toast", 1, totalGiven, aName);
            openDialogScreen(player, net.zootycoon.util.ZooTranslations.get("zootycoon.dialog.zookeeper.received_food", totalGiven, aName, this.getFoodStock()));
        } else {
            ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.warning", "zootycoon.zookeeper.wrong_food_toast", 2, aName);
            openDialogScreen(player, net.zootycoon.util.ZooTranslations.get("zootycoon.dialog.zookeeper.wrong_food", aName));
        }
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
    }
}

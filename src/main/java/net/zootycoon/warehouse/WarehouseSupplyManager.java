package net.zootycoon.warehouse;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.zootycoon.animal.AnimalDietRegistry;
import net.zootycoon.block.EmptyBarrelBlockEntity;
import net.zootycoon.claim.ZooClaim;
import net.zootycoon.claim.ZooClaimManager;
import net.zootycoon.entity.ZooCashierEntity;
import net.zootycoon.entity.ZooZookeeperEntity;
import net.zootycoon.item.ZooItems;
import net.zootycoon.network.WarehousePayloads;
import net.zootycoon.network.ZooNetworkPayloads;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class WarehouseSupplyManager {

    public static final int CASHIER_LOW_STOCK_THRESHOLD = 8;
    public static final int ZOOKEEPER_LOW_STOCK_THRESHOLD = 16;
    public static final int ZOOKEEPER_REQUIRED_FOOD = 32;
    public static final long FULFILLMENT_DELAY_TICKS = 80L; // 4 seconds delay

    public static class StaffSupplyRequest {
        public final UUID staffUuid;
        public int staffEntityId;
        public final String category; // "BURGER", "DRINK", "ANIMAL_FOOD"
        public final String staffType; // "CASHIER" or "ZOOKEEPER"
        public String staffName;
        public String subtitle;
        public UUID animalUuid;
        public String animalName;

        // For Cashier: 1 or 2 random finished burger/drink stacks
        // For Zookeeper: accepted food display stacks
        public final List<ItemStack> targetItems = new ArrayList<>();

        // For Cashier: raw ingredient -> count needed
        public final Map<Item, Integer> requiredRawIngredients = new LinkedHashMap<>();

        // Delay tracking once barrel has sufficient items
        public long readySinceGameTime = -1L;

        public StaffSupplyRequest(UUID staffUuid, int staffEntityId, String category, String staffType, String staffName, String subtitle) {
            this.staffUuid = staffUuid;
            this.staffEntityId = staffEntityId;
            this.category = category;
            this.staffType = staffType;
            this.staffName = staffName;
            this.subtitle = subtitle;
        }
    }

    private static final Map<UUID, StaffSupplyRequest> ACTIVE_REQUESTS = new HashMap<>();
    private static final Random RANDOM = new Random();

    public static void reset() {
        ACTIVE_REQUESTS.clear();
        WarehouseRecipeResolver.reset();
    }

    public static void tickBarrel(ServerLevel sLevel, EmptyBarrelBlockEntity barrel) {
        if (barrel == null || !barrel.isCategoryAssigned()) return;
        String category = barrel.getCategory();

        // 1. Discover/refresh requests for staff in the same zoo or nearby
        refreshStaffRequests(sLevel, barrel, category);

        // 2. Check if barrel can fulfill any active request for this category
        long now = sLevel.getGameTime();
        boolean anyFulfilled = false;

        List<StaffSupplyRequest> matching = getMatchingRequestsForBarrel(sLevel, barrel, category);
        for (StaffSupplyRequest req : matching) {
            if (canBarrelFulfill(sLevel, barrel, req)) {
                if (req.readySinceGameTime < 0L) {
                    req.readySinceGameTime = now;
                } else if (now - req.readySinceGameTime >= FULFILLMENT_DELAY_TICKS) {
                    if (consumeAndFulfill(sLevel, barrel, req)) {
                        ACTIVE_REQUESTS.remove(req.staffUuid);
                        anyFulfilled = true;
                    } else {
                        req.readySinceGameTime = -1L;
                    }
                }
            } else {
                req.readySinceGameTime = -1L;
            }
        }

        if (anyFulfilled) {
            barrel.syncToViewingPlayers(sLevel);
        }
    }

    public static void syncRequestsToPlayer(ServerLevel sLevel, EmptyBarrelBlockEntity barrel, ServerPlayer player) {
        if (barrel == null || player == null) return;
        String category = barrel.getCategory();
        refreshStaffRequests(sLevel, barrel, category);

        List<StaffSupplyRequest> matching = getMatchingRequestsForBarrel(sLevel, barrel, category);
        List<WarehousePayloads.StaffRequestDto> dtos = new ArrayList<>();

        for (StaffSupplyRequest req : matching) {
            // Request immediately disappears from UI as soon as the barrel is filled with sufficient items
            if (canBarrelFulfill(sLevel, barrel, req)) {
                if (req.readySinceGameTime < 0L) {
                    req.readySinceGameTime = sLevel.getGameTime();
                }
                continue;
            }

            List<ItemStack> targets = new ArrayList<>();
            for (ItemStack st : req.targetItems) {
                targets.add(st.copy());
            }

            List<ItemStack> raws = new ArrayList<>();
            int currentCount = 0;
            int requiredCount = 0;

            if ("CASHIER".equals(req.staffType)) {
                for (Map.Entry<Item, Integer> e : req.requiredRawIngredients.entrySet()) {
                    int need = e.getValue();
                    int have = countItemInBarrel(barrel, e.getKey());
                    raws.add(new ItemStack(e.getKey(), need));
                    requiredCount += need;
                    currentCount += Math.min(have, need);
                }
            } else {
                requiredCount = ZOOKEEPER_REQUIRED_FOOD;
                LivingEntity animal = resolveAnimal(sLevel, req.animalUuid);
                currentCount = Math.min(requiredCount, countValidAnimalFoodInBarrel(barrel, animal, req.targetItems));
                for (ItemStack foodIcon : req.targetItems) {
                    raws.add(new ItemStack(foodIcon.getItem(), ZOOKEEPER_REQUIRED_FOOD));
                }
            }

            dtos.add(new WarehousePayloads.StaffRequestDto(
                    req.staffUuid,
                    req.staffEntityId,
                    req.staffType,
                    req.staffName,
                    req.subtitle,
                    targets,
                    raws,
                    currentCount,
                    requiredCount
            ));
        }

        ServerPlayNetworking.send(player, new WarehousePayloads.SyncBarrelRequestsPayload(
                barrel.getBlockPos(),
                category,
                dtos
        ));
    }

    private static void refreshStaffRequests(ServerLevel sLevel, EmptyBarrelBlockEntity barrel, String category) {
        // Prune dead or already-stocked staff requests
        Iterator<Map.Entry<UUID, StaffSupplyRequest>> it = ACTIVE_REQUESTS.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, StaffSupplyRequest> entry = it.next();
            StaffSupplyRequest req = entry.getValue();
            Entity ent = sLevel.getEntity(req.staffUuid);
            if (ent == null || !ent.isAlive()) {
                it.remove();
                continue;
            }
            req.staffEntityId = ent.getId();
            if (ent instanceof ZooCashierEntity cashier) {
                boolean invalidMenuPresent = false;
                for (ItemStack targetSt : req.targetItems) {
                    if (!ZooCashierEntity.isValidItemForRole(cashier.getCashierRole(), targetSt)) {
                        invalidMenuPresent = true;
                        break;
                    }
                }
                if (invalidMenuPresent
                        || !cashier.isRoleAssigned()
                        || !req.category.equalsIgnoreCase(cashier.getCashierRole())
                        || cashier.getTotalStockCount() > CASHIER_LOW_STOCK_THRESHOLD) {
                    it.remove();
                }
            } else if (ent instanceof ZooZookeeperEntity keeper) {
                if (!keeper.hasActiveAssignedAnimal()
                        || keeper.getFoodStock() > ZOOKEEPER_LOW_STOCK_THRESHOLD) {
                    it.remove();
                }
            }
        }

        // Scan staff entities in level
        for (Entity entity : sLevel.getAllEntities()) {
            if (!entity.isAlive()) continue;
            if (!isStaffInSameZooOrNearby(sLevel, barrel, entity.blockPosition())) continue;

            if (entity instanceof ZooCashierEntity cashier) {
                String role = cashier.getCashierRole();
                if (("BURGER".equalsIgnoreCase(role) || "DRINK".equalsIgnoreCase(role))
                        && role.equalsIgnoreCase(category)
                        && cashier.getTotalStockCount() <= CASHIER_LOW_STOCK_THRESHOLD) {
                    if (!ACTIVE_REQUESTS.containsKey(cashier.getUUID())) {
                        StaffSupplyRequest created = createCashierRequest(sLevel, cashier, role.toUpperCase());
                        if (created != null) {
                            ACTIVE_REQUESTS.put(cashier.getUUID(), created);
                        }
                    }
                }
            } else if (entity instanceof ZooZookeeperEntity keeper && EmptyBarrelBlockEntity.CAT_ANIMAL_FOOD.equalsIgnoreCase(category)) {
                if (keeper.hasActiveAssignedAnimal() && keeper.getFoodStock() <= ZOOKEEPER_LOW_STOCK_THRESHOLD) {
                    UUID animalId = keeper.getAssignedAnimalUuid();
                    LivingEntity animal = resolveAnimal(sLevel, animalId);
                    if (animal != null) {
                        StaffSupplyRequest existing = ACTIVE_REQUESTS.get(keeper.getUUID());
                        if (existing == null || !animalId.equals(existing.animalUuid)) {
                            StaffSupplyRequest created = createZookeeperRequest(keeper, animal);
                            ACTIVE_REQUESTS.put(keeper.getUUID(), created);
                        }
                    }
                }
            }
        }
    }

    private static List<StaffSupplyRequest> getMatchingRequestsForBarrel(ServerLevel sLevel, EmptyBarrelBlockEntity barrel, String category) {
        List<StaffSupplyRequest> list = new ArrayList<>();
        for (StaffSupplyRequest req : ACTIVE_REQUESTS.values()) {
            if (!req.category.equalsIgnoreCase(category)) continue;
            Entity ent = sLevel.getEntity(req.staffUuid);
            if (ent != null && ent.isAlive() && isStaffInSameZooOrNearby(sLevel, barrel, ent.blockPosition())) {
                list.add(req);
            }
        }
        return list;
    }

    private static boolean isStaffInSameZooOrNearby(ServerLevel sLevel, EmptyBarrelBlockEntity barrel, BlockPos staffPos) {
        BlockPos barrelPos = barrel.getBlockPos();
        if (barrelPos.distSqr(staffPos) <= 128.0D * 128.0D) {
            return true;
        }
        ZooClaimManager cm = ZooClaimManager.get(sLevel);
        var barrelClaim = cm.getClaimAt(barrelPos);
        var staffClaim = cm.getClaimAt(staffPos);
        if (barrelClaim.isPresent() && staffClaim.isPresent()) {
            UUID o1 = barrelClaim.get().getOwnerUuid();
            UUID o2 = staffClaim.get().getOwnerUuid();
            if (o1 != null && o1.equals(o2)) {
                return true;
            }
        }
        if (barrel.getOwnerUuid() != null && staffClaim.isPresent() && barrel.getOwnerUuid().equals(staffClaim.get().getOwnerUuid())) {
            return true;
        }
        return false;
    }

    private static StaffSupplyRequest createCashierRequest(ServerLevel sLevel, ZooCashierEntity cashier, String role) {
        List<ItemStack> menus = WarehouseRecipeResolver.pickRandomMenus(sLevel, role, RANDOM);
        if (menus.isEmpty()) return null;

        Map<Item, Integer> rawMap = WarehouseRecipeResolver.resolveRawIngredientsForMenus(sLevel, role, menus);
        if (rawMap.isEmpty()) return null;

        String name = cashier.getName().getString();
        String sub = "BURGER".equalsIgnoreCase(role) ? "Stand Burger" : "Stand Minuman";
        StaffSupplyRequest req = new StaffSupplyRequest(
                cashier.getUUID(),
                cashier.getId(),
                role,
                "CASHIER",
                name,
                sub
        );
        req.targetItems.addAll(menus);
        req.requiredRawIngredients.putAll(rawMap);
        return req;
    }

    private static StaffSupplyRequest createZookeeperRequest(ZooZookeeperEntity keeper, LivingEntity animal) {
        String keeperName = keeper.getName().getString();
        String animalName = animal.hasCustomName()
                ? animal.getCustomName().getString()
                : animal.getType().getDescription().getString();

        StaffSupplyRequest req = new StaffSupplyRequest(
                keeper.getUUID(),
                keeper.getId(),
                EmptyBarrelBlockEntity.CAT_ANIMAL_FOOD,
                "ZOOKEEPER",
                keeperName,
                "Satwa: " + animalName
        );
        req.animalUuid = animal.getUUID();
        req.animalName = animalName;

        var diet = AnimalDietRegistry.getDiet(animal);
        if (diet != null && !diet.acceptedFoods.isEmpty()) {
            for (Item food : diet.acceptedFoods) {
                if (food != null && food != Items.AIR) {
                    req.targetItems.add(new ItemStack(food, ZOOKEEPER_REQUIRED_FOOD));
                }
            }
        }
        if (req.targetItems.isEmpty()) {
            req.targetItems.add(new ItemStack(Items.WHEAT, ZOOKEEPER_REQUIRED_FOOD));
        }
        return req;
    }

    public static boolean canBarrelFulfill(ServerLevel sLevel, EmptyBarrelBlockEntity barrel, StaffSupplyRequest req) {
        if ("CASHIER".equals(req.staffType)) {
            if (req.requiredRawIngredients.isEmpty()) return false;
            for (Map.Entry<Item, Integer> entry : req.requiredRawIngredients.entrySet()) {
                int have = countItemInBarrel(barrel, entry.getKey());
                if (have < entry.getValue()) {
                    return false;
                }
            }
            return true;
        } else {
            LivingEntity animal = resolveAnimal(sLevel, req.animalUuid);
            int have = countValidAnimalFoodInBarrel(barrel, animal, req.targetItems);
            return have >= ZOOKEEPER_REQUIRED_FOOD;
        }
    }

    private static boolean consumeAndFulfill(ServerLevel sLevel, EmptyBarrelBlockEntity barrel, StaffSupplyRequest req) {
        Entity ent = sLevel.getEntity(req.staffUuid);
        if (ent == null || !ent.isAlive()) return false;

        if ("CASHIER".equals(req.staffType) && ent instanceof ZooCashierEntity cashier) {
            if (!canBarrelFulfill(sLevel, barrel, req)) return false;

            // Consume raw ingredients from barrel
            for (Map.Entry<Item, Integer> entry : req.requiredRawIngredients.entrySet()) {
                removeItemsFromBarrel(barrel, entry.getKey(), entry.getValue());
            }

            // Add crafted menu items to cashier stock inventory
            SimpleContainer stock = cashier.getStockInventory();
            for (ItemStack menuStack : req.targetItems) {
                ItemStack toInsert = menuStack.copy();
                for (int i = 0; i < stock.getContainerSize() && !toInsert.isEmpty(); i++) {
                    ItemStack slotStack = stock.getItem(i);
                    if (slotStack.isEmpty()) {
                        stock.setItem(i, toInsert.copy());
                        toInsert.setCount(0);
                    } else if (ItemStack.isSameItemSameComponents(slotStack, toInsert)) {
                        int space = slotStack.getMaxStackSize() - slotStack.getCount();
                        if (space > 0) {
                            int add = Math.min(space, toInsert.getCount());
                            slotStack.grow(add);
                            toInsert.shrink(add);
                        }
                    }
                }
            }
            cashier.updateStockStatus();
            sLevel.playSound(null, barrel.getBlockPos(), SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.8F, 1.1F);
            sLevel.playSound(null, cashier.blockPosition(), SoundEvents.VILLAGER_WORK_BUTCHER, SoundSource.NEUTRAL, 0.8F, 1.1F);
            sLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, cashier.getX(), cashier.getY() + 1.2D, cashier.getZ(), 6, 0.25, 0.25, 0.25, 0.02);
            return true;
        }

        if ("ZOOKEEPER".equals(req.staffType) && ent instanceof ZooZookeeperEntity keeper) {
            LivingEntity animal = resolveAnimal(sLevel, req.animalUuid);
            if (!canBarrelFulfill(sLevel, barrel, req)) return false;

            int remainingToTake = ZOOKEEPER_REQUIRED_FOOD;
            SimpleContainer keeperInv = keeper.getFoodInventory();

            for (int i = 0; i < barrel.getContainerSize() && remainingToTake > 0; i++) {
                ItemStack st = barrel.getItem(i);
                if (!st.isEmpty() && isFoodMatchingForRequest(animal, st, req.targetItems)) {
                    int take = Math.min(remainingToTake, st.getCount());
                    ItemStack split = st.split(take);
                    ItemStack leftover = keeperInv.addItem(split);
                    int actualAdded = take - leftover.getCount();
                    if (!leftover.isEmpty()) {
                        st.grow(leftover.getCount());
                    }
                    if (st.isEmpty()) {
                        barrel.setItem(i, ItemStack.EMPTY);
                    }
                    remainingToTake -= actualAdded;
                    if (!leftover.isEmpty()) break;
                }
            }
            keeper.updateFoodStockFromInventory();
            sLevel.playSound(null, barrel.getBlockPos(), SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.8F, 1.1F);
            sLevel.playSound(null, keeper.blockPosition(), SoundEvents.VILLAGER_YES, SoundSource.NEUTRAL, 0.8F, 1.1F);
            sLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, keeper.getX(), keeper.getY() + 1.2D, keeper.getZ(), 6, 0.25, 0.25, 0.25, 0.02);
            return true;
        }

        return false;
    }

    private static int countItemInBarrel(EmptyBarrelBlockEntity barrel, Item item) {
        int total = 0;
        for (int i = 0; i < barrel.getContainerSize(); i++) {
            ItemStack st = barrel.getItem(i);
            if (!st.isEmpty() && st.is(item)) {
                total += st.getCount();
            }
        }
        return total;
    }

    private static void removeItemsFromBarrel(EmptyBarrelBlockEntity barrel, Item item, int amount) {
        int remaining = amount;
        for (int i = 0; i < barrel.getContainerSize() && remaining > 0; i++) {
            ItemStack st = barrel.getItem(i);
            if (!st.isEmpty() && st.is(item)) {
                int take = Math.min(remaining, st.getCount());
                st.shrink(take);
                remaining -= take;
                if (st.isEmpty()) {
                    barrel.setItem(i, ItemStack.EMPTY);
                }
            }
        }
    }

    private static int countValidAnimalFoodInBarrel(EmptyBarrelBlockEntity barrel, LivingEntity animal, List<ItemStack> fallbackTargets) {
        int total = 0;
        for (int i = 0; i < barrel.getContainerSize(); i++) {
            ItemStack st = barrel.getItem(i);
            if (!st.isEmpty() && isFoodMatchingForRequest(animal, st, fallbackTargets)) {
                total += st.getCount();
            }
        }
        return total;
    }

    private static boolean isFoodMatchingForRequest(LivingEntity animal, ItemStack stack, List<ItemStack> fallbackTargets) {
        if (stack == null || stack.isEmpty()) return false;
        if (animal != null && AnimalDietRegistry.isFoodValid(animal, stack)) {
            return true;
        }
        if (fallbackTargets != null) {
            for (ItemStack target : fallbackTargets) {
                if (!target.isEmpty() && stack.is(target.getItem())) {
                    return true;
                }
            }
        }
        return false;
    }

    private static LivingEntity resolveAnimal(ServerLevel sLevel, UUID animalUuid) {
        if (sLevel == null || animalUuid == null) return null;
        Entity ent = sLevel.getEntity(animalUuid);
        return (ent instanceof LivingEntity living && living.isAlive()) ? living : null;
    }

    public static void giveRecipeNoteToPlayer(ServerLevel sLevel, ServerPlayer player, BlockPos barrelPos, UUID staffUuid) {
        if (sLevel.getBlockEntity(barrelPos) instanceof EmptyBarrelBlockEntity barrel) {
            refreshStaffRequests(sLevel, barrel, barrel.getCategory());
        }

        StaffSupplyRequest req = ACTIVE_REQUESTS.get(staffUuid);
        if (req == null) return;

        CompoundTag tag = new CompoundTag();
        tag.putString("StaffRequestUuid", req.staffUuid.toString());
        tag.putString("BuilderName", req.staffName);

        ListTag materialsList = new ListTag();

        if ("CASHIER".equals(req.staffType)) {
            StringBuilder menuTitle = new StringBuilder();
            for (int i = 0; i < req.targetItems.size(); i++) {
                ItemStack st = req.targetItems.get(i);
                if (i > 0) menuTitle.append(", ");
                menuTitle.append(st.getCount()).append("x ").append(st.getHoverName().getString());
            }
            tag.putString("SchematicName", menuTitle.toString());

            for (Map.Entry<Item, Integer> entry : req.requiredRawIngredients.entrySet()) {
                ResourceLocation key = BuiltInRegistries.ITEM.getKey(entry.getKey());
                if (key != null) {
                    CompoundTag itemTag = new CompoundTag();
                    itemTag.putString("id", key.toString());
                    itemTag.putInt("needed", entry.getValue());
                    itemTag.putBoolean("checked", false);
                    materialsList.add(itemTag);
                }
            }
        } else {
            String title = "Pakan " + (req.animalName != null ? req.animalName : "Satwa") + " (Min. 32)";
            tag.putString("SchematicName", title);

            for (ItemStack foodStack : req.targetItems) {
                ResourceLocation key = BuiltInRegistries.ITEM.getKey(foodStack.getItem());
                if (key != null) {
                    CompoundTag itemTag = new CompoundTag();
                    itemTag.putString("id", key.toString());
                    itemTag.putInt("needed", ZOOKEEPER_REQUIRED_FOOD);
                    itemTag.putBoolean("checked", false);
                    materialsList.add(itemTag);
                }
            }
        }

        tag.put("Materials", materialsList);

        // Update existing note for the same staff member if player already has one
        ItemStack existingNote = ItemStack.EMPTY;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack st = player.getInventory().getItem(i);
            if (st.is(ZooItems.BUILDER_NOTE)) {
                CustomData cd = st.get(DataComponents.CUSTOM_DATA);
                if (cd != null && cd.copyTag().getString("StaffRequestUuid").equals(req.staffUuid.toString())) {
                    existingNote = st;
                    break;
                }
            }
        }

        Component customItemName = "CASHIER".equals(req.staffType)
                ? Component.translatable("zootycoon.warehouse.note.recipe_name", req.staffName)
                : Component.translatable("zootycoon.warehouse.note.food_name", req.animalName != null ? req.animalName : req.staffName);

        if (!existingNote.isEmpty()) {
            existingNote.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            existingNote.set(DataComponents.CUSTOM_NAME, customItemName);
        } else {
            ItemStack noteStack = new ItemStack(ZooItems.BUILDER_NOTE);
            noteStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            noteStack.set(DataComponents.CUSTOM_NAME, customItemName);
            if (!player.getInventory().add(noteStack)) {
                player.drop(noteStack, false);
            }
        }

        player.getInventory().setChanged();
        sLevel.playSound(null, player.blockPosition(), SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 0.9F, 1.1F);
        ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.info", "zootycoon.warehouse.note_taken", 1);
    }
}


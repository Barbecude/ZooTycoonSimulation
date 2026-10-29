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
import java.util.Locale;
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

        // 2. 30-second background auto-delivery if raw materials are complete in the barrel
        long now = sLevel.getGameTime();
        boolean anyFulfilled = false;

        List<StaffSupplyRequest> matching = getMatchingRequestsForBarrel(sLevel, barrel, category);
        for (StaffSupplyRequest req : matching) {
            if (canBarrelFulfill(sLevel, barrel, req)) {
                if (req.readySinceGameTime < 0L) {
                    req.readySinceGameTime = now;
                } else if (now - req.readySinceGameTime >= 600L) { // 30 seconds = 600 ticks at 20 TPS
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
            boolean allReady = canBarrelFulfill(sLevel, barrel, req);
            if (allReady && req.readySinceGameTime < 0L) {
                req.readySinceGameTime = sLevel.getGameTime();
            }

            List<ItemStack> targets = new ArrayList<>();
            for (ItemStack st : req.targetItems) {
                targets.add(st.copy());
            }

            List<WarehousePayloads.MenuItemRequestDto> menuDtos = new ArrayList<>();
            int currentCount = 0;
            int requiredCount = 0;

            if ("CASHIER".equals(req.staffType)) {
                // Virtual pool of barrel item stacks to allocate ingredients sequentially across menu items
                // Prevents multiple menu items from falsely double-counting the same ingredient
                List<ItemStack> virtualPool = new ArrayList<>();
                for (int i = 0; i < barrel.getContainerSize(); i++) {
                    ItemStack st = barrel.getItem(i);
                    if (!st.isEmpty()) {
                        virtualPool.add(st.copy());
                    }
                }

                for (ItemStack targetSt : req.targetItems) {
                    Map<Item, Integer> singleRaws = WarehouseRecipeResolver.resolveRawIngredientsForSingleItem(sLevel, req.category, targetSt.getItem());
                    List<WarehousePayloads.RawIngredientItemDto> rawDtos = new ArrayList<>();
                    boolean itemReady = true;

                    for (Map.Entry<Item, Integer> entry : singleRaws.entrySet()) {
                        int needed = entry.getValue() * targetSt.getCount();
                        int have = countItemInVirtualPool(virtualPool, entry.getKey());
                        boolean inBarrel = (have >= needed);
                        if (!inBarrel) {
                            itemReady = false;
                        }
                        rawDtos.add(new WarehousePayloads.RawIngredientItemDto(new ItemStack(entry.getKey(), needed), inBarrel));
                    }
                    if (singleRaws.isEmpty()) {
                        itemReady = false;
                    }

                    // If this menu item is fully satisfied, deduct its ingredients from the virtual pool
                    // so the next menu item cannot double-count them
                    if (itemReady) {
                        for (Map.Entry<Item, Integer> entry : singleRaws.entrySet()) {
                            int toConsume = entry.getValue() * targetSt.getCount();
                            consumeFromVirtualPool(virtualPool, entry.getKey(), toConsume);
                        }
                    }

                    menuDtos.add(new WarehousePayloads.MenuItemRequestDto(targetSt.copy(), rawDtos, itemReady));
                }

                for (Map.Entry<Item, Integer> e : req.requiredRawIngredients.entrySet()) {
                    int need = e.getValue();
                    int have = countItemInBarrel(barrel, e.getKey());
                    requiredCount += need;
                    currentCount += Math.min(have, need);
                }
            } else {
                requiredCount = ZOOKEEPER_REQUIRED_FOOD;
                LivingEntity animal = resolveAnimal(sLevel, req.animalUuid);
                currentCount = Math.min(requiredCount, countValidAnimalFoodInBarrel(barrel, animal, req.targetItems));
            }

            dtos.add(new WarehousePayloads.StaffRequestDto(
                    req.staffUuid,
                    req.staffEntityId,
                    req.staffType,
                    req.staffName,
                    req.subtitle,
                    targets,
                    menuDtos,
                    currentCount,
                    requiredCount,
                    allReady
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
            } else if (entity instanceof ZooZookeeperEntity keeper) {
                if (EmptyBarrelBlockEntity.CAT_ANIMAL_FOOD.equalsIgnoreCase(category)) {
                    if (keeper.hasActiveAssignedAnimal() && keeper.getFoodStock() <= ZOOKEEPER_LOW_STOCK_THRESHOLD) {
                        UUID animalId = keeper.getAssignedAnimalUuid();
                        LivingEntity animal = resolveAnimal(sLevel, animalId);
                        if (animal != null) {
                            List<ItemStack> expectedFoods = AnimalDietRegistry.getFoodsForAnimal(animal);
                            StaffSupplyRequest existing = ACTIVE_REQUESTS.get(keeper.getUUID());
                            if (existing == null || !animalId.equals(existing.animalUuid) || existing.targetItems.size() != expectedFoods.size()) {
                                StaffSupplyRequest created = createZookeeperRequest(keeper, animal);
                                ACTIVE_REQUESTS.put(keeper.getUUID(), created);
                            }
                        }
                    }
                } else if (EmptyBarrelBlockEntity.CAT_ANIMAL_CARE.equalsIgnoreCase(category)) {
                    if (keeper.hasActiveAssignedAnimal()) {
                        UUID animalId = keeper.getAssignedAnimalUuid();
                        LivingEntity animal = resolveAnimal(sLevel, animalId);
                        if (animal != null) {
                            StaffSupplyRequest careReq = evaluateZookeeperCareRequest(sLevel, keeper, animal);
                            if (careReq != null) {
                                ACTIVE_REQUESTS.put(keeper.getUUID(), careReq);
                            } else {
                                StaffSupplyRequest existing = ACTIVE_REQUESTS.get(keeper.getUUID());
                                if (existing != null && EmptyBarrelBlockEntity.CAT_ANIMAL_CARE.equalsIgnoreCase(existing.category)) {
                                    ACTIVE_REQUESTS.remove(keeper.getUUID());
                                }
                            }
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

        List<ItemStack> allFoods = AnimalDietRegistry.getFoodsForAnimal(animal);
        if (allFoods != null && !allFoods.isEmpty()) {
            for (ItemStack foodSt : allFoods) {
                if (foodSt != null && !foodSt.isEmpty()) {
                    req.targetItems.add(new ItemStack(foodSt.getItem(), ZOOKEEPER_REQUIRED_FOOD));
                }
            }
        }
        if (req.targetItems.isEmpty()) {
            req.targetItems.add(new ItemStack(Items.WHEAT, ZOOKEEPER_REQUIRED_FOOD));
        }
        return req;
    }

    private static StaffSupplyRequest evaluateZookeeperCareRequest(ServerLevel sLevel, ZooZookeeperEntity keeper, LivingEntity animal) {
        String keeperName = keeper.getName().getString();
        String animalName = animal.hasCustomName()
                ? animal.getCustomName().getString()
                : animal.getType().getDescription().getString();

        var habProfile = net.zootycoon.animal.habitat.HabitatProfileLoader.getProfile(animal);
        float effectiveTemp = net.zootycoon.animal.ZooClimate.getEffectiveTemperature(sLevel, animal.blockPosition());
        boolean tooCold = habProfile != null && effectiveTemp < habProfile.minTemperature();
        boolean tooHot = habProfile != null && effectiveTemp > habProfile.maxTemperature();

        if (tooCold && !keeper.hasCoal()) {
            StaffSupplyRequest req = new StaffSupplyRequest(
                    keeper.getUUID(),
                    keeper.getId(),
                    EmptyBarrelBlockEntity.CAT_ANIMAL_CARE,
                    "ZOOKEEPER",
                    keeperName,
                    "Kebutuhan: Pemanas (" + animalName + " Kedinginan)"
            );
            req.animalUuid = animal.getUUID();
            req.animalName = animalName;
            req.targetItems.add(new ItemStack(Items.COAL, 16));
            req.requiredRawIngredients.put(Items.COAL, 16);
            return req;
        }

        if (tooHot && !keeper.hasIce()) {
            StaffSupplyRequest req = new StaffSupplyRequest(
                    keeper.getUUID(),
                    keeper.getId(),
                    EmptyBarrelBlockEntity.CAT_ANIMAL_CARE,
                    "ZOOKEEPER",
                    keeperName,
                    "Kebutuhan: Pendingin (" + animalName + " Kepanasan)"
            );
            req.animalUuid = animal.getUUID();
            req.animalName = animalName;
            req.targetItems.add(new ItemStack(Items.ICE, 16));
            req.requiredRawIngredients.put(Items.ICE, 16);
            return req;
        }

        int clean = net.zootycoon.animal.ZooAnimalHungerManager.getCleanliness(animal);
        if (clean <= 60 && !keeper.hasBathingTools()) {
            StaffSupplyRequest req = new StaffSupplyRequest(
                    keeper.getUUID(),
                    keeper.getId(),
                    EmptyBarrelBlockEntity.CAT_ANIMAL_CARE,
                    "ZOOKEEPER",
                    keeperName,
                    "Kebutuhan: Alat Mandi (" + animalName + " Kotor)"
            );
            req.animalUuid = animal.getUUID();
            req.animalName = animalName;
            req.targetItems.add(new ItemStack(ZooItems.COPPER_WATERING_CAN, 1));
            req.targetItems.add(new ItemStack(Items.BRUSH, 1));
            req.requiredRawIngredients.put(ZooItems.COPPER_WATERING_CAN, 1);
            req.requiredRawIngredients.put(Items.BRUSH, 1);
            return req;
        }

        return null;
    }

    public static boolean canBarrelFulfill(ServerLevel sLevel, EmptyBarrelBlockEntity barrel, StaffSupplyRequest req) {
        if ("CASHIER".equals(req.staffType)) {
            if (req.requiredRawIngredients.isEmpty()) return false;
            List<ItemStack> pool = new ArrayList<>();
            for (int i = 0; i < barrel.getContainerSize(); i++) {
                ItemStack st = barrel.getItem(i);
                if (!st.isEmpty()) {
                    pool.add(st.copy());
                }
            }
            for (Map.Entry<Item, Integer> entry : req.requiredRawIngredients.entrySet()) {
                int have = countItemInVirtualPool(pool, entry.getKey());
                if (have < entry.getValue()) {
                    return false;
                }
                consumeFromVirtualPool(pool, entry.getKey(), entry.getValue());
            }
            return true;
        } else if (EmptyBarrelBlockEntity.CAT_ANIMAL_CARE.equalsIgnoreCase(req.category)) {
            if (req.requiredRawIngredients.containsKey(Items.COAL)) {
                int count = 0;
                for (int i = 0; i < barrel.getContainerSize(); i++) {
                    ItemStack st = barrel.getItem(i);
                    if (!st.isEmpty() && net.zootycoon.block.ZooHeaterBlock.getFuelTicks(st) > 0) {
                        count += st.getCount();
                    }
                }
                return count >= 16;
            } else if (req.requiredRawIngredients.containsKey(Items.ICE)) {
                int count = 0;
                for (int i = 0; i < barrel.getContainerSize(); i++) {
                    ItemStack st = barrel.getItem(i);
                    if (!st.isEmpty() && net.zootycoon.block.ZooCoolerBlock.getCoolingTicks(st) > 0) {
                        count += st.getCount();
                    }
                }
                return count >= 16;
            } else {
                boolean hasCan = false;
                boolean hasBrush = false;
                for (int i = 0; i < barrel.getContainerSize(); i++) {
                    ItemStack st = barrel.getItem(i);
                    if (!st.isEmpty()) {
                        if (net.zootycoon.animal.AnimalBathingManager.isWateringCan(st)) hasCan = true;
                        if (st.is(Items.BRUSH)) hasBrush = true;
                    }
                }
                return hasCan && hasBrush;
            }
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
            if (EmptyBarrelBlockEntity.CAT_ANIMAL_CARE.equalsIgnoreCase(req.category)) {
                if (!canBarrelFulfill(sLevel, barrel, req)) return false;

                if (req.requiredRawIngredients.containsKey(Items.COAL)) {
                    int remaining = 16;
                    for (int i = 0; i < barrel.getContainerSize() && remaining > 0; i++) {
                        ItemStack st = barrel.getItem(i);
                        if (!st.isEmpty() && net.zootycoon.block.ZooHeaterBlock.getFuelTicks(st) > 0) {
                            int take = Math.min(remaining, st.getCount());
                            ItemStack split = st.split(take);
                            ItemStack leftover = keeper.getFuelInventory().addItem(split);
                            int added = take - leftover.getCount();
                            if (!leftover.isEmpty()) st.grow(leftover.getCount());
                            if (st.isEmpty()) barrel.setItem(i, ItemStack.EMPTY);
                            remaining -= added;
                            if (!leftover.isEmpty()) break;
                        }
                    }
                    sLevel.playSound(null, barrel.getBlockPos(), SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.8F, 1.1F);
                    sLevel.playSound(null, keeper.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.NEUTRAL, 0.8F, 1.1F);
                    sLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, keeper.getX(), keeper.getY() + 1.2D, keeper.getZ(), 6, 0.25, 0.25, 0.25, 0.02);
                    return true;
                } else if (req.requiredRawIngredients.containsKey(Items.ICE)) {
                    int remaining = 16;
                    for (int i = 0; i < barrel.getContainerSize() && remaining > 0; i++) {
                        ItemStack st = barrel.getItem(i);
                        if (!st.isEmpty() && net.zootycoon.block.ZooCoolerBlock.getCoolingTicks(st) > 0) {
                            int take = Math.min(remaining, st.getCount());
                            ItemStack split = st.split(take);
                            ItemStack leftover = keeper.getFuelInventory().addItem(split);
                            int added = take - leftover.getCount();
                            if (!leftover.isEmpty()) st.grow(leftover.getCount());
                            if (st.isEmpty()) barrel.setItem(i, ItemStack.EMPTY);
                            remaining -= added;
                            if (!leftover.isEmpty()) break;
                        }
                    }
                    sLevel.playSound(null, barrel.getBlockPos(), SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.8F, 1.1F);
                    sLevel.playSound(null, keeper.blockPosition(), SoundEvents.GLASS_PLACE, SoundSource.NEUTRAL, 0.8F, 1.1F);
                    sLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, keeper.getX(), keeper.getY() + 1.2D, keeper.getZ(), 6, 0.25, 0.25, 0.25, 0.02);
                    return true;
                } else {
                    ItemStack takenCan = ItemStack.EMPTY;
                    ItemStack takenBrush = ItemStack.EMPTY;
                    for (int i = 0; i < barrel.getContainerSize(); i++) {
                        ItemStack st = barrel.getItem(i);
                        if (!st.isEmpty()) {
                            if (takenCan.isEmpty() && net.zootycoon.animal.AnimalBathingManager.isWateringCan(st)) {
                                takenCan = st.split(1);
                                if (st.isEmpty()) barrel.setItem(i, ItemStack.EMPTY);
                            } else if (takenBrush.isEmpty() && st.is(Items.BRUSH)) {
                                takenBrush = st.split(1);
                                if (st.isEmpty()) barrel.setItem(i, ItemStack.EMPTY);
                            }
                        }
                    }
                    if (!takenCan.isEmpty() && !takenBrush.isEmpty()) {
                        keeper.supplyBathingTools(takenCan, takenBrush);
                        sLevel.playSound(null, barrel.getBlockPos(), SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.8F, 1.1F);
                        sLevel.playSound(null, keeper.blockPosition(), SoundEvents.VILLAGER_YES, SoundSource.NEUTRAL, 0.8F, 1.1F);
                        sLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, keeper.getX(), keeper.getY() + 1.2D, keeper.getZ(), 8, 0.25, 0.25, 0.25, 0.02);
                        return true;
                    }
                    return false;
                }
            }
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

    public static boolean isRawIngredientMatching(ItemStack stack, Item targetItem) {
        if (stack == null || stack.isEmpty() || targetItem == null || targetItem == Items.AIR) return false;
        if (stack.is(targetItem)) return true;

        ResourceLocation stackId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        ResourceLocation targetId = BuiltInRegistries.ITEM.getKey(targetItem);
        if (stackId == null || targetId == null) return false;

        String stackPath = stackId.getPath().toLowerCase(Locale.ROOT);
        String targetPath = targetId.getPath().toLowerCase(Locale.ROOT);

        // 1. Direct path match across different mod namespaces (e.g. croptopia:tomato == farmersdelight:tomato)
        if (stackPath.equals(targetPath)) {
            return true;
        }

        // 2. Singular/plural and common crop aliases (e.g., onion/onions, tomato/tomatoes, cabbage/cabbages)
        if (isCropAliasMatch(stackPath, targetPath)) {
            return true;
        }

        // 3. Shared Fabric / Forge specific convention tags (e.g. c:crops/tomato, c:tomatoes, c:crops/onion)
        // Must specifically match the item/crop name, NEVER generic broad categories like c:foods, c:crops, c:meat, etc.
        String cleanTarget = normalizeCropName(targetPath);
        for (var stackTag : stack.getTags().toList()) {
            String ns = stackTag.location().getNamespace();
            if ("c".equals(ns) || "forge".equals(ns)) {
                String tagPath = stackTag.location().getPath().toLowerCase(Locale.ROOT);
                if (isBroadCategoryTag(tagPath)) {
                    continue;
                }
                String tagLeaf = tagPath.contains("/") ? tagPath.substring(tagPath.lastIndexOf('/') + 1) : tagPath;
                String cleanTagLeaf = normalizeCropName(tagLeaf);
                if (cleanTagLeaf.equals(cleanTarget)) {
                    if (BuiltInRegistries.ITEM.getOrCreateTag(stackTag).contains(BuiltInRegistries.ITEM.wrapAsHolder(targetItem))) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private static boolean isBroadCategoryTag(String tagPath) {
        if (tagPath == null) return true;
        String s = tagPath.toLowerCase(Locale.ROOT);
        return s.equals("foods") || s.equals("food")
                || s.equals("crops") || s.equals("crop")
                || s.equals("vegetables") || s.equals("vegetable")
                || s.equals("fruits") || s.equals("fruit")
                || s.equals("berries") || s.equals("berry")
                || s.equals("meat") || s.equals("meats")
                || s.equals("raw_meats") || s.equals("cooked_meats")
                || s.equals("seeds") || s.equals("seed")
                || s.equals("grain") || s.equals("grains")
                || s.equals("animal_foods") || s.equals("animal_food")
                || s.startsWith("diet_") || s.equals("items");
    }

    private static boolean isCropAliasMatch(String a, String b) {
        String cleanA = normalizeCropName(a);
        String cleanB = normalizeCropName(b);
        return cleanA.equals(cleanB);
    }

    private static String normalizeCropName(String name) {
        if (name == null) return "";
        String s = name.toLowerCase(Locale.ROOT);
        if (s.endsWith("_crop")) s = s.substring(0, s.length() - 5);
        if (s.endsWith("_leaf")) s = s.substring(0, s.length() - 5);
        if (s.endsWith("_leaves")) s = s.substring(0, s.length() - 7);
        if (s.endsWith("tomatoes")) return s.substring(0, s.length() - 8) + "tomato";
        if (s.endsWith("potatoes")) return s.substring(0, s.length() - 8) + "potato";
        if (s.endsWith("berries")) return s.substring(0, s.length() - 7) + "berry";
        if (s.endsWith("onions")) return s.substring(0, s.length() - 6) + "onion";
        if (s.endsWith("cabbages")) return s.substring(0, s.length() - 8) + "cabbage";
        if (s.endsWith("s") && !s.endsWith("ss") && s.length() > 3) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    private static int countItemInBarrel(EmptyBarrelBlockEntity barrel, Item item) {
        int total = 0;
        for (int i = 0; i < barrel.getContainerSize(); i++) {
            ItemStack st = barrel.getItem(i);
            if (!st.isEmpty() && isRawIngredientMatching(st, item)) {
                total += st.getCount();
            }
        }
        return total;
    }

    private static int countItemInVirtualPool(List<ItemStack> pool, Item item) {
        int total = 0;
        for (ItemStack st : pool) {
            if (!st.isEmpty() && isRawIngredientMatching(st, item)) {
                total += st.getCount();
            }
        }
        return total;
    }

    private static void consumeFromVirtualPool(List<ItemStack> pool, Item item, int amount) {
        int remaining = amount;
        for (ItemStack st : pool) {
            if (remaining <= 0) break;
            if (!st.isEmpty() && isRawIngredientMatching(st, item)) {
                int take = Math.min(remaining, st.getCount());
                st.shrink(take);
                remaining -= take;
            }
        }
    }

    private static void removeItemsFromBarrel(EmptyBarrelBlockEntity barrel, Item item, int amount) {
        int remaining = amount;
        for (int i = 0; i < barrel.getContainerSize() && remaining > 0; i++) {
            ItemStack st = barrel.getItem(i);
            if (!st.isEmpty() && isRawIngredientMatching(st, item)) {
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

    public static void fulfillStaffRequestManually(ServerLevel sLevel, ServerPlayer player, BlockPos barrelPos, UUID staffUuid) {
        if (sLevel == null || player == null || barrelPos == null || staffUuid == null) return;
        if (player.blockPosition().distSqr(barrelPos) > 64.0D) return;
        if (!(sLevel.getBlockEntity(barrelPos) instanceof EmptyBarrelBlockEntity barrel)) return;

        StaffSupplyRequest req = ACTIVE_REQUESTS.get(staffUuid);
        if (req != null && canBarrelFulfill(sLevel, barrel, req)) {
            if (consumeAndFulfill(sLevel, barrel, req)) {
                ACTIVE_REQUESTS.remove(req.staffUuid);
                barrel.syncToViewingPlayers(sLevel);
                sLevel.playSound(null, player.blockPosition(), SoundEvents.VILLAGER_YES, SoundSource.PLAYERS, 0.9F, 1.0F);
                ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.success", "zootycoon.warehouse.fulfilled", 1);
            }
        }
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

            // Distinct sections per menu item: Bahan [Menu 1] and Bahan [Menu 2]
            for (ItemStack menuSt : req.targetItems) {
                String sectionTitle = "Bahan " + menuSt.getHoverName().getString() + " (" + menuSt.getCount() + "x)";
                Map<Item, Integer> singleRaws = WarehouseRecipeResolver.resolveRawIngredientsForSingleItem(sLevel, req.category, menuSt.getItem());
                for (Map.Entry<Item, Integer> entry : singleRaws.entrySet()) {
                    ResourceLocation key = BuiltInRegistries.ITEM.getKey(entry.getKey());
                    if (key != null) {
                        CompoundTag itemTag = new CompoundTag();
                        itemTag.putString("id", key.toString());
                        itemTag.putInt("needed", entry.getValue() * menuSt.getCount());
                        itemTag.putBoolean("checked", false);
                        itemTag.putString("section", sectionTitle);
                        materialsList.add(itemTag);
                    }
                }
            }
        } else {
            String title = "Pakan " + (req.animalName != null ? req.animalName : "Satwa") + " (Min. 32)";
            tag.putString("SchematicName", title);

            String sectionTitle = "Pakan " + (req.animalName != null ? req.animalName : "Satwa");
            for (ItemStack foodStack : req.targetItems) {
                ResourceLocation key = BuiltInRegistries.ITEM.getKey(foodStack.getItem());
                if (key != null) {
                    CompoundTag itemTag = new CompoundTag();
                    itemTag.putString("id", key.toString());
                    itemTag.putInt("needed", ZOOKEEPER_REQUIRED_FOOD);
                    itemTag.putBoolean("checked", false);
                    itemTag.putString("section", sectionTitle);
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


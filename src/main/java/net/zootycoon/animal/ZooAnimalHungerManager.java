 package net.zootycoon.animal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.zootycoon.block.ZooFeedingTroughBlock;
import net.zootycoon.block.ZooFeedingTroughBlockEntity;
import net.zootycoon.claim.ZooClaim;
import net.zootycoon.claim.ZooClaimManager;
import net.zootycoon.compat.SereneSeasonsCompat;
import net.zootycoon.item.ZooItems;
import net.zootycoon.network.ZooNetworkPayloads;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ZooAnimalHungerManager {

    public static final int MAX_HUNGER = 100;
    public static final int HUNGRY_THRESHOLD = 50;
    public static final int CRITICAL_THRESHOLD = 15;
    private static final int DECAY_INTERVAL_TICKS = 240; // 240 ticks = 12 seconds per 1% drop -> 24,000 ticks (~20 minutes / 1 full Minecraft day) from 100% to 0%

    public static final int MAX_THIRST = 100;
    public static final int THIRSTY_THRESHOLD = 50;

    public static final int MAX_CLEANLINESS = 100;

    private static final Map<UUID, Integer> HUNGER_MAP = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> THIRST_MAP = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> CLEANLINESS_MAP = new ConcurrentHashMap<>();
    private static final Map<UUID, UUID> ZOOKEEPER_MAP = new ConcurrentHashMap<>();
    private static final Map<UUID, UUID> SPYGLASS_TARGET_MAP = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> SPYGLASS_TICKS_MAP = new ConcurrentHashMap<>();

    public static boolean isZooAnimalCandidate(LivingEntity e) {
        if (e == null || !e.isAlive()) return false;
        if (e instanceof Player) return false;
        if (e instanceof net.zootycoon.entity.ZooBuilderEntity) return false;
        if (e instanceof net.zootycoon.entity.ZooZookeeperEntity) return false;
        if (e instanceof net.zootycoon.entity.ZooSecurityEntity) return false;
        if (e instanceof net.zootycoon.entity.ZooHunterEntity) return false;
        if (e instanceof net.zootycoon.entity.ZooVisitorEntity) return false;
        if (e instanceof net.zootycoon.entity.ZooCashierEntity) return false;
        if (e.getTags().contains("zootycoon_staff")) return false;
        if (ZooClaimManager.isHostileMonster(e)) return false;

        // Exclude Bat (Kelelawar) from Zoo Animals
        if (e instanceof net.minecraft.world.entity.ambient.Bat) return false;
        String typeKey = BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).getPath();
        if (typeKey.equals("bat") || typeKey.endsWith("_bat") || typeKey.startsWith("bat_")) return false;

        return true;
    }

    public static boolean isScanningItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (stack.is(Items.SPYGLASS)) return true;
        if (stack.is(ZooItems.MAGNIFYING_GLASS)) return true;
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (id != null && "fieldguide".equals(id.getNamespace())) {
            return true;
        }
        return false;
    }

    public static UUID getOwnerUuid(Entity entity) {
        if (entity == null) return null;
        for (String tag : entity.getTags()) {
            if (tag.startsWith("zoo_owner_")) {
                try {
                    return UUID.fromString(tag.substring("zoo_owner_".length()));
                } catch (Exception ignored) {}
            }
        }
        return null;
    }

    public static boolean isOwnedBy(Entity entity, UUID playerUuid) {
        if (entity == null || playerUuid == null) return false;
        return entity.getTags().contains("zoo_owner_" + playerUuid);
    }

    public static void setOwner(Entity entity, UUID ownerUuid) {
        if (entity == null) return;
        entity.getTags().removeIf(t -> t.startsWith("zoo_owner_"));
        if (ownerUuid != null) {
            entity.addTag("zoo_owner_" + ownerUuid);
        }
    }

    public static boolean registerAnimalFromSpyglass(ServerPlayer player, LivingEntity animal) {
        return registerAnimalFromSpyglass(player, animal, true);
    }

    public static boolean registerAnimalFromSpyglass(ServerPlayer player, LivingEntity animal, boolean sendToast) {
        if (player == null || animal == null || !animal.isAlive()) return false;
        if (!isZooAnimalCandidate(animal)) return false;

        if (isTagged(animal) && isOwnedBy(animal, player.getUUID())) {
            return false;
        }

        if (isTagged(animal) && !isOwnedBy(animal, player.getUUID())) {
            UUID otherOwner = getOwnerUuid(animal);
            if (otherOwner != null) {
                ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.warning", "zootycoon.animal.owned_by_other", 2);
                return false;
            }
        }

        // Register animal
        setTagged(animal, true);
        setOwner(animal, player.getUUID());
        if (animal instanceof Mob mob) {
            mob.setPersistenceRequired();
        }
        setHunger(animal, MAX_HUNGER);
        setThirst(animal, MAX_THIRST);
        setCleanliness(animal, MAX_CLEANLINESS);

        ServerLevel level = (ServerLevel) player.level();
        level.playSound(null, animal.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 1.2f);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, animal.getX(), animal.getY() + animal.getBbHeight() * 0.5, animal.getZ(), 20, 0.4, 0.4, 0.4, 0.05);

        if (sendToast) {
            String animalName = animal.hasCustomName() ? animal.getCustomName().getString() : animal.getType().getDescription().getString();
            ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.animal", "zootycoon.animal.registered", 1, animalName);
        }

        ZooNetworkPayloads.sendAnimalSync(player);
        return true;
    }

    private static void tickVanillaSpyglassScoping(ServerLevel level) {
        for (ServerPlayer player : level.players()) {
            boolean activelyScanning = player.isScoping()
                    || isScanningItem(player.getUseItem())
                    || (player.isUsingItem() && (isScanningItem(player.getMainHandItem()) || isScanningItem(player.getOffhandItem())));

            if (activelyScanning) {
                Vec3 eyePos = player.getEyePosition();
                Vec3 lookVec = player.getViewVector(1.0f);
                Vec3 reachVec = eyePos.add(lookVec.scale(64.0));
                var box = player.getBoundingBox().expandTowards(lookVec.scale(64.0)).inflate(4.0);

                var hit = net.minecraft.world.entity.projectile.ProjectileUtil.getEntityHitResult(
                        player, eyePos, reachVec, box,
                        e -> e instanceof LivingEntity living && isZooAnimalCandidate(living),
                        64.0 * 64.0
                );

                if (hit != null && hit.getEntity() instanceof LivingEntity targetAnimal) {
                    UUID playerUuid = player.getUUID();
                    UUID currentTarget = SPYGLASS_TARGET_MAP.get(playerUuid);
                    if (targetAnimal.getUUID().equals(currentTarget)) {
                        int ticks = SPYGLASS_TICKS_MAP.getOrDefault(playerUuid, 0) + 1;
                        SPYGLASS_TICKS_MAP.put(playerUuid, ticks);
                        if (ticks >= 6) { // ~0.3 second of continuous scoping
                            if (!isTagged(targetAnimal) || !isOwnedBy(targetAnimal, playerUuid)) {
                                registerAnimalFromSpyglass(player, targetAnimal);
                            }
                            SPYGLASS_TICKS_MAP.put(playerUuid, -40); // 2-second cooldown to avoid spam
                        }
                    } else {
                        SPYGLASS_TARGET_MAP.put(playerUuid, targetAnimal.getUUID());
                        SPYGLASS_TICKS_MAP.put(playerUuid, 1);
                    }
                } else {
                    UUID pUuid = player.getUUID();
                    int currentTicks = SPYGLASS_TICKS_MAP.getOrDefault(pUuid, 0);
                    if (currentTicks > 0) {
                        SPYGLASS_TICKS_MAP.put(pUuid, currentTicks - 1);
                    } else if (currentTicks <= 0) {
                        SPYGLASS_TARGET_MAP.remove(pUuid);
                        SPYGLASS_TICKS_MAP.remove(pUuid);
                    }
                }
            } else {
                SPYGLASS_TARGET_MAP.remove(player.getUUID());
                SPYGLASS_TICKS_MAP.remove(player.getUUID());
            }
        }
    }

    public static boolean isAnimalOvercrowded(LivingEntity animal) {
        return net.zootycoon.economy.ZooEconomyEngine.isAnimalOvercrowded(animal);
    }

    public static void tickServer(ServerLevel level) {
        // Spyglass & Field Guide long-range scoping detection
        tickVanillaSpyglassScoping(level);

        if (level.getGameTime() % 20 != 0) return; // Hunger, thirst, and health checks every 1 second

        int thirstDecayInterval = 260; // Every 13 seconds, loses 1%
        try {
            if (SereneSeasonsCompat.isLoaded()) {
                String season = SereneSeasonsCompat.getCurrentSeason(level);
                if (season.contains("SUMMER")) {
                    thirstDecayInterval = 160; // Summer heat accelerates thirst
                }
            }
        } catch (Throwable ignored) {}

        // Iterate over all entities loaded in this level
        for (Entity entity : level.getAllEntities()) {
            if (!(entity instanceof LivingEntity animal)) continue;
            if (!animal.isAlive() || !isZooAnimalCandidate(animal)) continue;

            // ONLY registered animals are managed for zoo needs, feeding trough pathing, hunger, thirst and cleanliness!
            if (!isTagged(animal)) {
                continue;
            }

            // Universal thirst decay
            if (level.getGameTime() % thirstDecayInterval == 0) {
                int thirst = getThirst(animal);
                if (thirst > 0) {
                    setThirst(animal, thirst - 1);
                }
            }

            // Cleanliness decay
            if (level.getGameTime() % 240 == 0) { // Loses 1% every 12s
                int currentClean = getCleanliness(animal);
                if (currentClean > 0) {
                    setCleanliness(animal, currentClean - 1);
                }
            }

            int currentClean = getCleanliness(animal);
            int currentThirst = getThirst(animal);
            int currentHunger = getHunger(animal);

            // Universal Negative Minecraft Effects when stats drop to 0 or environment is uncomfortable
            applyNegativeStatEffects(level, animal, currentHunger, currentThirst, currentClean);

            // Overcrowding detection & subtle stress particles
            if (isAnimalOvercrowded(animal)) {
                if (level.getGameTime() % 40 == 0) {
                    level.sendParticles(ParticleTypes.SMOKE, animal.getX(), animal.getY() + animal.getBbHeight() + 0.2D, animal.getZ(), 2, 0.15, 0.1, 0.15, 0.01);
                }
                if (level.getGameTime() % net.zootycoon.economy.ZooEconomyEngine.OVERCROWDING_RATING_PENALTY_TICKS == 0) {
                    UUID owner = getOwnerUuid(animal);
                    if (owner != null && level.getServer() != null) {
                        ServerPlayer sp = level.getServer().getPlayerList().getPlayer(owner);
                        net.zootycoon.economy.ZooRatingManager.get(level.getServer()).deductRating(owner, net.zootycoon.economy.ZooEconomyEngine.OVERCROWDING_RATING_PENALTY, sp);
                    }
                }
            }

            // Animal pathing to feeding trough and water source (ONLY registered animals)
            if (animal instanceof net.minecraft.world.entity.PathfinderMob pathfinderMob) {
                net.zootycoon.animal.ai.AnimalDrinkWaterGoal.attach(pathfinderMob);
                net.zootycoon.animal.ai.AnimalEatDroppedFoodGoal.attach(pathfinderMob);
                net.zootycoon.animal.ai.AnimalEatFromTroughGoal.attach(pathfinderMob);
            }
            if (animal instanceof Mob mob) {
                handleUniversalAnimalNeeds(level, mob);
            }

            UUID ownerUuid = getOwnerUuid(animal);
            // Backward compatibility: If no owner tag but inside a claim, tag the claim's owner
            if (ownerUuid == null) {
                for (ZooClaim claim : ZooClaimManager.get(level).getAllClaims()) {
                    if (claim.contains(animal.blockPosition().getX(), animal.blockPosition().getZ())) {
                        ownerUuid = claim.getOwnerUuid();
                        setOwner(animal, ownerUuid);
                        break;
                    }
                }
            }

            ServerPlayer owner = ownerUuid != null ? level.getServer().getPlayerList().getPlayer(ownerUuid) : null;
            currentHunger = getHunger(animal);

            // 2. Faster hunger decay when dirty (<= 25 decays 2x faster)
            boolean isDirty = currentClean <= 25;
            int effectiveDecayInterval = isDirty ? (DECAY_INTERVAL_TICKS / 2) : DECAY_INTERVAL_TICKS;
            if (level.getGameTime() % effectiveDecayInterval == 0) {
                if (currentHunger > 0) {
                    currentHunger--;
                    setHunger(animal, currentHunger);
                }
            }

            // Natural health regeneration when at 100% hunger (like a player)
            if (currentHunger >= MAX_HUNGER && animal.getHealth() < animal.getMaxHealth()) {
                if (level.getGameTime() % 40 == 0) { // Recovers 1 HP every 2 seconds
                    animal.heal(1.0f);
                }
            }

            // Check critical starvation warning
            if (currentHunger <= CRITICAL_THRESHOLD && currentHunger > 0) {
                if (owner != null && level.getGameTime() % 600 == 0) {
                    String name = animal.hasCustomName() ? animal.getCustomName().getString() : animal.getType().getDescription().getString();
                    ZooNetworkPayloads.sendToast(owner, "zootycoon.toast.title.warning", "zootycoon.animal.starving_warning", 2, name, currentHunger);
                }
            }

            // Starvation damage & death
            if (currentHunger <= 0) {
                if (level.getGameTime() % 60 == 0) {
                    animal.hurt(animal.damageSources().starve(), 1.0f);
                }
                if (!animal.isAlive()) {
                    String name = animal.hasCustomName() ? animal.getCustomName().getString() : animal.getType().getDescription().getString();
                    if (owner != null) {
                        ZooNetworkPayloads.sendToast(owner, "zootycoon.toast.title.error", "zootycoon.animal.starved_death", 3, name);
                    }
                    level.playSound(null, animal.blockPosition(), SoundEvents.WITHER_DEATH, SoundSource.NEUTRAL, 0.6f, 0.8f);
                    level.sendParticles(ParticleTypes.SMOKE, animal.getX(), animal.getY() + 0.5, animal.getZ(), 15, 0.3, 0.3, 0.3, 0.05);
                }
            }
        }

        // Periodic sync to client so in-world hunger indicators update smoothly
        if (level.getGameTime() % 40 == 0) {
            for (ServerPlayer player : level.players()) {
                ZooNetworkPayloads.sendAnimalSync(player);
            }
        }
    }

    private static void applyNegativeStatEffects(ServerLevel level, LivingEntity animal, int currentHunger, int currentThirst, int currentClean) {
        // 1. Kebersihan (Cleanliness <= 0 -> Poison + Weakness)
        if (currentClean <= 0) {
            animal.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 0, false, true, true));
            animal.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 0, false, false, true));
            if (level.getGameTime() % 40 == 0) {
                level.sendParticles(ParticleTypes.ITEM_SLIME, animal.getX(), animal.getY() + animal.getBbHeight() * 0.5, animal.getZ(), 5, 0.2, 0.2, 0.2, 0.02);
            }
        } else if (currentClean <= 10) {
            animal.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 0, false, false, true));
            if (level.getGameTime() % 40 == 0) {
                level.sendParticles(ParticleTypes.ITEM_SLIME, animal.getX(), animal.getY() + animal.getBbHeight() * 0.5, animal.getZ(), 3, 0.2, 0.2, 0.2, 0.02);
            }
        }

        // 2. Haus (Thirst <= 0 -> Slowness II + Weakness + dry-out damage)
        if (currentThirst <= 0) {
            animal.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 1, false, true, true));
            animal.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 0, false, false, true));
            if (level.getGameTime() % 60 == 0 && animal.getHealth() > 1.0f) {
                animal.hurt(animal.damageSources().dryOut(), 1.0f);
            }
        } else if (currentThirst <= 15) {
            animal.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 0, false, false, true));
        }

        // 3. Lapar (Hunger <= 0 -> Hunger II + Weakness II + Slowness)
        if (currentHunger <= 0) {
            animal.addEffect(new MobEffectInstance(MobEffects.HUNGER, 80, 1, false, true, true));
            animal.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 1, false, true, true));
            animal.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 0, false, false, true));
        } else if (currentHunger <= 15) {
            animal.addEffect(new MobEffectInstance(MobEffects.HUNGER, 80, 0, false, false, true));
            animal.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 0, false, false, true));
        }

        // 4. Suhu / Iklim (Temperature discomfort -> Slowness/Freezing or Weakness/Heat)
        var habitatProfile = net.zootycoon.animal.habitat.HabitatProfileLoader.getProfile(animal);
        if (habitatProfile != null) {
            float effectiveTemp = net.zootycoon.animal.ZooClimate.getEffectiveTemperature(level, animal.blockPosition());
            if (effectiveTemp < habitatProfile.minTemperature()) {
                // Too Cold: Slowness + Freezing Ticks
                animal.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 1, false, true, true));
                animal.setTicksFrozen(Math.min(animal.getTicksRequiredToFreeze() + 60, animal.getTicksFrozen() + 30));
                if (effectiveTemp < habitatProfile.minTemperature() - 0.35f && level.getGameTime() % 80 == 0) {
                    animal.hurt(animal.damageSources().freeze(), 1.0f);
                }
            } else if (effectiveTemp > habitatProfile.maxTemperature()) {
                // Too Hot: Weakness + Hunger
                animal.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 0, false, true, true));
                animal.addEffect(new MobEffectInstance(MobEffects.HUNGER, 80, 0, false, false, true));
                if (effectiveTemp > habitatProfile.maxTemperature() + 0.35f && level.getGameTime() % 80 == 0) {
                    animal.hurt(animal.damageSources().onFire(), 1.0f);
                    level.sendParticles(ParticleTypes.FLAME, animal.getX(), animal.getY() + animal.getBbHeight() * 0.5, animal.getZ(), 3, 0.1, 0.1, 0.1, 0.01);
                }
            }
        }
    }

    public static void handleUniversalAnimalNeeds(ServerLevel level, Mob mob) {
        if (!isTagged(mob)) return; // Strictly only registered zoo animals!

        // 0. Immediate proximity food pickup: if a valid food item is near mob feet, consume it instantly
        List<ItemEntity> touching = level.getEntitiesOfClass(ItemEntity.class, mob.getBoundingBox().inflate(1.6D, 1.0D, 1.6D));
        for (ItemEntity ie : touching) {
            if (!ie.isAlive() || ie.getItem().isEmpty()) continue;
            if (AnimalDietRegistry.isFoodValid(mob, ie.getItem())) {
                ItemStack st = ie.getItem();
                if (feedAnimal(mob, st, null)) {
                    if (st.isEmpty()) {
                        ie.discard();
                    } else {
                        ie.setItem(st);
                    }
                    return;
                }
            }
        }

        int thirst = getThirst(mob);
        int hunger = getHunger(mob);

        // 1. Thirst pathing: if thirst <= 65, search for water trough first, then water block
        if (thirst <= 65) {
            BlockPos closestWaterTrough = null;
            ZooFeedingTroughBlockEntity targetWaterTrough = null;
            double closestTroughDist = Double.MAX_VALUE;

            for (BlockPos tp : ZooFeedingTroughBlock.KNOWN_TROUGHS) {
                if (tp.distSqr(mob.blockPosition()) <= 48.0 * 48.0) { // within 48 blocks
                    if (level.getBlockEntity(tp) instanceof ZooFeedingTroughBlockEntity trough && trough.hasWater()) {
                        double d = mob.distanceToSqr(tp.getX() + 0.5, tp.getY() + 0.5, tp.getZ() + 0.5);
                        if (d < closestTroughDist) {
                            closestTroughDist = d;
                            closestWaterTrough = tp;
                            targetWaterTrough = trough;
                        }
                    }
                }
            }

            if (closestWaterTrough != null && targetWaterTrough != null) {
                double maxDrinkDist = Math.max(2.6D, (mob.getBbWidth() * 0.5D) + 1.6D);
                double dx = (closestWaterTrough.getX() + 0.5) - mob.getX();
                double dz = (closestWaterTrough.getZ() + 0.5) - mob.getZ();
                double horizDistSq = dx * dx + dz * dz;
                double vertDist = Math.abs((closestWaterTrough.getY() + 0.5) - mob.getY());

                boolean isCloseEnough = (horizDistSq <= maxDrinkDist * maxDrinkDist && vertDist <= 3.0D)
                        || mob.blockPosition().closerThan(closestWaterTrough, maxDrinkDist);

                if (isCloseEnough) {
                    mob.getNavigation().stop();
                    mob.getLookControl().setLookAt(closestWaterTrough.getX() + 0.5, closestWaterTrough.getY() + 0.2, closestWaterTrough.getZ() + 0.5);

                    if (targetWaterTrough.hasWater()) {
                        setThirst(mob, MAX_THIRST);
                        level.playSound(null, mob.blockPosition(), SoundEvents.GENERIC_DRINK, SoundSource.NEUTRAL, 0.8f, 1.0f);
                        level.sendParticles(ParticleTypes.SPLASH, mob.getX(), mob.getY() + 0.3, mob.getZ(), 8, 0.25, 0.15, 0.25, 0.05);
                    }
                    return;
                } else {
                    mob.getNavigation().moveTo(closestWaterTrough.getX() + 0.5, closestWaterTrough.getY(), closestWaterTrough.getZ() + 0.5, 1.15);
                    mob.getLookControl().setLookAt(closestWaterTrough.getX() + 0.5, closestWaterTrough.getY() + 0.3, closestWaterTrough.getZ() + 0.5);
                    return;
                }
            }

            // Fallback: search for nearby water block (river, lake, etc.)
            BlockPos center = mob.blockPosition();
            BlockPos closestWater = null;
            double minWaterDist = Double.MAX_VALUE;

            for (BlockPos bp : BlockPos.betweenClosed(center.offset(-12, -2, -12), center.offset(12, 2, 12))) {
                if (level.getFluidState(bp).is(net.minecraft.tags.FluidTags.WATER)) {
                    double d = mob.distanceToSqr(bp.getX() + 0.5, bp.getY() + 0.5, bp.getZ() + 0.5);
                    if (d < minWaterDist) {
                        minWaterDist = d;
                        closestWater = bp.immutable();
                    }
                }
            }

            if (closestWater != null) {
                double maxDrinkDist = Math.max(2.6D, (mob.getBbWidth() * 0.5D) + 1.6D);
                double dx = (closestWater.getX() + 0.5) - mob.getX();
                double dz = (closestWater.getZ() + 0.5) - mob.getZ();
                double horizDistSq = dx * dx + dz * dz;
                double vertDist = Math.abs((closestWater.getY() + 0.5) - mob.getY());

                boolean isCloseEnough = (horizDistSq <= maxDrinkDist * maxDrinkDist && vertDist <= 3.0D)
                        || mob.blockPosition().closerThan(closestWater, maxDrinkDist);

                if (isCloseEnough) {
                    mob.getNavigation().stop();
                    mob.getLookControl().setLookAt(closestWater.getX() + 0.5, closestWater.getY() + 0.2, closestWater.getZ() + 0.5);
                    setThirst(mob, MAX_THIRST);
                    level.playSound(null, mob.blockPosition(), SoundEvents.GENERIC_DRINK, SoundSource.NEUTRAL, 0.8f, 1.0f);
                    level.sendParticles(ParticleTypes.SPLASH, mob.getX(), mob.getY() + 0.3, mob.getZ(), 8, 0.25, 0.15, 0.25, 0.05);
                    return;
                } else {
                    mob.getNavigation().moveTo(closestWater.getX() + 0.5, closestWater.getY(), closestWater.getZ() + 0.5, 1.05);
                    mob.getLookControl().setLookAt(closestWater.getX() + 0.5, closestWater.getY() + 0.3, closestWater.getZ() + 0.5);
                    return;
                }
            }
        }

        // 2. Hunger pathing: Feeding trough (when hunger <= 90) OR dropped food items in vicinity
        AnimalDietRegistry.FeedingType feedingType = AnimalDietRegistry.getFeedingType(mob);
        if (feedingType == AnimalDietRegistry.FeedingType.TROUGH && hunger <= 90) {
            BlockPos closestFoodTrough = null;
            ZooFeedingTroughBlockEntity targetFoodTrough = null;
            double closestFoodDist = Double.MAX_VALUE;

            for (BlockPos tp : ZooFeedingTroughBlock.KNOWN_TROUGHS) {
                if (tp.distSqr(mob.blockPosition()) <= 48.0 * 48.0) { // within 48 blocks
                    if (level.getBlockEntity(tp) instanceof ZooFeedingTroughBlockEntity trough && trough.hasFood()) {
                        if (trough.isAssigned() && !trough.isAssignedTo(mob)) {
                            continue;
                        }
                        if (AnimalDietRegistry.isFoodValid(mob, trough.getFoodStack())) {
                            double d = mob.distanceToSqr(tp.getX() + 0.5, tp.getY() + 0.5, tp.getZ() + 0.5);
                            if (d < closestFoodDist) {
                                closestFoodDist = d;
                                closestFoodTrough = tp;
                                targetFoodTrough = trough;
                            }
                        }
                    }
                }
            }

            if (closestFoodTrough != null && targetFoodTrough != null) {
                double maxEatDist = Math.max(2.6D, (mob.getBbWidth() * 0.5D) + 1.6D);
                double dx = (closestFoodTrough.getX() + 0.5) - mob.getX();
                double dz = (closestFoodTrough.getZ() + 0.5) - mob.getZ();
                double horizDistSq = dx * dx + dz * dz;
                double vertDist = Math.abs((closestFoodTrough.getY() + 0.5) - mob.getY());

                boolean isCloseEnough = (horizDistSq <= maxEatDist * maxEatDist && vertDist <= 3.0D)
                        || mob.blockPosition().closerThan(closestFoodTrough, maxEatDist);

                if (isCloseEnough) {
                    mob.getNavigation().stop();
                    mob.getLookControl().setLookAt(closestFoodTrough.getX() + 0.5, closestFoodTrough.getY() + 0.2, closestFoodTrough.getZ() + 0.5);

                                if (hunger < MAX_HUNGER && targetFoodTrough.hasFood()) {
                        ItemStack food = targetFoodTrough.extractFood(1);
                        if (!food.isEmpty()) {
                            feedAnimal(mob, food, null); // Emits HEART particles and restores hunger
                            targetFoodTrough.setChanged();
                        }
                    }
                } else {
                    mob.getNavigation().moveTo(closestFoodTrough.getX() + 0.5, closestFoodTrough.getY(), closestFoodTrough.getZ() + 0.5, 1.15);
                    mob.getLookControl().setLookAt(closestFoodTrough.getX() + 0.5, closestFoodTrough.getY() + 0.3, closestFoodTrough.getZ() + 0.5);
                }
            }
        }

        // 3. Search for nearby dropped food items (ItemEntity) on the ground
        ItemEntity closestFoodItem = null;
        double closestFoodDist = Double.MAX_VALUE;

        List<ItemEntity> nearbyItems = level.getEntitiesOfClass(ItemEntity.class, mob.getBoundingBox().inflate(16.0, 4.0, 16.0));
        for (ItemEntity ie : nearbyItems) {
            if (!ie.isAlive() || ie.getItem().isEmpty()) continue;
            if (AnimalDietRegistry.isFoodValid(mob, ie.getItem())) {
                double d = mob.distanceToSqr(ie);
                if (d < closestFoodDist) {
                    closestFoodDist = d;
                    closestFoodItem = ie;
                }
            }
        }

        if (closestFoodItem != null) {
            double maxEatDist = (mob.getBbWidth() * 0.5) + 1.25;
            if (closestFoodDist <= maxEatDist * maxEatDist) {
                mob.getNavigation().stop();
                mob.getLookControl().setLookAt(closestFoodItem.getX(), closestFoodItem.getY() + 0.1, closestFoodItem.getZ());

                ItemStack food = closestFoodItem.getItem();
                boolean fed = feedAnimal(mob, food, null);
                if (fed) {
                    if (food.isEmpty()) {
                        closestFoodItem.discard();
                    } else {
                        closestFoodItem.setItem(food);
                    }
                }
            } else {
                mob.getNavigation().moveTo(closestFoodItem.getX(), closestFoodItem.getY(), closestFoodItem.getZ(), 1.2);
                mob.getLookControl().setLookAt(closestFoodItem.getX(), closestFoodItem.getY() + 0.2, closestFoodItem.getZ());
            }
        }
    }

    public static boolean isTagged(Entity entity) {
        if (entity == null) return false;
        return entity.getTags().contains("zoo_registered");
    }

    public static void setTagged(Entity entity, boolean tagged) {
        if (entity == null) return;
        if (tagged) {
            entity.addTag("zoo_registered");
            if (entity instanceof net.minecraft.world.entity.PathfinderMob pMob) {
                net.zootycoon.animal.ai.AnimalDrinkWaterGoal.attach(pMob);
                net.zootycoon.animal.ai.AnimalEatDroppedFoodGoal.attach(pMob);
                net.zootycoon.animal.ai.AnimalEatFromTroughGoal.attach(pMob);
            }
            if (!HUNGER_MAP.containsKey(entity.getUUID())) {
                setHunger(entity, MAX_HUNGER);
            }
            if (!THIRST_MAP.containsKey(entity.getUUID())) {
                setThirst(entity, MAX_THIRST);
            }
            if (!CLEANLINESS_MAP.containsKey(entity.getUUID())) {
                setCleanliness(entity, MAX_CLEANLINESS);
            }
        } else {
            entity.getTags().removeIf(t -> t.contains("registered"));
        }
    }

    public static boolean unregisterAnimal(ServerPlayer player, int entityId) {
        if (player == null || player.level().getServer() == null) return false;

        for (ServerLevel sLevel : player.level().getServer().getAllLevels()) {
            Entity ent = sLevel.getEntity(entityId);
            if (ent instanceof LivingEntity animal) {
                UUID ownerUuid = getOwnerUuid(animal);
                boolean isOwner = isOwnedBy(animal, player.getUUID()) || player.getUUID().equals(ownerUuid);
                if (!isOwner && ownerUuid == null) {
                    for (ZooClaim claim : ZooClaimManager.get(sLevel).getAllClaims()) {
                        if (claim.getOwnerUuid().equals(player.getUUID()) && claim.contains(animal.blockPosition().getX(), animal.blockPosition().getZ())) {
                            isOwner = true;
                            break;
                        }
                    }
                }

                if (isOwner) {
                    setTagged(animal, false);
                    setOwner(animal, null);
                    setAssignedZookeeper(animal, null);
                    animal.getTags().removeIf(t -> t.contains("registered") || t.contains("owner_") || t.contains("hunger_") || t.contains("thirst_") || t.contains("cleanliness_") || t.contains("zoo_zk_"));
                    HUNGER_MAP.remove(animal.getUUID());
                    THIRST_MAP.remove(animal.getUUID());
                    CLEANLINESS_MAP.remove(animal.getUUID());
                    ZOOKEEPER_MAP.remove(animal.getUUID());
                    SPYGLASS_TARGET_MAP.remove(player.getUUID());
                    SPYGLASS_TICKS_MAP.remove(player.getUUID());

                    String name = animal.hasCustomName() ? animal.getCustomName().getString() : animal.getType().getDescription().getString();
                    ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.animal", "zootycoon.animal.unregistered", 2, name);
                    sLevel.playSound(null, animal.blockPosition(), SoundEvents.CHICKEN_EGG, SoundSource.PLAYERS, 1.0f, 0.8f);
                    ZooNetworkPayloads.sendAnimalSync(player);
                    return true;
                }
            }
        }
        return false;
    }

    public static int getHunger(Entity entity) {
        if (entity == null) return MAX_HUNGER;
        Integer cached = HUNGER_MAP.get(entity.getUUID());
        if (cached != null) return cached;

        for (String tag : entity.getTags()) {
            if (tag.startsWith("zoo_hunger_")) {
                try {
                    int val = Integer.parseInt(tag.substring("zoo_hunger_".length()));
                    HUNGER_MAP.put(entity.getUUID(), val);
                    return val;
                } catch (Exception ignored) {}
            }
        }
        return MAX_HUNGER;
    }

    public static void setHunger(Entity entity, int hunger) {
        if (entity == null) return;
        int clamped = Math.clamp(hunger, 0, MAX_HUNGER);
        HUNGER_MAP.put(entity.getUUID(), clamped);

        entity.getTags().removeIf(t -> t.startsWith("zoo_hunger_"));
        entity.addTag("zoo_hunger_" + clamped);
    }

    public static int getThirst(Entity entity) {
        if (entity == null) return MAX_THIRST;
        Integer cached = THIRST_MAP.get(entity.getUUID());
        if (cached != null) return cached;

        for (String tag : entity.getTags()) {
            if (tag.startsWith("zoo_thirst_")) {
                try {
                    int val = Integer.parseInt(tag.substring("zoo_thirst_".length()));
                    THIRST_MAP.put(entity.getUUID(), val);
                    return val;
                } catch (Exception ignored) {}
            }
        }
        return MAX_THIRST;
    }

    public static void setThirst(Entity entity, int thirst) {
        if (entity == null) return;
        int clamped = Math.clamp(thirst, 0, MAX_THIRST);
        THIRST_MAP.put(entity.getUUID(), clamped);

        entity.getTags().removeIf(t -> t.startsWith("zoo_thirst_"));
        entity.addTag("zoo_thirst_" + clamped);
    }

    public static int getCleanliness(Entity entity) {
        if (entity == null) return MAX_CLEANLINESS;
        Integer cached = CLEANLINESS_MAP.get(entity.getUUID());
        if (cached != null) return cached;

        for (String tag : entity.getTags()) {
            if (tag.startsWith("zoo_cleanliness_")) {
                try {
                    int val = Integer.parseInt(tag.substring("zoo_cleanliness_".length()));
                    CLEANLINESS_MAP.put(entity.getUUID(), val);
                    return val;
                } catch (Exception ignored) {}
            }
        }
        return MAX_CLEANLINESS;
    }

    public static void setCleanliness(Entity entity, int cleanliness) {
        if (entity == null) return;
        int clamped = Math.clamp(cleanliness, 0, MAX_CLEANLINESS);
        CLEANLINESS_MAP.put(entity.getUUID(), clamped);

        entity.getTags().removeIf(t -> t.startsWith("zoo_cleanliness_"));
        entity.addTag("zoo_cleanliness_" + clamped);
    }

    public static boolean cleanAnimal(LivingEntity animal, Player player) {
        if (animal == null || !animal.isAlive()) return false;
        setCleanliness(animal, MAX_CLEANLINESS);

        if (animal.level() instanceof ServerLevel sl) {
            sl.playSound(null, animal.blockPosition(), SoundEvents.BRUSH_GENERIC, SoundSource.PLAYERS, 1.0f, 1.0f);
            sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, animal.getX(), animal.getY() + animal.getBbHeight() * 0.5, animal.getZ(), 10, 0.3, 0.2, 0.3, 0.05);
            sl.sendParticles(ParticleTypes.WAX_OFF, animal.getX(), animal.getY() + animal.getBbHeight() * 0.5, animal.getZ(), 6, 0.25, 0.2, 0.25, 0.02);
        }

        if (player instanceof ServerPlayer sp) {
            String name = animal.hasCustomName() ? animal.getCustomName().getString() : animal.getType().getDescription().getString();
            ZooNetworkPayloads.sendToast(sp, "zootycoon.toast.title.success", "zootycoon.animal.cleaned_toast", 1, name);
        }
        return true;
    }

    public static UUID getAssignedZookeeper(Entity entity) {
        if (entity == null) return null;
        UUID cached = ZOOKEEPER_MAP.get(entity.getUUID());
        if (cached != null) return cached;

        for (String tag : entity.getTags()) {
            if (tag.startsWith("zoo_zk_")) {
                try {
                    UUID u = UUID.fromString(tag.substring("zoo_zk_".length()));
                    ZOOKEEPER_MAP.put(entity.getUUID(), u);
                    return u;
                } catch (Exception ignored) {}
            }
        }
        return null;
    }

    public static void setAssignedZookeeper(Entity entity, UUID zookeeperUuid) {
        if (entity == null) return;
        entity.getTags().removeIf(t -> t.startsWith("zoo_zk_"));
        if (zookeeperUuid == null) {
            ZOOKEEPER_MAP.remove(entity.getUUID());
        } else {
            ZOOKEEPER_MAP.put(entity.getUUID(), zookeeperUuid);
            entity.addTag("zoo_zk_" + zookeeperUuid);
        }
    }

    public static boolean feedAnimal(LivingEntity animal, ItemStack foodStack, Player feeder) {
        if (animal == null || foodStack.isEmpty()) return false;
        if (!AnimalDietRegistry.isFoodValid(animal, foodStack)) return false;

        int current = getHunger(animal);
        int restored = Math.min(MAX_HUNGER, current + 35);
        setHunger(animal, restored);
        if (animal.getHealth() < animal.getMaxHealth()) {
            animal.heal(2.0f);
        }
        if (feeder != null && !isTagged(animal)) {
            setTagged(animal, true);
            if (getOwnerUuid(animal) == null) {
                setOwner(animal, feeder.getUUID());
            }
        }

        ItemStack particleStack = foodStack.copyWithCount(1);
        if (feeder == null || !feeder.getAbilities().instabuild) {
            foodStack.shrink(1);
        }

        if (animal.level() instanceof ServerLevel sl) {
            sl.playSound(null, animal.blockPosition(), SoundEvents.GENERIC_EAT, SoundSource.NEUTRAL, 1.0f, 1.0f);
            // Always emit HEART (love) particles when fed by player, zookeeper, or feeder trough!
            sl.sendParticles(ParticleTypes.HEART,
                    animal.getX(), animal.getY() + animal.getBbHeight() + 0.25, animal.getZ(),
                    7, 0.35, 0.25, 0.35, 0.02);
            sl.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, particleStack),
                    animal.getX(), animal.getY() + animal.getBbHeight() * 0.5, animal.getZ(),
                    6, 0.2, 0.2, 0.2, 0.01);
        }

        if (animal instanceof Animal a && a.canFallInLove()) {
            a.setInLove(feeder);
        }

        return true;
    }

    public static void throwFoodAtAnimal(ServerLevel level, LivingEntity thrower, LivingEntity animal, ItemStack foodStack) {
        if (thrower == null || animal == null || foodStack.isEmpty()) return;

        ItemStack singleFood = foodStack.copyWithCount(1);
        foodStack.shrink(1);

        ItemEntity thrown = new ItemEntity(level,
                thrower.getX(),
                thrower.getEyeY() - 0.15D,
                thrower.getZ(),
                singleFood);

        Vec3 from = new Vec3(thrower.getX(), thrower.getEyeY() - 0.15D, thrower.getZ());
        Vec3 to = new Vec3(animal.getX(), animal.getY() + (animal.getBbHeight() * 0.5D), animal.getZ());
        Vec3 direction = to.subtract(from);
        if (direction.lengthSqr() < 1.0E-6D) {
            direction = new Vec3(0.0D, 0.1D, 0.0D);
        }
        Vec3 velocity = direction.normalize().scale(0.5D);
        thrown.setDeltaMovement(velocity.x, velocity.y + 0.18D, velocity.z);
        thrown.setPickUpDelay(20);
        level.addFreshEntity(thrown);
        level.playSound(null, thrower.blockPosition(), SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 0.7f, 1.0f);
    }
}

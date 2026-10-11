package net.zootycoon.animal;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.zootycoon.claim.ZooClaim;
import net.zootycoon.claim.ZooClaimManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ZooAnimalHungerManager {

    public static final int MAX_HUNGER = 100;
    public static final int HUNGRY_THRESHOLD = 50;
    public static final int CRITICAL_THRESHOLD = 15;
    private static final int DECAY_INTERVAL_TICKS = 240; // Every 12 seconds, loses 1%

    private static final Map<UUID, Integer> HUNGER_MAP = new ConcurrentHashMap<>();
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
        if (e.getTags().contains("zootycoon_staff")) return false;
        if (ZooClaimManager.isHostileMonster(e)) return false;

        // Exclude Bat (Kelelawar) from Zoo Animals
        if (e instanceof net.minecraft.world.entity.ambient.Bat) return false;
        String typeKey = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).getPath();
        if (typeKey.equals("bat") || typeKey.endsWith("_bat") || typeKey.startsWith("bat_")) return false;

        return true;
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
        if (player == null || animal == null || !animal.isAlive()) return false;
        if (!isZooAnimalCandidate(animal)) return false;

        if (isTagged(animal) && isOwnedBy(animal, player.getUUID())) {
            player.sendSystemMessage(
                    Component.literal("ℹ Satwa ini sudah terdaftar di kebun binatang Anda!").withStyle(ChatFormatting.YELLOW)
            );
            return false;
        }

        if (isTagged(animal) && !isOwnedBy(animal, player.getUUID())) {
            UUID otherOwner = getOwnerUuid(animal);
            if (otherOwner != null) {
                player.sendSystemMessage(
                        Component.literal("✖ Satwa ini sudah menjadi milik kebun binatang pemain lain!").withStyle(ChatFormatting.RED)
                );
                return false;
            }
        }

        // Register animal
        setTagged(animal, true);
        setOwner(animal, player.getUUID());
        if (animal instanceof net.minecraft.world.entity.Mob mob) {
            mob.setPersistenceRequired();
        }
        setHunger(animal, MAX_HUNGER);

        ServerLevel level = (ServerLevel) player.level();
        level.playSound(null, animal.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 1.2f);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, animal.getX(), animal.getY() + animal.getBbHeight() * 0.5, animal.getZ(), 20, 0.4, 0.4, 0.4, 0.05);

        String animalName = animal.hasCustomName() ? animal.getCustomName().getString() : animal.getType().getDescription().getString();
        player.sendSystemMessage(
                Component.literal("✔ Satwa [" + animalName + "] berhasil didaftarkan ke Kebun Binatang!").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)
        );
        player.displayClientMessage(
                Component.literal("✔ Satwa [" + animalName + "] Terdaftar di Zoo!").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD),
                true
        );

        net.zootycoon.network.ZooNetworkPayloads.sendAnimalSync(player);
        return true;
    }

    private static void tickVanillaSpyglassScoping(ServerLevel level) {
        for (ServerPlayer player : level.players()) {
            if (player.isScoping() || player.getUseItem().is(net.minecraft.world.item.Items.SPYGLASS)) {
                Vec3 eyePos = player.getEyePosition();
                Vec3 lookVec = player.getViewVector(1.0f);
                Vec3 reachVec = eyePos.add(lookVec.scale(64.0));
                var box = player.getBoundingBox().expandTowards(lookVec.scale(64.0)).inflate(4.0);

                var hit = net.minecraft.world.entity.projectile.ProjectileUtil.getEntityHitResult(
                        player, eyePos, reachVec, box,
                        e -> e instanceof LivingEntity living && isZooAnimalCandidate(living),
                        0.5
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

    public static void tickServer(ServerLevel level) {
        // Vanilla Spyglass Scoping Fallback (when Field Guide is NOT loaded)
        tickVanillaSpyglassScoping(level);

        if (level.getGameTime() % 20 != 0) return; // Hunger and health checks every 1 second

        // Iterate over all entities loaded in this level
        for (Entity entity : level.getAllEntities()) {
            if (!(entity instanceof LivingEntity animal)) continue;
            if (!animal.isAlive() || !isTagged(animal) || !isZooAnimalCandidate(animal)) continue;

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
            int currentHunger = getHunger(animal);

            // Decay hunger
            if (level.getGameTime() % DECAY_INTERVAL_TICKS == 0) {
                if (currentHunger > 0) {
                    currentHunger--;
                    setHunger(animal, currentHunger);
                }
            }

            // Emergency foraging for herbivores on grass
            if (currentHunger < 30 && AnimalDietRegistry.getFeedingType(animal) == AnimalDietRegistry.FeedingType.TROUGH) {
                var standingOn = level.getBlockState(animal.blockPosition().below());
                var inBlock = level.getBlockState(animal.blockPosition());
                if (standingOn.is(Blocks.GRASS_BLOCK) || inBlock.is(Blocks.SHORT_GRASS)) {
                    currentHunger = Math.min(MAX_HUNGER, currentHunger + 15);
                    setHunger(animal, currentHunger);
                    level.playSound(null, animal.blockPosition(), SoundEvents.GENERIC_EAT, SoundSource.NEUTRAL, 0.6f, 1.0f);
                    level.sendParticles(ParticleTypes.HAPPY_VILLAGER, animal.getX(), animal.getY() + 0.5, animal.getZ(), 4, 0.2, 0.2, 0.2, 0.01);
                }
            }

            // Check critical starvation warning
            if (currentHunger <= CRITICAL_THRESHOLD && currentHunger > 0) {
                if (owner != null && level.getGameTime() % 600 == 0) {
                    String name = animal.hasCustomName() ? animal.getCustomName().getString() : animal.getType().getDescription().getString();
                    owner.sendSystemMessage(
                            Component.literal("[Peringatan] Satwa [" + name + "] sekarat kelaparan (" + currentHunger + "%)! Segera beri makan!").withStyle(ChatFormatting.RED)
                    );
                }
            }

            // Starvation death
            if (currentHunger <= 0) {
                String name = animal.hasCustomName() ? animal.getCustomName().getString() : animal.getType().getDescription().getString();
                if (owner != null) {
                    owner.sendSystemMessage(
                            Component.literal("[Kematian] Satwa [" + name + "] mati karena kelaparan di kebun binatang Anda!").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD)
                    );
                }
                level.playSound(null, animal.blockPosition(), SoundEvents.WITHER_DEATH, SoundSource.NEUTRAL, 0.6f, 0.8f);
                level.sendParticles(ParticleTypes.SMOKE, animal.getX(), animal.getY() + 0.5, animal.getZ(), 15, 0.3, 0.3, 0.3, 0.05);
                animal.kill();
            }
        }

        // Periodic sync to client so in-world hunger indicators update smoothly
        if (level.getGameTime() % 40 == 0) {
            for (ServerPlayer player : level.players()) {
                net.zootycoon.network.ZooNetworkPayloads.sendAnimalSync(player);
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
            if (!HUNGER_MAP.containsKey(entity.getUUID())) {
                setHunger(entity, MAX_HUNGER);
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
                    animal.getTags().removeIf(t -> t.contains("registered") || t.contains("owner_") || t.contains("hunger_") || t.contains("zoo_zk_"));
                    HUNGER_MAP.remove(animal.getUUID());
                    ZOOKEEPER_MAP.remove(animal.getUUID());
                    SPYGLASS_TARGET_MAP.remove(player.getUUID());
                    SPYGLASS_TICKS_MAP.remove(player.getUUID());

                    String name = animal.hasCustomName() ? animal.getCustomName().getString() : animal.getType().getDescription().getString();
                    player.sendSystemMessage(
                            Component.literal("✔ Satwa [" + name + "] telah dilepaskan dari Kebun Binatang!").withStyle(ChatFormatting.YELLOW)
                    );
                    sLevel.playSound(null, animal.blockPosition(), SoundEvents.CHICKEN_EGG, SoundSource.PLAYERS, 1.0f, 0.8f);
                    net.zootycoon.network.ZooNetworkPayloads.sendAnimalSync(player);
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
        if (current >= MAX_HUNGER) return false;

        int restored = Math.min(MAX_HUNGER, current + 35);
        setHunger(animal, restored);
        setTagged(animal, true); // Feeding also ensures it's tagged

        ItemStack particleStack = foodStack.copy();
        foodStack.shrink(1);

        if (animal.level() instanceof ServerLevel sl) {
            sl.playSound(null, animal.blockPosition(), SoundEvents.GENERIC_EAT, SoundSource.NEUTRAL, 1.0f, 1.0f);
            sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, animal.getX(), animal.getY() + animal.getBbHeight() * 0.6, animal.getZ(), 8, 0.3, 0.3, 0.3, 0.02);
            sl.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, particleStack),
                    animal.getX(), animal.getY() + animal.getBbHeight() * 0.5, animal.getZ(), 6, 0.2, 0.2, 0.2, 0.01);
        }

        if (animal instanceof Animal a && restored >= 80 && a.canFallInLove()) {
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

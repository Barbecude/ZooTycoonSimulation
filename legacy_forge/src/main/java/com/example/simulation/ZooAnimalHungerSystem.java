package com.example.simulation;

import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class ZooAnimalHungerSystem {
    public static final int MAX_HUNGER = 20;
    public static final int HUNGRY_THRESHOLD = 8;
    public static final int BREED_READY_THRESHOLD = 14;
    private static final int FEED_RESTORE = 6;
    private static final int DECAY_INTERVAL = 200;

    private ZooAnimalHungerSystem() {
    }

    public static void tickServer(ServerLevel level) {
        if (level.getGameTime() % 20 != 0) return;

        ZooData data = ZooData.get(level);
        ListTag tagged = data.getTaggedAnimals();
        boolean changed = false;

        for (int i = 0; i < tagged.size(); i++) {
            CompoundTag tag = tagged.getCompound(i);
            Entity entity = resolveTaggedEntity(level, tag);
            if (!(entity instanceof LivingEntity living) || !living.isAlive()) continue;

            if (syncTagIdentity(tag, living)) changed = true;
            if (!tag.contains("hunger")) {
                tag.putInt("hunger", MAX_HUNGER);
                changed = true;
            }

            if (level.getGameTime() % DECAY_INTERVAL == 0) {
                int current = getTagHunger(tag);
                if (current > 0) {
                    tag.putInt("hunger", current - 1);
                    changed = true;
                }
            }

            if (consumeNearbyThrownFood(level, living, tag)) {
                changed = true;
            }
        }

        if (changed) {
            data.setDirty();
            syncClients(level, data);
        }
    }

    public static boolean isTaggedAnimal(ServerLevel level, Entity entity) {
        if (entity == null) return false;
        ZooData data = ZooData.get(level);
        return findTaggedEntry(data, entity) != null;
    }

    public static boolean isHungry(ServerLevel level, Entity entity) {
        if (entity == null) return false;
        ZooData data = ZooData.get(level);
        CompoundTag tag = findTaggedEntry(data, entity);
        if (tag == null) return false;
        return getTagHunger(tag) <= HUNGRY_THRESHOLD;
    }

    public static int getHunger(ServerLevel level, Entity entity) {
        if (entity == null) return -1;
        ZooData data = ZooData.get(level);
        CompoundTag tag = findTaggedEntry(data, entity);
        if (tag == null) return -1;
        return getTagHunger(tag);
    }

    public static String getTaggedAnimalType(ServerLevel level, Entity entity) {
        if (entity == null) return "";
        ZooData data = ZooData.get(level);
        CompoundTag tag = findTaggedEntry(data, entity);
        if (tag == null) return "";
        return tag.getString("type");
    }

    public static boolean canEatFood(ServerLevel level, Entity entity, Item item) {
        if (entity == null || item == null) return false;
        ZooData data = ZooData.get(level);
        CompoundTag tag = findTaggedEntry(data, entity);
        if (tag == null) return false;
        if (getTagHunger(tag) >= MAX_HUNGER) return false;

        String type = tag.getString("type");
        return !type.isEmpty() && FoodAnimalRegistry.isValidFoodForAnimal(type, item);
    }

    public static boolean feedTaggedAnimal(ServerLevel level, LivingEntity animal, Item food, @Nullable LivingEntity feeder) {
        if (animal == null || food == null) return false;

        ZooData data = ZooData.get(level);
        CompoundTag tag = findTaggedEntry(data, animal);
        if (tag == null) return false;
        syncTagIdentity(tag, animal);

        String type = tag.getString("type");
        if (type.isEmpty() || !FoodAnimalRegistry.isValidFoodForAnimal(type, food)) return false;

        int hunger = getTagHunger(tag);
        if (hunger >= MAX_HUNGER) return false;

        int updated = Math.min(MAX_HUNGER, hunger + FEED_RESTORE);
        tag.putInt("hunger", updated);
        data.setDirty();

        if (animal instanceof Animal baseAnimal && updated >= BREED_READY_THRESHOLD && baseAnimal.canFallInLove()) {
            Player player = feeder instanceof Player p ? p : null;
            baseAnimal.setInLove(player);
        }

        level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                animal.getX(), animal.getY() + (animal.getBbHeight() * 0.6D), animal.getZ(),
                8, 0.25D, 0.25D, 0.25D, 0.02D);
        level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(food)),
                animal.getX(), animal.getY() + (animal.getBbHeight() * 0.5D), animal.getZ(),
                6, 0.2D, 0.2D, 0.2D, 0.01D);
        level.playSound(null, animal.blockPosition(), SoundEvents.GENERIC_EAT, SoundSource.NEUTRAL, 0.7F, 1.0F);

        syncClients(level, data);
        return true;
    }

    public static void throwFoodAtTarget(ServerLevel level, LivingEntity thrower, LivingEntity target, Item food) {
        if (thrower == null || target == null || food == null) return;

        ItemEntity thrown = new ItemEntity(level,
                thrower.getX(),
                thrower.getEyeY() - 0.15D,
                thrower.getZ(),
                new ItemStack(food));

        Vec3 from = new Vec3(thrower.getX(), thrower.getEyeY() - 0.15D, thrower.getZ());
        Vec3 to = new Vec3(target.getX(), target.getY() + (target.getBbHeight() * 0.5D), target.getZ());
        Vec3 direction = to.subtract(from);
        if (direction.lengthSqr() < 1.0E-6D) {
            direction = new Vec3(0.0D, 0.1D, 0.0D);
        }
        Vec3 velocity = direction.normalize().scale(0.55D);
        thrown.setDeltaMovement(velocity.x, velocity.y + 0.15D, velocity.z);
        thrown.setPickUpDelay(20);
        level.addFreshEntity(thrown);
        thrower.swing(InteractionHand.MAIN_HAND);
    }

    @Nullable
    public static CompoundTag findTaggedEntry(ZooData data, Entity entity) {
        if (data == null || entity == null) return null;

        ListTag list = data.getTaggedAnimals();
        String uuid = entity.getUUID().toString();
        for (int i = 0; i < list.size(); i++) {
            CompoundTag tag = list.getCompound(i);
            if (tag.contains("uuid") && uuid.equals(tag.getString("uuid"))) {
                return tag;
            }
        }

        int id = entity.getId();
        for (int i = 0; i < list.size(); i++) {
            CompoundTag tag = list.getCompound(i);
            if (tag.getInt("id") == id) {
                return tag;
            }
        }

        return null;
    }

    @Nullable
    public static Entity resolveTaggedEntity(ServerLevel level, CompoundTag tag) {
        if (level == null || tag == null) return null;

        if (tag.contains("uuid")) {
            try {
                UUID uuid = UUID.fromString(tag.getString("uuid"));
                Entity byUuid = level.getEntity(uuid);
                if (byUuid != null) return byUuid;
            } catch (IllegalArgumentException ignored) {
            }
        }

        return level.getEntity(tag.getInt("id"));
    }

    private static int getTagHunger(CompoundTag tag) {
        int hunger = tag.contains("hunger") ? tag.getInt("hunger") : MAX_HUNGER;
        return Math.max(0, Math.min(MAX_HUNGER, hunger));
    }

    private static boolean syncTagIdentity(CompoundTag tag, Entity entity) {
        if (tag == null || entity == null) return false;
        boolean changed = false;

        if (tag.getInt("id") != entity.getId()) {
            tag.putInt("id", entity.getId());
            changed = true;
        }

        String uuid = entity.getUUID().toString();
        if (!uuid.equals(tag.getString("uuid"))) {
            tag.putString("uuid", uuid);
            changed = true;
        }

        return changed;
    }

    private static boolean consumeNearbyThrownFood(ServerLevel level, LivingEntity animal, CompoundTag tag) {
        String type = tag.getString("type");
        if (type.isEmpty()) return false;
        if (getTagHunger(tag) >= MAX_HUNGER) return false;

        for (ItemEntity itemEntity : level.getEntitiesOfClass(ItemEntity.class,
                animal.getBoundingBox().inflate(1.3D),
                item -> !item.getItem().isEmpty())) {
            ItemStack stack = itemEntity.getItem();
            Item item = stack.getItem();
            if (!FoodAnimalRegistry.isValidFoodForAnimal(type, item)) continue;

            if (!feedTaggedAnimal(level, animal, item, null)) return false;

            stack.shrink(1);
            if (stack.isEmpty()) {
                itemEntity.discard();
            } else {
                itemEntity.setItem(stack);
            }
            return true;
        }

        return false;
    }

    private static void syncClients(ServerLevel level, ZooData data) {
        SyncBalancePacket packet = new SyncBalancePacket(data.getBalance(), data.getTaggedAnimals(),
                data.getAnimalCount(), data.getStaffCount(), data.getVisitorCount(), data.getRating());
        PacketHandler.INSTANCE.send(net.minecraftforge.network.PacketDistributor.ALL.noArg(), packet);
    }
}

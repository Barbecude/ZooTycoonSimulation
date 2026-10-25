package net.zootycoon.animal;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.zootycoon.ZooTycoonMod;
import net.zootycoon.network.ZooNetworkPayloads;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages player-driven bathing and wet status for zoo animals.
 * Implements Phase 9 two-step bathing requiring a watering can and brush.
 */
public class AnimalBathingManager {

    public static final TagKey<Item> WATERING_CANS = TagKey.create(Registries.ITEM, ZooTycoonMod.id("watering_cans"));
    public static final int WET_DURATION_TICKS = 600; // 30 seconds (600 ticks)

    static {
        net.zootycoon.network.ZookeeperBathingPayloads.register();
    }

    private static final Map<UUID, Long> WET_EXPIRY_MAP = new ConcurrentHashMap<>();

    public static boolean isWateringCan(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        if (stack.is(WATERING_CANS) || stack.is(net.minecraft.world.item.Items.WATER_BUCKET)) {
            return true;
        }
        if (stack.getItem() instanceof net.zootycoon.item.WateringCanItem) {
            return true;
        }
        String path = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        return path.contains("watering_can");
    }

    public static boolean isWet(LivingEntity animal) {
        if (animal == null || !animal.isAlive()) {
            return false;
        }
        Long expiry = WET_EXPIRY_MAP.get(animal.getUUID());
        if (expiry == null) {
            return false;
        }
        if (animal.level().getGameTime() < expiry) {
            return true;
        }
        WET_EXPIRY_MAP.remove(animal.getUUID());
        return false;
    }

    public static void setWet(LivingEntity animal, int durationTicks) {
        if (animal == null) return;
        WET_EXPIRY_MAP.put(animal.getUUID(), animal.level().getGameTime() + durationTicks);
    }

    public static void reset() {
        WET_EXPIRY_MAP.clear();
    }

    public static boolean waterAnimal(LivingEntity animal, ServerPlayer player, ItemStack held, InteractionHand hand) {
        if (animal == null || !animal.isAlive() || !(animal.level() instanceof ServerLevel sl)) {
            return false;
        }

        setWet(animal, WET_DURATION_TICKS);

        sl.playSound(null, animal.blockPosition(), SoundEvents.BUCKET_EMPTY, SoundSource.PLAYERS, 1.0f, 1.2f);
        sl.playSound(null, animal.blockPosition(), SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS, 0.8f, 1.0f);

        sl.sendParticles(ParticleTypes.SPLASH, animal.getX(), animal.getY() + animal.getBbHeight() * 0.5, animal.getZ(),
                30, animal.getBbWidth() * 0.4, animal.getBbHeight() * 0.3, animal.getBbWidth() * 0.4, 0.1);
        sl.sendParticles(ParticleTypes.DRIPPING_WATER, animal.getX(), animal.getY() + animal.getBbHeight() * 0.8, animal.getZ(),
                15, animal.getBbWidth() * 0.3, 0.1, animal.getBbWidth() * 0.3, 0.05);

        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(player, new ZooNetworkPayloads.PlayBrushAnimationPayload());

        for (ServerPlayer sp2 : sl.players()) {
            if (sp2.distanceToSqr(animal) <= 4096.0D) {
                net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(sp2, new ZooNetworkPayloads.SyncAnimalWetPayload(animal.getId(), true));
            }
        }

        // Notify active inspectors to immediately update hologram
        ZooInspectionService.notifyAnimalUpdated(animal);
        ZooNetworkPayloads.sendAnimalSync(player);

        return true;
    }

    public static boolean brushAnimal(LivingEntity animal, ServerPlayer player, ItemStack held, InteractionHand hand) {
        if (animal == null || !animal.isAlive() || !(animal.level() instanceof ServerLevel sl)) {
            return false;
        }

        int currentClean = ZooAnimalHungerManager.getCleanliness(animal);
        int newClean = Math.min(ZooAnimalHungerManager.MAX_CLEANLINESS, currentClean + 35);
        ZooAnimalHungerManager.setCleanliness(animal, newClean);

        if (!player.getAbilities().instabuild) {
            held.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
        }

        sl.playSound(null, animal.blockPosition(), SoundEvents.BRUSH_GENERIC, SoundSource.PLAYERS, 1.0f, 1.0f);

        sl.sendParticles(ParticleTypes.BUBBLE_POP, animal.getX(), animal.getY() + animal.getBbHeight() * 0.5, animal.getZ(),
                20, animal.getBbWidth() * 0.4, animal.getBbHeight() * 0.3, animal.getBbWidth() * 0.4, 0.05);
        sl.sendParticles(ParticleTypes.WAX_OFF, animal.getX(), animal.getY() + animal.getBbHeight() * 0.5, animal.getZ(),
                12, animal.getBbWidth() * 0.4, animal.getBbHeight() * 0.3, animal.getBbWidth() * 0.4, 0.02);
        sl.sendParticles(ParticleTypes.SPLASH, animal.getX(), animal.getY() + animal.getBbHeight() * 0.5, animal.getZ(),
                8, animal.getBbWidth() * 0.3, 0.1, animal.getBbWidth() * 0.3, 0.05);

        player.swing(hand, true);

        for (ServerPlayer sp2 : sl.players()) {
            if (sp2.distanceToSqr(animal) <= 4096.0D) {
                net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(sp2, new ZooNetworkPayloads.SyncAnimalWetPayload(animal.getId(), false));
            }
        }

        // Notify active inspectors to immediately update hologram
        ZooInspectionService.notifyAnimalUpdated(animal);
        ZooNetworkPayloads.sendAnimalSync(player);

        return true;
    }
}


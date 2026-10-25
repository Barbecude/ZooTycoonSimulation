package net.zootycoon.client.bathing;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.zootycoon.animal.AnimalBathingManager;
import net.zootycoon.animal.ZooAnimalHungerManager;
import net.zootycoon.client.ZooTycoonClient;
import net.zootycoon.client.animation.PlayerBrushAnimationManager;
import net.zootycoon.client.inspect.InspectionClientCache;
import net.zootycoon.client.ui.ZooHudOverlay;
import net.zootycoon.network.ZooNetworkPayloads;

public class PlayerBathingClientController {

    public static final int STAGE_NONE = 0;
    public static final int STAGE_WATERING = 1;
    public static final int STAGE_BRUSHING = 2;

    public static final int MAX_PROGRESS_TICKS_WATERING = 60; // 3.0 seconds (slowed down)
    public static final int MAX_PROGRESS_TICKS_BRUSHING = 60; // 3.0 seconds (slowed down)

    private static LivingEntity activeTargetAnimal = null;
    private static int targetEntityId = -1;
    private static int currentStage = STAGE_NONE;
    private static int progressTicks = 0;
    private static int warningCooldown = 0;
    private static int justCleanedEntityId = -1;
    private static long justCleanedTimestamp = 0L;

    public static boolean isAnimalClean(LivingEntity target) {
        if (target == null) return false;
        if (target.getId() == justCleanedEntityId && (System.currentTimeMillis() - justCleanedTimestamp) < 10000L) {
            return true;
        }
        ZooNetworkPayloads.AnimalSyncData sync = ZooTycoonClient.clientAnimalMap.get(target.getId());
        if (sync != null && sync.cleanliness() >= 100) {
            return true;
        }
        InspectionClientCache.Snapshot snap = InspectionClientCache.getSnapshot(target.getId());
        if (snap != null && snap.cleanliness() >= 100) {
            return true;
        }
        return false;
    }

    public static boolean isBathing() {
        return currentStage != STAGE_NONE && activeTargetAnimal != null && activeTargetAnimal.isAlive();
    }

    public static LivingEntity getTargetAnimal() {
        return activeTargetAnimal;
    }

    public static int getTargetEntityId() {
        return targetEntityId;
    }

    public static int getCurrentStage() {
        return currentStage;
    }

    public static int getMaxTicks() {
        return (currentStage == STAGE_WATERING) ? MAX_PROGRESS_TICKS_WATERING : MAX_PROGRESS_TICKS_BRUSHING;
    }

    public static float getProgress() {
        return Math.min(1.0f, (float) progressTicks / (float) getMaxTicks());
    }

    public static void clientTick(Minecraft mc) {
        if (warningCooldown > 0) {
            warningCooldown--;
        }

        if (mc.level == null || mc.player == null) {
            reset();
            return;
        }

        // Must hold right click
        if (!mc.options.keyUse.isDown()) {
            reset();
            return;
        }

        ItemStack held = mc.player.getMainHandItem();
        boolean isCan = AnimalBathingManager.isWateringCan(held);
        boolean isBrush = held.is(Items.BRUSH);

        if (!isCan && !isBrush) {
            held = mc.player.getOffhandItem();
            isCan = AnimalBathingManager.isWateringCan(held);
            isBrush = held.is(Items.BRUSH);
        }

        if (!isCan && !isBrush) {
            reset();
            return;
        }

        LivingEntity target = findLookTargetAnimal(mc, 5.0D);
        if (!isValidAnimalTarget(target)) {
            reset();
            return;
        }

        int targetId = target.getId();
        activeTargetAnimal = target;
        targetEntityId = targetId;

        if (isCan) {
            if (AnimalBathingManager.isWet(target)) {
                if (warningCooldown <= 0) {
                    ZooHudOverlay.showToast(
                            "zootycoon.toast.title.animal",
                            "zootycoon.bathing.already_watered",
                            ZooHudOverlay.TOAST_INFO
                    );
                    warningCooldown = 60;
                }
                reset();
                return;
            }

            if (currentStage != STAGE_WATERING) {
                currentStage = STAGE_WATERING;
                progressTicks = 0;
            }

            progressTicks++;
            mc.player.swing(InteractionHand.MAIN_HAND);

            // Water splashing effects while holding
            if (progressTicks % 4 == 0) {
                mc.level.playLocalSound(target.getX(), target.getY(), target.getZ(),
                        SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS, 0.6f, 1.2f, false);
                for (int i = 0; i < 4; i++) {
                    double ox = (mc.level.random.nextDouble() - 0.5) * target.getBbWidth() * 0.8;
                    double oz = (mc.level.random.nextDouble() - 0.5) * target.getBbWidth() * 0.8;
                    double oy = target.getBbHeight() * 0.65;
                    mc.level.addParticle(ParticleTypes.SPLASH, target.getX() + ox, target.getY() + oy, target.getZ() + oz, 0, 0.1, 0);
                    mc.level.addParticle(ParticleTypes.DRIPPING_WATER, target.getX() + ox, target.getY() + oy, target.getZ() + oz, 0, 0, 0);
                }
            }

            if (progressTicks >= MAX_PROGRESS_TICKS_WATERING) {
                // Completed Stage 1: Watering
                AnimalBathingManager.setWet(target, AnimalBathingManager.WET_DURATION_TICKS);
                ClientPlayNetworking.send(new ZooNetworkPayloads.CompletePlayerBathingPayload(targetId, 1));
                mc.level.playLocalSound(target.getX(), target.getY(), target.getZ(),
                        SoundEvents.BUCKET_EMPTY, SoundSource.PLAYERS, 1.0f, 1.0f, false);
                for (int i = 0; i < 15; i++) {
                    double ox = (mc.level.random.nextDouble() - 0.5) * target.getBbWidth();
                    double oz = (mc.level.random.nextDouble() - 0.5) * target.getBbWidth();
                    mc.level.addParticle(ParticleTypes.SPLASH, target.getX() + ox, target.getY() + target.getBbHeight() * 0.6, target.getZ() + oz, 0, 0.15, 0);
                }
                reset();
            }
        } else {
            // Stage 2: Brushing
            if (!AnimalBathingManager.isWet(target)) {
                if (!isAnimalClean(target) && warningCooldown <= 0) {
                    mc.player.displayClientMessage(Component.translatable("zootycoon.bathing.need_water"), true);
                    warningCooldown = 30;
                }
                reset();
                return;
            }

            if (currentStage != STAGE_BRUSHING) {
                currentStage = STAGE_BRUSHING;
                progressTicks = 0;
            }

            progressTicks++;
            PlayerBrushAnimationManager.startBrushing();

            // Soap bubble particles & brushing sound while holding
            if (progressTicks % 4 == 0) {
                mc.level.playLocalSound(target.getX(), target.getY(), target.getZ(),
                        SoundEvents.BRUSH_GENERIC, SoundSource.PLAYERS, 0.8f, 1.0f, false);
                for (int i = 0; i < 3; i++) {
                    double ox = (mc.level.random.nextDouble() - 0.5) * target.getBbWidth() * 0.8;
                    double oz = (mc.level.random.nextDouble() - 0.5) * target.getBbWidth() * 0.8;
                    double oy = target.getBbHeight() * (0.3 + mc.level.random.nextDouble() * 0.4);
                    mc.level.addParticle(ParticleTypes.BUBBLE_POP, target.getX() + ox, target.getY() + oy, target.getZ() + oz, 0, 0.05, 0);
                    mc.level.addParticle(ParticleTypes.WAX_OFF, target.getX() + ox, target.getY() + oy, target.getZ() + oz, 0, 0.02, 0);
                }
            }

            if (progressTicks >= MAX_PROGRESS_TICKS_BRUSHING) {
                // Completed Stage 2: Brushing
                justCleanedEntityId = targetId;
                justCleanedTimestamp = System.currentTimeMillis();
                warningCooldown = 60;
                AnimalBathingManager.setWet(target, 0);
                ClientPlayNetworking.send(new ZooNetworkPayloads.CompletePlayerBathingPayload(targetId, 2));
                mc.level.playLocalSound(target.getX(), target.getY(), target.getZ(),
                        SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.7f, 1.4f, false);
                for (int i = 0; i < 20; i++) {
                    double ox = (mc.level.random.nextDouble() - 0.5) * target.getBbWidth();
                    double oz = (mc.level.random.nextDouble() - 0.5) * target.getBbWidth();
                    mc.level.addParticle(ParticleTypes.WAX_OFF, target.getX() + ox, target.getY() + target.getBbHeight() * 0.5, target.getZ() + oz, 0, 0.05, 0);
                }
                reset();
            }
        }
    }

    public static void reset() {
        activeTargetAnimal = null;
        targetEntityId = -1;
        currentStage = STAGE_NONE;
        progressTicks = 0;
    }

    private static boolean isValidAnimalTarget(LivingEntity target) {
        if (target == null || !target.isAlive()) return false;
        if (target instanceof Player) return false;
        if (target instanceof net.zootycoon.entity.ZooZookeeperEntity) return false;
        if (target instanceof net.zootycoon.entity.ZooBuilderEntity) return false;
        if (target instanceof net.zootycoon.entity.ZooCashierEntity) return false;
        if (target instanceof net.zootycoon.entity.ZooVisitorEntity) return false;
        if (target instanceof net.zootycoon.entity.ZooHunterEntity) return false;

        return ZooTycoonClient.clientAnimalMap.containsKey(target.getId())
                || ZooAnimalHungerManager.isZooAnimalCandidate(target);
    }

    private static LivingEntity findLookTargetAnimal(Minecraft mc, double maxDistance) {
        if (mc.player == null || mc.level == null) return null;

        // 1. Direct crosshair pick entity (standard Minecraft crosshair target)
        if (mc.crosshairPickEntity instanceof LivingEntity picked && picked.isAlive()) {
            if (mc.player.distanceToSqr(picked) <= maxDistance * maxDistance) {
                return picked;
            }
        }

        // 2. Raycast fallback
        Vec3 eyePos = mc.player.getEyePosition(1.0F);
        Vec3 viewVec = mc.player.getViewVector(1.0F);
        Vec3 reachVec = eyePos.add(viewVec.x * maxDistance, viewVec.y * maxDistance, viewVec.z * maxDistance);
        AABB searchBox = mc.player.getBoundingBox().expandTowards(viewVec.scale(maxDistance)).inflate(1.5D);

        EntityHitResult hit = ProjectileUtil.getEntityHitResult(
                mc.player,
                eyePos,
                reachVec,
                searchBox,
                entity -> entity instanceof LivingEntity && !entity.isSpectator() && entity.isPickable(),
                maxDistance * maxDistance
        );

        if (hit != null && hit.getEntity() instanceof LivingEntity living && living.isAlive()) {
            return living;
        }

        return null;
    }
}

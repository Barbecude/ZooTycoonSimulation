package net.zootycoon.client.bathing;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.zootycoon.animal.AnimalBathingManager;
import net.zootycoon.animal.ZooAnimalHungerManager;
import net.zootycoon.client.animation.PlayerBrushAnimationManager;
import net.zootycoon.network.ZooNetworkPayloads;

public class PlayerBathingClientController {

    public static final int STAGE_NONE = 0;
    public static final int STAGE_WATERING = 1;
    public static final int STAGE_BRUSHING = 2;

    public static final int MAX_PROGRESS_TICKS = 35; // ~1.75 seconds of holding

    private static int targetEntityId = -1;
    private static int currentStage = STAGE_NONE;
    private static int progressTicks = 0;
    private static int completionCooldown = 0;

    public static boolean isBathing() {
        return currentStage != STAGE_NONE && targetEntityId != -1;
    }

    public static int getTargetEntityId() {
        return targetEntityId;
    }

    public static int getCurrentStage() {
        return currentStage;
    }

    public static float getProgress() {
        return Math.min(1.0f, (float) progressTicks / (float) MAX_PROGRESS_TICKS);
    }

    public static void clientTick(Minecraft mc) {
        if (completionCooldown > 0) {
            completionCooldown--;
        }

        if (mc.level == null || mc.player == null) {
            reset();
            return;
        }

        // Must hold right click (use key)
        if (!mc.options.keyUse.isDown()) {
            reset();
            return;
        }

        ItemStack held = mc.player.getMainHandItem();
        boolean isCan = AnimalBathingManager.isWateringCan(held);
        boolean isBrush = held.is(Items.BRUSH);

        if (!isCan && !isBrush) {
            reset();
            return;
        }

        LivingEntity target = findLookTargetAnimal(mc, 4.5D);
        if (target == null || !target.isAlive() || !ZooAnimalHungerManager.isZooAnimalCandidate(target) || !ZooAnimalHungerManager.isTagged(target)) {
            reset();
            return;
        }

        int targetId = target.getId();

        if (isCan) {
            if (currentStage != STAGE_WATERING || targetEntityId != targetId) {
                currentStage = STAGE_WATERING;
                targetEntityId = targetId;
                progressTicks = 0;
            }

            progressTicks++;
            mc.player.swing(InteractionHand.MAIN_HAND);

            // Water splashing particles & sound effects while holding
            if (progressTicks % 5 == 0) {
                mc.level.playLocalSound(target.getX(), target.getY(), target.getZ(),
                        SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS, 0.6f, 1.2f, false);
                for (int i = 0; i < 4; i++) {
                    double ox = (mc.level.random.nextDouble() - 0.5) * target.getBbWidth() * 0.8;
                    double oz = (mc.level.random.nextDouble() - 0.5) * target.getBbWidth() * 0.8;
                    double oy = target.getBbHeight() * 0.7;
                    mc.level.addParticle(ParticleTypes.SPLASH, target.getX() + ox, target.getY() + oy, target.getZ() + oz, 0, 0.1, 0);
                    mc.level.addParticle(ParticleTypes.DRIPPING_WATER, target.getX() + ox, target.getY() + oy, target.getZ() + oz, 0, 0, 0);
                }
            }

            if (progressTicks >= MAX_PROGRESS_TICKS) {
                // Completed Stage 1: Watering
                ClientPlayNetworking.send(new ZooNetworkPayloads.CompletePlayerBathingPayload(targetId, 1));
                mc.level.playLocalSound(target.getX(), target.getY(), target.getZ(),
                        SoundEvents.BUCKET_EMPTY, SoundSource.PLAYERS, 1.0f, 1.0f, false);
                for (int i = 0; i < 15; i++) {
                    double ox = (mc.level.random.nextDouble() - 0.5) * target.getBbWidth();
                    double oz = (mc.level.random.nextDouble() - 0.5) * target.getBbWidth();
                    mc.level.addParticle(ParticleTypes.SPLASH, target.getX() + ox, target.getY() + target.getBbHeight() * 0.6, target.getZ() + oz, 0, 0.15, 0);
                }
                reset();
                completionCooldown = 10;
            }
        } else {
            // Stage 2: Brushing
            if (!AnimalBathingManager.isWet(target)) {
                if (progressTicks == 0 && completionCooldown == 0) {
                    mc.player.displayClientMessage(Component.translatable("zootycoon.bathing.need_water"), true);
                    completionCooldown = 20;
                }
                reset();
                return;
            }

            if (currentStage != STAGE_BRUSHING || targetEntityId != targetId) {
                currentStage = STAGE_BRUSHING;
                targetEntityId = targetId;
                progressTicks = 0;
            }

            progressTicks++;
            PlayerBrushAnimationManager.startBrushing();

            // Soap bubble particles & brushing sound while holding
            if (progressTicks % 5 == 0) {
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

            if (progressTicks >= MAX_PROGRESS_TICKS) {
                // Completed Stage 2: Brushing
                ClientPlayNetworking.send(new ZooNetworkPayloads.CompletePlayerBathingPayload(targetId, 2));
                mc.level.playLocalSound(target.getX(), target.getY(), target.getZ(),
                        SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.7f, 1.4f, false);
                for (int i = 0; i < 20; i++) {
                    double ox = (mc.level.random.nextDouble() - 0.5) * target.getBbWidth();
                    double oz = (mc.level.random.nextDouble() - 0.5) * target.getBbWidth();
                    mc.level.addParticle(ParticleTypes.WAX_OFF, target.getX() + ox, target.getY() + target.getBbHeight() * 0.5, target.getZ() + oz, 0, 0.05, 0);
                }
                reset();
                completionCooldown = 10;
            }
        }
    }

    public static void reset() {
        targetEntityId = -1;
        currentStage = STAGE_NONE;
        progressTicks = 0;
    }

    private static LivingEntity findLookTargetAnimal(Minecraft mc, double maxDistance) {
        if (mc.player == null || mc.level == null) return null;
        Vec3 eyePos = mc.player.getEyePosition(1.0F);
        Vec3 viewVec = mc.player.getViewVector(1.0F);
        Vec3 reachVec = eyePos.add(viewVec.x * maxDistance, viewVec.y * maxDistance, viewVec.z * maxDistance);
        AABB searchBox = mc.player.getBoundingBox().expandTowards(viewVec.scale(maxDistance)).inflate(1.0D);

        EntityHitResult hit = ProjectileUtil.getEntityHitResult(
                mc.player,
                eyePos,
                reachVec,
                searchBox,
                entity -> entity instanceof LivingEntity && !entity.isSpectator() && entity.isPickable(),
                maxDistance * maxDistance
        );

        if (hit != null && hit.getEntity() instanceof LivingEntity living) {
            return living;
        }

        // Fallback to crosshairPickEntity if raycast missed bounding edge
        if (mc.crosshairPickEntity instanceof LivingEntity pickedLiving && mc.player.distanceToSqr(pickedLiving) <= maxDistance * maxDistance) {
            return pickedLiving;
        }

        return null;
    }
}


package net.zootycoon.client.inspect;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.zootycoon.animal.ZooAnimalHungerManager;
import net.zootycoon.item.ZooItems;

import java.util.function.BiConsumer;

public class InspectionTargetTracker {

    private static LivingEntity currentTarget = null;
    private static BiConsumer<LivingEntity, LivingEntity> targetChangeListener = null;

    public static LivingEntity getTarget() {
        return currentTarget;
    }

    public static int getTargetId() {
        return currentTarget != null ? currentTarget.getId() : -1;
    }

    public static void setTargetChangeListener(BiConsumer<LivingEntity, LivingEntity> listener) {
        targetChangeListener = listener;
    }

    public static void onTargetChanged(LivingEntity oldEntity, LivingEntity newEntity) {
        if (targetChangeListener != null) {
            targetChangeListener.accept(oldEntity, newEntity);
        }
    }

    public static void reset() {
        setTarget(null);
    }

    private static void setTarget(LivingEntity newTarget) {
        if (currentTarget != newTarget) {
            LivingEntity oldTarget = currentTarget;
            currentTarget = newTarget;
            onTargetChanged(oldTarget, newTarget);
        }
    }

    public static void tick(Minecraft mc) {
        if (mc.player == null || mc.level == null) {
            reset();
            return;
        }

        // 1. Check activation requirements (Farm & Charm pattern)
        if (mc.screen != null || mc.options.hideGui) {
            reset();
            return;
        }

        Player player = mc.player;
        boolean holdingMagnifier = player.getMainHandItem().is(ZooItems.MAGNIFYING_GLASS)
                || player.getOffhandItem().is(ZooItems.MAGNIFYING_GLASS);
        if (!holdingMagnifier) {
            reset();
            return;
        }

        // 2. Target detection with 16 blocks range and solid block collider collision
        LivingEntity validTarget = findTarget(mc, player, mc.level);
        setTarget(validTarget);
    }

    private static LivingEntity findTarget(Minecraft mc, Player player, Level level) {
        Vec3 eyePos = player.getEyePosition(1.0F);
        Vec3 lookVec = player.getViewVector(1.0F);

        // A. Direct check on vanilla crosshairPickEntity
        if (mc.crosshairPickEntity instanceof LivingEntity living && living.isAlive() && ZooAnimalHungerManager.isZooAnimalCandidate(living)) {
            double distSqr = player.distanceToSqr(living);
            if (distSqr <= 16.0 * 16.0) {
                // Ensure line of sight is not occluded by a solid collider block
                HitResult blockHit = level.clip(new ClipContext(eyePos, eyePos.add(lookVec.scale(Math.sqrt(distSqr))), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
                if (blockHit.getType() == HitResult.Type.MISS || eyePos.distanceToSqr(blockHit.getLocation()) >= distSqr) {
                    return living;
                }
            }
        }

        // B. Extended raycast up to 16 blocks when crosshairPickEntity is null
        if (mc.crosshairPickEntity == null) {
            Vec3 reachVec = eyePos.add(lookVec.scale(16.0));
            HitResult blockHit = level.clip(new ClipContext(eyePos, reachVec, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
            double maxDistSqr = 16.0 * 16.0;
            if (blockHit.getType() != HitResult.Type.MISS) {
                maxDistSqr = eyePos.distanceToSqr(blockHit.getLocation());
            }

            AABB searchBox = player.getBoundingBox().expandTowards(lookVec.scale(16.0)).inflate(1.0);
            EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                    player,
                    eyePos,
                    reachVec,
                    searchBox,
                    e -> e instanceof LivingEntity l && l.isAlive() && !e.isSpectator(),
                    maxDistSqr
            );

            if (entityHit != null && entityHit.getEntity() instanceof LivingEntity living) {
                if (ZooAnimalHungerManager.isZooAnimalCandidate(living)) {
                    return living;
                }
            }
        }

        return null;
    }
}

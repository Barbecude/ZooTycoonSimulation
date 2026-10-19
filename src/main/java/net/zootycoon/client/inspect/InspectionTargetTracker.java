package net.zootycoon.client.inspect;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
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
import net.zootycoon.network.InspectionPayloads;

import java.util.function.BiConsumer;

public class InspectionTargetTracker {

    private static LivingEntity currentTarget = null;
    private static BiConsumer<LivingEntity, LivingEntity> targetChangeListener = null;
    private static int targetRetentionTicks = 0;

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
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() != null) {
            try {
                if (oldEntity != null) {
                    ClientPlayNetworking.send(new InspectionPayloads.InspectStopPayload());
                }
                if (newEntity != null) {
                    ClientPlayNetworking.send(new InspectionPayloads.InspectStartPayload(newEntity.getId()));
                }
            } catch (Exception ignored) {}
        }

        if (targetChangeListener != null) {
            targetChangeListener.accept(oldEntity, newEntity);
        }
    }

    public static void reset() {
        targetRetentionTicks = 0;
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

        // If holding right-click with magnifying glass, maintain active target lock
        if (player.isUsingItem() && player.getUseItem().is(ZooItems.MAGNIFYING_GLASS)) {
            if (currentTarget != null && currentTarget.isAlive() && isZooRegisteredAnimal(currentTarget)) {
                if (player.distanceToSqr(currentTarget) <= 24.0 * 24.0) {
                    targetRetentionTicks = 30;
                    return;
                }
            }
        }

        // 2. Target detection with 16 blocks range and solid block collider collision
        LivingEntity validTarget = findTarget(mc, player, mc.level);
        setTarget(validTarget);
    }

    public static boolean isZooRegisteredAnimal(LivingEntity e) {
        if (e == null || !e.isAlive()) return false;
        if (!ZooAnimalHungerManager.isZooAnimalCandidate(e)) return false;

        int id = e.getId();
        // 1. Check clientAnimals list from server sync
        for (net.zootycoon.network.ZooNetworkPayloads.AnimalSyncData a : net.zootycoon.client.ZooTycoonClient.clientAnimals) {
            if (a.entityId() == id) return true;
        }

        // 2. Check snapshot cache
        InspectionClientCache.Snapshot snap = InspectionClientCache.getSnapshot(id);
        if (snap != null && snap.registered()) return true;

        // 3. Check entity tags (singleplayer / integrated server)
        if (e.getTags().contains("zoo_registered")) return true;

        return false;
    }

    private static LivingEntity findTarget(Minecraft mc, Player player, Level level) {
        Vec3 eyePos = player.getEyePosition(1.0F);
        Vec3 lookVec = player.getViewVector(1.0F);
        Vec3 reachVec = eyePos.add(lookVec.scale(16.0));

        // 1. Calculate maximum reach distance before solid block collision
        HitResult blockHit = level.clip(new ClipContext(eyePos, reachVec, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        double maxDistSqr = 16.0 * 16.0;
        if (blockHit.getType() != HitResult.Type.MISS) {
            maxDistSqr = eyePos.distanceToSqr(blockHit.getLocation());
        }

        // A. Direct check on vanilla crosshairPickEntity (strictly registered animals only)
        if (mc.crosshairPickEntity instanceof LivingEntity living && living.isAlive() && isZooRegisteredAnimal(living)) {
            double distSqr = player.distanceToSqr(living);
            if (distSqr <= maxDistSqr) {
                targetRetentionTicks = 15;
                return living;
            }
        }

        // B. Extended raycast up to 16 blocks (strictly registered animals only)
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
            if (isZooRegisteredAnimal(living)) {
                targetRetentionTicks = 15;
                return living;
            }
        }

        // C. Hologram Card Volume Raycast:
        // When inspecting an animal, allow looking up at the hologram card floating above its head
        if (currentTarget != null && currentTarget.isAlive() && isZooRegisteredAnimal(currentTarget)) {
            double targetDistSqr = player.distanceToSqr(currentTarget);
            if (targetDistSqr <= 16.0 * 16.0) {
                // Expanded bounding box covering both the animal and the hologram floating above it
                double halfWidth = Math.max(0.9, currentTarget.getBbWidth() * 0.9);
                AABB hologramBox = new AABB(
                        currentTarget.getX() - halfWidth,
                        currentTarget.getY(),
                        currentTarget.getZ() - halfWidth,
                        currentTarget.getX() + halfWidth,
                        currentTarget.getY() + currentTarget.getBbHeight() + 2.8,
                        currentTarget.getZ() + halfWidth
                );

                java.util.Optional<Vec3> clip = hologramBox.clip(eyePos, reachVec);
                if (clip.isPresent()) {
                    double clipDistSqr = eyePos.distanceToSqr(clip.get());
                    if (clipDistSqr <= maxDistSqr) {
                        targetRetentionTicks = 15;
                        return currentTarget;
                    }
                }
            }
        }

        // D. Brief grace buffer (15 ticks = 0.75s) to avoid flickering when looking across the card
        if (currentTarget != null && currentTarget.isAlive()) {
            if (targetRetentionTicks > 0) {
                targetRetentionTicks--;
                return currentTarget;
            }
        }

        targetRetentionTicks = 0;
        return null;
    }
}


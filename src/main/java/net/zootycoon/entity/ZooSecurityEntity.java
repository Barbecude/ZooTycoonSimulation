package net.zootycoon.entity;

import net.minecraft.world.level.Level;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

public class ZooSecurityEntity extends PathfinderMob {
    private static final EntityDataAccessor<String> SECURITY_NAME =
            SynchedEntityData.defineId(ZooSecurityEntity.class, EntityDataSerializers.STRING);

    private static final String[] NAMES = new String[]{
            "Bambang", "Agus", "Tejo", "Bram", "Dika", "Eko", "Joko", "Sultan", "Wahyu", "Rizky", "Prasetyo", "Budi"
    };

    private UUID ownerUuid = null;

    public ZooSecurityEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
        this.setCustomNameVisible(true);
        this.xpReward = 10;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        String randomName = NAMES[(int) (Math.random() * NAMES.length)];
        builder.define(SECURITY_NAME, randomName);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.35D)
                .add(Attributes.ATTACK_DAMAGE, 7.0D)
                .add(Attributes.FOLLOW_RANGE, 36.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.3D, false));
        this.goalSelector.addGoal(2, new SecurityClaimStrollGoal(this, 0.6D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));

        // Priority 1: Hunt and eliminate Hunter mobs that invade the zoo!
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, ZooHunterEntity.class, true));
        // Priority 2: Retaliate against attackers
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
    }

    public static class SecurityClaimStrollGoal extends WaterAvoidingRandomStrollGoal {
        private final ZooSecurityEntity security;

        public SecurityClaimStrollGoal(ZooSecurityEntity mob, double speedModifier) {
            super(mob, speedModifier);
            this.security = mob;
        }

        @Override
        protected net.minecraft.world.phys.Vec3 getPosition() {
            net.minecraft.world.phys.Vec3 pos = super.getPosition();
            if (pos != null && this.security.level() instanceof net.minecraft.server.level.ServerLevel sLevel) {
                net.zootycoon.claim.ZooClaimManager cm = net.zootycoon.claim.ZooClaimManager.get(sLevel);
                net.minecraft.core.BlockPos bp = net.minecraft.core.BlockPos.containing(pos.x, pos.y, pos.z);
                boolean inside = (this.security.ownerUuid != null)
                        ? cm.isClaimedBy(bp, this.security.ownerUuid)
                        : cm.isInsideAnyClaim(bp);
                if (!inside) {
                    net.zootycoon.claim.ZooClaim claim = this.security.findClosestClaim(cm);
                    if (claim != null) {
                        var box = claim.getBox();
                        int minX = Math.min(box.minX() + 2, box.maxX());
                        int maxX = Math.max(box.maxX() - 2, minX);
                        int minZ = Math.min(box.minZ() + 2, box.maxZ());
                        int maxZ = Math.max(box.maxZ() - 2, minZ);
                        int tx = Math.clamp((int) Math.floor(pos.x), minX, maxX);
                        int tz = Math.clamp((int) Math.floor(pos.z), minZ, maxZ);
                        int ty = sLevel.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, tx, tz);
                        return new net.minecraft.world.phys.Vec3(tx + 0.5D, ty, tz + 0.5D);
                    }
                }
            }
            return pos;
        }
    }

    public net.zootycoon.claim.ZooClaim findClosestClaim(net.zootycoon.claim.ZooClaimManager claimManager) {
        java.util.Collection<net.zootycoon.claim.ZooClaim> claims = (ownerUuid != null)
                ? claimManager.getPlayerClaims(ownerUuid)
                : claimManager.getAllClaims();
        if (claims.isEmpty() && ownerUuid != null) {
            claims = claimManager.getAllClaims();
        }
        net.zootycoon.claim.ZooClaim closest = null;
        double minDistSq = Double.MAX_VALUE;
        net.minecraft.core.BlockPos currentPos = this.blockPosition();
        for (net.zootycoon.claim.ZooClaim claim : claims) {
            var box = claim.getBox();
            int cx = (box.minX() + box.maxX()) / 2;
            int cz = (box.minZ() + box.maxZ()) / 2;
            double d = currentPos.distSqr(new net.minecraft.core.Vec3i(cx, currentPos.getY(), cz));
            if (d < minDistSq) {
                minDistSq = d;
                closest = claim;
            }
        }
        return closest;
    }

    private void returnToClaimedLand(net.minecraft.server.level.ServerLevel sLevel, net.zootycoon.claim.ZooClaimManager claimManager) {
        net.zootycoon.claim.ZooClaim closestClaim = findClosestClaim(claimManager);
        if (closestClaim == null) return;

        var box = closestClaim.getBox();
        net.minecraft.core.BlockPos currentPos = this.blockPosition();
        int minX = Math.min(box.minX() + 2, box.maxX());
        int maxX = Math.max(box.maxX() - 2, minX);
        int minZ = Math.min(box.minZ() + 2, box.maxZ());
        int maxZ = Math.max(box.maxZ() - 2, minZ);
        int targetX = Math.clamp(currentPos.getX(), minX, maxX);
        int targetZ = Math.clamp(currentPos.getZ(), minZ, maxZ);
        int targetY = sLevel.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, targetX, targetZ);
        net.minecraft.core.BlockPos safePos = new net.minecraft.core.BlockPos(targetX, targetY, targetZ);

        double distToSafe = this.distanceToSqr(safePos.getX() + 0.5, safePos.getY(), safePos.getZ() + 0.5);
        if (distToSafe > 16.0 * 16.0) {
            this.teleportTo(safePos.getX() + 0.5, safePos.getY(), safePos.getZ() + 0.5);
            this.getNavigation().stop();
        } else {
            this.getNavigation().moveTo(safePos.getX() + 0.5, safePos.getY(), safePos.getZ() + 0.5, 1.15);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide() && level() instanceof net.minecraft.server.level.ServerLevel sLevel) {
            net.zootycoon.claim.ZooClaimManager claimManager = net.zootycoon.claim.ZooClaimManager.get(sLevel);
            if (ownerUuid == null && tickCount % 20 == 0) {
                net.zootycoon.claim.ZooClaim nearest = findClosestClaim(claimManager);
                if (nearest != null) {
                    ownerUuid = nearest.getOwnerUuid();
                }
            }

            if (this.ownerUuid != null && net.zootycoon.economy.ZooStaffManager.get(sLevel).areStaffOnStrike(this.ownerUuid)) {
                if (this.tickCount % 60 == 0) {
                    sLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.ANGRY_VILLAGER, this.getX(), this.getY() + 1.2D, this.getZ(), 1, 0.1, 0.1, 0.1, 0.0);
                }
                this.setTarget(null);
                this.getNavigation().stop();
                return;
            }

            // When not actively chasing a hunter/target, strictly stay inside the claimed zoo area
            if (getTarget() == null || !getTarget().isAlive()) {
                net.minecraft.core.BlockPos currentPos = this.blockPosition();
                boolean inside = (ownerUuid != null)
                        ? claimManager.isClaimedBy(currentPos, ownerUuid)
                        : claimManager.isInsideAnyClaim(currentPos);
                if (!inside) {
                    if (tickCount % 10 == 0) {
                        returnToClaimedLand(sLevel, claimManager);
                    }
                } else if (this.getNavigation().isInProgress()) {
                    net.minecraft.core.BlockPos targetPos = this.getNavigation().getTargetPos();
                    if (targetPos != null) {
                        boolean targetInside = (ownerUuid != null)
                                ? claimManager.isClaimedBy(targetPos, ownerUuid)
                                : claimManager.isInsideAnyClaim(targetPos);
                        if (!targetInside) {
                            this.getNavigation().stop();
                        }
                    }
                }
            }

            if (tickCount % 20 == 0) {
                String name = getSecurityName();
                if (getTarget() instanceof ZooHunterEntity) {
                    this.setCustomName(Component.literal(name + " " + net.zootycoon.util.ZooTranslations.get("zootycoon.entity.security.combat_tag")).withStyle(ChatFormatting.RED));
                } else {
                    this.setCustomName(Component.literal(name + " " + net.zootycoon.util.ZooTranslations.get("zootycoon.entity.security.tag")).withStyle(ChatFormatting.AQUA));
                }
            }
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!level().isClientSide() && hand == InteractionHand.MAIN_HAND && player instanceof ServerPlayer sPlayer) {
            String name = getSecurityName();
            boolean isAlert = getTarget() != null;
            String targetName = isAlert ? getTarget().getName().getString() : "";
            net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(sPlayer, new net.zootycoon.network.ZooNetworkPayloads.OpenSecurityScreenPayload(
                    this.getId(),
                    this.getUUID(),
                    name,
                    isAlert,
                    targetName,
                    (int) this.getHealth(),
                    (int) this.getMaxHealth()
            ));
            this.playSound(SoundEvents.VILLAGER_YES, 1.0F, 1.0F);
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    public void setSecurityName(String name) {
        this.entityData.set(SECURITY_NAME, name);
    }

    public String getSecurityName() {
        return this.entityData.get(SECURITY_NAME);
    }

    public void setOwnerUuid(UUID uuid) {
        this.ownerUuid = uuid;
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("SecurityName", getSecurityName());
        if (ownerUuid != null) {
            tag.putString("OwnerUuid", ownerUuid.toString());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        String name = tag.getString("SecurityName");
        if (!name.isEmpty()) {
            setSecurityName(name);
        }
        String oStr = tag.getString("OwnerUuid");
        if (!oStr.isEmpty()) {
            try {
                this.ownerUuid = UUID.fromString(oStr);
            } catch (Exception ignored) {}
        }
    }
}


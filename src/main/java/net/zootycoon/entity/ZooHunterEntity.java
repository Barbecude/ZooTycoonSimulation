package net.zootycoon.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.zootycoon.animal.ZooAnimalHungerManager;
import net.zootycoon.claim.ZooClaim;
import net.zootycoon.claim.ZooClaimManager;
import net.zootycoon.hunter.ZooHunterManager;
import net.zootycoon.network.ZooNetworkPayloads;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

public class ZooHunterEntity extends PathfinderMob {

    public enum HunterType {
        THIEF,
        ARCHER
    }

    private static final EntityDataAccessor<Integer> HUNTER_TYPE =
            SynchedEntityData.defineId(ZooHunterEntity.class, EntityDataSerializers.INT);

    private UUID partnerUuid = null;

    public ZooHunterEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_AXE));
        this.setDropChance(EquipmentSlot.MAINHAND, 0.05F);
        // No nameplate above hunter's head
        this.setCustomNameVisible(false);
        this.xpReward = 15;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(HUNTER_TYPE, HunterType.THIEF.ordinal());
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 24.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.33D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.FOLLOW_RANGE, 48.0D);
    }

    public HunterType getHunterType() {
        int ordinal = this.entityData.get(HUNTER_TYPE);
        HunterType[] values = HunterType.values();
        if (ordinal < 0 || ordinal >= values.length) return HunterType.THIEF;
        return values[ordinal];
    }

    public void setHunterType(HunterType type) {
        this.entityData.set(HUNTER_TYPE, type.ordinal());
        if (type == HunterType.ARCHER) {
            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
            this.setDropChance(EquipmentSlot.MAINHAND, 0.10F);
            this.xpReward = 20;
        } else {
            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_AXE));
            this.setDropChance(EquipmentSlot.MAINHAND, 0.05F);
            this.xpReward = 15;
        }
    }

    public boolean isArcher() {
        return getHunterType() == HunterType.ARCHER;
    }

    public boolean isThief() {
        return getHunterType() == HunterType.THIEF;
    }

    public boolean isHoldingBow() {
        return this.getMainHandItem().is(Items.BOW);
    }

    public void setPartnerUuid(UUID uuid) {
        this.partnerUuid = uuid;
    }

    public UUID getPartnerUuid() {
        return partnerUuid;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));

        // Priority 1: Kidnap registered zoo animals, break blocks / place temporary dirt if stuck, and carry them far outside the zoo!
        this.goalSelector.addGoal(1, new KidnapZooAnimalGoal(this));

        // Priority 1 for Archer: Shoot at Security, Zookeepers, and Players with bow
        this.goalSelector.addGoal(1, new HunterBowAttackGoal(this));

        // Priority 2 for Archer: Escort / follow thief partner when partner is moving or fleeing
        this.goalSelector.addGoal(2, new FollowThiefPartnerGoal(this));

        // Melee Attack Goal (fallback if thief, or if archer doesn't have bow)
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.25D, false) {
            @Override
            public boolean canUse() {
                if (ZooHunterEntity.this.isArcher() && ZooHunterEntity.this.isHoldingBow()) {
                    return false;
                }
                return !ZooHunterEntity.this.isVehicle() && super.canUse();
            }
        });

        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.65D));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        // Targeting goals:
        // 1. Retaliate against attackers and alert fellow hunters
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers(ZooHunterEntity.class));

        // 2. Target Security Guards (both thief & archer)
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, ZooSecurityEntity.class, true));

        // 3. Archer targets Zookeepers
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, ZooZookeeperEntity.class, true) {
            @Override
            public boolean canUse() {
                return ZooHunterEntity.this.isArcher() && super.canUse();
            }
        });

        // 4. Archer targets Players
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Player.class, true) {
            @Override
            public boolean canUse() {
                return ZooHunterEntity.this.isArcher() && super.canUse();
            }
        });
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        // Hunter must NEVER attack or kill zoo animals!
        if (ZooAnimalHungerManager.isZooAnimalCandidate(target)) {
            return false;
        }
        // Hunter must never attack other hunters!
        if (target instanceof ZooHunterEntity) {
            return false;
        }
        return super.canAttack(target);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (target instanceof LivingEntity living && ZooAnimalHungerManager.isZooAnimalCandidate(living)) {
            return false;
        }
        if (target instanceof ZooHunterEntity) {
            return false;
        }
        return super.doHurtTarget(target);
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity entity, EntityDimensions dimensions, float partialTick) {
        // Carry the kidnapped animal right above the hunter's head / raised hands
        return new Vec3(0.0D, dimensions.height() + 0.15D, 0.0D);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("HunterType", getHunterType().ordinal());
        if (partnerUuid != null) {
            tag.putUUID("PartnerUuid", partnerUuid);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("HunterType")) {
            setHunterType(HunterType.values()[Math.clamp(tag.getInt("HunterType"), 0, HunterType.values().length - 1)]);
        }
        if (tag.hasUUID("PartnerUuid")) {
            partnerUuid = tag.getUUID("PartnerUuid");
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide() && tickCount % 40 == 0 && level() instanceof ServerLevel sLevel) {
            long time = sLevel.getDayTime() % 24000L;
            boolean isDay = time >= 1000L && time <= 12000L;
            if (isDay && !isVehicle() && getTarget() == null) {
                sLevel.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + 0.8D, getZ(), 12, 0.25D, 0.4D, 0.25D, 0.05D);
                this.discard();
            }
        }
    }

    @Override
    public void die(DamageSource damageSource) {
        if (!this.getPassengers().isEmpty()) {
            this.ejectPassengers();
        }
        super.die(damageSource);
        if (level() instanceof ServerLevel sLevel) {
            this.spawnAtLocation(new ItemStack(Items.EMERALD, 1 + random.nextInt(3)));
            if (isArcher()) {
                this.spawnAtLocation(new ItemStack(Items.ARROW, 4 + random.nextInt(6)));
                if (random.nextFloat() < 0.15F) {
                    this.spawnAtLocation(new ItemStack(Items.BOW));
                }
            } else {
                this.spawnAtLocation(new ItemStack(Items.ARROW, 2 + random.nextInt(3)));
            }
            sLevel.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + 1.0, getZ(), 15, 0.3, 0.5, 0.3, 0.05);
            sLevel.playSound(null, blockPosition(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.HOSTILE, 1.0f, 1.0f);

            for (var p : sLevel.players()) {
                if (p.distanceToSqr(this) < 48 * 48) {
                    ZooNetworkPayloads.sendToast(p, "zootycoon.toast.title.security", "zootycoon.hunter.defeated", 1);
                }
            }
        }
    }

    /**
     * AI Goal for Archer:
     * Maintains bow range, charges bow, and shoots arrows at Security, Zookeeper, and Player targets.
     */
    public static class HunterBowAttackGoal extends Goal {
        private final ZooHunterEntity hunter;
        private int attackCooldown = 0;
        private int chargeTicks = 0;

        public HunterBowAttackGoal(ZooHunterEntity hunter) {
            this.hunter = hunter;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (!hunter.isArcher() || !hunter.isHoldingBow()) return false;
            LivingEntity target = hunter.getTarget();
            return target != null && target.isAlive() && hunter.canAttack(target);
        }

        @Override
        public boolean canContinueToUse() {
            return canUse() || (hunter.isUsingItem() && hunter.getTarget() != null && hunter.getTarget().isAlive());
        }

        @Override
        public void start() {
            super.start();
            hunter.setAggressive(true);
        }

        @Override
        public void stop() {
            super.stop();
            hunter.setAggressive(false);
            hunter.stopUsingItem();
            this.chargeTicks = 0;
            this.attackCooldown = 20;
        }

        @Override
        public void tick() {
            LivingEntity target = hunter.getTarget();
            if (target == null) return;

            hunter.getLookControl().setLookAt(target, 30.0F, 30.0F);
            double distSq = hunter.distanceToSqr(target);
            boolean canSee = hunter.getSensing().hasLineOfSight(target);

            // Positioning: keep distance
            if (distSq > 16.0D * 16.0D) {
                hunter.getNavigation().moveTo(target, 1.25D);
            } else if (distSq < 5.0D * 5.0D) {
                Vec3 away = hunter.position().subtract(target.position()).normalize();
                hunter.getNavigation().moveTo(hunter.getX() + away.x * 6.0D, hunter.getY(), hunter.getZ() + away.z * 6.0D, 1.3D);
            } else {
                hunter.getNavigation().stop();
            }

            if (attackCooldown > 0) {
                attackCooldown--;
                if (hunter.isUsingItem()) {
                    hunter.stopUsingItem();
                }
                return;
            }

            if (canSee) {
                if (!hunter.isUsingItem()) {
                    hunter.startUsingItem(InteractionHand.MAIN_HAND);
                    chargeTicks = 0;
                } else {
                    chargeTicks++;
                    if (chargeTicks >= 20) { // 1 second charge
                        shootArrowAt(target);
                        hunter.stopUsingItem();
                        chargeTicks = 0;
                        attackCooldown = 25 + hunter.getRandom().nextInt(15);
                    }
                }
            } else {
                hunter.stopUsingItem();
                chargeTicks = 0;
                hunter.getNavigation().moveTo(target, 1.25D);
            }
        }

        private void shootArrowAt(LivingEntity target) {
            Level level = hunter.level();
            if (!(level instanceof ServerLevel sLevel)) return;

            ItemStack bowStack = hunter.getMainHandItem();
            ItemStack arrowStack = new ItemStack(Items.ARROW);
            AbstractArrow arrow = ProjectileUtil.getMobArrow(hunter, arrowStack, 1.6F, bowStack);

            double dx = target.getX() - hunter.getX();
            double dy = target.getY(0.3333333333333333D) - arrow.getY();
            double dz = target.getZ() - hunter.getZ();
            double dist = Math.sqrt(dx * dx + dz * dz);

            arrow.shoot(dx, dy + dist * 0.18D, dz, 1.6F, 2.0F);
            sLevel.playSound(null, hunter.blockPosition(), SoundEvents.ARROW_SHOOT, SoundSource.HOSTILE, 1.0F, 1.0F / (hunter.getRandom().nextFloat() * 0.4F + 0.8F));
            sLevel.addFreshEntity(arrow);
        }
    }

    /**
     * AI Goal for Archer:
     * Escorts thief partner while the thief is running or stealing an animal.
     */
    public static class FollowThiefPartnerGoal extends Goal {
        private final ZooHunterEntity archer;
        private ZooHunterEntity partner;
        private int repathDelay = 0;

        public FollowThiefPartnerGoal(ZooHunterEntity archer) {
            this.archer = archer;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (!archer.isArcher()) return false;
            if (archer.getTarget() != null && archer.getTarget().isAlive()) return false;
            if (archer.getPartnerUuid() == null) return false;
            if (!(archer.level() instanceof ServerLevel sl)) return false;

            Entity p = sl.getEntity(archer.getPartnerUuid());
            if (p instanceof ZooHunterEntity h && h.isAlive() && h.isThief()) {
                this.partner = h;
                return archer.distanceToSqr(partner) > 7.0D * 7.0D;
            }
            return false;
        }

        @Override
        public void tick() {
            if (partner == null || !partner.isAlive()) return;
            archer.getLookControl().setLookAt(partner, 20.0F, 20.0F);

            if (--repathDelay <= 0) {
                repathDelay = 15;
                archer.getNavigation().moveTo(partner, 1.35D);
            }
        }

        @Override
        public boolean canContinueToUse() {
            if (archer.getTarget() != null && archer.getTarget().isAlive()) return false;
            return partner != null && partner.isAlive() && archer.distanceToSqr(partner) > 5.0D * 5.0D;
        }
    }

    /**
     * AI Goal for Thief:
     * 1. Finds a registered zoo animal.
     * 2. Approaches the animal; if stuck/blocked, breaks obstructing blocks or places temporary 10-second dirt under feet to climb.
     * 3. Lifts the animal overhead (startRiding) and runs VERY FAR away outside the zoo area (even after leaving zoo boundaries).
     */
    public static class KidnapZooAnimalGoal extends Goal {
        private final ZooHunterEntity hunter;
        private LivingEntity targetAnimal;
        private BlockPos pickupOrigin;
        private BlockPos escapeTarget;
        private Vec3 lastCheckPos;
        private int stuckTicks = 0;
        private BlockPos breakingPos = null;
        private int breakProgressTicks = 0;

        public KidnapZooAnimalGoal(ZooHunterEntity hunter) {
            this.hunter = hunter;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (!hunter.isThief()) return false;
            if (!(hunter.level() instanceof ServerLevel sLevel)) return false;

            if (hunter.isVehicle() && !hunter.getPassengers().isEmpty() && hunter.getFirstPassenger() instanceof LivingEntity carried) {
                this.targetAnimal = carried;
                return true;
            }

            if (hunter.tickCount % 15 != 0) return false;

            List<LivingEntity> candidates = sLevel.getEntitiesOfClass(
                    LivingEntity.class,
                    hunter.getBoundingBox().inflate(64.0D, 24.0D, 64.0D),
                    e -> e.isAlive() && !e.isPassenger() && ZooAnimalHungerManager.isTagged(e) && ZooAnimalHungerManager.isZooAnimalCandidate(e)
            );

            if (candidates.isEmpty()) return false;

            LivingEntity closest = null;
            double minDist = Double.MAX_VALUE;
            for (LivingEntity c : candidates) {
                double d = hunter.distanceToSqr(c);
                if (d < minDist) {
                    minDist = d;
                    closest = c;
                }
            }

            this.targetAnimal = closest;
            return this.targetAnimal != null;
        }

        @Override
        public boolean canContinueToUse() {
            if (!hunter.isThief()) return false;
            if (hunter.isVehicle() && !hunter.getPassengers().isEmpty()) {
                return true;
            }
            return targetAnimal != null && targetAnimal.isAlive() && !targetAnimal.isPassenger() && hunter.distanceToSqr(targetAnimal) <= 80.0D * 80.0D;
        }

        @Override
        public void start() {
            this.lastCheckPos = hunter.position();
            this.stuckTicks = 0;
            this.breakingPos = null;
            this.breakProgressTicks = 0;
        }

        @Override
        public void stop() {
            this.breakingPos = null;
            this.breakProgressTicks = 0;
            this.stuckTicks = 0;
        }

        @Override
        public void tick() {
            if (!(hunter.level() instanceof ServerLevel sLevel)) return;

            // PHASE 2: Carrying the kidnapped animal VERY FAR outside the zoo!
            if (hunter.isVehicle() && !hunter.getPassengers().isEmpty()) {
                Entity passenger = hunter.getFirstPassenger();
                if (!(passenger instanceof LivingEntity carried) || !carried.isAlive()) {
                    hunter.ejectPassengers();
                    return;
                }

                if (pickupOrigin == null) {
                    pickupOrigin = hunter.blockPosition();
                }
                if (escapeTarget == null) {
                    escapeTarget = computeEscapeTarget(sLevel, pickupOrigin);
                }

                // If hunter reached or is close to current waypoint, advance escape target even further out!
                if (escapeTarget != null && hunter.distanceToSqr(escapeTarget.getX() + 0.5, hunter.getY(), escapeTarget.getZ() + 0.5) < 25.0D) {
                    escapeTarget = computeEscapeTarget(sLevel, hunter.blockPosition());
                }

                hunter.getNavigation().moveTo(escapeTarget.getX() + 0.5, escapeTarget.getY(), escapeTarget.getZ() + 0.5, 1.45D);

                // Handle obstacles while escaping
                handleStuckAndObstacles(sLevel, Vec3.atCenterOf(escapeTarget));

                double distFromOriginSqr = hunter.distanceToSqr(pickupOrigin.getX() + 0.5, pickupOrigin.getY(), pickupOrigin.getZ() + 0.5);
                boolean outsideClaims = true;
                for (ZooClaim claim : ZooClaimManager.get(sLevel).getAllClaims()) {
                    if (claim.contains(hunter.blockPosition().getX(), hunter.blockPosition().getZ())) {
                        outsideClaims = false;
                        break;
                    }
                }

                boolean playerChasing = sLevel.hasNearbyAlivePlayer(hunter.getX(), hunter.getY(), hunter.getZ(), 40.0D);

                // Escapes far away into the wilderness: at least 180 blocks away and outside claims, with no player actively chasing
                // or if already pushed 260+ blocks deep into the wild
                if (outsideClaims && ((distFromOriginSqr >= 180.0 * 180.0 && !playerChasing) || distFromOriginSqr >= 260.0 * 260.0)) {
                    String animalName = carried.hasCustomName() ? carried.getCustomName().getString() : carried.getType().getDescription().getString();
                    UUID ownerUuid = ZooAnimalHungerManager.getOwnerUuid(carried);
                    if (ownerUuid != null) {
                        ServerPlayer owner = sLevel.getServer().getPlayerList().getPlayer(ownerUuid);
                        if (owner != null) {
                            ZooNetworkPayloads.sendToast(owner, "zootycoon.toast.title.security", "zootycoon.hunter.escaped_with_animal", 3, animalName);
                            ZooNetworkPayloads.sendAnimalSync(owner);
                        }
                    }
                    sLevel.sendParticles(ParticleTypes.POOF, hunter.getX(), hunter.getY() + 1.0, hunter.getZ(), 20, 0.4, 0.5, 0.4, 0.05);
                    carried.discard();
                    hunter.discard();

                    // If archer partner is also fleeing nearby, discard partner as well
                    if (hunter.getPartnerUuid() != null) {
                        Entity p = sLevel.getEntity(hunter.getPartnerUuid());
                        if (p instanceof ZooHunterEntity ph && ph.isAlive() && ph.distanceToSqr(hunter) < 48.0 * 48.0) {
                            sLevel.sendParticles(ParticleTypes.POOF, ph.getX(), ph.getY() + 1.0, ph.getZ(), 15, 0.4, 0.5, 0.4, 0.05);
                            ph.discard();
                        }
                    }
                }
                return;
            }

            // PHASE 1: Approaching target animal to kidnap it
            if (targetAnimal == null || !targetAnimal.isAlive()) return;

            hunter.getLookControl().setLookAt(targetAnimal, 30.0F, 30.0F);
            double distSqr = hunter.distanceToSqr(targetAnimal);

            if (distSqr <= 6.25D) { // Within 2.5 blocks -> Pick up animal!
                this.pickupOrigin = hunter.blockPosition();
                this.escapeTarget = computeEscapeTarget(sLevel, this.pickupOrigin);
                hunter.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                targetAnimal.startRiding(hunter, true);
                sLevel.playSound(null, hunter.blockPosition(), SoundEvents.ARMOR_EQUIP_LEATHER.value(), SoundSource.HOSTILE, 1.0F, 0.9F);
                sLevel.sendParticles(ParticleTypes.ANGRY_VILLAGER, hunter.getX(), hunter.getY() + 1.5, hunter.getZ(), 8, 0.3, 0.3, 0.3, 0.02);

                String animalName = targetAnimal.hasCustomName() ? targetAnimal.getCustomName().getString() : targetAnimal.getType().getDescription().getString();
                UUID ownerUuid = ZooAnimalHungerManager.getOwnerUuid(targetAnimal);
                if (ownerUuid != null) {
                    ServerPlayer owner = sLevel.getServer().getPlayerList().getPlayer(ownerUuid);
                    if (owner != null) {
                        ZooNetworkPayloads.sendToast(owner, "zootycoon.toast.title.security", "zootycoon.hunter.kidnapping", 3, animalName);
                    }
                }
                return;
            }

            hunter.getNavigation().moveTo(targetAnimal, 1.25D);
            handleStuckAndObstacles(sLevel, targetAnimal.position());
        }

        private void handleStuckAndObstacles(ServerLevel sLevel, Vec3 goalVec) {
            Vec3 currentPos = hunter.position();
            if (lastCheckPos != null) {
                double movedHorizSqr = (currentPos.x - lastCheckPos.x) * (currentPos.x - lastCheckPos.x)
                        + (currentPos.z - lastCheckPos.z) * (currentPos.z - lastCheckPos.z);
                if (movedHorizSqr < 0.04D) {
                    stuckTicks++;
                } else {
                    stuckTicks = Math.max(0, stuckTicks - 2);
                }
            }
            lastCheckPos = currentPos;

            Vec3 dir = goalVec.subtract(currentPos);
            Vec3 horizDir = new Vec3(dir.x, 0, dir.z);
            if (horizDir.lengthSqr() > 1.0E-4D) {
                horizDir = horizDir.normalize();
            } else {
                horizDir = hunter.getLookAngle();
            }

            BlockPos frontFoot = BlockPos.containing(currentPos.x + horizDir.x * 1.1, currentPos.y, currentPos.z + horizDir.z * 1.1);
            BlockPos frontHead = frontFoot.above();

            // 1. Check if there is a breakable barrier in front (fence, gate, door, wall, glass, leaves, or solid block when stuck)
            BlockPos candidateBreak = null;
            if (isBreakableObstacle(sLevel, frontHead)) {
                candidateBreak = frontHead;
            } else if (isBreakableObstacle(sLevel, frontFoot)) {
                candidateBreak = frontFoot;
            }

            if (candidateBreak != null && (stuckTicks >= 8 || isEnclosureBarrier(sLevel.getBlockState(candidateBreak)))) {
                if (!candidateBreak.equals(breakingPos)) {
                    breakingPos = candidateBreak;
                    breakProgressTicks = 0;
                }
                breakProgressTicks++;
                hunter.swing(InteractionHand.MAIN_HAND);
                if (breakProgressTicks % 5 == 0) {
                    BlockState bs = sLevel.getBlockState(breakingPos);
                    sLevel.playSound(null, breakingPos, bs.getSoundType().getHitSound(), SoundSource.HOSTILE, 0.8F, 1.0F);
                    sLevel.sendParticles(ParticleTypes.CRIT, breakingPos.getX() + 0.5, breakingPos.getY() + 0.5, breakingPos.getZ() + 0.5, 4, 0.2, 0.2, 0.2, 0.02);
                }
                if (breakProgressTicks >= 20) {
                    BlockState bs = sLevel.getBlockState(breakingPos);
                    sLevel.levelEvent(2001, breakingPos, Block.getId(bs));
                    sLevel.destroyBlock(breakingPos, true, hunter);
                    breakingPos = null;
                    breakProgressTicks = 0;
                    stuckTicks = 0;
                }
                return;
            }

            // 2. If stuck, place temporary dirt under feet (or bridge ahead) that disappears after 10 seconds!
            if (stuckTicks >= 16) {
                BlockPos frontBelow = frontFoot.below();
                if (sLevel.getBlockState(frontBelow).isAir() || sLevel.getBlockState(frontBelow).canBeReplaced()) {
                    ZooHunterManager.placeTempDirt(sLevel, frontBelow);
                    stuckTicks = 0;
                } else {
                    BlockPos feetPos = hunter.blockPosition();
                    BlockPos headAbove = feetPos.above(2);
                    if (sLevel.getBlockState(headAbove).isAir() || sLevel.getBlockState(headAbove).canBeReplaced()) {
                        hunter.setPos(hunter.getX(), hunter.getY() + 1.05D, hunter.getZ());
                        ZooHunterManager.placeTempDirt(sLevel, feetPos);
                        hunter.setDeltaMovement(horizDir.x * 0.25D, 0.1D, horizDir.z * 0.25D);
                    }
                    stuckTicks = 0;
                }
            }
        }

        private boolean isEnclosureBarrier(BlockState state) {
            Block b = state.getBlock();
            return b instanceof FenceBlock
                    || b instanceof FenceGateBlock
                    || b instanceof DoorBlock
                    || b instanceof WallBlock
                    || b instanceof LeavesBlock;
        }

        private boolean isBreakableObstacle(ServerLevel level, BlockPos pos) {
            BlockState state = level.getBlockState(pos);
            if (state.isAir() || !state.getFluidState().isEmpty()) return false;
            if (isEnclosureBarrier(state)) return true;
            float speed = state.getDestroySpeed(level, pos);
            return speed >= 0.0F && speed <= 4.0F && state.isSolidRender(level, pos);
        }

        private BlockPos computeEscapeTarget(ServerLevel sLevel, BlockPos origin) {
            double angle = hunter.getRandom().nextDouble() * Math.PI * 2.0D;
            // Try to pick a direction pointing away from the nearest zoo claim center
            for (ZooClaim claim : ZooClaimManager.get(sLevel).getAllClaims()) {
                if (claim.contains(origin.getX(), origin.getZ())) {
                    var box = claim.getBox();
                    double cx = (box.minX() + box.maxX()) * 0.5D;
                    double cz = (box.minZ() + box.maxZ()) * 0.5D;
                    double dx = origin.getX() - cx;
                    double dz = origin.getZ() - cz;
                    if (dx * dx + dz * dz > 1.0D) {
                        angle = Math.atan2(dz, dx);
                    }
                    break;
                }
            }
            // Go far out: 200 blocks away!
            int tx = origin.getX() + (int) Math.round(Math.cos(angle) * 200.0D);
            int tz = origin.getZ() + (int) Math.round(Math.sin(angle) * 200.0D);
            int ty = sLevel.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, tx, tz);
            return new BlockPos(tx, ty, tz);
        }
    }
}

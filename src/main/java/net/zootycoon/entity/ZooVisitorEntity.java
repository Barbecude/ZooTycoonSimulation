package net.zootycoon.entity;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.nbt.CompoundTag;
import net.zootycoon.ZooTycoonMod;
import net.zootycoon.animal.ZooAnimalHungerManager;
import net.zootycoon.block.ToiletBlockEntity;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

public class ZooVisitorEntity extends PathfinderMob {

    // Variants: 0..22 (corresponds to visitor_0.png .. visitor_22.png)
    private static final EntityDataAccessor<Integer> VARIANT =
            SynchedEntityData.defineId(ZooVisitorEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Boolean> IS_CHILD =
            SynchedEntityData.defineId(ZooVisitorEntity.class, EntityDataSerializers.BOOLEAN);

    // Needs (0..100)
    private static final EntityDataAccessor<Integer> HUNGER =
            SynchedEntityData.defineId(ZooVisitorEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Integer> THIRST =
            SynchedEntityData.defineId(ZooVisitorEntity.class, EntityDataSerializers.INT);

    // Mood
    private static final EntityDataAccessor<Integer> MOOD =
            SynchedEntityData.defineId(ZooVisitorEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Boolean> HAS_TRASH =
            SynchedEntityData.defineId(ZooVisitorEntity.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<Boolean> NEEDS_TOILET =
            SynchedEntityData.defineId(ZooVisitorEntity.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<Boolean> IS_CONSUMING =
            SynchedEntityData.defineId(ZooVisitorEntity.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<Boolean> IS_DRINKING =
            SynchedEntityData.defineId(ZooVisitorEntity.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<Boolean> IS_LEAVING =
            SynchedEntityData.defineId(ZooVisitorEntity.class, EntityDataSerializers.BOOLEAN);

    public static final int MAX_VARIANTS = 23;
    private static final int MAX_STAY = 9600; // 8 minutes (9600 ticks)

    public enum VisitorMood {
        NEUTRAL("zootycoon.visitor.mood.neutral", 0xFFFFFF),
        TOILET("zootycoon.visitor.mood.toilet", 0xFFA500),
        AMAZED("zootycoon.visitor.mood.amazed", 0x00FFFF),
        ADORED("zootycoon.visitor.mood.adored", 0xFF69B4),
        HUNGRY("zootycoon.visitor.mood.hungry", 0xFF0000),
        THIRSTY("zootycoon.visitor.mood.thirsty", 0x0000FF);

        public final String labelKey;
        public final int color;

        VisitorMood(String labelKey, int color) {
            this.labelKey = labelKey;
            this.color = color;
        }

        public String getLabel() {
            return net.zootycoon.util.ZooTranslations.get(this.labelKey);
        }

        public static VisitorMood fromOrdinal(int ordinal) {
            VisitorMood[] values = values();
            if (ordinal < 0 || ordinal >= values.length) {
                return NEUTRAL;
            }
            return values[ordinal];
        }
    }

    private static final net.minecraft.resources.ResourceLocation SPRINT_MODIFIER_ID =
            ZooTycoonMod.id("visitor_sprint_boost");
    private static final net.minecraft.world.entity.ai.attributes.AttributeModifier SPRINT_MODIFIER =
            new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                    SPRINT_MODIFIER_ID, 0.40D, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_BASE);

    private BlockPos gatePos = null;
    private UUID guidingPlayerUuid = null;
    private UUID guidedPlayerUuid = null;
    private int guideTimeout = 0;
    private int stayTime = 0;
    private int toiletTimer = 0;
    private boolean hasUsedToilet = false;
    private boolean wantsSouvenir = false;
    private int watchingAnimalId = -1;
    // 0 = small/baby, 1 = normal, 2 = big
    private int watchingAnimalSize = -1;
    private int eatTimer = 0;
    private int runBurstTimer = 0;
    private int runBurstType = 0; // 0 = none, 1 = run only, 2 = run & jump
    private int jumpCooldown = 0;
    private int sitCooldown = 0;

    // Consumer preference: 0 = FOOD_ONLY (max 3 meals, never drinks), 1 = DRINK_ONLY (max 3 drinks, never eats)
    private int consumerType = 0;
    private int foodCount = 0;
    private int drinkCount = 0;
    private int cashierCooldown = 0;
    private BlockPos escapeTarget = null;
    private int escapeTicks = 0;

    public ZooVisitorEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.consumerType = Math.random() < 0.5 ? 0 : 1;
        if (isChildVisitor()) {
            this.refreshDimensions();
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(VARIANT, (int) (Math.random() * MAX_VARIANTS));
        builder.define(IS_CHILD, Math.random() < 0.20); // 20% child chance
        builder.define(HUNGER, 0);
        builder.define(THIRST, 0);
        builder.define(MOOD, VisitorMood.NEUTRAL.ordinal());
        builder.define(HAS_TRASH, false);
        builder.define(NEEDS_TOILET, Math.random() < 0.02); // 2% arrive already needing restroom
        builder.define(IS_CONSUMING, false);
        builder.define(IS_DRINKING, false);
        builder.define(IS_LEAVING, false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.FOLLOW_RANGE, 64.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new RunAwayFromCashierGoal(this));
        this.goalSelector.addGoal(1, new FollowGuidePlayerGoal(this));
        this.goalSelector.addGoal(1, new LeaveZooGoal(this));
        this.goalSelector.addGoal(1, new BuyTicketAtEntranceGoal(this));
        this.goalSelector.addGoal(2, new FindToiletGoal(this));
        this.goalSelector.addGoal(2, new QueueAtCashierGoal(this));
        this.goalSelector.addGoal(2, new FindTrashCanGoal(this));
        this.goalSelector.addGoal(3, new OpenDoorGoal(this, true));
        this.goalSelector.addGoal(4, new FindSeatGoal(this));
        this.goalSelector.addGoal(5, new WatchAnimalsGoal(this));
        this.goalSelector.addGoal(6, new VisitorZooStrollGoal(this, 0.7D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    public VisitorMood getMood() {
        return VisitorMood.fromOrdinal(this.entityData.get(MOOD));
    }

    public void setMood(VisitorMood mood) {
        this.entityData.set(MOOD, mood.ordinal());
    }

    public int getVariant() {
        return this.entityData.get(VARIANT);
    }

    public void setVariant(int variant) {
        this.entityData.set(VARIANT, Math.max(0, Math.min(MAX_VARIANTS - 1, variant)));
    }

    public boolean isChildVisitor() {
        return this.entityData.get(IS_CHILD);
    }

    public void setChildVisitor(boolean child) {
        this.entityData.set(IS_CHILD, child);
        this.refreshDimensions();
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (IS_CHILD.equals(key)) {
            this.refreshDimensions();
        }
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        EntityDimensions dimensions = super.getDefaultDimensions(pose);
        if (isChildVisitor()) {
            return dimensions.scale(0.6F);
        }
        return dimensions;
    }

    @Override
    public Vec3 getVehicleAttachmentPoint(Entity vehicle) {
        if (isChildVisitor()) {
            return new Vec3(0.0, Player.DEFAULT_VEHICLE_ATTACHMENT.y * 0.6D, 0.0);
        }
        return Player.DEFAULT_VEHICLE_ATTACHMENT;
    }

    public int getHunger() {
        if (consumerType != 0 || foodCount >= 3) {
            return 0;
        }
        return this.entityData.get(HUNGER);
    }

    public void setHunger(int hunger) {
        if (consumerType != 0 || foodCount >= 3) {
            this.entityData.set(HUNGER, 0);
            return;
        }
        this.entityData.set(HUNGER, Math.clamp(hunger, 0, 100));
    }

    public int getThirst() {
        if (consumerType != 1 || drinkCount >= 3) {
            return 0;
        }
        return this.entityData.get(THIRST);
    }

    public void setThirst(int thirst) {
        if (consumerType != 1 || drinkCount >= 3) {
            this.entityData.set(THIRST, 0);
            return;
        }
        this.entityData.set(THIRST, Math.clamp(thirst, 0, 100));
    }

    public boolean canEat() {
        return consumerType == 0 && foodCount < 3;
    }

    public boolean canDrink() {
        return consumerType == 1 && drinkCount < 3;
    }

    public int getConsumerType() {
        return consumerType;
    }

    public void setConsumerType(int type) {
        this.consumerType = type;
    }

    public int getFoodCount() {
        return foodCount;
    }

    public void setFoodCount(int count) {
        this.foodCount = count;
    }

    public int getDrinkCount() {
        return drinkCount;
    }

    public void setDrinkCount(int count) {
        this.drinkCount = count;
    }

    public boolean hasCashierCooldown() {
        return cashierCooldown > 0;
    }

    public int getCashierCooldown() {
        return cashierCooldown;
    }

    public void setCashierCooldown(int ticks) {
        this.cashierCooldown = ticks;
    }

    public BlockPos getEscapeTarget() {
        return escapeTarget;
    }

    public int getEscapeTicks() {
        return escapeTicks;
    }

    public void clearEscape() {
        this.escapeTarget = null;
        this.escapeTicks = 0;
    }

    public boolean hasTrash() {
        return this.entityData.get(HAS_TRASH);
    }

    public void setHasTrash(boolean trash) {
        this.entityData.set(HAS_TRASH, trash);
    }

    public boolean needsToilet() {
        return !hasUsedToilet && this.entityData.get(NEEDS_TOILET);
    }

    public void setNeedsToilet(boolean needsToilet) {
        if (needsToilet && this.hasUsedToilet) {
            this.entityData.set(NEEDS_TOILET, false);
            return;
        }
        this.entityData.set(NEEDS_TOILET, needsToilet);
    }

    public boolean hasUsedToilet() {
        return this.hasUsedToilet;
    }

    public void markToiletUsed() {
        this.hasUsedToilet = true;
        this.toiletTimer = 0;
        this.entityData.set(NEEDS_TOILET, false);
        updateMood();
    }

    public boolean wantsSouvenir() {
        return wantsSouvenir && this.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD).isEmpty();
    }

    public void setWantsSouvenir(boolean wants) {
        this.wantsSouvenir = wants;
    }

    public void setGatePos(BlockPos pos) {
        this.gatePos = pos;
    }

    public BlockPos getGatePos() {
        return gatePos;
    }

    public static boolean isLateNight(Level level) {
        if (level != null) {
            long dayTime = level.getDayTime() % 24000L;
            return dayTime >= 16500L && dayTime <= 23000L;
        }
        return false;
    }

    public boolean isTimeToLeave() {
        return stayTime > MAX_STAY || isLateNight(this.level());
    }

    public BlockPos resolveLeaveTarget() {
        if (this.gatePos != null) {
            return this.gatePos;
        }
        net.zootycoon.claim.ZooClaim claim = findActiveZooClaim();
        if (claim != null) {
            var box = claim.getBox();
            BlockPos cur = this.blockPosition();
            int dMinX = Math.abs(cur.getX() - box.minX());
            int dMaxX = Math.abs(cur.getX() - box.maxX());
            int dMinZ = Math.abs(cur.getZ() - box.minZ());
            int dMaxZ = Math.abs(cur.getZ() - box.maxZ());

            int minD = Math.min(Math.min(dMinX, dMaxX), Math.min(dMinZ, dMaxZ));
            int tx = cur.getX();
            int tz = cur.getZ();

            if (minD == dMinX) tx = box.minX() - 4;
            else if (minD == dMaxX) tx = box.maxX() + 4;
            else if (minD == dMinZ) tz = box.minZ() - 4;
            else tz = box.maxZ() + 4;

            int ty = cur.getY();
            if (this.level() instanceof net.minecraft.server.level.ServerLevel sl) {
                int surf = sl.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, tx, tz);
                if (Math.abs(surf - cur.getY()) <= 8) {
                    ty = surf;
                }
            }
            this.gatePos = new BlockPos(tx, ty, tz);
            return this.gatePos;
        }
        return null;
    }

    public boolean isLeaving() {
        return this.entityData.get(IS_LEAVING);
    }

    public net.zootycoon.claim.ZooClaim findActiveZooClaim() {
        if (!(this.level() instanceof net.minecraft.server.level.ServerLevel sLevel)) {
            return null;
        }
        BlockPos pos = this.blockPosition();
        net.zootycoon.claim.ZooClaim nearest = null;
        double bestDist = Double.MAX_VALUE;
        for (net.zootycoon.claim.ZooClaim claim : net.zootycoon.claim.ZooClaimManager.get(sLevel).getAllClaims()) {
            if (claim.contains(pos.getX(), pos.getZ())) {
                return claim;
            }
            var box = claim.getBox();
            int cx = Math.clamp(pos.getX(), box.minX(), box.maxX());
            int cz = Math.clamp(pos.getZ(), box.minZ(), box.maxZ());
            double d = (pos.getX() - cx) * (pos.getX() - cx) + (pos.getZ() - cz) * (pos.getZ() - cz);
            if (d < bestDist) {
                bestDist = d;
                nearest = claim;
            }
        }
        return nearest;
    }

    public net.minecraft.world.phys.AABB getFullZooSearchBox() {
        if (!(this.level() instanceof net.minecraft.server.level.ServerLevel sLevel)) {
            return this.getBoundingBox().inflate(64.0D, 16.0D, 64.0D);
        }
        net.zootycoon.claim.ZooClaim active = findActiveZooClaim();
        if (active == null) {
            return this.getBoundingBox().inflate(64.0D, 16.0D, 64.0D);
        }
        int minX = this.blockPosition().getX() - 16;
        int maxX = this.blockPosition().getX() + 16;
        int minZ = this.blockPosition().getZ() - 16;
        int maxZ = this.blockPosition().getZ() + 16;
        UUID owner = active.getOwnerUuid();
        for (net.zootycoon.claim.ZooClaim c : net.zootycoon.claim.ZooClaimManager.get(sLevel).getAllClaims()) {
            if (owner == null || owner.equals(c.getOwnerUuid())) {
                var b = c.getBox();
                minX = Math.min(minX, b.minX() - 16);
                maxX = Math.max(maxX, b.maxX() + 16);
                minZ = Math.min(minZ, b.minZ() - 16);
                maxZ = Math.max(maxZ, b.maxZ() + 16);
            }
        }
        return new net.minecraft.world.phys.AABB(
                minX, this.level().getMinBuildHeight(), minZ,
                maxX + 1, this.level().getMaxBuildHeight(), maxZ + 1
        );
    }

    public void moveTowardLongRange(BlockPos target, double speed) {
        if (target == null) return;
        double dx = (target.getX() + 0.5D) - this.getX();
        double dz = (target.getZ() + 0.5D) - this.getZ();
        double horizDistSq = dx * dx + dz * dz;
        if (horizDistSq <= 22.0D * 22.0D) {
            this.getNavigation().moveTo(target.getX() + 0.5D, target.getY(), target.getZ() + 0.5D, speed);
            return;
        }
        double dist = Math.sqrt(horizDistSq);
        double step = 18.0D;
        int wpX = net.minecraft.util.Mth.floor(this.getX() + (dx / dist) * step);
        int wpZ = net.minecraft.util.Mth.floor(this.getZ() + (dz / dist) * step);
        int wpY = target.getY();
        if (this.level() instanceof net.minecraft.server.level.ServerLevel sLevel) {
            int surfY = sLevel.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, wpX, wpZ);
            if (Math.abs(surfY - this.blockPosition().getY()) <= 8) {
                wpY = surfY;
            } else {
                wpY = this.blockPosition().getY();
            }
        }
        this.getNavigation().moveTo(wpX + 0.5D, wpY, wpZ + 0.5D, speed);
    }

    public void walkIntoZooInterior() {
        if (!(this.level() instanceof net.minecraft.server.level.ServerLevel sLevel)) return;
        net.zootycoon.claim.ZooClaim claim = findActiveZooClaim();
        if (claim == null) return;
        var box = claim.getBox();
        BlockPos cur = this.blockPosition();
        int marginX = Math.min(4, Math.max(1, (box.maxX() - box.minX()) / 4));
        int marginZ = Math.min(4, Math.max(1, (box.maxZ() - box.minZ()) / 4));
        int tx = Math.clamp(cur.getX(), box.minX() + marginX, box.maxX() - marginX);
        int tz = Math.clamp(cur.getZ(), box.minZ() + marginZ, box.maxZ() - marginZ);
        int ty = sLevel.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, tx, tz);
        if (Math.abs(ty - cur.getY()) > 8) {
            ty = cur.getY();
        }
        moveTowardLongRange(new BlockPos(tx, ty, tz), 1.05D);
    }

    /**
     * Authoritative mood updater based on priority:
     * Leaving (NEUTRAL) > TOILET > HUNGRY (>=50) > THIRSTY (>=50) > Animal Watching (AMAZED / ADORED / HAPPY) > NEUTRAL
     */
    /**
     * Authoritative mood updater based on priority:
     * Leaving (NEUTRAL) > TOILET > HUNGRY (>=50) > THIRSTY (>=50) > Animal Watching (AMAZED / ADORED / HAPPY) > NEUTRAL
     */
    public void updateMood() {
        if (isTimeToLeave() || isLeaving()) {
            setMood(VisitorMood.NEUTRAL);
            return;
        }
        if (needsToilet()) {
            setMood(VisitorMood.TOILET);
            return;
        }
        if (canEat() && getHunger() >= 50) {
            setMood(VisitorMood.HUNGRY);
            return;
        }
        if (canDrink() && getThirst() >= 50) {
            setMood(VisitorMood.THIRSTY);
            return;
        }
        if (watchingAnimalId != -1) {
            if (watchingAnimalSize == 2) {
                setMood(VisitorMood.AMAZED);
            } else if (watchingAnimalSize == 0) {
                setMood(VisitorMood.ADORED);
            } else {
                setMood(VisitorMood.NEUTRAL);
            }
            return;
        }
        setMood(VisitorMood.NEUTRAL);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide()) {
            stayTime++;
            if (cashierCooldown > 0) cashierCooldown--;
            if (escapeTicks > 0) escapeTicks--;

            boolean leavingNow = isTimeToLeave();
            if (this.entityData.get(IS_LEAVING) != leavingNow) {
                this.entityData.set(IS_LEAVING, leavingNow);
            }

            // Enforce strict zoo claim boundary for ticketed visitors who are not leaving
            if (!leavingNow && this.getTags().contains("ticket_purchased") && this.tickCount % 25 == 0) {
                if (this.level() instanceof net.minecraft.server.level.ServerLevel sLevel) {
                    if (!net.zootycoon.claim.ZooClaimManager.get(sLevel).isInsideAnyClaim(this.blockPosition())) {
                        walkIntoZooInterior();
                    }
                }
            }

            // Every 10 seconds (200 ticks): hunger and thirst build up according to exclusive diet type and 3x quota
            if (this.tickCount % 200 == 0 && !leavingNow) {
                if (canEat()) {
                    setHunger(getHunger() + 8);
                }
                if (canDrink()) {
                    setThirst(getThirst() + 10);
                }
            }

            // Every 20 seconds (400 ticks): 3% chance to need toilet (max 1 toilet quota per visitor)
            if (this.tickCount % 400 == 0 && !needsToilet() && !hasUsedToilet && !leavingNow) {
                if (this.random.nextFloat() < 0.03F) {
                    setNeedsToilet(true);
                }
            }

            // Every 15 seconds (300 ticks): chance to want a souvenir plushie if not wearing one
            if (this.tickCount % 300 == 0 && !leavingNow && this.getTags().contains("ticket_purchased")) {
                if (this.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD).isEmpty() && !wantsSouvenir) {
                    if (this.random.nextFloat() < 0.22F) {
                        this.wantsSouvenir = true;
                    }
                } else if (!this.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD).isEmpty()) {
                    this.wantsSouvenir = false;
                }
            }

            // Toilet timer check: if toilet need isn't met for 90s (1800 ticks), visitor leaves
            if (needsToilet()) {
                toiletTimer++;
                if (toiletTimer > 1800) {
                    forceLeave();
                }
            } else {
                toiletTimer = 0;
            }

            // Occasional trash generation
            if (this.random.nextFloat() < 0.0003F && !hasTrash() && !leavingNow) {
                setHasTrash(true);
            }

            // Authentic player-like eating / drinking animation & particle tick
            if (eatTimer > 0) {
                eatTimer--;
                if (escapeTicks <= 0) {
                    this.getNavigation().stop();
                }
                net.minecraft.world.item.ItemStack held = this.getMainHandItem();
                if (!held.isEmpty() && this.level() instanceof net.minecraft.server.level.ServerLevel sLevel) {
                    if (eatTimer % 4 == 0) {
                        float pitch = 0.9F + this.random.nextFloat() * 0.2F;
                        if (isDrinking()) {
                            sLevel.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GENERIC_DRINK, this.getSoundSource(), 0.85F, pitch);
                        } else {
                            sLevel.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GENERIC_EAT, this.getSoundSource(), 0.85F, pitch);
                        }
                        Vec3 look = this.getLookAngle();
                        double px = this.getX() + look.x * 0.35D;
                        double py = this.getEyeY() - 0.15D;
                        double pz = this.getZ() + look.z * 0.35D;
                        sLevel.sendParticles(
                                new net.minecraft.core.particles.ItemParticleOption(net.minecraft.core.particles.ParticleTypes.ITEM, held),
                                px, py, pz,
                                5, 0.08D, 0.08D, 0.08D, 0.03D
                        );
                    }
                }
                if (eatTimer == 0) {
                    if (!isDrinking()) {
                        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_BURP, this.getSoundSource(), 0.7F, 0.9F + this.random.nextFloat() * 0.2F);
                    }
                    this.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, net.minecraft.world.item.ItemStack.EMPTY);
                    this.setItemInHand(InteractionHand.OFF_HAND, net.minecraft.world.item.ItemStack.EMPTY);
                    this.entityData.set(IS_CONSUMING, false);
                    this.entityData.set(IS_DRINKING, false);
                }
            }

            // Guide timeout tick
            if (guidedPlayerUuid != null) {
                if (guideTimeout > 0) {
                    guideTimeout--;
                } else {
                    guidedPlayerUuid = null;
                }
            }

            // Update mood every 5 ticks
            if (this.tickCount % 5 == 0) {
                updateMood();
            }

            // Natural running and jumping behavior
            if (this.isPassenger() || needsToilet()) {
                if (runBurstTimer > 0) {
                    runBurstTimer = 0;
                    runBurstType = 0;
                    this.setSprinting(false);
                    var attr = this.getAttribute(Attributes.MOVEMENT_SPEED);
                    if (attr != null && attr.hasModifier(SPRINT_MODIFIER_ID)) {
                        attr.removeModifier(SPRINT_MODIFIER_ID);
                    }
                }
            } else if (runBurstTimer > 0) {
                runBurstTimer--;
                this.setSprinting(true);

                var attr = this.getAttribute(Attributes.MOVEMENT_SPEED);
                if (attr != null && !attr.hasModifier(SPRINT_MODIFIER_ID)) {
                    attr.addTransientModifier(SPRINT_MODIFIER);
                }

                if (runBurstType == 2) { // Lari sambil loncat
                    if (jumpCooldown > 0) {
                        jumpCooldown--;
                    }
                    if (jumpCooldown <= 0 && this.onGround() && this.getDeltaMovement().horizontalDistanceSqr() > 0.003D) {
                        this.getJumpControl().jump();
                        jumpCooldown = 12 + this.random.nextInt(8); // jump cadence every ~0.6-1.0s
                    }
                }

                if (runBurstTimer <= 0) {
                    this.setSprinting(false);
                    runBurstType = 0;
                    if (attr != null && attr.hasModifier(SPRINT_MODIFIER_ID)) {
                        attr.removeModifier(SPRINT_MODIFIER_ID);
                    }
                }
            } else {
                // Periodically decide if visitor wants a burst of excitement/running
                int interval = isChildVisitor() ? 50 : 90;
                if (this.tickCount % interval == 0) {
                    float chance = isChildVisitor() ? 0.30F : 0.12F;
                    if (this.random.nextFloat() < chance && (!this.getNavigation().isDone() || this.getDeltaMovement().horizontalDistanceSqr() > 0.002D)) {
                        runBurstTimer = 40 + this.random.nextInt(60); // 2 to 5 seconds
                        runBurstType = this.random.nextBoolean() ? 1 : 2; // 50% run only, 50% run and jump!
                        jumpCooldown = 4;
                        this.setSprinting(true);
                    }
                }
            }
        }
    }

    @Override
    public boolean hurt(net.minecraft.world.damagesource.DamageSource damageSource, float amount) {
        if (this.isPassenger()) {
            this.stopRiding();
        }
        return super.hurt(damageSource, amount);
    }

    public void forceLeave() {
        this.stayTime = MAX_STAY + 1;
        this.entityData.set(IS_LEAVING, true);
        this.setMood(VisitorMood.NEUTRAL);
        BlockPos gate = resolveLeaveTarget();
        if (gate != null) {
            this.getNavigation().stop();
            moveTowardLongRange(gate, 1.2D);
            if (this.distanceToSqr(gate.getX() + 0.5, gate.getY(), gate.getZ() + 0.5) < 9.0D) {
                this.discard();
            }
        } else {
            this.discard();
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.level().isClientSide() && hand == InteractionHand.MAIN_HAND && player instanceof ServerPlayer sPlayer) {
            VisitorMood currentMood = getMood();
            net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(sPlayer, new net.zootycoon.network.ZooNetworkPayloads.OpenVisitorScreenPayload(
                    this.getId(),
                    this.getUUID(),
                    getVariant(),
                    isChildVisitor(),
                    currentMood.name(),
                    currentMood.getLabel(),
                    getHunger(),
                    getThirst(),
                    needsToilet(),
                    hasTrash()
            ));
            this.playSound(SoundEvents.VILLAGER_YES, 1.0F, 1.0F);
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (gatePos != null) {
            tag.putInt("GateX", gatePos.getX());
            tag.putInt("GateY", gatePos.getY());
            tag.putInt("GateZ", gatePos.getZ());
        }
        tag.putInt("StayTime", stayTime);
        tag.putInt("Variant", getVariant());
        tag.putBoolean("IsChild", isChildVisitor());
        tag.putInt("Hunger", getHunger());
        tag.putInt("Thirst", getThirst());
        tag.putInt("Mood", getMood().ordinal());
        tag.putBoolean("HasTrash", hasTrash());
        tag.putBoolean("HasUsedToilet", hasUsedToilet);
        tag.putBoolean("NeedsToilet", needsToilet());
        tag.putInt("SitCooldown", sitCooldown);
        tag.putInt("ConsumerType", consumerType);
        tag.putInt("FoodCount", foodCount);
        tag.putInt("DrinkCount", drinkCount);
        tag.putInt("CashierCooldown", cashierCooldown);
        if (guidingPlayerUuid != null) {
            tag.putString("GuidingPlayerUuid", guidingPlayerUuid.toString());
        }
        if (guidedPlayerUuid != null) {
            tag.putString("GuidedPlayerUuid", guidedPlayerUuid.toString());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("GateX") && tag.contains("GateY") && tag.contains("GateZ")) {
            gatePos = new BlockPos(
                    tag.getInt("GateX"),
                    tag.getInt("GateY"),
                    tag.getInt("GateZ")
            );
        }
        stayTime = tag.getInt("StayTime");
        this.entityData.set(IS_LEAVING, isTimeToLeave());
        setVariant(tag.getInt("Variant"));
        setChildVisitor(tag.getBoolean("IsChild"));
        if (tag.contains("ConsumerType")) {
            consumerType = tag.getInt("ConsumerType");
        } else {
            consumerType = (this.getUUID().getLeastSignificantBits() & 1) == 0 ? 0 : 1;
        }
        foodCount = tag.getInt("FoodCount");
        drinkCount = tag.getInt("DrinkCount");
        cashierCooldown = tag.getInt("CashierCooldown");
        setHunger(tag.getInt("Hunger"));
        setThirst(tag.getInt("Thirst"));
        setMood(VisitorMood.fromOrdinal(tag.contains("Mood") ? tag.getInt("Mood") : VisitorMood.NEUTRAL.ordinal()));
        setHasTrash(tag.getBoolean("HasTrash"));
        hasUsedToilet = tag.getBoolean("HasUsedToilet");
        setNeedsToilet(tag.getBoolean("NeedsToilet"));
        sitCooldown = tag.getInt("SitCooldown");
        if (tag.contains("GuidingPlayerUuid")) {
            try {
                guidingPlayerUuid = UUID.fromString(tag.getString("GuidingPlayerUuid"));
            } catch (Exception ignored) {}
        }
        if (tag.contains("GuidedPlayerUuid")) {
            try {
                guidedPlayerUuid = UUID.fromString(tag.getString("GuidedPlayerUuid"));
            } catch (Exception ignored) {}
        }
    }

    public void startFollowingPlayer(Player player) {
        if (player == null) return;
        if (this.isPassenger()) {
            this.stopRiding();
        }
        this.guidingPlayerUuid = player.getUUID();
        this.guidedPlayerUuid = player.getUUID();
        this.guideTimeout = 1200;
        this.getNavigation().stop();
    }

    public void stopFollowingPlayer() {
        this.guidingPlayerUuid = null;
        this.guidedPlayerUuid = null;
        this.guideTimeout = 0;
        this.getNavigation().stop();
    }

    public UUID getGuidedPlayerUuid() {
        return guidedPlayerUuid;
    }

    public void setGuidedPlayerUuid(UUID uuid) {
        this.guidedPlayerUuid = uuid;
    }

    public void setEatTimer(int ticks) {
        this.eatTimer = ticks;
    }

    public boolean isConsuming() {
        return this.entityData.get(IS_CONSUMING);
    }

    public boolean isDrinking() {
        return this.entityData.get(IS_DRINKING);
    }

    public void startConsuming(net.minecraft.world.item.ItemStack purchasedStack, boolean drink) {
        if (purchasedStack == null || purchasedStack.isEmpty()) return;
        this.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, purchasedStack.copyWithCount(1));
        this.setDropChance(net.minecraft.world.entity.EquipmentSlot.MAINHAND, 0.0F);
        this.setItemInHand(InteractionHand.OFF_HAND, net.minecraft.world.item.ItemStack.EMPTY);
        this.eatTimer = 48;
        this.entityData.set(IS_CONSUMING, true);
        this.entityData.set(IS_DRINKING, drink);
        if (escapeTicks <= 0) {
            this.getNavigation().stop();
        }
    }

    public void onTransactionComplete(BlockPos cashierPos, String role, net.minecraft.world.item.ItemStack purchasedItem, boolean isDrink) {
        this.cashierCooldown = 400; // 20s cooldown against queueing again
        if ("BURGER".equalsIgnoreCase(role)) {
            this.foodCount++;
            setHunger(0);
            if (purchasedItem != null && !purchasedItem.isEmpty()) {
                startConsuming(purchasedItem, false);
            }
        } else if ("DRINK".equalsIgnoreCase(role)) {
            this.drinkCount++;
            setThirst(0);
            if (purchasedItem != null && !purchasedItem.isEmpty()) {
                startConsuming(purchasedItem, true);
            }
        } else if ("SOUVENIR".equalsIgnoreCase(role)) {
            this.wantsSouvenir = false;
        } else if ("TICKET".equalsIgnoreCase(role)) {
            this.addTag("ticket_purchased");
        }
        updateMood();
        triggerRunAwayFromCashier(cashierPos);
    }

    public void triggerRunAwayFromCashier(BlockPos cashierPos) {
        if (!(this.level() instanceof net.minecraft.server.level.ServerLevel sLevel)) return;
        net.zootycoon.claim.ZooClaim claim = findActiveZooClaim();
        double targetX;
        double targetZ;

        if (claim != null) {
            var box = claim.getBox();
            int marginX = Math.min(4, Math.max(1, (box.maxX() - box.minX()) / 4));
            int marginZ = Math.min(4, Math.max(1, (box.maxZ() - box.minZ()) / 4));
            int interiorMinX = box.minX() + marginX;
            int interiorMaxX = box.maxX() - marginX;
            int interiorMinZ = box.minZ() + marginZ;
            int interiorMaxZ = box.maxZ() - marginZ;

            boolean outside = !claim.contains(this.blockPosition().getX(), this.blockPosition().getZ());
            double dx = this.getX() - (cashierPos.getX() + 0.5D);
            double dz = this.getZ() - (cashierPos.getZ() + 0.5D);
            double dist = Math.sqrt(dx * dx + dz * dz);

            if (outside || dist < 4.0D) {
                if (dist < 0.1D) {
                    dx = ((interiorMinX + interiorMaxX) / 2.0D) - this.getX();
                    dz = ((interiorMinZ + interiorMaxZ) / 2.0D) - this.getZ();
                    dist = Math.sqrt(dx * dx + dz * dz);
                }
                double nx = dx / (dist > 0.01D ? dist : 1.0D);
                double nz = dz / (dist > 0.01D ? dist : 1.0D);

                // If running away points further outside the claim, point into the claim interior instead
                double testX = this.getX() + nx * 14.0D;
                double testZ = this.getZ() + nz * 14.0D;
                if (!claim.contains((int) testX, (int) testZ)) {
                    double cx = (interiorMinX + interiorMaxX) / 2.0D;
                    double cz = (interiorMinZ + interiorMaxZ) / 2.0D;
                    double toClaimX = cx - this.getX();
                    double toClaimZ = cz - this.getZ();
                    double toClaimLen = Math.sqrt(toClaimX * toClaimX + toClaimZ * toClaimZ);
                    nx = toClaimX / (toClaimLen > 0.01D ? toClaimLen : 1.0D);
                    nz = toClaimZ / (toClaimLen > 0.01D ? toClaimLen : 1.0D);
                }

                double runDist = 14.0D + this.random.nextDouble() * 4.0D;
                targetX = Math.clamp(this.getX() + nx * runDist, interiorMinX, interiorMaxX);
                targetZ = Math.clamp(this.getZ() + nz * runDist, interiorMinZ, interiorMaxZ);
            } else {
                double nx = dist > 0.01D ? dx / dist : 1.0D;
                double nz = dist > 0.01D ? dz / dist : 0.0D;
                double runDist = 14.0D + this.random.nextDouble() * 4.0D;
                targetX = Math.clamp(this.getX() + nx * runDist, interiorMinX, interiorMaxX);
                targetZ = Math.clamp(this.getZ() + nz * runDist, interiorMinZ, interiorMaxZ);
            }
        } else {
            double dx = this.getX() - (cashierPos.getX() + 0.5D);
            double dz = this.getZ() - (cashierPos.getZ() + 0.5D);
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (dist < 0.1D) {
                double ang = this.random.nextDouble() * Math.PI * 2.0D;
                dx = Math.cos(ang);
                dz = Math.sin(ang);
                dist = 1.0D;
            }
            double nx = dx / dist;
            double nz = dz / dist;
            double runDist = 14.0D + this.random.nextDouble() * 4.0D;
            targetX = this.getX() + nx * runDist;
            targetZ = this.getZ() + nz * runDist;
        }

        int ty = (int) this.getY();
        int surfY = sLevel.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int) targetX, (int) targetZ);
        if (Math.abs(surfY - this.blockPosition().getY()) <= 8) {
            ty = surfY;
        }

        this.escapeTarget = new BlockPos((int) targetX, ty, (int) targetZ);
        this.escapeTicks = 70; // 3.5 seconds
        this.runBurstTimer = 70;
        this.runBurstType = 1;
        this.setSprinting(true);
        var attr = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attr != null && !attr.hasModifier(SPRINT_MODIFIER_ID)) {
            attr.addTransientModifier(SPRINT_MODIFIER);
        }
        this.getNavigation().moveTo(this.escapeTarget.getX() + 0.5D, this.escapeTarget.getY(), this.escapeTarget.getZ() + 0.5D, 1.35D);
    }

    public void completeGuideReward(net.minecraft.server.level.ServerLevel sLevel, String facilityType) {
        if (this.guidedPlayerUuid == null) return;
        Player player = sLevel.getPlayerByUUID(this.guidedPlayerUuid);
        this.guidingPlayerUuid = null;
        this.guidedPlayerUuid = null;
        this.guideTimeout = 0;
        if (player instanceof ServerPlayer sPlayer && sLevel.getServer() != null) {
            int newRating = net.zootycoon.economy.ZooRatingManager.get(sLevel.getServer()).addRating(player.getUUID(), 2, sPlayer);
            String toastKey = switch (facilityType) {
                case "TOILET" -> "zootycoon.toast.rating.guide_toilet_success";
                case "BURGER" -> "zootycoon.toast.rating.guide_burger_success";
                case "DRINK" -> "zootycoon.toast.rating.guide_drink_success";
                default -> "zootycoon.toast.rating.guide_success";
            };
            net.zootycoon.network.ZooNetworkPayloads.sendToast(sPlayer, "zootycoon.toast.title.rating_up", toastKey, 1, newRating);
            sLevel.playSound(null, this.blockPosition(), SoundEvents.PLAYER_LEVELUP, net.minecraft.sounds.SoundSource.NEUTRAL, 0.8F, 1.2F);
            sLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 1.2D, this.getZ(), 8, 0.3, 0.3, 0.3, 0.05);
            sLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.HEART, this.getX(), this.getY() + 1.4D, this.getZ(), 4, 0.25, 0.25, 0.25, 0.02);
        }
    }

    public Player getGuidingPlayer() {
        if (guidingPlayerUuid == null) return null;
        if (this.level() instanceof net.minecraft.server.level.ServerLevel sl) {
            return sl.getPlayerByUUID(guidingPlayerUuid);
        }
        return null;
    }

    // =========================================================================
    // GOALS
    // =========================================================================

    /**
     * Priority-1 Goal that forces visitors who just finished buying from a cashier
     * to immediately sprint far away into the zoo so cashier fronts never jam.
     */
    public static class RunAwayFromCashierGoal extends Goal {
        private final ZooVisitorEntity visitor;

        public RunAwayFromCashierGoal(ZooVisitorEntity visitor) {
            this.visitor = visitor;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return visitor.escapeTicks > 0 && visitor.escapeTarget != null;
        }

        @Override
        public boolean canContinueToUse() {
            return visitor.escapeTicks > 0 && visitor.escapeTarget != null;
        }

        @Override
        public void start() {
            if (visitor.escapeTarget != null) {
                visitor.setSprinting(true);
                visitor.getNavigation().moveTo(
                        visitor.escapeTarget.getX() + 0.5D,
                        visitor.escapeTarget.getY(),
                        visitor.escapeTarget.getZ() + 0.5D,
                        1.35D
                );
            }
        }

        @Override
        public void tick() {
            if (visitor.escapeTarget != null) {
                double distSq = visitor.distanceToSqr(
                        visitor.escapeTarget.getX() + 0.5D,
                        visitor.escapeTarget.getY(),
                        visitor.escapeTarget.getZ() + 0.5D
                );
                if (distSq <= 2.25D) {
                    visitor.escapeTicks = 0;
                    visitor.escapeTarget = null;
                    visitor.getNavigation().stop();
                } else if (visitor.getNavigation().isDone() || visitor.tickCount % 15 == 0) {
                    visitor.getNavigation().moveTo(
                            visitor.escapeTarget.getX() + 0.5D,
                            visitor.escapeTarget.getY(),
                            visitor.escapeTarget.getZ() + 0.5D,
                            1.35D
                    );
                }
            }
        }

        @Override
        public void stop() {
            visitor.escapeTarget = null;
            visitor.escapeTicks = 0;
            visitor.setSprinting(false);
            var attr = visitor.getAttribute(Attributes.MOVEMENT_SPEED);
            if (attr != null && attr.hasModifier(SPRINT_MODIFIER_ID)) {
                attr.removeModifier(SPRINT_MODIFIER_ID);
            }
        }
    }

    /**
     * Goal for newly spawned visitors outside the zoo to queue at the Ticket Booth first,
     * then immediately walk inside the claimed zoo area once their ticket is purchased.
     */
    public static class BuyTicketAtEntranceGoal extends Goal {
        private final ZooVisitorEntity visitor;
        private ZooCashierEntity targetTicketCashier;
        private int repathTimer = 0;

        public BuyTicketAtEntranceGoal(ZooVisitorEntity visitor) {
            this.visitor = visitor;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (visitor.isTimeToLeave() || visitor.getTags().contains("ticket_purchased") || visitor.hasCashierCooldown()) {
                return false;
            }
            if (visitor.guidingPlayerUuid != null) {
                return false;
            }
            List<ZooCashierEntity> ticketCashiers = visitor.level().getEntitiesOfClass(
                    ZooCashierEntity.class,
                    visitor.getFullZooSearchBox(),
                    c -> c.isAlive() && "TICKET".equalsIgnoreCase(c.getCashierRole())
            );
            if (ticketCashiers.isEmpty()) {
                return false;
            }
            ZooCashierEntity best = null;
            double bestScore = Double.MAX_VALUE;
            for (ZooCashierEntity c : ticketCashiers) {
                double score = Math.sqrt(visitor.distanceToSqr(c)) + c.getQueueSize() * 6.0D;
                if (score < bestScore) {
                    bestScore = score;
                    best = c;
                }
            }
            this.targetTicketCashier = best;
            return this.targetTicketCashier != null;
        }

        @Override
        public boolean canContinueToUse() {
            return !visitor.isTimeToLeave()
                    && !visitor.getTags().contains("ticket_purchased")
                    && !visitor.hasCashierCooldown()
                    && targetTicketCashier != null
                    && targetTicketCashier.isAlive()
                    && "TICKET".equalsIgnoreCase(targetTicketCashier.getCashierRole());
        }

        @Override
        public void start() {
            this.repathTimer = 0;
        }

        @Override
        public void stop() {
            if (this.targetTicketCashier != null) {
                this.targetTicketCashier.removeFromQueue(visitor.getUUID());
            }
            this.targetTicketCashier = null;
            if (visitor.getTags().contains("ticket_purchased") && !visitor.isTimeToLeave()) {
                visitor.walkIntoZooInterior();
            }
        }

        @Override
        public void tick() {
            if (targetTicketCashier == null) return;
            visitor.getLookControl().setLookAt(targetTicketCashier, 30.0F, 30.0F);

            double distToCashierSq = visitor.distanceToSqr(targetTicketCashier);
            BlockPos targetSlot;
            if (distToCashierSq <= 14.0D * 14.0D) {
                int qIdx = targetTicketCashier.enqueueOrGetIndex(visitor.getUUID());
                targetSlot = targetTicketCashier.getQueueSlotPos(qIdx);
            } else {
                targetSlot = targetTicketCashier.getQueueSlotPos(targetTicketCashier.getQueueSize());
            }

            double distToSlotSq = visitor.distanceToSqr(targetSlot.getX() + 0.5D, targetSlot.getY(), targetSlot.getZ() + 0.5D);
            if (distToSlotSq <= 0.65D) {
                visitor.getNavigation().stop();
                return;
            }

            if (repathTimer > 0) repathTimer--;
            if (repathTimer <= 0 || visitor.getNavigation().isDone()) {
                visitor.moveTowardLongRange(targetSlot, 1.0D);
                repathTimer = 15;
            }
        }
    }

    /**
     * Goal for visitors inside the zoo to find and queue at Burger, Drink, or Souvenir cashiers
     * anywhere across the claimed zoo area, lining up neatly in front of the cashier's face.
     */
    public static class QueueAtCashierGoal extends Goal {
        private final ZooVisitorEntity visitor;
        private ZooCashierEntity targetCashier;
        private String targetRole;
        private int repathTimer = 0;

        public QueueAtCashierGoal(ZooVisitorEntity visitor) {
            this.visitor = visitor;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        private String determineDesiredRole() {
            if (visitor.isTimeToLeave() || visitor.isConsuming() || visitor.needsToilet() || visitor.hasCashierCooldown()) {
                return null;
            }
            if (!visitor.getTags().contains("ticket_purchased") || visitor.guidingPlayerUuid != null) {
                return null;
            }
            if (visitor.canEat() && (visitor.getHunger() >= 35 || visitor.getMood() == VisitorMood.HUNGRY)) {
                return "BURGER";
            }
            if (visitor.canDrink() && (visitor.getThirst() >= 25 || visitor.getMood() == VisitorMood.THIRSTY)) {
                return "DRINK";
            }
            if (visitor.wantsSouvenir()) {
                return "SOUVENIR";
            }
            return null;
        }

        @Override
        public boolean canUse() {
            String role = determineDesiredRole();
            if (role == null) return false;

            List<ZooCashierEntity> cashiers = visitor.level().getEntitiesOfClass(
                    ZooCashierEntity.class,
                    visitor.getFullZooSearchBox(),
                    c -> c.isAlive() && role.equalsIgnoreCase(c.getCashierRole()) && !c.isOutOfStock()
            );
            if (cashiers.isEmpty()) {
                return false;
            }

            ZooCashierEntity best = null;
            double bestScore = Double.MAX_VALUE;
            for (ZooCashierEntity c : cashiers) {
                double score = Math.sqrt(visitor.distanceToSqr(c)) + c.getQueueSize() * 10.0D;
                if (score < bestScore) {
                    bestScore = score;
                    best = c;
                }
            }
            if (best != null) {
                this.targetCashier = best;
                this.targetRole = role;
                return true;
            }
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            if (targetCashier == null || !targetCashier.isAlive() || targetCashier.isOutOfStock()) {
                return false;
            }
            String desired = determineDesiredRole();
            if (desired == null || !desired.equalsIgnoreCase(targetRole)) {
                return false;
            }
            return targetRole.equalsIgnoreCase(targetCashier.getCashierRole());
        }

        @Override
        public void start() {
            this.repathTimer = 0;
        }

        @Override
        public void stop() {
            if (this.targetCashier != null) {
                this.targetCashier.removeFromQueue(visitor.getUUID());
            }
            if ("SOUVENIR".equalsIgnoreCase(this.targetRole)
                    && !visitor.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD).isEmpty()) {
                visitor.setWantsSouvenir(false);
            }
            this.targetCashier = null;
            this.targetRole = null;
        }

        @Override
        public void tick() {
            if (targetCashier == null) return;
            visitor.getLookControl().setLookAt(targetCashier, 30.0F, 30.0F);

            double distToCashierSq = visitor.distanceToSqr(targetCashier);
            BlockPos targetSlot;
            if (distToCashierSq <= 12.0D * 12.0D) {
                int qIdx = targetCashier.enqueueOrGetIndex(visitor.getUUID());
                targetSlot = targetCashier.getQueueSlotPos(qIdx);
            } else {
                targetSlot = targetCashier.getQueueSlotPos(targetCashier.getQueueSize());
            }

            double distToSlotSq = visitor.distanceToSqr(targetSlot.getX() + 0.5D, targetSlot.getY(), targetSlot.getZ() + 0.5D);
            if (distToSlotSq <= 0.65D) {
                visitor.getNavigation().stop();
                return;
            }

            if (repathTimer > 0) repathTimer--;
            if (repathTimer <= 0 || visitor.getNavigation().isDone()) {
                visitor.moveTowardLongRange(targetSlot, 1.0D);
                repathTimer = 15;
            }
        }
    }

    /**
     * Goal for following the guiding player when guided to a facility (Toilet, Food, or Drink).
     */
    public static class FollowGuidePlayerGoal extends Goal {
        private final ZooVisitorEntity visitor;
        private int pathingCooldown = 0;

        public FollowGuidePlayerGoal(ZooVisitorEntity visitor) {
            this.visitor = visitor;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (visitor.isTimeToLeave()) {
                if (visitor.guidingPlayerUuid != null) {
                    visitor.stopFollowingPlayer();
                }
                return false;
            }
            Player player = visitor.getGuidingPlayer();
            if (player == null || !player.isAlive()) {
                if (visitor.guidingPlayerUuid != null) {
                    visitor.stopFollowingPlayer();
                }
                return false;
            }
            if (visitor.distanceToSqr(player) > 36.0 * 36.0) {
                visitor.stopFollowingPlayer();
                return false;
            }
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return canUse();
        }

        @Override
        public void start() {
            this.pathingCooldown = 0;
            visitor.getNavigation().stop();
        }

        @Override
        public void stop() {
            visitor.getNavigation().stop();
        }

        @Override
        public void tick() {
            Player player = visitor.getGuidingPlayer();
            if (player == null) return;

            visitor.getLookControl().setLookAt(player, 30.0F, 30.0F);

            if (handleFacilityApproached()) {
                return;
            }

            double distSq = visitor.distanceToSqr(player);
            if (distSq > 3.5 * 3.5) {
                if (pathingCooldown > 0) {
                    pathingCooldown--;
                }
                if (pathingCooldown <= 0 || visitor.getNavigation().isDone()) {
                    visitor.getNavigation().moveTo(player, 1.25D);
                    pathingCooldown = 15;
                }
            } else {
                visitor.getNavigation().stop();
            }
        }

        private boolean handleFacilityApproached() {
            BlockPos vPos = visitor.blockPosition();

            // 1. Toilet facility check: if visitor needs toilet and toilet is within 6 blocks
            if (visitor.needsToilet() || visitor.getMood() == VisitorMood.TOILET) {
                for (BlockPos p : BlockPos.betweenClosed(vPos.offset(-6, -2, -6), vPos.offset(6, 2, 6))) {
                    if (visitor.level().getBlockState(p).is(net.zootycoon.block.ZooBlocks.TOILET)) {
                        // Stop following player movement so FindToiletGoal can path and occupy the toilet
                        visitor.guidingPlayerUuid = null;
                        visitor.getNavigation().stop();
                        return true;
                    }
                }
            }

            // 2. Hunger facility check: must be a registered BURGER stand
            if (visitor.getHunger() >= 50 || visitor.getMood() == VisitorMood.HUNGRY) {
                List<ZooCashierEntity> burgerCashiers = visitor.level().getEntitiesOfClass(
                        ZooCashierEntity.class,
                        visitor.getBoundingBox().inflate(6.0D, 3.0D, 6.0D),
                        c -> c.isAlive() && "BURGER".equals(c.getCashierRole())
                );
                if (!burgerCashiers.isEmpty()) {
                    // Stop following player and queue at the burger cashier
                    visitor.guidingPlayerUuid = null;
                    ZooCashierEntity c = burgerCashiers.get(0);
                    int qIdx = c.enqueueOrGetIndex(visitor.getUUID());
                    visitor.moveTowardLongRange(c.getQueueSlotPos(qIdx), 1.0D);
                    return true;
                }
            }

            // 3. Thirst facility check: must be a registered DRINK stand (or ICE_CREAM)
            if (visitor.getThirst() >= 50 || visitor.getMood() == VisitorMood.THIRSTY) {
                List<ZooCashierEntity> drinkCashiers = visitor.level().getEntitiesOfClass(
                        ZooCashierEntity.class,
                        visitor.getBoundingBox().inflate(6.0D, 3.0D, 6.0D),
                        c -> c.isAlive() && ("DRINK".equals(c.getCashierRole()) || "ICE_CREAM".equals(c.getCashierRole()))
                );
                if (!drinkCashiers.isEmpty()) {
                    // Stop following player and queue at the drink cashier
                    visitor.guidingPlayerUuid = null;
                    ZooCashierEntity c = drinkCashiers.get(0);
                    int qIdx = c.enqueueOrGetIndex(visitor.getUUID());
                    visitor.moveTowardLongRange(c.getQueueSlotPos(qIdx), 1.0D);
                    return true;
                }
            }

            return false;
        }
    }

    /**
     * Goal for watching animals in the zoo.
     * Searches for nearby living Animal entities, approaches them, and observes them.
     */
    public static class WatchAnimalsGoal extends Goal {
        private final ZooVisitorEntity visitor;
        private Animal targetAnimal;
        private int watchTimer;
        private BlockPos watchPos;

        public WatchAnimalsGoal(ZooVisitorEntity visitor) {
            this.visitor = visitor;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (visitor.isTimeToLeave()) return false;
            if (!visitor.getTags().contains("ticket_purchased")) return false;
            if (visitor.tickCount % 40 != 0) return false;

            // Search for registered zoo animals across the zoo
            List<Animal> nearbyAnimals = visitor.level().getEntitiesOfClass(
                    Animal.class,
                    visitor.getBoundingBox().inflate(36.0D),
                    a -> a.isAlive() && ZooAnimalHungerManager.isTagged(a)
            );

            if (nearbyAnimals.isEmpty()) return false;

            // Pick nearest animal
            Animal best = null;
            double bestDist = Double.MAX_VALUE;
            for (Animal a : nearbyAnimals) {
                double d = visitor.distanceToSqr(a);
                if (d < bestDist) {
                    bestDist = d;
                    best = a;
                }
            }

            if (best != null) {
                this.targetAnimal = best;
                return true;
            }
            return false;
        }

        @Override
        public void start() {
            int base = 140 + visitor.random.nextInt(100); // ~7-12 seconds
            watchTimer = base;
            if (targetAnimal != null) {
                visitor.watchingAnimalId = targetAnimal.getId();
                visitor.watchingAnimalSize = determineAnimalSize(targetAnimal);
                watchPos = computeWatchPos(targetAnimal);
                visitor.updateMood();
            }
        }

        @Override
        public void stop() {
            visitor.watchingAnimalId = -1;
            visitor.watchingAnimalSize = -1;
            targetAnimal = null;
            watchPos = null;
            visitor.updateMood();
        }

        @Override
        public void tick() {
            if (targetAnimal == null || !targetAnimal.isAlive()) {
                watchTimer = 0;
                return;
            }

            if (watchPos == null || visitor.distanceToSqr(watchPos.getX() + 0.5, watchPos.getY(), watchPos.getZ() + 0.5) < 2.0D) {
                watchPos = computeWatchPos(targetAnimal);
            }

            if (watchPos != null) {
                double dist = visitor.distanceToSqr(watchPos.getX() + 0.5, watchPos.getY(), watchPos.getZ() + 0.5);
                if (dist > 4.0D || visitor.getNavigation().isDone()) {
                    visitor.moveTowardLongRange(watchPos, 1.0D);
                }
            }

            visitor.getLookControl().setLookAt(targetAnimal, 30.0F, 30.0F);
            visitor.watchingAnimalSize = determineAnimalSize(targetAnimal);

            if (watchTimer > 0) {
                watchTimer--;
            }
        }

        @Override
        public boolean canContinueToUse() {
            return watchTimer > 0 && targetAnimal != null && targetAnimal.isAlive() && !visitor.isTimeToLeave();
        }

        private int determineAnimalSize(Animal animal) {
            if (animal.isBaby() || animal.getBbWidth() < 0.8F) {
                return 0; // Small / baby (ADORED / Gemas)
            }
            if (animal.getBbWidth() > 1.4F || animal.getBbHeight() > 1.8F) {
                return 2; // Big / imposing (AMAZED / Takjub)
            }
            return 1; // Normal (NEUTRAL / Biasa)
        }

        private BlockPos computeWatchPos(Animal target) {
            double radius = 4.0D;
            double angle = ((visitor.getId() * 37) + (target.getId() * 19)) % 360;
            double rad = Math.toRadians(angle);
            int dx = (int) Math.round(Math.cos(rad) * radius);
            int dz = (int) Math.round(Math.sin(rad) * radius);
            return target.blockPosition().offset(dx, 0, dz);
        }
    }

    /**
     * Goal for leaving the zoo once stay time is over or visitor is forced to leave.
     */
    public static class LeaveZooGoal extends Goal {
        private final ZooVisitorEntity visitor;

        public LeaveZooGoal(ZooVisitorEntity visitor) {
            this.visitor = visitor;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return visitor.isTimeToLeave();
        }

        @Override
        public void start() {
            visitor.setMood(VisitorMood.NEUTRAL);
            BlockPos gate = visitor.resolveLeaveTarget();
            if (gate != null) {
                visitor.moveTowardLongRange(gate, 1.15D);
            } else {
                visitor.discard();
            }
        }

        @Override
        public void tick() {
            BlockPos gate = visitor.resolveLeaveTarget();
            if (gate == null) {
                visitor.discard();
                return;
            }

            if (visitor.getNavigation().isDone() || visitor.tickCount % 20 == 0) {
                visitor.moveTowardLongRange(gate, 1.15D);
            }

            if (visitor.distanceToSqr(gate.getX() + 0.5, gate.getY(), gate.getZ() + 0.5) < 9.0D) {
                visitor.discard();
            }
        }
    }

    /**
     * Goal for finding a trash can to dispose of trash, or dropping it on the ground (littering) if none found.
     */
    public static class FindTrashCanGoal extends Goal {
        private final ZooVisitorEntity visitor;
        private BlockPos targetPos;
        private int timer;
        private int searchTimer;

        public FindTrashCanGoal(ZooVisitorEntity visitor) {
            this.visitor = visitor;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (!visitor.hasTrash() || visitor.isTimeToLeave()) return false;

            BlockPos p = visitor.blockPosition();
            for (BlockPos pos : BlockPos.betweenClosed(p.offset(-16, -3, -16), p.offset(16, 3, 16))) {
                if (visitor.level().getBlockState(pos).is(net.zootycoon.block.ZooBlocks.TRASH_CAN)) {
                    this.targetPos = pos.immutable();
                    this.searchTimer = 0;
                    return true;
                }
            }

            this.searchTimer++;
            if (this.searchTimer > 300) { // ~15 seconds without finding trash can
                this.targetPos = null;
                return true; // triggers littering
            }

            return false;
        }

        @Override
        public void start() {
            if (targetPos != null) {
                visitor.getNavigation().moveTo(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5, 1.0D);
            }
            timer = 0;
        }

        @Override
        public void tick() {
            if (targetPos != null) {
                double distSq = visitor.distanceToSqr(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5);
                if (distSq < 4.0D) {
                    visitor.getNavigation().stop();
                    timer++;

                    if (timer == 2) {
                        net.minecraft.world.level.block.state.BlockState state = visitor.level().getBlockState(targetPos);
                        if (state.is(net.zootycoon.block.ZooBlocks.TRASH_CAN)) {
                            visitor.level().setBlockAndUpdate(targetPos, state.setValue(net.zootycoon.block.TrashCanBlock.OPEN, true));
                            visitor.playSound(SoundEvents.CHEST_OPEN, 0.5F, 0.8F);
                        }
                    }

                    if (timer > 20) {
                        visitor.setHasTrash(false);
                        visitor.playSound(SoundEvents.ITEM_PICKUP, 1.0F, 1.0F);

                        net.minecraft.world.level.block.state.BlockState state = visitor.level().getBlockState(targetPos);
                        if (state.is(net.zootycoon.block.ZooBlocks.TRASH_CAN)) {
                            visitor.level().setBlockAndUpdate(targetPos, state.setValue(net.zootycoon.block.TrashCanBlock.OPEN, false));
                            visitor.playSound(SoundEvents.CHEST_CLOSE, 0.5F, 0.8F);
                        }

                        targetPos = null;
                        searchTimer = 0;
                    }
                } else if (visitor.getNavigation().isDone() || visitor.tickCount % 20 == 0) {
                    visitor.getNavigation().moveTo(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5, 1.0D);
                }
            } else {
                // Littering on ground
                BlockPos feet = visitor.blockPosition();
                if (visitor.level().isEmptyBlock(feet)) {
                    visitor.level().setBlockAndUpdate(feet, net.zootycoon.block.ZooBlocks.TRASH.defaultBlockState()
                            .setValue(net.zootycoon.block.TrashBlock.VARIANT, visitor.getRandom().nextInt(4)));
                    visitor.setHasTrash(false);
                    this.searchTimer = 0;
                    visitor.playSound(SoundEvents.CHICKEN_STEP, 1.0F, 0.8F);
                }
            }
        }

        @Override
        public boolean canContinueToUse() {
            if (visitor.isTimeToLeave()) return false;
            if (targetPos == null && visitor.hasTrash()) return true;
            return targetPos != null && visitor.hasTrash() && visitor.level().getBlockState(targetPos).is(net.zootycoon.block.ZooBlocks.TRASH_CAN);
        }
    }

    /**
     * Goal for finding a toilet across the entire zoo, preferring unoccupied toilets,
     * queuing in a straight line in front of the toilet if all toilets are occupied,
     * mounting immediately when occupant, and walking away after dismounting so doorways never jam.
     */
    public static class FindToiletGoal extends Goal {
        private final ZooVisitorEntity visitor;
        private BlockPos targetPos;
        private BlockPos doorPos;
        private Stage stage;
        private int usingTicks;
        private int queueTicks;
        private int repathCooldown;

        private enum Stage {
            APPROACHING,
            USING,
            QUEUING
        }

        public FindToiletGoal(ZooVisitorEntity visitor) {
            this.visitor = visitor;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        private List<BlockPos> collectAllZooToilets() {
            List<BlockPos> list = new java.util.ArrayList<>();
            for (BlockPos known : ToiletBlockEntity.KNOWN_TOILETS) {
                if (visitor.level().getBlockState(known).is(net.zootycoon.block.ZooBlocks.TOILET)) {
                    list.add(known);
                }
            }
            if (list.isEmpty()) {
                BlockPos p = visitor.blockPosition();
                for (BlockPos pos : BlockPos.betweenClosed(p.offset(-32, -4, -32), p.offset(32, 4, 32))) {
                    if (visitor.level().getBlockState(pos).is(net.zootycoon.block.ZooBlocks.TOILET)) {
                        BlockPos imm = pos.immutable();
                        ToiletBlockEntity.KNOWN_TOILETS.add(imm);
                        list.add(imm);
                    }
                }
            }
            return list;
        }

        private BlockPos findBestToilet() {
            List<BlockPos> toilets = collectAllZooToilets();
            if (toilets.isEmpty()) return null;

            UUID myId = visitor.getUUID();
            BlockPos bestFreePos = null;
            double bestFreeDistSq = Double.MAX_VALUE;

            BlockPos bestQueuedPos = null;
            double bestQueuedScore = Double.MAX_VALUE;

            for (BlockPos pos : toilets) {
                if (!(visitor.level().getBlockEntity(pos) instanceof ToiletBlockEntity toilet)) {
                    continue;
                }
                if (toilet.isVisitorOccupant(myId)) {
                    return pos;
                }
                double distSq = visitor.distanceToSqr(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
                if (!toilet.isOccupied() && toilet.getQueueSize() == 0) {
                    if (distSq < bestFreeDistSq) {
                        bestFreeDistSq = distSq;
                        bestFreePos = pos;
                    }
                } else {
                    int qSize = toilet.getQueueSize();
                    if (toilet.isInVisitorQueue(myId)) {
                        qSize = Math.max(0, toilet.getVisitorQueueIndex(myId));
                    }
                    double score = qSize * 20.0D + Math.sqrt(distSq);
                    if (score < bestQueuedScore) {
                        bestQueuedScore = score;
                        bestQueuedPos = pos;
                    }
                }
            }

            return bestFreePos != null ? bestFreePos : bestQueuedPos;
        }

        @Override
        public boolean canUse() {
            if (!visitor.needsToilet() || visitor.isTimeToLeave()) return false;
            if (visitor.guidingPlayerUuid != null) return false;

            BlockPos bestPos = findBestToilet();
            if (bestPos != null) {
                this.targetPos = bestPos;
                this.doorPos = findNearestDoorNear(this.targetPos, 4);
                this.stage = Stage.APPROACHING;
                return true;
            }
            return false;
        }

        @Override
        public void start() {
            usingTicks = 0;
            queueTicks = 0;
            repathCooldown = 0;
            if (targetPos != null) {
                repathIfNeeded(targetPos);
            }
        }

        @Override
        public void stop() {
            if (targetPos != null && !visitor.level().isClientSide()) {
                if (visitor.level().getBlockEntity(targetPos) instanceof ToiletBlockEntity toilet) {
                    toilet.releaseVisitorOccupant(visitor.getUUID());
                }
            }
            if (visitor.isPassenger()) {
                visitor.stopRiding();
            }
            if (visitor.getPose() == net.minecraft.world.entity.Pose.CROUCHING) {
                visitor.setPose(net.minecraft.world.entity.Pose.STANDING);
            }
            targetPos = null;
            doorPos = null;
            stage = null;
            usingTicks = 0;
            queueTicks = 0;
        }

        @Override
        public void tick() {
            if (targetPos == null || visitor.level().isClientSide()) return;

            if (!(visitor.level().getBlockEntity(targetPos) instanceof ToiletBlockEntity toilet)) {
                targetPos = null;
                stage = null;
                return;
            }

            if (stage == Stage.USING) {
                visitor.getNavigation().stop();
                if (usingTicks > 0) {
                    usingTicks--;
                }
                if (usingTicks <= 0) {
                    net.minecraft.core.Direction facing = toilet.getFacingDirection();
                    BlockPos finishedToiletPos = targetPos;

                    if (visitor.isPassenger()) {
                        visitor.stopRiding();
                    }
                    visitor.setPose(net.minecraft.world.entity.Pose.STANDING);
                    visitor.markToiletUsed();

                    if (visitor.level() instanceof net.minecraft.server.level.ServerLevel sl) {
                        toilet.flush(sl);
                        toilet.releaseVisitorOccupant(visitor.getUUID());
                        if (visitor.guidedPlayerUuid != null) {
                            visitor.completeGuideReward(sl, "TOILET");
                        }
                    }

                    // Step out of the toilet block and walk 4 blocks away to the side so queued visitors are never blocked
                    BlockPos stepOut = finishedToiletPos.relative(facing, 1);
                    if (visitor.level().getBlockState(stepOut).getCollisionShape(visitor.level(), stepOut).isEmpty()) {
                        visitor.setPos(stepOut.getX() + 0.5D, stepOut.getY(), stepOut.getZ() + 0.5D);
                    }
                    BlockPos walkAway = finishedToiletPos.relative(facing, 4).relative(facing.getClockWise(), 1);
                    visitor.moveTowardLongRange(walkAway, 1.15D);

                    targetPos = null;
                    stage = null;
                }
                return;
            }

            // Periodically check if another toilet in the zoo is completely free while we are waiting in a queue
            if (visitor.tickCount % 20 == 0 && !toilet.isVisitorOccupant(visitor.getUUID()) && toilet.isOccupied()) {
                BlockPos better = findBestToilet();
                if (better != null && !better.equals(targetPos)) {
                    if (visitor.level().getBlockEntity(better) instanceof ToiletBlockEntity betterToilet && !betterToilet.isOccupied()) {
                        toilet.releaseVisitorOccupant(visitor.getUUID());
                        targetPos = better;
                        doorPos = findNearestDoorNear(targetPos, 4);
                        stage = Stage.APPROACHING;
                        repathIfNeeded(targetPos);
                        return;
                    }
                }
            }

            double distToToiletSq = visitor.distanceToSqr(targetPos.getX() + 0.5D, targetPos.getY(), targetPos.getZ() + 0.5D);

            // Once within 10 blocks of the toilet, register occupancy or queue spot immediately
            if (distToToiletSq <= 10.0D * 10.0D) {
                if (!toilet.isVisitorOccupant(visitor.getUUID()) && !toilet.isInVisitorQueue(visitor.getUUID())) {
                    toilet.requestVisitorUse(visitor.getUUID());
                }
            }

            // Case 1: This visitor is the active occupant allowed to enter and sit on the toilet
            if (toilet.isVisitorOccupant(visitor.getUUID())) {
                stage = Stage.APPROACHING;
                if (doorPos != null && visitor.distanceToSqr(doorPos.getX() + 0.5D, doorPos.getY(), doorPos.getZ() + 0.5D) < 6.25D) {
                    openDoorAt(doorPos);
                }
                if (distToToiletSq <= 6.25D) {
                    visitor.getNavigation().stop();
                    stage = Stage.USING;
                    usingTicks = 80;
                    if (visitor.level() instanceof net.minecraft.server.level.ServerLevel sl) {
                        toilet.seatVisitor(sl, visitor);
                    }
                } else {
                    repathIfNeeded(targetPos);
                }
                return;
            }

            // Case 2: Toilet is occupied, so line up neatly at 2 + queueIndex blocks in front of the toilet
            if (toilet.isInVisitorQueue(visitor.getUUID())) {
                stage = Stage.QUEUING;
                queueTicks++;
                int qIdx = Math.max(0, toilet.getVisitorQueueIndex(visitor.getUUID()));
                BlockPos queueSlot = toilet.getQueueSlotPos(qIdx);
                visitor.getLookControl().setLookAt(targetPos.getX() + 0.5D, targetPos.getY() + 0.8D, targetPos.getZ() + 0.5D);

                double distToSlotSq = visitor.distanceToSqr(queueSlot.getX() + 0.5D, queueSlot.getY(), queueSlot.getZ() + 0.5D);
                if (distToSlotSq <= 0.75D) {
                    visitor.getNavigation().stop();
                } else {
                    repathIfNeeded(queueSlot);
                }

                if (queueTicks > 900) { // 45s safety timeout
                    toilet.releaseVisitorOccupant(visitor.getUUID());
                    targetPos = null;
                    stage = null;
                }
                return;
            }

            // Case 3: Still > 10 blocks away, approaching the toilet area across the zoo
            BlockPos approachTarget = toilet.getQueueSlotPos(toilet.getQueueSize());
            repathIfNeeded(approachTarget);
        }

        @Override
        public boolean canContinueToUse() {
            if (visitor.isTimeToLeave()) return false;
            if (stage == Stage.USING && targetPos != null) return true;
            if (!visitor.needsToilet()) return false;
            return targetPos != null && visitor.level().getBlockState(targetPos).is(net.zootycoon.block.ZooBlocks.TOILET);
        }

        private void repathIfNeeded(BlockPos pos) {
            if (pos == null) return;
            if (repathCooldown > 0) {
                repathCooldown--;
                return;
            }
            if (visitor.getNavigation().isDone() || visitor.tickCount % 20 == 0) {
                visitor.moveTowardLongRange(pos, 1.0D);
                repathCooldown = 12;
            }
        }

        private void openDoorAt(BlockPos pos) {
            var state = visitor.level().getBlockState(pos);
            if (state.getBlock() instanceof net.minecraft.world.level.block.DoorBlock door) {
                if (!state.getValue(net.minecraft.world.level.block.DoorBlock.OPEN)) {
                    door.setOpen(visitor, visitor.level(), state, pos, true);
                }
                return;
            }
            if (state.getBlock() instanceof net.minecraft.world.level.block.FenceGateBlock) {
                if (!state.getValue(net.minecraft.world.level.block.FenceGateBlock.OPEN)) {
                    visitor.level().setBlock(pos, state.setValue(net.minecraft.world.level.block.FenceGateBlock.OPEN, true), 3);
                }
            }
        }

        private BlockPos findNearestDoorNear(BlockPos center, int radius) {
            BlockPos best = null;
            double bestDist = Double.MAX_VALUE;
            for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -2, -radius), center.offset(radius, 2, radius))) {
                var st = visitor.level().getBlockState(pos);
                if (!(st.getBlock() instanceof net.minecraft.world.level.block.DoorBlock)
                        && !(st.getBlock() instanceof net.minecraft.world.level.block.FenceGateBlock)) {
                    continue;
                }
                double d = visitor.distanceToSqr(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                if (d < bestDist) {
                    bestDist = d;
                    best = pos.immutable();
                }
            }
            return best;
        }
    }

    /**
     * Stroll goal that keeps ticketed visitors strictly inside the claimed zoo boundaries.
     */
    public static class VisitorZooStrollGoal extends WaterAvoidingRandomStrollGoal {
        private final ZooVisitorEntity visitor;

        public VisitorZooStrollGoal(ZooVisitorEntity visitor, double speedModifier) {
            super(visitor, speedModifier);
            this.visitor = visitor;
        }

        @Override
        public boolean canUse() {
            if (visitor.isTimeToLeave() || !visitor.getTags().contains("ticket_purchased")) {
                return false;
            }
            return super.canUse();
        }

        @Override
        protected Vec3 getPosition() {
            Vec3 candidate = super.getPosition();
            if (!(visitor.level() instanceof net.minecraft.server.level.ServerLevel sLevel)) {
                return candidate;
            }
            net.zootycoon.claim.ZooClaimManager cm = net.zootycoon.claim.ZooClaimManager.get(sLevel);
            if (candidate != null && cm.isInsideAnyClaim(BlockPos.containing(candidate))) {
                return candidate;
            }
            net.zootycoon.claim.ZooClaim claim = visitor.findActiveZooClaim();
            if (claim != null) {
                var box = claim.getBox();
                int marginX = Math.min(3, Math.max(1, (box.maxX() - box.minX()) / 4));
                int marginZ = Math.min(3, Math.max(1, (box.maxZ() - box.minZ()) / 4));
                int rx = candidate != null ? net.minecraft.util.Mth.floor(candidate.x) : visitor.blockPosition().getX();
                int rz = candidate != null ? net.minecraft.util.Mth.floor(candidate.z) : visitor.blockPosition().getZ();
                int tx = Math.clamp(rx, box.minX() + marginX, box.maxX() - marginX);
                int tz = Math.clamp(rz, box.minZ() + marginZ, box.maxZ() - marginZ);
                int ty = sLevel.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, tx, tz);
                if (Math.abs(ty - visitor.blockPosition().getY()) > 8) {
                    ty = visitor.blockPosition().getY();
                }
                return new Vec3(tx + 0.5D, ty, tz + 0.5D);
            }
            return candidate;
        }
    }

    /**
     * Goal for finding nearby seatable objects (chairs, benches, sofas, stools from any furniture mod)
     * and sitting down to relax for a while.
     */
    public static class FindSeatGoal extends Goal {
        private final ZooVisitorEntity visitor;
        private BlockPos targetPos;
        private int sitTicks = 0;
        private int searchCooldown = 0;
        private boolean isSeated = false;

        public FindSeatGoal(ZooVisitorEntity visitor) {
            this.visitor = visitor;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (visitor.isTimeToLeave()) return false;
            if (!visitor.getTags().contains("ticket_purchased")) return false;
            if (visitor.needsToilet()) return false;
            if (visitor.hasTrash()) return false;
            if (visitor.isPassenger()) return false;
            if (visitor.sitCooldown > 0) {
                visitor.sitCooldown--;
                return false;
            }

            if (searchCooldown > 0) {
                searchCooldown--;
                return false;
            }
            searchCooldown = 40 + visitor.getRandom().nextInt(30);

            // Search within 14 blocks horizontally and 2 blocks vertically
            BlockPos current = visitor.blockPosition();
            BlockPos best = null;
            double bestDist = Double.MAX_VALUE;

            for (BlockPos p : BlockPos.betweenClosed(current.offset(-14, -2, -14), current.offset(14, 2, 14))) {
                if (net.zootycoon.util.ZooSeatHelper.isSeatableBlock(visitor.level(), p, visitor.level().getBlockState(p))) {
                    double d = visitor.distanceToSqr(p.getX() + 0.5, p.getY(), p.getZ() + 0.5);
                    if (d < bestDist) {
                        bestDist = d;
                        best = p.immutable();
                    }
                }
            }

            if (best != null) {
                this.targetPos = best;
                return true;
            }
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            if (visitor.isTimeToLeave() || visitor.needsToilet()) return false;
            if (isSeated) {
                return sitTicks > 0 && visitor.isPassenger();
            }
            return targetPos != null && net.zootycoon.util.ZooSeatHelper.isSeatNotOccupied(visitor.level(), targetPos);
        }

        @Override
        public void start() {
            this.isSeated = false;
            this.sitTicks = 0;
            if (targetPos != null) {
                visitor.getNavigation().moveTo(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5, 1.0D);
            }
        }

        @Override
        public void stop() {
            if (visitor.isPassenger()) {
                visitor.stopRiding();
            }
            this.targetPos = null;
            this.isSeated = false;
            this.sitTicks = 0;
            visitor.sitCooldown = 500 + visitor.getRandom().nextInt(600); // 25-55s cooldown
        }

        @Override
        public void tick() {
            if (isSeated) {
                sitTicks--;
                // Occasionally look around peacefully while sitting
                if (visitor.getRandom().nextInt(30) == 0) {
                    double angle = visitor.getYRot() + (visitor.getRandom().nextFloat() - 0.5F) * 70.0F;
                    double rad = Math.toRadians(angle);
                    visitor.getLookControl().setLookAt(visitor.getX() + Math.sin(rad) * 4.0, visitor.getEyeY(), visitor.getZ() + Math.cos(rad) * 4.0);
                }
                if (sitTicks <= 0) {
                    visitor.stopRiding();
                    isSeated = false;
                }
                return;
            }

            if (targetPos == null) return;

            double distSq = visitor.distanceToSqr(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5);
            if (distSq <= 2.2D) {
                if (visitor.level() instanceof net.minecraft.server.level.ServerLevel sl) {
                    boolean success = net.zootycoon.util.ZooSeatHelper.seatVisitorAt(sl, targetPos, visitor);
                    if (success) {
                        this.isSeated = true;
                        this.sitTicks = 180 + visitor.getRandom().nextInt(240); // 9-21 seconds sitting
                        visitor.getNavigation().stop();
                        return;
                    }
                }
                this.targetPos = null;
            } else if (visitor.getNavigation().isDone() || visitor.tickCount % 20 == 0) {
                visitor.getNavigation().moveTo(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5, 1.0D);
            }
        }
    }
}

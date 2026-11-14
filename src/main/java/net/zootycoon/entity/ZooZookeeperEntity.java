package net.zootycoon.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.Heightmap;
import net.zootycoon.animal.AnimalDietRegistry;
import net.zootycoon.block.ZooBlocks;
import net.zootycoon.block.ZooFeedingTroughBlock;
import net.zootycoon.block.ZooFeedingTroughBlockEntity;
import net.zootycoon.claim.ZooClaim;
import net.zootycoon.claim.ZooClaimManager;
import net.zootycoon.item.ZooItems;
import net.zootycoon.network.ZooNetworkPayloads;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.zootycoon.animal.ZooAnimalHungerManager;
import net.zootycoon.animal.ZooClimate;
import net.zootycoon.animal.habitat.HabitatProfile;
import net.zootycoon.animal.habitat.HabitatProfileLoader;
import net.zootycoon.block.ZooCoolerBlock;
import net.zootycoon.block.ZooHeaterBlock;
import net.zootycoon.block.entity.ZooThermalBlockEntity;
import net.zootycoon.util.ZooTranslations;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ZooZookeeperEntity extends PathfinderMob {

    static {
        net.zootycoon.network.ZookeeperBathingPayloads.register();
    }

    private static final EntityDataAccessor<Integer> FOOD_STOCK_DATA =
            SynchedEntityData.defineId(ZooZookeeperEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> SWEEPING =
            SynchedEntityData.defineId(ZooZookeeperEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> HAS_BATHING_TOOLS =
            SynchedEntityData.defineId(ZooZookeeperEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> BATHING_ACTION =
            SynchedEntityData.defineId(ZooZookeeperEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> HAS_ASSIGNED_ANIMAL =
            SynchedEntityData.defineId(ZooZookeeperEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> IS_MALE =
            SynchedEntityData.defineId(ZooZookeeperEntity.class, EntityDataSerializers.BOOLEAN);

    private static final String[] MALE_NAMES = new String[]{
            // 50 nama 1 kata
            "Budi", "Bambang", "Joko", "Agus", "Surya",
            "Bayu", "Eko", "Hadi", "Indra", "Rian",
            "Fajar", "Gilang", "Dimas", "Rizky", "Randi",
            "Aris", "Dedi", "Asep", "Cecep", "Ujang",
            "Teguh", "Wahyu", "Bagas", "Ilham", "Danang",
            "Yanto", "Rudi", "Anton", "Doni", "Hendra",
            "Tono", "Irfan", "Yoga", "Panji", "Aditya",
            "Lukman", "Ferdi", "Kurnia", "Satria", "Putra",
            "Wawan", "Sigit", "Gunawan", "Heru", "Tirta",
            "Arya", "Reza", "Guntur", "Dodi", "Rahmat",
            // 50 nama 2 kata
            "Budi Santoso", "Bambang Pamungkas", "Joko Susilo", "Agus Setiawan", "Surya Wijaya",
            "Bayu Pratama", "Eko Prasetyo", "Hadi Wibowo", "Indra Kusuma", "Rian Hidayat",
            "Fajar Nugraha", "Gilang Ramadhan", "Dimas Anggara", "Rizky Fauzi", "Randi Saputra",
            "Aris Munandar", "Dedi Suryadi", "Asep Sunandar", "Cecep Gorbachev", "Ujang Bedog",
            "Teguh Prakoso", "Wahyu Utomo", "Bagas Kara", "Ilham Akbar", "Danang Sutrisno",
            "Yanto Kopling", "Rudi Hartono", "Anton Medan", "Doni Salman", "Hendra Gunawan",
            "Tono Martono", "Irfan Hakim", "Yoga Pratama", "Panji Petualang", "Aditya Rahman",
            "Lukman Sardi", "Ferdi Sambo", "Kurnia Mega", "Satria Baja", "Putra Bangsa",
            "Wawan Bakso", "Sigit Rendang", "Heru Kucing", "Tirta Amarta", "Arya Kamandanu",
            "Reza Rahadian", "Guntur Bumi", "Dodi Sudrajat", "Yusuf Mansur", "Rahmat Hidayat"
    };

    private static final String[] FEMALE_NAMES = new String[]{
            // 50 nama 1 kata
            "Siti", "Putri", "Dewi", "Tasya", "Nabila",
            "Salsa", "Melati", "Vina", "Lala", "Nana",
            "Siska", "Kiki", "Ratna", "Lilis", "Dina",
            "Rina", "Wati", "Yuni", "Tati", "Euis",
            "Neneng", "Cici", "Bunga", "Sari", "Lulu",
            "Fifi", "Hani", "Ika", "Maya", "Nia",
            "Rara", "Susi", "Titin", "Ulfa", "Vera",
            "Wulan", "Zahra", "Ayu", "Bella", "Citra",
            "Dita", "Eka", "Feni", "Gita", "Hesti",
            "Indah", "Jeni", "Laras", "Mega", "Tiara",
            // 50 nama 2 kata
            "Siti Nurhalu", "Putri Kerupuk", "Dewi Galon", "Tasya Tupperware", "Nabila Padang",
            "Salsa Sarden", "Melati Microwave", "Vina Vitamin", "Lala Laler", "Nana Nanas",
            "Siska Sambalado", "Kiki Kompor", "Ratna Rice", "Lilis Lemper", "Margaret Baskom",
            "Dina Dispenser", "Rina Rendang", "Wati Wajan", "Yuni Yoghurt", "Tati Teflon",
            "Euis EsTeh", "Neneng Nugget", "Popon Pisang", "Cici Cilok", "Bunga Bakwan",
            "Sari Sandal", "Lulu Lontong", "Fifi Fanta", "Gigi Gorengan", "Hani Handuk",
            "Ika Ikan", "Juju Jengkol", "Kokom Kolak", "Lina Lele", "Maya Martabak",
            "Nia Nastar", "Pipit Pempek", "Rara Rujak", "Susi Soto", "Titin Tahu",
            "Ulfa Uduk", "Vera Vespa", "Wulan Waffle", "Yayuk Yakult", "Zahra Zebra",
            "Ayu Angsa", "Bella Bubur", "Citra Cendol", "Dita Donat", "Tiara Terasi"
    };


    private UUID ownerUuid = null;
    private UUID assignedAnimalUuid = null;
    private UUID currentBathingTargetUuid = null;
    private String currentDutyState = "IDLE"; // "REFILL", "NEED_FOOD", "SWEEPING", "BATHING", "IDLE"
    private int actionCooldown = 0;
    private int bathingStepTicks = 0;
    private BlockPos targetPlantingPos = null;
    private int plantingStuckTicks = 0;

    private static final int FOOD_SLOTS = 8;
    private final SimpleContainer foodInventory = new SimpleContainer(FOOD_SLOTS);

    private static final int FUEL_SLOTS = 8;
    private final SimpleContainer fuelInventory = new SimpleContainer(FUEL_SLOTS);

    private boolean hasWateringCan = false;
    private ItemStack wateringCanStack = ItemStack.EMPTY;
    private ItemStack brushStack = ItemStack.EMPTY;

    private BlockPos currentTrashTarget = null;
    private int trashStuckTicks = 0;
    private int navStuckTicks = 0;
    private double lastNavX = 0.0D;
    private double lastNavZ = 0.0D;
    private BlockPos tempDirtPos = null;
    private int tempDirtTimer = 0;
    private BlockState previousBlockState = null;

    private UUID callTargetUuid = null;
    private int callTicks = 0;
    private int callStuckTicks = 0;
    private int callTeleportStage = 0;
    private double callBestDistSq = Double.MAX_VALUE;

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(FOOD_STOCK_DATA, 0); // Starts with 0 food units (player supplies food)
        builder.define(SWEEPING, false);
        builder.define(HAS_BATHING_TOOLS, false);
        builder.define(BATHING_ACTION, 0);
        builder.define(HAS_ASSIGNED_ANIMAL, false);
        builder.define(IS_MALE, false);
    }

    public ZooZookeeperEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.setPersistenceRequired();

        this.setPathfindingMalus(net.minecraft.world.level.pathfinder.PathType.DOOR_WOOD_CLOSED, 0.0F);
        this.setPathfindingMalus(net.minecraft.world.level.pathfinder.PathType.DOOR_OPEN, 0.0F);
        this.setPathfindingMalus(net.minecraft.world.level.pathfinder.PathType.TRAPDOOR, 0.0F);

        if (!level.isClientSide()) {
            boolean male = this.random.nextBoolean();
            this.entityData.set(IS_MALE, male);
            String[] pool = male ? MALE_NAMES : FEMALE_NAMES;
            int nameIdx = this.random.nextInt(pool.length);
            this.setCustomName(Component.literal(pool[nameIdx]));
            this.setCustomNameVisible(true);
            this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            this.addTag("zootycoon_staff");
            this.addTag("zootycoon_zookeeper");
        }
    }

    @Override
    protected net.minecraft.world.entity.ai.navigation.PathNavigation createNavigation(Level level) {
        return new net.zootycoon.entity.ai.navigation.ZooPathNavigation(this, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.FOLLOW_RANGE, 64.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(0, new net.zootycoon.entity.ai.goal.ZooDoorAndGateInteractGoal(this));
        this.goalSelector.addGoal(1, new net.minecraft.world.entity.ai.goal.AvoidEntityGoal<>(this, ZooHunterEntity.class, 14.0F, 1.1D, 1.35D));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(3, new ZookeeperClaimStrollGoal(this, 0.4));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
    }

    public static class ZookeeperClaimStrollGoal extends WaterAvoidingRandomStrollGoal {
        private final ZooZookeeperEntity zookeeper;

        public ZookeeperClaimStrollGoal(ZooZookeeperEntity mob, double speedModifier) {
            super(mob, speedModifier);
            this.zookeeper = mob;
        }

        @Override
        public boolean canUse() {
            if (this.zookeeper.hasActiveAssignedAnimal() || this.zookeeper.isBeingCalled()) {
                return false;
            }
            return super.canUse();
        }

        @Override
        protected net.minecraft.world.phys.Vec3 getPosition() {
            net.minecraft.world.phys.Vec3 pos = super.getPosition();
            if (pos != null && this.zookeeper.level() instanceof ServerLevel sLevel) {
                ZooClaimManager cm = ZooClaimManager.get(sLevel);
                BlockPos bp = BlockPos.containing(pos.x, pos.y, pos.z);
                boolean inside = (this.zookeeper.ownerUuid != null)
                        ? cm.isClaimedBy(bp, this.zookeeper.ownerUuid)
                        : cm.isInsideAnyClaim(bp);
                if (!inside) {
                    ZooClaim claim = this.zookeeper.findClosestClaim(cm);
                    if (claim != null) {
                        var box = claim.getBox();
                        int minX = Math.min(box.minX() + 2, box.maxX());
                        int maxX = Math.max(box.maxX() - 2, minX);
                        int minZ = Math.min(box.minZ() + 2, box.maxZ());
                        int maxZ = Math.max(box.maxZ() - 2, minZ);
                        int tx = Math.clamp((int) Math.floor(pos.x), minX, maxX);
                        int tz = Math.clamp((int) Math.floor(pos.z), minZ, maxZ);
                        int ty = sLevel.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, tx, tz);
                        return new net.minecraft.world.phys.Vec3(tx + 0.5D, ty, tz + 0.5D);
                    }
                }
            }
            return pos;
        }
    }

    public boolean hasActiveAssignedAnimal() {
        if (this.assignedAnimalUuid == null) {
            if (this.level() instanceof ServerLevel sLevel) {
                for (LivingEntity m : getAssignedFamily(sLevel)) {
                    if (m.isAlive()) {
                        this.assignedAnimalUuid = m.getUUID();
                        this.entityData.set(HAS_ASSIGNED_ANIMAL, true);
                        return true;
                    }
                }
            }
            return false;
        }
        if (this.level() instanceof ServerLevel sLevel) {
            Entity ent = sLevel.getEntity(this.assignedAnimalUuid);
            if (ent instanceof LivingEntity living && living.isAlive()) {
                return true;
            }
            for (LivingEntity m : getAssignedFamily(sLevel)) {
                if (m.isAlive()) {
                    this.assignedAnimalUuid = m.getUUID();
                    this.entityData.set(HAS_ASSIGNED_ANIMAL, true);
                    return true;
                }
            }
            this.assignedAnimalUuid = null;
            this.entityData.set(HAS_ASSIGNED_ANIMAL, false);
        }
        return false;
    }

    public List<LivingEntity> getAssignedFamily(ServerLevel sLevel) {
        if (sLevel == null) return Collections.emptyList();
        List<LivingEntity> family = new ArrayList<>();
        Set<UUID> seen = new HashSet<>();

        // 1. Primary assigned animal
        if (this.assignedAnimalUuid != null) {
            Entity ent = sLevel.getEntity(this.assignedAnimalUuid);
            if (ent == null && sLevel.getServer() != null) {
                for (ServerLevel sl : sLevel.getServer().getAllLevels()) {
                    ent = sl.getEntity(this.assignedAnimalUuid);
                    if (ent != null) break;
                }
            }
            if (ent instanceof LivingEntity l && l.isAlive()) {
                family.add(l);
                seen.add(l.getUUID());
            }
        }

        // 2. Any other living animals in enclosure/sector assigned to this zookeeper
        String myZkTag = "zoo_zk_" + this.getStringUUID();
        UUID myZkUuid = this.getUUID();
        List<LivingEntity> nearby = sLevel.getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(96.0D),
                e -> e.isAlive() && (e.getTags().contains(myZkTag) || myZkUuid.equals(ZooAnimalHungerManager.getAssignedZookeeper(e)))
        );
        for (LivingEntity e : nearby) {
            if (seen.add(e.getUUID())) {
                family.add(e);
            }
        }

        // If primary was missing or dead, promote first valid member
        if (!family.isEmpty() && (this.assignedAnimalUuid == null || !seen.contains(this.assignedAnimalUuid))) {
            this.assignedAnimalUuid = family.get(0).getUUID();
            this.entityData.set(HAS_ASSIGNED_ANIMAL, true);
        }

        // 3. Any offspring belonging to any member in family
        // Search around each parent (64 blocks) AND around the zookeeper (96 blocks)
        List<LivingEntity> offspringToAdd = new ArrayList<>();
        Set<EntityType<?>> familySpecies = new HashSet<>();
        for (LivingEntity member : family) {
            familySpecies.add(member.getType());
        }

        // Look around the zookeeper for any baby animals of the same species or with offspring tags
        List<LivingEntity> potentialBabies = sLevel.getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(96.0D),
                e -> e.isAlive() && !seen.contains(e.getUUID()) && (
                        familySpecies.contains(e.getType()) && (
                                (e instanceof AgeableMob ageable && ageable.isBaby())
                                || (e instanceof Animal an && an.isBaby())
                                || e.getTags().contains("zoo_offspring")
                        )
                )
        );
        for (LivingEntity baby : potentialBabies) {
            if (seen.add(baby.getUUID())) {
                offspringToAdd.add(baby);
                if (!ZooAnimalHungerManager.isTagged(baby)) {
                    ZooAnimalHungerManager.setTagged(baby, true);
                    if (this.ownerUuid != null) {
                        ZooAnimalHungerManager.setOwner(baby, this.ownerUuid);
                    }
                    ZooAnimalHungerManager.setAssignedZookeeper(baby, this.getUUID());
                }
            }
        }

        // Also check around each parent for specific parent tags or baby of same species
        for (LivingEntity parent : family) {
            String parentTag = "zoo_parent_" + parent.getStringUUID();
            final String parentUuidStr = parent.getStringUUID();
            List<LivingEntity> offspring = sLevel.getEntitiesOfClass(
                    LivingEntity.class,
                    parent.getBoundingBox().inflate(64.0D),
                    e -> e.isAlive() && !seen.contains(e.getUUID()) && (
                            e.getTags().contains(parentTag)
                            || parentUuidStr.equals(ZooAnimalHungerManager.getParentUuidStr(e))
                            || (e.getType().equals(parent.getType()) && ((e instanceof AgeableMob a && a.isBaby()) || (e instanceof Animal an && an.isBaby())))
                    )
            );
            for (LivingEntity off : offspring) {
                if (seen.add(off.getUUID())) {
                    offspringToAdd.add(off);
                    if (!ZooAnimalHungerManager.isTagged(off)) {
                        ZooAnimalHungerManager.setTagged(off, true);
                        if (this.ownerUuid != null) {
                            ZooAnimalHungerManager.setOwner(off, this.ownerUuid);
                        }
                        ZooAnimalHungerManager.setAssignedZookeeper(off, this.getUUID());
                    }
                }
            }
        }
        family.addAll(offspringToAdd);
        return family;
    }

    public boolean isMale() {
        return this.entityData.get(IS_MALE);
    }

    public void setIsMale(boolean male) {
        this.entityData.set(IS_MALE, male);
    }

    /**
     * Picks a name from the 100-name pool (matching gender) that no other zookeeper in the game currently uses.
     * Duplicates are only allowed when every name in the pool is already taken.
     */
    public void assignUniqueName(ServerLevel sLevel) {
        java.util.Set<String> used = new java.util.HashSet<>();
        if (sLevel.getServer() != null) {
            for (ServerLevel lvl : sLevel.getServer().getAllLevels()) {
                for (Entity e : lvl.getAllEntities()) {
                    if (e != this && e instanceof ZooZookeeperEntity other && other.getCustomName() != null) {
                        used.add(other.getCustomName().getString());
                    }
                }
            }
        }
        String[] pool = isMale() ? MALE_NAMES : FEMALE_NAMES;
        java.util.List<String> free = new java.util.ArrayList<>();
        for (String n : pool) {
            if (!used.contains(n)) free.add(n);
        }
        String chosen = free.isEmpty()
                ? pool[this.random.nextInt(pool.length)]
                : free.get(this.random.nextInt(free.size()));
        this.setCustomName(Component.literal(chosen));
        this.setCustomNameVisible(true);
    }

    public boolean isBeingCalled() {
        return this.callTargetUuid != null && this.callTicks > 0;
    }

    /** Sends this idle zookeeper walking to the player; teleports a bit closer if the path gets stuck. */
    public void callToPlayer(ServerPlayer player) {
        if (hasActiveAssignedAnimal()) return;
        this.callTargetUuid = player.getUUID();
        this.callTicks = 600;
        this.callStuckTicks = 0;
        this.callTeleportStage = 0;
        this.callBestDistSq = Double.MAX_VALUE;
        this.currentTrashTarget = null;
        this.trashStuckTicks = 0;
        if (this.level() instanceof ServerLevel sLevel && tempDirtPos != null) {
            removeTempDirt(sLevel);
        }
        this.entityData.set(SWEEPING, false);
        if (this.getMainHandItem().is(ZooItems.BROOM)) {
            this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }
        this.currentDutyState = "IDLE";
        this.getNavigation().stop();
    }

    private void finishCall() {
        UUID prevTarget = this.callTargetUuid;
        this.callTicks = 0;
        this.callTargetUuid = null;
        this.callStuckTicks = 0;
        this.getNavigation().stop();
        if (prevTarget != null && this.level() instanceof ServerLevel sLevel) {
            ServerPlayer target = sLevel.getServer().getPlayerList().getPlayer(prevTarget);
            if (target != null) {
                ZooNetworkPayloads.sendAnimalSync(target);
            }
        }
    }

    private void tickCall(ServerLevel sLevel) {
        ServerPlayer target = callTargetUuid == null ? null : sLevel.getServer().getPlayerList().getPlayer(callTargetUuid);
        if (target == null || target.level() != sLevel || hasActiveAssignedAnimal()) {
            finishCall();
            return;
        }
        callTicks--;
        double distSq = this.distanceToSqr(target);
        if (distSq <= 9.0D) {
            this.getLookControl().setLookAt(target, 30.0F, 30.0F);
            sLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 1.2D, this.getZ(), 6, 0.3, 0.4, 0.3, 0.02);
            finishCall();
            return;
        }
        if (callTicks <= 0) {
            teleportNearPlayer(sLevel, target, 2.5D);
            finishCall();
            return;
        }
        if (this.tickCount % 10 == 0) {
            this.getNavigation().moveTo(target, 1.2D);
        }
        if (this.tickCount % 20 == 0) {
            if (distSq > callBestDistSq - 1.0D) {
                callStuckTicks += 20;
            } else {
                callBestDistSq = distSq;
                callStuckTicks = 0;
            }
            if (callStuckTicks >= 60) {
                teleportNearPlayer(sLevel, target, callTeleportStage == 0 ? 7.0D : 2.5D);
                callTeleportStage++;
                callStuckTicks = 0;
                callBestDistSq = this.distanceToSqr(target);
            }
        }
    }

    private void teleportNearPlayer(ServerLevel sLevel, ServerPlayer target, double dist) {
        double dx = this.getX() - target.getX();
        double dz = this.getZ() - target.getZ();
        double len = Math.sqrt(dx * dx + dz * dz);
        if (len < 0.001D) { dx = 1.0D; dz = 0.0D; len = 1.0D; }
        double tx = target.getX() + dx / len * dist;
        double tz = target.getZ() + dz / len * dist;
        int baseY = target.blockPosition().getY();
        for (int dy = 3; dy >= -4; dy--) {
            BlockPos feet = BlockPos.containing(tx, baseY + dy, tz);
            if (sLevel.getBlockState(feet.below()).isSolid()
                    && sLevel.getBlockState(feet).getCollisionShape(sLevel, feet).isEmpty()
                    && sLevel.getBlockState(feet.above()).getCollisionShape(sLevel, feet.above()).isEmpty()) {
                this.teleportTo(tx, feet.getY(), tz);
                this.getNavigation().stop();
                return;
            }
        }
        this.teleportTo(target.getX(), target.getY(), target.getZ());
        this.getNavigation().stop();
    }

    /** Dismisses this zookeeper: drops carried items and removes the entity. */
    public void dismiss(ServerLevel sLevel) {
        removeTempDirt(sLevel);
        Containers.dropContents(sLevel, this, this.foodInventory);
        Containers.dropContents(sLevel, this, this.fuelInventory);
        sLevel.sendParticles(ParticleTypes.POOF, this.getX(), this.getY() + 1.0D, this.getZ(), 12, 0.3, 0.5, 0.3, 0.02);
        this.discard();
    }

    public ZooClaim findClosestClaim(ZooClaimManager claimManager) {
        java.util.Collection<ZooClaim> claims = (ownerUuid != null)
                ? claimManager.getPlayerClaims(ownerUuid)
                : claimManager.getAllClaims();
        if (claims.isEmpty() && ownerUuid != null) {
            claims = claimManager.getAllClaims();
        }
        ZooClaim closest = null;
        double minDistanceSq = Double.MAX_VALUE;
        BlockPos currentPos = this.blockPosition();
        for (ZooClaim claim : claims) {
            var box = claim.getBox();
            int cx = (box.minX() + box.maxX()) / 2;
            int cz = (box.minZ() + box.maxZ()) / 2;
            double d = currentPos.distSqr(new net.minecraft.core.Vec3i(cx, currentPos.getY(), cz));
            if (d < minDistanceSq) {
                minDistanceSq = d;
                closest = claim;
            }
        }
        return closest;
    }

    public boolean isOnStrike() {
        if (!(this.level() instanceof ServerLevel sLevel)) return false;
        if (this.ownerUuid == null) {
            net.zootycoon.claim.ZooClaim nearest = findClosestClaim(ZooClaimManager.get(sLevel));
            if (nearest != null) {
                this.ownerUuid = nearest.getOwnerUuid();
            }
        }
        return this.ownerUuid != null && net.zootycoon.economy.ZooStaffManager.get(sLevel).isStaffEntityOnStrike(this.ownerUuid, this.getUUID());
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) return;
        ServerLevel sLevel = (ServerLevel) this.level();

        if (actionCooldown > 0) actionCooldown--;

        if (this.tickCount % 20 == 0) {
            var moveAttr = this.getAttribute(Attributes.MOVEMENT_SPEED);
            if (moveAttr != null) {
                var modId = net.zootycoon.ZooTycoonMod.id("unpaid_slowdown");
                int delayTier = net.zootycoon.economy.ZooStaffManager.get(sLevel).getStaffDelayTier(this.getUUID());
                if (delayTier >= 2 && !isOnStrike()) {
                    if (!moveAttr.hasModifier(modId)) {
                        moveAttr.addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                                modId, -0.25D, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                    }
                } else if (moveAttr.hasModifier(modId)) {
                    moveAttr.removeModifier(modId);
                }
            }
        }

        // Player call: walk/teleport to the player even when on strike so player can pay via dialog
        if (callTicks > 0) {
            if (isOnStrike() && this.tickCount % 60 == 0) {
                sLevel.sendParticles(ParticleTypes.ANGRY_VILLAGER, this.getX(), this.getY() + 1.2D, this.getZ(), 1, 0.1, 0.1, 0.1, 0.0);
            }
            tickCall(sLevel);
            return;
        }

        if (isOnStrike()) {
            this.getNavigation().stop();
            if (this.tickCount % 60 == 0) {
                sLevel.sendParticles(ParticleTypes.ANGRY_VILLAGER, this.getX(), this.getY() + 1.2D, this.getZ(), 1, 0.1, 0.1, 0.1, 0.0);
            }
            return;
        }

        // Temporary dirt expiration countdown
        if (tempDirtPos != null) {
            tempDirtTimer--;
            if (tempDirtTimer <= 0) {
                removeTempDirt(sLevel);
            }
        }

        // Active litter cleaning and stuck detection for unassigned zookeepers
        if (this.assignedAnimalUuid == null && currentTrashTarget != null) {
            if (!sLevel.getBlockState(currentTrashTarget).is(ZooBlocks.TRASH)) {
                currentTrashTarget = null;
                trashStuckTicks = 0;
            } else {
                double distSq = this.distanceToSqr(currentTrashTarget.getX() + 0.5, currentTrashTarget.getY() + 0.5, currentTrashTarget.getZ() + 0.5);
                double reach = (tempDirtPos != null) ? 4.8 : 2.4;
                if (distSq <= reach * reach) {
                    sweepLitterAt(sLevel, currentTrashTarget);
                } else if (tempDirtPos == null && distSq <= 4.8 * 4.8) {
                    boolean navStuck = this.getNavigation().isDone() || this.getDeltaMovement().horizontalDistanceSqr() < 0.0008;
                    if (navStuck) {
                        trashStuckTicks++;
                        if (trashStuckTicks >= 25) {
                            tryPlaceTemporaryDirt(sLevel);
                        }
                    } else if (trashStuckTicks > 0) {
                        trashStuckTicks--;
                    }
                }
            }
        } else if (this.assignedAnimalUuid != null && tempDirtPos != null) {
            removeTempDirt(sLevel);
            currentTrashTarget = null;
            trashStuckTicks = 0;
        }

        // If elevated on temporary dirt, keep mob centered on dirt and look towards trash
        if (tempDirtPos != null) {
            this.getNavigation().stop();
            if (currentTrashTarget != null) {
                this.getLookControl().setLookAt(currentTrashTarget.getX() + 0.5, currentTrashTarget.getY() + 0.5, currentTrashTarget.getZ() + 0.5);
            }
        }

        // Sanitize legacy custom name if it contains " - Zookeeper" or "Zookeeper"
        Component cn = this.getCustomName();
        if (cn != null) {
            String str = cn.getString();
            if (str.contains(" - Zookeeper") || str.contains("Zookeeper - ") || str.equalsIgnoreCase("Zookeeper")) {
                String cleaned = str.replace(" - Zookeeper", "").replace("Zookeeper - ", "").trim();
                if (cleaned.isEmpty() || cleaned.equalsIgnoreCase("Zookeeper")) {
                    String[] pool = isMale() ? MALE_NAMES : FEMALE_NAMES;
                    cleaned = pool[Math.abs(this.getUUID().hashCode()) % pool.length];
                }
                this.setCustomName(Component.literal(cleaned));
            }
        }

        ZooClaimManager claimManager = ZooClaimManager.get(sLevel);
        if (ownerUuid == null && this.tickCount % 20 == 0) {
            ZooClaim nearest = findClosestClaim(claimManager);
            if (nearest != null) {
                ownerUuid = nearest.getOwnerUuid();
            }
        }

        // Restrict strictly to claimed zoo area
        BlockPos currentPos = this.blockPosition();
        boolean inside = (ownerUuid != null)
                ? claimManager.isClaimedBy(currentPos, ownerUuid)
                : claimManager.isInsideAnyClaim(currentPos);
        if (!inside) {
            if (this.tickCount % 10 == 0) {
                returnToClaimedLand(sLevel, claimManager);
            }
        } else if (this.getNavigation().isInProgress()) {
            BlockPos targetPos = this.getNavigation().getTargetPos();
            if (targetPos != null) {
                boolean targetInside = (ownerUuid != null)
                        ? claimManager.isClaimedBy(targetPos, ownerUuid)
                        : claimManager.isInsideAnyClaim(targetPos);
                if (!targetInside) {
                    this.getNavigation().stop();
                }
            }

            // Anti-stuck and jumping suppression against furniture, toilets, and gacha blocks
            double movedDistSq = (this.getX() - lastNavX) * (this.getX() - lastNavX)
                    + (this.getZ() - lastNavZ) * (this.getZ() - lastNavZ);
            if (movedDistSq < 0.0009D) {
                navStuckTicks++;
                if (navStuckTicks >= 6) {
                    // Suppress continuous jump bouncing when blocked
                    if (this.onGround() && this.getDeltaMovement().y > 0.0D) {
                        this.setDeltaMovement(this.getDeltaMovement().x, 0.0D, this.getDeltaMovement().z);
                    }

                    // Check for clear walkway to the left or right of facing direction
                    net.minecraft.core.Direction facing = this.getDirection();
                    BlockPos myPos = this.blockPosition();
                    net.minecraft.core.Direction left = facing.getCounterClockWise();
                    net.minecraft.core.Direction right = facing.getClockWise();

                    boolean leftClear = isPassableFloor(sLevel, myPos.relative(left));
                    boolean rightClear = isPassableFloor(sLevel, myPos.relative(right));

                    if (leftClear || rightClear) {
                        net.minecraft.core.Direction escapeDir = leftClear ? left : right;
                        double nudgeX = escapeDir.getStepX() * 0.12D;
                        double nudgeZ = escapeDir.getStepZ() * 0.12D;
                        this.setDeltaMovement(this.getDeltaMovement().x + nudgeX, this.getDeltaMovement().y, this.getDeltaMovement().z + nudgeZ);
                    }

                    if (navStuckTicks % 12 == 0) {
                        this.getNavigation().recomputePath();
                    }

                    if (navStuckTicks >= 35) {
                        this.getNavigation().stop();
                        navStuckTicks = 0;
                    }
                }
            } else {
                navStuckTicks = 0;
            }
            lastNavX = this.getX();
            lastNavZ = this.getZ();
        } else {
            navStuckTicks = 0;
            lastNavX = this.getX();
            lastNavZ = this.getZ();
        }

        // Run active bathing or planting duty every tick, or routine duties every 20 ticks
        if ("BATHING".equals(this.currentDutyState)) {
            tickBathingDuty(sLevel);
        } else if ("PLANTING_VEGETATION".equals(this.currentDutyState)) {
            if (actionCooldown <= 0) {
                tickPlantingDuty(sLevel);
            }
        } else if (this.tickCount % 20 == 0 && actionCooldown <= 0) {
            int delayTier = net.zootycoon.economy.ZooStaffManager.get(sLevel).getStaffDelayTier(this.getUUID());
            if (delayTier < 2 || this.tickCount % 40 == 0) {
                handleStaffDuties(sLevel);
            }
        }
    }

    private boolean isPassableFloor(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        BlockState above = level.getBlockState(pos.above());
        BlockState ground = level.getBlockState(pos.below());

        if (state.isAir() || !state.blocksMotion()) {
            if (above.isAir() || !above.blocksMotion()) {
                return ground.isSolid() && !net.zootycoon.util.ZooSeatHelper.isSeatableBlockState(ground)
                        && !ground.is(ZooBlocks.TOILET) && !ground.is(ZooBlocks.GACHA_MACHINE);
            }
        }
        return false;
    }

    private void returnToClaimedLand(ServerLevel sLevel, ZooClaimManager claimManager) {
        ZooClaim closestClaim = findClosestClaim(claimManager);
        if (closestClaim == null) return;

        var box = closestClaim.getBox();
        BlockPos currentPos = this.blockPosition();
        int minX = Math.min(box.minX() + 2, box.maxX());
        int maxX = Math.max(box.maxX() - 2, minX);
        int minZ = Math.min(box.minZ() + 2, box.maxZ());
        int maxZ = Math.max(box.maxZ() - 2, minZ);
        int targetX = Math.clamp(currentPos.getX(), minX, maxX);
        int targetZ = Math.clamp(currentPos.getZ(), minZ, maxZ);
        int targetY = sLevel.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, targetX, targetZ);
        BlockPos safePos = new BlockPos(targetX, targetY, targetZ);

        double distToSafe = this.distanceToSqr(safePos.getX() + 0.5, safePos.getY(), safePos.getZ() + 0.5);
        if (distToSafe > 10.0 * 10.0) {
            this.teleportTo(safePos.getX() + 0.5, safePos.getY(), safePos.getZ() + 0.5);
            this.getNavigation().stop();
        } else {
            this.getNavigation().moveTo(safePos.getX() + 0.5, safePos.getY(), safePos.getZ() + 0.5, 1.25);
        }
    }

    private void handleStaffDuties(ServerLevel sLevel) {
        ZooClaimManager claimManager = ZooClaimManager.get(sLevel);
        BlockPos myPos = this.blockPosition();

        // Check if assigned animal is valid and alive
        LivingEntity assignedAnimal = null;
        if (this.assignedAnimalUuid != null) {
            Entity ent = sLevel.getEntity(this.assignedAnimalUuid);
            if (ent == null && sLevel.getServer() != null) {
                for (ServerLevel sl : sLevel.getServer().getAllLevels()) {
                    ent = sl.getEntity(this.assignedAnimalUuid);
                    if (ent != null) break;
                }
            }
            if (ent instanceof LivingEntity living && living.isAlive()) {
                assignedAnimal = living;
            } else {
                for (LivingEntity m : getAssignedFamily(sLevel)) {
                    if (m.isAlive()) {
                        assignedAnimal = m;
                        this.assignedAnimalUuid = m.getUUID();
                        this.entityData.set(HAS_ASSIGNED_ANIMAL, true);
                        break;
                    }
                }
                if (assignedAnimal == null) {
                    this.assignedAnimalUuid = null;
                    this.entityData.set(HAS_ASSIGNED_ANIMAL, false);
                }
            }
        }

        // ========================================================
        // 1. Zookeepers with an assigned animal will NEVER sweep trash!
        // Only unassigned zookeepers sweep and clean visitor litter.
        // ========================================================
        if (assignedAnimal == null) {
            BlockPos nearestTrash = null;
            double minTrashDistSq = Double.MAX_VALUE;

            for (int dx = -14; dx <= 14; dx++) {
                for (int dz = -14; dz <= 14; dz++) {
                    for (int dy = -3; dy <= 3; dy++) {
                        BlockPos p = myPos.offset(dx, dy, dz);
                        if (sLevel.getBlockState(p).is(ZooBlocks.TRASH)) {
                            if (ownerUuid != null && !claimManager.isClaimedBy(p, ownerUuid)) continue;
                            double d = myPos.distSqr(p);
                            if (d < minTrashDistSq) {
                                minTrashDistSq = d;
                                nearestTrash = p.immutable();
                            }
                        }
                    }
                }
            }

            if (nearestTrash != null) {
                currentTrashTarget = nearestTrash;
                currentDutyState = "SWEEPING";
                this.entityData.set(SWEEPING, true);
                // Hold broom in hand
                if (!this.getMainHandItem().is(ZooItems.BROOM)) {
                    this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ZooItems.BROOM));
                }

                double distSq = this.distanceToSqr(nearestTrash.getX() + 0.5, nearestTrash.getY() + 0.5, nearestTrash.getZ() + 0.5);
                double reach = (tempDirtPos != null) ? 4.8 : 2.4;
                if (distSq <= reach * reach) {
                    sweepLitterAt(sLevel, nearestTrash);
                    return;
                }

                if (tempDirtPos == null) {
                    this.getNavigation().moveTo(nearestTrash.getX() + 0.5, nearestTrash.getY() + 0.5, nearestTrash.getZ() + 0.5, 1.15);
                } else {
                    this.getNavigation().stop();
                }
                return;
            }

            // Cleaned or no trash: remove broom and stay idle/strolling
            currentTrashTarget = null;
            trashStuckTicks = 0;
            if (tempDirtPos != null) {
                removeTempDirt(sLevel);
            }
            if (this.getMainHandItem().is(ZooItems.BROOM)) {
                this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            }
            this.entityData.set(SWEEPING, false);
            currentDutyState = "IDLE";
            return;
        }

        // Assigned animal is present: NEVER sweep trash! Put away broom if held
        if (this.getMainHandItem().is(ZooItems.BROOM)) {
            this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }
        this.entityData.set(SWEEPING, false);

        // ========================================================
        // 2a. PRIORITY 2a: Thermal Management for assigned animal or offspring
        // Checks if animal is freezing or overheating
        // ========================================================
        if (assignedAnimal != null) {
            LivingEntity thermalTarget = assignedAnimal;
            for (LivingEntity member : getAssignedFamily(sLevel)) {
                HabitatProfile p = HabitatProfileLoader.getProfile(member);
                if (p != null) {
                    var memberThermal = ZooClimate.evaluateThermalState(sLevel, member.blockPosition(), p);
                    if (memberThermal.tooCold() || memberThermal.tooHot()) {
                        thermalTarget = member;
                        break;
                    }
                }
            }

            HabitatProfile habProfile = HabitatProfileLoader.getProfile(thermalTarget);
            if (habProfile != null) {
                var thermalState = ZooClimate.evaluateThermalState(sLevel, thermalTarget.blockPosition(), habProfile);
                boolean tooCold = thermalState.tooCold();
                boolean tooHot = thermalState.tooHot();

                if (tooCold) {
                    BlockPos nearestHeater = findNearestHeater(sLevel, thermalTarget.blockPosition(), 20.0D);
                    if (nearestHeater != null) {
                        if (hasCoal()) {
                            currentDutyState = "REFUEL_HEATER";
                            ItemStack coalItem = peekCoal();
                            this.setItemSlot(EquipmentSlot.MAINHAND, coalItem.copy());

                            double distSq = this.distanceToSqr(nearestHeater.getX() + 0.5D, nearestHeater.getY() + 0.5D, nearestHeater.getZ() + 0.5D);
                            if (distSq > 2.5D * 2.5D) {
                                this.getNavigation().moveTo(nearestHeater.getX() + 0.5D, nearestHeater.getY(), nearestHeater.getZ() + 0.5D, 1.25D);
                                return;
                            } else {
                                this.getNavigation().stop();
                                this.getLookControl().setLookAt(nearestHeater.getX() + 0.5D, nearestHeater.getY() + 0.5D, nearestHeater.getZ() + 0.5D, 30.0F, 30.0F);
                                this.swing(InteractionHand.MAIN_HAND);

                                ItemStack taken = takeCoal(1);
                                int fuelTicks = ZooHeaterBlock.getFuelTicks(taken);
                                if (fuelTicks > 0 && sLevel.getBlockEntity(nearestHeater) instanceof ZooThermalBlockEntity thermal) {
                                    thermal.addFuel(fuelTicks);
                                    sLevel.playSound(null, nearestHeater, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                                    sLevel.sendParticles(ParticleTypes.FLAME, nearestHeater.getX() + 0.5D, nearestHeater.getY() + 0.5D, nearestHeater.getZ() + 0.5D, 8, 0.2, 0.2, 0.2, 0.02);
                                    actionCooldown = 40;
                                    this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                                    return;
                                }
                            }
                        } else {
                            currentDutyState = "NEED_COAL";
                        }
                    } else {
                        currentDutyState = "NEED_HEATER_NEARBY";
                    }
                } else if (tooHot) {
                    BlockPos nearestCooler = findNearestCooler(sLevel, thermalTarget.blockPosition(), 20.0D);
                    if (nearestCooler != null) {
                        if (hasIce()) {
                            currentDutyState = "REFUEL_COOLER";
                            ItemStack iceItem = peekIce();
                            this.setItemSlot(EquipmentSlot.MAINHAND, iceItem.copy());

                            double distSq = this.distanceToSqr(nearestCooler.getX() + 0.5D, nearestCooler.getY() + 0.5D, nearestCooler.getZ() + 0.5D);
                            if (distSq > 2.5D * 2.5D) {
                                this.getNavigation().moveTo(nearestCooler.getX() + 0.5D, nearestCooler.getY(), nearestCooler.getZ() + 0.5D, 1.25D);
                                return;
                            } else {
                                this.getNavigation().stop();
                                this.getLookControl().setLookAt(nearestCooler.getX() + 0.5D, nearestCooler.getY() + 0.5D, nearestCooler.getZ() + 0.5D, 30.0F, 30.0F);
                                this.swing(InteractionHand.MAIN_HAND);

                                ItemStack taken = takeIce(1);
                                int coolTicks = ZooCoolerBlock.getCoolingTicks(taken);
                                if (coolTicks > 0 && sLevel.getBlockEntity(nearestCooler) instanceof ZooThermalBlockEntity thermal) {
                                    thermal.addFuel(coolTicks);
                                    sLevel.playSound(null, nearestCooler, SoundEvents.GLASS_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
                                    sLevel.sendParticles(ParticleTypes.SNOWFLAKE, nearestCooler.getX() + 0.5D, nearestCooler.getY() + 0.8D, nearestCooler.getZ() + 0.5D, 8, 0.2, 0.2, 0.2, 0.02);
                                    actionCooldown = 40;
                                    this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                                    return;
                                }
                            }
                        } else {
                            currentDutyState = "NEED_ICE";
                        }
                    } else {
                        currentDutyState = "NEED_COOLER_NEARBY";
                    }
                }
            }
        }

        if (!"REFUEL_HEATER".equals(currentDutyState) && !"REFUEL_COOLER".equals(currentDutyState)) {
            if (hasCoal() && this.getMainHandItem().is(peekCoal().getItem())) {
                this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            } else if (hasIce() && this.getMainHandItem().is(peekIce().getItem())) {
                this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            }
        }

        // ========================================================
        // 2b. PRIORITY 2b: Bathe assigned animal or its offspring if dirty (cleanliness <= 60%) and tools are available
        // ========================================================
        LivingEntity dirtyTarget = null;
        int lowestCleanliness = 60;
        if (assignedAnimal != null) {
            for (LivingEntity member : getAssignedFamily(sLevel)) {
                int c = ZooAnimalHungerManager.getCleanliness(member);
                if (c <= 60 && c <= lowestCleanliness) {
                    lowestCleanliness = c;
                    dirtyTarget = member;
                }
            }
        }

        if (dirtyTarget != null) {
            if (this.hasBathingTools()) {
                currentDutyState = "BATHING";
                this.currentBathingTargetUuid = dirtyTarget.getUUID();
                tickBathingDuty(sLevel, dirtyTarget);
                return;
            } else {
                currentDutyState = "NEED_BATHING_TOOLS";
            }
        }

        // If not bathing, clear bathing tools from hand if held
        if (!"BATHING".equals(currentDutyState)) {
            if (this.getBathingAction() != 0) {
                this.setBathingAction(0);
            }
            bathingStepTicks = 0;
            if (this.getMainHandItem().is(Items.BRUSH) || net.zootycoon.animal.AnimalBathingManager.isWateringCan(this.getMainHandItem())) {
                this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            }
        }

        // ========================================================
        // 2b. PRIORITY 2b: Feeding Duty for assigned animal and offspring
        // Handles both Thrown Food (Carnivores/Predators) and Trough (Herbivores/Omnivores)
        // ========================================================
        if (assignedAnimal != null) {
            var diet = AnimalDietRegistry.getDiet(assignedAnimal);
            boolean isGrazing = (diet != null && diet.feedingType == AnimalDietRegistry.FeedingType.GRAZING);
            boolean isThrownMeat = (diet != null && diet.feedingType == AnimalDietRegistry.FeedingType.THROWN_MEAT);

            if (isGrazing) {
                List<LivingEntity> family = getAssignedFamily(sLevel);
                LivingEntity hungryTarget = null;
                int minHunger = 75;
                for (LivingEntity member : family) {
                    if (ZooAnimalHungerManager.isSick(member)) continue;
                    int h = ZooAnimalHungerManager.getHunger(member);
                    if (h <= 75 && h <= minHunger) {
                        minHunger = h;
                        hungryTarget = member;
                    }
                }

                // 1. If hungry grazing animal exists and keeper carries edible food, feed or throw directly!
                ItemStack carriedFood = this.peekFoodForFamily(family);
                if (hungryTarget != null && !carriedFood.isEmpty()) {
                    currentDutyState = "FEEDING_ANIMAL";
                    Item targetFoodItem = carriedFood.getItem();
                    this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(targetFoodItem));

                    double distSq = this.distanceToSqr(hungryTarget);
                    if (distSq > 4.5 * 4.5) {
                        this.getNavigation().moveTo(hungryTarget, 1.25);
                        return;
                    } else {
                        this.getNavigation().stop();
                        this.getLookControl().setLookAt(hungryTarget, 30.0F, 30.0F);
                        this.swing(InteractionHand.MAIN_HAND);

                        ItemStack takenFood = this.takeFoodForFamily(family, 1);
                        ItemStack feedStack = !takenFood.isEmpty() ? takenFood : new ItemStack(targetFoodItem, 1);

                        if (distSq <= 3.5 * 3.5) {
                            ZooAnimalHungerManager.feedAnimal(hungryTarget, feedStack, null);
                        } else {
                            ItemEntity itemEntity = new ItemEntity(sLevel, this.getX(), this.getEyeY() - 0.2, this.getZ(), feedStack);
                            Vec3 targetPos = hungryTarget.position().add(0, hungryTarget.getBbHeight() * 0.25, 0);
                            Vec3 throwVec = targetPos.subtract(itemEntity.position()).normalize().scale(0.38);
                            itemEntity.setDeltaMovement(throwVec.x, throwVec.y + 0.12, throwVec.z);
                            itemEntity.setDefaultPickUpDelay();
                            sLevel.addFreshEntity(itemEntity);
                        }

                        sLevel.playSound(null, this.blockPosition(), SoundEvents.GENERIC_EAT, SoundSource.NEUTRAL, 0.8f, 1.0f);
                        actionCooldown = 40; // 2 seconds cooldown
                        return;
                    }
                }

                // 2. Grazing animals: Zookeeper replants vegetation in enclosure when carried
                ItemStack carriedVegetation = this.peekMatchingVegetation(diet);
                if (!carriedVegetation.isEmpty()) {
                    BlockPos plantPos = findValidPlantingSpotNearAnimal(sLevel, assignedAnimal, carriedVegetation.getItem());
                    if (plantPos != null) {
                        this.targetPlantingPos = plantPos;
                        this.plantingStuckTicks = 0;
                        this.currentDutyState = "PLANTING_VEGETATION";
                        tickPlantingDuty(sLevel);
                        return;
                    }
                }

                if (hungryTarget != null) {
                    currentDutyState = "NEED_FOOD";
                }
            }

            if (isThrownMeat) {
                // Carnivores / Thrown food: Zookeeper throws food directly to the animal or its hungry offspring
                LivingEntity hungryTarget = null;
                int minHunger = 75;
                for (LivingEntity member : getAssignedFamily(sLevel)) {
                    if (ZooAnimalHungerManager.isSick(member)) continue;
                    int h = ZooAnimalHungerManager.getHunger(member);
                    if (h <= 75 && h <= minHunger) {
                        minHunger = h;
                        hungryTarget = member;
                    }
                }

                if (hungryTarget != null) {
                    if (this.getFoodStock() > 0) {
                        currentDutyState = "FEEDING_ANIMAL";
                        ItemStack heldFood = this.peekFirstFood();
                        Item targetFoodItem = !heldFood.isEmpty() ? heldFood.getItem() : ((diet != null && !diet.acceptedFoods.isEmpty()) ? diet.acceptedFoods.get(0) : Items.BEEF);
                        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(targetFoodItem));

                        double distSq = this.distanceToSqr(hungryTarget);
                        if (distSq > 4.5 * 4.5) {
                            this.getNavigation().moveTo(hungryTarget, 1.25);
                            return;
                        } else {
                            this.getNavigation().stop();
                            this.getLookControl().setLookAt(hungryTarget, 30.0F, 30.0F);
                            this.swing(InteractionHand.MAIN_HAND);

                            ItemStack takenFood = this.takeFirstFood(1);
                            ItemStack throwStack = !takenFood.isEmpty() ? takenFood : new ItemStack(targetFoodItem, 1);
                            ItemEntity itemEntity = new ItemEntity(sLevel, this.getX(), this.getEyeY() - 0.2, this.getZ(), throwStack);
                            Vec3 targetPos = hungryTarget.position().add(0, hungryTarget.getBbHeight() * 0.25, 0);
                            Vec3 throwVec = targetPos.subtract(itemEntity.position()).normalize().scale(0.38);
                            itemEntity.setDeltaMovement(throwVec.x, throwVec.y + 0.12, throwVec.z);
                            itemEntity.setDefaultPickUpDelay();
                            sLevel.addFreshEntity(itemEntity);

                            sLevel.playSound(null, this.blockPosition(), SoundEvents.SNOWBALL_THROW, SoundSource.NEUTRAL, 0.6f, 0.9f + this.getRandom().nextFloat() * 0.2f);
                            actionCooldown = 40; // 2 seconds cooldown between throws
                            return;
                        }
                    } else {
                        currentDutyState = "NEED_FOOD";
                    }
                }
            } else if (requiresFeedingTrough(diet)) {
                // Trough eaters only: NEVER refill troughs for grazing animals, and NEVER refill unassigned troughs
                List<LivingEntity> troughFamily = getAssignedFamily(sLevel);
                ItemStack availableFood = this.peekFoodForFamily(troughFamily);
                Item targetFoodItem = !availableFood.isEmpty() ? availableFood.getItem() : Items.WHEAT;
                if (availableFood.isEmpty()) {
                    var validFoods = AnimalDietRegistry.getFoodsForAnimal(assignedAnimal);
                    if (!validFoods.isEmpty()) {
                        targetFoodItem = validFoods.get(0).getItem();
                    } else if (diet != null && !diet.acceptedFoods.isEmpty()) {
                        targetFoodItem = diet.acceptedFoods.get(0);
                    }
                }

                BlockPos designatedTrough = null;
                double minAssignedDistSq = Double.MAX_VALUE;

                ZooFeedingTroughBlock.syncKnownTroughs(sLevel, myPos, 48);

                for (BlockPos troughPos : ZooFeedingTroughBlock.KNOWN_TROUGHS) {
                    if (ownerUuid != null && !claimManager.isClaimedBy(troughPos, ownerUuid)) continue;
                    double distSq = myPos.distSqr(troughPos);
                    if (distSq > 48 * 48) continue;

                    BlockEntity be = sLevel.getBlockEntity(troughPos);
                    if (be instanceof ZooFeedingTroughBlockEntity troughBe && isTroughEligibleForFamilyRefill(troughBe, troughFamily)) {
                        ItemStack foodInTrough = troughBe.getFoodStack();
                        boolean unusableLeftover = !foodInTrough.isEmpty() && !isFoodEatenByAny(troughFamily, foodInTrough);
                        boolean canRefillWithMyFood = !availableFood.isEmpty() && (
                                foodInTrough.isEmpty() || unusableLeftover
                                        || (foodInTrough.getCount() < 32 && (this.hasMatchingFoodForTrough(foodInTrough) || foodInTrough.is(availableFood.getItem())))
                        );
                        if (canRefillWithMyFood && distSq < minAssignedDistSq) {
                            minAssignedDistSq = distSq;
                            designatedTrough = troughPos;
                        }
                    }
                }

                if (designatedTrough != null) {
                    if (!availableFood.isEmpty()) {
                        currentDutyState = "REFILL";
                        BlockEntity targetBe = sLevel.getBlockEntity(designatedTrough);
                        ItemStack heldItem = availableFood;
                        if (targetBe instanceof ZooFeedingTroughBlockEntity tBe && !tBe.getFoodStack().isEmpty() && isFoodEatenByAny(troughFamily, tBe.getFoodStack())) {
                            heldItem = this.peekMatchingFood(tBe.getFoodStack());
                        }
                        Item itemToHold = !heldItem.isEmpty() ? heldItem.getItem() : targetFoodItem;
                        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(itemToHold));

                        double distSq = this.distanceToSqr(designatedTrough.getX() + 0.5, designatedTrough.getY(), designatedTrough.getZ() + 0.5);
                        if (distSq > 2.5 * 2.5) {
                            this.getNavigation().moveTo(designatedTrough.getX() + 0.5, designatedTrough.getY(), designatedTrough.getZ() + 0.5, 1.2);
                        } else {
                            // Refill the assigned trough with food valid for this animal family
                            BlockEntity be = sLevel.getBlockEntity(designatedTrough);
                            if (be instanceof ZooFeedingTroughBlockEntity troughBe && isTroughEligibleForFamilyRefill(troughBe, troughFamily)) {
                                int toInsert = Math.min(8, this.getFoodStock());
                                ItemStack currentInTrough = troughBe.getFoodStack();
                                if (!currentInTrough.isEmpty() && !isFoodEatenByAny(troughFamily, currentInTrough)) {
                                    ItemStack junk = troughBe.extractFood(64);
                                    if (!junk.isEmpty()) {
                                        sLevel.addFreshEntity(new ItemEntity(sLevel, designatedTrough.getX() + 0.5, designatedTrough.getY() + 1.0, designatedTrough.getZ() + 0.5, junk));
                                    }
                                    currentInTrough = ItemStack.EMPTY;
                                }
                                ItemStack takenFood = !currentInTrough.isEmpty() ? this.takeMatchingFood(currentInTrough, toInsert) : this.takeFoodForFamily(troughFamily, toInsert);
                                if (!takenFood.isEmpty()) {
                                    int initialCount = takenFood.getCount();
                                    boolean inserted = troughBe.insertFood(takenFood, initialCount);
                                    if (inserted) {
                                        if (!takenFood.isEmpty()) {
                                            this.foodInventory.addItem(takenFood);
                                            updateFoodStockFromInventory();
                                        }
                                        sLevel.playSound(null, designatedTrough, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.9f, 1.2f);
                                        sLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, designatedTrough.getX() + 0.5, designatedTrough.getY() + 0.8, designatedTrough.getZ() + 0.5, 6, 0.2, 0.2, 0.2, 0.02);
                                        actionCooldown = 40; // 2 seconds cooldown between refills
                                    } else {
                                        this.foodInventory.addItem(takenFood);
                                        updateFoodStockFromInventory();
                                    }
                                }
                            }
                        }
                        return;
                    } else {
                        currentDutyState = "NEED_FOOD";
                    }
                }
            }
        }

        // If not feeding/refilling, clear food from hand if held
        if ("REFILL".equals(currentDutyState) || "FEEDING_ANIMAL".equals(currentDutyState)) {
            currentDutyState = "IDLE";
            this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }

        // ========================================================
        // 3. PRIORITY 3: Follow Assigned Animal (Natural 5-block distance)
        // When animal is full (> 25% hunger), zookeeper follows its animal
        // ========================================================
        if (assignedAnimal != null) {
            currentDutyState = "FOLLOW_ANIMAL";
            if (!this.getMainHandItem().isEmpty() && !this.getMainHandItem().is(ZooItems.BROOM)) {
                this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            }

            LivingEntity targetToFollow = assignedAnimal;
            double closestDistSq = this.distanceToSqr(assignedAnimal);
            for (LivingEntity member : getAssignedFamily(sLevel)) {
                double d = this.distanceToSqr(member);
                if (d < closestDistSq) {
                    closestDistSq = d;
                    targetToFollow = member;
                }
            }

            this.getLookControl().setLookAt(targetToFollow, 30.0F, 30.0F);

            // Natural 5-block following distance:
            // Move closer if further than 6 blocks, stop when at or within 5 blocks
            if (closestDistSq > 6.0 * 6.0) {
                this.getNavigation().moveTo(targetToFollow, 1.05D);
            } else if (closestDistSq <= 5.0 * 5.0) {
                this.getNavigation().stop();
            }
            return;
        }

        // ========================================================
        // 4. PRIORITY 4: No assigned animal, idle patrol
        // ========================================================
        currentDutyState = "IDLE";
        this.entityData.set(SWEEPING, false);
        if (this.getMainHandItem().is(ZooItems.BROOM)) {
            this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.level().isClientSide() && hand == InteractionHand.MAIN_HAND && player instanceof ServerPlayer sp) {
            if (this.ownerUuid == null) {
                this.ownerUuid = player.getUUID();
            }

            openDialogScreen(sp, null);
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    public void openDialogScreen(ServerPlayer player, String dialogOverride) {
        if (isOnStrike()) {
            this.level().playSound(null, this.blockPosition(), SoundEvents.VILLAGER_NO, SoundSource.NEUTRAL, 1.0f, 1.0f);
            ServerPlayNetworking.send(player, new ZooNetworkPayloads.OpenStaffStrikeScreenPayload(
                    this.getId(),
                    this.getUUID(),
                    this.getName().getString(),
                    "zootycoon.dialog.zookeeper.role"
            ));
            return;
        }

        LivingEntity animal = null;
        if (assignedAnimalUuid != null && this.level() instanceof ServerLevel sLevel) {
            Entity ent = sLevel.getEntity(assignedAnimalUuid);
            if (ent instanceof LivingEntity l) animal = l;
        }

        String keeperName = this.getName().getString();
        boolean hasAnimal = animal != null;
        String animalName = hasAnimal ? (animal.hasCustomName() ? animal.getCustomName().getString() : animal.getType().getDescription().getString()) : "-";
        int hunger = hasAnimal ? ZooAnimalHungerManager.getHunger(animal) : 0;

        LivingEntity mostUrgentAnimal = animal;
        int lowestHunger = hunger;
        if (hasAnimal && this.level() instanceof ServerLevel sLevel) {
            for (LivingEntity member : getAssignedFamily(sLevel)) {
                int mh = ZooAnimalHungerManager.getHunger(member);
                if (mh < lowestHunger) {
                    lowestHunger = mh;
                    mostUrgentAnimal = member;
                }
            }
        }
        hunger = lowestHunger;
        String dialogAnimalName = (mostUrgentAnimal != null && mostUrgentAnimal.hasCustomName())
                ? mostUrgentAnimal.getCustomName().getString()
                : (mostUrgentAnimal != null ? mostUrgentAnimal.getType().getDescription().getString() : animalName);
        if (mostUrgentAnimal != null && mostUrgentAnimal != animal && mostUrgentAnimal.getTags().contains("zoo_offspring") && !mostUrgentAnimal.hasCustomName()) {
            dialogAnimalName = "Anak " + dialogAnimalName;
        }

        List<String> foods = new ArrayList<>();
        if (hasAnimal) {
            var diet = AnimalDietRegistry.getDiet(animal);
            for (Item item : diet.acceptedFoods) {
                foods.add(BuiltInRegistries.ITEM.getKey(item).toString());
            }
        }

        String dialogText = "";
        if (dialogOverride != null) {
            dialogText = dialogOverride;
        } else if (!hasAnimal) {
            dialogText = ZooTranslations.get("zootycoon.dialog.zookeeper.speech_no_animal");
        } else {
            LivingEntity coldAnimal = null;
            LivingEntity hotAnimal = null;
            LivingEntity dirtyAnimal = null;
            if (this.level() instanceof ServerLevel sLevel) {
                for (LivingEntity m : getAssignedFamily(sLevel)) {
                    HabitatProfile p = HabitatProfileLoader.getProfile(m);
                    if (p != null) {
                        var memberThermal = ZooClimate.evaluateThermalState(sLevel, m.blockPosition(), p);
                        if (memberThermal.tooCold() && coldAnimal == null) coldAnimal = m;
                        if (memberThermal.tooHot() && hotAnimal == null) hotAnimal = m;
                    }
                    if (ZooAnimalHungerManager.getCleanliness(m) <= 60 && dirtyAnimal == null) {
                        dirtyAnimal = m;
                    }
                }
            }
            if (coldAnimal == null && hotAnimal == null) {
                HabitatProfile habProfile = HabitatProfileLoader.getProfile(animal);
                if (habProfile != null) {
                    var thermal = ZooClimate.evaluateThermalState(animal.level(), animal.blockPosition(), habProfile);
                    if (thermal.tooCold()) coldAnimal = animal;
                    if (thermal.tooHot()) hotAnimal = animal;
                }
            }

            if (coldAnimal != null) {
                String cName = coldAnimal.hasCustomName() ? coldAnimal.getCustomName().getString() : (coldAnimal != animal ? "Anak " + animalName : animalName);
                BlockPos heater = findNearestHeater((ServerLevel) this.level(), coldAnimal.blockPosition(), 20.0D);
                if (heater == null) {
                    dialogText = ZooTranslations.get("zootycoon.dialog.zookeeper.need_heater_block", cName);
                } else if (!hasCoal()) {
                    dialogText = ZooTranslations.get("zootycoon.dialog.zookeeper.need_coal", cName);
                }
            } else if (hotAnimal != null) {
                String hName = hotAnimal.hasCustomName() ? hotAnimal.getCustomName().getString() : (hotAnimal != animal ? "Anak " + animalName : animalName);
                BlockPos cooler = findNearestCooler((ServerLevel) this.level(), hotAnimal.blockPosition(), 20.0D);
                if (cooler == null) {
                    dialogText = ZooTranslations.get("zootycoon.dialog.zookeeper.need_cooler_block", hName);
                } else if (!hasIce()) {
                    dialogText = ZooTranslations.get("zootycoon.dialog.zookeeper.need_ice", hName);
                }
            }

            if (dialogText.isEmpty()) {
                if (dirtyAnimal != null && !this.hasBathingTools()) {
                    String dName = dirtyAnimal.hasCustomName() ? dirtyAnimal.getCustomName().getString() : (dirtyAnimal != animal ? "Anak " + animalName : animalName);
                    dialogText = ZooTranslations.get("zootycoon.dialog.zookeeper.need_bathing_tools", dName);
                } else {
                    ChatFormatting hungerColor = hunger <= 25 ? ChatFormatting.RED : (hunger <= 60 ? ChatFormatting.YELLOW : ChatFormatting.GREEN);
                    String hungerColoredStr = hungerColor + String.valueOf(hunger) + "%" + ChatFormatting.RESET;
                    var diet = AnimalDietRegistry.getDiet(animal);
                    boolean isGrazingDiet = (diet != null && diet.feedingType == AnimalDietRegistry.FeedingType.GRAZING);
                    boolean isThrownDiet = (diet != null && diet.feedingType == AnimalDietRegistry.FeedingType.THROWN_MEAT);

                    if (this.getFoodStock() <= 0) {
                        String vegLabel = (diet != null && diet.reforestationItem == Items.BONE_MEAL) ? "Bone Meal" : (diet != null ? diet.missingBlockLabel : "Rumput");
                        dialogText = isGrazingDiet
                                ? (hunger <= 75
                                        ? dialogAnimalName + " sedang lapar (" + hungerColoredStr + ")! Beri saya " + vegLabel + " untuk ditanam atau pakan langsung."
                                        : "Satwa ini mencari makan vegetasi di kandang (" + hungerColoredStr + "). Pastikan " + vegLabel + " atau vegetasi tersedia!")
                                : (isThrownDiet
                                ? ZooTranslations.get("zootycoon.dialog.zookeeper.empty_stock_throw", dialogAnimalName, hungerColoredStr)
                                : ZooTranslations.get("zootycoon.dialog.zookeeper.empty_stock", dialogAnimalName, hungerColoredStr));
                    } else {
                        dialogText = isGrazingDiet
                                ? "Saya sedang merawat & memberi makan " + dialogAnimalName + " (" + hungerColoredStr + ")! Stok: " + this.getFoodStock() + "x."
                                : (isThrownDiet
                                ? ZooTranslations.get("zootycoon.dialog.zookeeper.speech_with_animal_throw", dialogAnimalName, hungerColoredStr, String.valueOf(this.getFoodStock()))
                                : ZooTranslations.get("zootycoon.dialog.zookeeper.speech_with_animal", dialogAnimalName, hungerColoredStr, String.valueOf(this.getFoodStock())));
                    }
                }
            }
        }

        ServerPlayNetworking.send(player, new ZooNetworkPayloads.OpenZookeeperScreenPayload(
                this.getUUID(), keeperName, hasAnimal, dialogAnimalName, hunger, this.getFoodStock(), dialogText, foods
        ));
    }

    private void tickBathingDuty(ServerLevel sLevel) {
        LivingEntity target = null;
        if (this.currentBathingTargetUuid != null) {
            Entity ent = sLevel.getEntity(this.currentBathingTargetUuid);
            if (ent == null && sLevel.getServer() != null) {
                for (ServerLevel sl : sLevel.getServer().getAllLevels()) {
                    ent = sl.getEntity(this.currentBathingTargetUuid);
                    if (ent != null) break;
                }
            }
            if (ent instanceof LivingEntity l && l.isAlive()) {
                target = l;
            }
        }

        if (target == null) {
            int lowestCleanliness = 60;
            for (LivingEntity member : getAssignedFamily(sLevel)) {
                int c = ZooAnimalHungerManager.getCleanliness(member);
                if (c <= 60 && c <= lowestCleanliness) {
                    lowestCleanliness = c;
                    target = member;
                }
            }
            if (target != null) {
                this.currentBathingTargetUuid = target.getUUID();
            }
        }

        if (target == null) {
            stopBathing();
            return;
        }
        tickBathingDuty(sLevel, target);
    }

    private void tickBathingDuty(ServerLevel sLevel, LivingEntity assignedAnimal) {
        if (assignedAnimal == null || !this.hasBathingTools()) {
            stopBathing();
            return;
        }

        int cleanliness = ZooAnimalHungerManager.getCleanliness(assignedAnimal);
        if (cleanliness >= ZooAnimalHungerManager.MAX_CLEANLINESS) {
            stopBathing();
            return;
        }

        double maxBathDist = Math.max(3.8, (assignedAnimal.getBbWidth() * 0.5) + 2.6);
        double distSq = this.distanceToSqr(assignedAnimal);

        boolean isWet = net.zootycoon.animal.AnimalBathingManager.isWet(assignedAnimal);
        ItemStack targetTool = isWet ? (!brushStack.isEmpty() ? brushStack : new ItemStack(Items.BRUSH))
                : (!wateringCanStack.isEmpty() ? wateringCanStack : new ItemStack(Items.WATER_BUCKET));
        if (!this.getMainHandItem().is(targetTool.getItem())) {
            this.setItemSlot(EquipmentSlot.MAINHAND, targetTool.copy());
        }

        this.getLookControl().setLookAt(assignedAnimal, 30.0F, 30.0F);

        if (distSq > maxBathDist * maxBathDist) {
            this.getNavigation().moveTo(assignedAnimal, 1.35);
            this.setBathingAction(0);
            return;
        }

        // Close enough: stop moving and perform bathing actions
        this.getNavigation().stop();
        if (assignedAnimal instanceof Mob animalMob && animalMob.getNavigation().isInProgress()) {
            animalMob.getNavigation().stop();
            animalMob.getLookControl().setLookAt(this, 15.0F, 15.0F);
        }

        if (!isWet) {
            // Step 1: Pour water with watering can
            this.setBathingAction(1);
            bathingStepTicks++;

            if (bathingStepTicks % 4 == 0) {
                sLevel.sendParticles(ParticleTypes.SPLASH, assignedAnimal.getX(), assignedAnimal.getY() + assignedAnimal.getBbHeight() * 0.5, assignedAnimal.getZ(),
                        10, assignedAnimal.getBbWidth() * 0.35, assignedAnimal.getBbHeight() * 0.25, assignedAnimal.getBbWidth() * 0.35, 0.08);
                sLevel.sendParticles(ParticleTypes.DRIPPING_WATER, assignedAnimal.getX(), assignedAnimal.getY() + assignedAnimal.getBbHeight() * 0.8, assignedAnimal.getZ(),
                        5, assignedAnimal.getBbWidth() * 0.25, 0.1, assignedAnimal.getBbWidth() * 0.25, 0.04);
                sLevel.playSound(null, this.blockPosition(), SoundEvents.WATER_AMBIENT, SoundSource.NEUTRAL, 0.5f, 1.2f);
            }

            if (bathingStepTicks >= 30) {
                bathingStepTicks = 0;
                net.zootycoon.animal.AnimalBathingManager.setWet(assignedAnimal, net.zootycoon.animal.AnimalBathingManager.WET_DURATION_TICKS);

                sLevel.playSound(null, assignedAnimal.blockPosition(), SoundEvents.BUCKET_EMPTY, SoundSource.NEUTRAL, 1.0f, 1.2f);
                sLevel.playSound(null, assignedAnimal.blockPosition(), SoundEvents.GENERIC_SPLASH, SoundSource.NEUTRAL, 0.8f, 1.0f);

                net.zootycoon.animal.ZooInspectionService.notifyAnimalUpdated(assignedAnimal);
                this.setBathingAction(0);
                ItemStack brushItem = !brushStack.isEmpty() ? brushStack : new ItemStack(Items.BRUSH);
                this.setItemSlot(EquipmentSlot.MAINHAND, brushItem.copy());
            }
        } else {
            // Step 2: Scrub with brush
            this.setBathingAction(2);
            bathingStepTicks++;

            if (bathingStepTicks % 5 == 0) {
                sLevel.playSound(null, assignedAnimal.blockPosition(), SoundEvents.BRUSH_GENERIC, SoundSource.NEUTRAL, 0.8f, 1.0f + this.getRandom().nextFloat() * 0.2f);
                sLevel.sendParticles(ParticleTypes.BUBBLE_POP, assignedAnimal.getX(), assignedAnimal.getY() + assignedAnimal.getBbHeight() * 0.5, assignedAnimal.getZ(),
                        8, assignedAnimal.getBbWidth() * 0.35, assignedAnimal.getBbHeight() * 0.25, assignedAnimal.getBbWidth() * 0.35, 0.04);
                sLevel.sendParticles(ParticleTypes.WAX_OFF, assignedAnimal.getX(), assignedAnimal.getY() + assignedAnimal.getBbHeight() * 0.5, assignedAnimal.getZ(),
                        6, assignedAnimal.getBbWidth() * 0.35, assignedAnimal.getBbHeight() * 0.25, assignedAnimal.getBbWidth() * 0.35, 0.02);
            }

            if (bathingStepTicks >= 35) {
                bathingStepTicks = 0;
                ZooAnimalHungerManager.setCleanliness(assignedAnimal, ZooAnimalHungerManager.MAX_CLEANLINESS);

                hurtBrush(1, sLevel);

                sLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, assignedAnimal.getX(), assignedAnimal.getY() + assignedAnimal.getBbHeight() * 0.8, assignedAnimal.getZ(),
                        12, 0.3, 0.3, 0.3, 0.02);

                stopBathing();
                net.zootycoon.animal.ZooInspectionService.notifyAnimalUpdated(assignedAnimal);
                for (ServerPlayer sp : sLevel.players()) {
                    if (sp.distanceToSqr(assignedAnimal) <= 32 * 32) {
                        ZooNetworkPayloads.sendAnimalSync(sp);
                    }
                }
                actionCooldown = 40;
            }
        }
    }

    private void stopBathing() {
        this.currentDutyState = "IDLE";
        this.currentBathingTargetUuid = null;
        this.setBathingAction(0);
        this.bathingStepTicks = 0;
        this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
    }

    public void handleFoodTransfer(ServerPlayer player) {
        LivingEntity animal = null;
        if (assignedAnimalUuid != null && this.level() instanceof ServerLevel sLevel) {
            Entity ent = sLevel.getEntity(assignedAnimalUuid);
            if (ent instanceof LivingEntity l) animal = l;
        }

        int totalGiven = 0;
        boolean hasAnyFoodItem = false;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack st = player.getInventory().getItem(i);
            if (!st.isEmpty()) {
                var diet = (animal != null) ? AnimalDietRegistry.getDiet(animal) : null;
                boolean isVeg = isMatchingVegetationItem(st, diet);
                if (AnimalDietRegistry.isAnyFood(st) || AnimalDietRegistry.isAnyTroughFood(st) || isVeg) {
                    hasAnyFoodItem = true;
                }
                boolean valid = (animal != null) ? (AnimalDietRegistry.isFoodValid(animal, st) || isVeg) : (AnimalDietRegistry.isAnyFood(st) || AnimalDietRegistry.isAnyTroughFood(st));
                if (valid) {
                    int toTake = Math.min(st.getCount(), 32 - totalGiven);
                    ItemStack taken = st.split(toTake);
                    ItemStack leftover = this.foodInventory.addItem(taken);
                    int added = toTake - leftover.getCount();
                    if (!leftover.isEmpty()) {
                        st.grow(leftover.getCount());
                    }
                    totalGiven += added;
                    if (totalGiven >= 32 || !leftover.isEmpty()) break;
                }
            }
        }
        updateFoodStockFromInventory();
        player.getInventory().setChanged();

        String anDesc = (animal != null) ? (animal.hasCustomName() ? animal.getCustomName().getString() : animal.getType().getDescription().getString()) : "satwa";

        if (totalGiven > 0) {
            this.playSound(SoundEvents.VILLAGER_YES, 1.0f, 1.0f);
            ((ServerLevel) this.level()).sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 1.2, this.getZ(), 6, 0.2, 0.2, 0.2, 0.02);
            ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.zookeeper", "zootycoon.zookeeper.food_given_toast", 1, totalGiven, anDesc);
        } else if (!hasAnyFoodItem) {
            this.playSound(SoundEvents.VILLAGER_NO, 1.0f, 1.0f);
            ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.zookeeper", "zootycoon.zookeeper.no_food_in_inv", 2);
        } else {
            this.playSound(SoundEvents.VILLAGER_NO, 1.0f, 1.0f);
            ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.zookeeper", "zootycoon.zookeeper.wrong_food_toast", 2, anDesc);
        }
    }

    public void supplyBathingTools(ItemStack can, ItemStack brush) {
        this.hasWateringCan = true;
        this.wateringCanStack = can != null ? can.copy() : ItemStack.EMPTY;
        this.brushStack = brush != null ? brush.copy() : ItemStack.EMPTY;
        this.entityData.set(HAS_BATHING_TOOLS, true);
        this.currentDutyState = "BATHING";
        this.actionCooldown = 0;
        this.bathingStepTicks = 0;
    }

    public void handleBathingToolsTransfer(ServerPlayer player) {
        boolean anyDirty = false;
        if (this.level() instanceof ServerLevel sLevel) {
            for (LivingEntity m : getAssignedFamily(sLevel)) {
                if (ZooAnimalHungerManager.getCleanliness(m) <= 60) {
                    anyDirty = true;
                    break;
                }
            }
        }
        if (!anyDirty && assignedAnimalUuid != null && this.level() instanceof ServerLevel sLevel) {
            Entity ent = sLevel.getEntity(assignedAnimalUuid);
            if (ent instanceof LivingEntity l && l.isAlive() && ZooAnimalHungerManager.getCleanliness(l) <= 60) {
                anyDirty = true;
            }
        }

        if (!anyDirty) {
            // Animal and all offspring are clean! Zookeeper does not need tools
            return;
        }

        ItemStack canFound = ItemStack.EMPTY;
        int canSlot = -1;
        ItemStack brushFound = ItemStack.EMPTY;
        int brushSlot = -1;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack st = player.getInventory().getItem(i);
            if (!st.isEmpty()) {
                if (canSlot == -1 && net.zootycoon.animal.AnimalBathingManager.isWateringCan(st)) {
                    canFound = st;
                    canSlot = i;
                } else if (brushSlot == -1 && st.is(Items.BRUSH)) {
                    brushFound = st;
                    brushSlot = i;
                }
            }
        }

        if (canSlot != -1 && brushSlot != -1 && canSlot != brushSlot) {
            ItemStack takenCan = canFound.split(1);
            ItemStack takenBrush = brushFound.split(1);
            this.hasWateringCan = true;
            this.wateringCanStack = takenCan;
            this.brushStack = takenBrush;
            this.entityData.set(HAS_BATHING_TOOLS, true);
            player.getInventory().setChanged();

            this.currentDutyState = "BATHING";
            this.actionCooldown = 0;
            this.bathingStepTicks = 0;

            this.playSound(SoundEvents.VILLAGER_YES, 1.0f, 1.0f);
            if (this.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 1.2, this.getZ(), 8, 0.2, 0.2, 0.2, 0.02);
            }
            ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.success", "zootycoon.zookeeper.bathing_tools_given_toast", 1, this.getName().getString());
        } else {
            this.playSound(SoundEvents.VILLAGER_NO, 1.0f, 1.0f);
            ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.warning", "zootycoon.zookeeper.no_bathing_tools_toast", 2);
        }
    }

    public void hurtBrush(int amount, ServerLevel sLevel) {
        if (!brushStack.isEmpty()) {
            brushStack.hurtAndBreak(amount, this, EquipmentSlot.MAINHAND);
            if (brushStack.isEmpty()) {
                sLevel.playSound(null, this.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0f, 1.0f);
                this.hasWateringCan = false;
                this.wateringCanStack = ItemStack.EMPTY;
            }
            this.entityData.set(HAS_BATHING_TOOLS, hasBathingTools());
        }
    }

    public boolean hasBathingTools() {
        if (this.level() != null && this.level().isClientSide()) {
            return this.entityData.get(HAS_BATHING_TOOLS);
        }
        return this.hasWateringCan && !this.brushStack.isEmpty();
    }

    public boolean hasWateringCan() {
        return this.hasWateringCan;
    }

    public boolean hasBrush() {
        return !this.brushStack.isEmpty();
    }

    public int getBathingAction() {
        return this.entityData.get(BATHING_ACTION);
    }

    public void setBathingAction(int action) {
        this.entityData.set(BATHING_ACTION, action);
    }

    public ItemStack getBrushStack() {
        return this.brushStack;
    }

    public ItemStack getWateringCanStack() {
        return this.wateringCanStack;
    }

    public SimpleContainer getFuelInventory() {
        return this.fuelInventory;
    }

    public boolean hasCoal() {
        for (int i = 0; i < FUEL_SLOTS; i++) {
            ItemStack stack = this.fuelInventory.getItem(i);
            if (!stack.isEmpty() && ZooHeaterBlock.getFuelTicks(stack) > 0) {
                return true;
            }
        }
        return false;
    }

    public ItemStack peekCoal() {
        for (int i = 0; i < FUEL_SLOTS; i++) {
            ItemStack stack = this.fuelInventory.getItem(i);
            if (!stack.isEmpty() && ZooHeaterBlock.getFuelTicks(stack) > 0) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    public ItemStack takeCoal(int maxCount) {
        if (maxCount <= 0) return ItemStack.EMPTY;
        for (int i = 0; i < FUEL_SLOTS; i++) {
            ItemStack stack = this.fuelInventory.getItem(i);
            if (!stack.isEmpty() && ZooHeaterBlock.getFuelTicks(stack) > 0) {
                int toTake = Math.min(stack.getCount(), maxCount);
                ItemStack taken = stack.split(toTake);
                if (stack.isEmpty()) {
                    this.fuelInventory.setItem(i, ItemStack.EMPTY);
                }
                return taken;
            }
        }
        return ItemStack.EMPTY;
    }

    public boolean hasIce() {
        for (int i = 0; i < FUEL_SLOTS; i++) {
            ItemStack stack = this.fuelInventory.getItem(i);
            if (!stack.isEmpty() && ZooCoolerBlock.getCoolingTicks(stack) > 0) {
                return true;
            }
        }
        return false;
    }

    public ItemStack peekIce() {
        for (int i = 0; i < FUEL_SLOTS; i++) {
            ItemStack stack = this.fuelInventory.getItem(i);
            if (!stack.isEmpty() && ZooCoolerBlock.getCoolingTicks(stack) > 0) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    public ItemStack takeIce(int maxCount) {
        if (maxCount <= 0) return ItemStack.EMPTY;
        for (int i = 0; i < FUEL_SLOTS; i++) {
            ItemStack stack = this.fuelInventory.getItem(i);
            if (!stack.isEmpty() && ZooCoolerBlock.getCoolingTicks(stack) > 0) {
                int toTake = Math.min(stack.getCount(), maxCount);
                ItemStack taken = stack.split(toTake);
                if (stack.isEmpty()) {
                    this.fuelInventory.setItem(i, ItemStack.EMPTY);
                }
                return taken;
            }
        }
        return ItemStack.EMPTY;
    }

    public ItemStack addFuelOrCoolant(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return ItemStack.EMPTY;
        ItemStack leftover = this.fuelInventory.addItem(stack);
        if (leftover.isEmpty()) return ItemStack.EMPTY;

        // If SimpleContainer.addItem left items, search empty slots or merge into same items
        for (int i = 0; i < FUEL_SLOTS && !leftover.isEmpty(); i++) {
            ItemStack slot = this.fuelInventory.getItem(i);
            if (slot.isEmpty()) {
                this.fuelInventory.setItem(i, leftover.copy());
                leftover = ItemStack.EMPTY;
                break;
            } else if (ItemStack.isSameItemSameComponents(slot, leftover)) {
                int space = slot.getMaxStackSize() - slot.getCount();
                if (space > 0) {
                    int add = Math.min(space, leftover.getCount());
                    slot.grow(add);
                    leftover.shrink(add);
                }
            }
        }
        if (leftover.isEmpty()) return ItemStack.EMPTY;

        // If inventory is still full of other fuel/coolant types, make room for urgent need
        boolean isIce = ZooCoolerBlock.getCoolingTicks(stack) > 0;
        for (int i = 0; i < FUEL_SLOTS && !leftover.isEmpty(); i++) {
            ItemStack slot = this.fuelInventory.getItem(i);
            if (isIce && ZooHeaterBlock.getFuelTicks(slot) > 0) {
                this.fuelInventory.setItem(i, leftover.copy());
                leftover = ItemStack.EMPTY;
                break;
            } else if (!isIce && ZooCoolerBlock.getCoolingTicks(slot) > 0) {
                this.fuelInventory.setItem(i, leftover.copy());
                leftover = ItemStack.EMPTY;
                break;
            }
        }
        return leftover;
    }

    public void handleFuelTransfer(ServerPlayer player, String fuelType) {
        int totalGiven = 0;
        boolean hasFuelItem = false;
        boolean isCoal = "COAL".equalsIgnoreCase(fuelType);

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack st = player.getInventory().getItem(i);
            if (!st.isEmpty()) {
                int fuelTicks = isCoal ? ZooHeaterBlock.getFuelTicks(st) : ZooCoolerBlock.getCoolingTicks(st);
                if (fuelTicks > 0) {
                    hasFuelItem = true;
                    int toTake = Math.min(st.getCount(), 16 - totalGiven);
                    ItemStack taken = st.split(toTake);
                    ItemStack leftover = this.fuelInventory.addItem(taken);
                    int added = toTake - leftover.getCount();
                    if (!leftover.isEmpty()) {
                        st.grow(leftover.getCount());
                    }
                    totalGiven += added;
                    if (totalGiven >= 16 || !leftover.isEmpty()) break;
                }
            }
        }
        player.getInventory().setChanged();

        if (totalGiven > 0) {
            this.playSound(SoundEvents.VILLAGER_YES, 1.0f, 1.0f);
            ((ServerLevel) this.level()).sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 1.2, this.getZ(), 6, 0.2, 0.2, 0.2, 0.02);
            String toastKey = isCoal ? "zootycoon.zookeeper.coal_given_toast" : "zootycoon.zookeeper.ice_given_toast";
            ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.zookeeper", toastKey, 1, totalGiven);
        } else if (!hasFuelItem) {
            this.playSound(SoundEvents.VILLAGER_NO, 1.0f, 1.0f);
            String toastKey = isCoal ? "zootycoon.zookeeper.no_coal_in_inv" : "zootycoon.zookeeper.no_ice_in_inv";
            ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.zookeeper", toastKey, 2);
        }
    }

    public BlockPos findNearestHeater(ServerLevel sLevel, BlockPos center, double maxDist) {
        BlockPos best = null;
        double bestDistSq = maxDist * maxDist;
        for (BlockPos pos : ZooThermalBlockEntity.KNOWN_HEATERS) {
            if (ownerUuid != null && !ZooClaimManager.get(sLevel).isClaimedBy(pos, ownerUuid)) continue;
            double d = center.distSqr(pos);
            if (d <= bestDistSq) {
                if (sLevel.getBlockEntity(pos) instanceof ZooThermalBlockEntity thermal && thermal.getThermalType() == ZooThermalBlockEntity.ThermalType.HEATING) {
                    if (thermal.getFuelTime() < 2400) {
                        bestDistSq = d;
                        best = pos;
                    }
                }
            }
        }
        return best;
    }

    public BlockPos findNearestCooler(ServerLevel sLevel, BlockPos center, double maxDist) {
        BlockPos best = null;
        double bestDistSq = maxDist * maxDist;
        for (BlockPos pos : ZooThermalBlockEntity.KNOWN_COOLERS) {
            if (ownerUuid != null && !ZooClaimManager.get(sLevel).isClaimedBy(pos, ownerUuid)) continue;
            double d = center.distSqr(pos);
            if (d <= bestDistSq) {
                if (sLevel.getBlockEntity(pos) instanceof ZooThermalBlockEntity thermal && thermal.getThermalType() == ZooThermalBlockEntity.ThermalType.COOLING) {
                    if (thermal.getFuelTime() < 2400) {
                        bestDistSq = d;
                        best = pos;
                    }
                }
            }
        }
        return best;
    }

    public boolean isSweeping() {
        return this.entityData.get(SWEEPING) || this.getMainHandItem().is(ZooItems.BROOM);
    }

    public void setOwnerUuid(UUID uuid) {
        this.ownerUuid = uuid;
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    public UUID getAssignedAnimalUuid() {
        return assignedAnimalUuid;
    }

    public void setAssignedAnimalUuid(UUID uuid) {
        this.assignedAnimalUuid = uuid;
        this.entityData.set(HAS_ASSIGNED_ANIMAL, uuid != null);
    }

    public boolean hasAssignedAnimal() {
        return this.entityData.get(HAS_ASSIGNED_ANIMAL);
    }

    public SimpleContainer getFoodInventory() {
        return this.foodInventory;
    }

    public void updateFoodStockFromInventory() {
        int total = 0;
        for (int i = 0; i < FOOD_SLOTS; i++) {
            ItemStack stack = this.foodInventory.getItem(i);
            if (!stack.isEmpty()) {
                total += stack.getCount();
            }
        }
        this.entityData.set(FOOD_STOCK_DATA, total);
    }

    public ItemStack takeFirstFood(int maxCount) {
        if (maxCount <= 0) return ItemStack.EMPTY;
        for (int i = 0; i < FOOD_SLOTS; i++) {
            ItemStack stack = this.foodInventory.getItem(i);
            if (!stack.isEmpty()) {
                int toTake = Math.min(stack.getCount(), maxCount);
                ItemStack taken = stack.split(toTake);
                if (stack.isEmpty()) {
                    this.foodInventory.setItem(i, ItemStack.EMPTY);
                }
                updateFoodStockFromInventory();
                return taken;
            }
        }
        return ItemStack.EMPTY;
    }

    public static boolean requiresFeedingTrough(AnimalDietRegistry.AnimalDiet diet) {
        return diet == null
                || (diet.feedingType != AnimalDietRegistry.FeedingType.GRAZING
                && diet.feedingType != AnimalDietRegistry.FeedingType.THROWN_MEAT);
    }

    public static boolean isTroughEligibleForFamilyRefill(ZooFeedingTroughBlockEntity troughBe, List<LivingEntity> family) {
        if (troughBe == null || troughBe.hasWater() || !troughBe.isAssigned() || family == null || family.isEmpty()) {
            return false;
        }
        for (LivingEntity member : family) {
            if (troughBe.isAssignedTo(member)) {
                return true;
            }
        }
        return false;
    }

    public ItemStack peekFoodForFamily(List<LivingEntity> animals) {
        if (animals == null || animals.isEmpty()) return ItemStack.EMPTY;
        for (int i = 0; i < FOOD_SLOTS; i++) {
            ItemStack stack = this.foodInventory.getItem(i);
            if (!stack.isEmpty() && isFoodEatenByAny(animals, stack)) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    public ItemStack takeFoodForFamily(List<LivingEntity> animals, int maxCount) {
        if (maxCount <= 0 || animals == null || animals.isEmpty()) return ItemStack.EMPTY;
        for (int i = 0; i < FOOD_SLOTS; i++) {
            ItemStack stack = this.foodInventory.getItem(i);
            if (!stack.isEmpty() && isFoodEatenByAny(animals, stack)) {
                int toTake = Math.min(stack.getCount(), maxCount);
                ItemStack taken = stack.split(toTake);
                if (stack.isEmpty()) {
                    this.foodInventory.setItem(i, ItemStack.EMPTY);
                }
                updateFoodStockFromInventory();
                return taken;
            }
        }
        return ItemStack.EMPTY;
    }

    private static boolean isFoodEatenByAny(List<LivingEntity> animals, ItemStack stack) {
        for (LivingEntity animal : animals) {
            if (AnimalDietRegistry.isFoodValid(animal, stack)) return true;
        }
        return false;
    }

    public boolean hasMatchingFoodForTrough(ItemStack troughStack) {
        if (troughStack == null || troughStack.isEmpty()) return false;
        for (int i = 0; i < FOOD_SLOTS; i++) {
            ItemStack stack = this.foodInventory.getItem(i);
            if (!stack.isEmpty() && (stack.is(troughStack.getItem()) || ItemStack.isSameItemSameComponents(stack, troughStack))) {
                return true;
            }
        }
        return false;
    }

    public ItemStack peekMatchingFood(ItemStack troughStack) {
        if (troughStack == null || troughStack.isEmpty()) return peekFirstFood();
        for (int i = 0; i < FOOD_SLOTS; i++) {
            ItemStack stack = this.foodInventory.getItem(i);
            if (!stack.isEmpty() && (stack.is(troughStack.getItem()) || ItemStack.isSameItemSameComponents(stack, troughStack))) {
                return stack;
            }
        }
        return peekFirstFood();
    }

    public ItemStack takeMatchingFood(ItemStack troughStack, int maxCount) {
        if (maxCount <= 0) return ItemStack.EMPTY;
        if (troughStack == null || troughStack.isEmpty()) return takeFirstFood(maxCount);
        for (int i = 0; i < FOOD_SLOTS; i++) {
            ItemStack stack = this.foodInventory.getItem(i);
            if (!stack.isEmpty() && (stack.is(troughStack.getItem()) || ItemStack.isSameItemSameComponents(stack, troughStack))) {
                int toTake = Math.min(stack.getCount(), maxCount);
                ItemStack taken = stack.split(toTake);
                if (stack.isEmpty()) {
                    this.foodInventory.setItem(i, ItemStack.EMPTY);
                }
                updateFoodStockFromInventory();
                return taken;
            }
        }
        return takeFirstFood(maxCount);
    }

    public ItemStack peekFirstFood() {
        for (int i = 0; i < FOOD_SLOTS; i++) {
            ItemStack stack = this.foodInventory.getItem(i);
            if (!stack.isEmpty()) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    public ItemStack peekMatchingVegetation(net.zootycoon.animal.AnimalDietRegistry.AnimalDiet diet) {
        if (diet == null) return ItemStack.EMPTY;
        for (int i = 0; i < FOOD_SLOTS; i++) {
            ItemStack stack = this.foodInventory.getItem(i);
            if (!stack.isEmpty() && isMatchingVegetationItem(stack, diet)) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private boolean isMatchingVegetationItem(ItemStack stack, net.zootycoon.animal.AnimalDietRegistry.AnimalDiet diet) {
        if (stack.isEmpty() || diet == null) return false;
        // DILARANG MENANAM DAUN DI LANTAI! Daun hanya ada di pohon yang tumbuh dari sapling.
        if (stack.is(net.minecraft.tags.ItemTags.LEAVES)
                || (stack.getItem() instanceof net.minecraft.world.item.BlockItem bi && bi.getBlock() instanceof net.minecraft.world.level.block.LeavesBlock)) {
            return false;
        }

        // Reforestation item spesifik (misal: ACACIA_SAPLING, BONE_MEAL, BAMBOO, CACTUS, dsb)
        if (diet.reforestationItem != null && stack.is(diet.reforestationItem)) {
            return true;
        }
        if (diet.reforestationItem == Items.BONE_MEAL && stack.is(Items.BONE_MEAL)) {
            return true;
        }

        // Sapling pohon (hanya jika satwa butuh sapling/pohon)
        boolean isTreeAnimal = diet.reforestationItem != null && (diet.reforestationItem.getDefaultInstance().is(net.minecraft.tags.ItemTags.SAPLINGS)
                || (diet.reforestationItem instanceof net.minecraft.world.item.BlockItem rbi && rbi.getBlock() instanceof net.minecraft.world.level.block.SaplingBlock));
        if (isTreeAnimal && (stack.is(net.minecraft.tags.ItemTags.SAPLINGS)
                || (stack.getItem() instanceof net.minecraft.world.item.BlockItem bi && bi.getBlock() instanceof net.minecraft.world.level.block.SaplingBlock))) {
            return true;
        }

        return false;
    }

    public boolean hasVegetationSupplies(net.zootycoon.animal.AnimalDietRegistry.AnimalDiet diet) {
        if (diet == null) return false;
        return !peekMatchingVegetation(diet).isEmpty();
    }

    public ItemStack takeMatchingVegetation(Item item, int maxCount) {
        if (maxCount <= 0 || item == null) return ItemStack.EMPTY;
        for (int i = 0; i < FOOD_SLOTS; i++) {
            ItemStack stack = this.foodInventory.getItem(i);
            if (!stack.isEmpty() && stack.is(item)) {
                int toTake = Math.min(stack.getCount(), maxCount);
                ItemStack taken = stack.split(toTake);
                if (stack.isEmpty()) {
                    this.foodInventory.setItem(i, ItemStack.EMPTY);
                }
                updateFoodStockFromInventory();
                return taken;
            }
        }
        return takeFirstFood(maxCount);
    }

    private BlockPos findValidPlantingSpotNearAnimal(ServerLevel sLevel, LivingEntity animal, Item item) {
        if (sLevel == null || animal == null || item == null) return null;
        // DILARANG MENANAM BLOK DAUN DI LANTAI! Daun hanya ada di pohon yang tumbuh dari sapling.
        if (item.getDefaultInstance().is(net.minecraft.tags.ItemTags.LEAVES)
                || (item instanceof net.minecraft.world.item.BlockItem bi && bi.getBlock() instanceof net.minecraft.world.level.block.LeavesBlock)) {
            return null;
        }
        BlockPos center = animal.blockPosition();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int radius = 10;
        BlockPos bestPos = null;
        double bestDistSq = Double.MAX_VALUE;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx * dx + dz * dz > radius * radius) continue;
                for (int dy = -3; dy <= 3; dy++) {
                    pos.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                    var groundState = sLevel.getBlockState(pos);
                    var aboveState = sLevel.getBlockState(pos.above());

                    boolean isValidSpot = false;
                    if (item == Items.BONE_MEAL) {
                        if ((groundState.is(Blocks.GRASS_BLOCK) || groundState.is(Blocks.DIRT) || groundState.is(Blocks.COARSE_DIRT) || groundState.is(Blocks.ROOTED_DIRT))
                                && (aboveState.isAir() || aboveState.canBeReplaced())) {
                            if (!aboveState.is(Blocks.SHORT_GRASS) && !aboveState.is(Blocks.TALL_GRASS) && !aboveState.is(Blocks.FERN)) {
                                isValidSpot = true;
                            }
                        }
                    } else if (item == Items.GRASS_BLOCK) {
                        if ((groundState.is(Blocks.DIRT) || groundState.is(Blocks.COARSE_DIRT)) && (aboveState.isAir() || aboveState.canBeReplaced())) {
                            isValidSpot = true;
                        }
                    } else if (item == Items.CACTUS) {
                        if ((groundState.is(Blocks.SAND) || groundState.is(Blocks.RED_SAND)) && aboveState.isAir()) {
                            BlockPos ab = pos.above();
                            if (sLevel.getBlockState(ab.north()).isAir() && sLevel.getBlockState(ab.south()).isAir()
                                    && sLevel.getBlockState(ab.east()).isAir() && sLevel.getBlockState(ab.west()).isAir()) {
                                isValidSpot = true;
                            }
                        }
                    } else if (item == Items.BAMBOO) {
                        if ((groundState.is(Blocks.GRASS_BLOCK) || groundState.is(Blocks.DIRT) || groundState.is(Blocks.SAND)) && aboveState.isAir()) {
                            isValidSpot = true;
                        }
                    } else if (item instanceof net.minecraft.world.item.BlockItem bi && bi.getBlock() instanceof net.minecraft.world.level.block.SaplingBlock
                            || item == Items.ACACIA_SAPLING || item == Items.OAK_SAPLING || item == Items.CHERRY_SAPLING || item == Items.SPRUCE_SAPLING || item == Items.BIRCH_SAPLING || item == Items.JUNGLE_SAPLING || item == Items.DARK_OAK_SAPLING) {
                        // Jangan tanam sapling jika tepat di atasnya sudah ada blok daun/batang pohon, atau terlalu dekat sapling lain
                        if ((groundState.is(Blocks.GRASS_BLOCK) || groundState.is(Blocks.DIRT)) && aboveState.isAir()) {
                            boolean blockedAbove = false;
                            for (int up = 1; up <= 6; up++) {
                                BlockState upState = sLevel.getBlockState(pos.above(up));
                                if (upState.is(net.minecraft.tags.BlockTags.LEAVES) || upState.is(net.minecraft.tags.BlockTags.LOGS)) {
                                    blockedAbove = true;
                                    break;
                                }
                            }
                            if (!blockedAbove) {
                                boolean nearOtherSapling = false;
                                for (int sx = -3; sx <= 3; sx++) {
                                    for (int sz = -3; sz <= 3; sz++) {
                                        if (sx == 0 && sz == 0) continue;
                                        BlockState nearState = sLevel.getBlockState(pos.offset(sx, 1, sz));
                                        if (nearState.getBlock() instanceof net.minecraft.world.level.block.SaplingBlock) {
                                            nearOtherSapling = true;
                                            break;
                                        }
                                    }
                                    if (nearOtherSapling) break;
                                }
                                if (!nearOtherSapling) {
                                    isValidSpot = true;
                                }
                            }
                        }
                    } else if (item == Items.DANDELION || item == Items.POPPY || item == Items.RED_MUSHROOM || item == Items.BROWN_MUSHROOM) {
                        if ((groundState.is(Blocks.GRASS_BLOCK) || groundState.is(Blocks.DIRT)) && aboveState.isAir()) {
                            isValidSpot = true;
                        }
                    }

                    if (isValidSpot) {
                        double d = this.distanceToSqr(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);
                        if (d < bestDistSq) {
                            bestDistSq = d;
                            bestPos = pos.immutable();
                        }
                    }
                }
            }
        }
        return bestPos;
    }

    public LivingEntity getAssignedAnimalEntity(ServerLevel sLevel) {
        if (this.assignedAnimalUuid == null || sLevel == null) return null;
        Entity ent = sLevel.getEntity(this.assignedAnimalUuid);
        if (ent == null && sLevel.getServer() != null) {
            for (ServerLevel sl : sLevel.getServer().getAllLevels()) {
                ent = sl.getEntity(this.assignedAnimalUuid);
                if (ent != null) break;
            }
        }
        if (ent instanceof LivingEntity living && living.isAlive()) {
            return living;
        }
        return null;
    }

    private void tickPlantingDuty(ServerLevel sLevel) {
        LivingEntity assignedAnimal = getAssignedAnimalEntity(sLevel);
        if (assignedAnimal == null || !assignedAnimal.isAlive()) {
            stopPlanting();
            return;
        }

        var diet = AnimalDietRegistry.getDiet(assignedAnimal);
        if (diet == null || diet.feedingType != AnimalDietRegistry.FeedingType.GRAZING) {
            stopPlanting();
            return;
        }

        ItemStack carried = peekMatchingVegetation(diet);
        if (carried.isEmpty()) {
            stopPlanting();
            return;
        }

        if (targetPlantingPos == null) {
            targetPlantingPos = findValidPlantingSpotNearAnimal(sLevel, assignedAnimal, carried.getItem());
        }

        if (targetPlantingPos == null) {
            stopPlanting();
            return;
        }

        // Verify target spot is still valid
        BlockState ground = sLevel.getBlockState(targetPlantingPos);
        BlockState above = sLevel.getBlockState(targetPlantingPos.above());
        boolean stillValid = false;
        if (carried.is(Items.BONE_MEAL)) {
            stillValid = (ground.is(Blocks.GRASS_BLOCK) || ground.is(Blocks.DIRT) || ground.is(Blocks.COARSE_DIRT) || ground.is(Blocks.ROOTED_DIRT))
                    && (above.isAir() || above.canBeReplaced())
                    && !above.is(Blocks.SHORT_GRASS) && !above.is(Blocks.TALL_GRASS);
        } else if (carried.getItem() instanceof net.minecraft.world.item.BlockItem) {
            stillValid = (above.isAir() || above.canBeReplaced());
        }

        if (!stillValid) {
            targetPlantingPos = findValidPlantingSpotNearAnimal(sLevel, assignedAnimal, carried.getItem());
            if (targetPlantingPos == null) {
                stopPlanting();
                return;
            }
        }

        if (!this.getMainHandItem().is(carried.getItem())) {
            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(carried.getItem()));
        }

        double tx = targetPlantingPos.getX() + 0.5;
        double ty = targetPlantingPos.getY() + 1.0;
        double tz = targetPlantingPos.getZ() + 0.5;
        double distSq = this.distanceToSqr(tx, ty, tz);

        this.getLookControl().setLookAt(tx, ty, tz, 30.0F, 30.0F);

        if (distSq > 2.8 * 2.8) {
            plantingStuckTicks++;
            if (plantingStuckTicks > 80) {
                plantingStuckTicks = 0;
                targetPlantingPos = null;
                return;
            }
            this.getNavigation().moveTo(tx, ty, tz, 1.25);
            return;
        }

        plantingStuckTicks = 0;
        this.getNavigation().stop();
        this.swing(InteractionHand.MAIN_HAND);

        ItemStack taken = this.takeMatchingVegetation(carried.getItem(), 1);
        if (!taken.isEmpty()) {
            plantVegetationBlock(sLevel, targetPlantingPos, taken.getItem());
            sLevel.playSound(null, targetPlantingPos, SoundEvents.BONE_MEAL_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
            sLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, tx, ty + 0.2, tz, 8, 0.3, 0.3, 0.3, 0.02);
            actionCooldown = 30;
        }

        targetPlantingPos = null;
        if (peekMatchingVegetation(diet).isEmpty()) {
            stopPlanting();
        }
    }

    private void stopPlanting() {
        this.currentDutyState = "IDLE";
        this.targetPlantingPos = null;
        this.plantingStuckTicks = 0;
        if (this.getMainHandItem().is(Items.BONE_MEAL) || this.getMainHandItem().getItem() instanceof net.minecraft.world.item.BlockItem) {
            this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }
    }

    private void plantVegetationBlock(ServerLevel sLevel, BlockPos pos, Item item) {
        // DILARANG MENANAM BLOK DAUN DI LANTAI!
        if (item.getDefaultInstance().is(net.minecraft.tags.ItemTags.LEAVES)
                || (item instanceof net.minecraft.world.item.BlockItem bi && bi.getBlock() instanceof net.minecraft.world.level.block.LeavesBlock)) {
            return;
        }

        if (item == Items.BONE_MEAL) {
            BlockState state = sLevel.getBlockState(pos);
            if (state.is(Blocks.DIRT) || state.is(Blocks.COARSE_DIRT) || state.is(Blocks.ROOTED_DIRT)) {
                sLevel.setBlockAndUpdate(pos, Blocks.GRASS_BLOCK.defaultBlockState());
            } else if (state.getBlock() instanceof net.minecraft.world.level.block.BonemealableBlock bonemealable) {
                if (bonemealable.isValidBonemealTarget(sLevel, pos, state)) {
                    bonemealable.performBonemeal(sLevel, sLevel.random, pos, state);
                }
            }
            BlockPos above = pos.above();
            BlockState aboveState = sLevel.getBlockState(above);
            if (aboveState.isAir() || aboveState.canBeReplaced()) {
                sLevel.setBlockAndUpdate(above, Blocks.SHORT_GRASS.defaultBlockState());
            }
            sLevel.levelEvent(1505, pos, 0);
        } else if (item == Items.GRASS_BLOCK) {
            sLevel.setBlockAndUpdate(pos, Blocks.GRASS_BLOCK.defaultBlockState());
        } else if (item == Items.BAMBOO) {
            sLevel.setBlockAndUpdate(pos.above(), Blocks.BAMBOO.defaultBlockState());
        } else if (item == Items.CACTUS) {
            sLevel.setBlockAndUpdate(pos.above(), Blocks.CACTUS.defaultBlockState());
        } else if (item == Items.ACACIA_SAPLING) {
            sLevel.setBlockAndUpdate(pos.above(), Blocks.ACACIA_SAPLING.defaultBlockState());
        } else if (item == Items.OAK_SAPLING) {
            sLevel.setBlockAndUpdate(pos.above(), Blocks.OAK_SAPLING.defaultBlockState());
        } else if (item == Items.CHERRY_SAPLING) {
            sLevel.setBlockAndUpdate(pos.above(), Blocks.CHERRY_SAPLING.defaultBlockState());
        } else if (item == Items.SPRUCE_SAPLING) {
            sLevel.setBlockAndUpdate(pos.above(), Blocks.SPRUCE_SAPLING.defaultBlockState());
        } else if (item == Items.BIRCH_SAPLING) {
            sLevel.setBlockAndUpdate(pos.above(), Blocks.BIRCH_SAPLING.defaultBlockState());
        } else if (item == Items.JUNGLE_SAPLING) {
            sLevel.setBlockAndUpdate(pos.above(), Blocks.JUNGLE_SAPLING.defaultBlockState());
        } else if (item == Items.DARK_OAK_SAPLING) {
            sLevel.setBlockAndUpdate(pos.above(), Blocks.DARK_OAK_SAPLING.defaultBlockState());
        } else if (item == Items.DANDELION) {
            sLevel.setBlockAndUpdate(pos.above(), Blocks.DANDELION.defaultBlockState());
        } else if (item == Items.POPPY) {
            sLevel.setBlockAndUpdate(pos.above(), Blocks.POPPY.defaultBlockState());
        } else if (item == Items.RED_MUSHROOM) {
            sLevel.setBlockAndUpdate(pos.above(), Blocks.RED_MUSHROOM.defaultBlockState());
        } else if (item == Items.BROWN_MUSHROOM) {
            sLevel.setBlockAndUpdate(pos.above(), Blocks.BROWN_MUSHROOM.defaultBlockState());
        } else if (item instanceof net.minecraft.world.item.BlockItem bi) {
            if (!(bi.getBlock() instanceof net.minecraft.world.level.block.LeavesBlock)) {
                sLevel.setBlockAndUpdate(pos.above(), bi.getBlock().defaultBlockState());
            }
        }
    }

    public int getFoodStock() {
        return this.entityData.get(FOOD_STOCK_DATA);
    }

    public void setFoodStock(int amount) {
        this.entityData.set(FOOD_STOCK_DATA, amount);
    }

    public void addFoodStock(int amount) {
        this.setFoodStock(this.getFoodStock() + amount);
    }

    private void sweepLitterAt(ServerLevel sLevel, BlockPos trashPos) {
        this.getLookControl().setLookAt(trashPos.getX() + 0.5, trashPos.getY() + 0.5, trashPos.getZ() + 0.5);
        this.swing(InteractionHand.MAIN_HAND);
        sLevel.playSound(null, trashPos, SoundEvents.BRUSH_GENERIC, SoundSource.BLOCKS, 1.0f, 1.0f);
        sLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, trashPos.getX() + 0.5, trashPos.getY() + 0.3, trashPos.getZ() + 0.5, 6, 0.2, 0.1, 0.2, 0.02);
        sLevel.destroyBlock(trashPos, false, this);
        this.actionCooldown = 30;
        this.trashStuckTicks = 0;
        this.currentTrashTarget = null;
        if (this.tempDirtPos != null && this.tempDirtTimer > 30) {
            this.tempDirtTimer = 30;
        }
    }

    private boolean tryPlaceTemporaryDirt(ServerLevel sLevel) {
        if (this.tempDirtPos != null) return false;

        BlockPos groundPos = BlockPos.containing(this.getX(), this.getY() - 0.1, this.getZ());
        BlockPos feetPos = groundPos.above();

        BlockState stateAtFeet = sLevel.getBlockState(feetPos);
        boolean feetOk = stateAtFeet.isAir() || stateAtFeet.canBeReplaced();
        if (!feetOk) return false;

        BlockPos headPos = feetPos.above();
        BlockState stateAtHead = sLevel.getBlockState(headPos);
        boolean headOk = stateAtHead.isAir() || stateAtHead.canBeReplaced();
        if (!headOk) return false;

        BlockPos newHeadPos = feetPos.above(2);
        BlockState stateAbove = sLevel.getBlockState(newHeadPos);
        boolean aboveOk = stateAbove.isAir() || stateAbove.canBeReplaced() || !stateAbove.isSolid();
        if (!aboveOk) return false;

        this.previousBlockState = stateAtFeet;
        sLevel.setBlockAndUpdate(feetPos, net.minecraft.world.level.block.Blocks.DIRT.defaultBlockState());
        this.teleportTo(feetPos.getX() + 0.5, feetPos.getY() + 1.0, feetPos.getZ() + 0.5);
        this.setDeltaMovement(0, 0.1, 0);
        this.getNavigation().stop();

        sLevel.playSound(null, feetPos, SoundEvents.GRAVEL_PLACE, SoundSource.BLOCKS, 1.0f, 0.8f);
        sLevel.sendParticles(new net.minecraft.core.particles.BlockParticleOption(ParticleTypes.BLOCK, net.minecraft.world.level.block.Blocks.DIRT.defaultBlockState()),
                feetPos.getX() + 0.5, feetPos.getY() + 0.5, feetPos.getZ() + 0.5, 8, 0.2, 0.2, 0.2, 0.05);

        this.tempDirtPos = feetPos.immutable();
        this.tempDirtTimer = 80;
        this.trashStuckTicks = 0;
        return true;
    }

    private void removeTempDirt(ServerLevel sLevel) {
        if (tempDirtPos != null) {
            if (sLevel.getBlockState(tempDirtPos).is(net.minecraft.world.level.block.Blocks.DIRT)) {
                BlockState toRestore = (previousBlockState != null && !previousBlockState.is(net.minecraft.world.level.block.Blocks.DIRT))
                        ? previousBlockState
                        : net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
                sLevel.setBlockAndUpdate(tempDirtPos, toRestore);
                sLevel.playSound(null, tempDirtPos, SoundEvents.GRASS_BREAK, SoundSource.BLOCKS, 0.8f, 1.2f);
                sLevel.sendParticles(ParticleTypes.POOF, tempDirtPos.getX() + 0.5, tempDirtPos.getY() + 0.5, tempDirtPos.getZ() + 0.5, 6, 0.2, 0.2, 0.2, 0.03);
            }
            tempDirtPos = null;
            tempDirtTimer = 0;
            previousBlockState = null;
        }
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        if (tempDirtPos != null && this.level() instanceof ServerLevel sLevel) {
            removeTempDirt(sLevel);
        }
        super.remove(reason);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
        Containers.dropContents(level, this, this.foodInventory);
        Containers.dropContents(level, this, this.fuelInventory);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (ownerUuid != null) tag.putString("OwnerUuid", ownerUuid.toString());
        if (assignedAnimalUuid != null) tag.putString("AssignedAnimalUuid", assignedAnimalUuid.toString());
        tag.putInt("FoodStock", getFoodStock());
        ListTag foodTag = new ListTag();
        for (int i = 0; i < FOOD_SLOTS; i++) {
            ItemStack stack = this.foodInventory.getItem(i);
            if (!stack.isEmpty()) {
                CompoundTag slotTag = new CompoundTag();
                slotTag.putByte("Slot", (byte) i);
                foodTag.add(stack.save(this.registryAccess(), slotTag));
            }
        }
        tag.put("FoodInventory", foodTag);

        ListTag fuelTag = new ListTag();
        for (int i = 0; i < FUEL_SLOTS; i++) {
            ItemStack stack = this.fuelInventory.getItem(i);
            if (!stack.isEmpty()) {
                CompoundTag slotTag = new CompoundTag();
                slotTag.putByte("Slot", (byte) i);
                fuelTag.add(stack.save(this.registryAccess(), slotTag));
            }
        }
        tag.put("FuelInventory", fuelTag);

        tag.putString("DutyState", currentDutyState);
        tag.putBoolean("IsMale", isMale());
        tag.putBoolean("HasWateringCan", hasWateringCan);
        if (!wateringCanStack.isEmpty()) {
            tag.put("WateringCanStack", wateringCanStack.save(this.registryAccess()));
        }
        if (!brushStack.isEmpty()) {
            tag.put("BrushStack", brushStack.save(this.registryAccess()));
        }
        if (tempDirtPos != null) {
            tag.putInt("TempDirtX", tempDirtPos.getX());
            tag.putInt("TempDirtY", tempDirtPos.getY());
            tag.putInt("TempDirtZ", tempDirtPos.getZ());
            tag.putInt("TempDirtTimer", tempDirtTimer);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        String oStr = tag.getString("OwnerUuid");
        if (!oStr.isEmpty()) {
            try { this.ownerUuid = UUID.fromString(oStr); } catch (Exception ignored) {}
        }
        String aStr = tag.getString("AssignedAnimalUuid");
        if (!aStr.isEmpty()) {
            try { this.assignedAnimalUuid = UUID.fromString(aStr); } catch (Exception ignored) {}
        }
        this.entityData.set(HAS_ASSIGNED_ANIMAL, this.assignedAnimalUuid != null);
        this.foodInventory.clearContent();
        if (tag.contains("FoodInventory", Tag.TAG_LIST)) {
            ListTag foodTag = tag.getList("FoodInventory", Tag.TAG_COMPOUND);
            for (int i = 0; i < foodTag.size(); i++) {
                CompoundTag slotTag = foodTag.getCompound(i);
                int slot = slotTag.getByte("Slot") & 255;
                if (slot < FOOD_SLOTS) {
                    this.foodInventory.setItem(slot, ItemStack.parseOptional(this.registryAccess(), slotTag));
                }
            }
            updateFoodStockFromInventory();
        } else if (tag.contains("FoodStock")) {
            this.setFoodStock(tag.getInt("FoodStock"));
        }

        this.fuelInventory.clearContent();
        if (tag.contains("FuelInventory", Tag.TAG_LIST)) {
            ListTag fuelTag = tag.getList("FuelInventory", Tag.TAG_COMPOUND);
            for (int i = 0; i < fuelTag.size(); i++) {
                CompoundTag slotTag = fuelTag.getCompound(i);
                int slot = slotTag.getByte("Slot") & 255;
                if (slot < FUEL_SLOTS) {
                    this.fuelInventory.setItem(slot, ItemStack.parseOptional(this.registryAccess(), slotTag));
                }
            }
        }

        if (tag.contains("DutyState")) {
            this.currentDutyState = tag.getString("DutyState");
        }
        if (tag.contains("IsMale")) {
            this.setIsMale(tag.getBoolean("IsMale"));
        }
        if (tag.contains("HasWateringCan")) {
            this.hasWateringCan = tag.getBoolean("HasWateringCan");
        }
        if (tag.contains("WateringCanStack")) {
            this.wateringCanStack = ItemStack.parseOptional(this.registryAccess(), tag.getCompound("WateringCanStack"));
            if (!this.wateringCanStack.isEmpty()) {
                this.hasWateringCan = true;
            }
        }
        if (tag.contains("BrushStack")) {
            this.brushStack = ItemStack.parseOptional(this.registryAccess(), tag.getCompound("BrushStack"));
        }
        this.entityData.set(HAS_BATHING_TOOLS, hasBathingTools());

        if (tag.contains("TempDirtTimer")) {
            this.tempDirtTimer = tag.getInt("TempDirtTimer");
            this.tempDirtPos = new BlockPos(tag.getInt("TempDirtX"), tag.getInt("TempDirtY"), tag.getInt("TempDirtZ"));
        }
    }
}

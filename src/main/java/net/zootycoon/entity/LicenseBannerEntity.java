package net.zootycoon.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.zootycoon.item.LicenseBannerItem;
import net.zootycoon.item.ZooItems;

public class LicenseBannerEntity extends HangingEntity {

    private static final EntityDataAccessor<Integer> DATA_TIER_LEVEL =
            SynchedEntityData.defineId(LicenseBannerEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> DATA_ZOO_NAME =
            SynchedEntityData.defineId(LicenseBannerEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> DATA_DIRECTION_3D =
            SynchedEntityData.defineId(LicenseBannerEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> DATA_SPECIES_KEY =
            SynchedEntityData.defineId(LicenseBannerEntity.class, EntityDataSerializers.STRING);

    public LicenseBannerEntity(EntityType<? extends LicenseBannerEntity> type, Level level) {
        super(type, level);
    }

    public LicenseBannerEntity(Level level, BlockPos pos, Direction direction, int tierLevel, String zooName) {
        this(level, pos, direction, tierLevel, zooName, "");
    }

    public LicenseBannerEntity(Level level, BlockPos pos, Direction direction, int tierLevel, String zooName, String speciesKey) {
        super(ZooEntities.LICENSE_BANNER, level, pos);
        this.setTierLevel(tierLevel);
        this.setZooName(zooName != null ? zooName : "Player's Zoo");
        this.setSpeciesKey(speciesKey);
        this.setBannerDirection(direction);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_TIER_LEVEL, 1);
        builder.define(DATA_ZOO_NAME, "Player's Zoo");
        builder.define(DATA_DIRECTION_3D, Direction.SOUTH.get3DDataValue());
        builder.define(DATA_SPECIES_KEY, "");
    }

    public void setBannerDirection(Direction direction) {
        if (direction == null || !direction.getAxis().isHorizontal()) {
            direction = Direction.SOUTH;
        }
        this.entityData.set(DATA_DIRECTION_3D, direction.get3DDataValue());
        this.setDirection(direction);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (DATA_DIRECTION_3D.equals(key)) {
            Direction dir = Direction.from3DDataValue(this.entityData.get(DATA_DIRECTION_3D));
            if (dir.getAxis().isHorizontal()) {
                this.setDirection(dir);
            }
        }
    }

    public int getTierLevel() {
        return Math.clamp(this.entityData.get(DATA_TIER_LEVEL), 1, 5);
    }

    public void setTierLevel(int tierLevel) {
        this.entityData.set(DATA_TIER_LEVEL, Math.clamp(tierLevel, 1, 5));
    }

    public String getZooName() {
        return this.entityData.get(DATA_ZOO_NAME);
    }

    public void setZooName(String zooName) {
        this.entityData.set(DATA_ZOO_NAME, zooName != null ? zooName : "Player's Zoo");
    }

    public String getSpeciesKey() {
        return this.entityData.get(DATA_SPECIES_KEY);
    }

    public void setSpeciesKey(String key) {
        this.entityData.set(DATA_SPECIES_KEY, key != null ? key : "");
    }

    public boolean isAnimalInfoBanner() {
        return !getSpeciesKey().isEmpty();
    }

    @Override
    protected AABB calculateBoundingBox(BlockPos pos, Direction direction) {
        if (direction == null || !direction.getAxis().isHorizontal()) {
            direction = Direction.SOUTH;
        }
        Vec3 center = Vec3.atCenterOf(pos).relative(direction, -0.46875D).add(0.0D, 0.5D, 0.0D);
        Direction.Axis axis = direction.getAxis();
        double sizeX = axis == Direction.Axis.X ? 0.0625D : 3.0D;
        double sizeY = 2.0D;
        double sizeZ = axis == Direction.Axis.Z ? 0.0625D : 3.0D;
        return AABB.ofSize(center, sizeX, sizeY, sizeZ);
    }

    @Override
    public boolean survives() {
        if (!this.level().noCollision(this)) {
            return false;
        }
        Direction dir = this.getDirection();
        if (dir == null || !dir.getAxis().isHorizontal()) return false;
        BlockPos wallBase = this.pos.relative(dir.getOpposite());
        Direction right = dir.getCounterClockWise();
        for (int w = -1; w <= 1; w++) {
            for (int h = 0; h <= 1; h++) {
                BlockPos supportPos = wallBase.relative(right, w).above(h);
                if (!this.level().getBlockState(supportPos).isSolid()) {
                    return false;
                }
            }
        }
        return this.level().getEntities(this, this.getBoundingBox(), HANGING_ENTITY).isEmpty();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putByte("Facing", (byte) this.getDirection().get2DDataValue());
        tag.putInt("TierLevel", getTierLevel());
        tag.putString("ZooName", getZooName());
        tag.putString("SpeciesKey", getSpeciesKey());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("TierLevel")) {
            setTierLevel(tag.getInt("TierLevel"));
        }
        if (tag.contains("ZooName")) {
            setZooName(tag.getString("ZooName"));
        }
        if (tag.contains("SpeciesKey")) {
            setSpeciesKey(tag.getString("SpeciesKey"));
        }
        Direction dir = Direction.from2DDataValue(tag.getByte("Facing"));
        setBannerDirection(dir);
    }

    @Override
    public void dropItem(Entity brokenEntity) {
        if (!this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            return;
        }
        this.playSound(SoundEvents.PAINTING_BREAK, 1.0F, 1.0F);
        if (brokenEntity instanceof Player player && player.hasInfiniteMaterials()) {
            return;
        }
        ItemStack drop = isAnimalInfoBanner()
                ? LicenseBannerItem.createAnimalInfoBannerStack(getTierLevel(), getSpeciesKey(), getZooName())
                : LicenseBannerItem.createStackForTier(getTierLevel(), getZooName());
        this.spawnAtLocation(drop);
    }

    @Override
    public void playPlacementSound() {
        this.playSound(SoundEvents.PAINTING_PLACE, 1.0F, 1.0F);
    }

    @Override
    public void moveTo(double x, double y, double z, float yRot, float xRot) {
        this.setPos(x, y, z);
    }

    @Override
    public void lerpTo(double x, double y, double z, float yRot, float xRot, int steps) {
        this.setPos(x, y, z);
    }

    @Override
    public Vec3 trackingPosition() {
        return Vec3.atLowerCornerOf(this.pos);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity entity) {
        return new ClientboundAddEntityPacket(this, this.getDirection().get3DDataValue(), this.getPos());
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket packet) {
        super.recreateFromPacket(packet);
        Direction direction = Direction.from3DDataValue(packet.getData());
        this.setBannerDirection(direction);
    }

    @Override
    public ItemStack getPickResult() {
        return isAnimalInfoBanner()
                ? LicenseBannerItem.createAnimalInfoBannerStack(getTierLevel(), getSpeciesKey(), getZooName())
                : LicenseBannerItem.createStackForTier(getTierLevel(), getZooName());
    }
}

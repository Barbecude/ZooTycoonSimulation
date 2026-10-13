package net.zootycoon.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;
import net.zootycoon.entity.ToiletSeatEntity;
import net.zootycoon.entity.ZooEntities;
import net.zootycoon.entity.ZooVisitorEntity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class ToiletBlockEntity extends BlockEntity {
    private UUID occupiedBy = null;
    private int seatEntityId = -1;
    private int doorCloseTicks = 0;
    private final List<UUID> queue = new ArrayList<>();

    // Visitor occupant state
    private UUID visitorOccupant = null;
    private final List<UUID> visitorQueue = new ArrayList<>();
    private static final int VISITOR_USE_DURATION = 80;

    public ToiletBlockEntity(BlockPos pos, BlockState state) {
        super(ZooBlocks.TOILET_BLOCK_ENTITY, pos, state);
    }

    public boolean isOccupied() {
        return occupiedBy != null || visitorOccupant != null;
    }

    public boolean requestVisitorUse(UUID visitorUUID) {
        if (isOccupied()) {
            if (!visitorQueue.contains(visitorUUID) && !visitorUUID.equals(visitorOccupant)) {
                visitorQueue.add(visitorUUID);
                setChanged();
            }
            return false;
        }
        visitorOccupant = visitorUUID;
        setChanged();
        return true;
    }

    public boolean isVisitorOccupant(UUID uuid) {
        return uuid != null && uuid.equals(visitorOccupant);
    }

    public boolean isInVisitorQueue(UUID uuid) {
        return visitorQueue.contains(uuid);
    }

    public int getQueueSize() {
        return queue.size() + visitorQueue.size();
    }

    public void flush(ServerLevel sl) {
        sl.playSound(null, worldPosition, SoundEvents.WATER_AMBIENT, SoundSource.BLOCKS, 0.9f, 1.2f);
        sl.sendParticles(ParticleTypes.SPLASH, worldPosition.getX() + 0.5, worldPosition.getY() + 0.8, worldPosition.getZ() + 0.5, 8, 0.2, 0.1, 0.2, 0.05);
    }

    public void seatVisitor(ServerLevel sl, ZooVisitorEntity visitor) {
        if (sl == null || visitor == null) return;
        openNearbyDoors();

        Vec3 seatPos = computeSeatPos();
        ToiletSeatEntity seat = new ToiletSeatEntity(ZooEntities.TOILET_SEAT, sl);
        seat.setSeatBlockPos(worldPosition);
        seat.setPos(seatPos.x, seatPos.y, seatPos.z);
        seat.setYRot(visitor.getYRot());
        sl.addFreshEntity(seat);

        this.seatEntityId = seat.getId();
        this.doorCloseTicks = 0;
        setChanged();

        visitor.startRiding(seat, true);
        sl.playSound(null, worldPosition, SoundEvents.WOODEN_DOOR_OPEN, SoundSource.BLOCKS, 0.8f, 1.05f);
    }

    public void releaseVisitorOccupant(UUID uuid) {
        if (uuid != null && uuid.equals(visitorOccupant)) {
            visitorOccupant = null;
            if (this.seatEntityId >= 0 && level instanceof ServerLevel sl) {
                if (sl.getEntity(this.seatEntityId) instanceof ToiletSeatEntity seat) {
                    seat.discard();
                }
            }
            this.seatEntityId = -1;
            openNearbyDoors();
            doorCloseTicks = 20;
            setChanged();
        }
        visitorQueue.remove(uuid);
        setChanged();
    }

    public void requestUse(ServerPlayer player) {
        if (player == null || this.level == null || this.level.isClientSide()) return;

        if (isOccupied()) {
            if (occupiedBy != null && occupiedBy.equals(player.getUUID())) {
                return;
            }
            if (!queue.contains(player.getUUID())) {
                queue.add(player.getUUID());
                setChanged();
            }
            net.zootycoon.network.ZooNetworkPayloads.sendToast(
                    player,
                    "zootycoon.toast.title.warning",
                    "zootycoon.toilet.occupied",
                    2,
                    queue.size() + visitorQueue.size()
            );
            if (level instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.ANGRY_VILLAGER, worldPosition.getX() + 0.5, worldPosition.getY() + 1.0, worldPosition.getZ() + 0.5, 2, 0.2, 0.2, 0.2, 0.01);
            }
            return;
        }

        seatPlayer(player);
    }

    private void seatPlayer(ServerPlayer player) {
        if (!(level instanceof ServerLevel sl)) return;

        openNearbyDoors();

        Vec3 seatPos = computeSeatPos();
        ToiletSeatEntity seat = new ToiletSeatEntity(ZooEntities.TOILET_SEAT, sl);
        seat.setSeatBlockPos(worldPosition);
        seat.setPos(seatPos.x, seatPos.y, seatPos.z);
        seat.setYRot(player.getYRot());
        sl.addFreshEntity(seat);

        this.occupiedBy = player.getUUID();
        this.seatEntityId = seat.getId();
        this.doorCloseTicks = 0;
        setChanged();

        player.startRiding(seat);
        sl.playSound(null, worldPosition, SoundEvents.WOODEN_DOOR_OPEN, SoundSource.BLOCKS, 0.8f, 1.05f);
        sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, seatPos.x, seatPos.y + 0.2, seatPos.z, 10, 0.25, 0.1, 0.25, 0.01);
    }

    private Vec3 computeSeatPos() {
        Direction facing = Direction.NORTH;
        if (this.getBlockState().hasProperty(ToiletBlock.FACING)) {
            facing = this.getBlockState().getValue(ToiletBlock.FACING);
        }
        double x = worldPosition.getX() + 0.5;
        double y = worldPosition.getY() + 0.32;
        double z = worldPosition.getZ() + 0.5;

        double back = 0.18;
        x -= facing.getStepX() * back;
        z -= facing.getStepZ() * back;
        return new Vec3(x, y, z);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ToiletBlockEntity be) {
        if (!(level instanceof ServerLevel sl)) return;

        if (be.occupiedBy != null) {
            ServerPlayer occupant = sl.getServer().getPlayerList().getPlayer(be.occupiedBy);
            ToiletSeatEntity seat = be.seatEntityId >= 0 ? (sl.getEntity(be.seatEntityId) instanceof ToiletSeatEntity s ? s : null) : null;

            boolean stillSitting = occupant != null && seat != null && occupant.isPassenger() && occupant.getVehicle() == seat;
            if (!stillSitting) {
                if (occupant != null && occupant.isPassenger() && occupant.getVehicle() == seat) {
                    occupant.stopRiding();
                }
                be.clearOccupied(sl);
            } else {
                if (sl.getGameTime() % 20 == 0) {
                    sl.sendParticles(ParticleTypes.SPLASH, pos.getX() + 0.5, pos.getY() + 0.9, pos.getZ() + 0.5, 2, 0.1, 0.05, 0.1, 0.0);
                }
            }
        }

        if (be.occupiedBy == null && !be.queue.isEmpty()) {
            Iterator<UUID> it = be.queue.iterator();
            while (it.hasNext()) {
                UUID next = it.next();
                ServerPlayer player = sl.getServer().getPlayerList().getPlayer(next);
                if (player == null) {
                    it.remove();
                    be.setChanged();
                    continue;
                }
                if (player.distanceToSqr(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5) > 36.0D) {
                    continue;
                }
                it.remove();
                be.setChanged();
                be.seatPlayer(player);
                break;
            }
        }

        if (be.doorCloseTicks > 0) {
            be.doorCloseTicks--;
            if (be.doorCloseTicks <= 0) {
                be.closeNearbyDoors();
            }
        }

        // Validate visitor occupant
        if (be.visitorOccupant != null) {
            boolean alive = !sl.getEntitiesOfClass(ZooVisitorEntity.class,
                    new net.minecraft.world.phys.AABB(pos).inflate(10.0D),
                    v -> v.getUUID().equals(be.visitorOccupant)).isEmpty();
            if (!alive) {
                be.visitorOccupant = null;
                be.openNearbyDoors();
                be.doorCloseTicks = 20;
                be.setChanged();
            }
        }
    }

    private void clearOccupied(ServerLevel sl) {
        if (this.seatEntityId >= 0) {
            if (sl.getEntity(this.seatEntityId) instanceof ToiletSeatEntity seat) {
                seat.discard();
            }
        }
        this.occupiedBy = null;
        this.seatEntityId = -1;
        setChanged();
        openNearbyDoors();
        this.doorCloseTicks = 20;
        sl.playSound(null, worldPosition, SoundEvents.WATER_AMBIENT, SoundSource.BLOCKS, 0.9f, 1.2f);
        sl.sendParticles(ParticleTypes.SPLASH, worldPosition.getX() + 0.5, worldPosition.getY() + 0.8, worldPosition.getZ() + 0.5, 8, 0.2, 0.1, 0.2, 0.05);
    }

    private void openNearbyDoors() {
        if (level == null || level.isClientSide()) return;
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -1; dy <= 2; dy++) {
                for (int dz = -2; dz <= 2; dz++) {
                    BlockPos p = worldPosition.offset(dx, dy, dz);
                    BlockState bs = level.getBlockState(p);
                    if (bs.getBlock() instanceof DoorBlock) {
                        if (!bs.getValue(DoorBlock.OPEN)) {
                            level.setBlock(p, bs.setValue(DoorBlock.OPEN, true), 3);
                            level.playSound(null, p, SoundEvents.WOODEN_DOOR_OPEN, SoundSource.BLOCKS, 0.7f, 1.0f);
                        }
                    }
                }
            }
        }
    }

    private void closeNearbyDoors() {
        if (level == null || level.isClientSide()) return;
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -1; dy <= 2; dy++) {
                for (int dz = -2; dz <= 2; dz++) {
                    BlockPos p = worldPosition.offset(dx, dy, dz);
                    BlockState bs = level.getBlockState(p);
                    if (bs.getBlock() instanceof DoorBlock) {
                        if (bs.getValue(DoorBlock.OPEN)) {
                            level.setBlock(p, bs.setValue(DoorBlock.OPEN, false), 3);
                            level.playSound(null, p, SoundEvents.WOODEN_DOOR_CLOSE, SoundSource.BLOCKS, 0.7f, 1.0f);
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
    }
}

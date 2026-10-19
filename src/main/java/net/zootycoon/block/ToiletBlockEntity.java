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
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ToiletBlockEntity extends BlockEntity {
    public static final Set<BlockPos> KNOWN_TOILETS = ConcurrentHashMap.newKeySet();

    public static void reset() {
        KNOWN_TOILETS.clear();
    }

    private UUID occupiedBy = null;
    private int seatEntityId = -1;
    private int doorCloseTicks = 0;
    private final List<UUID> queue = new ArrayList<>();

    // Visitor occupant state
    private UUID visitorOccupant = null;
    private final List<UUID> visitorQueue = new ArrayList<>();
    private int releaseCooldownTicks = 0;
    private static final int VISITOR_USE_DURATION = 80;

    public ToiletBlockEntity(BlockPos pos, BlockState state) {
        super(ZooBlocks.TOILET_BLOCK_ENTITY, pos, state);
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();
        KNOWN_TOILETS.add(this.worldPosition.immutable());
    }

    @Override
    public void setRemoved() {
        KNOWN_TOILETS.remove(this.worldPosition);
        super.setRemoved();
    }

    public boolean isOccupied() {
        return occupiedBy != null || visitorOccupant != null || releaseCooldownTicks > 0;
    }

    public boolean requestVisitorUse(UUID visitorUUID) {
        if (visitorUUID == null) return false;
        if (visitorUUID.equals(visitorOccupant)) {
            return true;
        }
        if (!isOccupied() && (visitorQueue.isEmpty() || visitorQueue.get(0).equals(visitorUUID))) {
            visitorQueue.remove(visitorUUID);
            visitorOccupant = visitorUUID;
            setChanged();
            return true;
        }
        if (!visitorQueue.contains(visitorUUID)) {
            visitorQueue.add(visitorUUID);
            setChanged();
        }
        return false;
    }

    public boolean isVisitorOccupant(UUID uuid) {
        return uuid != null && uuid.equals(visitorOccupant);
    }

    public boolean isInVisitorQueue(UUID uuid) {
        return uuid != null && visitorQueue.contains(uuid);
    }

    public int getVisitorQueueIndex(UUID uuid) {
        if (uuid == null) return -1;
        return visitorQueue.indexOf(uuid);
    }

    public Direction getFacingDirection() {
        if (this.getBlockState().hasProperty(ToiletBlock.FACING)) {
            return this.getBlockState().getValue(ToiletBlock.FACING);
        }
        return Direction.NORTH;
    }

    public BlockPos getQueueSlotPos(int queueIndex) {
        Direction facing = getFacingDirection();
        int dist = 2 + Math.max(0, queueIndex);
        return worldPosition.relative(facing, dist);
    }

    public int getQueueSize() {
        return queue.size() + visitorQueue.size() + (visitorOccupant != null ? 1 : 0);
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
        seat.setYRot(getFacingDirection().toYRot());
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
            this.releaseCooldownTicks = 15;
            openNearbyDoors();
            doorCloseTicks = 25;
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

        KNOWN_TOILETS.add(pos.immutable());

        if (be.releaseCooldownTicks > 0) {
            be.releaseCooldownTicks--;
        }

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

        // Prune invalid or departed visitors from visitorQueue
        if (!be.visitorQueue.isEmpty()) {
            Iterator<UUID> vIt = be.visitorQueue.iterator();
            while (vIt.hasNext()) {
                UUID vId = vIt.next();
                if (sl.getEntity(vId) instanceof ZooVisitorEntity v && v.isAlive() && v.needsToilet() && !v.isTimeToLeave()) {
                    continue;
                }
                vIt.remove();
                be.setChanged();
            }
        }

        // Validate visitor occupant
        if (be.visitorOccupant != null) {
            boolean valid = sl.getEntity(be.visitorOccupant) instanceof ZooVisitorEntity v
                    && v.isAlive()
                    && v.needsToilet()
                    && !v.isTimeToLeave()
                    && v.distanceToSqr(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5) <= 64.0 * 64.0;
            if (!valid) {
                be.releaseVisitorOccupant(be.visitorOccupant);
            }
        }

        // Promote next queued visitor when toilet is free and release cooldown is over
        if (!be.isOccupied() && !be.visitorQueue.isEmpty()) {
            be.visitorOccupant = be.visitorQueue.remove(0);
            be.setChanged();
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

package com.example.simulation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class ToiletBlockEntity extends BlockEntity {
    private UUID occupiedBy;
    private int seatEntityId = -1;
    private int doorCloseTicks = 0;
    private final List<UUID> queue = new ArrayList<>();

    public ToiletBlockEntity(BlockPos pos, BlockState state) {
        super(IndoZooTycoon.TOILET_BE.get(), pos, state);
    }

    public boolean isOccupied() {
        return occupiedBy != null;
    }

    public int getQueueSize() {
        return queue.size();
    }

    public void requestUse(ServerPlayer player) {
        if (player == null || this.level == null || this.level.isClientSide) return;

        if (occupiedBy != null) {
            if (occupiedBy.equals(player.getUUID())) {
                return;
            }
            if (!queue.contains(player.getUUID())) {
                queue.add(player.getUUID());
                setChanged();
            }
            player.displayClientMessage(net.minecraft.network.chat.Component.literal("Toilet penuh. Antrian: " + queue.size()).withStyle(net.minecraft.ChatFormatting.GOLD), true);
            ((ServerLevel) level).sendParticles(ParticleTypes.ANGRY_VILLAGER, worldPosition.getX() + 0.5, worldPosition.getY() + 1.0, worldPosition.getZ() + 0.5, 2, 0.2, 0.2, 0.2, 0.01);
            return;
        }

        seatPlayer(player);
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
                sl.sendParticles(ParticleTypes.COMPOSTER, pos.getX() + 0.5, pos.getY() + 0.9, pos.getZ() + 0.5, 1, 0.1, 0.05, 0.1, 0.0);
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
    }

    private void seatPlayer(ServerPlayer player) {
        if (!(level instanceof ServerLevel sl)) return;

        openNearbyDoors();

        Vec3 seatPos = computeSeatPos();
        ToiletSeatEntity seat = new ToiletSeatEntity(IndoZooTycoon.TOILET_SEAT_ENTITY.get(), sl);
        seat.setSeatBlockPos(worldPosition);
        seat.moveTo(seatPos.x, seatPos.y, seatPos.z, player.getYRot(), 0.0F);
        sl.addFreshEntity(seat);

        this.occupiedBy = player.getUUID();
        this.seatEntityId = seat.getId();
        this.doorCloseTicks = 0;
        setChanged();

        player.startRiding(seat, true);
        sl.playSound(null, worldPosition, SoundEvents.WOODEN_DOOR_OPEN, SoundSource.BLOCKS, 0.8f, 1.05f);
        sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, seatPos.x, seatPos.y + 0.2, seatPos.z, 10, 0.25, 0.1, 0.25, 0.01);
    }

    private Vec3 computeSeatPos() {
        Direction facing = Direction.NORTH;
        if (this.getBlockState().hasProperty(RestroomBlock.FACING)) {
            facing = this.getBlockState().getValue(RestroomBlock.FACING);
        }
        double x = worldPosition.getX() + 0.5;
        double y = worldPosition.getY() + 0.32;
        double z = worldPosition.getZ() + 0.5;

        double back = 0.18;
        x -= facing.getStepX() * back;
        z -= facing.getStepZ() * back;
        return new Vec3(x, y, z);
    }

    private void clearOccupied(ServerLevel sl) {
        if (this.seatEntityId >= 0) {
            if (sl.getEntity(this.seatEntityId) instanceof ToiletSeatEntity seat) {
                seat.discard();
            }
        }

        ServerPlayer occupant = this.occupiedBy != null ? sl.getServer().getPlayerList().getPlayer(this.occupiedBy) : null;
        this.occupiedBy = null;
        this.seatEntityId = -1;
        setChanged();

        if (occupant != null) {
            pushPlayerExit(occupant);
        }

        this.doorCloseTicks = 20;
    }

    private void pushPlayerExit(ServerPlayer player) {
        Direction facing = Direction.NORTH;
        if (this.getBlockState().hasProperty(RestroomBlock.FACING)) {
            facing = this.getBlockState().getValue(RestroomBlock.FACING);
        }
        Vec3 out = new Vec3(worldPosition.getX() + 0.5 + facing.getStepX() * 0.8, worldPosition.getY() + 0.1, worldPosition.getZ() + 0.5 + facing.getStepZ() * 0.8);
        player.teleportTo(out.x, out.y, out.z);
    }

    private void openNearbyDoors() {
        if (!(level instanceof ServerLevel sl)) return;
        for (BlockPos p : BlockPos.betweenClosed(worldPosition.offset(-2, -1, -2), worldPosition.offset(2, 2, 2))) {
            BlockState st = sl.getBlockState(p);
            if (st.getBlock() instanceof DoorBlock door) {
                if (!st.getValue(DoorBlock.OPEN)) {
                    door.setOpen(null, sl, st, p, true);
                }
            } else if (st.getBlock() instanceof FenceGateBlock) {
                if (!st.getValue(FenceGateBlock.OPEN)) {
                    sl.setBlock(p, st.setValue(FenceGateBlock.OPEN, true), 3);
                }
            }
        }
    }

    private void closeNearbyDoors() {
        if (!(level instanceof ServerLevel sl)) return;
        for (BlockPos p : BlockPos.betweenClosed(worldPosition.offset(-2, -1, -2), worldPosition.offset(2, 2, 2))) {
            if (!sl.getEntitiesOfClass(ServerPlayer.class, new net.minecraft.world.phys.AABB(p).inflate(1.2D)).isEmpty()) {
                continue;
            }
            BlockState st = sl.getBlockState(p);
            if (st.getBlock() instanceof DoorBlock door) {
                if (st.getValue(DoorBlock.OPEN)) {
                    door.setOpen(null, sl, st, p, false);
                }
            } else if (st.getBlock() instanceof FenceGateBlock) {
                if (st.getValue(FenceGateBlock.OPEN)) {
                    sl.setBlock(p, st.setValue(FenceGateBlock.OPEN, false), 3);
                }
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (occupiedBy != null) tag.putUUID("OccupiedBy", occupiedBy);
        tag.putInt("SeatEntityId", seatEntityId);
        tag.putInt("DoorCloseTicks", doorCloseTicks);
        ListTag q = new ListTag();
        for (UUID id : queue) {
            CompoundTag e = new CompoundTag();
            e.putUUID("Id", id);
            q.add(e);
        }
        tag.put("Queue", q);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        occupiedBy = tag.hasUUID("OccupiedBy") ? tag.getUUID("OccupiedBy") : null;
        seatEntityId = tag.getInt("SeatEntityId");
        doorCloseTicks = tag.getInt("DoorCloseTicks");
        queue.clear();
        if (tag.contains("Queue", Tag.TAG_LIST)) {
            ListTag q = tag.getList("Queue", Tag.TAG_COMPOUND);
            for (int i = 0; i < q.size(); i++) {
                CompoundTag e = q.getCompound(i);
                if (e.hasUUID("Id")) {
                    queue.add(e.getUUID("Id"));
                }
            }
        }
    }
}


package com.example.simulation;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

public class ToiletSeatEntity extends Entity {
    private BlockPos seatBlockPos = BlockPos.ZERO;

    public ToiletSeatEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public void setSeatBlockPos(BlockPos pos) {
        this.seatBlockPos = pos.immutable();
    }

    public BlockPos getSeatBlockPos() {
        return seatBlockPos;
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("SeatPos")) {
            this.seatBlockPos = BlockPos.of(tag.getLong("SeatPos"));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putLong("SeatPos", this.seatBlockPos.asLong());
    }

    @Override
    public void tick() {
        super.tick();
        this.setDeltaMovement(0, 0, 0);
        if (!this.level().isClientSide) {
            if (this.getPassengers().isEmpty()) {
                this.discard();
            }
        }
    }

    @Override
    public void move(MoverType type, net.minecraft.world.phys.Vec3 vec) {
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}

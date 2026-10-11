package net.zootycoon.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;

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
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    public boolean hurt(DamageSource damageSource, float amount) {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    public void tick() {
        super.tick();
        this.setDeltaMovement(0, 0, 0);
        if (!this.level().isClientSide()) {
            if (this.getPassengers().isEmpty()) {
                this.discard();
            }
        }
    }

    @Override
    public void move(MoverType type, Vec3 vec) {
    }

    @Override
    public boolean isPickable() {
        return false;
    }
}


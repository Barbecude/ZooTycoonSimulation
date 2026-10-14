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
            } else if (!this.seatBlockPos.equals(BlockPos.ZERO) && this.level().isEmptyBlock(this.seatBlockPos)) {
                this.discard();
            }
        }
    }

    @Override
    public Vec3 getDismountLocationForPassenger(net.minecraft.world.entity.LivingEntity passenger) {
        if (seatBlockPos != null && !seatBlockPos.equals(BlockPos.ZERO)) {
            net.minecraft.core.Direction facing = net.minecraft.core.Direction.NORTH;
            var state = this.level().getBlockState(seatBlockPos);
            if (state.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING)) {
                facing = state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING);
            } else if (state.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING)) {
                net.minecraft.core.Direction d = state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING);
                if (d.getAxis().isHorizontal()) {
                    facing = d;
                }
            }
            BlockPos front = seatBlockPos.relative(facing);
            if (this.level().getBlockState(front).isAir() || !this.level().getBlockState(front).isSolid()) {
                return new Vec3(front.getX() + 0.5D, front.getY(), front.getZ() + 0.5D);
            }
        }
        return super.getDismountLocationForPassenger(passenger);
    }

    @Override
    public void move(MoverType type, Vec3 vec) {
    }

    @Override
    public boolean isPickable() {
        return false;
    }
}


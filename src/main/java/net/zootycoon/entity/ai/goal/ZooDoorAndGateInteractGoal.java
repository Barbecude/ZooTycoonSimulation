package net.zootycoon.entity.ai.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.EnumSet;

public class ZooDoorAndGateInteractGoal extends Goal {
    private final Mob mob;
    private BlockPos targetPos;
    private boolean doorOpen = false;
    private int closeTimer = 0;

    public ZooDoorAndGateInteractGoal(Mob mob) {
        this.mob = mob;
        this.setFlags(EnumSet.noneOf(Flag.class));
    }

    @Override
    public boolean canUse() {
        if (!mob.getNavigation().isInProgress()) return false;

        BlockPos current = mob.blockPosition();
        Direction dir = mob.getDirection();
        BlockPos ahead = current.relative(dir);

        BlockPos candidate = checkInteractable(ahead);
        if (candidate == null) {
            candidate = checkInteractable(current);
        }
        if (candidate != null) {
            this.targetPos = candidate;
            return true;
        }
        return false;
    }

    private BlockPos checkInteractable(BlockPos pos) {
        Level level = mob.level();
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof DoorBlock && !state.getValue(DoorBlock.OPEN)) {
            return pos;
        }
        if (state.getBlock() instanceof TrapDoorBlock && !state.getValue(TrapDoorBlock.OPEN)) {
            return pos;
        }
        if (state.getBlock() instanceof FenceGateBlock && !state.getValue(FenceGateBlock.OPEN)) {
            return pos;
        }

        BlockState above = level.getBlockState(pos.above());
        if (above.getBlock() instanceof TrapDoorBlock && !above.getValue(TrapDoorBlock.OPEN)) {
            return pos.above();
        }
        if (above.getBlock() instanceof FenceGateBlock && !above.getValue(FenceGateBlock.OPEN)) {
            return pos.above();
        }
        return null;
    }

    @Override
    public void start() {
        if (targetPos != null) {
            Level level = mob.level();
            BlockState state = level.getBlockState(targetPos);
            if (state.getBlock() instanceof DoorBlock door && !state.getValue(DoorBlock.OPEN)) {
                door.setOpen(mob, level, state, targetPos, true);
                doorOpen = true;
                closeTimer = 30;
            } else if (state.getBlock() instanceof TrapDoorBlock && !state.getValue(TrapDoorBlock.OPEN)) {
                level.setBlock(targetPos, state.setValue(TrapDoorBlock.OPEN, true), 10);
                level.playSound(null, targetPos, SoundEvents.WOODEN_TRAPDOOR_OPEN, SoundSource.BLOCKS, 1.0F, 1.0F);
                doorOpen = true;
                closeTimer = 30;
            } else if (state.getBlock() instanceof FenceGateBlock && !state.getValue(FenceGateBlock.OPEN)) {
                level.setBlock(targetPos, state.setValue(FenceGateBlock.OPEN, true), 10);
                level.playSound(null, targetPos, SoundEvents.FENCE_GATE_OPEN, SoundSource.BLOCKS, 1.0F, 1.0F);
                doorOpen = true;
                closeTimer = 30;
            }
        }
    }

    @Override
    public boolean canContinueToUse() {
        return doorOpen && closeTimer > 0;
    }

    @Override
    public void tick() {
        closeTimer--;
        if (closeTimer <= 0 && targetPos != null) {
            double distSq = mob.distanceToSqr(targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5);
            if (distSq > 2.25D) {
                Level level = mob.level();
                BlockState state = level.getBlockState(targetPos);
                if (state.getBlock() instanceof DoorBlock door && state.getValue(DoorBlock.OPEN)) {
                    door.setOpen(mob, level, state, targetPos, false);
                } else if (state.getBlock() instanceof TrapDoorBlock && state.getValue(TrapDoorBlock.OPEN)) {
                    level.setBlock(targetPos, state.setValue(TrapDoorBlock.OPEN, false), 10);
                    level.playSound(null, targetPos, SoundEvents.WOODEN_TRAPDOOR_CLOSE, SoundSource.BLOCKS, 1.0F, 1.0F);
                } else if (state.getBlock() instanceof FenceGateBlock && state.getValue(FenceGateBlock.OPEN)) {
                    level.setBlock(targetPos, state.setValue(FenceGateBlock.OPEN, false), 10);
                    level.playSound(null, targetPos, SoundEvents.FENCE_GATE_CLOSE, SoundSource.BLOCKS, 1.0F, 1.0F);
                }
                doorOpen = false;
                targetPos = null;
            }
        }
    }
}

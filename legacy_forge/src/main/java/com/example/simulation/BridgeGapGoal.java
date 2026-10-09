package com.example.simulation;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class BridgeGapGoal extends Goal {
    private final PathfinderMob mob;
    private int cooldown = 0;
    private final Map<Long, Integer> placedBlocks = new HashMap<>();

    public BridgeGapGoal(PathfinderMob mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (this.cooldown > 0) {
            this.cooldown--;
        }
        
        // Cleanup blocks
        if (!placedBlocks.isEmpty()) {
            Iterator<Map.Entry<Long, Integer>> it = placedBlocks.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Long, Integer> entry = it.next();
                entry.setValue(entry.getValue() - 1);
                if (entry.getValue() <= 0) {
                    BlockPos pos = BlockPos.of(entry.getKey());
                    if (mob.level().getBlockState(pos).is(Blocks.DIRT)) {
                        mob.level().destroyBlock(pos, false);
                    }
                    it.remove();
                }
            }
        }

        if (this.cooldown > 0) return false;
        if (!this.mob.getNavigation().isStuck()) return false;

        // Check for gap
        BlockPos pos = this.mob.blockPosition();
        BlockPos forward = pos.relative(this.mob.getDirection());
        
        // Simple gap check: Air below forward position
        if (this.mob.level().getBlockState(forward).isAir() && this.mob.level().getBlockState(forward.below()).isAir()) {
            return true;
        }
        
        // Simple wall check: Solid block forward, Air above
        if (this.mob.level().getBlockState(forward).isSolid() && this.mob.level().getBlockState(forward.above()).isAir()) {
             // Climb check
             return true;
        }

        return false;
    }

    @Override
    public void start() {
        BlockPos pos = this.mob.blockPosition();
        BlockPos forward = pos.relative(this.mob.getDirection());
        
        // Bridge
        if (this.mob.level().getBlockState(forward).isAir() && this.mob.level().getBlockState(forward.below()).isAir()) {
            BlockPos placePos = forward.below();
            if (this.mob.level().setBlock(placePos, Blocks.DIRT.defaultBlockState(), 3)) {
                placedBlocks.put(placePos.asLong(), 100); // 5 seconds
                this.cooldown = 20;
            }
        } 
        // Climb (place block at feet if stuck against wall)
        else if (this.mob.level().getBlockState(forward).isSolid()) {
             if (this.mob.level().getBlockState(pos).isAir()) { // Ensure space
                 // Jump and place? Or place below?
                 // For now, let's just place a block at current pos to step up if needed, but that might suffocate.
                 // Better: Place block at current pos if we are in air (jumping)
             }
        }
    }
    
    @Override
    public boolean canContinueToUse() {
        return false; // One-shot action
    }
}

package net.zootycoon.entity.ai.navigation;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathFinder;

public class ZooPathNavigation extends GroundPathNavigation {

    public ZooPathNavigation(Mob mob, Level level) {
        super(mob, level);
        this.setCanOpenDoors(true);
        this.setCanPassDoors(true);
    }

    @Override
    protected PathFinder createPathFinder(int maxVisitedNodes) {
        this.nodeEvaluator = new ZooWalkNodeEvaluator();
        this.nodeEvaluator.setCanPassDoors(true);
        this.nodeEvaluator.setCanOpenDoors(true);
        // Generous node budget allows visitors & staff to pathfind across large, block-dense zoos
        int budget = Math.max(maxVisitedNodes, 2048);
        return new PathFinder(this.nodeEvaluator, budget);
    }
}

package com.example.simulation;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

@GameTestHolder(IndoZooTycoon.MODID)
public class VisitorBehaviorGameTests {
    @GameTest(template = "empty")
    public static void visitorTargetsTaggedAnimal(GameTestHelper helper) {
        Cow cow = helper.spawn(EntityType.COW, 2, 1, 2);
        ZooData data = ZooData.get(helper.getLevel());
        data.addAnimal(cow.getId(), "Cow", "cow");

        VisitorEntity visitor = (VisitorEntity) helper.spawn(IndoZooTycoon.VISITOR_ENTITY.get(), 1, 1, 1);
        visitor.setHunter(false);

        helper.runAtTickTime(80, () -> {
            if (visitor.getWatchingAnimalId() != cow.getId()) {
                helper.fail("Visitor tidak menarget hewan bertag");
                return;
            }
            helper.succeed();
        });
    }

    @GameTest(template = "empty")
    public static void visitorDistributionAcrossTaggedAnimals(GameTestHelper helper) {
        Cow cowA = helper.spawn(EntityType.COW, 2, 1, 2);
        Cow cowB = helper.spawn(EntityType.COW, 6, 1, 2);
        ZooData data = ZooData.get(helper.getLevel());
        data.addAnimal(cowA.getId(), "CowA", "cow");
        data.addAnimal(cowB.getId(), "CowB", "cow");

        VisitorEntity v1 = (VisitorEntity) helper.spawn(IndoZooTycoon.VISITOR_ENTITY.get(), 1, 1, 1);
        VisitorEntity v2 = (VisitorEntity) helper.spawn(IndoZooTycoon.VISITOR_ENTITY.get(), 7, 1, 1);
        v1.setHunter(false);
        v2.setHunter(false);

        helper.runAtTickTime(120, () -> {
            int viewersA = data.getViewerCount(cowA.getId());
            int viewersB = data.getViewerCount(cowB.getId());
            if (viewersA < 1) {
                helper.fail("Distribusi visitor tidak merata untuk hewan A");
                return;
            }
            if (viewersB < 1) {
                helper.fail("Distribusi visitor tidak merata untuk hewan B");
                return;
            }
            helper.succeed();
        });
    }

    @GameTest(template = "empty")
    public static void hunterRunsWhenCarrying(GameTestHelper helper) {
        VisitorEntity hunter = (VisitorEntity) helper.spawn(IndoZooTycoon.VISITOR_ENTITY.get(), 1, 1, 1);
        hunter.setHunter(true);
        hunter.setHunterMode(VisitorEntity.HunterMode.KIDNAPPER);
        Animal cow = helper.spawn(EntityType.COW, 2, 1, 1);
        ZooData data = ZooData.get(helper.getLevel());
        data.addAnimal(cow.getId(), "Cow", "cow");

        helper.runAtTickTime(120, () -> {
            double speed = hunter.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED).getBaseValue();
            if (!hunter.isSprinting()) {
                helper.fail("Hunter tidak sprint saat carry");
                return;
            }
            if (speed <= 0.4D) {
                helper.fail("Hunter tidak masuk run mode");
                return;
            }
            helper.succeed();
        });
    }

    @GameTest(template = "empty")
    public static void visitorNotStuck(GameTestHelper helper) {
        Cow cow = helper.spawn(EntityType.COW, 6, 1, 2);
        ZooData data = ZooData.get(helper.getLevel());
        data.addAnimal(cow.getId(), "Cow", "cow");

        VisitorEntity visitor = (VisitorEntity) helper.spawn(IndoZooTycoon.VISITOR_ENTITY.get(), 1, 1, 1);
        visitor.setHunter(false);
        Vec3 start = visitor.position();

        helper.runAtTickTime(120, () -> {
            double moved = visitor.position().distanceTo(start);
            if (moved <= 0.2D) {
                helper.fail("Visitor tidak bergerak");
                return;
            }
            helper.succeed();
        });
    }

    @GameTest(template = "empty")
    public static void hunterPlacesBlockWhenBlocked(GameTestHelper helper) {
        Cow cow = helper.spawn(EntityType.COW, 6, 1, 1);
        ZooData data = ZooData.get(helper.getLevel());
        data.addAnimal(cow.getId(), "Cow", "minecraft:cow");

        helper.setBlock(2, 1, 1, Blocks.STONE.defaultBlockState());

        VisitorEntity hunter = (VisitorEntity) helper.spawn(IndoZooTycoon.VISITOR_ENTITY.get(), 1, 1, 1);
        hunter.setHunter(true);
        hunter.setHunterMode(VisitorEntity.HunterMode.KIDNAPPER);

        helper.runAtTickTime(140, () -> {
            if (!helper.getBlockState(new net.minecraft.core.BlockPos(2, 1, 1)).is(Blocks.STONE)) {
                helper.fail("Hunter merusak struktur (stone hilang)");
                return;
            }
            BlockPos feet = hunter.blockPosition();
            boolean dirtAtFeet = helper.getBlockState(feet).is(Blocks.DIRT);
            boolean dirtBelow = helper.getBlockState(feet.below()).is(Blocks.DIRT);
            if (!dirtAtFeet && !dirtBelow) {
                helper.fail("Hunter tidak menaruh dirt di bawah kaki");
                return;
            }
            helper.succeed();
        });
    }
}

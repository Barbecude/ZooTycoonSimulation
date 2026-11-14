package net.zootycoon.mixin;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.Animal;
import net.zootycoon.animal.ZooAnimalHungerManager;
import net.zootycoon.network.ZooNetworkPayloads;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.UUID;

@Mixin(Animal.class)
public abstract class AnimalBreedingMixin {

    @Inject(method = "spawnChildFromBreeding", at = @At("TAIL"))
    private void zooTycoon$onBabySpawned(ServerLevel serverLevel, Animal mate, CallbackInfo ci) {
        Animal parentA = (Animal) (Object) this;
        Animal parentB = mate;

        UUID ownerUuid = ZooAnimalHungerManager.getOwnerUuid(parentA);
        if (ownerUuid == null && parentB != null) {
            ownerUuid = ZooAnimalHungerManager.getOwnerUuid(parentB);
        }

        if (ownerUuid == null) {
            return;
        }

        ZooAnimalHungerManager.markParentsPaired(parentA, parentB);

        List<AgeableMob> babies = serverLevel.getEntitiesOfClass(
                AgeableMob.class,
                parentA.getBoundingBox().inflate(3.5D),
                b -> b.isBaby() && b.tickCount <= 3
        );

        for (AgeableMob baby : babies) {
            if (!ZooAnimalHungerManager.isTagged(baby)) {
                ZooAnimalHungerManager.setTagged(baby, true);
                ZooAnimalHungerManager.setOwner(baby, ownerUuid);
                baby.addTag("zoo_offspring");
                baby.addTag("zoo_parent_" + parentA.getStringUUID());
                ZooAnimalHungerManager.setHunger(baby, ZooAnimalHungerManager.MAX_HUNGER);
                ZooAnimalHungerManager.setThirst(baby, ZooAnimalHungerManager.MAX_THIRST);
                ZooAnimalHungerManager.setCleanliness(baby, ZooAnimalHungerManager.MAX_CLEANLINESS);

                // Auto-assign zookeeper from parent to baby
                UUID parentZkUuid = ZooAnimalHungerManager.getAssignedZookeeper(parentA);
                if (parentZkUuid == null && parentB != null) {
                    parentZkUuid = ZooAnimalHungerManager.getAssignedZookeeper(parentB);
                }
                if (parentZkUuid == null) {
                    parentZkUuid = ZooAnimalHungerManager.findExistingZookeeperForSpecies(serverLevel, ownerUuid, baby.getType());
                }
                if (parentZkUuid != null) {
                    ZooAnimalHungerManager.setAssignedZookeeper(baby, parentZkUuid);
                }

                serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, baby.getX(), baby.getY() + 0.5D, baby.getZ(), 10, 0.3, 0.3, 0.3, 0.02);

                ServerPlayer ownerPlayer = serverLevel.getServer().getPlayerList().getPlayer(ownerUuid);
                if (ownerPlayer != null) {
                    ZooNetworkPayloads.sendToast(ownerPlayer, "zootycoon.toast.title.animal", "zootycoon.animal.baby_born", 1, baby.getName().getString());
                    ZooNetworkPayloads.sendAnimalSync(ownerPlayer);
                }
            }
        }
    }
}


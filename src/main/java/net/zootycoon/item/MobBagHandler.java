package net.zootycoon.item;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.zootycoon.animal.ZooAnimalHungerManager;
import net.zootycoon.entity.ZooBuilderEntity;
import net.zootycoon.entity.ZooCashierEntity;
import net.zootycoon.entity.ZooHunterEntity;
import net.zootycoon.entity.ZooSecurityEntity;
import net.zootycoon.entity.ZooVehicleEntity;
import net.zootycoon.entity.ZooVisitorEntity;
import net.zootycoon.entity.ZooZookeeperEntity;
import net.zootycoon.network.ZooNetworkPayloads;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class MobBagHandler {

    public static final int CMD_BAG_EMPTY = 102207;
    public static final int CMD_BAG_FULL = 102208;

    private static final Set<String> LARGE_OR_MEDIUM_ANIMAL_KEYWORDS = Set.of(
            "giraffe", "elephant", "rhino", "rhinoceros", "hippo", "hippopotamus",
            "bear", "grizzly", "polar_bear", "panda", "sun_bear", "spectacled_bear",
            "lion", "tiger", "leopard", "snow_leopard", "cheetah", "jaguar", "cougar", "puma", "panther",
            "hyena", "gorilla", "orangutan", "chimpanzee", "mandrill", "gelada", "baboon",
            "bison", "buffalo", "yak", "moose", "elk", "deer", "reindeer", "caribou",
            "gazelle", "antelope", "impala", "wildebeest", "okapi", "zebra",
            "horse", "donkey", "mule", "camel", "dromedary", "llama", "trader_llama", "alpaca",
            "cow", "mooshroom", "bull", "goat", "sheep", "ram", "pig", "hog", "boar", "warthog",
            "tapir", "anteater", "aardvark", "kangaroo", "wallaby",
            "ostrich", "emu", "cassowary", "shoebill", "flamingo",
            "crocodile", "alligator", "caiman", "gharial", "komodo", "anaconda",
            "sniffer", "alligator_snapping_turtle",
            "shark", "orca", "whale", "cachalot", "dolphin", "seal", "sea_lion", "walrus", "manatee", "giant_squid",
            "arapaima", "sturgeon", "wolf", "direwolf", "maned_wolf"
    );

    private MobBagHandler() {}

    public static boolean isMobBag(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !stack.is(Items.WARPED_FUNGUS_ON_A_STICK)) {
            return false;
        }
        CustomModelData cmd = stack.get(DataComponents.CUSTOM_MODEL_DATA);
        if (cmd != null && (cmd.value() == CMD_BAG_EMPTY || cmd.value() == CMD_BAG_FULL)) {
            return true;
        }
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        if (cd != null) {
            CompoundTag tag = cd.copyTag();
            return tag.getBoolean("zoo_mob_bag")
                    || tag.getBoolean("jbt_bag")
                    || tag.contains("StoredEntityTag")
                    || tag.contains("jbt_filled");
        }
        return false;
    }

    public static boolean isBagFilled(ItemStack stack) {
        if (!isMobBag(stack)) {
            return false;
        }
        CustomModelData cmd = stack.get(DataComponents.CUSTOM_MODEL_DATA);
        if (cmd != null && cmd.value() == CMD_BAG_FULL) {
            return true;
        }
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        if (cd != null) {
            CompoundTag tag = cd.copyTag();
            return tag.getBoolean("zoo_bag_filled")
                    || tag.getBoolean("jbt_filled")
                    || tag.contains("StoredEntityTag");
        }
        return false;
    }

    public static boolean isCapturableAnimal(Entity entity) {
        if (!(entity instanceof LivingEntity living) || !living.isAlive()) {
            return false;
        }
        if (entity instanceof Player
                || entity instanceof Npc
                || entity instanceof Monster
                || entity instanceof ZooBuilderEntity
                || entity instanceof ZooZookeeperEntity
                || entity instanceof ZooSecurityEntity
                || entity instanceof ZooCashierEntity
                || entity instanceof ZooVisitorEntity
                || entity instanceof ZooHunterEntity
                || entity instanceof ZooVehicleEntity) {
            return false;
        }
        return entity instanceof Animal
                || entity instanceof AmbientCreature
                || entity instanceof WaterAnimal
                || ZooAnimalHungerManager.isZooAnimalCandidate(living);
    }

    public static boolean isSmallAnimal(LivingEntity animal) {
        if (!isCapturableAnimal(animal)) {
            return false;
        }
        ResourceLocation typeKey = BuiltInRegistries.ENTITY_TYPE.getKey(animal.getType());
        if (typeKey != null) {
            String path = typeKey.getPath().toLowerCase();
            for (String forbidden : LARGE_OR_MEDIUM_ANIMAL_KEYWORDS) {
                if (path.equals(forbidden)
                        || path.startsWith(forbidden + "_")
                        || path.endsWith("_" + forbidden)
                        || path.contains("_" + forbidden + "_")) {
                    return false;
                }
            }
        }
        return animal.getBbWidth() <= 0.85F && animal.getBbHeight() <= 1.05F;
    }

    public static void initServerEvents() {
        // 1. Direct right-click on an entity
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            ItemStack stack = player.getItemInHand(hand);
            if (!isMobBag(stack)) {
                return InteractionResult.PASS;
            }
            if (world.isClientSide()) {
                return InteractionResult.SUCCESS;
            }
            if (!(player instanceof ServerPlayer serverPlayer) || !(world instanceof ServerLevel sLevel)) {
                return InteractionResult.SUCCESS;
            }

            sanitizeLegacyJbtTag(stack);

            if (isBagFilled(stack)) {
                Vec3 releasePos = entity.position().add(0.0, 0.2, 0.0);
                releaseAnimalFromBag(sLevel, serverPlayer, stack, releasePos);
                return InteractionResult.SUCCESS;
            }

            if (isCapturableAnimal(entity) && entity instanceof LivingEntity living) {
                tryCaptureAnimal(sLevel, serverPlayer, stack, living);
                return InteractionResult.SUCCESS;
            }

            return InteractionResult.SUCCESS;
        });

        // 2. Right-click on a block
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            ItemStack stack = player.getItemInHand(hand);
            if (!isMobBag(stack)) {
                return InteractionResult.PASS;
            }
            if (world.isClientSide()) {
                return InteractionResult.SUCCESS;
            }
            if (!(player instanceof ServerPlayer serverPlayer) || !(world instanceof ServerLevel sLevel)) {
                return InteractionResult.SUCCESS;
            }

            sanitizeLegacyJbtTag(stack);

            if (isBagFilled(stack)) {
                Direction face = hitResult.getDirection();
                BlockPos targetPos = hitResult.getBlockPos().relative(face);
                Vec3 releasePos = new Vec3(targetPos.getX() + 0.5, targetPos.getY() + 0.1, targetPos.getZ() + 0.5);
                releaseAnimalFromBag(sLevel, serverPlayer, stack, releasePos);
                return InteractionResult.SUCCESS;
            }

            // If empty and clicking near a small animal, check raycast entity hit first
            LivingEntity aimedAnimal = raycastAnimal(player, 5.0D);
            if (aimedAnimal != null) {
                tryCaptureAnimal(sLevel, serverPlayer, stack, aimedAnimal);
                return InteractionResult.SUCCESS;
            }

            return InteractionResult.SUCCESS;
        });

        // 3. Right-click in the air (raycast up to 5 blocks, matching JBT behavior)
        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack stack = player.getItemInHand(hand);
            if (!isMobBag(stack)) {
                return InteractionResultHolder.pass(stack);
            }
            if (world.isClientSide()) {
                return InteractionResultHolder.success(stack);
            }
            if (!(player instanceof ServerPlayer serverPlayer) || !(world instanceof ServerLevel sLevel)) {
                return InteractionResultHolder.success(stack);
            }

            sanitizeLegacyJbtTag(stack);

            if (isBagFilled(stack)) {
                Vec3 eye = player.getEyePosition();
                Vec3 end = eye.add(player.getViewVector(1.0F).scale(5.0D));
                BlockHitResult blockHit = world.clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
                if (blockHit.getType() == HitResult.Type.BLOCK) {
                    BlockPos targetPos = blockHit.getBlockPos().relative(blockHit.getDirection());
                    Vec3 releasePos = new Vec3(targetPos.getX() + 0.5, targetPos.getY() + 0.1, targetPos.getZ() + 0.5);
                    releaseAnimalFromBag(sLevel, serverPlayer, stack, releasePos);
                }
                return InteractionResultHolder.success(stack);
            } else {
                LivingEntity aimedAnimal = raycastAnimal(player, 5.0D);
                if (aimedAnimal != null) {
                    tryCaptureAnimal(sLevel, serverPlayer, stack, aimedAnimal);
                }
                return InteractionResultHolder.success(stack);
            }
        });
    }

    private static LivingEntity raycastAnimal(Player player, double maxDist) {
        Vec3 start = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0F);
        Vec3 end = start.add(look.scale(maxDist));
        AABB searchBox = player.getBoundingBox().expandTowards(look.scale(maxDist)).inflate(1.0D);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(
                player,
                start,
                end,
                searchBox,
                e -> isCapturableAnimal(e) && !e.isSpectator(),
                maxDist * maxDist
        );
        if (hit != null && hit.getEntity() instanceof LivingEntity living) {
            return living;
        }
        return null;
    }

    private static void sanitizeLegacyJbtTag(ItemStack stack) {
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        if (cd == null) {
            return;
        }
        CompoundTag tag = cd.copyTag();
        if (tag.contains("jbt_bag")) {
            tag.remove("jbt_bag");
            tag.putBoolean("zoo_mob_bag", true);
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
    }

    public static boolean tryCaptureAnimal(ServerLevel sLevel, ServerPlayer player, ItemStack bagStack, LivingEntity target) {
        if (!isSmallAnimal(target)) {
            sLevel.playSound(null, target.blockPosition(), SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 1.0F, 1.0F);
            ZooNetworkPayloads.sendToast(
                    player,
                    "zootycoon.toast.title.warning",
                    "zootycoon.mob_bag.too_large",
                    2
            );
            return false;
        }

        if (target.isPassenger()) {
            target.stopRiding();
        }
        if (target instanceof Mob mob && mob.isLeashed()) {
            mob.dropLeash(true, true);
        }

        ResourceLocation typeId = BuiltInRegistries.ENTITY_TYPE.getKey(target.getType());
        if (typeId == null) {
            return false;
        }

        String displayName = target.getDisplayName().getString();

        CompoundTag entityTag = new CompoundTag();
        target.saveWithoutId(entityTag);
        entityTag.remove("Pos");
        entityTag.remove("Motion");
        entityTag.remove("Rotation");
        entityTag.remove("FallDistance");
        entityTag.remove("Fire");
        entityTag.remove("OnGround");
        entityTag.remove("PortalCooldown");
        entityTag.remove("UUID");
        entityTag.remove("Leash");
        entityTag.remove("Passengers");

        CompoundTag bagTag = new CompoundTag();
        bagTag.putBoolean("zoo_mob_bag", true);
        bagTag.putBoolean("zoo_bag_filled", true);
        bagTag.putString("StoredEntityType", typeId.toString());
        bagTag.putString("StoredEntityName", displayName);
        bagTag.put("StoredEntityTag", entityTag);

        bagStack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(CMD_BAG_FULL));
        bagStack.set(DataComponents.CUSTOM_DATA, CustomData.of(bagTag));
        bagStack.set(
                DataComponents.LORE,
                new ItemLore(List.of(
                        Component.literal(displayName).withStyle(s -> s.withColor(ChatFormatting.BLUE).withItalic(false))
                ))
        );

        sLevel.playSound(null, target.blockPosition(), SoundEvents.BUNDLE_INSERT, SoundSource.PLAYERS, 1.2F, 0.9F);
        sLevel.sendParticles(
                ParticleTypes.CLOUD,
                target.getX(),
                target.getY() + target.getBbHeight() * 0.5,
                target.getZ(),
                15, 0.3, 0.3, 0.3, 0.05
        );

        target.discard();

        ZooNetworkPayloads.sendToast(
                player,
                "zootycoon.toast.title.info",
                "zootycoon.mob_bag.captured",
                0,
                displayName
        );
        return true;
    }

    public static boolean releaseAnimalFromBag(ServerLevel sLevel, ServerPlayer player, ItemStack bagStack, Vec3 spawnPos) {
        CustomData cd = bagStack.get(DataComponents.CUSTOM_DATA);
        if (cd == null) {
            return false;
        }
        CompoundTag bagTag = cd.copyTag();
        String typeStr = bagTag.getString("StoredEntityType");
        String storedName = bagTag.getString("StoredEntityName");
        CompoundTag entityTag = bagTag.getCompound("StoredEntityTag");

        boolean spawned = false;
        if (!typeStr.isEmpty()) {
            Optional<EntityType<?>> typeOpt = EntityType.byString(typeStr);
            if (typeOpt.isPresent()) {
                Entity created = typeOpt.get().create(sLevel);
                if (created != null) {
                    if (!entityTag.isEmpty()) {
                        created.load(entityTag);
                    }
                    created.setUUID(UUID.randomUUID());
                    created.moveTo(spawnPos.x, spawnPos.y, spawnPos.z, player.getYRot(), 0.0F);
                    created.setDeltaMovement(Vec3.ZERO);
                    created.setInvulnerable(false);
                    if (created instanceof Mob mob) {
                        mob.setNoAi(false);
                    }
                    sLevel.addFreshEntity(created);
                    if (storedName.isEmpty()) {
                        storedName = created.getDisplayName().getString();
                    }
                    spawned = true;
                }
            }
        } else if (bagTag.getBoolean("jbt_filled")) {
            // Fallback for any legacy JBT bag entity
            for (ServerLevel lvl : sLevel.getServer().getAllLevels()) {
                for (Entity ent : lvl.getAllEntities()) {
                    if (ent.getTags().contains("jbt_bag_contained")) {
                        ent.removeTag("jbt_bag_contained");
                        ent.setInvulnerable(false);
                        if (ent instanceof Mob mob) {
                            mob.setNoAi(false);
                        }
                        ent.teleportTo(sLevel, spawnPos.x, spawnPos.y, spawnPos.z, Set.of(), player.getYRot(), 0.0F);
                        storedName = ent.getDisplayName().getString();
                        spawned = true;
                        break;
                    }
                }
                if (spawned) break;
            }
        }

        if (!spawned) {
            return false;
        }

        CompoundTag emptyTag = new CompoundTag();
        emptyTag.putBoolean("zoo_mob_bag", true);
        emptyTag.putBoolean("zoo_bag_filled", false);

        bagStack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(CMD_BAG_EMPTY));
        bagStack.set(DataComponents.CUSTOM_DATA, CustomData.of(emptyTag));
        bagStack.set(DataComponents.LORE, ItemLore.EMPTY);

        sLevel.playSound(null, BlockPos.containing(spawnPos), SoundEvents.BUNDLE_DROP_CONTENTS, SoundSource.PLAYERS, 1.2F, 0.9F);
        sLevel.sendParticles(
                ParticleTypes.CLOUD,
                spawnPos.x,
                spawnPos.y + 0.4,
                spawnPos.z,
                15, 0.3, 0.3, 0.3, 0.05
        );

        ZooNetworkPayloads.sendToast(
                player,
                "zootycoon.toast.title.info",
                "zootycoon.mob_bag.released",
                0,
                storedName.isEmpty() ? "Satwa" : storedName
        );
        return true;
    }
}

package net.zootycoon.entity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.zootycoon.ZooTycoonMod;

public class ZooEntities {
    public static final ResourceKey<EntityType<?>> BUILDER_KEY = ResourceKey.create(
            Registries.ENTITY_TYPE,
            ZooTycoonMod.id("builder")
    );

    public static final EntityType<ZooBuilderEntity> BUILDER = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            ZooTycoonMod.id("builder"),
            EntityType.Builder.of(ZooBuilderEntity::new, MobCategory.MISC)
                    .sized(0.6f, 1.8f)
                    .eyeHeight(1.62f)
                    .build("zootycoon:builder")
    );

    public static final ResourceKey<EntityType<?>> ZOOKEEPER_KEY = ResourceKey.create(
            Registries.ENTITY_TYPE,
            ZooTycoonMod.id("zookeeper")
    );

    public static final EntityType<ZooZookeeperEntity> ZOOKEEPER = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            ZooTycoonMod.id("zookeeper"),
            EntityType.Builder.of(ZooZookeeperEntity::new, MobCategory.MISC)
                    .sized(0.6f, 1.8f)
                    .eyeHeight(1.62f)
                    .build("zootycoon:zookeeper")
    );

    public static final ResourceKey<EntityType<?>> SECURITY_KEY = ResourceKey.create(
            Registries.ENTITY_TYPE,
            ZooTycoonMod.id("security")
    );

    public static final EntityType<ZooSecurityEntity> SECURITY = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            ZooTycoonMod.id("security"),
            EntityType.Builder.of(ZooSecurityEntity::new, MobCategory.MISC)
                    .sized(0.6f, 1.8f)
                    .eyeHeight(1.62f)
                    .build("zootycoon:security")
    );

    public static final ResourceKey<EntityType<?>> HUNTER_KEY = ResourceKey.create(
            Registries.ENTITY_TYPE,
            ZooTycoonMod.id("hunter")
    );

    public static final EntityType<ZooHunterEntity> HUNTER = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            ZooTycoonMod.id("hunter"),
            EntityType.Builder.of(ZooHunterEntity::new, MobCategory.MONSTER)
                    .sized(0.6f, 1.8f)
                    .eyeHeight(1.62f)
                    .build("zootycoon:hunter")
    );

    public static final ResourceKey<EntityType<?>> VISITOR_KEY = ResourceKey.create(
            Registries.ENTITY_TYPE,
            ZooTycoonMod.id("visitor")
    );

    public static final EntityType<ZooVisitorEntity> VISITOR = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            ZooTycoonMod.id("visitor"),
            EntityType.Builder.of(ZooVisitorEntity::new, MobCategory.MISC)
                    .sized(0.6f, 1.8f)
                    .eyeHeight(1.62f)
                    .build("zootycoon:visitor")
    );

    public static final ResourceKey<EntityType<?>> TOILET_SEAT_KEY = ResourceKey.create(
            Registries.ENTITY_TYPE,
            ZooTycoonMod.id("toilet_seat")
    );

    public static final EntityType<ToiletSeatEntity> TOILET_SEAT = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            ZooTycoonMod.id("toilet_seat"),
            EntityType.Builder.of(ToiletSeatEntity::new, MobCategory.MISC)
                    .sized(0.0f, 0.0f)
                    .build("zootycoon:toilet_seat")
    );

    public static void register() {
        FabricDefaultAttributeRegistry.register(BUILDER, ZooBuilderEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ZOOKEEPER, ZooZookeeperEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(SECURITY, ZooSecurityEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(HUNTER, ZooHunterEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(VISITOR, ZooVisitorEntity.createAttributes());
        ZooTycoonMod.LOGGER.info("Registered Zoo Tycoon NPC entities: Builder, Zookeeper, Security, Hunter, and Visitor.");
    }
}

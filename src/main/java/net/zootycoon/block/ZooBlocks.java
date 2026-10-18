package net.zootycoon.block;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.zootycoon.ZooTycoonMod;
import net.zootycoon.construction.ConstructionTapeBlock;

public class ZooBlocks {
    // 1. Construction Tape
    public static final ResourceKey<Item> CONSTRUCTION_TAPE_ITEM_KEY = ResourceKey.create(
            Registries.ITEM,
            ZooTycoonMod.id("construction_tape")
    );
    public static final Block CONSTRUCTION_TAPE = new ConstructionTapeBlock();
    public static final BlockItem CONSTRUCTION_TAPE_ITEM = new BlockItem(
            CONSTRUCTION_TAPE,
            new Item.Properties()
    );

    // 2. Toilet Block
    public static final ResourceKey<Item> TOILET_ITEM_KEY = ResourceKey.create(
            Registries.ITEM,
            ZooTycoonMod.id("toilet")
    );
    public static final Block TOILET = new ToiletBlock();
    public static final BlockItem TOILET_ITEM = new BlockItem(
            TOILET,
            new Item.Properties()
    );

    // 3. Trash Can Block (Tong Sampah)
    public static final ResourceKey<Item> TRASH_CAN_ITEM_KEY = ResourceKey.create(
            Registries.ITEM,
            ZooTycoonMod.id("trash_can")
    );
    public static final Block TRASH_CAN = new TrashCanBlock();
    public static final BlockItem TRASH_CAN_ITEM = new BlockItem(
            TRASH_CAN,
            new Item.Properties()
    );

    // 4. Trash / Litter Block (Sampah Berserakan)
    public static final ResourceKey<Item> TRASH_ITEM_KEY = ResourceKey.create(
            Registries.ITEM,
            ZooTycoonMod.id("trash")
    );
    public static final Block TRASH = new TrashBlock();
    public static final BlockItem TRASH_ITEM = new BlockItem(
            TRASH,
            new Item.Properties()
    );

    // 5. Feeding Trough Block
    public static final ResourceKey<Item> FEEDING_TROUGH_ITEM_KEY = ResourceKey.create(
            Registries.ITEM,
            ZooTycoonMod.id("feeding_trough")
    );
    public static final Block FEEDING_TROUGH = new ZooFeedingTroughBlock();
    public static final BlockItem FEEDING_TROUGH_ITEM = new BlockItem(
            FEEDING_TROUGH,
            new Item.Properties()
    );

    // 6. Toilet Block Entity
    public static final ResourceKey<BlockEntityType<?>> TOILET_BE_KEY = ResourceKey.create(
            Registries.BLOCK_ENTITY_TYPE,
            ZooTycoonMod.id("toilet")
    );
    public static final BlockEntityType<ToiletBlockEntity> TOILET_BLOCK_ENTITY = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            ZooTycoonMod.id("toilet"),
            FabricBlockEntityTypeBuilder.create(ToiletBlockEntity::new, TOILET).build()
    );

    // 7. Feeding Trough Block Entity
    public static final ResourceKey<BlockEntityType<?>> FEEDING_TROUGH_BE_KEY = ResourceKey.create(
            Registries.BLOCK_ENTITY_TYPE,
            ZooTycoonMod.id("feeding_trough")
    );
    public static final BlockEntityType<ZooFeedingTroughBlockEntity> FEEDING_TROUGH_BLOCK_ENTITY = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            ZooTycoonMod.id("feeding_trough"),
            FabricBlockEntityTypeBuilder.create(ZooFeedingTroughBlockEntity::new, FEEDING_TROUGH).build()
    );

    // 8. Gacha Machine Block
    public static final ResourceKey<Block> GACHA_MACHINE_KEY = ResourceKey.create(
            Registries.BLOCK,
            ZooTycoonMod.id("gacha_machine")
    );
    public static final ResourceKey<Item> GACHA_MACHINE_ITEM_KEY = ResourceKey.create(
            Registries.ITEM,
            ZooTycoonMod.id("gacha_machine")
    );
    public static final Block GACHA_MACHINE = new ZooGachaMachineBlock();
    public static final BlockItem GACHA_MACHINE_ITEM = new BlockItem(
            GACHA_MACHINE,
            new Item.Properties()
    );

    // 9. Gacha Machine Block Entity
    public static final ResourceKey<BlockEntityType<?>> GACHA_MACHINE_BE_KEY = ResourceKey.create(
            Registries.BLOCK_ENTITY_TYPE,
            ZooTycoonMod.id("gacha_machine")
    );
    public static final BlockEntityType<ZooGachaMachineBlockEntity> GACHA_MACHINE_BLOCK_ENTITY = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            ZooTycoonMod.id("gacha_machine"),
            FabricBlockEntityTypeBuilder.create(ZooGachaMachineBlockEntity::new, GACHA_MACHINE).build()
    );

    // 10. Heater Block
    public static final ResourceKey<Block> HEATER_KEY = ResourceKey.create(
            Registries.BLOCK,
            ZooTycoonMod.id("heater")
    );
    public static final ResourceKey<Item> HEATER_ITEM_KEY = ResourceKey.create(
            Registries.ITEM,
            ZooTycoonMod.id("heater")
    );
    public static final Block HEATER = new ZooHeaterBlock();
    public static final BlockItem HEATER_ITEM = new BlockItem(
            HEATER,
            new Item.Properties()
    );
    public static final BlockEntityType<net.zootycoon.block.entity.ZooThermalBlockEntity> HEATER_BLOCK_ENTITY = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            ZooTycoonMod.id("heater"),
            FabricBlockEntityTypeBuilder.create((pos, state) -> new net.zootycoon.block.entity.ZooThermalBlockEntity(pos, state, net.zootycoon.block.entity.ZooThermalBlockEntity.ThermalType.HEATING), HEATER).build()
    );

    // 11. Cooler Box Block
    public static final ResourceKey<Block> COOLER_KEY = ResourceKey.create(
            Registries.BLOCK,
            ZooTycoonMod.id("cooler_box")
    );
    public static final ResourceKey<Item> COOLER_ITEM_KEY = ResourceKey.create(
            Registries.ITEM,
            ZooTycoonMod.id("cooler_box")
    );
    public static final Block COOLER = new ZooCoolerBlock();
    public static final BlockItem COOLER_ITEM = new BlockItem(
            COOLER,
            new Item.Properties()
    );
    public static final BlockEntityType<net.zootycoon.block.entity.ZooThermalBlockEntity> COOLER_BLOCK_ENTITY = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            ZooTycoonMod.id("cooler_box"),
            FabricBlockEntityTypeBuilder.create((pos, state) -> new net.zootycoon.block.entity.ZooThermalBlockEntity(pos, state, net.zootycoon.block.entity.ZooThermalBlockEntity.ThermalType.COOLING), COOLER).build()
    );

    public static void register() {
        // Blocks
        Registry.register(BuiltInRegistries.BLOCK, ConstructionTapeBlock.BLOCK_KEY, CONSTRUCTION_TAPE);
        Registry.register(BuiltInRegistries.BLOCK, ToiletBlock.BLOCK_KEY, TOILET);
        Registry.register(BuiltInRegistries.BLOCK, TrashCanBlock.BLOCK_KEY, TRASH_CAN);
        Registry.register(BuiltInRegistries.BLOCK, TrashBlock.BLOCK_KEY, TRASH);
        Registry.register(BuiltInRegistries.BLOCK, ZooTycoonMod.id("feeding_trough"), FEEDING_TROUGH);
        Registry.register(BuiltInRegistries.BLOCK, GACHA_MACHINE_KEY, GACHA_MACHINE);
        Registry.register(BuiltInRegistries.BLOCK, HEATER_KEY, HEATER);
        Registry.register(BuiltInRegistries.BLOCK, COOLER_KEY, COOLER);

        // Block Items
        Registry.register(BuiltInRegistries.ITEM, CONSTRUCTION_TAPE_ITEM_KEY, CONSTRUCTION_TAPE_ITEM);
        Registry.register(BuiltInRegistries.ITEM, TOILET_ITEM_KEY, TOILET_ITEM);
        Registry.register(BuiltInRegistries.ITEM, TRASH_CAN_ITEM_KEY, TRASH_CAN_ITEM);
        Registry.register(BuiltInRegistries.ITEM, TRASH_ITEM_KEY, TRASH_ITEM);
        Registry.register(BuiltInRegistries.ITEM, FEEDING_TROUGH_ITEM_KEY, FEEDING_TROUGH_ITEM);
        Registry.register(BuiltInRegistries.ITEM, GACHA_MACHINE_ITEM_KEY, GACHA_MACHINE_ITEM);
        Registry.register(BuiltInRegistries.ITEM, HEATER_ITEM_KEY, HEATER_ITEM);
        Registry.register(BuiltInRegistries.ITEM, COOLER_ITEM_KEY, COOLER_ITEM);
    }
}

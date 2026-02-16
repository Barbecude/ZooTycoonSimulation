package com.example.simulation;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(IndoZooTycoon.MODID)
public class IndoZooTycoon {
        public static final String MODID = "indozoo";

        public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
        public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
        public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister
                        .create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);
        public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister
                        .create(ForgeRegistries.ENTITY_TYPES, MODID);
        public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES,
                        MODID);
        public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister
                        .create(Registries.CREATIVE_MODE_TAB, MODID);

        // Block
        public static final RegistryObject<Block> ZOO_COMPUTER_BLOCK = BLOCKS.register("zoo_computer",
                        ZooComputerBlock::new);
        public static final RegistryObject<Item> ZOO_COMPUTER_ITEM = ITEMS.register("zoo_computer",
                        () -> new BlockItem(ZOO_COMPUTER_BLOCK.get(), new Item.Properties()));

        public static final RegistryObject<Block> ZONE_MARKER_BLOCK = BLOCKS.register("zone_marker",
                        ZoneMarkerBlock::new);
        public static final RegistryObject<Item> ZONE_MARKER_ITEM = ITEMS.register("zone_marker",
                        () -> new BlockItem(ZONE_MARKER_BLOCK.get(), new Item.Properties()));

        public static final RegistryObject<Block> TRASH_BLOCK = BLOCKS.register("trash", TrashBlock::new);
        public static final RegistryObject<Item> TRASH_ITEM = ITEMS.register("trash",
                        () -> new BlockItem(TRASH_BLOCK.get(), new Item.Properties()));

        public static final RegistryObject<Block> FOOD_STALL_BLOCK = BLOCKS.register("food_stall", FoodStallBlock::new);
        public static final RegistryObject<Item> FOOD_STALL_ITEM = ITEMS.register("food_stall",
                        () -> new BlockItem(FOOD_STALL_BLOCK.get(), new Item.Properties()));

        public static final RegistryObject<Block> DRINK_STALL_BLOCK = BLOCKS.register("drink_stall",
                        () -> new FoodStallBlock()); // Reusing class for now
        public static final RegistryObject<Item> DRINK_STALL_ITEM = ITEMS.register("drink_stall",
                        () -> new BlockItem(DRINK_STALL_BLOCK.get(), new Item.Properties()));

        public static final RegistryObject<Block> ANIMAL_FEEDER_BLOCK = BLOCKS.register("animal_feeder",
                        AnimalFeederBlock::new);
        public static final RegistryObject<Item> ANIMAL_FEEDER_ITEM = ITEMS.register("animal_feeder",
                        () -> new BlockItem(ANIMAL_FEEDER_BLOCK.get(), new Item.Properties()));

        public static final RegistryObject<Item> CAPTURE_CAGE_ITEM = ITEMS.register("capture_cage",
                        CaptureCageItem::new);

        public static final RegistryObject<Item> BIOME_CHANGER_ITEM = ITEMS.register("biome_changer",
                        BiomeChangerItem::new);

        public static final RegistryObject<Block> RESTROOM_BLOCK = BLOCKS.register("restroom",
                        RestroomBlock::new);
        public static final RegistryObject<Item> RESTROOM_ITEM = ITEMS.register("restroom",
                        () -> new BlockItem(RESTROOM_BLOCK.get(), new Item.Properties()));

        public static final RegistryObject<Block> ZOO_BANNER_BLOCK = BLOCKS.register("zoo_banner",
                        ZooBannerBlock::new);
        public static final RegistryObject<Item> ZOO_BANNER_ITEM = ITEMS.register("zoo_banner",
                        () -> new BlockItem(ZOO_BANNER_BLOCK.get(), new Item.Properties()));

        public static final RegistryObject<Item> ANIMAL_TAG_ITEM = ITEMS.register("animal_tag",
                        AnimalTagItem::new);

        // Entities
        public static final RegistryObject<EntityType<StaffEntity>> STAFF_ENTITY = ENTITY_TYPES.register("staff",
                        () -> EntityType.Builder.of(StaffEntity::new, MobCategory.CREATURE).sized(0.6F, 1.95F)
                                        .build("staff"));
        public static final RegistryObject<EntityType<VisitorEntity>> VISITOR_ENTITY = ENTITY_TYPES.register("visitor",
                        () -> EntityType.Builder.of(VisitorEntity::new, MobCategory.CREATURE).sized(0.6F, 1.95F)
                                        .build("visitor"));

        // Spawn Eggs
        public static final RegistryObject<Item> STAFF_SPAWN_EGG = ITEMS.register("staff_spawn_egg",
                        () -> new ForgeSpawnEggItem(STAFF_ENTITY, 0x3498DB, 0x2C3E50, new Item.Properties()));
        public static final RegistryObject<Item> VISITOR_SPAWN_EGG = ITEMS.register("visitor_spawn_egg",
                        () -> new ForgeSpawnEggItem(VISITOR_ENTITY, 0xF1C40F, 0xE67E22, new Item.Properties()));

        // Block Entity
        public static final RegistryObject<BlockEntityType<AnimalFeederBlockEntity>> ANIMAL_FEEDER_BE = BLOCK_ENTITIES
                        .register("animal_feeder",
                                        () -> BlockEntityType.Builder
                                                        .of(AnimalFeederBlockEntity::new, ANIMAL_FEEDER_BLOCK.get())
                                                        .build(null));

        public static final RegistryObject<BlockEntityType<ZooComputerBlockEntity>> ZOO_COMPUTER_BE = BLOCK_ENTITIES
                        .register("zoo_computer",
                                        () -> BlockEntityType.Builder
                                                        .of(ZooComputerBlockEntity::new, ZOO_COMPUTER_BLOCK.get())
                                                        .build(null));

        // Menu
        public static final RegistryObject<MenuType<ZooComputerMenu>> ZOO_COMPUTER_MENU = MENUS.register(
                        "zoo_computer_menu",
                        () -> IForgeMenuType.create(ZooComputerMenu::new));

        // Tab
        public static final RegistryObject<CreativeModeTab> ZOO_TAB = CREATIVE_TABS.register("zoo_tab",
                        () -> CreativeModeTab.builder()
                                        .title(Component.literal("IndoZoo Tycoon"))
                                        .displayItems((params, output) -> {
                                                output.accept(ZOO_COMPUTER_ITEM.get());
                                                output.accept(ZONE_MARKER_ITEM.get());
                                                output.accept(TRASH_ITEM.get());
                                                output.accept(FOOD_STALL_ITEM.get());
                                                output.accept(DRINK_STALL_ITEM.get());
                                                output.accept(ANIMAL_FEEDER_ITEM.get());
                                                output.accept(CAPTURE_CAGE_ITEM.get());
                                                output.accept(BIOME_CHANGER_ITEM.get());
                                                output.accept(RESTROOM_ITEM.get());
                                                output.accept(ZOO_BANNER_ITEM.get());
                                                output.accept(ANIMAL_TAG_ITEM.get());
                                                output.accept(STAFF_SPAWN_EGG.get());
                                                output.accept(VISITOR_SPAWN_EGG.get());
                                        })
                                        .build());

        public IndoZooTycoon() {
                IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
                BLOCKS.register(modBus);
                ITEMS.register(modBus);
                BLOCK_ENTITIES.register(modBus);
                ENTITY_TYPES.register(modBus);
                MENUS.register(modBus);
                CREATIVE_TABS.register(modBus);
                modBus.addListener(this::onCommonSetup);
                MinecraftForge.EVENT_BUS.addListener(this::onRegisterCommands);

                // Register config
                ZooConfig.register();
                PacketHandler.register();
        }

        private void onCommonSetup(net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent event) {
                event.enqueueWork(() -> {
                        AnimalRegistry.initialize();
                        ZooItemRegistry.initialize();
                });
        }

        private void onRegisterCommands(RegisterCommandsEvent event) {
                ZooCommand.register(event.getDispatcher());
        }
}

package com.example.simulation;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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
import java.util.function.Supplier;

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
                        .create(new net.minecraft.resources.ResourceLocation("minecraft", "creative_mode_tab"), MODID);

        // Block
        public static final RegistryObject<Block> ZOO_COMPUTER_BLOCK = BLOCKS.register("zoo_computer",
                        ZooComputerBlock::new);
        public static final RegistryObject<Item> ZOO_COMPUTER_ITEM = ITEMS.register("zoo_computer",
                        () -> new BlockItem(ZOO_COMPUTER_BLOCK.get(), new Item.Properties()));

        // Food Stall
        public static final RegistryObject<Block> FOOD_STALL_BLOCK = BLOCKS.register("food_stall",
                        FoodStallBlock::new);
        public static final RegistryObject<Item> FOOD_STALL_ITEM = ITEMS.register("food_stall",
                        () -> new BlockItem(FOOD_STALL_BLOCK.get(), new Item.Properties()));

        public static final RegistryObject<Block> TRASH_BLOCK = BLOCKS.register("trash", TrashBlock::new);
        public static final RegistryObject<Item> TRASH_ITEM = ITEMS.register("trash",
                        () -> new BlockItem(TRASH_BLOCK.get(), new Item.Properties()));

        public static final RegistryObject<Block> TRASH_CAN_BLOCK = BLOCKS.register("trash_can", TrashCanBlock::new);
        public static final RegistryObject<Item> TRASH_CAN_ITEM = ITEMS.register("trash_can",
                        () -> new BlockItem(TRASH_CAN_BLOCK.get(), new Item.Properties()));

        public static final RegistryObject<Block> ANIMAL_FEEDER_BLOCK = BLOCKS.register("animal_feeder",
                        AnimalFeederBlock::new);
        public static final RegistryObject<Item> ANIMAL_FEEDER_ITEM = ITEMS.register("animal_feeder",
                        () -> new BlockItem(ANIMAL_FEEDER_BLOCK.get(), new Item.Properties()));

        public static final RegistryObject<Item> CAPTURE_CAGE_ITEM = ITEMS.register("capture_cage",
                        CaptureCageItem::new);

        public static final RegistryObject<Item> BIOME_CHANGER_ITEM = ITEMS.register("biome_changer",
                        BiomeChangerItem::new);

        public static final RegistryObject<Block> RESTROOM_BLOCK = BLOCKS.register("toilet",
                        RestroomBlock::new);
        public static final RegistryObject<Item> RESTROOM_ITEM = ITEMS.register("toilet",
                        () -> new BlockItem(RESTROOM_BLOCK.get(), new Item.Properties()));

        // --- Hunter sack items ---
        public static final RegistryObject<Item> HUNTER_SACK_SMALL = ITEMS.register("hunter_sack_small",
                        () -> new Item(new Item.Properties()));
        public static final RegistryObject<Item> HUNTER_SACK_BIG = ITEMS.register("hunter_sack_big",
                        () -> new Item(new Item.Properties()));

        // --- Food & Drink items (FD-style, standalone) ---
        public static final RegistryObject<Item> FD_HAMBURGER               = ITEMS.register("fd_hamburger",               () -> new Item(new Item.Properties()));
        public static final RegistryObject<Item> FD_CHICKEN_SANDWICH        = ITEMS.register("fd_chicken_sandwich",        () -> new Item(new Item.Properties()));
        public static final RegistryObject<Item> FD_BACON_SANDWICH          = ITEMS.register("fd_bacon_sandwich",          () -> new Item(new Item.Properties()));
        public static final RegistryObject<Item> FD_BARBECUE_STICK          = ITEMS.register("fd_barbecue_stick",          () -> new Item(new Item.Properties()));
        public static final RegistryObject<Item> FD_BEEF_PATTY              = ITEMS.register("fd_beef_patty",              () -> new Item(new Item.Properties()));
        public static final RegistryObject<Item> FD_EGG_SANDWICH            = ITEMS.register("fd_egg_sandwich",            () -> new Item(new Item.Properties()));
        public static final RegistryObject<Item> FD_MUTTON_WRAP             = ITEMS.register("fd_mutton_wrap",             () -> new Item(new Item.Properties()));
        public static final RegistryObject<Item> FD_HONEY_COOKIE            = ITEMS.register("fd_honey_cookie",            () -> new Item(new Item.Properties()));
        public static final RegistryObject<Item> FD_SWEET_BERRY_COOKIE      = ITEMS.register("fd_sweet_berry_cookie",      () -> new Item(new Item.Properties()));
        public static final RegistryObject<Item> FD_CAKE_SLICE              = ITEMS.register("fd_cake_slice",              () -> new Item(new Item.Properties()));
        public static final RegistryObject<Item> FD_SWEET_BERRY_CHEESECAKE  = ITEMS.register("fd_sweet_berry_cheesecake_slice",  () -> new Item(new Item.Properties()));
        public static final RegistryObject<Item> FD_APPLE_PIE_SLICE         = ITEMS.register("fd_apple_pie_slice",         () -> new Item(new Item.Properties()));
        public static final RegistryObject<Item> FD_CHOCOLATE_PIE_SLICE     = ITEMS.register("fd_chocolate_pie_slice",     () -> new Item(new Item.Properties()));
        public static final RegistryObject<Item> FD_DUMPLINGS               = ITEMS.register("fd_dumplings",               () -> new Item(new Item.Properties()));
        public static final RegistryObject<Item> FD_KELP_ROLL               = ITEMS.register("fd_kelp_roll",               () -> new Item(new Item.Properties()));
        public static final RegistryObject<Item> FD_COD_ROLL                = ITEMS.register("fd_cod_roll",                () -> new Item(new Item.Properties()));
        public static final RegistryObject<Item> FD_FRIED_EGG               = ITEMS.register("fd_fried_egg",               () -> new Item(new Item.Properties()));
        public static final RegistryObject<Item> FD_FRUIT_SALAD             = ITEMS.register("fd_fruit_salad",             () -> new Item(new Item.Properties()));
        public static final RegistryObject<Item> FD_MELON_JUICE             = ITEMS.register("fd_melon_juice",             () -> new Item(new Item.Properties()));
        public static final RegistryObject<Item> FD_APPLE_CIDER             = ITEMS.register("fd_apple_cider",             () -> new Item(new Item.Properties()));
        public static final RegistryObject<Item> FD_HOT_COCOA               = ITEMS.register("fd_hot_cocoa",               () -> new Item(new Item.Properties()));
        public static final RegistryObject<Item> FD_MILK_BOTTLE             = ITEMS.register("fd_milk_bottle",             () -> new Item(new Item.Properties()));
        public static final RegistryObject<Item> FD_BONE_BROTH              = ITEMS.register("fd_bone_broth",              () -> new Item(new Item.Properties()));
        public static final RegistryObject<Item> FD_MELON_POPSICLE          = ITEMS.register("fd_melon_popsicle",          () -> new Item(new Item.Properties()));

        // --- Raw material items (Bahan Baku) for food crafting ---
        public static final RegistryObject<Item> FD_WHEAT_DOUGH             = ITEMS.register("fd_wheat_dough",             () -> new Item(new Item.Properties()));
        public static final RegistryObject<Item> FD_MINCED_BEEF             = ITEMS.register("fd_minced_beef",             () -> new Item(new Item.Properties()));
        public static final RegistryObject<Item> FD_CHICKEN_CUTS            = ITEMS.register("fd_chicken_cuts",            () -> new Item(new Item.Properties()));
        public static final RegistryObject<Item> FD_BACON_RAW               = ITEMS.register("fd_bacon_raw",               () -> new Item(new Item.Properties()));
        public static final RegistryObject<Item> FD_MUTTON_CHOPS            = ITEMS.register("fd_mutton_chops",            () -> new Item(new Item.Properties()));
        public static final RegistryObject<Item> FD_COD_SLICE               = ITEMS.register("fd_cod_slice",               () -> new Item(new Item.Properties()));
        public static final RegistryObject<Item> FD_PIE_CRUST               = ITEMS.register("fd_pie_crust",               () -> new Item(new Item.Properties()));
        public static final RegistryObject<Item> FD_RICE                    = ITEMS.register("fd_rice",                    () -> new Item(new Item.Properties()));

        public static final RegistryObject<Block> ZOO_WALL_BANNER_BLOCK = BLOCKS.register("zoo_wall_banner",
                        ZooWallBannerBlock::new);
        public static final RegistryObject<Block> ZOO_BANNER_BLOCK = BLOCKS.register("zoo_banner",
                        ZooBannerBlock::new);
        public static final RegistryObject<Item> ZOO_BANNER_ITEM = ITEMS.register("zoo_banner",
                        () -> new net.minecraft.world.item.BannerItem(ZOO_BANNER_BLOCK.get(), ZOO_WALL_BANNER_BLOCK.get(), new Item.Properties().stacksTo(16)));

        public static final RegistryObject<Item> ANIMAL_TAG_ITEM = ITEMS.register("animal_tag",
                        AnimalTagItem::new);

        // Entities
        public static final RegistryObject<EntityType<StaffEntity>> STAFF_ENTITY = ENTITY_TYPES.register("staff",
                        () -> EntityType.Builder.of(StaffEntity::new, MobCategory.CREATURE).sized(0.6F, 1.95F)
                                        .build("staff"));
        public static final RegistryObject<EntityType<VisitorEntity>> VISITOR_ENTITY = ENTITY_TYPES.register("visitor",
                        () -> EntityType.Builder.of(VisitorEntity::new, MobCategory.CREATURE).sized(0.6F, 1.95F)
                                        .build("visitor"));
        public static final RegistryObject<EntityType<ToiletSeatEntity>> TOILET_SEAT_ENTITY = ENTITY_TYPES.register("toilet_seat",
                        () -> EntityType.Builder.<ToiletSeatEntity>of(ToiletSeatEntity::new, MobCategory.MISC).sized(0.0F, 0.0F)
                                        .clientTrackingRange(1)
                                        .updateInterval(1)
                                        .build("toilet_seat"));

        public static final RegistryObject<EntityType<CashierEntity>> CASHIER_ENTITY = ENTITY_TYPES.register("cashier",
                        () -> EntityType.Builder.of(CashierEntity::new, MobCategory.CREATURE).sized(0.6F, 1.95F)
                                        .build("cashier"));

        public static final RegistryObject<EntityType<ZookeeperEntity>> ZOOKEEPER_ENTITY = ENTITY_TYPES.register("zookeeper",
                        () -> EntityType.Builder.of(ZookeeperEntity::new, MobCategory.CREATURE).sized(0.6F, 1.95F)
                                        .build("zookeeper"));

        public static final RegistryObject<EntityType<SecurityEntity>> SECURITY_ENTITY = ENTITY_TYPES.register("security",
                        () -> EntityType.Builder.of(SecurityEntity::new, MobCategory.CREATURE).sized(0.6F, 1.95F)
                                        .build("security"));

        // Spawn Eggs
        public static final RegistryObject<Item> STAFF_SPAWN_EGG = ITEMS.register("staff_spawn_egg",
                        () -> new ConfiguredSpawnEggItem(STAFF_ENTITY::get, 0x3498DB, 0x2C3E50, new Item.Properties(), staffRoleTag(0)));
        public static final RegistryObject<Item> ZOOKEEPER_SPAWN_EGG = ITEMS.register("zookeeper_spawn_egg",
                        () -> new ForgeSpawnEggItem(ZOOKEEPER_ENTITY::get, 0x2E8B57, 0x1B4F72, new Item.Properties()));
        public static final RegistryObject<Item> SECURITY_SPAWN_EGG = ITEMS.register("security_spawn_egg",
                        () -> new ForgeSpawnEggItem(SECURITY_ENTITY::get, 0xB03A2E, 0x1C2833, new Item.Properties()));
        public static final RegistryObject<Item> VISITOR_SPAWN_EGG = ITEMS.register("visitor_spawn_egg",
                        () -> new ConfiguredSpawnEggItem(VISITOR_ENTITY::get, 0xF1C40F, 0xE67E22, new Item.Properties(), hunterTag(false)));
        public static final RegistryObject<Item> HUNTER_SPAWN_EGG = ITEMS.register("hunter_spawn_egg",
                        () -> new ConfiguredSpawnEggItem(VISITOR_ENTITY::get, 0x1C1C1C, 0xFF0000, new Item.Properties(), hunterTag(true)));
        public static final RegistryObject<Item> CASHIER_SPAWN_EGG = ITEMS.register("cashier_spawn_egg",
                        () -> new ForgeSpawnEggItem(CASHIER_ENTITY::get, 0x8B4513, 0xD4AF37, new Item.Properties()));

        // Block Entity
        public static final RegistryObject<BlockEntityType<AnimalFeederBlockEntity>> ANIMAL_FEEDER_BE = BLOCK_ENTITIES
                        .register("animal_feeder",
                                        () -> BlockEntityType.Builder
                                                        .of(AnimalFeederBlockEntity::new, ANIMAL_FEEDER_BLOCK.get())
                                                        .build(null));

        public static final RegistryObject<BlockEntityType<net.minecraft.world.level.block.entity.BannerBlockEntity>> ZOO_BANNER_BE = BLOCK_ENTITIES
                        .register("zoo_banner",
                                        () -> BlockEntityType.Builder
                                                        .of(net.minecraft.world.level.block.entity.BannerBlockEntity::new, ZOO_BANNER_BLOCK.get(), ZOO_WALL_BANNER_BLOCK.get())
                                                        .build(null));

        public static final RegistryObject<BlockEntityType<ZooComputerBlockEntity>> ZOO_COMPUTER_BE = BLOCK_ENTITIES
                        .register("zoo_computer",
                                        () -> BlockEntityType.Builder
                                                        .of(ZooComputerBlockEntity::new, ZOO_COMPUTER_BLOCK.get())
                                                        .build(null));

        public static final RegistryObject<BlockEntityType<ToiletBlockEntity>> TOILET_BE = BLOCK_ENTITIES
                        .register("toilet",
                                        () -> BlockEntityType.Builder
                                                        .of(ToiletBlockEntity::new, RESTROOM_BLOCK.get())
                                                        .build(null));

        public static final RegistryObject<BlockEntityType<ShelfBlockEntity>> SHELF_BE = BLOCK_ENTITIES
                        .register("shelf",
                                        () -> BlockEntityType.Builder
                                                        .of(ShelfBlockEntity::new)
                                                        .build(null));

        public static final RegistryObject<BlockEntityType<FoodStallBlockEntity>> FOOD_STALL_BE = BLOCK_ENTITIES
                        .register("food_stall",
                                        () -> BlockEntityType.Builder
                                                        .of(FoodStallBlockEntity::new, FOOD_STALL_BLOCK.get())
                                                        .build(null));

        // Menu
        public static final RegistryObject<MenuType<ZooComputerMenu>> ZOO_COMPUTER_MENU = MENUS.register(
                        "zoo_computer_menu",
                        () -> IForgeMenuType.create(ZooComputerMenu::new));
        public static final RegistryObject<MenuType<AnimalFeederMenu>> ANIMAL_FEEDER_MENU = MENUS.register(
                        "animal_feeder_menu",
                        () -> IForgeMenuType.create(AnimalFeederMenu::new));
        public static final RegistryObject<MenuType<ShelfMenu>> SHELF_MENU = MENUS.register(
                        "shelf_menu",
                        () -> IForgeMenuType.create(ShelfMenu::new));
        public static final RegistryObject<MenuType<FoodStallMenu>> FOOD_STALL_MENU = MENUS.register(
                        "food_stall_menu",
                        () -> IForgeMenuType.create(FoodStallMenu::new));

        // Tab
        public static final RegistryObject<CreativeModeTab> ZOO_TAB = CREATIVE_TABS.register("zoo_tab",
                        () -> CreativeModeTab.builder()
                                        .title(Component.literal("IndoZoo Tycoon"))
                                        .displayItems((params, output) -> {
                                                output.accept(FOOD_STALL_ITEM.get());
                                                output.accept(TRASH_ITEM.get());
                                                output.accept(TRASH_CAN_ITEM.get());
                                                output.accept(ANIMAL_FEEDER_ITEM.get());
                                                output.accept(CAPTURE_CAGE_ITEM.get());
                                                output.accept(BIOME_CHANGER_ITEM.get());
                                                output.accept(RESTROOM_ITEM.get());
                                                output.accept(ZOO_BANNER_ITEM.get());
                                                output.accept(ANIMAL_TAG_ITEM.get());
                                                output.accept(STAFF_SPAWN_EGG.get());
                                                output.accept(ZOOKEEPER_SPAWN_EGG.get());
                                                output.accept(SECURITY_SPAWN_EGG.get());
                                                output.accept(VISITOR_SPAWN_EGG.get().getDefaultInstance());
                                                output.accept(HUNTER_SPAWN_EGG.get().getDefaultInstance());
                                                output.accept(CASHIER_SPAWN_EGG.get());
                                        })
                                        .build());

        private static CompoundTag staffRoleTag(int role) {
                CompoundTag tag = new CompoundTag();
                tag.putInt("Role", role);
                return tag;
        }

        private static CompoundTag hunterTag(boolean hunter) {
                CompoundTag tag = new CompoundTag();
                tag.putBoolean("IsHunter", hunter);
                return tag;
        }

        private static class ConfiguredSpawnEggItem extends ForgeSpawnEggItem {
                private final CompoundTag entityTag;

                private ConfiguredSpawnEggItem(Supplier<? extends EntityType<? extends Mob>> type, int primaryColor, int secondaryColor, Item.Properties properties, CompoundTag entityTag) {
                        super(type, primaryColor, secondaryColor, properties);
                        this.entityTag = entityTag;
                }

                @Override
                public ItemStack getDefaultInstance() {
                        ItemStack stack = super.getDefaultInstance();
                        if (entityTag != null && !entityTag.isEmpty()) {
                                stack.getOrCreateTag().put("EntityTag", entityTag.copy());
                        }
                        return stack;
                }
        }

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
                        FoodAnimalRegistry.initialize();
                        ZooItemRegistry.initialize();
                });
        }

        private void onRegisterCommands(RegisterCommandsEvent event) {
                ZooCommand.register(event.getDispatcher());
        }
}

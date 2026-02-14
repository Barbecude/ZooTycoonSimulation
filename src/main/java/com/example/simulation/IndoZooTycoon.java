package com.example.simulation;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.MinecraftForge;
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

    // ========== Deferred Registers ==========
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister
            .create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister
            .create(ForgeRegistries.ENTITY_TYPES, MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister
            .create(Registries.CREATIVE_MODE_TAB, MODID);

    // ========== Block: Zoo Computer ==========
    public static final RegistryObject<Block> ZOO_COMPUTER_BLOCK = BLOCKS.register("zoo_computer",
            ZooComputerBlock::new);

    public static final RegistryObject<Item> ZOO_COMPUTER_ITEM = ITEMS.register("zoo_computer",
            () -> new BlockItem(ZOO_COMPUTER_BLOCK.get(), new Item.Properties()));

    // ========== Block Entity ==========
    public static final RegistryObject<BlockEntityType<ZooComputerBlockEntity>> ZOO_COMPUTER_BE = BLOCK_ENTITIES
            .register("zoo_computer",
                    () -> BlockEntityType.Builder.of(ZooComputerBlockEntity::new,
                            ZOO_COMPUTER_BLOCK.get()).build(null));

    // ========== Entity: Staff (Zookeeper) ==========
    public static final RegistryObject<EntityType<StaffEntity>> STAFF_ENTITY = ENTITY_TYPES.register("staff",
            () -> EntityType.Builder.of(StaffEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.95F)
                    .build("staff"));

    // ========== Entity: Visitor ==========
    public static final RegistryObject<EntityType<VisitorEntity>> VISITOR_ENTITY = ENTITY_TYPES.register("visitor",
            () -> EntityType.Builder.of(VisitorEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.95F)
                    .build("visitor"));

    // ========== Creative Tab ==========
    public static final RegistryObject<CreativeModeTab> ZOO_TAB = CREATIVE_TABS.register("zoo_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.literal("IndoZoo Tycoon"))
                    .icon(() -> ZOO_COMPUTER_ITEM.get().getDefaultInstance())
                    .displayItems((params, output) -> {
                        output.accept(ZOO_COMPUTER_ITEM.get());
                    })
                    .build());

    // ========== Constructor ==========
    public IndoZooTycoon() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        ENTITY_TYPES.register(modBus);
        CREATIVE_TABS.register(modBus);

        // Register server-side events (commands, etc.)
        MinecraftForge.EVENT_BUS.addListener(this::onRegisterCommands);
    }

    // ========== Command Registration ==========
    private void onRegisterCommands(RegisterCommandsEvent event) {
        ZooCommand.register(event.getDispatcher());
    }
}

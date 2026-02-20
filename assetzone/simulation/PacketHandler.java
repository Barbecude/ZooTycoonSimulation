package com.example.simulation;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {
        private static final String PROTOCOL_VERSION = "1";
        public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
                        new ResourceLocation(IndoZooTycoon.MODID, "main"),
                        () -> PROTOCOL_VERSION,
                        PROTOCOL_VERSION::equals,
                        PROTOCOL_VERSION::equals);

        public static void register() {
                int id = 0;
                INSTANCE.registerMessage(id++, SyncBalancePacket.class,
                                SyncBalancePacket::toBytes,
                                SyncBalancePacket::new,
                                SyncBalancePacket::handle);
                INSTANCE.registerMessage(id++, OpenGuiPacket.class,
                                OpenGuiPacket::toBytes,
                                OpenGuiPacket::new,
                                OpenGuiPacket::handle);
                INSTANCE.registerMessage(id++, OpenNamingGuiPacket.class,
                                OpenNamingGuiPacket::toBytes,
                                OpenNamingGuiPacket::new,
                                OpenNamingGuiPacket::handle);
                INSTANCE.registerMessage(id++, TagAnimalPacket.class,
                                TagAnimalPacket::toBytes,
                                TagAnimalPacket::new,
                                TagAnimalPacket::handle);
        }
}

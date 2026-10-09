package net.zootycoon.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.zootycoon.ZooTycoonMod;
import net.zootycoon.claim.ClaimTier;
import net.zootycoon.claim.ZooClaimManager;
import net.zootycoon.construction.ConstructionManager;
import net.zootycoon.schematic.ZooSchematic;
import net.zootycoon.schematic.ZooSchematicRegistry;
import net.zootycoon.studio.SchematicCaptureService;
import net.zootycoon.studio.StudioDimensionManager;

import java.util.UUID;

public class ZooNetworkPayloads {

    // 1. Claim Land Payload
    public record ClaimLandPayload(BlockPos center, String tierName) implements CustomPayload {
        public static final Type<ClaimLandPayload> TYPE = new Type<>(ZooTycoonMod.id("claim_land"));
        public static final StreamCodec<FriendlyByteBuf, ClaimLandPayload> CODEC = CustomPayload.codec(
                (payload, buf) -> {
                    buf.writeBlockPos(payload.center);
                    buf.writeUtf(payload.tierName);
                },
                buf -> new ClaimLandPayload(buf.readBlockPos(), buf.readUtf())
        );
        @Override public Type<? extends CustomPayload> type() { return TYPE; }
    }

    // 2. Request Build Payload
    public record RequestBuildPayload(BlockPos origin, String schematicId) implements CustomPayload {
        public static final Type<RequestBuildPayload> TYPE = new Type<>(ZooTycoonMod.id("request_build"));
        public static final StreamCodec<FriendlyByteBuf, RequestBuildPayload> CODEC = CustomPayload.codec(
                (payload, buf) -> {
                    buf.writeBlockPos(payload.origin);
                    buf.writeUtf(payload.schematicId);
                },
                buf -> new RequestBuildPayload(buf.readBlockPos(), buf.readUtf())
        );
        @Override public Type<? extends CustomPayload> type() { return TYPE; }
    }

    // 3. Auto-Buy Missing Materials Payload
    public record AutoBuyMissingPayload(UUID siteId) implements CustomPayload {
        public static final Type<AutoBuyMissingPayload> TYPE = new Type<>(ZooTycoonMod.id("auto_buy_missing"));
        public static final StreamCodec<FriendlyByteBuf, AutoBuyMissingPayload> CODEC = CustomPayload.codec(
                (payload, buf) -> buf.writeUUID(payload.siteId),
                buf -> new AutoBuyMissingPayload(buf.readUUID())
        );
        @Override public Type<? extends CustomPayload> type() { return TYPE; }
    }

    // 4. Enter Studio Payload
    public record EnterStudioPayload(String category) implements CustomPayload {
        public static final Type<EnterStudioPayload> TYPE = new Type<>(ZooTycoonMod.id("enter_studio"));
        public static final StreamCodec<FriendlyByteBuf, EnterStudioPayload> CODEC = CustomPayload.codec(
                (payload, buf) -> buf.writeUtf(payload.category),
                buf -> new EnterStudioPayload(buf.readUtf())
        );
        @Override public Type<? extends CustomPayload> type() { return TYPE; }
    }

    // 5. Save Custom Schematic Payload
    public record SaveCustomSchematicPayload(BlockPos pos1, BlockPos pos2, String name, String category, String desc) implements CustomPayload {
        public static final Type<SaveCustomSchematicPayload> TYPE = new Type<>(ZooTycoonMod.id("save_schematic"));
        public static final StreamCodec<FriendlyByteBuf, SaveCustomSchematicPayload> CODEC = CustomPayload.codec(
                (payload, buf) -> {
                    buf.writeBlockPos(payload.pos1);
                    buf.writeBlockPos(payload.pos2);
                    buf.writeUtf(payload.name);
                    buf.writeUtf(payload.category);
                    buf.writeUtf(payload.desc);
                },
                buf -> new SaveCustomSchematicPayload(buf.readBlockPos(), buf.readBlockPos(), buf.readUtf(), buf.readUtf(), buf.readUtf())
        );
        @Override public Type<? extends CustomPayload> type() { return TYPE; }
    }

    public static void registerServerReceivers() {
        PayloadTypeRegistry.playC2S().register(ClaimLandPayload.TYPE, ClaimLandPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(RequestBuildPayload.TYPE, RequestBuildPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(AutoBuyMissingPayload.TYPE, AutoBuyMissingPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(EnterStudioPayload.TYPE, EnterStudioPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(SaveCustomSchematicPayload.TYPE, SaveCustomSchematicPayload.CODEC);

        // Handler: Claim Land
        ServerPlayNetworking.registerGlobalReceiver(ClaimLandPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> {
                ClaimTier tier = ClaimTier.valueOf(payload.tierName);
                ZooClaimManager.get(player.serverLevel()).tryClaim(player, payload.center, tier);
            });
        });

        // Handler: Request Build
        ServerPlayNetworking.registerGlobalReceiver(RequestBuildPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> {
                var schemOpt = ZooSchematicRegistry.get(payload.schematicId);
                if (schemOpt.isPresent()) {
                    ZooSchematic schem = schemOpt.get();
                    StructureTemplate template = player.serverLevel().getStructureManager()
                            .getOrCreate(schem.getStructureLocation());
                    ConstructionManager.startConstruction(player, payload.origin, schem, template);
                }
            });
        });

        // Handler: Auto-Buy Missing Materials
        ServerPlayNetworking.registerGlobalReceiver(AutoBuyMissingPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> {
                ConstructionManager.getSite(payload.siteId).ifPresent(site -> {
                    if (site.getOwnerUuid().equals(player.getUUID())) {
                        site.autoBuyMissing(player);
                    }
                });
            });
        });

        // Handler: Enter Studio
        ServerPlayNetworking.registerGlobalReceiver(EnterStudioPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> StudioDimensionManager.enterStudio(player, payload.category));
        });

        // Handler: Save Custom Schematic
        ServerPlayNetworking.registerGlobalReceiver(SaveCustomSchematicPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> {
                boolean saved = SchematicCaptureService.captureAndSave(
                        player, payload.pos1, payload.pos2, payload.name, payload.category, payload.desc
                );
                if (saved) {
                    StudioDimensionManager.exitStudio(player);
                }
            });
        });
    }
}


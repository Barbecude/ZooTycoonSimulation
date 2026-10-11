package net.zootycoon.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.zootycoon.ZooTycoonMod;
import net.zootycoon.claim.ClaimTier;
import net.zootycoon.claim.ZooClaimManager;
import net.zootycoon.construction.ConstructionManager;
import net.zootycoon.schematic.ZooSchematic;
import net.zootycoon.schematic.ZooSchematicRegistry;
import net.zootycoon.studio.SchematicCaptureService;
import net.zootycoon.studio.StudioDimensionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ZooNetworkPayloads {

    // 1. Claim Land Payload
    public record ClaimLandPayload(BlockPos center, String tierName) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<ClaimLandPayload> TYPE = new CustomPacketPayload.Type<>(ZooTycoonMod.id("claim_land"));
        public static final StreamCodec<FriendlyByteBuf, ClaimLandPayload> CODEC = CustomPacketPayload.codec(
                (payload, buf) -> {
                    buf.writeBlockPos(payload.center);
                    buf.writeUtf(payload.tierName);
                },
                buf -> new ClaimLandPayload(buf.readBlockPos(), buf.readUtf())
        );
        @Override public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    // 1b. Claim Box Payload (2-Point selection)
    public record ClaimBoxPayload(BlockPos pos1, BlockPos pos2) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<ClaimBoxPayload> TYPE = new CustomPacketPayload.Type<>(ZooTycoonMod.id("claim_box"));
        public static final StreamCodec<FriendlyByteBuf, ClaimBoxPayload> CODEC = CustomPacketPayload.codec(
                (payload, buf) -> {
                    buf.writeBlockPos(payload.pos1);
                    buf.writeBlockPos(payload.pos2);
                },
                buf -> new ClaimBoxPayload(buf.readBlockPos(), buf.readBlockPos())
        );
        @Override public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    // 2. Request Build Payload
    public record RequestBuildPayload(BlockPos origin, String schematicId, String rotation) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<RequestBuildPayload> TYPE = new CustomPacketPayload.Type<>(ZooTycoonMod.id("request_build"));
        public static final StreamCodec<FriendlyByteBuf, RequestBuildPayload> CODEC = CustomPacketPayload.codec(
                (payload, buf) -> {
                    buf.writeBlockPos(payload.origin);
                    buf.writeUtf(payload.schematicId);
                    buf.writeUtf(payload.rotation != null ? payload.rotation : "NONE");
                },
                buf -> new RequestBuildPayload(buf.readBlockPos(), buf.readUtf(), buf.readUtf())
        );
        @Override public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }
    }



    // 4. Enter Studio Payload
    public record EnterStudioPayload(String category) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<EnterStudioPayload> TYPE = new CustomPacketPayload.Type<>(ZooTycoonMod.id("enter_studio"));
        public static final StreamCodec<FriendlyByteBuf, EnterStudioPayload> CODEC = CustomPacketPayload.codec(
                (payload, buf) -> buf.writeUtf(payload.category),
                buf -> new EnterStudioPayload(buf.readUtf())
        );
        @Override public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    // 4b. Edit Studio Payload
    public record EditStudioPayload(String schematicId, String category) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<EditStudioPayload> TYPE = new CustomPacketPayload.Type<>(ZooTycoonMod.id("edit_studio"));
        public static final StreamCodec<FriendlyByteBuf, EditStudioPayload> CODEC = CustomPacketPayload.codec(
                (payload, buf) -> {
                    buf.writeUtf(payload.schematicId);
                    buf.writeUtf(payload.category);
                },
                buf -> new EditStudioPayload(buf.readUtf(), buf.readUtf())
        );
        @Override public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    // 5. Save Custom Schematic Payload
    public record SaveCustomSchematicPayload(BlockPos pos1, BlockPos pos2, String name, String category, String desc) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<SaveCustomSchematicPayload> TYPE = new CustomPacketPayload.Type<>(ZooTycoonMod.id("save_schematic"));
        public static final StreamCodec<FriendlyByteBuf, SaveCustomSchematicPayload> CODEC = CustomPacketPayload.codec(
                (payload, buf) -> {
                    buf.writeBlockPos(payload.pos1);
                    buf.writeBlockPos(payload.pos2);
                    buf.writeUtf(payload.name);
                    buf.writeUtf(payload.category);
                    buf.writeUtf(payload.desc);
                },
                buf -> new SaveCustomSchematicPayload(buf.readBlockPos(), buf.readBlockPos(), buf.readUtf(), buf.readUtf(), buf.readUtf())
        );
        @Override public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    // 6. Exit Studio Payload
    public record ExitStudioPayload() implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<ExitStudioPayload> TYPE = new CustomPacketPayload.Type<>(ZooTycoonMod.id("exit_studio"));
        public static final StreamCodec<FriendlyByteBuf, ExitStudioPayload> CODEC = CustomPacketPayload.codec(
                (payload, buf) -> {},
                buf -> new ExitStudioPayload()
        );
        @Override public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    // 7. Claim Data for Client Sync
    public record ClaimData(
            UUID claimId, UUID ownerUuid, String ownerName,
            int minX, int minY, int minZ,
            int maxX, int maxY, int maxZ,
            String tierName
    ) {
        public static void write(FriendlyByteBuf buf, ClaimData d) {
            buf.writeUUID(d.claimId);
            buf.writeUUID(d.ownerUuid);
            buf.writeUtf(d.ownerName);
            buf.writeInt(d.minX);
            buf.writeInt(d.minY);
            buf.writeInt(d.minZ);
            buf.writeInt(d.maxX);
            buf.writeInt(d.maxY);
            buf.writeInt(d.maxZ);
            buf.writeUtf(d.tierName);
        }

        public static ClaimData read(FriendlyByteBuf buf) {
            return new ClaimData(
                    buf.readUUID(), buf.readUUID(), buf.readUtf(),
                    buf.readInt(), buf.readInt(), buf.readInt(),
                    buf.readInt(), buf.readInt(), buf.readInt(),
                    buf.readUtf()
            );
        }
    }

    // 8. Sync Claims Payload (S2C)
    public record SyncClaimsPayload(List<ClaimData> claims) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<SyncClaimsPayload> TYPE = new CustomPacketPayload.Type<>(ZooTycoonMod.id("sync_claims"));
        public static final StreamCodec<FriendlyByteBuf, SyncClaimsPayload> CODEC = CustomPacketPayload.codec(
                (payload, buf) -> {
                    buf.writeInt(payload.claims.size());
                    for (ClaimData d : payload.claims) {
                        ClaimData.write(buf, d);
                    }
                },
                buf -> {
                    int count = buf.readInt();
                    List<ClaimData> list = new ArrayList<>(count);
                    for (int i = 0; i < count; i++) {
                        list.add(ClaimData.read(buf));
                    }
                    return new SyncClaimsPayload(list);
                }
        );
        @Override public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    // 9. Sync Studio State Payload (S2C)
    public record SyncStudioStatePayload(boolean inStudio, String category) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<SyncStudioStatePayload> TYPE = new CustomPacketPayload.Type<>(ZooTycoonMod.id("sync_studio_state"));
        public static final StreamCodec<FriendlyByteBuf, SyncStudioStatePayload> CODEC = CustomPacketPayload.codec(
                (payload, buf) -> {
                    buf.writeBoolean(payload.inStudio);
                    buf.writeUtf(payload.category);
                },
                buf -> new SyncStudioStatePayload(buf.readBoolean(), buf.readUtf())
        );
        @Override public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record ItemEntry(String itemId, int count) {
        public static void write(FriendlyByteBuf buf, ItemEntry e) {
            buf.writeUtf(e.itemId);
            buf.writeInt(e.count);
        }
        public static ItemEntry read(FriendlyByteBuf buf) {
            return new ItemEntry(buf.readUtf(), buf.readInt());
        }
    }

    // 10. Deposit Materials Payload (C2S)
    public record DepositMaterialsPayload(UUID siteId) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<DepositMaterialsPayload> TYPE = new CustomPacketPayload.Type<>(ZooTycoonMod.id("deposit_materials"));
        public static final StreamCodec<FriendlyByteBuf, DepositMaterialsPayload> CODEC = CustomPacketPayload.codec(
                (payload, buf) -> buf.writeUUID(payload.siteId),
                buf -> new DepositMaterialsPayload(buf.readUUID())
        );
        @Override public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    // 10b. Deposit Tools Payload (C2S)
    public record DepositToolsPayload(UUID siteId) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<DepositToolsPayload> TYPE = new CustomPacketPayload.Type<>(ZooTycoonMod.id("deposit_tools"));
        public static final StreamCodec<FriendlyByteBuf, DepositToolsPayload> CODEC = CustomPacketPayload.codec(
                (payload, buf) -> buf.writeUUID(payload.siteId),
                buf -> new DepositToolsPayload(buf.readUUID())
        );
        @Override public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }
    }



    // 10d. Hire Staff Payload (C2S)
    public record HireStaffPayload(String staffType) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<HireStaffPayload> TYPE = new CustomPacketPayload.Type<>(ZooTycoonMod.id("hire_staff"));
        public static final StreamCodec<FriendlyByteBuf, HireStaffPayload> CODEC = CustomPacketPayload.codec(
                (payload, buf) -> buf.writeUtf(payload.staffType),
                buf -> new HireStaffPayload(buf.readUtf())
        );
        @Override public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    // 10e. Sync Staff Payload (S2C)
    public record SyncStaffPayload(int builders, int activeBuilders, int zookeepers, int security) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<SyncStaffPayload> TYPE = new CustomPacketPayload.Type<>(ZooTycoonMod.id("sync_staff"));
        public static final StreamCodec<FriendlyByteBuf, SyncStaffPayload> CODEC = CustomPacketPayload.codec(
                (payload, buf) -> {
                    buf.writeInt(payload.builders);
                    buf.writeInt(payload.activeBuilders);
                    buf.writeInt(payload.zookeepers);
                    buf.writeInt(payload.security);
                },
                buf -> new SyncStaffPayload(buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt())
        );
        @Override public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    // 10f. Request Staff Sync Payload (C2S)
    public record RequestStaffSyncPayload() implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<RequestStaffSyncPayload> TYPE = new CustomPacketPayload.Type<>(ZooTycoonMod.id("request_staff_sync"));
        public static final StreamCodec<FriendlyByteBuf, RequestStaffSyncPayload> CODEC = CustomPacketPayload.codec(
                (payload, buf) -> {},
                buf -> new RequestStaffSyncPayload()
        );
        @Override public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }
    }


    // 11. Open Builder Screen Payload (S2C)
    public record OpenBuilderScreenPayload(
            UUID siteId, String schematicName, float progress,
            int placed, int total, List<ItemEntry> missing,
            String builderName,
            int phase, // 0 = clearing land, 1 = placing blocks, 2 = gajian / wage
            int obstructingCount,
            boolean needsShovel, boolean hasShovel,
            boolean needsPickaxe, boolean hasPickaxe,
            boolean needsAxe, boolean hasAxe,
            int wageDue,
            UUID builderUuid
    ) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<OpenBuilderScreenPayload> TYPE = new CustomPacketPayload.Type<>(ZooTycoonMod.id("open_builder_screen"));
        public static final StreamCodec<FriendlyByteBuf, OpenBuilderScreenPayload> CODEC = CustomPacketPayload.codec(
                (payload, buf) -> {
                    buf.writeUUID(payload.siteId);
                    buf.writeUtf(payload.schematicName);
                    buf.writeFloat(payload.progress);
                    buf.writeInt(payload.placed);
                    buf.writeInt(payload.total);
                    buf.writeInt(payload.missing.size());
                    for (ItemEntry e : payload.missing) {
                        ItemEntry.write(buf, e);
                    }
                    buf.writeUtf(payload.builderName != null ? payload.builderName : "Builder");
                    buf.writeInt(payload.phase);
                    buf.writeInt(payload.obstructingCount);
                    buf.writeBoolean(payload.needsShovel);
                    buf.writeBoolean(payload.hasShovel);
                    buf.writeBoolean(payload.needsPickaxe);
                    buf.writeBoolean(payload.hasPickaxe);
                    buf.writeBoolean(payload.needsAxe);
                    buf.writeBoolean(payload.hasAxe);
                    buf.writeInt(payload.wageDue);
                    buf.writeUUID(payload.builderUuid != null ? payload.builderUuid : UUID.randomUUID());
                },
                buf -> {
                    UUID siteId = buf.readUUID();
                    String schemName = buf.readUtf();
                    float prog = buf.readFloat();
                    int placed = buf.readInt();
                    int total = buf.readInt();
                    int count = buf.readInt();
                    List<ItemEntry> missing = new ArrayList<>(count);
                    for (int i = 0; i < count; i++) {
                        missing.add(ItemEntry.read(buf));
                    }
                    String builderName = buf.readUtf();
                    int phase = buf.readInt();
                    int obstructingCount = buf.readInt();
                    boolean needsShovel = buf.readBoolean();
                    boolean hasShovel = buf.readBoolean();
                    boolean needsPickaxe = buf.readBoolean();
                    boolean hasPickaxe = buf.readBoolean();
                    boolean needsAxe = buf.readBoolean();
                    boolean hasAxe = buf.readBoolean();
                    int wageDue = buf.readInt();
                    UUID builderUuid = buf.readUUID();
                    return new OpenBuilderScreenPayload(
                            siteId, schemName, prog, placed, total, missing, builderName,
                            phase, obstructingCount, needsShovel, hasShovel, needsPickaxe, hasPickaxe, needsAxe, hasAxe,
                            wageDue, builderUuid
                    );
                }
        );
        @Override public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    // 12. Pay Builder Wage Payload (C2S)
    public record PayBuilderWagePayload(UUID builderUuid) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<PayBuilderWagePayload> TYPE = new CustomPacketPayload.Type<>(ZooTycoonMod.id("pay_builder_wage"));
        public static final StreamCodec<FriendlyByteBuf, PayBuilderWagePayload> CODEC = CustomPacketPayload.codec(
                (payload, buf) -> buf.writeUUID(payload.builderUuid),
                buf -> new PayBuilderWagePayload(buf.readUUID())
        );
        @Override public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    // 14. Rename Animal Payload (C2S)
    public record RenameAnimalPayload(int entityId, String newName) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<RenameAnimalPayload> TYPE = new CustomPacketPayload.Type<>(ZooTycoonMod.id("rename_animal"));
        public static final StreamCodec<FriendlyByteBuf, RenameAnimalPayload> CODEC = CustomPacketPayload.codec(
                (payload, buf) -> {
                    buf.writeInt(payload.entityId);
                    buf.writeUtf(payload.newName);
                },
                buf -> new RenameAnimalPayload(buf.readInt(), buf.readUtf())
        );
        @Override public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    // 15. Feed Animal From GUI Payload (C2S)
    public record FeedAnimalFromGuiPayload(int entityId) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<FeedAnimalFromGuiPayload> TYPE = new CustomPacketPayload.Type<>(ZooTycoonMod.id("feed_animal_from_gui"));
        public static final StreamCodec<FriendlyByteBuf, FeedAnimalFromGuiPayload> CODEC = CustomPacketPayload.codec(
                (payload, buf) -> buf.writeInt(payload.entityId),
                buf -> new FeedAnimalFromGuiPayload(buf.readInt())
        );
        @Override public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    // 16. Request Animal Sync Payload (C2S)
    public record RequestAnimalSyncPayload() implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<RequestAnimalSyncPayload> TYPE = new CustomPacketPayload.Type<>(ZooTycoonMod.id("request_animal_sync"));
        public static final StreamCodec<FriendlyByteBuf, RequestAnimalSyncPayload> CODEC = CustomPacketPayload.codec(
                (payload, buf) -> {},
                buf -> new RequestAnimalSyncPayload()
        );
        @Override public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    // 16b. Unregister Animal Payload (C2S)
    public record UnregisterAnimalPayload(int entityId) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<UnregisterAnimalPayload> TYPE = new CustomPacketPayload.Type<>(ZooTycoonMod.id("unregister_animal"));
        public static final StreamCodec<FriendlyByteBuf, UnregisterAnimalPayload> CODEC = CustomPacketPayload.codec(
                (payload, buf) -> buf.writeInt(payload.entityId),
                buf -> new UnregisterAnimalPayload(buf.readInt())
        );
        @Override public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    // 16c. Scan Animal with Spyglass Payload (C2S)
    public record ScanAnimalPayload(int entityId) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<ScanAnimalPayload> TYPE = new CustomPacketPayload.Type<>(ZooTycoonMod.id("scan_animal"));
        public static final StreamCodec<FriendlyByteBuf, ScanAnimalPayload> CODEC = CustomPacketPayload.codec(
                (payload, buf) -> buf.writeInt(payload.entityId),
                buf -> new ScanAnimalPayload(buf.readInt())
        );
        @Override public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    // 17. Sync Animals Payload (S2C)
    public record AnimalSyncData(
            int entityId,
            String entityTypeId,
            String customName,
            int hunger,
            float health,
            float maxHealth,
            boolean hasZookeeper,
            String zookeeperName
    ) {}

    public record SyncAnimalsPayload(List<AnimalSyncData> animals) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<SyncAnimalsPayload> TYPE = new CustomPacketPayload.Type<>(ZooTycoonMod.id("sync_animals"));
        public static final StreamCodec<FriendlyByteBuf, SyncAnimalsPayload> CODEC = CustomPacketPayload.codec(
                (payload, buf) -> {
                    buf.writeInt(payload.animals.size());
                    for (var d : payload.animals) {
                        buf.writeInt(d.entityId());
                        buf.writeUtf(d.entityTypeId());
                        buf.writeUtf(d.customName());
                        buf.writeInt(d.hunger());
                        buf.writeFloat(d.health());
                        buf.writeFloat(d.maxHealth());
                        buf.writeBoolean(d.hasZookeeper());
                        buf.writeUtf(d.zookeeperName());
                    }
                },
                buf -> {
                    int size = buf.readInt();
                    List<AnimalSyncData> list = new ArrayList<>(size);
                    for (int i = 0; i < size; i++) {
                        list.add(new AnimalSyncData(
                                buf.readInt(), buf.readUtf(), buf.readUtf(), buf.readInt(),
                                buf.readFloat(), buf.readFloat(),
                                buf.readBoolean(), buf.readUtf()
                        ));
                    }
                    return new SyncAnimalsPayload(list);
                }
        );
        @Override public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    // 14. Open Zookeeper Screen Payload (S2C)
    public record OpenZookeeperScreenPayload(
            UUID keeperUuid,
            String keeperName,
            boolean hasAnimal,
            String animalName,
            int hunger,
            int foodStock,
            String dialogText,
            List<String> acceptedFoods
    ) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<OpenZookeeperScreenPayload> TYPE = new CustomPacketPayload.Type<>(ZooTycoonMod.id("open_zookeeper_screen"));
        public static final StreamCodec<FriendlyByteBuf, OpenZookeeperScreenPayload> CODEC = CustomPacketPayload.codec(
                (payload, buf) -> {
                    buf.writeUUID(payload.keeperUuid);
                    buf.writeUtf(payload.keeperName);
                    buf.writeBoolean(payload.hasAnimal);
                    buf.writeUtf(payload.animalName);
                    buf.writeInt(payload.hunger);
                    buf.writeInt(payload.foodStock);
                    buf.writeUtf(payload.dialogText);
                    buf.writeInt(payload.acceptedFoods.size());
                    for (String s : payload.acceptedFoods) {
                        buf.writeUtf(s);
                    }
                },
                buf -> {
                    UUID keeperUuid = buf.readUUID();
                    String keeperName = buf.readUtf();
                    boolean hasAnimal = buf.readBoolean();
                    String animalName = buf.readUtf();
                    int hunger = buf.readInt();
                    int foodStock = buf.readInt();
                    String dialogText = buf.readUtf();
                    int size = buf.readInt();
                    List<String> foods = new ArrayList<>(size);
                    for (int i = 0; i < size; i++) {
                        foods.add(buf.readUtf());
                    }
                    return new OpenZookeeperScreenPayload(keeperUuid, keeperName, hasAnimal, animalName, hunger, foodStock, dialogText, foods);
                }
        );
        @Override public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    // 15. Supply Zookeeper Food Payload (C2S)
    public record SupplyZookeeperFoodPayload(UUID keeperUuid) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<SupplyZookeeperFoodPayload> TYPE = new CustomPacketPayload.Type<>(ZooTycoonMod.id("supply_zookeeper_food"));
        public static final StreamCodec<FriendlyByteBuf, SupplyZookeeperFoodPayload> CODEC = CustomPacketPayload.codec(
                (payload, buf) -> buf.writeUUID(payload.keeperUuid),
                buf -> new SupplyZookeeperFoodPayload(buf.readUUID())
        );
        @Override public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public static void registerServerReceivers() {
        PayloadTypeRegistry.playC2S().register(ClaimLandPayload.TYPE, ClaimLandPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ClaimBoxPayload.TYPE, ClaimBoxPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(RequestBuildPayload.TYPE, RequestBuildPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(DepositMaterialsPayload.TYPE, DepositMaterialsPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(DepositToolsPayload.TYPE, DepositToolsPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(HireStaffPayload.TYPE, HireStaffPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(RequestStaffSyncPayload.TYPE, RequestStaffSyncPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(PayBuilderWagePayload.TYPE, PayBuilderWagePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(EnterStudioPayload.TYPE, EnterStudioPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(EditStudioPayload.TYPE, EditStudioPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(SaveCustomSchematicPayload.TYPE, SaveCustomSchematicPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ExitStudioPayload.TYPE, ExitStudioPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(RenameAnimalPayload.TYPE, RenameAnimalPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(UnregisterAnimalPayload.TYPE, UnregisterAnimalPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ScanAnimalPayload.TYPE, ScanAnimalPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(FeedAnimalFromGuiPayload.TYPE, FeedAnimalFromGuiPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(RequestAnimalSyncPayload.TYPE, RequestAnimalSyncPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(SupplyZookeeperFoodPayload.TYPE, SupplyZookeeperFoodPayload.CODEC);

        PayloadTypeRegistry.playS2C().register(SyncClaimsPayload.TYPE, SyncClaimsPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SyncStudioStatePayload.TYPE, SyncStudioStatePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SyncStaffPayload.TYPE, SyncStaffPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(OpenBuilderScreenPayload.TYPE, OpenBuilderScreenPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(OpenZookeeperScreenPayload.TYPE, OpenZookeeperScreenPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SyncAnimalsPayload.TYPE, SyncAnimalsPayload.CODEC);

        // Handler: Claim Land (Old / Direct)
        ServerPlayNetworking.registerGlobalReceiver(ClaimLandPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> {
                ClaimTier tier = ClaimTier.valueOf(payload.tierName);
                ZooClaimManager.get((ServerLevel) player.level()).tryClaim(player, payload.center, tier);
            });
        });

        // Handler: Claim Box (2-Point Stick selection)
        ServerPlayNetworking.registerGlobalReceiver(ClaimBoxPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> {
                ZooClaimManager.get((ServerLevel) player.level()).tryClaimBox(player, payload.pos1, payload.pos2);
            });
        });

        // Handler: Request Build
        ServerPlayNetworking.registerGlobalReceiver(RequestBuildPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> {
                var schemOpt = ZooSchematicRegistry.get(payload.schematicId);
                if (schemOpt.isEmpty()) {
                    ZooSchematicRegistry.loadCustomSchematics();
                    schemOpt = ZooSchematicRegistry.get(payload.schematicId);
                }
                if (schemOpt.isPresent()) {
                    ZooSchematic schem = schemOpt.get();
                    StructureTemplate template = ZooSchematicRegistry.loadTemplate(context.server(), schem);
                    net.minecraft.world.level.block.Rotation rot;
                    try {
                        rot = net.minecraft.world.level.block.Rotation.valueOf(payload.rotation);
                    } catch (Exception e) {
                        rot = net.minecraft.world.level.block.Rotation.NONE;
                    }
                    ConstructionManager.startConstruction(player, payload.origin, schem, template, rot);
                } else {
                    player.displayClientMessage(
                            Component.literal("Skematik tidak ditemukan!").withStyle(ChatFormatting.RED),
                            true
                    );
                }
            });
        });

        // Handler: Deposit Materials
        ServerPlayNetworking.registerGlobalReceiver(DepositMaterialsPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> {
                ConstructionManager.getSite(payload.siteId).ifPresent(site -> {
                    int totalAccepted = 0;
                    for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                        ItemStack stack = player.getInventory().getItem(i);
                        if (!stack.isEmpty()) {
                            int accepted = site.deliverMaterial(stack);
                            totalAccepted += accepted;
                        }
                    }
                    if (totalAccepted > 0) {
                        player.level().playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.ITEM_PICKUP, net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 1.0f);
                        player.sendSystemMessage(Component.literal("Mantap Bos! " + totalAccepted + " bahan udah diserahin ke kuli.").withStyle(ChatFormatting.GREEN));
                    } else {
                        player.displayClientMessage(Component.literal("Gak ada bahan yang cocok di badan Bos buat proyek ini!").withStyle(ChatFormatting.YELLOW), true);
                    }
                });
            });
        });

        // Handler: Deposit Tools
        ServerPlayNetworking.registerGlobalReceiver(DepositToolsPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> {
                ConstructionManager.getSite(payload.siteId).ifPresent(site -> {
                    boolean success = site.depositTools(player);
                    if (success) {
                        player.level().playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.ITEM_PICKUP, net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 1.0f);
                        player.sendSystemMessage(Component.literal("Mantap Bos! Alat kerja udah diserahin ke kuli. Siap babat lahan!").withStyle(ChatFormatting.GREEN));
                        if (site.getBuilderUuid() != null) {
                            ServerLevel sLevel = (ServerLevel) player.level();
                            var ent = sLevel.getEntity(site.getBuilderUuid());
                            if (ent instanceof net.minecraft.world.entity.LivingEntity living) {
                                if (site.isClearingPhase()) {
                                    BlockPos obs = site.getNextObstructingBlock(sLevel);
                                    if (obs != null) {
                                        living.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, site.getToolStackFor(sLevel.getBlockState(obs)));
                                    } else if (!site.getEquippedShovel().isEmpty()) {
                                        living.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, site.getEquippedShovel());
                                    } else if (!site.getEquippedPickaxe().isEmpty()) {
                                        living.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, site.getEquippedPickaxe());
                                    } else if (!site.getEquippedAxe().isEmpty()) {
                                        living.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, site.getEquippedAxe());
                                    }
                                }
                            }
                        }
                    } else {
                        player.displayClientMessage(Component.literal("Bos gak lagi bawa alat kerja yang dibutuhin (Sekop/Beliung/Kapak)!").withStyle(ChatFormatting.YELLOW), true);
                    }
                });
            });
        });

        // Handler: Hire Staff
        ServerPlayNetworking.registerGlobalReceiver(HireStaffPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> {
                boolean success = net.zootycoon.economy.ZooStaffManager.get(context.server()).hireStaff(player, payload.staffType());
                if (success) {
                    player.level().playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.PLAYER_LEVELUP, net.minecraft.sounds.SoundSource.PLAYERS, 0.8f, 1.2f);
                    player.displayClientMessage(Component.literal("Berhasil merekrut pegawai baru (" + payload.staffType() + ")!").withStyle(ChatFormatting.GREEN), true);
                } else {
                    player.displayClientMessage(Component.literal("Emerald tidak cukup untuk merekrut pegawai ini!").withStyle(ChatFormatting.RED), true);
                }
            });
        });

        // Handler: Request Staff Sync
        ServerPlayNetworking.registerGlobalReceiver(RequestStaffSyncPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> {
                net.zootycoon.economy.ZooStaffManager.get(context.server()).syncToPlayer(player);
            });
        });

        // Handler: Pay Builder Wage
        ServerPlayNetworking.registerGlobalReceiver(PayBuilderWagePayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> {
                net.minecraft.world.entity.Entity entity = ((ServerLevel) player.level()).getEntity(payload.builderUuid());
                if (entity instanceof net.zootycoon.entity.ZooBuilderEntity builder && builder.isAwaitingPayment()) {
                    builder.tryPayWageFromPlayer(player);
                }
            });
        });


        // Handler: Enter Studio
        ServerPlayNetworking.registerGlobalReceiver(EnterStudioPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> StudioDimensionManager.enterStudio(player, payload.category));
        });

        // Handler: Edit Studio
        ServerPlayNetworking.registerGlobalReceiver(EditStudioPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> StudioDimensionManager.editInStudio(player, payload.schematicId, payload.category));
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

        // Handler: Exit Studio
        ServerPlayNetworking.registerGlobalReceiver(ExitStudioPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> StudioDimensionManager.exitStudio(player));
        });

        // Handler: Rename Animal
        ServerPlayNetworking.registerGlobalReceiver(RenameAnimalPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> {
                ServerLevel level = (ServerLevel) player.level();
                net.minecraft.world.entity.Entity entity = level.getEntity(payload.entityId);
                if (entity instanceof net.minecraft.world.entity.LivingEntity living) {
                    living.setCustomName(Component.literal(payload.newName).withStyle(ChatFormatting.GOLD));
                    living.setCustomNameVisible(true);
                    net.zootycoon.animal.ZooAnimalHungerManager.setTagged(living, true);

                    level.sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER,
                            living.getX(), living.getY() + living.getBbHeight() * 0.6, living.getZ(),
                            12, 0.35, 0.35, 0.35, 0.02);
                    level.sendParticles(net.minecraft.core.particles.ParticleTypes.ENCHANT,
                            living.getX(), living.getY() + living.getBbHeight() * 0.8, living.getZ(),
                            15, 0.4, 0.4, 0.4, 0.02);
                    level.playSound(null, living.blockPosition(), net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP, net.minecraft.sounds.SoundSource.PLAYERS, 0.8f, 1.1f);
                    player.sendSystemMessage(Component.literal("Berhasil memberi nama satwa: " + payload.newName).withStyle(ChatFormatting.GREEN));
                }
            });
        });

        // Handler: Unregister Animal
        ServerPlayNetworking.registerGlobalReceiver(UnregisterAnimalPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> {
                net.zootycoon.animal.ZooAnimalHungerManager.unregisterAnimal(player, payload.entityId);
            });
        });

        // Handler: Scan Animal with Spyglass
        ServerPlayNetworking.registerGlobalReceiver(ScanAnimalPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> {
                if (player == null || player.level() == null) return;
                net.minecraft.world.entity.Entity ent = player.level().getEntity(payload.entityId);
                if (ent instanceof net.minecraft.world.entity.LivingEntity living && living.isAlive()) {
                    net.zootycoon.animal.ZooAnimalHungerManager.registerAnimalFromSpyglass(player, living);
                }
            });
        });

        // Handler: Feed Animal from GUI
        ServerPlayNetworking.registerGlobalReceiver(FeedAnimalFromGuiPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> {
                ServerLevel level = (ServerLevel) player.level();
                net.minecraft.world.entity.Entity entity = level.getEntity(payload.entityId);
                if (entity instanceof net.minecraft.world.entity.LivingEntity living) {
                    ItemStack foundFood = ItemStack.EMPTY;
                    for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                        ItemStack is = player.getInventory().getItem(i);
                        if (!is.isEmpty() && net.zootycoon.animal.AnimalDietRegistry.isFoodValid(living, is)) {
                            foundFood = is;
                            break;
                        }
                    }

                    if (!foundFood.isEmpty()) {
                        boolean fed = net.zootycoon.animal.ZooAnimalHungerManager.feedAnimal(living, foundFood, player);
                        if (fed) {
                            player.sendSystemMessage(Component.literal("Berhasil menyuapi satwa! Rasa lapar berkurang.").withStyle(ChatFormatting.GREEN));
                        }
                    } else {
                        player.displayClientMessage(Component.literal("Bos tidak memiliki makanan yang cocok di tas!").withStyle(ChatFormatting.RED), true);
                    }
                }
            });
        });

        // Handler: Request Animal Sync
        ServerPlayNetworking.registerGlobalReceiver(RequestAnimalSyncPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> sendAnimalSync(player));
        });

        // Handler: Supply Food to Zookeeper from GUI
        ServerPlayNetworking.registerGlobalReceiver(SupplyZookeeperFoodPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            net.minecraft.server.level.ServerLevel level = (net.minecraft.server.level.ServerLevel) player.level();
            context.server().execute(() -> {
                net.minecraft.world.entity.Entity ent = level.getEntity(payload.keeperUuid());
                if (ent instanceof net.zootycoon.entity.ZooZookeeperEntity keeper) {
                    keeper.handleFoodTransfer(player);
                }
            });
        });
    }

    public static void sendAnimalSync(ServerPlayer player) {
        if (player == null || player.level().getServer() == null) return;
        List<AnimalSyncData> list = new ArrayList<>();

        for (ServerLevel sLevel : player.level().getServer().getAllLevels()) {
            for (net.minecraft.world.entity.Entity entity : sLevel.getAllEntities()) {
                if (entity instanceof net.minecraft.world.entity.LivingEntity animal
                        && net.zootycoon.animal.ZooAnimalHungerManager.isTagged(animal)
                        && net.zootycoon.animal.ZooAnimalHungerManager.isZooAnimalCandidate(animal)
                        && net.zootycoon.animal.ZooAnimalHungerManager.isOwnedBy(animal, player.getUUID())) {

                    String typeId = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(animal.getType()).toString();
                    String name = animal.hasCustomName() ? animal.getCustomName().getString() : "";
                    int hunger = net.zootycoon.animal.ZooAnimalHungerManager.getHunger(animal);
                    UUID zkUuid = net.zootycoon.animal.ZooAnimalHungerManager.getAssignedZookeeper(animal);

                    boolean hasZk = false;
                    String zkName = "";
                    if (zkUuid != null) {
                        net.minecraft.world.entity.Entity zkEnt = sLevel.getEntity(zkUuid);
                        if (zkEnt instanceof net.zootycoon.entity.ZooZookeeperEntity zke) {
                            hasZk = true;
                            zkName = zke.getName().getString();
                        }
                    }

                    float health = animal.getHealth();
                    float maxHealth = animal.getMaxHealth();
                    list.add(new AnimalSyncData(animal.getId(), typeId, name, hunger, health, maxHealth, hasZk, zkName));
                }
            }
        }

        ServerPlayNetworking.send(player, new SyncAnimalsPayload(list));
    }
}


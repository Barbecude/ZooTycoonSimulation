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
    public record SyncStaffPayload(int builders, int activeBuilders, int zookeepers, int security, int cashiers) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<SyncStaffPayload> TYPE = new CustomPacketPayload.Type<>(ZooTycoonMod.id("sync_staff"));
        public static final StreamCodec<FriendlyByteBuf, SyncStaffPayload> CODEC = CustomPacketPayload.codec(
                (payload, buf) -> {
                    buf.writeInt(payload.builders);
                    buf.writeInt(payload.activeBuilders);
                    buf.writeInt(payload.zookeepers);
                    buf.writeInt(payload.security);
                    buf.writeInt(payload.cashiers);
                },
                buf -> new SyncStaffPayload(buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt())
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

    // 10g. Sync Emerald Balance Payload (S2C)
    public record SyncEmeraldBalancePayload(int balance, int delta) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<SyncEmeraldBalancePayload> TYPE = new CustomPacketPayload.Type<>(ZooTycoonMod.id("sync_emerald_balance"));
        public static final StreamCodec<FriendlyByteBuf, SyncEmeraldBalancePayload> CODEC = CustomPacketPayload.codec(
                (payload, buf) -> {
                    buf.writeInt(payload.balance);
                    buf.writeInt(payload.delta);
                },
                buf -> new SyncEmeraldBalancePayload(buf.readInt(), buf.readInt())
        );
        @Override public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    // 10h. Assign Zookeeper Payload (C2S)
    public record AssignZookeeperPayload(int animalEntityId, UUID zookeeperUuid) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<AssignZookeeperPayload> TYPE = new CustomPacketPayload.Type<>(ZooTycoonMod.id("assign_zookeeper"));
        public static final StreamCodec<FriendlyByteBuf, AssignZookeeperPayload> CODEC = CustomPacketPayload.codec(
                (payload, buf) -> {
                    buf.writeInt(payload.animalEntityId);
                    buf.writeBoolean(payload.zookeeperUuid != null);
                    if (payload.zookeeperUuid != null) {
                        buf.writeUUID(payload.zookeeperUuid);
                    }
                },
                buf -> {
                    int animalId = buf.readInt();
                    UUID zkUuid = buf.readBoolean() ? buf.readUUID() : null;
                    return new AssignZookeeperPayload(animalId, zkUuid);
                }
        );
        @Override public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record ZookeeperSyncData(UUID uuid, String name, int entityId, boolean isAssigned, int assignedAnimalId) {
        public static void encode(FriendlyByteBuf buf, ZookeeperSyncData data) {
            buf.writeUUID(data.uuid);
            buf.writeUtf(data.name);
            buf.writeInt(data.entityId);
            buf.writeBoolean(data.isAssigned);
            buf.writeInt(data.assignedAnimalId);
        }
        public static ZookeeperSyncData decode(FriendlyByteBuf buf) {
            return new ZookeeperSyncData(buf.readUUID(), buf.readUtf(), buf.readInt(), buf.readBoolean(), buf.readInt());
        }
    }

    // 10i. Sync Zookeepers Payload (S2C)
    public record SyncZookeepersPayload(List<ZookeeperSyncData> zookeepers) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<SyncZookeepersPayload> TYPE = new CustomPacketPayload.Type<>(ZooTycoonMod.id("sync_zookeepers"));
        public static final StreamCodec<FriendlyByteBuf, SyncZookeepersPayload> CODEC = CustomPacketPayload.codec(
                (payload, buf) -> {
                    buf.writeInt(payload.zookeepers.size());
                    for (var zk : payload.zookeepers) {
                        ZookeeperSyncData.encode(buf, zk);
                    }
                },
                buf -> {
                    int count = buf.readInt();
                    List<ZookeeperSyncData> list = new ArrayList<>(count);
                    for (int i = 0; i < count; i++) {
                        list.add(ZookeeperSyncData.decode(buf));
                    }
                    return new SyncZookeepersPayload(list);
                }
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
            int thirst,
            int cleanliness,
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
                        buf.writeInt(d.thirst());
                        buf.writeInt(d.cleanliness());
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
                                buf.readInt(), buf.readUtf(), buf.readUtf(), buf.readInt(), buf.readInt(), buf.readInt(),
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

    // 16. Open Security Screen Payload (S2C)
    public record OpenSecurityScreenPayload(
            int entityId,
            UUID securityUuid,
            String securityName,
            boolean isAlert,
            String targetName,
            int health,
            int maxHealth
    ) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<OpenSecurityScreenPayload> TYPE = new CustomPacketPayload.Type<>(ZooTycoonMod.id("open_security_screen"));
        public static final StreamCodec<FriendlyByteBuf, OpenSecurityScreenPayload> CODEC = CustomPacketPayload.codec(
                (payload, buf) -> {
                    buf.writeInt(payload.entityId);
                    buf.writeUUID(payload.securityUuid);
                    buf.writeUtf(payload.securityName != null ? payload.securityName : net.zootycoon.util.ZooTranslations.get("zootycoon.dialog.security.default_name"));
                    buf.writeBoolean(payload.isAlert);
                    buf.writeUtf(payload.targetName != null ? payload.targetName : "");
                    buf.writeInt(payload.health);
                    buf.writeInt(payload.maxHealth);
                },
                buf -> new OpenSecurityScreenPayload(
                        buf.readInt(),
                        buf.readUUID(),
                        buf.readUtf(),
                        buf.readBoolean(),
                        buf.readUtf(),
                        buf.readInt(),
                        buf.readInt()
                )
        );
        @Override public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    // 17. Open Visitor Screen Payload (S2C)
    public record OpenVisitorScreenPayload(
            int entityId,
            UUID visitorUuid,
            int variant,
            boolean isChild,
            String moodName,
            String moodLabel,
            int hunger,
            int thirst,
            boolean needsToilet,
            boolean hasTrash
    ) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<OpenVisitorScreenPayload> TYPE = new CustomPacketPayload.Type<>(ZooTycoonMod.id("open_visitor_screen"));
        public static final StreamCodec<FriendlyByteBuf, OpenVisitorScreenPayload> CODEC = CustomPacketPayload.codec(
                (payload, buf) -> {
                    buf.writeInt(payload.entityId);
                    buf.writeUUID(payload.visitorUuid);
                    buf.writeInt(payload.variant);
                    buf.writeBoolean(payload.isChild);
                    buf.writeUtf(payload.moodName != null ? payload.moodName : "NEUTRAL");
                    buf.writeUtf(payload.moodLabel != null ? payload.moodLabel : "");
                    buf.writeInt(payload.hunger);
                    buf.writeInt(payload.thirst);
                    buf.writeBoolean(payload.needsToilet);
                    buf.writeBoolean(payload.hasTrash);
                },
                buf -> new OpenVisitorScreenPayload(
                        buf.readInt(),
                        buf.readUUID(),
                        buf.readInt(),
                        buf.readBoolean(),
                        buf.readUtf(),
                        buf.readUtf(),
                        buf.readInt(),
                        buf.readInt(),
                        buf.readBoolean(),
                        buf.readBoolean()
                )
        );
        @Override public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    // 18. Greet Visitor Payload (C2S)
    public record GreetVisitorPayload(int entityId) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<GreetVisitorPayload> TYPE = new CustomPacketPayload.Type<>(ZooTycoonMod.id("greet_visitor"));
        public static final StreamCodec<FriendlyByteBuf, GreetVisitorPayload> CODEC = CustomPacketPayload.codec(
                (payload, buf) -> buf.writeInt(payload.entityId),
                buf -> new GreetVisitorPayload(buf.readInt())
        );
        @Override public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    // 18b. Guide Visitor Payload (C2S)
    public record GuideVisitorPayload(int entityId, boolean follow) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<GuideVisitorPayload> TYPE = new CustomPacketPayload.Type<>(ZooTycoonMod.id("guide_visitor"));
        public static final StreamCodec<FriendlyByteBuf, GuideVisitorPayload> CODEC = CustomPacketPayload.codec(
                (payload, buf) -> {
                    buf.writeInt(payload.entityId);
                    buf.writeBoolean(payload.follow);
                },
                buf -> new GuideVisitorPayload(buf.readInt(), buf.readBoolean())
        );
        @Override public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    // 18c. Sync Rating Payload (S2C)
    public record SyncRatingPayload(int rating) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<SyncRatingPayload> TYPE = new CustomPacketPayload.Type<>(ZooTycoonMod.id("sync_rating"));
        public static final StreamCodec<FriendlyByteBuf, SyncRatingPayload> CODEC = CustomPacketPayload.codec(
                (payload, buf) -> buf.writeInt(payload.rating),
                buf -> new SyncRatingPayload(buf.readInt())
        );
        @Override public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    // 19. Interact Security Payload (C2S)
    public record InteractSecurityPayload(int entityId) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<InteractSecurityPayload> TYPE = new CustomPacketPayload.Type<>(ZooTycoonMod.id("interact_security"));
        public static final StreamCodec<FriendlyByteBuf, InteractSecurityPayload> CODEC = CustomPacketPayload.codec(
                (payload, buf) -> buf.writeInt(payload.entityId),
                buf -> new InteractSecurityPayload(buf.readInt())
        );
        @Override public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    // 20. Open Cashier Screen Payload (S2C)
    public record OpenCashierScreenPayload(int entityId, String currentRole, String cashierName, List<ItemStack> stockItems) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<OpenCashierScreenPayload> TYPE = new CustomPacketPayload.Type<>(ZooTycoonMod.id("open_cashier_screen"));
        public static final StreamCodec<FriendlyByteBuf, OpenCashierScreenPayload> CODEC = CustomPacketPayload.codec(
                (payload, buf) -> {
                    buf.writeInt(payload.entityId);
                    buf.writeUtf(payload.currentRole != null ? payload.currentRole : "UNASSIGNED");
                    buf.writeUtf(payload.cashierName != null ? payload.cashierName : "");
                    List<ItemStack> items = payload.stockItems != null ? payload.stockItems : List.of();
                    buf.writeInt(items.size());
                    for (ItemStack st : items) {
                        if (st == null || st.isEmpty()) {
                            buf.writeUtf("minecraft:air");
                            buf.writeInt(0);
                        } else {
                            ResourceLocation key = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(st.getItem());
                            buf.writeUtf(key != null ? key.toString() : "minecraft:air");
                            buf.writeInt(st.getCount());
                        }
                    }
                },
                buf -> {
                    int entityId = buf.readInt();
                    String role = buf.readUtf();
                    String name = buf.readUtf();
                    int size = buf.readInt();
                    List<ItemStack> items = new ArrayList<>(size);
                    for (int i = 0; i < size; i++) {
                        String idStr = buf.readUtf();
                        int count = buf.readInt();
                        if (count <= 0 || "minecraft:air".equals(idStr)) {
                            items.add(ItemStack.EMPTY);
                        } else {
                            try {
                                net.minecraft.world.item.Item item = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(ResourceLocation.parse(idStr));
                                items.add(new ItemStack(item, count));
                            } catch (Exception e) {
                                items.add(ItemStack.EMPTY);
                            }
                        }
                    }
                    return new OpenCashierScreenPayload(entityId, role, name, items);
                }
        );
        @Override public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    // 21. Set Cashier Role Payload (C2S)
    public record SetCashierRolePayload(int entityId, String role) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<SetCashierRolePayload> TYPE = new CustomPacketPayload.Type<>(ZooTycoonMod.id("set_cashier_role"));
        public static final StreamCodec<FriendlyByteBuf, SetCashierRolePayload> CODEC = CustomPacketPayload.codec(
                (payload, buf) -> {
                    buf.writeInt(payload.entityId);
                    buf.writeUtf(payload.role != null ? payload.role : "UNASSIGNED");
                },
                buf -> new SetCashierRolePayload(buf.readInt(), buf.readUtf())
        );
        @Override public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    // 21b. Cashier Slot Click / Inventory Transfer Payload (C2S)
    // action: 0 = insert from playerSlot into cashierSlot (or -1 for auto-slot)
    //         1 = withdraw from cashierSlot to player inventory
    public record CashierSlotClickPayload(int entityId, int action, int playerSlot, int cashierSlot, boolean singleItem) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<CashierSlotClickPayload> TYPE = new CustomPacketPayload.Type<>(ZooTycoonMod.id("cashier_slot_click"));
        public static final StreamCodec<FriendlyByteBuf, CashierSlotClickPayload> CODEC = CustomPacketPayload.codec(
                (payload, buf) -> {
                    buf.writeInt(payload.entityId);
                    buf.writeInt(payload.action);
                    buf.writeInt(payload.playerSlot);
                    buf.writeInt(payload.cashierSlot);
                    buf.writeBoolean(payload.singleItem);
                },
                buf -> new CashierSlotClickPayload(buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readBoolean())
        );
        @Override public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    // 21c. Rotate Cashier Payload (C2S)
    public record RotateCashierPayload(int entityId) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<RotateCashierPayload> TYPE = new CustomPacketPayload.Type<>(ZooTycoonMod.id("rotate_cashier"));
        public static final StreamCodec<FriendlyByteBuf, RotateCashierPayload> CODEC = CustomPacketPayload.codec(
                (payload, buf) -> buf.writeInt(payload.entityId),
                buf -> new RotateCashierPayload(buf.readInt())
        );
        @Override public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    // 20. Show Toast Notification Payload (S2C)
    public record ShowToastPayload(String titleKey, String msgKey, List<String> args, int toastType) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<ShowToastPayload> TYPE = new CustomPacketPayload.Type<>(ZooTycoonMod.id("show_toast"));
        public static final StreamCodec<FriendlyByteBuf, ShowToastPayload> CODEC = CustomPacketPayload.codec(
                (payload, buf) -> {
                    buf.writeUtf(payload.titleKey != null ? payload.titleKey : "zootycoon.toast.title.info");
                    buf.writeUtf(payload.msgKey != null ? payload.msgKey : "");
                    List<String> list = payload.args != null ? payload.args : List.of();
                    buf.writeInt(list.size());
                    for (String s : list) {
                        buf.writeUtf(s != null ? s : "");
                    }
                    buf.writeInt(payload.toastType);
                },
                buf -> {
                    String titleKey = buf.readUtf();
                    String msgKey = buf.readUtf();
                    int count = buf.readInt();
                    List<String> args = new ArrayList<>(count);
                    for (int i = 0; i < count; i++) {
                        args.add(buf.readUtf());
                    }
                    int toastType = buf.readInt();
                    return new ShowToastPayload(titleKey, msgKey, args, toastType);
                }
        );
        @Override public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    // 21. Request Client Selection Save Payload (S2C) for /kandang and /fasilitas
    public record RequestClientSelectionSavePayload(String name, String category) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<RequestClientSelectionSavePayload> TYPE = new CustomPacketPayload.Type<>(ZooTycoonMod.id("req_client_sel_save"));
        public static final StreamCodec<FriendlyByteBuf, RequestClientSelectionSavePayload> CODEC = CustomPacketPayload.codec(
                (payload, buf) -> {
                    buf.writeUtf(payload.name);
                    buf.writeUtf(payload.category);
                },
                buf -> new RequestClientSelectionSavePayload(buf.readUtf(), buf.readUtf())
        );
        @Override public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public static void sendToast(ServerPlayer player, String titleKey, String msgKey, int toastType, Object... args) {
        if (player == null) return;
        List<String> strArgs = new ArrayList<>();
        if (args != null) {
            for (Object a : args) {
                strArgs.add(String.valueOf(a));
            }
        }
        ServerPlayNetworking.send(player, new ShowToastPayload(titleKey, msgKey, strArgs, toastType));
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
        PayloadTypeRegistry.playC2S().register(GreetVisitorPayload.TYPE, GreetVisitorPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(GuideVisitorPayload.TYPE, GuideVisitorPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(InteractSecurityPayload.TYPE, InteractSecurityPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(SetCashierRolePayload.TYPE, SetCashierRolePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(CashierSlotClickPayload.TYPE, CashierSlotClickPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(RotateCashierPayload.TYPE, RotateCashierPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(AssignZookeeperPayload.TYPE, AssignZookeeperPayload.CODEC);

        PayloadTypeRegistry.playS2C().register(SyncClaimsPayload.TYPE, SyncClaimsPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SyncStudioStatePayload.TYPE, SyncStudioStatePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SyncStaffPayload.TYPE, SyncStaffPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(OpenBuilderScreenPayload.TYPE, OpenBuilderScreenPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(OpenZookeeperScreenPayload.TYPE, OpenZookeeperScreenPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(OpenSecurityScreenPayload.TYPE, OpenSecurityScreenPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(OpenVisitorScreenPayload.TYPE, OpenVisitorScreenPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(OpenCashierScreenPayload.TYPE, OpenCashierScreenPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SyncAnimalsPayload.TYPE, SyncAnimalsPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SyncZookeepersPayload.TYPE, SyncZookeepersPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SyncEmeraldBalancePayload.TYPE, SyncEmeraldBalancePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SyncRatingPayload.TYPE, SyncRatingPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ShowToastPayload.TYPE, ShowToastPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(RequestClientSelectionSavePayload.TYPE, RequestClientSelectionSavePayload.CODEC);

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
                    sendToast(player, "zootycoon.toast.title.error", "zootycoon.build.not_found", 3);
                }
            });
        });

        // Handler: Deposit Materials
        ServerPlayNetworking.registerGlobalReceiver(DepositMaterialsPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> {
                ConstructionManager.getSite(payload.siteId).ifPresent(site -> {
                    if (player.isCreative()) {
                        site.fulfillAllMaterials();
                        if (site.getBuilderUuid() != null) {
                            var b = ((ServerLevel) player.level()).getEntity(site.getBuilderUuid());
                            if (b instanceof net.zootycoon.entity.ZooBuilderEntity zb) {
                                zb.updateNeedsIndicator();
                            }
                        }
                        player.level().playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.ITEM_PICKUP, net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 1.0f);
                        sendToast(player, "zootycoon.toast.title.builder", "zootycoon.builder.materials_given", 1, 999);
                        ConstructionManager.saveToFile(player.server);
                        return;
                    }
                    int totalAccepted = 0;
                    for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                        ItemStack stack = player.getInventory().getItem(i);
                        if (!stack.isEmpty()) {
                            int accepted = site.deliverMaterial(stack);
                            totalAccepted += accepted;
                        }
                    }
                    if (site.getBuilderUuid() != null) {
                        var b = ((ServerLevel) player.level()).getEntity(site.getBuilderUuid());
                        if (b instanceof net.zootycoon.entity.ZooBuilderEntity zb) {
                            zb.updateNeedsIndicator();
                        }
                    }
                    if (totalAccepted > 0) {
                        player.level().playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.ITEM_PICKUP, net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 1.0f);
                        sendToast(player, "zootycoon.toast.title.builder", "zootycoon.builder.materials_given", 1, totalAccepted);
                    } else {
                        sendToast(player, "zootycoon.toast.title.builder", "zootycoon.builder.no_materials_in_hand", 2);
                    }
                    ConstructionManager.saveToFile(player.server);
                });
            });
        });

        // Handler: Deposit Tools
        ServerPlayNetworking.registerGlobalReceiver(DepositToolsPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> {
                ConstructionManager.getSite(payload.siteId).ifPresent(site -> {
                    boolean success;
                    if (player.isCreative()) {
                        site.fulfillAllTools();
                        success = true;
                    } else {
                        success = site.depositTools(player);
                    }
                    if (success) {
                        player.level().playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.ITEM_PICKUP, net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 1.0f);
                        sendToast(player, "zootycoon.toast.title.builder", "zootycoon.builder.tools_given", 1);
                        if (site.getBuilderUuid() != null) {
                            ServerLevel sLevel = (ServerLevel) player.level();
                            var ent = sLevel.getEntity(site.getBuilderUuid());
                            if (ent instanceof net.zootycoon.entity.ZooBuilderEntity zb) {
                                zb.updateNeedsIndicator();
                            }
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
                        ConstructionManager.saveToFile(player.server);
                    } else {
                        sendToast(player, "zootycoon.toast.title.builder", "zootycoon.builder.no_tools_in_hand", 2);
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
                    sendToast(player, "zootycoon.toast.title.success", "zootycoon.staff.hired", 1, payload.staffType());
                } else {
                    sendToast(player, "zootycoon.toast.title.error", "zootycoon.staff.not_enough_emeralds", 3);
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
                if (saved && StudioDimensionManager.isInStudio(player.getUUID())) {
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
                    sendToast(player, "zootycoon.toast.title.animal", "zootycoon.animal.renamed", 1, payload.newName);
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

        // Handler: Scan Animal with Spyglass / Field Guide
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
                            int hungerAfter = net.zootycoon.animal.ZooAnimalHungerManager.getHunger(living);
                            sendToast(player, "zootycoon.toast.title.animal", "zootycoon.animal.fed", 1, living.getName().getString(), hungerAfter);
                            sendAnimalSync(player);
                        }
                    } else {
                        sendToast(player, "zootycoon.toast.title.animal", "zootycoon.animal.wrong_food", 3);
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

        // Handler: Greet Visitor from GUI
        ServerPlayNetworking.registerGlobalReceiver(GreetVisitorPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            net.minecraft.server.level.ServerLevel level = (net.minecraft.server.level.ServerLevel) player.level();
            context.server().execute(() -> {
                net.minecraft.world.entity.Entity ent = level.getEntity(payload.entityId());
                if (ent instanceof net.zootycoon.entity.ZooVisitorEntity visitor) {
                    level.playSound(null, visitor.blockPosition(), net.minecraft.sounds.SoundEvents.VILLAGER_YES, net.minecraft.sounds.SoundSource.NEUTRAL, 1.0F, 1.0F);
                    level.sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER, visitor.getX(), visitor.getY() + 1.2D, visitor.getZ(), 5, 0.25, 0.25, 0.25, 0.05);
                    sendToast(player, "zootycoon.toast.title.info", "zootycoon.visitor.greeted", 1);
                }
            });
        });

        // Handler: Guide Visitor ("Ikuti Saya" / "Belum Ada")
        ServerPlayNetworking.registerGlobalReceiver(GuideVisitorPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            net.minecraft.server.level.ServerLevel level = (net.minecraft.server.level.ServerLevel) player.level();
            context.server().execute(() -> {
                net.minecraft.world.entity.Entity ent = level.getEntity(payload.entityId());
                if (ent instanceof net.zootycoon.entity.ZooVisitorEntity visitor && visitor.isAlive()) {
                    if (payload.follow()) {
                        // "Ikuti Saya" -> visitor starts following player
                        visitor.startFollowingPlayer(player);
                        level.playSound(null, visitor.blockPosition(), net.minecraft.sounds.SoundEvents.VILLAGER_YES, net.minecraft.sounds.SoundSource.NEUTRAL, 1.0F, 1.1F);
                        level.sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER, visitor.getX(), visitor.getY() + 1.2D, visitor.getZ(), 5, 0.25, 0.25, 0.25, 0.05);
                        sendToast(player, "zootycoon.toast.title.visitor", "zootycoon.dialog.visitor.guiding_toast", 1);
                    } else {
                        // "Belum Ada" -> visitor disappointed, rating decreases
                        visitor.stopFollowingPlayer();
                        level.playSound(null, visitor.blockPosition(), net.minecraft.sounds.SoundEvents.VILLAGER_NO, net.minecraft.sounds.SoundSource.NEUTRAL, 1.0F, 0.9F);
                        level.sendParticles(net.minecraft.core.particles.ParticleTypes.ANGRY_VILLAGER, visitor.getX(), visitor.getY() + 1.2D, visitor.getZ(), 5, 0.25, 0.25, 0.25, 0.02);
                        int newRating = net.zootycoon.economy.ZooRatingManager.get(level.getServer()).deductRating(player.getUUID(), 2, player);
                        sendToast(player, "zootycoon.toast.title.rating_down", "zootycoon.toast.rating.not_available", 2, newRating);
                    }
                }
            });
        });

        // Handler: Interact Security from GUI
        ServerPlayNetworking.registerGlobalReceiver(InteractSecurityPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            net.minecraft.server.level.ServerLevel level = (net.minecraft.server.level.ServerLevel) player.level();
            context.server().execute(() -> {
                net.minecraft.world.entity.Entity ent = level.getEntity(payload.entityId());
                if (ent instanceof net.zootycoon.entity.ZooSecurityEntity security) {
                    level.playSound(null, security.blockPosition(), net.minecraft.sounds.SoundEvents.ARMOR_EQUIP_IRON.value(), net.minecraft.sounds.SoundSource.NEUTRAL, 1.0F, 1.0F);
                    sendToast(player, "zootycoon.toast.title.security", "zootycoon.security.patrol_ordered", 0);
                }
            });
        });

        // Handler: Set Cashier Role from GUI (permanent once assigned)
        ServerPlayNetworking.registerGlobalReceiver(SetCashierRolePayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            net.minecraft.server.level.ServerLevel level = (net.minecraft.server.level.ServerLevel) player.level();
            context.server().execute(() -> {
                net.minecraft.world.entity.Entity ent = level.getEntity(payload.entityId());
                if (ent instanceof net.zootycoon.entity.ZooCashierEntity cashier && cashier.isAlive()) {
                    if (cashier.tryAssignRole(payload.role())) {
                        level.playSound(null, cashier.blockPosition(), net.minecraft.sounds.SoundEvents.VILLAGER_YES, net.minecraft.sounds.SoundSource.NEUTRAL, 1.0F, 1.0F);
                        sendToast(player, "zootycoon.toast.title.info", "zootycoon.cashier.assigned", 1, cashier.getName().getString());
                        cashier.sendScreenToPlayer(player);
                    } else {
                        sendToast(player, "zootycoon.toast.title.warning", "zootycoon.cashier.role_locked", 2);
                    }
                }
            });
        });

        // Handler: Rotate Cashier from GUI
        ServerPlayNetworking.registerGlobalReceiver(RotateCashierPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            net.minecraft.server.level.ServerLevel level = (net.minecraft.server.level.ServerLevel) player.level();
            context.server().execute(() -> {
                net.minecraft.world.entity.Entity ent = level.getEntity(payload.entityId());
                if (ent instanceof net.zootycoon.entity.ZooCashierEntity cashier && cashier.isAlive()) {
                    cashier.rotateClockwise();
                    level.playSound(null, cashier.blockPosition(), net.minecraft.sounds.SoundEvents.ITEM_FRAME_ROTATE_ITEM, net.minecraft.sounds.SoundSource.NEUTRAL, 0.9F, 1.1F);
                }
            });
        });

        // Handler: Cashier Slot Click / Inventory Transfer
        ServerPlayNetworking.registerGlobalReceiver(CashierSlotClickPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            net.minecraft.server.level.ServerLevel level = (net.minecraft.server.level.ServerLevel) player.level();
            context.server().execute(() -> {
                net.minecraft.world.entity.Entity ent = level.getEntity(payload.entityId());
                if (!(ent instanceof net.zootycoon.entity.ZooCashierEntity cashier) || !cashier.isAlive()) {
                    return;
                }
                String role = cashier.getCashierRole();
                var stockInv = cashier.getStockInventory();

                if (payload.action() == 0) {
                    // Insert from player inventory slot into cashier 5-slot stock
                    int pSlot = payload.playerSlot();
                    if (pSlot < 0 || pSlot >= player.getInventory().items.size()) return;
                    ItemStack pStack = player.getInventory().getItem(pSlot);
                    if (pStack.isEmpty()) return;

                    if (!net.zootycoon.entity.ZooCashierEntity.isValidItemForRole(role, pStack)) {
                        String msgKey = switch (role) {
                            case "BURGER" -> "zootycoon.cashier.invalid_item_burger";
                            case "DRINK" -> "zootycoon.cashier.invalid_item_drink";
                            case "SOUVENIR" -> "zootycoon.cashier.invalid_item_souvenir";
                            default -> "zootycoon.cashier.invalid_item_generic";
                        };
                        sendToast(player, "zootycoon.toast.title.warning", msgKey, 2);
                        return;
                    }

                    int maxToMove = payload.singleItem() ? 1 : pStack.getCount();
                    int movedTotal = 0;
                    int targetSlot = payload.cashierSlot();

                    // 1. If a specific cashierSlot (0..4) was targeted, try inserting there first
                    if (targetSlot >= 0 && targetSlot < net.zootycoon.entity.ZooCashierEntity.STOCK_SLOTS) {
                        ItemStack existing = stockInv.getItem(targetSlot);
                        if (existing.isEmpty()) {
                            int cap = Math.min(maxToMove, pStack.getMaxStackSize());
                            stockInv.setItem(targetSlot, pStack.split(cap));
                            movedTotal += cap;
                        } else if (ItemStack.isSameItemSameComponents(existing, pStack) && existing.getCount() < existing.getMaxStackSize()) {
                            int space = existing.getMaxStackSize() - existing.getCount();
                            int add = Math.min(maxToMove, space);
                            existing.grow(add);
                            pStack.shrink(add);
                            movedTotal += add;
                        }
                    }

                    // 2. If auto-slot (-1) or still remaining items from a stack transfer, merge into existing stacks then empty slots
                    int remaining = maxToMove - movedTotal;
                    if (remaining > 0 && !pStack.isEmpty()) {
                        for (int i = 0; i < net.zootycoon.entity.ZooCashierEntity.STOCK_SLOTS && remaining > 0 && !pStack.isEmpty(); i++) {
                            ItemStack existing = stockInv.getItem(i);
                            if (!existing.isEmpty() && ItemStack.isSameItemSameComponents(existing, pStack) && existing.getCount() < existing.getMaxStackSize()) {
                                int space = existing.getMaxStackSize() - existing.getCount();
                                int add = Math.min(remaining, Math.min(space, pStack.getCount()));
                                existing.grow(add);
                                pStack.shrink(add);
                                remaining -= add;
                                movedTotal += add;
                            }
                        }
                        for (int i = 0; i < net.zootycoon.entity.ZooCashierEntity.STOCK_SLOTS && remaining > 0 && !pStack.isEmpty(); i++) {
                            ItemStack existing = stockInv.getItem(i);
                            if (existing.isEmpty()) {
                                int add = Math.min(remaining, Math.min(pStack.getMaxStackSize(), pStack.getCount()));
                                stockInv.setItem(i, pStack.split(add));
                                remaining -= add;
                                movedTotal += add;
                            }
                        }
                    }

                    if (pStack.isEmpty()) {
                        player.getInventory().setItem(pSlot, ItemStack.EMPTY);
                    }
                    if (movedTotal > 0) {
                        player.getInventory().setChanged();
                        cashier.updateStockStatus();
                        level.playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.ITEM_PICKUP, net.minecraft.sounds.SoundSource.PLAYERS, 0.7F, 1.15F);
                        cashier.sendScreenToPlayer(player);
                    }
                } else if (payload.action() == 1) {
                    // Withdraw from cashierSlot (0..4) back to player inventory
                    int cSlot = payload.cashierSlot();
                    if (cSlot < 0 || cSlot >= net.zootycoon.entity.ZooCashierEntity.STOCK_SLOTS) return;
                    ItemStack cStack = stockInv.getItem(cSlot);
                    if (cStack.isEmpty()) return;

                    int takeCount = payload.singleItem() ? 1 : cStack.getCount();
                    ItemStack extracted = cStack.split(takeCount);
                    if (!player.getInventory().add(extracted)) {
                        // Put unadded remainder back in slot
                        if (cStack.isEmpty()) {
                            stockInv.setItem(cSlot, extracted);
                        } else {
                            cStack.grow(extracted.getCount());
                        }
                    } else {
                        if (cStack.isEmpty()) {
                            stockInv.setItem(cSlot, ItemStack.EMPTY);
                        }
                        player.getInventory().setChanged();
                        cashier.updateStockStatus();
                        level.playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.ITEM_PICKUP, net.minecraft.sounds.SoundSource.PLAYERS, 0.7F, 0.9F);
                        cashier.sendScreenToPlayer(player);
                    }
                }
            });
        });

        // Handler: Assign Zookeeper to Animal
        ServerPlayNetworking.registerGlobalReceiver(AssignZookeeperPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> {
                for (ServerLevel sLevel : player.level().getServer().getAllLevels()) {
                    net.minecraft.world.entity.Entity ent = sLevel.getEntity(payload.animalEntityId());
                    if (ent instanceof net.minecraft.world.entity.LivingEntity animal && net.zootycoon.animal.ZooAnimalHungerManager.isOwnedBy(animal, player.getUUID())) {
                        UUID oldZkUuid = net.zootycoon.animal.ZooAnimalHungerManager.getAssignedZookeeper(animal);
                        if (oldZkUuid != null) {
                            for (ServerLevel sl : player.level().getServer().getAllLevels()) {
                                net.minecraft.world.entity.Entity oldZkEnt = sl.getEntity(oldZkUuid);
                                if (oldZkEnt instanceof net.zootycoon.entity.ZooZookeeperEntity oldKeeper) {
                                    oldKeeper.setAssignedAnimalUuid(null);
                                    break;
                                }
                            }
                        }
                        net.zootycoon.animal.ZooAnimalHungerManager.setAssignedZookeeper(animal, payload.zookeeperUuid());
                        if (payload.zookeeperUuid() != null) {
                            for (ServerLevel sl : player.level().getServer().getAllLevels()) {
                                net.minecraft.world.entity.Entity zkEnt = sl.getEntity(payload.zookeeperUuid());
                                if (zkEnt instanceof net.zootycoon.entity.ZooZookeeperEntity keeper) {
                                    UUID prevAnimalUuid = keeper.getAssignedAnimalUuid();
                                    if (prevAnimalUuid != null && !prevAnimalUuid.equals(animal.getUUID())) {
                                        for (ServerLevel sl2 : player.level().getServer().getAllLevels()) {
                                            net.minecraft.world.entity.Entity prevAnimalEnt = sl2.getEntity(prevAnimalUuid);
                                            if (prevAnimalEnt instanceof net.minecraft.world.entity.LivingEntity prevLiving) {
                                                net.zootycoon.animal.ZooAnimalHungerManager.setAssignedZookeeper(prevLiving, null);
                                                break;
                                            }
                                        }
                                    }
                                    keeper.setAssignedAnimalUuid(animal.getUUID());
                                    break;
                                }
                            }
                        }
                        sendAnimalSync(player);
                        sendToast(player, "zootycoon.toast.title.success", "zootycoon.ui.animals.zookeeper_assigned_toast", 1);
                        break;
                    }
                }
            });
        });
    }

    public static void sendAnimalSync(ServerPlayer player) {
        if (player == null || player.level().getServer() == null) return;
        List<AnimalSyncData> list = new ArrayList<>();
        List<ZookeeperSyncData> keepers = new ArrayList<>();

        for (ServerLevel sLevel : player.level().getServer().getAllLevels()) {
            for (net.minecraft.world.entity.Entity entity : sLevel.getAllEntities()) {
                if (entity instanceof net.zootycoon.entity.ZooZookeeperEntity zk && player.getUUID().equals(zk.getOwnerUuid())) {
                    UUID aUuid = zk.getAssignedAnimalUuid();
                    int assignedAnimalId = -1;
                    boolean isAssigned = false;
                    if (aUuid != null) {
                        for (ServerLevel sl : player.level().getServer().getAllLevels()) {
                            net.minecraft.world.entity.Entity aEnt = sl.getEntity(aUuid);
                            if (aEnt instanceof net.minecraft.world.entity.LivingEntity living && net.zootycoon.animal.ZooAnimalHungerManager.isTagged(living)) {
                                if (zk.getUUID().equals(net.zootycoon.animal.ZooAnimalHungerManager.getAssignedZookeeper(living))) {
                                    assignedAnimalId = living.getId();
                                    isAssigned = true;
                                    break;
                                }
                            }
                        }
                        if (!isAssigned) {
                            zk.setAssignedAnimalUuid(null);
                        }
                    }
                    keepers.add(new ZookeeperSyncData(zk.getUUID(), zk.getName().getString(), zk.getId(), isAssigned, assignedAnimalId));
                }

                if (entity instanceof net.minecraft.world.entity.LivingEntity animal
                        && net.zootycoon.animal.ZooAnimalHungerManager.isTagged(animal)
                        && net.zootycoon.animal.ZooAnimalHungerManager.isZooAnimalCandidate(animal)
                        && net.zootycoon.animal.ZooAnimalHungerManager.isOwnedBy(animal, player.getUUID())) {

                    String typeId = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(animal.getType()).toString();
                    String name = animal.hasCustomName() ? animal.getCustomName().getString() : "";
                    int hunger = net.zootycoon.animal.ZooAnimalHungerManager.getHunger(animal);
                    int thirst = net.zootycoon.animal.ZooAnimalHungerManager.getThirst(animal);
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
                    int cleanliness = net.zootycoon.animal.ZooAnimalHungerManager.getCleanliness(animal);
                    list.add(new AnimalSyncData(animal.getId(), typeId, name, hunger, thirst, cleanliness, health, maxHealth, hasZk, zkName));
                }
            }
        }

        ServerPlayNetworking.send(player, new SyncAnimalsPayload(list));
        ServerPlayNetworking.send(player, new SyncZookeepersPayload(keepers));
    }
}


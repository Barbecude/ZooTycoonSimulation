package net.zootycoon.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.zootycoon.ZooTycoonMod;
import net.zootycoon.block.EmptyBarrelBlockEntity;
import net.zootycoon.warehouse.WarehouseSupplyManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WarehousePayloads {

    private static boolean registered = false;

    public record RawIngredientItemDto(ItemStack stack, boolean inBarrel) {
        public static void write(RegistryFriendlyByteBuf buf, RawIngredientItemDto dto) {
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, dto.stack);
            buf.writeBoolean(dto.inBarrel);
        }

        public static RawIngredientItemDto read(RegistryFriendlyByteBuf buf) {
            ItemStack stack = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
            boolean inBarrel = buf.readBoolean();
            return new RawIngredientItemDto(stack, inBarrel);
        }
    }

    public record MenuItemRequestDto(
            ItemStack targetItem,
            List<RawIngredientItemDto> rawIngredients,
            boolean isReadyInBarrel
    ) {
        public static void write(RegistryFriendlyByteBuf buf, MenuItemRequestDto dto) {
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, dto.targetItem);
            buf.writeVarInt(dto.rawIngredients.size());
            for (RawIngredientItemDto raw : dto.rawIngredients) {
                RawIngredientItemDto.write(buf, raw);
            }
            buf.writeBoolean(dto.isReadyInBarrel);
        }

        public static MenuItemRequestDto read(RegistryFriendlyByteBuf buf) {
            ItemStack target = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
            int size = buf.readVarInt();
            List<RawIngredientItemDto> raws = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                raws.add(RawIngredientItemDto.read(buf));
            }
            boolean ready = buf.readBoolean();
            return new MenuItemRequestDto(target, raws, ready);
        }
    }

    public record StaffRequestDto(
            UUID staffUuid,
            int staffEntityId,
            String staffType,
            String staffName,
            String subtitle,
            List<ItemStack> targetItems,
            List<MenuItemRequestDto> menuItems,
            int currentCount,
            int requiredCount,
            boolean allReadyToFulfill
    ) {
        public static void write(RegistryFriendlyByteBuf buf, StaffRequestDto dto) {
            buf.writeUUID(dto.staffUuid);
            buf.writeVarInt(dto.staffEntityId);
            buf.writeUtf(dto.staffType);
            buf.writeUtf(dto.staffName);
            buf.writeUtf(dto.subtitle);

            buf.writeVarInt(dto.targetItems.size());
            for (ItemStack st : dto.targetItems) {
                ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, st);
            }

            buf.writeVarInt(dto.menuItems.size());
            for (MenuItemRequestDto mi : dto.menuItems) {
                MenuItemRequestDto.write(buf, mi);
            }

            buf.writeVarInt(dto.currentCount);
            buf.writeVarInt(dto.requiredCount);
            buf.writeBoolean(dto.allReadyToFulfill);
        }

        public static StaffRequestDto read(RegistryFriendlyByteBuf buf) {
            UUID staffUuid = buf.readUUID();
            int staffEntityId = buf.readVarInt();
            String staffType = buf.readUtf();
            String staffName = buf.readUtf();
            String subtitle = buf.readUtf();

            int targetSize = buf.readVarInt();
            List<ItemStack> targets = new ArrayList<>(targetSize);
            for (int i = 0; i < targetSize; i++) {
                targets.add(ItemStack.OPTIONAL_STREAM_CODEC.decode(buf));
            }

            int menuSize = buf.readVarInt();
            List<MenuItemRequestDto> menus = new ArrayList<>(menuSize);
            for (int i = 0; i < menuSize; i++) {
                menus.add(MenuItemRequestDto.read(buf));
            }

            int currentCount = buf.readVarInt();
            int requiredCount = buf.readVarInt();
            boolean allReady = buf.readBoolean();
            return new StaffRequestDto(staffUuid, staffEntityId, staffType, staffName, subtitle, targets, menus, currentCount, requiredCount, allReady);
        }
    }

    public record OpenBarrelCategoryScreenPayload(BlockPos pos) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<OpenBarrelCategoryScreenPayload> TYPE =
                new CustomPacketPayload.Type<>(ZooTycoonMod.id("open_barrel_category_screen"));
        public static final StreamCodec<RegistryFriendlyByteBuf, OpenBarrelCategoryScreenPayload> CODEC =
                CustomPacketPayload.codec(
                        (payload, buf) -> buf.writeBlockPos(payload.pos),
                        buf -> new OpenBarrelCategoryScreenPayload(buf.readBlockPos())
                );

        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record SelectBarrelCategoryPayload(BlockPos pos, String category) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<SelectBarrelCategoryPayload> TYPE =
                new CustomPacketPayload.Type<>(ZooTycoonMod.id("select_barrel_category"));
        public static final StreamCodec<RegistryFriendlyByteBuf, SelectBarrelCategoryPayload> CODEC =
                CustomPacketPayload.codec(
                        (payload, buf) -> {
                            buf.writeBlockPos(payload.pos);
                            buf.writeUtf(payload.category);
                        },
                        buf -> new SelectBarrelCategoryPayload(buf.readBlockPos(), buf.readUtf())
                );

        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record SyncBarrelRequestsPayload(BlockPos pos, String category, List<StaffRequestDto> requests) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<SyncBarrelRequestsPayload> TYPE =
                new CustomPacketPayload.Type<>(ZooTycoonMod.id("sync_barrel_requests"));
        public static final StreamCodec<RegistryFriendlyByteBuf, SyncBarrelRequestsPayload> CODEC =
                CustomPacketPayload.codec(
                        (payload, buf) -> {
                            buf.writeBlockPos(payload.pos);
                            buf.writeUtf(payload.category);
                            buf.writeVarInt(payload.requests.size());
                            for (StaffRequestDto dto : payload.requests) {
                                StaffRequestDto.write(buf, dto);
                            }
                        },
                        buf -> {
                            BlockPos pos = buf.readBlockPos();
                            String category = buf.readUtf();
                            int count = buf.readVarInt();
                            List<StaffRequestDto> list = new ArrayList<>(count);
                            for (int i = 0; i < count; i++) {
                                list.add(StaffRequestDto.read(buf));
                            }
                            return new SyncBarrelRequestsPayload(pos, category, list);
                        }
                );

        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record RequestBarrelRecipeNotePayload(BlockPos pos, UUID staffUuid) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<RequestBarrelRecipeNotePayload> TYPE =
                new CustomPacketPayload.Type<>(ZooTycoonMod.id("request_barrel_recipe_note"));
        public static final StreamCodec<RegistryFriendlyByteBuf, RequestBarrelRecipeNotePayload> CODEC =
                CustomPacketPayload.codec(
                        (payload, buf) -> {
                            buf.writeBlockPos(payload.pos);
                            buf.writeUUID(payload.staffUuid);
                        },
                        buf -> new RequestBarrelRecipeNotePayload(buf.readBlockPos(), buf.readUUID())
                );

        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record FulfillStaffRequestPayload(BlockPos pos, UUID staffUuid) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<FulfillStaffRequestPayload> TYPE =
                new CustomPacketPayload.Type<>(ZooTycoonMod.id("fulfill_staff_request"));
        public static final StreamCodec<RegistryFriendlyByteBuf, FulfillStaffRequestPayload> CODEC =
                CustomPacketPayload.codec(
                        (payload, buf) -> {
                            buf.writeBlockPos(payload.pos);
                            buf.writeUUID(payload.staffUuid);
                        },
                        buf -> new FulfillStaffRequestPayload(buf.readBlockPos(), buf.readUUID())
                );

        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public static void registerCommon() {
        if (registered) return;
        registered = true;

        PayloadTypeRegistry.playS2C().register(OpenBarrelCategoryScreenPayload.TYPE, OpenBarrelCategoryScreenPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SyncBarrelRequestsPayload.TYPE, SyncBarrelRequestsPayload.CODEC);

        PayloadTypeRegistry.playC2S().register(SelectBarrelCategoryPayload.TYPE, SelectBarrelCategoryPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(RequestBarrelRecipeNotePayload.TYPE, RequestBarrelRecipeNotePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(FulfillStaffRequestPayload.TYPE, FulfillStaffRequestPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(SelectBarrelCategoryPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> {
                if (player.level() instanceof ServerLevel sLevel) {
                    if (player.blockPosition().distSqr(payload.pos()) > 64.0D) return;
                    BlockEntity be = sLevel.getBlockEntity(payload.pos());
                    if (be instanceof EmptyBarrelBlockEntity barrelBe) {
                        if (!barrelBe.isCategoryAssigned()) {
                            barrelBe.setCategoryAndOwner(payload.category(), player.getUUID());
                        }
                        barrelBe.openChestForPlayer(player, sLevel);
                    }
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(RequestBarrelRecipeNotePayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> {
                if (player.level() instanceof ServerLevel sLevel) {
                    WarehouseSupplyManager.giveRecipeNoteToPlayer(sLevel, player, payload.pos(), payload.staffUuid());
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(FulfillStaffRequestPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> {
                if (player.level() instanceof ServerLevel sLevel) {
                    WarehouseSupplyManager.fulfillStaffRequestManually(sLevel, player, payload.pos(), payload.staffUuid());
                }
            });
        });
    }
}


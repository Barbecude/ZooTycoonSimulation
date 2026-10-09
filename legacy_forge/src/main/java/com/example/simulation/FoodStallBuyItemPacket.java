package com.example.simulation;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

/**
 * C→S packet: player clicks "Stok" in the Katalog tab or "Ambil" in the Bahan Baku tab.
 * <ul>
 *   <li>{@code toInventory=false} → items placed in stall inventory (Katalog tab).</li>
 *   <li>{@code toInventory=true}  → items given to player inventory, zoo balance charged
 *       at {@code price × count} (Bahan Baku tab).</li>
 * </ul>
 */
public class FoodStallBuyItemPacket {

    private final BlockPos pos;
    private final String  itemId;
    private final int     count;
    private final int     sellPrice;
    private final boolean toInventory;

    public FoodStallBuyItemPacket(BlockPos pos, String itemId, int count, int sellPrice, boolean toInventory) {
        this.pos         = pos;
        this.itemId      = itemId;
        this.count       = count;
        this.sellPrice   = sellPrice;
        this.toInventory = toInventory;
    }

    // ── Codec ─────────────────────────────────────────────────────────────────

    public static void encode(FoodStallBuyItemPacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeUtf(msg.itemId, 128);
        buf.writeByte(msg.count);
        buf.writeVarInt(msg.sellPrice);
        buf.writeBoolean(msg.toInventory);
    }

    public static FoodStallBuyItemPacket decode(FriendlyByteBuf buf) {
        return new FoodStallBuyItemPacket(
                buf.readBlockPos(),
                buf.readUtf(128),
                buf.readByte() & 0xFF,
                buf.readVarInt(),
                buf.readBoolean());
    }

    // ── Handler ───────────────────────────────────────────────────────────────

    public static void handle(FoodStallBuyItemPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(msg.itemId));
            if (item == null || item == net.minecraft.world.item.Items.AIR) return;

            int toPlace = Math.max(1, Math.min(msg.count, item.getMaxStackSize()));
            ItemStack stack = new ItemStack(item, toPlace);

            if (msg.toInventory) {
                // ── Bahan Baku: give to player inventory, deduct from zoo balance ──
                long cost = (long) msg.sellPrice * toPlace;
                ZooData data = ZooData.get(player.serverLevel());
                if (data.getBalance() < cost) {
                    player.displayClientMessage(
                            net.minecraft.network.chat.Component.literal(
                                    "Balance tidak cukup! Butuh Rp" + String.format("%,d", cost))
                                    .withStyle(net.minecraft.ChatFormatting.RED),
                            true);
                    return;
                }
                data.setBalance((int) (data.getBalance() - cost));
                // Broadcast balance update
                SyncBalancePacket syncPkt = new SyncBalancePacket(
                        data.getBalance(), data.getTaggedAnimals(),
                        data.getAnimalCount(), data.getStaffCount(),
                        data.getVisitorCount(), data.getRating());
                PacketHandler.INSTANCE.send(net.minecraftforge.network.PacketDistributor.ALL.noArg(), syncPkt);

                if (!player.getInventory().add(stack)) {
                    // Full inventory — drop at feet
                    player.drop(stack, false);
                }
                player.displayClientMessage(
                        net.minecraft.network.chat.Component.literal(
                                "Dibeli: " + toPlace + "x " + stack.getHoverName().getString()
                                + " (-Rp" + String.format("%,d", cost) + ")")
                                .withStyle(net.minecraft.ChatFormatting.GREEN),
                        true);
                return;
            }

            // ── Katalog: put items into stall inventory ────────────────────────
            if (!(player.serverLevel().getBlockEntity(msg.pos) instanceof FoodStallBlockEntity stall)) return;

            // Try to merge into same item or find an empty slot
            for (int i = 0; i < stall.getContainerSize(); i++) {
                ItemStack existing = stall.getItem(i);
                if (existing.isEmpty()) {
                    stall.setItem(i, stack);
                    stall.setFoodPrice(msg.sellPrice);
                    stall.setChanged();
                    return;
                }
                if (ItemStack.isSameItemSameTags(existing, stack)
                        && existing.getCount() < existing.getMaxStackSize()) {
                    int space = existing.getMaxStackSize() - existing.getCount();
                    existing.grow(Math.min(space, stack.getCount()));
                    stall.setFoodPrice(msg.sellPrice);
                    stall.setChanged();
                    return;
                }
            }
            // No room: drop to player
            player.drop(stack, false);
        });
        ctx.get().setPacketHandled(true);
    }
}

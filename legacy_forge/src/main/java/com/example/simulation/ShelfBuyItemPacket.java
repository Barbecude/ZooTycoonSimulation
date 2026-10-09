package com.example.simulation;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

/**
 * Client → Server packet: buy items from the "shop" catalog into a shelf.
 * The zoo balance is charged per item. Sends back a balance sync after purchase.
 */
public class ShelfBuyItemPacket {
    private final BlockPos pos;
    private final String itemId;
    private final int count;

    public ShelfBuyItemPacket(BlockPos pos, String itemId, int count) {
        this.pos = pos;
        this.itemId = itemId;
        this.count = count;
    }

    public ShelfBuyItemPacket(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.itemId = buf.readUtf();
        this.count = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeUtf(itemId);
        buf.writeInt(count);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            net.minecraft.server.level.ServerPlayer player = ctx.getSender();
            if (player == null) return;
            ServerLevel level = player.serverLevel();

            // Validate shelf
            if (!(level.getBlockEntity(pos) instanceof ShelfBlockEntity shelf)) return;

            // Resolve item
            ResourceLocation rl = ResourceLocation.tryParse(itemId);
            if (rl == null) return;
            Item item = ForgeRegistries.ITEMS.getValue(rl);
            if (item == null || item == net.minecraft.world.item.Items.AIR) return;

            int pricePerItem = getPriceForItem(itemId);
            int totalCost = pricePerItem * count;

            ZooData data = ZooData.get(level);
            if (data.getBalance() < totalCost) {
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "§cSaldo tidak cukup! Butuh Rp " + String.format("%,d", totalCost)));
                return;
            }

            // Insert items into shelf slots
            int remaining = count;
            for (int i = 0; i < 9 && remaining > 0; i++) {
                ItemStack existing = shelf.getItem(i);
                if (existing.isEmpty()) {
                    int maxStack = new ItemStack(item).getMaxStackSize();
                    int toPlace = Math.min(remaining, maxStack);
                    shelf.setItem(i, new ItemStack(item, toPlace));
                    remaining -= toPlace;
                } else if (existing.getItem() == item && existing.getCount() < existing.getMaxStackSize()) {
                    int canAdd = existing.getMaxStackSize() - existing.getCount();
                    int adding = Math.min(canAdd, remaining);
                    existing.grow(adding);
                    remaining -= adding;
                }
            }

            int actualBought = count - remaining;
            if (actualBought <= 0) {
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§cShelf penuh!"));
                return;
            }

            int actualCost = pricePerItem * actualBought;
            data.addBalance(-actualCost);
            String shortName = itemId.contains(":") ? itemId.split(":")[1] : itemId;
            data.logTransaction("Pengeluaran",
                    "Beli " + actualBought + "x " + shortName + " ke shelf",
                    -actualCost);

            // Sync balance to all players
            SyncBalancePacket syncPacket = new SyncBalancePacket(
                    data.getBalance(), data.getTaggedAnimals(),
                    data.getAnimalCount(), data.getStaffCount(),
                    data.getVisitorCount(), data.getRating());
            PacketHandler.INSTANCE.send(
                    net.minecraftforge.network.PacketDistributor.ALL.noArg(), syncPacket);

            // Sync transaction log
            SyncTransactionLogPacket logPacket = new SyncTransactionLogPacket(data.getTransactionLog());
            PacketHandler.INSTANCE.send(
                    net.minecraftforge.network.PacketDistributor.ALL.noArg(), logPacket);

            shelf.setChanged();
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§aBerhasil membeli " + actualBought + "x " + shortName
                            + " ke shelf (Rp " + String.format("%,d", actualCost) + ")"));
        });
        return true;
    }

    /** Default cost-per-item for items in the shelf shop catalog. */
    public static int getPriceForItem(String id) {
        return switch (id) {
            case "minecraft:cooked_beef"    -> 8_000;
            case "minecraft:bread"          -> 5_000;
            case "minecraft:apple"          -> 3_000;
            case "minecraft:cooked_chicken" -> 7_000;
            case "minecraft:honey_bottle"   -> 5_000;
            case "minecraft:potion"         -> 3_000;
            case "minecraft:milk_bucket"    -> 6_000;
            default                         -> 1_000;
        };
    }
}

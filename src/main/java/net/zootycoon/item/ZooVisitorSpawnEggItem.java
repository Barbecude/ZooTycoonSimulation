package net.zootycoon.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.zootycoon.entity.ZooVisitorEntity;
import net.zootycoon.visitor.ZooVisitorManager;

public class ZooVisitorSpawnEggItem extends Item {

    public ZooVisitorSpawnEggItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (level instanceof ServerLevel sLevel) {
            BlockPos clickedPos = context.getClickedPos();
            Direction clickedFace = context.getClickedFace();
            BlockPos spawnPos = clickedPos.relative(clickedFace);
            Player player = context.getPlayer();

            Vec3 spawnVec = new Vec3(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
            BlockPos gatePos = player != null && player.isShiftKeyDown() ? clickedPos : spawnPos;

            ZooVisitorEntity visitor = ZooVisitorManager.spawnVisitor(sLevel, spawnVec, gatePos);
            if (visitor != null && player != null) {
                if (player.isShiftKeyDown()) {
                    player.displayClientMessage(
                            Component.literal("Pengunjung disummon dengan Gate di " + gatePos.toShortString())
                                    .withStyle(ChatFormatting.GREEN),
                            true
                    );
                } else {
                    player.displayClientMessage(
                            Component.literal("Pengunjung disummon! (Shift+Klik untuk set pintu masuk/gate)")
                                    .withStyle(ChatFormatting.AQUA),
                            true
                    );
                }
                if (!player.getAbilities().instabuild) {
                    context.getItemInHand().shrink(1);
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (!level.isClientSide() && level instanceof ServerLevel sLevel) {
            Vec3 spawnVec = player.position();
            ZooVisitorManager.spawnVisitor(sLevel, spawnVec, BlockPos.containing(spawnVec));
            if (!player.getAbilities().instabuild) {
                itemStack.shrink(1);
            }
            player.displayClientMessage(
                    Component.literal("Pengunjung disummon di posisi Anda!").withStyle(ChatFormatting.AQUA),
                    true
            );
        }
        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    }
}

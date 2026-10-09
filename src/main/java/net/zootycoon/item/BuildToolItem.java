package net.zootycoon.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BuildToolItem extends Item {

    // Client-side state hook
    public interface ClientScreenOpener {
        void openMainScreen();
        void handleBlockClick(UseOnContext context);
        void cancelPlacement();
        boolean isPlacingSchematic();
    }

    public static ClientScreenOpener clientHook = null;

    public BuildToolItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide() && clientHook != null) {
            if (player.isShiftKeyDown() && clientHook.isPlacingSchematic()) {
                clientHook.cancelPlacement();
                player.displayClientMessage(
                        Component.literal("Preview penempatan dibatalkan.").withStyle(ChatFormatting.YELLOW),
                        true
                );
                return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
            }

            clientHook.openMainScreen();
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();

        if (level.isClientSide() && clientHook != null && player != null) {
            if (clientHook.isPlacingSchematic()) {
                clientHook.handleBlockClick(context);
                return InteractionResult.SUCCESS;
            }
        }

        return super.useOn(context);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag isAdvanced) {
        tooltip.add(Component.translatable("item.zootycoon.build_tool.tooltip").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("item.zootycoon.build_tool.tooltip_preview").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, level, tooltip, isAdvanced);
    }
}


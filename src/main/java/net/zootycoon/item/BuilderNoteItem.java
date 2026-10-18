package net.zootycoon.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

import java.util.List;

public class BuilderNoteItem extends Item {

    public BuilderNoteItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            net.zootycoon.client.ui.BuilderNoteScreen.open(stack, hand);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        if (cd != null) {
            var tag = cd.copyTag();
            if (tag.contains("SchematicName")) {
                tooltip.add(Component.literal("Proyek: " + tag.getString("SchematicName")).withStyle(ChatFormatting.YELLOW));
            }
            if (tag.contains("BuilderName")) {
                tooltip.add(Component.literal("Kuli: " + tag.getString("BuilderName")).withStyle(ChatFormatting.GRAY));
            }
        }
        tooltip.add(Component.translatable("item.zootycoon.builder_note.desc").withStyle(ChatFormatting.DARK_GRAY));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}


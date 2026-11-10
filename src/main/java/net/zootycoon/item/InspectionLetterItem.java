package net.zootycoon.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.zootycoon.license.LicenseTier;
import net.zootycoon.util.ZooTranslations;

import java.util.List;

public class InspectionLetterItem extends Item {

    public InspectionLetterItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    public static ItemStack createLetterStack(String zooName, int tierLevel, boolean passed, List<String> inspectedSpecies, List<String> notes) {
        ItemStack stack = new ItemStack(ZooItems.INSPECTION_LETTER);
        CompoundTag tag = new CompoundTag();
        tag.putString("ZooName", zooName != null ? zooName : "Player's Zoo");
        tag.putInt("TierLevel", tierLevel);
        tag.putBoolean("Passed", passed);

        ListTag speciesList = new ListTag();
        if (inspectedSpecies != null) {
            for (String s : inspectedSpecies) {
                speciesList.add(StringTag.valueOf(s));
            }
        }
        tag.put("InspectedSpecies", speciesList);

        ListTag notesList = new ListTag();
        if (notes != null) {
            for (String n : notes) {
                notesList.add(StringTag.valueOf(n));
            }
        }
        tag.put("ImprovementNotes", notesList);

        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return stack;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            net.zootycoon.client.ui.InspectionLetterScreen.openFromItem(stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        if (cd != null) {
            CompoundTag tag = cd.copyTag();
            String zooName = tag.getString("ZooName");
            int tierLevel = tag.contains("TierLevel") ? tag.getInt("TierLevel") : 1;
            boolean passed = tag.getBoolean("Passed");
            LicenseTier tier = LicenseTier.byLevel(tierLevel);

            if (!zooName.isEmpty()) {
                tooltip.add(Component.literal(zooName).withStyle(ChatFormatting.GOLD));
            }
            tooltip.add(Component.literal(tier.getDisplayName()).withStyle(ChatFormatting.AQUA));
            String statusKey = passed ? "zootycoon.license.status.passed" : "zootycoon.license.status.failed";
            tooltip.add(Component.literal(ZooTranslations.get(statusKey))
                    .withStyle(passed ? ChatFormatting.GREEN : ChatFormatting.RED));
        }
        tooltip.add(Component.translatable("item.zootycoon.inspection_letter.desc").withStyle(ChatFormatting.DARK_GRAY));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}

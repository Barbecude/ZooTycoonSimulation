package net.zootycoon.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.zootycoon.entity.LicenseBannerEntity;
import net.zootycoon.license.LicenseTier;
import net.zootycoon.util.ZooTranslations;

import java.util.List;

public class LicenseBannerItem extends Item {

    private final int tierLevel;

    public LicenseBannerItem(int tierLevel, Properties properties) {
        super(properties.stacksTo(1));
        this.tierLevel = Math.clamp(tierLevel, 1, 5);
    }

    public int getTierLevel() {
        return tierLevel;
    }

    public static Item getItemForTier(int tierLevel) {
        return switch (Math.clamp(tierLevel, 1, 5)) {
            case 1 -> ZooItems.LICENSE_BANNER_TIER_1;
            case 2 -> ZooItems.LICENSE_BANNER_TIER_2;
            case 3 -> ZooItems.LICENSE_BANNER_TIER_3;
            case 4 -> ZooItems.LICENSE_BANNER_TIER_4;
            default -> ZooItems.LICENSE_BANNER_TIER_5;
        };
    }

    public static ItemStack createStackForTier(int tierLevel, String zooName) {
        Item item = getItemForTier(tierLevel);
        ItemStack stack = new ItemStack(item);
        CompoundTag tag = new CompoundTag();
        tag.putInt("TierLevel", Math.clamp(tierLevel, 1, 5));
        tag.putString("ZooName", (zooName != null && !zooName.isEmpty()) ? zooName : "Player's Zoo");
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return stack;
    }

    public static ItemStack createAnimalInfoBannerStack(int tierLevel, String speciesKey, String zooName) {
        Item item = getItemForTier(tierLevel);
        ItemStack stack = new ItemStack(item);
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("AnimalInfoBanner", true);
        tag.putInt("TierLevel", Math.clamp(tierLevel, 1, 5));
        tag.putString("SpeciesKey", speciesKey != null ? speciesKey : "");
        tag.putString("ZooName", (zooName != null && !zooName.isEmpty()) ? zooName : "Player's Zoo");
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return stack;
    }

    @Override
    public Component getName(ItemStack stack) {
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        if (cd != null) {
            CompoundTag tag = cd.copyTag();
            if (tag.contains("SpeciesKey") && !tag.getString("SpeciesKey").isEmpty()) {
                String spKey = tag.getString("SpeciesKey");
                LicenseTier.SpeciesEntry entry = LicenseTier.findSpeciesEntry(spKey);
                String spName = entry != null ? entry.defaultName() : spKey;
                return Component.literal(ZooTranslations.get("item.zootycoon.animal_info_banner.prefix") + " - " + spName);
            }
        }
        LicenseTier tier = LicenseTier.byLevel(this.tierLevel);
        return Component.literal(ZooTranslations.get("item.zootycoon.license_banner.prefix") + " - " + tier.getDisplayName());
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockPos clickedPos = context.getClickedPos();
        Direction direction = context.getClickedFace();
        BlockPos placePos = clickedPos.relative(direction);
        Player player = context.getPlayer();
        ItemStack itemStack = context.getItemInHand();

        if (player != null && !this.mayPlace(player, direction, itemStack, placePos)) {
            return InteractionResult.FAIL;
        }

        Level level = context.getLevel();
        String zooName = player != null ? player.getName().getString() + "'s Zoo" : "Player's Zoo";
        String speciesKey = "";
        CustomData cd = itemStack.get(DataComponents.CUSTOM_DATA);
        if (cd != null) {
            CompoundTag tag = cd.copyTag();
            if (tag.contains("ZooName") && !tag.getString("ZooName").isEmpty()) {
                zooName = tag.getString("ZooName");
            }
            if (tag.contains("SpeciesKey")) {
                speciesKey = tag.getString("SpeciesKey");
            }
        }

        LicenseBannerEntity banner = new LicenseBannerEntity(level, placePos, direction, this.tierLevel, zooName, speciesKey);
        if (banner.survives()) {
            if (!level.isClientSide) {
                banner.playPlacementSound();
                level.gameEvent(player, GameEvent.ENTITY_PLACE, banner.position());
                level.addFreshEntity(banner);
                if (player instanceof net.minecraft.server.level.ServerPlayer sp) {
                    net.zootycoon.advancement.ZooAdvancements.grant(sp, net.zootycoon.advancement.ZooAdvancements.PLACE_BANNER);
                }
            }
            itemStack.shrink(1);
            return InteractionResult.sidedSuccess(level.isClientSide);
        } else {
            return InteractionResult.CONSUME;
        }
    }

    protected boolean mayPlace(Player player, Direction direction, ItemStack stack, BlockPos pos) {
        return direction.getAxis().isHorizontal() && player.mayUseItemAt(pos, direction, stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        LicenseTier tier = LicenseTier.byLevel(this.tierLevel);
        String zooName = "";
        String speciesKey = "";
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        if (cd != null) {
            CompoundTag tag = cd.copyTag();
            zooName = tag.getString("ZooName");
            if (tag.contains("SpeciesKey")) {
                speciesKey = tag.getString("SpeciesKey");
            }
        }
        if (!speciesKey.isEmpty()) {
            LicenseTier.SpeciesEntry entry = LicenseTier.findSpeciesEntry(speciesKey);
            String spName = entry != null ? entry.defaultName() : speciesKey;
            tooltip.add(Component.literal(spName).withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.translatable("zootycoon.species.info_banner.subtitle", tier.getTierLevel()).withStyle(ChatFormatting.AQUA));
            String funFact = ZooTranslations.get("zootycoon.species." + speciesKey + ".fun_fact");
            if (funFact != null && !funFact.isEmpty()) {
                tooltip.add(Component.literal(funFact).withStyle(ChatFormatting.GRAY));
            }
            super.appendHoverText(stack, context, tooltip, flag);
            return;
        }
        if (!zooName.isEmpty()) {
            tooltip.add(Component.literal(zooName).withStyle(ChatFormatting.GOLD));
        }
        tooltip.add(Component.literal("Tier " + tier.getTierLevel() + ": " + tier.getDisplayName()).withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("item.zootycoon.license_banner.desc").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}

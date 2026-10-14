package net.zootycoon.item;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.zootycoon.animal.ZooAnimalHungerManager;
import net.zootycoon.compat.SereneSeasonsCompat;
import net.zootycoon.network.ZooNetworkPayloads;
import net.zootycoon.util.ZooTranslations;

import java.util.List;

public class MagnifyingGlassItem extends Item {

    public MagnifyingGlassItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!player.level().isClientSide() && player instanceof ServerPlayer sp) {
            if (ZooAnimalHungerManager.isZooAnimalCandidate(target)) {
                ZooAnimalHungerManager.registerAnimalFromSpyglass(sp, target);
                var env = SereneSeasonsCompat.evaluateEnvironment(target);

                sp.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.8f, 1.4f);
                sp.serverLevel().sendParticles(ParticleTypes.HAPPY_VILLAGER, target.getX(), target.getY() + target.getBbHeight() + 0.3, target.getZ(), 5, 0.2, 0.2, 0.2, 0.02);

                ZooNetworkPayloads.sendToast(sp, "item.zootycoon.magnifying_glass",
                        "zootycoon.animal.scan_toast", 1, target.getDisplayName().getString(), env.tempCategory(), env.seasonName());
                return InteractionResult.SUCCESS;
            }
        }
        return super.interactLivingEntity(stack, player, target, hand);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("item.zootycoon.magnifying_glass.desc").withStyle(net.minecraft.ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("item.zootycoon.magnifying_glass.desc2").withStyle(net.minecraft.ChatFormatting.AQUA));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}


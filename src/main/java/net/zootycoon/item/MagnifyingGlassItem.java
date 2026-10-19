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
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public net.minecraft.world.item.UseAnim getUseAnimation(ItemStack stack) {
        return net.minecraft.world.item.UseAnim.SPYGLASS;
    }

    @Override
    public net.minecraft.world.InteractionResultHolder<ItemStack> use(net.minecraft.world.level.Level level, Player player, InteractionHand hand) {
        player.playSound(SoundEvents.SPYGLASS_USE, 1.0F, 1.0F);
        player.startUsingItem(hand);
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            performRaycastScan(sp);
        }
        return net.minecraft.world.item.ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public void releaseUsing(ItemStack stack, net.minecraft.world.level.Level level, LivingEntity entity, int timeLeft) {
        entity.playSound(SoundEvents.SPYGLASS_STOP_USING, 1.0F, 1.0F);
        super.releaseUsing(stack, level, entity, timeLeft);
    }

    public static void performRaycastScan(ServerPlayer player) {
        net.minecraft.world.phys.Vec3 eyePos = player.getEyePosition();
        net.minecraft.world.phys.Vec3 lookVec = player.getViewVector(1.0f);
        net.minecraft.world.phys.Vec3 reachVec = eyePos.add(lookVec.scale(48.0));
        var box = player.getBoundingBox().expandTowards(lookVec.scale(48.0)).inflate(3.0);

        var hit = net.minecraft.world.entity.projectile.ProjectileUtil.getEntityHitResult(
                player, eyePos, reachVec, box,
                e -> e instanceof LivingEntity living && ZooAnimalHungerManager.isZooAnimalCandidate(living),
                48.0 * 48.0
        );

        if (hit != null && hit.getEntity() instanceof LivingEntity target) {
            scanAndRegister(player, target);
        }
    }

    private static void scanAndRegister(ServerPlayer sp, LivingEntity target) {
        boolean newlyRegistered = ZooAnimalHungerManager.registerAnimalFromSpyglass(sp, target, true);
        var env = SereneSeasonsCompat.evaluateEnvironment(target);

        sp.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.8f, 1.4f);
        sp.serverLevel().sendParticles(ParticleTypes.HAPPY_VILLAGER, target.getX(), target.getY() + target.getBbHeight() + 0.3, target.getZ(), 8, 0.2, 0.2, 0.2, 0.02);

        // Always send animal sync to client so the hologram has fresh data immediately
        ZooNetworkPayloads.sendAnimalSync(sp);

        if (!newlyRegistered) {
            String name = target.hasCustomName() ? target.getCustomName().getString() : target.getType().getDescription().getString();
            ZooNetworkPayloads.sendToast(sp, "item.zootycoon.magnifying_glass",
                    "zootycoon.animal.scan_toast", 1, name, env.tempCategory(), env.seasonName());
        }
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        player.playSound(SoundEvents.SPYGLASS_USE, 1.0F, 1.0F);
        player.startUsingItem(hand);
        if (ZooAnimalHungerManager.isZooAnimalCandidate(target)) {
            if (player.level().isClientSide()) {
                net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(new net.zootycoon.network.ZooNetworkPayloads.ScanAnimalPayload(target.getId()));
                target.addTag("zoo_registered");
                net.zootycoon.client.inspect.InspectionTargetTracker.markRegisteredLocally(target.getId());
            } else if (player instanceof ServerPlayer sp) {
                scanAndRegister(sp, target);
            }
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("item.zootycoon.magnifying_glass.desc").withStyle(net.minecraft.ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("item.zootycoon.magnifying_glass.desc2").withStyle(net.minecraft.ChatFormatting.AQUA));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}


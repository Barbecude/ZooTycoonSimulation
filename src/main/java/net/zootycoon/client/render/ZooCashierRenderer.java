package net.zootycoon.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.zootycoon.ZooTycoonMod;
import net.zootycoon.client.model.ZooVisitorModel;
import net.zootycoon.entity.ZooCashierEntity;

public class ZooCashierRenderer extends HumanoidMobRenderer<ZooCashierEntity, HumanoidModel<ZooCashierEntity>> {
    private static final ResourceLocation TEXTURE_TICKET =
            ZooTycoonMod.id("textures/entity/staff/cashier_ticket.png");
    private static final ResourceLocation TEXTURE_BURGER =
            ZooTycoonMod.id("textures/entity/staff/cashier_burger.png");
    private static final ResourceLocation TEXTURE_DRINKS =
            ZooTycoonMod.id("textures/entity/staff/cashier_drinks.png");
    private static final ResourceLocation TEXTURE_ICE_CREAM =
            ZooTycoonMod.id("textures/entity/staff/cashier_ice_cream.png");

    public ZooCashierRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ZooVisitorModel.LAYER_LOCATION)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(ZooCashierEntity entity) {
        String role = entity.getCashierRole();
        if ("TICKET".equalsIgnoreCase(role)) {
            return TEXTURE_TICKET;
        } else if ("DRINK".equalsIgnoreCase(role)) {
            return TEXTURE_DRINKS;
        } else if ("ICE_CREAM".equalsIgnoreCase(role)) {
            return TEXTURE_ICE_CREAM;
        }
        return TEXTURE_BURGER;
    }

    @Override
    protected boolean shouldShowName(ZooCashierEntity entity) {
        return false;
    }

    @Override
    protected void renderNameTag(ZooCashierEntity entity, Component displayName, PoseStack poseStack,
                                 MultiBufferSource bufferSource, int packedLight, float partialTick) {
        // Intentionally suppressed: no nametag renders above cashier (including spectator/Replay Mod)
    }
}


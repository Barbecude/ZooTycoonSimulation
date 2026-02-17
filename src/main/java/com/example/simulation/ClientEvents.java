package com.example.simulation;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientEvents {

    // ========== Both sides: Entity attributes ==========
    @Mod.EventBusSubscriber(modid = IndoZooTycoon.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModBusEvents {
        @SubscribeEvent
        public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
            event.put(IndoZooTycoon.STAFF_ENTITY.get(), StaffEntity.createAttributes().build());
            event.put(IndoZooTycoon.VISITOR_ENTITY.get(), VisitorEntity.createAttributes().build());
        }
    }

    // ========== Client only: Screen + Renderers ==========
    @Mod.EventBusSubscriber(modid = IndoZooTycoon.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModBusEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                MenuScreens.register(IndoZooTycoon.ZOO_COMPUTER_MENU.get(), ZooComputerScreen::new);
                net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(IndoZooTycoon.TRASH_BLOCK.get(),
                        net.minecraft.client.renderer.RenderType.cutout());
            });
        }

        @SubscribeEvent
        public static void onRegisterOverlays(net.minecraftforge.client.event.RegisterGuiOverlaysEvent event) {
            event.registerAboveAll("zoo_hud", ZooOverlay.HUD_OVERLAY);
        }

        @SubscribeEvent
        public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
            // Staff renderer with Custom Animation Model
            event.registerEntityRenderer(IndoZooTycoon.STAFF_ENTITY.get(),
                    ctx -> new HumanoidMobRenderer<StaffEntity, PlayerModel<StaffEntity>>(
                            ctx, new StaffModel(ctx.bakeLayer(ModelLayers.PLAYER)), 0.5f) {
                        @Override
                        public ResourceLocation getTextureLocation(StaffEntity entity) {
                            // 0 = Janitor, 1 = Zookeeper
                            String role = entity.getRole() == 1 ? "zookeeper.png" : "janitor.png";
                            return new ResourceLocation(IndoZooTycoon.MODID, "textures/entity/staff/" + role);
                        }
                    });

            // Visitor renderer - Dynamic Texture & Player Model
            event.registerEntityRenderer(IndoZooTycoon.VISITOR_ENTITY.get(),
                    ctx -> {
                        var renderer = new HumanoidMobRenderer<VisitorEntity, HumanoidModel<VisitorEntity>>(
                                ctx, new HumanoidModel<>(ctx.bakeLayer(ModelLayers.PLAYER)), 0.5f) {
                            @Override
                            public ResourceLocation getTextureLocation(VisitorEntity entity) {
                                // Reverted to use custom textures
                                return new ResourceLocation(IndoZooTycoon.MODID,
                                        "textures/entity/visitor/visitor_" + entity.getVariant() + ".png");
                            }

                            @Override
                            protected void scale(VisitorEntity entity, com.mojang.blaze3d.vertex.PoseStack poseStack,
                                    float partialTickTime) {
                                if (entity.isChildVisitor()) {
                                    poseStack.scale(0.6F, 0.6F, 0.6F); // Force visual resize to 60%
                                } else {
                                    super.scale(entity, poseStack, partialTickTime);
                                }
                            }
                        };
                        renderer.addLayer(new VisitorMoodLayer(renderer));
                        return renderer;
                    });
        }
    }

    // Custom Model for Staff Animation
    public static class StaffModel extends PlayerModel<StaffEntity> {
        public StaffModel(ModelPart root) {
            super(root, false);
        }

        @Override
        public void setupAnim(StaffEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                float netHeadYaw, float headPitch) {
            super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

            int state = entity.getAnimState();
            if (state == 1) { // Sweeping (Janitor)
                // Gerakan menyapu (lengan kanan maju mundur)
                this.rightArm.xRot = -1.0F + (float) Math.sin(ageInTicks * 0.2F) * 0.5F;
                this.rightArm.zRot = (float) Math.cos(ageInTicks * 0.2F) * 0.5F;
                this.leftArm.xRot = -0.5F; // Pegang gagang sapu di bawah
            } else if (state == 2) { // Greeting (Janitor)
                // Melambai
                this.rightArm.xRot = -2.8F; // Angkat tangan ke atas
                this.rightArm.zRot = 0.2F + (float) Math.sin(ageInTicks * 0.4F) * 0.3F; // Wave
            } else if (state == 3) { // Explaining (Zookeeper)
                // Gerakan tangan menjelaskan
                this.rightArm.xRot = -0.5F + (float) Math.sin(ageInTicks * 0.3F) * 0.2F;
                this.leftArm.xRot = -0.5F + (float) Math.cos(ageInTicks * 0.25F) * 0.2F;
            }
        }
    }

    // ========== Client Forge Events (Input) ==========
    @Mod.EventBusSubscriber(modid = IndoZooTycoon.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ClientForgeEvents {
        @SubscribeEvent
        public static void onRightClickItem(net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickItem event) {
            // Cek jika Client Side dan Item yang dipegang adalah STICK
            if (event.getLevel().isClientSide && event.getItemStack().getItem() == net.minecraft.world.item.Items.STICK) {
                // Buka Screen Tutorial
                net.minecraft.client.Minecraft.getInstance().setScreen(new TutorialScreen());
            }
        }
    }
}

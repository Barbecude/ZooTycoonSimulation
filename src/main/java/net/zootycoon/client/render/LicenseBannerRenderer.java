package net.zootycoon.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.zootycoon.ZooTycoonMod;
import net.zootycoon.client.inspect.HologramBarRenderer;
import net.zootycoon.entity.LicenseBannerEntity;
import net.zootycoon.license.LicenseTier;
import net.zootycoon.util.ZooTranslations;
import org.joml.Matrix4f;

public class LicenseBannerRenderer extends EntityRenderer<LicenseBannerEntity> {

    public LicenseBannerRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    public static ResourceLocation getCustomBannerTexture(int tierLevel) {
        return ZooTycoonMod.id("textures/entity/license_banner/tier_" + Math.clamp(tierLevel, 1, 5) + ".png");
    }

    @Override
    public ResourceLocation getTextureLocation(LicenseBannerEntity entity) {
        return getCustomBannerTexture(entity.getTierLevel());
    }

    @Override
    public void render(LicenseBannerEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();

        Direction dir = entity.getDirection();
        float yaw = switch (dir) {
            case NORTH -> 180.0F;
            case WEST -> 90.0F;
            case EAST -> -90.0F;
            default -> 0.0F; // SOUTH
        };

        // HangingEntity position is centered on its bounding box (3.0 blocks wide x 2.0 blocks tall)
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw));

        int tierLevel = entity.getTierLevel();
        ResourceLocation customTex = getCustomBannerTexture(tierLevel);
        boolean hasCustomAsset = Minecraft.getInstance().getResourceManager().getResource(customTex).isPresent();

        if (hasCustomAsset) {
            renderCustomTextureBanner(poseStack, bufferSource, customTex, packedLight);
        } else {
            renderCertificatePlaceholder(entity, tierLevel, poseStack, bufferSource, packedLight);
        }

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private void renderCustomTextureBanner(PoseStack poseStack, MultiBufferSource bufferSource, ResourceLocation texture, int light) {
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        VertexConsumer vc = bufferSource.getBuffer(RenderType.entitySolid(texture));
        float halfW = 1.5F;
        float halfH = 1.0F;
        float z = 0.01F;

        vc.addVertex(matrix, -halfW, -halfH, z).setColor(255, 255, 255, 255).setUv(0.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
        vc.addVertex(matrix, halfW, -halfH, z).setColor(255, 255, 255, 255).setUv(1.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
        vc.addVertex(matrix, halfW, halfH, z).setColor(255, 255, 255, 255).setUv(1.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
        vc.addVertex(matrix, -halfW, halfH, z).setColor(255, 255, 255, 255).setUv(0.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
    }

    private void renderCertificatePlaceholder(LicenseBannerEntity entity, int tierLevel, PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        LicenseTier tier = LicenseTier.byLevel(tierLevel);
        int accentColor = tier.getAccentColor();

        // Scale to UI pixel space: 3.0 blocks = 240 units, 2.0 blocks = 160 units -> 1 unit = 1/80 block (0.0125f)
        poseStack.scale(0.0125F, -0.0125F, 0.0125F);

        float halfW = 120.0F;
        float halfH = 80.0F;

        Matrix4f mat = poseStack.last().pose();
        VertexConsumer guiVc = bufferSource.getBuffer(RenderType.gui());

        // 1. Outer Dark Wood / Plaque Frame (3x2 full span: -120..120, -80..80)
        HologramBarRenderer.drawRect(guiVc, mat, -halfW, -halfH, halfW, halfH, 0.0F, 0xFF1A1410, light);

        // 2. Metallic Gold / Tier Accent Trim Border
        HologramBarRenderer.drawRect(guiVc, mat, -halfW + 4, -halfH + 4, halfW - 4, halfH - 4, -0.2F, accentColor, light);

        // 3. Inner Certificate Parchment / Slate Background
        HologramBarRenderer.drawRect(guiVc, mat, -halfW + 7, -halfH + 7, halfW - 7, halfH - 7, -0.4F, 0xFF0E131D, light);

        // 4. Inner Subtle Gold Line & Header Ribbon
        HologramBarRenderer.drawRect(guiVc, mat, -halfW + 12, -halfH + 12, halfW - 12, -halfH + 30, -0.6F, 0xFF192234, light);
        HologramBarRenderer.drawRect(guiVc, mat, -halfW + 12, -halfH + 30, halfW - 12, -halfH + 31, -0.6F, accentColor, light);
        HologramBarRenderer.drawRect(guiVc, mat, -halfW + 16, halfH - 28, halfW - 16, halfH - 27, -0.6F, 0xFF2B3548, light);

        Font font = Minecraft.getInstance().font;

        poseStack.pushPose();
        poseStack.translate(0.0F, 0.0F, 1.0F);
        Matrix4f textMat = poseStack.last().pose();

        // Line 1: Official Header
        String header = ZooTranslations.get("zootycoon.license.banner.header");
        drawCenteredBatch(font, header, 0, -64, 0xFFFFD700, textMat, bufferSource, light);

        // Line 2: License Tier Name (Larger Scale)
        poseStack.pushPose();
        poseStack.translate(0.0F, -32.0F, 0.1F);
        poseStack.scale(1.45F, 1.45F, 1.0F);
        String tierTitle = tier.getDisplayName().toUpperCase();
        drawCenteredBatch(font, tierTitle, 0, 0, accentColor, poseStack.last().pose(), bufferSource, light);
        poseStack.popPose();

        // Line 3: Tier Badge
        String tierSub = "TIER " + tier.getTierLevel() + " - " + ZooTranslations.get("zootycoon.license.banner.certified");
        drawCenteredBatch(font, tierSub, 0, -8, 0xFFE2E8F0, textMat, bufferSource, light);

        // Line 4: Zoo Name
        String awardedTo = ZooTranslations.get("zootycoon.license.banner.awarded_to", entity.getZooName());
        drawCenteredBatch(font, awardedTo, 0, 14, 0xFF55FF55, textMat, bufferSource, light);

        // Line 5: Species Count Info
        String speciesInfo = ZooTranslations.get("zootycoon.license.banner.species_verified", tier.getRequiredSpecies().size());
        drawCenteredBatch(font, speciesInfo, 0, 32, 0xFFA0AEC0, textMat, bufferSource, light);

        // Line 6: Official Footer Seal
        String seal = ZooTranslations.get("zootycoon.license.banner.seal");
        drawCenteredBatch(font, seal, 0, 58, 0xFFFFD700, textMat, bufferSource, light);

        poseStack.popPose();
    }

    private void drawCenteredBatch(Font font, String text, float centerX, float y, int color, Matrix4f matrix, MultiBufferSource buffer, int light) {
        if (text == null || text.isEmpty()) return;
        float x = centerX - (font.width(text) / 2.0F);
        font.drawInBatch(text, x, y, color, false, matrix, buffer, Font.DisplayMode.POLYGON_OFFSET, 0, light);
    }
}

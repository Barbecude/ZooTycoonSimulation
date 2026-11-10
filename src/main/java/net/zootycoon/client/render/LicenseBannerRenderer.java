package net.zootycoon.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.zootycoon.ZooTycoonMod;
import net.zootycoon.entity.LicenseBannerEntity;
import net.zootycoon.license.LicenseTier;
import net.zootycoon.util.ZooTranslations;
import org.joml.Matrix4f;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LicenseBannerRenderer extends EntityRenderer<LicenseBannerEntity> {

    private static final Map<String, LivingEntity> DUMMY_CACHE = new ConcurrentHashMap<>();
    private static final ResourceLocation WILDERNATURE_BOARD_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("wildernature", "textures/entity/bounty_board.png");
    private static final ResourceLocation FALLBACK_BOARD_TEXTURE =
            ZooTycoonMod.id("textures/entity/animal_info_board.png");
    private static final ResourceLocation ANIMAL_INFO_PARCHMENT_TEXTURE =
            ZooTycoonMod.id("textures/entity/license_banner/animal_info_parchment.png");

    private final ModelPart bountyBoardModel;

    public LicenseBannerRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.bountyBoardModel = createBountyBoardModelPart();
    }

    private static ModelPart createBountyBoardModelPart() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("board", CubeListBuilder.create()
                        .texOffs(36, 32).addBox(22.0F, -30.0F, -1.0F, 2.0F, 30.0F, 2.0F)
                        .texOffs(0, 0).addBox(-6.0F, -28.0F, 0.0F, 28.0F, 18.0F, 0.0F)
                        .texOffs(0, 26).addBox(-6.0F, -8.0F, 0.0F, 28.0F, 5.0F, 0.0F)
                        .texOffs(0, 22).addBox(-6.0F, -10.0F, -1.0F, 28.0F, 2.0F, 2.0F)
                        .texOffs(28, 32).addBox(-8.0F, -30.0F, -1.0F, 2.0F, 30.0F, 2.0F)
                        .texOffs(0, 18).addBox(-6.0F, -30.0F, -1.0F, 28.0F, 2.0F, 2.0F),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        return LayerDefinition.create(mesh, 64, 64).bakeRoot();
    }

    private static ResourceLocation resolveBountyBoardTexture() {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.getResourceManager().getResource(WILDERNATURE_BOARD_TEXTURE).isPresent()) {
            return WILDERNATURE_BOARD_TEXTURE;
        }
        return FALLBACK_BOARD_TEXTURE;
    }

    public static float getYawForDirection(Direction dir) {
        if (dir == null) return 0.0F;
        return switch (dir) {
            case NORTH -> 180.0F;
            case WEST -> -90.0F;
            case EAST -> 90.0F;
            default -> 0.0F; // SOUTH
        };
    }

    public static ResourceLocation getCustomBannerTexture(int tierLevel) {
        return ZooTycoonMod.id("textures/entity/license_banner/tier_" + Math.clamp(tierLevel, 1, 5) + ".png");
    }

    public static ResourceLocation getAnimalInfoParchmentTexture() {
        return ANIMAL_INFO_PARCHMENT_TEXTURE;
    }

    @Override
    public ResourceLocation getTextureLocation(LicenseBannerEntity entity) {
        return entity.isAnimalInfoBanner() ? ANIMAL_INFO_PARCHMENT_TEXTURE : getCustomBannerTexture(entity.getTierLevel());
    }

    @Override
    public void render(LicenseBannerEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();

        float yaw = getYawForDirection(entity.getDirection());
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw));

        int tierLevel = entity.getTierLevel();
        ResourceLocation customTex = getCustomBannerTexture(tierLevel);

        if (entity.isAnimalInfoBanner()) {
            renderAnimalInfoBanner(entity, partialTick, poseStack, bufferSource, packedLight);
        } else {
            // 1. Render 3x2 Textured Wooden Plaque with Parchment in world space with scene lighting
            renderPlaqueQuad(poseStack, bufferSource, customTex, packedLight, 1.5F, 1.0F, 0.0F, 0.01F, true);
            // 2. Render Inscribed Certificate Text
            renderInscribedCertificateText(entity, tierLevel, poseStack, bufferSource, packedLight);
        }

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private void renderAnimalInfoBanner(LicenseBannerEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        // 1. Standing wooden Bounty Board stand & frame (2 blocks wide, 1.875 blocks tall)
        poseStack.pushPose();
        poseStack.translate(-0.5F, 0.5625F, 0.0F);
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
        VertexConsumer boardVc = bufferSource.getBuffer(RenderType.entityCutoutNoCull(resolveBountyBoardTexture()));
        bountyBoardModel.render(poseStack, boardVc, light, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();

        // 2. Clean medal-free parchment info panel seated inside the 28x18 rectangular board slot (centered at Y = +0.25)
        renderPlaqueQuad(poseStack, bufferSource, ANIMAL_INFO_PARCHMENT_TEXTURE, light, 0.855F, 0.545F, 0.25F, 0.02F, false);

        // 3. Framed 2D Animal Photo & Fun Fact Text inside the rectangular board slot
        String speciesKey = entity.getSpeciesKey();
        render2DAnimalPhoto(speciesKey, poseStack, bufferSource, light, partialTick);
        renderAnimalInfoText(speciesKey, poseStack, bufferSource, light);
    }

    private LivingEntity getOrCreateDummy(String primaryId, EntityType<?> type) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || type == null) return null;
        return DUMMY_CACHE.computeIfAbsent(primaryId, k -> {
            try {
                Entity e = type.create(mc.level);
                if (e instanceof LivingEntity le) {
                    return le;
                }
            } catch (Throwable ignored) {}
            return null;
        });
    }

    private void render2DAnimalPhoto(String speciesKey, PoseStack poseStack, MultiBufferSource bufferSource, int light, float partialTick) {
        // Photo mat frame on left pane (centered at X = -0.45, Y = +0.25)
        renderPhotoFrame(poseStack, bufferSource, light, -0.45F, 0.25F, 0.30F, 0.37F);

        LicenseTier.SpeciesEntry entry = LicenseTier.findSpeciesEntry(speciesKey);
        if (entry == null) return;
        EntityType<?> entityType = entry.resolveEntityType();
        if (entityType == null) return;

        LivingEntity dummy = getOrCreateDummy(entry.resolvePrimaryEntityId(), entityType);
        if (dummy == null) return;

        poseStack.pushPose();
        float photoW = 0.50F;
        float photoH = 0.62F;
        float bbW = Math.max(0.5F, dummy.getBbWidth());
        float bbH = Math.max(0.5F, dummy.getBbHeight());
        float scaleXY = Math.clamp(Math.min(photoW / bbW, photoH / bbH), 0.10F, 0.36F);
        float bottomY = 0.25F - (bbH * scaleXY) * 0.5F;

        // Flat 2D portrait tucked cleanly inside the wooden Bounty Board frame
        poseStack.translate(-0.45F, bottomY, 0.036F);
        poseStack.scale(scaleXY, scaleXY, 0.015F);
        poseStack.mulPose(Axis.YP.rotationDegrees(30.0F));

        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        dispatcher.setRenderShadow(false);
        dispatcher.render(dummy, 0.0D, 0.0D, 0.0D, 0.0F, 0.0F, poseStack, bufferSource, light);
        dispatcher.setRenderShadow(true);

        poseStack.popPose();
    }

    private void renderPhotoFrame(PoseStack poseStack, MultiBufferSource bufferSource, int light,
                                  float centerX, float centerY, float halfW, float halfH) {
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        VertexConsumer vc = bufferSource.getBuffer(RenderType.entitySolid(ANIMAL_INFO_PARCHMENT_TEXTURE));

        // Outer wood/gold border sampled from parchment border corner
        float zBorder = 0.023F;
        float bu0 = 0.004F, bu1 = 0.020F, bv0 = 0.004F, bv1 = 0.020F;
        vc.addVertex(matrix, centerX - halfW, centerY - halfH, zBorder).setColor(255, 255, 255, 255).setUv(bu0, bv1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
        vc.addVertex(matrix, centerX + halfW, centerY - halfH, zBorder).setColor(255, 255, 255, 255).setUv(bu1, bv1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
        vc.addVertex(matrix, centerX + halfW, centerY + halfH, zBorder).setColor(255, 255, 255, 255).setUv(bu1, bv0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
        vc.addVertex(matrix, centerX - halfW, centerY + halfH, zBorder).setColor(255, 255, 255, 255).setUv(bu0, bv0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);

        // Inner soft backdrop sampled from parchment interior
        float zInner = 0.025F;
        float pad = 0.025F;
        float pu0 = 0.25F, pu1 = 0.35F, pv0 = 0.25F, pv1 = 0.35F;
        float iW = halfW - pad;
        float iH = halfH - pad;
        vc.addVertex(matrix, centerX - iW, centerY - iH, zInner).setColor(242, 232, 210, 255).setUv(pu0, pv1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
        vc.addVertex(matrix, centerX + iW, centerY - iH, zInner).setColor(242, 232, 210, 255).setUv(pu1, pv1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
        vc.addVertex(matrix, centerX + iW, centerY + iH, zInner).setColor(242, 232, 210, 255).setUv(pu1, pv0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
        vc.addVertex(matrix, centerX - iW, centerY + iH, zInner).setColor(242, 232, 210, 255).setUv(pu0, pv0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
    }

    private void renderAnimalInfoText(String speciesKey, PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        LicenseTier.SpeciesEntry entry = LicenseTier.findSpeciesEntry(speciesKey);
        Font font = Minecraft.getInstance().font;

        poseStack.pushPose();
        // Centered vertically on the 28x18 board window (Y = +0.25), slightly proud of the parchment
        poseStack.translate(0.0F, 0.25F, 0.026F);
        poseStack.scale(0.0074F, -0.0074F, 0.0074F);

        Matrix4f textMat = poseStack.last().pose();
        int textStartX = -10;
        int textWidth = 112;

        // 1. English Official Name (Enlarged)
        String officialName = entry != null ? entry.defaultName() : speciesKey;
        String trimmedName = font.plainSubstrByWidth(officialName, (int) (textWidth / 1.25F));
        poseStack.pushPose();
        poseStack.translate(textStartX, -52.0F, 0.0F);
        poseStack.scale(1.25F, 1.25F, 1.0F);
        font.drawInBatch(trimmedName, 0, 0, 0xFF1F1206, false, poseStack.last().pose(), bufferSource, Font.DisplayMode.POLYGON_OFFSET, 0, light);
        poseStack.popPose();

        // 2. Fun Fact Header (no license tier subtitle)
        String funFactTitle = ZooTranslations.get("zootycoon.license.banner.fun_fact_title");
        font.drawInBatch(funFactTitle, textStartX, -32, 0xFF7D1B1B, false, textMat, bufferSource, Font.DisplayMode.POLYGON_OFFSET, 0, light);

        // 3. Wrapped Educational Fun Fact Description
        String funFact = ZooTranslations.get("zootycoon.species." + speciesKey + ".fun_fact");
        var wrappedLines = font.split(Component.literal(funFact), textWidth);
        int lineY = -20;
        for (var formattedLine : wrappedLines) {
            font.drawInBatch(formattedLine, textStartX, lineY, 0xFF2E1B0E, false, textMat, bufferSource, Font.DisplayMode.POLYGON_OFFSET, 0, light);
            lineY += 9;
            if (lineY > 60) break; // Keep inside board bounds
        }

        poseStack.popPose();
    }

    private void renderPlaqueQuad(PoseStack poseStack, MultiBufferSource bufferSource, ResourceLocation texture, int light,
                                  float halfW, float halfH, float centerY, float zFront, boolean withWoodenBack) {
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        VertexConsumer vc = bufferSource.getBuffer(RenderType.entitySolid(texture));

        float yMin = centerY - halfH;
        float yMax = centerY + halfH;

        // Front Face (Facing +Z out toward viewer)
        vc.addVertex(matrix, -halfW, yMin, zFront).setColor(255, 255, 255, 255).setUv(0.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
        vc.addVertex(matrix, halfW, yMin, zFront).setColor(255, 255, 255, 255).setUv(1.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
        vc.addVertex(matrix, halfW, yMax, zFront).setColor(255, 255, 255, 255).setUv(1.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
        vc.addVertex(matrix, -halfW, yMax, zFront).setColor(255, 255, 255, 255).setUv(0.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);

        if (withWoodenBack) {
            float zBack = -0.02F;
            // Solid dark-oak wood border UV sample from corner of tier_X.png
            float u0 = 0.002F;
            float u1 = 0.015F;
            float v0 = 0.002F;
            float v1 = 0.015F;

            // Back Face (Facing -Z)
            vc.addVertex(matrix, halfW, yMin, zBack).setColor(255, 255, 255, 255).setUv(u0, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, -1.0F);
            vc.addVertex(matrix, -halfW, yMin, zBack).setColor(255, 255, 255, 255).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, -1.0F);
            vc.addVertex(matrix, -halfW, yMax, zBack).setColor(255, 255, 255, 255).setUv(u1, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, -1.0F);
            vc.addVertex(matrix, halfW, yMax, zBack).setColor(255, 255, 255, 255).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, -1.0F);

            // Top Edge (+Y)
            vc.addVertex(matrix, -halfW, yMax, zFront).setColor(255, 255, 255, 255).setUv(u0, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 1.0F, 0.0F);
            vc.addVertex(matrix, halfW, yMax, zFront).setColor(255, 255, 255, 255).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 1.0F, 0.0F);
            vc.addVertex(matrix, halfW, yMax, zBack).setColor(255, 255, 255, 255).setUv(u1, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 1.0F, 0.0F);
            vc.addVertex(matrix, -halfW, yMax, zBack).setColor(255, 255, 255, 255).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 1.0F, 0.0F);

            // Bottom Edge (-Y)
            vc.addVertex(matrix, -halfW, yMin, zBack).setColor(255, 255, 255, 255).setUv(u0, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, -1.0F, 0.0F);
            vc.addVertex(matrix, halfW, yMin, zBack).setColor(255, 255, 255, 255).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, -1.0F, 0.0F);
            vc.addVertex(matrix, halfW, yMin, zFront).setColor(255, 255, 255, 255).setUv(u1, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, -1.0F, 0.0F);
            vc.addVertex(matrix, -halfW, yMin, zFront).setColor(255, 255, 255, 255).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, -1.0F, 0.0F);

            // Left Edge (-X)
            vc.addVertex(matrix, -halfW, yMin, zBack).setColor(255, 255, 255, 255).setUv(u0, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, -1.0F, 0.0F, 0.0F);
            vc.addVertex(matrix, -halfW, yMin, zFront).setColor(255, 255, 255, 255).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, -1.0F, 0.0F, 0.0F);
            vc.addVertex(matrix, -halfW, yMax, zFront).setColor(255, 255, 255, 255).setUv(u1, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, -1.0F, 0.0F, 0.0F);
            vc.addVertex(matrix, -halfW, yMax, zBack).setColor(255, 255, 255, 255).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, -1.0F, 0.0F, 0.0F);

            // Right Edge (+X)
            vc.addVertex(matrix, halfW, yMin, zFront).setColor(255, 255, 255, 255).setUv(u0, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 1.0F, 0.0F, 0.0F);
            vc.addVertex(matrix, halfW, yMin, zBack).setColor(255, 255, 255, 255).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 1.0F, 0.0F, 0.0F);
            vc.addVertex(matrix, halfW, yMax, zBack).setColor(255, 255, 255, 255).setUv(u1, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 1.0F, 0.0F, 0.0F);
            vc.addVertex(matrix, halfW, yMax, zFront).setColor(255, 255, 255, 255).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 1.0F, 0.0F, 0.0F);
        }
    }

    private void renderInscribedCertificateText(LicenseBannerEntity entity, int tierLevel, PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        LicenseTier tier = LicenseTier.byLevel(tierLevel);
        Font font = Minecraft.getInstance().font;

        poseStack.pushPose();
        // Translate slightly in front of plaque surface and scale: 1 unit = 1/80 block (0.0125F)
        poseStack.translate(0.0F, 0.0F, 0.015F);
        poseStack.scale(0.0125F, -0.0125F, 0.0125F);

        Matrix4f textMat = poseStack.last().pose();

        // 1. License Tier Title (Enlarged, cleanly centered above the medal)
        poseStack.pushPose();
        poseStack.translate(0.0F, -32.0F, 0.001F);
        poseStack.scale(1.35F, 1.35F, 1.0F);
        String tierTitle = tier.getDisplayName().toUpperCase();
        drawCenteredOffset(font, tierTitle, 0, 0, 0xFF1F1206, poseStack.last().pose(), bufferSource, light);
        poseStack.popPose();

        // 2. Zoo Name / Awardee
        String awardedTo = ZooTranslations.get("zootycoon.license.banner.awarded_to", entity.getZooName());
        drawCenteredOffset(font, awardedTo, 0, -4, 0xFF2E1B0E, textMat, bufferSource, light);

        // 3. Species Verified Info
        String speciesInfo = ZooTranslations.get("zootycoon.license.banner.species_verified", tier.getRequiredSpecies().size());
        drawCenteredOffset(font, speciesInfo, 0, 14, 0xFF6A5442, textMat, bufferSource, light);

        poseStack.popPose();
    }

    private void drawCenteredOffset(Font font, String text, float centerX, float y, int color, Matrix4f matrix, MultiBufferSource buffer, int light) {
        if (text == null || text.isEmpty()) return;
        float x = centerX - (font.width(text) / 2.0F);
        font.drawInBatch(text, x, y, color, false, matrix, buffer, Font.DisplayMode.POLYGON_OFFSET, 0, light);
    }
}

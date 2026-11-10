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
            // 1. Render 3x2 Textured Wooden Plaque with light 3D outer rim & metallic corner studs
            renderCertificationPlaque(poseStack, bufferSource, customTex, packedLight);
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

        // 2. Wrapped Educational Fun Fact Description (red heading removed per user request)
        String funFact = ZooTranslations.get("zootycoon.species." + speciesKey + ".fun_fact");
        var wrappedLines = font.split(Component.literal(funFact), textWidth);
        int lineY = -32;
        for (var formattedLine : wrappedLines) {
            font.drawInBatch(formattedLine, textStartX, lineY, 0xFF2E1B0E, false, textMat, bufferSource, Font.DisplayMode.POLYGON_OFFSET, 0, light);
            lineY += 9;
            if (lineY > 62) break; // Keep inside board bounds
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

    private void renderCertificationPlaque(PoseStack poseStack, MultiBufferSource bufferSource, ResourceLocation texture, int light) {
        // Base 3x2 plaque body + wooden back
        renderPlaqueQuad(poseStack, bufferSource, texture, light, 1.5F, 1.0F, 0.0F, 0.01F, true);

        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        VertexConsumer vc = bufferSource.getBuffer(RenderType.entitySolid(texture));

        // 4 raised 3D wooden border strips around the perimeter (z = 0.01F -> 0.036F)
        float rimW = 0.090F;
        float frameU0 = 0.005F, frameU1 = 0.025F, frameV0 = 0.005F, frameV1 = 0.025F;
        addTexturedBox(vc, pose, matrix, light, -1.5F, 1.0F - rimW, 0.01F, 1.5F, 1.0F, 0.036F, frameU0, frameV0, frameU1, frameV1);
        addTexturedBox(vc, pose, matrix, light, -1.5F, -1.0F, 0.01F, 1.5F, -1.0F + rimW, 0.036F, frameU0, frameV0, frameU1, frameV1);
        addTexturedBox(vc, pose, matrix, light, -1.5F, -1.0F + rimW, 0.01F, -1.5F + rimW, 1.0F - rimW, 0.036F, frameU0, frameV0, frameU1, frameV1);
        addTexturedBox(vc, pose, matrix, light, 1.5F - rimW, -1.0F + rimW, 0.01F, 1.5F, 1.0F - rimW, 0.036F, frameU0, frameV0, frameU1, frameV1);

        // 4 raised 3D metallic corner studs/plates (z = 0.01F -> 0.044F), sampled from corner plate UV
        float stud = 0.145F;
        float studU0 = 0.042F, studU1 = 0.068F, studV0 = 0.062F, studV1 = 0.102F;
        addTexturedBox(vc, pose, matrix, light, -1.5F, 1.0F - stud, 0.01F, -1.5F + stud, 1.0F, 0.044F, studU0, studV0, studU1, studV1);
        addTexturedBox(vc, pose, matrix, light, 1.5F - stud, 1.0F - stud, 0.01F, 1.5F, 1.0F, 0.044F, studU0, studV0, studU1, studV1);
        addTexturedBox(vc, pose, matrix, light, -1.5F, -1.0F, 0.01F, -1.5F + stud, -1.0F + stud, 0.044F, studU0, studV0, studU1, studV1);
        addTexturedBox(vc, pose, matrix, light, 1.5F - stud, -1.0F, 0.01F, 1.5F, -1.0F + stud, 0.044F, studU0, studV0, studU1, studV1);
    }

    private void addTexturedBox(VertexConsumer vc, PoseStack.Pose pose, Matrix4f matrix, int light,
                                float x0, float y0, float z0, float x1, float y1, float z1,
                                float u0, float v0, float u1, float v1) {
        // Front (+Z)
        vc.addVertex(matrix, x0, y0, z1).setColor(255, 255, 255, 255).setUv(u0, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
        vc.addVertex(matrix, x1, y0, z1).setColor(255, 255, 255, 255).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
        vc.addVertex(matrix, x1, y1, z1).setColor(255, 255, 255, 255).setUv(u1, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
        vc.addVertex(matrix, x0, y1, z1).setColor(255, 255, 255, 255).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
        // Top (+Y)
        vc.addVertex(matrix, x0, y1, z1).setColor(245, 245, 245, 255).setUv(u0, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 1.0F, 0.0F);
        vc.addVertex(matrix, x1, y1, z1).setColor(245, 245, 245, 255).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 1.0F, 0.0F);
        vc.addVertex(matrix, x1, y1, z0).setColor(245, 245, 245, 255).setUv(u1, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 1.0F, 0.0F);
        vc.addVertex(matrix, x0, y1, z0).setColor(245, 245, 245, 255).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 1.0F, 0.0F);
        // Bottom (-Y)
        vc.addVertex(matrix, x0, y0, z0).setColor(220, 220, 220, 255).setUv(u0, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, -1.0F, 0.0F);
        vc.addVertex(matrix, x1, y0, z0).setColor(220, 220, 220, 255).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, -1.0F, 0.0F);
        vc.addVertex(matrix, x1, y0, z1).setColor(220, 220, 220, 255).setUv(u1, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, -1.0F, 0.0F);
        vc.addVertex(matrix, x0, y0, z1).setColor(220, 220, 220, 255).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, -1.0F, 0.0F);
        // Left (-X)
        vc.addVertex(matrix, x0, y0, z0).setColor(235, 235, 235, 255).setUv(u0, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, -1.0F, 0.0F, 0.0F);
        vc.addVertex(matrix, x0, y0, z1).setColor(235, 235, 235, 255).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, -1.0F, 0.0F, 0.0F);
        vc.addVertex(matrix, x0, y1, z1).setColor(235, 235, 235, 255).setUv(u1, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, -1.0F, 0.0F, 0.0F);
        vc.addVertex(matrix, x0, y1, z0).setColor(235, 235, 235, 255).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, -1.0F, 0.0F, 0.0F);
        // Right (+X)
        vc.addVertex(matrix, x1, y0, z1).setColor(235, 235, 235, 255).setUv(u0, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 1.0F, 0.0F, 0.0F);
        vc.addVertex(matrix, x1, y0, z0).setColor(235, 235, 235, 255).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 1.0F, 0.0F, 0.0F);
        vc.addVertex(matrix, x1, y1, z0).setColor(235, 235, 235, 255).setUv(u1, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 1.0F, 0.0F, 0.0F);
        vc.addVertex(matrix, x1, y1, z1).setColor(235, 235, 235, 255).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 1.0F, 0.0F, 0.0F);
    }

    private void renderInscribedCertificateText(LicenseBannerEntity entity, int tierLevel, PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        LicenseTier tier = LicenseTier.byLevel(tierLevel);
        Font font = Minecraft.getInstance().font;

        poseStack.pushPose();
        // Translate slightly in front of plaque surface and scale: 1 unit = 1/80 block (0.0125F)
        poseStack.translate(0.0F, 0.0F, 0.015F);
        poseStack.scale(0.0125F, -0.0125F, 0.0125F);

        // 1. Official Conservation Header
        String header = ZooTranslations.get("zootycoon.license.banner.header");
        drawScaledCentered(font, header, 0.0F, -52.0F, 0.62F, 184.0F, 0xFF7A561F, poseStack, bufferSource, light);

        // 2. License Tier Title (Enlarged, cleanly centered without tier stars)
        String tierTitle = tier.getDisplayName().toUpperCase();
        drawScaledCentered(font, tierTitle, 0.0F, -38.0F, 1.30F, 196.0F, 0xFF1F1206, poseStack, bufferSource, light);

        // 3. Zoo Name / Awardee (sits cleanly below the central divider line)
        String awardedTo = ZooTranslations.get("zootycoon.license.banner.awarded_to", entity.getZooName());
        drawScaledCentered(font, awardedTo, 0.0F, -9.0F, 1.05F, 188.0F, 0xFF2E1B0E, poseStack, bufferSource, light);

        // 4. Cumulative verified species count up through this tier
        String speciesInfo = ZooTranslations.get("zootycoon.license.banner.species_verified", tier.getCumulativeSpeciesCount());
        drawScaledCentered(font, speciesInfo, 0.0F, 8.0F, 0.90F, 176.0F, 0xFF5C4332, poseStack, bufferSource, light);

        // 5. Inspector Certification & Tier Grade flanking the preserved bottom-center medal badge
        drawScaledCentered(font, "Zoo Inspector", -62.5F, 37.5F, 0.72F, 64.0F, 0xFF3A2414, poseStack, bufferSource, light);
        String certifiedLabel = ZooTranslations.get("zootycoon.license.banner.certified");
        drawScaledCentered(font, certifiedLabel, -62.5F, 50.0F, 0.56F, 66.0F, 0xFF6B5340, poseStack, bufferSource, light);

        drawScaledCentered(font, "Tier " + tierLevel, 62.5F, 37.5F, 0.72F, 64.0F, 0xFF3A2414, poseStack, bufferSource, light);
        String verifiedLabel = ZooTranslations.get("zootycoon.license.banner.verified");
        drawScaledCentered(font, verifiedLabel, 62.5F, 50.0F, 0.52F, 68.0F, 0xFF6B5340, poseStack, bufferSource, light);

        poseStack.popPose();
    }

    private void drawScaledCentered(Font font, String text, float centerX, float y, float baseScale, float maxWidth,
                                    int color, PoseStack poseStack, MultiBufferSource buffer, int light) {
        if (text == null || text.isEmpty()) return;
        int rawW = font.width(text);
        float scale = rawW > 0 ? Math.min(baseScale, maxWidth / rawW) : baseScale;
        poseStack.pushPose();
        poseStack.translate(centerX, y, 0.001F);
        poseStack.scale(scale, scale, 1.0F);
        font.drawInBatch(text, -rawW / 2.0F, 0.0F, color, false, poseStack.last().pose(), buffer, Font.DisplayMode.POLYGON_OFFSET, 0, light);
        poseStack.popPose();
    }
}

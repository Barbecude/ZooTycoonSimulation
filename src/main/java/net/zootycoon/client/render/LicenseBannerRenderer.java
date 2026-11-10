package net.zootycoon.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
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

        poseStack.mulPose(Axis.YP.rotationDegrees(yaw));

        int tierLevel = entity.getTierLevel();
        ResourceLocation customTex = getCustomBannerTexture(tierLevel);

        // 1. Render 3x2 Textured Wooden Plaque with Parchment in world space with scene lighting
        renderPlaqueQuad(poseStack, bufferSource, customTex, packedLight);

        // 2. Render content: Animal Info Banner or Inscribed Certificate
        if (entity.isAnimalInfoBanner()) {
            renderAnimalInfoBanner(entity, tierLevel, partialTick, poseStack, bufferSource, packedLight);
        } else {
            renderInscribedCertificateText(entity, tierLevel, poseStack, bufferSource, packedLight);
        }

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private void renderAnimalInfoBanner(LicenseBannerEntity entity, int tierLevel, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        String speciesKey = entity.getSpeciesKey();
        render3DAnimalEntity(speciesKey, poseStack, bufferSource, light, partialTick);
        renderAnimalInfoText(speciesKey, tierLevel, poseStack, bufferSource, light);
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

    private void render3DAnimalEntity(String speciesKey, PoseStack poseStack, MultiBufferSource bufferSource, int light, float partialTick) {
        LicenseTier.SpeciesEntry entry = LicenseTier.findSpeciesEntry(speciesKey);
        if (entry == null) return;
        EntityType<?> entityType = entry.resolveEntityType();
        if (entityType == null) return;

        LivingEntity dummy = getOrCreateDummy(entry.resolvePrimaryEntityId(), entityType);
        if (dummy == null) return;

        poseStack.pushPose();
        // Center of left pane: X = -52px * (1/64) = -0.8125F, slightly raised at Y = -0.15F, offset forward Z = 0.08F
        poseStack.translate(-0.8125F, -0.15F, 0.08F);

        // Auto-scale based on bounding box
        float bbMax = Math.max(0.6f, Math.max(dummy.getBbHeight(), dummy.getBbWidth()));
        float scale = Math.clamp(0.45F / bbMax, 0.15F, 0.55F);
        poseStack.scale(scale, scale, scale);

        // Subtle breathing/idle rotation
        long gameTime = dummy.level() != null ? dummy.level().getGameTime() : 0L;
        float rotY = 25.0F + (float) Math.sin((gameTime + partialTick) * 0.04F) * 12.0F;
        poseStack.mulPose(Axis.YP.rotationDegrees(rotY));

        // In-world lighting and shadow suppression
        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        dispatcher.setRenderShadow(false);
        dispatcher.render(dummy, 0.0D, 0.0D, 0.0D, 0.0F, partialTick, poseStack, bufferSource, light);
        dispatcher.setRenderShadow(true);

        poseStack.popPose();
    }

    private void renderAnimalInfoText(String speciesKey, int tierLevel, PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        LicenseTier.SpeciesEntry entry = LicenseTier.findSpeciesEntry(speciesKey);
        LicenseTier tier = LicenseTier.byLevel(tierLevel);
        Font font = Minecraft.getInstance().font;

        poseStack.pushPose();
        // Scale 1 unit = 1/64 block
        poseStack.translate(0.0F, 0.0F, 0.015F);
        poseStack.scale(0.015625F, -0.015625F, 0.015625F);

        Matrix4f textMat = poseStack.last().pose();
        int textStartX = -8; // -96 + 80 + 8 = -8
        int textWidth = 104; // Test requirement textPaneWidth

        // 1. English Official Name (Enlarged)
        String officialName = entry != null ? entry.defaultName() : speciesKey;
        poseStack.pushPose();
        poseStack.translate(textStartX, -48.0F, 0.0F);
        poseStack.scale(1.25F, 1.25F, 1.0F);
        font.drawInBatch(officialName, 0, 0, 0xFF1F1206, false, poseStack.last().pose(), bufferSource, Font.DisplayMode.POLYGON_OFFSET, 0, light);
        poseStack.popPose();

        // 2. Category / Tier Subtitle
        String subtitle = tier.getDisplayName().toUpperCase() + " • EXHIBIT";
        font.drawInBatch(subtitle, textStartX, -32, 0xFF5A432D, false, textMat, bufferSource, Font.DisplayMode.POLYGON_OFFSET, 0, light);

        // 3. Fun Fact Header
        String funFactTitle = ZooTranslations.get("zootycoon.license.banner.fun_fact_title");
        font.drawInBatch(funFactTitle, textStartX, -18, 0xFF7D1B1B, false, textMat, bufferSource, Font.DisplayMode.POLYGON_OFFSET, 0, light);

        // 4. Wrapped Educational Fun Fact Description
        String funFact = ZooTranslations.get("zootycoon.species." + speciesKey + ".fun_fact");
        var wrappedLines = font.split(Component.literal(funFact), textWidth);
        int lineY = -6;
        for (var formattedLine : wrappedLines) {
            font.drawInBatch(formattedLine, textStartX, lineY, 0xFF2E1B0E, false, textMat, bufferSource, Font.DisplayMode.POLYGON_OFFSET, 0, light);
            lineY += 9;
            if (lineY > 56) break; // Keep inside bounds
        }

        poseStack.popPose();
    }

    private void renderPlaqueQuad(PoseStack poseStack, MultiBufferSource bufferSource, ResourceLocation texture, int light) {
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        VertexConsumer vc = bufferSource.getBuffer(RenderType.entitySolid(texture));

        float halfW = 1.5F;
        float halfH = 1.0F;
        float z = 0.01F;

        // Front Face (Facing +Z into room)
        vc.addVertex(matrix, -halfW, -halfH, z).setColor(255, 255, 255, 255).setUv(0.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
        vc.addVertex(matrix, halfW, -halfH, z).setColor(255, 255, 255, 255).setUv(1.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
        vc.addVertex(matrix, halfW, halfH, z).setColor(255, 255, 255, 255).setUv(1.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
        vc.addVertex(matrix, -halfW, halfH, z).setColor(255, 255, 255, 255).setUv(0.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
    }

    private void renderInscribedCertificateText(LicenseBannerEntity entity, int tierLevel, PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        LicenseTier tier = LicenseTier.byLevel(tierLevel);
        Font font = Minecraft.getInstance().font;

        poseStack.pushPose();
        // Translate slightly in front of plaque surface and scale: 1 unit = 1/80 block (0.0125F)
        poseStack.translate(0.0F, 0.0F, 0.015F);
        poseStack.scale(0.0125F, -0.0125F, 0.0125F);

        Matrix4f textMat = poseStack.last().pose();

        // 1. Official Header Inscription
        String header = ZooTranslations.get("zootycoon.license.banner.header");
        drawCenteredOffset(font, header, 0, -56, 0xFF2A1708, textMat, bufferSource, light);

        // 2. License Tier Title (Enlarged)
        poseStack.pushPose();
        poseStack.translate(0.0F, -34.0F, 0.001F);
        poseStack.scale(1.35F, 1.35F, 1.0F);
        String tierTitle = tier.getDisplayName().toUpperCase();
        drawCenteredOffset(font, tierTitle, 0, 0, 0xFF1F1206, poseStack.last().pose(), bufferSource, light);
        poseStack.popPose();

        // 3. Certified Status Badge
        String tierSub = "TIER " + tier.getTierLevel() + " • " + ZooTranslations.get("zootycoon.license.banner.certified");
        drawCenteredOffset(font, tierSub, 0, -10, 0xFF5A432D, textMat, bufferSource, light);

        // 4. Zoo Name / Awardee
        String awardedTo = ZooTranslations.get("zootycoon.license.banner.awarded_to", entity.getZooName());
        drawCenteredOffset(font, awardedTo, 0, 10, 0xFF2E1B0E, textMat, bufferSource, light);

        // 5. Species Verified Info
        String speciesInfo = ZooTranslations.get("zootycoon.license.banner.species_verified", tier.getRequiredSpecies().size());
        drawCenteredOffset(font, speciesInfo, 0, 26, 0xFF6A5442, textMat, bufferSource, light);

        // 6. Seal Authority Endorsement
        String seal = ZooTranslations.get("zootycoon.license.banner.seal");
        drawCenteredOffset(font, seal, 0, 56, 0xFF7D1B1B, textMat, bufferSource, light);

        poseStack.popPose();
    }

    private void drawCenteredOffset(Font font, String text, float centerX, float y, int color, Matrix4f matrix, MultiBufferSource buffer, int light) {
        if (text == null || text.isEmpty()) return;
        float x = centerX - (font.width(text) / 2.0F);
        font.drawInBatch(text, x, y, color, false, matrix, buffer, Font.DisplayMode.POLYGON_OFFSET, 0, light);
    }
}

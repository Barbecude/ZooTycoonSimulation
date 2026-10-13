package net.zootycoon.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.Vec3;
import net.zootycoon.claim.ZooClaim;
import net.zootycoon.economy.TransactionHelper;
import net.zootycoon.item.ZooItems;
import net.zootycoon.network.ZooNetworkPayloads;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ClaimBoundaryRenderer {
    public static final List<ZooClaim> CLIENT_CLAIMS = new ArrayList<>();

    // 2-point stick claim selection state
    private static BlockPos pos1 = null;
    private static BlockPos pos2 = null;
    private static int claimOffsetX = 0;
    private static int claimOffsetZ = 0;
    private static long lastClickTime = 0L;

    public static BlockPos getPos1() { return pos1; }
    public static BlockPos getPos2() { return pos2; }
    public static int getClaimOffsetX() { return claimOffsetX; }
    public static int getClaimOffsetZ() { return claimOffsetZ; }

    public static boolean hasSelection() {
        return pos1 != null;
    }

    public static boolean isBoxReady() {
        return pos1 != null && pos2 != null;
    }

    public static void clearClaimPreview() {
        pos1 = null;
        pos2 = null;
        claimOffsetX = 0;
        claimOffsetZ = 0;
    }

    public static void shiftOffsetX(int delta) {
        claimOffsetX += delta;
        showSelectionStatus();
    }

    public static void shiftOffsetZ(int delta) {
        claimOffsetZ += delta;
        showSelectionStatus();
    }

    public static void handleLeftClick(BlockPos clickedPos) {
        long now = System.currentTimeMillis();
        if (now - lastClickTime < 250) return;
        lastClickTime = now;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        if (pos1 == null) {
            pos1 = clickedPos;
            pos2 = null;
            claimOffsetX = 0;
            claimOffsetZ = 0;
            if (player != null) {
                net.zootycoon.client.ui.ZooHudOverlay.showToast("zootycoon.toast.title.claim", "zootycoon.claim.pos1", net.zootycoon.client.ui.ZooHudOverlay.TOAST_INFO, pos1.getX(), pos1.getZ());
            }
        } else if (pos2 == null) {
            if (clickedPos.equals(pos1)) {
                if (player != null) {
                    net.zootycoon.client.ui.ZooHudOverlay.showToast("zootycoon.toast.title.claim", "zootycoon.claim.same_pos", net.zootycoon.client.ui.ZooHudOverlay.TOAST_WARNING);
                }
                return;
            }
            pos2 = clickedPos;
            showSelectionStatus();
        } else {
            // Already had pos1 and pos2 -> start fresh selection
            pos1 = clickedPos;
            pos2 = null;
            claimOffsetX = 0;
            claimOffsetZ = 0;
            if (player != null) {
                net.zootycoon.client.ui.ZooHudOverlay.showToast("zootycoon.toast.title.claim", "zootycoon.claim.pos1", net.zootycoon.client.ui.ZooHudOverlay.TOAST_INFO, pos1.getX(), pos1.getZ());
            }
        }
    }

    public static boolean handleRightClickBlock(BlockPos clickedPos) {
        long now = System.currentTimeMillis();
        if (now - lastClickTime < 250) return false;

        if (pos1 != null && pos2 == null && !clickedPos.equals(pos1)) {
            lastClickTime = now;
            pos2 = clickedPos;
            showSelectionStatus();
            return true;
        }
        return false;
    }

    public record SelectionInfo(
            int width, int length, int totalBlocks, int newBlocks,
            int cost, boolean conflictOther, boolean canAfford, int ownedEmeralds
    ) {}

    public static SelectionInfo getSelectionInfo() {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || pos1 == null || pos2 == null) return null;

        int minX = Math.min(pos1.getX(), pos2.getX()) + claimOffsetX;
        int maxX = Math.max(pos1.getX(), pos2.getX()) + claimOffsetX;
        int minZ = Math.min(pos1.getZ(), pos2.getZ()) + claimOffsetZ;
        int maxZ = Math.max(pos1.getZ(), pos2.getZ()) + claimOffsetZ;

        int width = maxX - minX + 1;
        int length = maxZ - minZ + 1;
        int totalBlocks = width * length;

        BoundingBox box = new BoundingBox(minX, -64, minZ, maxX, 320, maxZ);
        boolean conflictOther = false;
        for (ZooClaim claim : CLIENT_CLAIMS) {
            if (!claim.getOwnerUuid().equals(player.getUUID()) && claim.getBox().intersects(box)) {
                conflictOther = true;
                break;
            }
        }

        int newBlocks = 0;
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                boolean owned = false;
                for (ZooClaim claim : CLIENT_CLAIMS) {
                    if (claim.getOwnerUuid().equals(player.getUUID()) && claim.contains(x, z)) {
                        owned = true;
                        break;
                    }
                }
                if (!owned) {
                    newBlocks++;
                }
            }
        }

        int cost = (newBlocks == 0) ? 0 : Math.max(1, (int) Math.ceil(newBlocks / 4.0));
        int ownedEmeralds = TransactionHelper.countEmeralds(player);
        boolean canAfford = ownedEmeralds >= cost;

        return new SelectionInfo(width, length, totalBlocks, newBlocks, cost, conflictOther, canAfford, ownedEmeralds);
    }

    public static void showSelectionStatus() {
        // Selection info is now cleanly rendered on the top-left HUD overlay
    }

    public static void confirmActiveClaim() {
        if (pos1 != null && pos2 != null) {
            BlockPos actualPos1 = pos1.offset(claimOffsetX, 0, claimOffsetZ);
            BlockPos actualPos2 = pos2.offset(claimOffsetX, 0, claimOffsetZ);
            ClientPlayNetworking.send(new ZooNetworkPayloads.ClaimBoxPayload(actualPos1, actualPos2));
            clearClaimPreview();
        }
    }

    public static void clientTick(Minecraft mc) {
        Player player = mc.player;
        if (player == null || mc.level == null) return;

        boolean holdingTool = player.getMainHandItem().is(net.minecraft.world.item.Items.STICK)
                || player.getOffhandItem().is(net.minecraft.world.item.Items.STICK);
        if (!holdingTool) {
            clearClaimPreview();
        }
    }

    public static void render(WorldRenderContext context) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.level == null) return;

        boolean holdingBuildTool = player.getMainHandItem().is(net.minecraft.world.item.Items.STICK)
                || player.getOffhandItem().is(net.minecraft.world.item.Items.STICK);
        if (!holdingBuildTool) return;

        MultiBufferSource bufferSource = context.consumers();
        if (bufferSource == null) return;
        VertexConsumer lineConsumer = bufferSource.getBuffer(RenderType.lines());

        PoseStack poseStack = context.matrixStack();
        Camera camera = context.camera();
        Vec3 camPos = camera.getPosition();

        int worldMinY = mc.level.getMinBuildHeight();
        int worldMaxY = mc.level.getMaxBuildHeight();

        // 1. Identify active selection and smoothly merge with any touched/overlapping owned claims
        Set<UUID> mergedClaimIds = new HashSet<>();
        int selMinX = 0, selMaxX = 0, selMinZ = 0, selMaxZ = 0;
        boolean hasActiveBox = (pos1 != null && pos2 != null);
        boolean conflictOther = false;

        if (hasActiveBox) {
            selMinX = Math.min(pos1.getX(), pos2.getX()) + claimOffsetX;
            selMaxX = Math.max(pos1.getX(), pos2.getX()) + claimOffsetX + 1;
            selMinZ = Math.min(pos1.getZ(), pos2.getZ()) + claimOffsetZ;
            selMaxZ = Math.max(pos1.getZ(), pos2.getZ()) + claimOffsetZ + 1;

            BoundingBox selBox = new BoundingBox(selMinX, worldMinY, selMinZ, selMaxX - 1, worldMaxY, selMaxZ - 1);

            for (ZooClaim claim : CLIENT_CLAIMS) {
                if (!claim.getOwnerUuid().equals(player.getUUID())) {
                    if (claim.getBox().intersects(selBox)) {
                        conflictOther = true;
                    }
                } else {
                    BoundingBox cb = claim.getBox();
                    if (claim.getBox().intersects(selBox) || !(cb.maxX() < selMinX - 1 || cb.minX() > selMaxX || cb.maxZ() < selMinZ - 1 || cb.minZ() > selMaxZ)) {
                        selMinX = Math.min(selMinX, cb.minX());
                        selMaxX = Math.max(selMaxX, cb.maxX() + 1);
                        selMinZ = Math.min(selMinZ, cb.minZ());
                        selMaxZ = Math.max(selMaxZ, cb.maxZ() + 1);
                        mergedClaimIds.add(claim.getClaimId());
                    }
                }
            }
        }

        // 2. Render Existing Claims
        double pY = player.getY() - camPos.y;

        for (ZooClaim claim : CLIENT_CLAIMS) {
            if (mergedClaimIds.contains(claim.getClaimId())) {
                continue;
            }

            BoundingBox box = claim.getBox();
            boolean isOwner = claim.getOwnerUuid().equals(player.getUUID());
            float r = isOwner ? 0.0f : 1.0f;
            float g = isOwner ? 0.9f : 0.2f;
            float b = isOwner ? 1.0f : 0.2f;
            float a = 0.85f;

            double minX = box.minX() - camPos.x;
            double maxX = box.maxX() + 1.0 - camPos.x;
            double minY = worldMinY - camPos.y;
            double maxY = worldMaxY - camPos.y;
            double minZ = box.minZ() - camPos.z;
            double maxZ = box.maxZ() + 1.0 - camPos.z;

            // Full height wireframe
            LevelRenderer.renderLineBox(poseStack, lineConsumer, minX, minY, minZ, maxX, maxY, maxZ, r, g, b, 0.35f);
            // Ground-level visible border ring
            renderHorizontalPerimeter(poseStack, lineConsumer, minX, maxX, pY + 0.05, minZ, maxZ, r, g, b, a);
            renderHorizontalPerimeter(poseStack, lineConsumer, minX, maxX, pY + 1.05, minZ, maxZ, r, g, b, a * 0.6f);
        }

        // 3. Render Active Selection
        if (pos1 != null && pos2 == null) {
            double bMinX = pos1.getX() - camPos.x;
            double bMaxX = pos1.getX() + 1.0 - camPos.x;
            double bMinY = pos1.getY() - camPos.y;
            double bMaxY = pos1.getY() + 1.0 - camPos.y;
            double bMinZ = pos1.getZ() - camPos.z;
            double bMaxZ = pos1.getZ() + 1.0 - camPos.z;

            // 1x1 block marker around Corner 1
            LevelRenderer.renderLineBox(poseStack, lineConsumer, bMinX, bMinY, bMinZ, bMaxX, bMaxY, bMaxZ, 0.0f, 1.0f, 0.4f, 1.0f);
            // Vertical beam
            LevelRenderer.renderLineBox(poseStack, lineConsumer, bMinX, worldMinY - camPos.y, bMinZ, bMaxX, worldMaxY - camPos.y, bMaxZ, 0.0f, 1.0f, 0.4f, 0.4f);
        } else if (hasActiveBox) {
            float r = conflictOther ? 1.0f : 0.0f;
            float g = conflictOther ? 0.1f : 1.0f;
            float b = conflictOther ? 0.1f : 0.3f;
            float a = 0.95f;

            double minX = selMinX - camPos.x;
            double maxX = selMaxX - camPos.x;
            double minY = worldMinY - camPos.y;
            double maxY = worldMaxY - camPos.y;
            double minZ = selMinZ - camPos.z;
            double maxZ = selMaxZ - camPos.z;

            // 1x1 Block marker at Corner 1
            double c1X = (pos1.getX() + claimOffsetX) - camPos.x;
            double c1Y = pos1.getY() - camPos.y;
            double c1Z = (pos1.getZ() + claimOffsetZ) - camPos.z;
            LevelRenderer.renderLineBox(poseStack, lineConsumer, c1X, c1Y, c1Z, c1X + 1.0, c1Y + 1.0, c1Z + 1.0, 0.0f, 1.0f, 0.5f, 1.0f);

            // 1x1 Block marker at Corner 2
            double c2X = (pos2.getX() + claimOffsetX) - camPos.x;
            double c2Y = pos2.getY() - camPos.y;
            double c2Z = (pos2.getZ() + claimOffsetZ) - camPos.z;
            LevelRenderer.renderLineBox(poseStack, lineConsumer, c2X, c2Y, c2Z, c2X + 1.0, c2Y + 1.0, c2Z + 1.0, 0.0f, 1.0f, 0.5f, 1.0f);

            // Ground perimeter rings (feet, waist, head height)
            renderHorizontalPerimeter(poseStack, lineConsumer, minX, maxX, pY + 0.05, minZ, maxZ, r, g, b, a);
            renderHorizontalPerimeter(poseStack, lineConsumer, minX, maxX, pY + 1.0, minZ, maxZ, r, g, b, a * 0.8f);
            renderHorizontalPerimeter(poseStack, lineConsumer, minX, maxX, pY + 2.0, minZ, maxZ, r, g, b, a * 0.6f);

            // Corner vertical pillars
            renderVerticalPillars(poseStack, lineConsumer, minX, maxX, pY - 8.0, pY + 16.0, minZ, maxZ, r, g, b, a);

            // Full world box wireframe
            LevelRenderer.renderLineBox(poseStack, lineConsumer, minX, minY, minZ, maxX, maxY, maxZ, r, g, b, 0.35f);
        }
    }

    private static void renderHorizontalPerimeter(
            PoseStack poseStack, VertexConsumer consumer,
            double minX, double maxX, double y, double minZ, double maxZ,
            float r, float g, float b, float a
    ) {
        var pose = poseStack.last();
        consumer.addVertex(pose, (float) minX, (float) y, (float) minZ).setColor(r, g, b, a).setNormal(pose, 1, 0, 0);
        consumer.addVertex(pose, (float) maxX, (float) y, (float) minZ).setColor(r, g, b, a).setNormal(pose, 1, 0, 0);

        consumer.addVertex(pose, (float) maxX, (float) y, (float) minZ).setColor(r, g, b, a).setNormal(pose, 0, 0, 1);
        consumer.addVertex(pose, (float) maxX, (float) y, (float) maxZ).setColor(r, g, b, a).setNormal(pose, 0, 0, 1);

        consumer.addVertex(pose, (float) maxX, (float) y, (float) maxZ).setColor(r, g, b, a).setNormal(pose, -1, 0, 0);
        consumer.addVertex(pose, (float) minX, (float) y, (float) maxZ).setColor(r, g, b, a).setNormal(pose, -1, 0, 0);

        consumer.addVertex(pose, (float) minX, (float) y, (float) maxZ).setColor(r, g, b, a).setNormal(pose, 0, 0, -1);
        consumer.addVertex(pose, (float) minX, (float) y, (float) minZ).setColor(r, g, b, a).setNormal(pose, 0, 0, -1);
    }

    private static void renderVerticalPillars(
            PoseStack poseStack, VertexConsumer consumer,
            double minX, double maxX, double minY, double maxY, double minZ, double maxZ,
            float r, float g, float b, float a
    ) {
        var pose = poseStack.last();
        consumer.addVertex(pose, (float) minX, (float) minY, (float) minZ).setColor(r, g, b, a).setNormal(pose, 0, 1, 0);
        consumer.addVertex(pose, (float) minX, (float) maxY, (float) minZ).setColor(r, g, b, a).setNormal(pose, 0, 1, 0);

        consumer.addVertex(pose, (float) maxX, (float) minY, (float) minZ).setColor(r, g, b, a).setNormal(pose, 0, 1, 0);
        consumer.addVertex(pose, (float) maxX, (float) maxY, (float) minZ).setColor(r, g, b, a).setNormal(pose, 0, 1, 0);

        consumer.addVertex(pose, (float) maxX, (float) minY, (float) maxZ).setColor(r, g, b, a).setNormal(pose, 0, 1, 0);
        consumer.addVertex(pose, (float) maxX, (float) maxY, (float) maxZ).setColor(r, g, b, a).setNormal(pose, 0, 1, 0);

        consumer.addVertex(pose, (float) minX, (float) minY, (float) maxZ).setColor(r, g, b, a).setNormal(pose, 0, 1, 0);
        consumer.addVertex(pose, (float) minX, (float) maxY, (float) maxZ).setColor(r, g, b, a).setNormal(pose, 0, 1, 0);
    }
}

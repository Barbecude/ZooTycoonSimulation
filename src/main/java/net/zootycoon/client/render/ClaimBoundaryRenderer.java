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

    // Cached selection mesh data & dirty state
    private static boolean cacheDirty = true;
    private static CachedSelectionData cachedSelection = null;

    public static class CachedSelectionData {
        public final BlockPos pos1;
        public final BlockPos pos2;
        public final int offsetX;
        public final int offsetZ;
        public final UUID playerUuid;
        public final int claimsHash;

        public final boolean hasActiveBox;
        public final boolean hasSinglePos;
        public final boolean conflictOther;
        public final Set<UUID> mergedClaimIds;

        // Selection perimeter bounds in world coordinates
        public final double selMinX;
        public final double selMaxX;
        public final double selMinZ;
        public final double selMaxZ;

        // Selection corner marker points in world coordinates
        public final double c1X, c1Y, c1Z;
        public final double c2X, c2Y, c2Z;

        // Precalculated selection metrics
        public final int width;
        public final int length;
        public final int totalBlocks;
        public final int newBlocks;
        public final int cost;

        public CachedSelectionData(
                BlockPos pos1, BlockPos pos2, int offsetX, int offsetZ,
                UUID playerUuid, int claimsHash,
                boolean hasActiveBox, boolean hasSinglePos, boolean conflictOther,
                Set<UUID> mergedClaimIds,
                double selMinX, double selMaxX, double selMinZ, double selMaxZ,
                double c1X, double c1Y, double c1Z,
                double c2X, double c2Y, double c2Z,
                int width, int length, int totalBlocks, int newBlocks, int cost
        ) {
            this.pos1 = pos1;
            this.pos2 = pos2;
            this.offsetX = offsetX;
            this.offsetZ = offsetZ;
            this.playerUuid = playerUuid;
            this.claimsHash = claimsHash;
            this.hasActiveBox = hasActiveBox;
            this.hasSinglePos = hasSinglePos;
            this.conflictOther = conflictOther;
            this.mergedClaimIds = mergedClaimIds;
            this.selMinX = selMinX;
            this.selMaxX = selMaxX;
            this.selMinZ = selMinZ;
            this.selMaxZ = selMaxZ;
            this.c1X = c1X;
            this.c1Y = c1Y;
            this.c1Z = c1Z;
            this.c2X = c2X;
            this.c2Y = c2Y;
            this.c2Z = c2Z;
            this.width = width;
            this.length = length;
            this.totalBlocks = totalBlocks;
            this.newBlocks = newBlocks;
            this.cost = cost;
        }
    }

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

    public static void invalidateCache() {
        cacheDirty = true;
    }

    public static void clearClaimPreview() {
        pos1 = null;
        pos2 = null;
        claimOffsetX = 0;
        claimOffsetZ = 0;
        cacheDirty = true;
        cachedSelection = null;
    }

    public static void shiftOffsetX(int delta) {
        claimOffsetX += delta;
        cacheDirty = true;
        updateSelectionCache();
        showSelectionStatus();
    }

    public static void shiftOffsetZ(int delta) {
        claimOffsetZ += delta;
        cacheDirty = true;
        updateSelectionCache();
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
            cacheDirty = true;
            updateSelectionCache();
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
            cacheDirty = true;
            updateSelectionCache();
            showSelectionStatus();
        } else {
            // Already had pos1 and pos2 -> start fresh selection
            pos1 = clickedPos;
            pos2 = null;
            claimOffsetX = 0;
            claimOffsetZ = 0;
            cacheDirty = true;
            updateSelectionCache();
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
            cacheDirty = true;
            updateSelectionCache();
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

        ensureSelectionCache(player, mc.level);
        CachedSelectionData data = cachedSelection;
        if (data == null || !data.hasActiveBox) return null;

        int ownedEmeralds = TransactionHelper.countEmeralds(player);
        boolean canAfford = ownedEmeralds >= data.cost;

        return new SelectionInfo(
                data.width, data.length, data.totalBlocks, data.newBlocks,
                data.cost, data.conflictOther, canAfford, ownedEmeralds
        );
    }

    private static void updateSelectionCache() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.level != null) {
            rebuildSelectionCache(mc.player, mc.level);
        } else {
            cacheDirty = true;
        }
    }

    private static void ensureSelectionCache(Player player, net.minecraft.world.level.Level level) {
        if (cacheDirty || cachedSelection == null || !isCacheValid(player)) {
            rebuildSelectionCache(player, level);
        }
    }

    private static boolean isCacheValid(Player player) {
        if (cachedSelection == null) return false;
        if (!java.util.Objects.equals(pos1, cachedSelection.pos1)) return false;
        if (!java.util.Objects.equals(pos2, cachedSelection.pos2)) return false;
        if (claimOffsetX != cachedSelection.offsetX) return false;
        if (claimOffsetZ != cachedSelection.offsetZ) return false;
        if (!player.getUUID().equals(cachedSelection.playerUuid)) return false;
        if (CLIENT_CLAIMS.hashCode() != cachedSelection.claimsHash) return false;
        return true;
    }

    private static void rebuildSelectionCache(Player player, net.minecraft.world.level.Level level) {
        cacheDirty = false;
        if (pos1 == null) {
            cachedSelection = null;
            return;
        }

        int worldMinY = level != null ? level.getMinBuildHeight() : -64;
        int worldMaxY = level != null ? level.getMaxBuildHeight() : 320;
        int claimsHash = CLIENT_CLAIMS.hashCode();
        UUID playerUuid = player.getUUID();

        if (pos2 == null) {
            double c1X = pos1.getX();
            double c1Y = pos1.getY();
            double c1Z = pos1.getZ();
            cachedSelection = new CachedSelectionData(
                    pos1, null, claimOffsetX, claimOffsetZ, playerUuid, claimsHash,
                    false, true, false, java.util.Collections.emptySet(),
                    c1X, c1X + 1.0, c1Z, c1Z + 1.0,
                    c1X, c1Y, c1Z, 0, 0, 0,
                    0, 0, 0, 0, 0
            );
            return;
        }

        // Active 2-point box selection
        int selMinX = Math.min(pos1.getX(), pos2.getX()) + claimOffsetX;
        int selMaxX = Math.max(pos1.getX(), pos2.getX()) + claimOffsetX + 1;
        int selMinZ = Math.min(pos1.getZ(), pos2.getZ()) + claimOffsetZ;
        int selMaxZ = Math.max(pos1.getZ(), pos2.getZ()) + claimOffsetZ + 1;

        BoundingBox selBox = new BoundingBox(selMinX, worldMinY, selMinZ, selMaxX - 1, worldMaxY, selMaxZ - 1);
        boolean conflictOther = false;
        Set<UUID> mergedClaimIds = new HashSet<>();

        for (ZooClaim claim : CLIENT_CLAIMS) {
            if (!claim.getOwnerUuid().equals(playerUuid)) {
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

        double c1X = pos1.getX() + claimOffsetX;
        double c1Y = pos1.getY();
        double c1Z = pos1.getZ() + claimOffsetZ;

        double c2X = pos2.getX() + claimOffsetX;
        double c2Y = pos2.getY();
        double c2Z = pos2.getZ() + claimOffsetZ;

        // Precalculate selection metrics
        int rawMinX = Math.min(pos1.getX(), pos2.getX()) + claimOffsetX;
        int rawMaxX = Math.max(pos1.getX(), pos2.getX()) + claimOffsetX;
        int rawMinZ = Math.min(pos1.getZ(), pos2.getZ()) + claimOffsetZ;
        int rawMaxZ = Math.max(pos1.getZ(), pos2.getZ()) + claimOffsetZ;
        int width = rawMaxX - rawMinX + 1;
        int length = rawMaxZ - rawMinZ + 1;
        int totalBlocks = width * length;

        int newBlocks = 0;
        for (int x = rawMinX; x <= rawMaxX; x++) {
            for (int z = rawMinZ; z <= rawMaxZ; z++) {
                boolean owned = false;
                for (ZooClaim claim : CLIENT_CLAIMS) {
                    if (claim.getOwnerUuid().equals(playerUuid) && claim.contains(x, z)) {
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

        cachedSelection = new CachedSelectionData(
                pos1, pos2, claimOffsetX, claimOffsetZ, playerUuid, claimsHash,
                true, false, conflictOther, java.util.Collections.unmodifiableSet(mergedClaimIds),
                selMinX, selMaxX, selMinZ, selMaxZ,
                c1X, c1Y, c1Z, c2X, c2Y, c2Z,
                width, length, totalBlocks, newBlocks, cost
        );
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

        ensureSelectionCache(player, mc.level);
        CachedSelectionData selData = cachedSelection;

        // 2. Render Existing Claims
        double pY = player.getY() - camPos.y;
        Set<UUID> mergedClaimIds = (selData != null && selData.hasActiveBox) ? selData.mergedClaimIds : java.util.Collections.emptySet();

        for (ZooClaim claim : CLIENT_CLAIMS) {
            if (mergedClaimIds.contains(claim.getClaimId())) {
                continue;
            }

            BoundingBox box = claim.getBox();
            boolean isOwner = claim.getOwnerUuid().equals(player.getUUID());
            float r = isOwner ? 0.0f : 1.0f;
            float g = isOwner ? 0.9f : 0.2f;
            float b = isOwner ? 1.0f : 0.2f;

            double minX = box.minX() - camPos.x;
            double maxX = box.maxX() + 1.0 - camPos.x;
            double minZ = box.minZ() - camPos.z;
            double maxZ = box.maxZ() + 1.0 - camPos.z;

            // Ground-level perimeter
            renderHorizontalPerimeter(poseStack, lineConsumer, minX, maxX, pY + 0.05, minZ, maxZ, r, g, b, 0.75f);

            // Tall towering vertical corner lines up into the sky
            renderVerticalPillars(poseStack, lineConsumer, minX, maxX, pY - 2.0, pY + 96.0, minZ, maxZ, r, g, b, 0.65f);

            // Elevated sky perimeter ring
            renderHorizontalPerimeter(poseStack, lineConsumer, minX, maxX, pY + 96.0, minZ, maxZ, r, g, b, 0.45f);
        }

        // 3. Render Active Selection
        if (selData != null && selData.hasSinglePos) {
            double bMinX = selData.c1X - camPos.x;
            double bMaxX = selData.c1X + 1.0 - camPos.x;
            double bMinY = selData.c1Y - camPos.y;
            double bMaxY = selData.c1Y + 1.0 - camPos.y;
            double bMinZ = selData.c1Z - camPos.z;
            double bMaxZ = selData.c1Z + 1.0 - camPos.z;

            // Clean 1x1 block marker around Corner 1 with vertical beacon
            LevelRenderer.renderLineBox(poseStack, lineConsumer, bMinX, bMinY, bMinZ, bMaxX, bMaxY, bMaxZ, 0.0f, 1.0f, 0.4f, 1.0f);
            renderVerticalPillars(poseStack, lineConsumer, bMinX, bMaxX, bMinY, bMinY + 64.0, bMinZ, bMaxZ, 0.0f, 1.0f, 0.4f, 0.75f);
        } else if (selData != null && selData.hasActiveBox) {
            float r = selData.conflictOther ? 1.0f : 0.0f;
            float g = selData.conflictOther ? 0.1f : 1.0f;
            float b = selData.conflictOther ? 0.1f : 0.3f;
            float a = 0.95f;

            double minX = selData.selMinX - camPos.x;
            double maxX = selData.selMaxX - camPos.x;
            double minZ = selData.selMinZ - camPos.z;
            double maxZ = selData.selMaxZ - camPos.z;

            // 1x1 Block marker at Corner 1
            double c1X = selData.c1X - camPos.x;
            double c1Y = selData.c1Y - camPos.y;
            double c1Z = selData.c1Z - camPos.z;
            LevelRenderer.renderLineBox(poseStack, lineConsumer, c1X, c1Y, c1Z, c1X + 1.0, c1Y + 1.0, c1Z + 1.0, 0.0f, 1.0f, 0.5f, 1.0f);

            // 1x1 Block marker at Corner 2
            double c2X = selData.c2X - camPos.x;
            double c2Y = selData.c2Y - camPos.y;
            double c2Z = selData.c2Z - camPos.z;
            LevelRenderer.renderLineBox(poseStack, lineConsumer, c2X, c2Y, c2Z, c2X + 1.0, c2Y + 1.0, c2Z + 1.0, 0.0f, 1.0f, 0.5f, 1.0f);

            // Ground perimeter ring
            renderHorizontalPerimeter(poseStack, lineConsumer, minX, maxX, pY + 0.05, minZ, maxZ, r, g, b, a);

            // Tall towering vertical pillars up into the sky
            renderVerticalPillars(poseStack, lineConsumer, minX, maxX, pY - 2.0, pY + 96.0, minZ, maxZ, r, g, b, a * 0.85f);

            // Elevated sky perimeter ring
            renderHorizontalPerimeter(poseStack, lineConsumer, minX, maxX, pY + 96.0, minZ, maxZ, r, g, b, a * 0.6f);
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

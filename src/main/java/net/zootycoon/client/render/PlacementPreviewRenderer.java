package net.zootycoon.client.render;

import com.mojang.blaze3d.platform.InputConstants;
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
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.zootycoon.ZooTycoonMod;
import net.zootycoon.claim.ZooClaim;
import net.zootycoon.network.ZooNetworkPayloads;
import net.zootycoon.schematic.SchematicScanner;
import net.zootycoon.schematic.ZooSchematic;
import net.zootycoon.schematic.ZooSchematicRegistry;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class PlacementPreviewRenderer {
    private static ZooSchematic activeSchematic = null;
    private static BlockPos baseRaycastPos = null;
    private static int placementOffsetX = 0;
    private static int placementOffsetY = 0;
    private static int placementOffsetZ = 0;
    private static Rotation currentRotation = Rotation.NONE;
    private static boolean lastRState = false;

    private static StructureTemplate activeTemplate = null;
    private static List<StructureTemplate.StructureBlockInfo> currentBlocks = null;
    private static BoundingBox currentBoundingBox = null;

    @SuppressWarnings("unchecked")
    private static List<StructureTemplate.StructureBlockInfo> getRawTemplateBlocks(StructureTemplate template) {
        return SchematicScanner.getRawTemplateBlocks(template);
    }

    public static void setActiveSchematic(ZooSchematic schematic) {
        ClaimBoundaryRenderer.clearClaimPreview();
        activeSchematic = schematic;
        placementOffsetX = 0;
        placementOffsetY = 0;
        placementOffsetZ = 0;
        currentRotation = Rotation.NONE;
        lastRState = false;
        loadTemplateForSchematic(schematic);
    }

    public static ZooSchematic getActiveSchematic() {
        return activeSchematic;
    }

    public static Rotation getCurrentRotation() {
        return currentRotation;
    }

    public static BlockPos getTargetPlacementPos() {
        Minecraft mc = Minecraft.getInstance();
        if (baseRaycastPos == null && mc.player != null) {
            HitResult hit = mc.player.pick(64.0D, 0.0f, false);
            if (hit.getType() == HitResult.Type.BLOCK) {
                BlockHitResult bHit = (BlockHitResult) hit;
                baseRaycastPos = bHit.getBlockPos().relative(bHit.getDirection());
            } else {
                baseRaycastPos = mc.player.blockPosition();
            }
        }
        if (baseRaycastPos == null) return null;
        return baseRaycastPos.offset(placementOffsetX, placementOffsetY, placementOffsetZ);
    }

    public static void shiftOffsetX(int delta) {
        placementOffsetX += delta;
    }

    public static void shiftOffsetY(int delta) {
        placementOffsetY += delta;
    }

    public static void shiftOffsetZ(int delta) {
        placementOffsetZ += delta;
    }

    public static void rotate(int steps) {
        if (activeSchematic == null) return;
        if (steps > 0) {
            for (int i = 0; i < steps; i++) {
                currentRotation = nextRotation(currentRotation);
            }
        } else if (steps < 0) {
            for (int i = 0; i < -steps; i++) {
                currentRotation = prevRotation(currentRotation);
            }
        }
        updateRotatedBlocks();

        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.5f, 1.2f);
        }
    }

    private static Rotation nextRotation(Rotation r) {
        return switch (r) {
            case NONE -> Rotation.CLOCKWISE_90;
            case CLOCKWISE_90 -> Rotation.CLOCKWISE_180;
            case CLOCKWISE_180 -> Rotation.COUNTERCLOCKWISE_90;
            case COUNTERCLOCKWISE_90 -> Rotation.NONE;
        };
    }

    private static Rotation prevRotation(Rotation r) {
        return switch (r) {
            case NONE -> Rotation.COUNTERCLOCKWISE_90;
            case COUNTERCLOCKWISE_90 -> Rotation.CLOCKWISE_180;
            case CLOCKWISE_180 -> Rotation.CLOCKWISE_90;
            case CLOCKWISE_90 -> Rotation.NONE;
        };
    }

    public static void clear() {
        activeSchematic = null;
        activeTemplate = null;
        baseRaycastPos = null;
        currentBlocks = null;
        currentBoundingBox = null;
        placementOffsetX = 0;
        placementOffsetY = 0;
        placementOffsetZ = 0;
        currentRotation = Rotation.NONE;
        lastRState = false;
    }

    public static void confirmBuild(BlockPos clickedPos) {
        BlockPos targetPos = getTargetPlacementPos();
        if (targetPos == null && clickedPos != null) {
            targetPos = clickedPos.above().offset(placementOffsetX, placementOffsetY, placementOffsetZ);
        }
        if (activeSchematic != null && targetPos != null) {
            ClientPlayNetworking.send(new ZooNetworkPayloads.RequestBuildPayload(
                    targetPos,
                    activeSchematic.getId(),
                    currentRotation.name()
            ));
            clear();
        }
    }

    private static void loadTemplateForSchematic(ZooSchematic schem) {
        if (schem == null) {
            activeTemplate = null;
            currentBlocks = null;
            currentBoundingBox = null;
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            activeTemplate = null;
            currentBlocks = null;
            currentBoundingBox = null;
            return;
        }

        Path nbtFile = ZooSchematicRegistry.getCustomSchematicsDir().resolve(schem.getStructureLocation().getPath() + ".nbt");
        if (Files.exists(nbtFile)) {
            try {
                CompoundTag tag = NbtIo.readCompressed(nbtFile, NbtAccounter.unlimitedHeap());
                StructureTemplate template = new StructureTemplate();
                template.load(mc.level.holderLookup(Registries.BLOCK), tag);
                activeTemplate = template;
                updateRotatedBlocks();
                ZooTycoonMod.LOGGER.info("Loaded schematic template {} for preview with {} blocks", schem.getName(), currentBlocks.size());
                return;
            } catch (Exception e) {
                ZooTycoonMod.LOGGER.error("Failed to load preview template: " + nbtFile, e);
            }
        }
        activeTemplate = null;
        currentBlocks = Collections.emptyList();
        currentBoundingBox = null;
    }

    public static void updateRotatedBlocks() {
        if (activeTemplate == null) {
            currentBlocks = Collections.emptyList();
            currentBoundingBox = null;
            return;
        }
        StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(currentRotation);
        List<StructureTemplate.StructureBlockInfo> rawList = getRawTemplateBlocks(activeTemplate);
        List<StructureTemplate.StructureBlockInfo> rotated = new ArrayList<>();
        for (StructureTemplate.StructureBlockInfo raw : rawList) {
            if (raw.state().isAir()) continue;
            BlockPos rotPos = StructureTemplate.calculateRelativePosition(settings, raw.pos());
            BlockState rotState = raw.state().rotate(currentRotation);
            rotated.add(new StructureTemplate.StructureBlockInfo(rotPos, rotState, raw.nbt()));
        }
        currentBlocks = rotated;
        currentBoundingBox = activeTemplate.getBoundingBox(settings, BlockPos.ZERO);
        ZooTycoonMod.LOGGER.info("Preview blocks updated: {} non-air blocks, rotation={}", currentBlocks.size(), currentRotation);
    }

    public static int getPlacementOffsetX() {
        return placementOffsetX;
    }

    public static int getPlacementOffsetY() {
        return placementOffsetY;
    }

    public static int getPlacementOffsetZ() {
        return placementOffsetZ;
    }

    public static String getRotationDegreeString() {
        return switch (currentRotation) {
            case NONE -> "0°";
            case CLOCKWISE_90 -> "90°";
            case CLOCKWISE_180 -> "180°";
            case COUNTERCLOCKWISE_90 -> "270°";
        };
    }

    public static boolean isTargetInsideClaim() {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || activeSchematic == null) return false;

        BlockPos targetPos = getTargetPlacementPos();
        if (targetPos == null) return false;

        BoundingBox worldBox = currentBoundingBox != null
                ? currentBoundingBox.moved(targetPos.getX(), targetPos.getY(), targetPos.getZ())
                : new BoundingBox(targetPos);

        for (ZooClaim claim : ClaimBoundaryRenderer.CLIENT_CLAIMS) {
            if (claim.getOwnerUuid().equals(player.getUUID())) {
                if (claim.contains(worldBox.minX(), worldBox.minZ())
                        && claim.contains(worldBox.maxX(), worldBox.maxZ())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void clientTick(Minecraft mc) {
        if (activeSchematic == null) return;
        Player player = mc.player;
        if (player == null || mc.level == null) {
            clear();
            return;
        }

        boolean holdingTool = player.getMainHandItem().is(net.minecraft.world.item.Items.STICK)
                || player.getOffhandItem().is(net.minecraft.world.item.Items.STICK);
        if (!holdingTool) {
            clear();
            return;
        }

        // Keyboard rotation: R key
        boolean rDown = InputConstants.isKeyDown(mc.getWindow().getWindow(), InputConstants.KEY_R);
        if (rDown && !lastRState) {
            boolean shift = InputConstants.isKeyDown(mc.getWindow().getWindow(), InputConstants.KEY_LSHIFT)
                    || InputConstants.isKeyDown(mc.getWindow().getWindow(), InputConstants.KEY_RSHIFT);
            rotate(shift ? -1 : 1);
        }
        lastRState = rDown;

        HitResult hit = player.pick(64.0D, 0.0f, false);
        if (hit.getType() == HitResult.Type.BLOCK) {
            BlockHitResult bHit = (BlockHitResult) hit;
            baseRaycastPos = bHit.getBlockPos().relative(bHit.getDirection());
        } else {
            baseRaycastPos = player.blockPosition();
        }
    }

    /**
     * Called during WorldRenderEvents.AFTER_ENTITIES so ghost blocks and outlines are rendered.
     */
    public static void render(WorldRenderContext context) {
        if (activeSchematic == null) return;
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.level == null) return;

        BlockPos targetPos = getTargetPlacementPos();
        if (targetPos == null) return;

        MultiBufferSource buffer = context.consumers();
        if (buffer == null) return;

        PoseStack poseStack = context.matrixStack();
        Camera camera = context.camera();
        Vec3 camPos = camera.getPosition();

        boolean isValidClaim = isTargetInsideClaim();
        float r = isValidClaim ? 0.0f : 1.0f;
        float g = isValidClaim ? 1.0f : 0.1f;
        float b = isValidClaim ? 0.4f : 0.1f;

        // 1. Ghost block rendering
        if (currentBlocks != null && !currentBlocks.isEmpty()) {
            int renderLimit = Math.min(currentBlocks.size(), 4000);
            for (int i = 0; i < renderLimit; i++) {
                StructureTemplate.StructureBlockInfo info = currentBlocks.get(i);
                BlockPos worldPos = targetPos.offset(info.pos());

                poseStack.pushPose();
                poseStack.translate(worldPos.getX() - camPos.x, worldPos.getY() - camPos.y, worldPos.getZ() - camPos.z);

                try {
                    mc.getBlockRenderer().renderSingleBlock(
                            info.state(),
                            poseStack,
                            buffer,
                            0xF000F0,
                            net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY
                    );
                } catch (Throwable ignored) {}

                poseStack.popPose();
            }

            // 2. Individual block wireframe outlines
            VertexConsumer lineConsumer = buffer.getBuffer(RenderType.lines());
            for (int i = 0; i < renderLimit; i++) {
                StructureTemplate.StructureBlockInfo info = currentBlocks.get(i);
                BlockPos worldPos = targetPos.offset(info.pos());
                double bx = worldPos.getX() - camPos.x;
                double by = worldPos.getY() - camPos.y;
                double bz = worldPos.getZ() - camPos.z;

                LevelRenderer.renderLineBox(poseStack, lineConsumer, bx, by, bz, bx + 1.0, by + 1.0, bz + 1.0, r, g, b, 0.4f);
            }
        }

        // 3. Full schematic bounding box outline
        BoundingBox worldBox = currentBoundingBox != null
                ? currentBoundingBox.moved(targetPos.getX(), targetPos.getY(), targetPos.getZ())
                : new BoundingBox(targetPos);

        if (worldBox != null) {
            VertexConsumer lineConsumer = buffer.getBuffer(RenderType.lines());
            double minX = worldBox.minX() - camPos.x;
            double maxX = worldBox.maxX() + 1.0 - camPos.x;
            double minY = worldBox.minY() - camPos.y;
            double maxY = worldBox.maxY() + 1.0 - camPos.y;
            double minZ = worldBox.minZ() - camPos.z;
            double maxZ = worldBox.maxZ() + 1.0 - camPos.z;

            LevelRenderer.renderLineBox(poseStack, lineConsumer, minX, minY, minZ, maxX, maxY, maxZ, r, g, b, 0.95f);
        }
    }
}

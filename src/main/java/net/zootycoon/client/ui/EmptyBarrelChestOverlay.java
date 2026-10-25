package net.zootycoon.client.ui;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.zootycoon.block.EmptyBarrelBlockEntity;
import net.zootycoon.entity.ZooEntities;
import net.zootycoon.network.WarehousePayloads;
import net.zootycoon.util.ZooTranslations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class EmptyBarrelChestOverlay {

    private static BlockPos activeBarrelPos = null;
    private static String activeCategory = "";
    private static List<WarehousePayloads.StaffRequestDto> activeRequests = new ArrayList<>();
    private static float scrollOffset = 0.0f;
    private static int maxScroll = 0;

    private static final Map<UUID, LivingEntity> dummyStaffCache = new HashMap<>();

    private static final int CHEST_WIDTH = 176;
    private static final int CHEST_HEIGHT = 166;
    private static final int CARD_HEIGHT = 68;
    private static final int CARD_GAP = 5;

    public static void prepareBarrelOpen(BlockPos pos, String category) {
        activeBarrelPos = pos;
        activeCategory = category != null ? category : "";
        activeRequests.clear();
        scrollOffset = 0.0f;
    }

    public static void onSyncRequests(WarehousePayloads.SyncBarrelRequestsPayload payload) {
        activeBarrelPos = payload.pos();
        activeCategory = payload.category() != null ? payload.category() : "";
        activeRequests = new ArrayList<>(payload.requests());
    }

    public static void reset() {
        activeBarrelPos = null;
        activeCategory = "";
        activeRequests.clear();
        dummyStaffCache.clear();
        scrollOffset = 0.0f;
        maxScroll = 0;
    }

    public static void register() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof ContainerScreen containerScreen && isBarrelContainer(containerScreen)) {
                ScreenEvents.afterRender(screen).register((scr, graphics, mouseX, mouseY, tickDelta) -> {
                    renderLeftRequestPanel((ContainerScreen) scr, graphics, mouseX, mouseY);
                });

                ScreenMouseEvents.allowMouseClick(screen).register((scr, mouseX, mouseY, button) -> {
                    return !handleMouseClicked((ContainerScreen) scr, mouseX, mouseY, button);
                });

                ScreenMouseEvents.allowMouseScroll(screen).register((scr, mouseX, mouseY, horizontalAmount, verticalAmount) -> {
                    return !handleMouseScrolled((ContainerScreen) scr, mouseX, mouseY, verticalAmount);
                });

                ScreenEvents.remove(screen).register(scr -> {
                    activeBarrelPos = null;
                    activeRequests.clear();
                    scrollOffset = 0.0f;
                });
            }
        });
    }

    private static boolean isBarrelContainer(ContainerScreen screen) {
        if (activeBarrelPos != null) {
            return true;
        }
        String title = screen.getTitle().getString();
        return title.equals(ZooTranslations.get("zootycoon.warehouse.barrel.title.burger"))
                || title.equals(ZooTranslations.get("zootycoon.warehouse.barrel.title.drink"))
                || title.equals(ZooTranslations.get("zootycoon.warehouse.barrel.title.animal_food"));
    }

    private static int getPanelWidth(Screen screen) {
        int chestLeft = (screen.width - CHEST_WIDTH) / 2;
        return Math.min(176, Math.max(126, chestLeft - 10));
    }

    private static int getPanelX(Screen screen) {
        int chestLeft = (screen.width - CHEST_WIDTH) / 2;
        int panelW = getPanelWidth(screen);
        return Math.max(4, chestLeft - panelW - 6);
    }

    private static int getPanelY(Screen screen) {
        return (screen.height - CHEST_HEIGHT) / 2;
    }

    private static void renderLeftRequestPanel(ContainerScreen screen, GuiGraphics graphics, int mouseX, int mouseY) {
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;

        int panelW = getPanelWidth(screen);
        int panelX = getPanelX(screen);
        int panelY = getPanelY(screen);
        int panelH = CHEST_HEIGHT;

        // 1. Main Slate Frame (matching EasyNpcDialogScreen & MainManagementScreen)
        graphics.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xF212151D);
        graphics.renderOutline(panelX, panelY, panelW, panelH, 0xFF2A3140);

        // 2. Top Header Bar
        graphics.fill(panelX + 1, panelY + 1, panelX + panelW - 1, panelY + 25, 0xFF19202D);
        String headerTitle = ZooTranslations.get("zootycoon.warehouse.panel.header");
        graphics.drawString(font, headerTitle, panelX + 8, panelY + 5, 0xFFFFD700, false);

        String countLabel = activeRequests.size() + " " + ZooTranslations.get("zootycoon.warehouse.panel.requests_count");
        graphics.drawString(font, countLabel, panelX + 8, panelY + 15, 0xFFA0AAB8, false);
        graphics.fill(panelX + 1, panelY + 25, panelX + panelW - 1, panelY + 26, 0xFF252D3C);

        // 3. Scrollable Vertical Request Card Area
        int listX = panelX + 5;
        int listY = panelY + 29;
        int listH = panelH - 33;
        int listW = panelW - 10;

        if (activeRequests.isEmpty()) {
            String empty1 = ZooTranslations.get("zootycoon.warehouse.panel.empty1");
            String empty2 = ZooTranslations.get("zootycoon.warehouse.panel.empty2");
            int w1 = font.width(empty1);
            int w2 = font.width(empty2);
            graphics.drawString(font, empty1, panelX + (panelW - w1) / 2, listY + 46, 0xFF94A3B8, false);
            graphics.drawString(font, empty2, panelX + (panelW - w2) / 2, listY + 58, 0xFF64748B, false);
            return;
        }

        int totalContentH = activeRequests.size() * CARD_HEIGHT + Math.max(0, activeRequests.size() - 1) * CARD_GAP;
        maxScroll = Math.max(0, totalContentH - listH);
        scrollOffset = Math.clamp(scrollOffset, 0.0f, (float) maxScroll);

        int cardW = maxScroll > 0 ? (listW - 6) : listW;

        ItemStack hoveredStack = null;
        WarehousePayloads.StaffRequestDto hoveredDto = null;

        graphics.enableScissor(panelX + 2, listY, panelX + panelW - 2, listY + listH);

        for (int i = 0; i < activeRequests.size(); i++) {
            WarehousePayloads.StaffRequestDto req = activeRequests.get(i);
            int cardY = listY + i * (CARD_HEIGHT + CARD_GAP) - (int) scrollOffset;

            if (cardY + CARD_HEIGHT < listY || cardY > listY + listH) {
                continue;
            }

            // Card container
            graphics.fill(listX, cardY, listX + cardW, cardY + CARD_HEIGHT, 0xFF19202D);
            graphics.renderOutline(listX, cardY, cardW, CARD_HEIGHT, 0xFF2A364B);

            // A. Left Box: 3D Staff Skin Preview (Cashier or Zookeeper)
            int previewX = listX + 3;
            int previewY = cardY + 3;
            int previewW = 34;
            int previewH = CARD_HEIGHT - 6;

            graphics.fill(previewX, previewY, previewX + previewW, previewY + previewH, 0xFF12151D);
            graphics.renderOutline(previewX, previewY, previewW, previewH, 0xFF252D3C);

            LivingEntity staffEntity = resolveStaffEntity(req);
            if (staffEntity != null) {
                try {
                    InventoryScreen.renderEntityInInventoryFollowsMouse(
                            graphics,
                            previewX + 1, previewY + 2,
                            previewX + previewW - 1, previewY + previewH - 2,
                            24, 0.05f, (float) mouseX, (float) mouseY, staffEntity
                    );
                } catch (Throwable ignored) {
                }
            }

            // B. Right Column: Staff Name, Subtitle, Target Finished Food Icons, and Ambil Resep Button
            int infoX = previewX + previewW + 5;
            int infoW = cardW - (infoX - listX) - 4;

            // Row 1: Staff Name
            String staffName = req.staffName();
            if (font.width(staffName) > infoW) {
                staffName = font.plainSubstrByWidth(staffName, infoW - font.width("..")) + "..";
            }
            graphics.drawString(font, staffName, infoX, cardY + 4, 0xFFF8FAFC, false);

            // Row 2: Subtitle / Progress
            String subText = req.subtitle();
            if ("ZOOKEEPER".equalsIgnoreCase(req.staffType())) {
                subText = req.subtitle() + " (" + req.currentCount() + "/" + req.requiredCount() + ")";
            }
            if (font.width(subText) > infoW) {
                subText = font.plainSubstrByWidth(subText, infoW - font.width("..")) + "..";
            }
            graphics.drawString(font, subText, infoX, cardY + 14, 0xFF94A3B8, false);

            // Row 3: Target Menu / Food Items
            int slotY = cardY + 25;
            int slotSize = 18;
            List<ItemStack> targets = req.targetItems();

            if ("CASHIER".equalsIgnoreCase(req.staffType())) {
                // Cashier: display max 2 menu variants with their item icon and name/count
                int maxVariants = Math.min(2, targets.size());
                int curX = infoX;
                for (int v = 0; v < maxVariants; v++) {
                    ItemStack st = targets.get(v);
                    graphics.fill(curX, slotY, curX + slotSize, slotY + slotSize, 0xFF12151D);
                    graphics.renderOutline(curX, slotY, slotSize, slotSize, 0xFF334155);
                    graphics.renderFakeItem(st, curX + 1, slotY + 1);
                    graphics.renderItemDecorations(font, st, curX + 1, slotY + 1, String.valueOf(st.getCount()));

                    if (mouseX >= curX && mouseX <= curX + slotSize && mouseY >= slotY && mouseY <= slotY + slotSize
                            && mouseY >= listY && mouseY <= listY + listH) {
                        hoveredStack = st;
                        hoveredDto = req;
                    }

                    if (maxVariants == 1) {
                        String itemName = st.getHoverName().getString();
                        int nameMaxW = infoW - slotSize - 4;
                        if (font.width(itemName) > nameMaxW) {
                            itemName = font.plainSubstrByWidth(itemName, Math.max(8, nameMaxW - font.width(".."))) + "..";
                        }
                        graphics.drawString(font, itemName, curX + slotSize + 4, slotY + 5, 0xFFE2E8F0, false);
                    }
                    curX += slotSize + 4;
                }
            } else {
                // Zookeeper: show up to 5 accepted food icons (cycling smoothly if more than 5)
                int maxIcons = Math.min(targets.size(), Math.max(1, infoW / (slotSize + 2)));
                int pageOffset = 0;
                if (targets.size() > maxIcons) {
                    pageOffset = (int) ((System.currentTimeMillis() / 1800L) % targets.size());
                }
                for (int k = 0; k < maxIcons; k++) {
                    ItemStack st = targets.get((pageOffset + k) % targets.size());
                    int sx = infoX + k * (slotSize + 2);
                    graphics.fill(sx, slotY, sx + slotSize, slotY + slotSize, 0xFF12151D);
                    graphics.renderOutline(sx, slotY, slotSize, slotSize, 0xFF334155);
                    graphics.renderFakeItem(st, sx + 1, slotY + 1);
                    if (k == 0) {
                        graphics.renderItemDecorations(font, st, sx + 1, slotY + 1, "32");
                    }

                    if (mouseX >= sx && mouseX <= sx + slotSize && mouseY >= slotY && mouseY <= slotY + slotSize
                            && mouseY >= listY && mouseY <= listY + listH) {
                        hoveredStack = st;
                        hoveredDto = req;
                    }
                }
            }

            // Row 4: Ambil Resep / Ambil Catatan Button
            int btnX = infoX;
            int btnY = cardY + 46;
            int btnW = infoW;
            int btnH = 17;

            boolean btnHovered = mouseX >= btnX && mouseX <= btnX + btnW
                    && mouseY >= btnY && mouseY <= btnY + btnH
                    && mouseY >= listY && mouseY <= listY + listH;

            int bgColor = btnHovered ? 0xFF252D3D : 0xFF14171F;
            int borderColor = btnHovered ? 0xFFFFFFFF : 0xFF3A4456;
            int txtColor = btnHovered ? 0xFFFFFFFF : 0xFFD0D7DE;

            graphics.fill(btnX, btnY, btnX + btnW, btnY + btnH, bgColor);
            graphics.renderOutline(btnX, btnY, btnW, btnH, borderColor);

            String btnLabel = "CASHIER".equalsIgnoreCase(req.staffType())
                    ? ZooTranslations.get("zootycoon.warehouse.btn.take_recipe")
                    : ZooTranslations.get("zootycoon.warehouse.btn.take_food_note");
            int lblW = font.width(btnLabel);
            graphics.drawString(font, btnLabel, btnX + (btnW - lblW) / 2, btnY + (btnH - 8) / 2, txtColor, false);
        }

        graphics.disableScissor();

        // 4. Scrollbar if cards overflow
        if (maxScroll > 0) {
            int trackX = panelX + panelW - 7;
            int trackY = listY;
            int trackH = listH;
            graphics.fill(trackX, trackY, trackX + 3, trackY + trackH, 0xFF12151D);
            int thumbH = Math.max(18, (int) ((float) listH / totalContentH * trackH));
            int thumbY = trackY + (int) ((scrollOffset / maxScroll) * (trackH - thumbH));
            graphics.fill(trackX, thumbY, trackX + 3, thumbY + thumbH, 0xFF475569);
        }

        // 5. Tooltip when hovering over requested item
        if (hoveredStack != null && hoveredDto != null) {
            List<Component> lines = new ArrayList<>();
            lines.add(hoveredStack.getHoverName());
            if ("CASHIER".equalsIgnoreCase(hoveredDto.staffType())) {
                lines.add(Component.literal(ZooTranslations.get("zootycoon.warehouse.tooltip.raw_needed")).withStyle(ChatFormatting.YELLOW));
                for (ItemStack raw : hoveredDto.rawIngredients()) {
                    lines.add(Component.literal("- " + raw.getCount() + "x " + raw.getHoverName().getString()).withStyle(ChatFormatting.GRAY));
                }
            } else {
                lines.add(Component.literal(ZooTranslations.get("zootycoon.warehouse.tooltip.zookeeper_food")).withStyle(ChatFormatting.GRAY));
            }
            graphics.renderTooltip(font, lines, Optional.empty(), mouseX, mouseY);
        }
    }

    private static boolean handleMouseClicked(ContainerScreen screen, double mouseX, double mouseY, int button) {
        if (button != 0) return false;

        int panelW = getPanelWidth(screen);
        int panelX = getPanelX(screen);
        int panelY = getPanelY(screen);
        int panelH = CHEST_HEIGHT;

        if (mouseX < panelX || mouseX > panelX + panelW || mouseY < panelY || mouseY > panelY + panelH) {
            return false;
        }

        int listX = panelX + 5;
        int listY = panelY + 29;
        int listH = panelH - 33;
        int listW = panelW - 10;
        int cardW = maxScroll > 0 ? (listW - 6) : listW;

        if (mouseY >= listY && mouseY <= listY + listH) {
            for (int i = 0; i < activeRequests.size(); i++) {
                WarehousePayloads.StaffRequestDto req = activeRequests.get(i);
                int cardY = listY + i * (CARD_HEIGHT + CARD_GAP) - (int) scrollOffset;
                if (cardY + CARD_HEIGHT < listY || cardY > listY + listH) continue;

                int previewW = 34;
                int infoX = listX + 3 + previewW + 5;
                int infoW = cardW - (infoX - listX) - 4;
                int btnX = infoX;
                int btnY = cardY + 46;
                int btnW = infoW;
                int btnH = 17;

                if (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH) {
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    BlockPos posToSend = activeBarrelPos != null ? activeBarrelPos : BlockPos.ZERO;
                    ClientPlayNetworking.send(new WarehousePayloads.RequestBarrelRecipeNotePayload(posToSend, req.staffUuid()));
                    return true;
                }
            }
        }

        // Consume click inside left panel so ContainerScreen doesn't treat it as an outside drop click
        return true;
    }

    private static boolean handleMouseScrolled(ContainerScreen screen, double mouseX, double mouseY, double verticalAmount) {
        int panelW = getPanelWidth(screen);
        int panelX = getPanelX(screen);
        int panelY = getPanelY(screen);
        int panelH = CHEST_HEIGHT;

        if (mouseX >= panelX && mouseX <= panelX + panelW && mouseY >= panelY && mouseY <= panelY + panelH) {
            if (maxScroll > 0) {
                scrollOffset = Math.clamp(scrollOffset - (float) verticalAmount * 22.0f, 0.0f, (float) maxScroll);
            }
            return true;
        }
        return false;
    }

    private static LivingEntity resolveStaffEntity(WarehousePayloads.StaffRequestDto req) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return null;

        if (req.staffEntityId() != -1) {
            Entity ent = mc.level.getEntity(req.staffEntityId());
            if (ent instanceof LivingEntity living) {
                return living;
            }
        }
        for (Entity ent : mc.level.entitiesForRendering()) {
            if (ent.getUUID().equals(req.staffUuid()) && ent instanceof LivingEntity living) {
                return living;
            }
        }
        if (dummyStaffCache.containsKey(req.staffUuid())) {
            return dummyStaffCache.get(req.staffUuid());
        }
        LivingEntity dummy = "CASHIER".equalsIgnoreCase(req.staffType())
                ? ZooEntities.CASHIER.create(mc.level)
                : ZooEntities.ZOOKEEPER.create(mc.level);
        if (dummy != null) {
            dummyStaffCache.put(req.staffUuid(), dummy);
        }
        return dummy;
    }
}

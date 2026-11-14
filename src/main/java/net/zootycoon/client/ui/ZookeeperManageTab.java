package net.zootycoon.client.ui;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.zootycoon.client.ZooTycoonClient;
import net.zootycoon.economy.TransactionHelper;
import net.zootycoon.economy.ZooEconomyEngine;
import net.zootycoon.network.ZooNetworkPayloads;
import net.zootycoon.util.ZooTranslations;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Sub view of the staff screen: list zookeepers, call idle ones to the player or fire them. */
public class ZookeeperManageTab {
    private static final int ROW_H = 30;
    private static final int ROW_GAP = 4;
    private static final int BTN_H = 18;
    private static final int CALL_W = 68;
    private static final int PEEK_W = 40;
    private static final int FIRE_W = 52;
    private static final long CONFIRM_MS = 3000L;

    private final MainManagementScreen parent;
    private final StaffWageDropdownHelper wageDropdown = new StaffWageDropdownHelper();
    private final Set<UUID> pendingCalledKeepers = new HashSet<>();
    private float scrollOffset = 0.0f;
    private UUID confirmFireUuid = null;
    private long confirmFireUntil = 0L;

    public ZookeeperManageTab(MainManagementScreen parent) {
        this.parent = parent;
    }

    public void init(MainManagementScreen screen) {
        scrollOffset = 0.0f;
        confirmFireUuid = null;
        wageDropdown.close();
        ClientPlayNetworking.send(new ZooNetworkPayloads.RequestAnimalSyncPayload());
    }

    private int panelW() { return Math.min(parent.width - 30, 520); }
    private int panelX() { return (parent.width - panelW()) / 2; }
    private int listTop() { return 74; }
    private int listBottom() { return parent.height - 14; }

    private List<ZooNetworkPayloads.ZookeeperSyncData> sortedKeepers() {
        List<ZooNetworkPayloads.ZookeeperSyncData> list = new ArrayList<>(ZooTycoonClient.clientZookeeperList);
        list.sort(Comparator.comparing(ZooNetworkPayloads.ZookeeperSyncData::isOnStrike).reversed()
                .thenComparing(ZooNetworkPayloads.ZookeeperSyncData::isAssigned)
                .thenComparing(ZooNetworkPayloads.ZookeeperSyncData::name));
        return list;
    }

    private int maxScroll(int count) {
        int content = count * (ROW_H + ROW_GAP);
        return Math.max(0, content - (listBottom() - listTop()));
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        Minecraft mc = Minecraft.getInstance();
        List<ZooNetworkPayloads.ZookeeperSyncData> keepers = sortedKeepers();
        int px = panelX();
        int pw = panelW();

        long idle = keepers.stream().filter(k -> !k.isResigned() && !k.isAssigned()).count();
        long working = keepers.stream().filter(k -> !k.isResigned() && k.isAssigned()).count();
        graphics.drawString(mc.font, Component.literal(ZooTranslations.get("zootycoon.ui.staff.manage.summary", idle, working)),
                px, 54, 0xFFC9D1D9, false);
        StaffRecruitmentTab.renderWageRightAligned(graphics, mc, px + pw, 54, "zookeeper");

        if (keepers.isEmpty()) {
            graphics.drawCenteredString(mc.font, Component.literal(ZooTranslations.get("zootycoon.ui.staff.manage.empty")),
                    parent.width / 2, listTop() + 20, 0xFF8B949E);
            return;
        }

        scrollOffset = Math.clamp(scrollOffset, 0.0f, (float) maxScroll(keepers.size()));
        int top = listTop();
        int bottom = listBottom();
        graphics.enableScissor(px, top, px + pw, bottom);

        int y = top - (int) scrollOffset;
        for (var zk : keepers) {
            if (y + ROW_H >= top && y <= bottom) {
                drawRow(graphics, mc, zk, px, y, pw, mouseX, mouseY, top, bottom);
            }
            y += ROW_H + ROW_GAP;
        }
        graphics.disableScissor();
    }

    private void drawRow(GuiGraphics graphics, Minecraft mc, ZooNetworkPayloads.ZookeeperSyncData zk,
                         int x, int y, int w, int mouseX, int mouseY, int top, int bottom) {
        boolean onStrike = zk.isOnStrike();
        int borderCol = onStrike ? 0xFFE06C75 : 0xFF242A38;
        graphics.fill(x, y, x + w, y + ROW_H, 0xF20F1219);
        graphics.fill(x, y, x + w, y + 1, borderCol);
        graphics.fill(x, y + ROW_H - 1, x + w, y + ROW_H, borderCol);
        graphics.fill(x, y, x + 1, y + ROW_H, borderCol);
        graphics.fill(x + w - 1, y, x + w, y + ROW_H, borderCol);

        boolean busy = zk.isAssigned();
        boolean isCalling = zk.isCalling() || pendingCalledKeepers.contains(zk.uuid());
        if (!zk.isCalling()) {
            pendingCalledKeepers.remove(zk.uuid());
        }

        int nameX = x + 10;
        if (onStrike) {
            StaffRecruitmentTab.renderStrikeRowIcon(graphics, x + 8, y + 4);
            nameX = x + 21;
        }
        graphics.drawString(mc.font, Component.literal(zk.name()), nameX, y + 5, 0xFFE6EDF3, false);
        String status;
        int statusColor;
        if (zk.isResigned()) {
            status = ZooTranslations.get("zootycoon.ui.staff.manage.resigned", zk.owedAmount());
            statusColor = 0xFFFF6666;
        } else if (isCalling) {
            status = ZooTranslations.get("zootycoon.ui.staff.manage.calling");
            statusColor = 0xFFFFAA00;
        } else if (onStrike) {
            status = ZooTranslations.get("zootycoon.ui.staff.manage.on_strike");
            statusColor = 0xFFFF5555;
        } else if (busy) {
            status = ZooTranslations.get("zootycoon.ui.staff.manage.working");
            statusColor = 0xFF8B949E;
        } else {
            status = ZooTranslations.get("zootycoon.ui.staff.manage.idle");
            statusColor = 0xFF55FF55;
        }
        graphics.drawString(mc.font, Component.literal(status), x + 10, y + 17, statusColor, false);

        int by = y + (ROW_H - BTN_H) / 2;
        int fireX = x + w - FIRE_W - 8;
        int peekX = fireX - PEEK_W - 4;
        int callX = peekX - CALL_W - 4;
        int wageX = callX - StaffWageDropdownHelper.BADGE_W - 4;

        boolean activeStaff = !zk.isResigned();
        boolean confirming = activeStaff && zk.uuid().equals(confirmFireUuid) && System.currentTimeMillis() < confirmFireUntil;
        boolean canCall = activeStaff && (!busy || onStrike) && !isCalling;
        wageDropdown.drawIndicatorButton(graphics, mc, zk.uuid(), wageX, by,
                zk.dailyWage(), zk.missedDays(), zk.owedAmount(), zk.isResigned(),
                mouseX, mouseY, top, bottom);
        drawButton(graphics, mc, callX, by, CALL_W,
                ZooTranslations.get(isCalling ? "zootycoon.ui.staff.manage.btn_calling" : "zootycoon.ui.staff.manage.btn_call"),
                canCall, false, mouseX, mouseY, top, bottom);
        drawButton(graphics, mc, peekX, by, PEEK_W,
                ZooTranslations.get("zootycoon.ui.staff.manage.btn_peek"),
                activeStaff, false, mouseX, mouseY, top, bottom);
        drawButton(graphics, mc, fireX, by, FIRE_W,
                ZooTranslations.get(confirming ? "zootycoon.ui.staff.manage.btn_confirm" : "zootycoon.ui.staff.manage.btn_fire"),
                activeStaff, true, mouseX, mouseY, top, bottom);
    }

    public void renderOverlay(GuiGraphics graphics, int mouseX, int mouseY) {
        wageDropdown.renderOverlay(graphics, mouseX, mouseY);
    }

    private void drawButton(GuiGraphics graphics, Minecraft mc, int x, int y, int w, String label,
                            boolean enabled, boolean danger, int mouseX, int mouseY, int top, int bottom) {
        boolean hovered = enabled && mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + BTN_H
                && mouseY >= top && mouseY <= bottom;
        int bg = !enabled ? 0xFF101217 : (hovered ? (danger ? 0xFF2B181B : 0xFF252D3D) : 0xFF14171F);
        int border = !enabled ? 0xFF1C1F26 : (hovered ? (danger ? 0xFFE06C75 : 0xFFFFFFFF) : (danger ? 0xFF4A2528 : 0xFF3A4456));
        int text = !enabled ? 0xFF586069 : (hovered ? 0xFFFFFFFF : 0xFFD0D7DE);
        graphics.fill(x, y, x + w, y + BTN_H, bg);
        graphics.fill(x, y, x + w, y + 1, border);
        graphics.fill(x, y + BTN_H - 1, x + w, y + BTN_H, border);
        graphics.fill(x, y, x + 1, y + BTN_H, border);
        graphics.fill(x + w - 1, y, x + w, y + BTN_H, border);
        graphics.drawString(mc.font, Component.literal(label), x + (w - mc.font.width(label)) / 2, y + (BTN_H - 8) / 2, text, false);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return false;
        if (wageDropdown.isOpen()) {
            return wageDropdown.handleOverlayClick(mouseX, mouseY);
        }
        List<ZooNetworkPayloads.ZookeeperSyncData> keepers = sortedKeepers();
        int px = panelX();
        int pw = panelW();
        int top = listTop();
        int bottom = listBottom();
        if (mouseX < px || mouseX > px + pw || mouseY < top || mouseY > bottom) return false;

        int y = top - (int) scrollOffset;
        for (var zk : keepers) {
            int by = y + (ROW_H - BTN_H) / 2;
            int fireX = px + pw - FIRE_W - 8;
            int peekX = fireX - PEEK_W - 4;
            int callX = peekX - CALL_W - 4;
            int wageX = callX - StaffWageDropdownHelper.BADGE_W - 4;
            if (mouseY >= by && mouseY <= by + BTN_H) {
                if (wageDropdown.toggleIfClicked(zk.uuid(), wageX, by,
                        zk.dailyWage(), zk.missedDays(), zk.owedAmount(), zk.isResigned(), mouseX, mouseY)) {
                    return true;
                }
                if (zk.isResigned()) {
                    return false;
                }
                if (mouseX >= callX && mouseX <= callX + CALL_W) {
                    boolean isCalling = zk.isCalling() || pendingCalledKeepers.contains(zk.uuid());
                    if (zk.isAssigned() && !zk.isOnStrike()) {
                        ZooHudOverlay.showToast("zootycoon.toast.title.warning", "zootycoon.staff.manage.busy", ZooHudOverlay.TOAST_WARNING);
                    } else if (isCalling) {
                        // Already called and on the way, button is disabled
                    } else {
                        pendingCalledKeepers.add(zk.uuid());
                        ClientPlayNetworking.send(new ZooNetworkPayloads.ManageZookeeperPayload(zk.uuid(), 0));
                    }
                    return true;
                }
                if (mouseX >= peekX && mouseX <= peekX + PEEK_W) {
                    net.zootycoon.client.camera.StaffPeekCameraManager.startRequest("zookeeper", zk.uuid());
                    return true;
                }
                if (mouseX >= fireX && mouseX <= fireX + FIRE_W) {
                    if (zk.uuid().equals(confirmFireUuid) && System.currentTimeMillis() < confirmFireUntil) {
                        confirmFireUuid = null;
                        ClientPlayNetworking.send(new ZooNetworkPayloads.ManageZookeeperPayload(zk.uuid(), 1));
                    } else {
                        confirmFireUuid = zk.uuid();
                        confirmFireUntil = System.currentTimeMillis() + CONFIRM_MS;
                    }
                    return true;
                }
            }
            y += ROW_H + ROW_GAP;
        }
        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        wageDropdown.close();
        int max = maxScroll(ZooTycoonClient.clientZookeeperList.size());
        if (max <= 0) return false;
        scrollOffset = Math.clamp(scrollOffset - (float) verticalAmount * 24.0f, 0.0f, (float) max);
        return true;
    }
}


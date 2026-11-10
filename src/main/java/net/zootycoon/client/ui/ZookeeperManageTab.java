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
    private static final int CALL_W = 70;
    private static final int FIRE_W = 64;
    private static final long CONFIRM_MS = 3000L;

    private final MainManagementScreen parent;
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
        ClientPlayNetworking.send(new ZooNetworkPayloads.RequestAnimalSyncPayload());

        // Recruit button in the header, left of the emerald balance pill (same spot as the staff dropdown)
        Minecraft mc = Minecraft.getInstance();
        int emeralds = mc.player != null ? TransactionHelper.countEmeralds(mc.player) : 0;
        int pillW = 22 + screen.getFont().width(String.valueOf(emeralds)) + 10;
        int pillX = screen.width - pillW - 25;
        int btnW = 118;
        int cost = ZooEconomyEngine.getStaffHiringCost("zookeeper");
        Component label = Component.literal(ZooTranslations.get("zootycoon.ui.staff.manage.btn_recruit") + "  " + cost);
        screen.addWidgetPublic(new ZooButton(pillX - btnW - 8, 14, btnW, 22, label, b ->
                ClientPlayNetworking.send(new ZooNetworkPayloads.HireStaffPayload("zookeeper")))
                .setItemIcon(new ItemStack(Items.EMERALD))
                .asEmerald());
    }

    private int panelW() { return Math.min(parent.width - 30, 520); }
    private int panelX() { return (parent.width - panelW()) / 2; }
    private int listTop() { return 74; }
    private int listBottom() { return parent.height - 14; }

    private List<ZooNetworkPayloads.ZookeeperSyncData> sortedKeepers() {
        List<ZooNetworkPayloads.ZookeeperSyncData> list = new ArrayList<>(ZooTycoonClient.clientZookeeperList);
        list.sort(Comparator.comparing(ZooNetworkPayloads.ZookeeperSyncData::isAssigned)
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

        long idle = keepers.stream().filter(k -> !k.isAssigned()).count();
        long working = keepers.size() - idle;
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
        graphics.fill(x, y, x + w, y + ROW_H, 0xF20F1219);
        graphics.fill(x, y, x + w, y + 1, 0xFF242A38);
        graphics.fill(x, y + ROW_H - 1, x + w, y + ROW_H, 0xFF242A38);
        graphics.fill(x, y, x + 1, y + ROW_H, 0xFF242A38);
        graphics.fill(x + w - 1, y, x + w, y + ROW_H, 0xFF242A38);

        boolean busy = zk.isAssigned();
        boolean isCalling = zk.isCalling() || pendingCalledKeepers.contains(zk.uuid());
        if (!zk.isCalling()) {
            pendingCalledKeepers.remove(zk.uuid());
        }

        graphics.drawString(mc.font, Component.literal(zk.name()), x + 10, y + 5, 0xFFE6EDF3, false);
        String status;
        int statusColor;
        if (busy) {
            status = ZooTranslations.get("zootycoon.ui.staff.manage.working");
            statusColor = 0xFF8B949E;
        } else if (isCalling) {
            status = ZooTranslations.get("zootycoon.ui.staff.manage.calling");
            statusColor = 0xFFFFAA00;
        } else {
            status = ZooTranslations.get("zootycoon.ui.staff.manage.idle");
            statusColor = 0xFF55FF55;
        }
        graphics.drawString(mc.font, Component.literal(status), x + 10, y + 17, statusColor, false);

        int by = y + (ROW_H - BTN_H) / 2;
        int fireX = x + w - FIRE_W - 8;
        int callX = fireX - CALL_W - 6;

        boolean confirming = zk.uuid().equals(confirmFireUuid) && System.currentTimeMillis() < confirmFireUntil;
        boolean canCall = !busy && !isCalling;
        drawButton(graphics, mc, callX, by, CALL_W,
                ZooTranslations.get(isCalling ? "zootycoon.ui.staff.manage.btn_calling" : "zootycoon.ui.staff.manage.btn_call"),
                canCall, false, mouseX, mouseY, top, bottom);
        drawButton(graphics, mc, fireX, by, FIRE_W,
                ZooTranslations.get(confirming ? "zootycoon.ui.staff.manage.btn_confirm" : "zootycoon.ui.staff.manage.btn_fire"),
                true, true, mouseX, mouseY, top, bottom);
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
            int callX = fireX - CALL_W - 6;
            if (mouseY >= by && mouseY <= by + BTN_H) {
                if (mouseX >= callX && mouseX <= callX + CALL_W) {
                    boolean isCalling = zk.isCalling() || pendingCalledKeepers.contains(zk.uuid());
                    if (zk.isAssigned()) {
                        ZooHudOverlay.showToast("zootycoon.toast.title.warning", "zootycoon.staff.manage.busy", ZooHudOverlay.TOAST_WARNING);
                    } else if (isCalling) {
                        // Already called and on the way, button is disabled
                    } else {
                        pendingCalledKeepers.add(zk.uuid());
                        ClientPlayNetworking.send(new ZooNetworkPayloads.ManageZookeeperPayload(zk.uuid(), 0));
                    }
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
        int max = maxScroll(ZooTycoonClient.clientZookeeperList.size());
        if (max <= 0) return false;
        scrollOffset = Math.clamp(scrollOffset - (float) verticalAmount * 24.0f, 0.0f, (float) max);
        return true;
    }
}


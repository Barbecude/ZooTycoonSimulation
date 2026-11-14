package net.zootycoon.client.ui;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.zootycoon.client.ZooTycoonClient;
import net.zootycoon.network.ZooNetworkPayloads;
import net.zootycoon.util.ZooTranslations;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Sub view of the staff screen: list security guards, call them to player or fire them. */
public class SecurityManageTab {
    private static final int ROW_H = 30;
    private static final int ROW_GAP = 4;
    private static final int BTN_H = 18;
    private static final int CALL_W = 70;
    private static final int PEEK_W = 42;
    private static final int FIRE_W = 56;
    private static final long CONFIRM_MS = 3000L;

    private final MainManagementScreen parent;
    private final Set<UUID> pendingCalledSecurity = new HashSet<>();
    private float scrollOffset = 0.0f;
    private UUID confirmFireUuid = null;
    private long confirmFireUntil = 0L;

    public SecurityManageTab(MainManagementScreen parent) {
        this.parent = parent;
    }

    public void init(MainManagementScreen screen) {
        scrollOffset = 0.0f;
        confirmFireUuid = null;
        ClientPlayNetworking.send(new ZooNetworkPayloads.RequestAnimalSyncPayload());
    }

    private int panelW() { return Math.min(parent.width - 30, 520); }
    private int panelX() { return (parent.width - panelW()) / 2; }
    private int listTop() { return 74; }
    private int listBottom() { return parent.height - 14; }

    private List<ZooNetworkPayloads.SecuritySyncData> sortedSecurity() {
        List<ZooNetworkPayloads.SecuritySyncData> list = new ArrayList<>(ZooTycoonClient.clientSecurityList);
        list.sort(Comparator.comparing(ZooNetworkPayloads.SecuritySyncData::isOnStrike).reversed()
                .thenComparing(Comparator.comparing(ZooNetworkPayloads.SecuritySyncData::isAggro).reversed())
                .thenComparing(ZooNetworkPayloads.SecuritySyncData::name));
        return list;
    }

    private int maxScroll(int count) {
        int content = count * (ROW_H + ROW_GAP);
        return Math.max(0, content - (listBottom() - listTop()));
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        Minecraft mc = Minecraft.getInstance();
        List<ZooNetworkPayloads.SecuritySyncData> securityList = sortedSecurity();
        int px = panelX();
        int pw = panelW();

        long combat = securityList.stream().filter(ZooNetworkPayloads.SecuritySyncData::isAggro).count();
        long patrol = securityList.size() - combat;
        graphics.drawString(mc.font, Component.literal(ZooTranslations.get("zootycoon.ui.staff.manage.security_summary", securityList.size(), patrol, combat)),
                px, 54, 0xFFC9D1D9, false);
        StaffRecruitmentTab.renderWageRightAligned(graphics, mc, px + pw, 54, "security");

        if (securityList.isEmpty()) {
            graphics.drawCenteredString(mc.font, Component.literal(ZooTranslations.get("zootycoon.ui.staff.manage.security_empty")),
                    parent.width / 2, listTop() + 20, 0xFF8B949E);
            return;
        }

        scrollOffset = Math.clamp(scrollOffset, 0.0f, (float) maxScroll(securityList.size()));
        int top = listTop();
        int bottom = listBottom();
        graphics.enableScissor(px, top, px + pw, bottom);

        int y = top - (int) scrollOffset;
        for (var s : securityList) {
            if (y + ROW_H >= top && y <= bottom) {
                drawRow(graphics, mc, s, px, y, pw, mouseX, mouseY, top, bottom);
            }
            y += ROW_H + ROW_GAP;
        }
        graphics.disableScissor();
    }

    private void drawRow(GuiGraphics graphics, Minecraft mc, ZooNetworkPayloads.SecuritySyncData s,
                         int x, int y, int w, int mouseX, int mouseY, int top, int bottom) {
        boolean onStrike = s.isOnStrike();
        int borderCol = onStrike ? 0xFFE06C75 : 0xFF242A38;
        graphics.fill(x, y, x + w, y + ROW_H, 0xF20F1219);
        graphics.fill(x, y, x + w, y + 1, borderCol);
        graphics.fill(x, y + ROW_H - 1, x + w, y + ROW_H, borderCol);
        graphics.fill(x, y, x + 1, y + ROW_H, borderCol);
        graphics.fill(x + w - 1, y, x + w, y + ROW_H, borderCol);

        boolean isCalling = s.isCalling() || pendingCalledSecurity.contains(s.uuid());
        if (!s.isCalling()) {
            pendingCalledSecurity.remove(s.uuid());
        }

        int nameX = x + 10;
        if (onStrike) {
            StaffRecruitmentTab.renderStrikeRowIcon(graphics, x + 8, y + 4);
            nameX = x + 21;
        }
        graphics.drawString(mc.font, Component.literal(s.name()), nameX, y + 5, 0xFFE6EDF3, false);
        String status;
        int statusColor;
        if (isCalling) {
            status = ZooTranslations.get("zootycoon.ui.staff.manage.calling");
            statusColor = 0xFFFFAA00;
        } else if (onStrike) {
            status = ZooTranslations.get("zootycoon.ui.staff.manage.on_strike");
            statusColor = 0xFFFF5555;
        } else if (s.isAggro()) {
            status = ZooTranslations.get("zootycoon.ui.staff.manage.security_combat");
            statusColor = 0xFFFF5555;
        } else {
            status = ZooTranslations.get("zootycoon.ui.staff.manage.security_patrol");
            statusColor = 0xFF55FF55;
        }
        graphics.drawString(mc.font, Component.literal(status), x + 10, y + 17, statusColor, false);

        int by = y + (ROW_H - BTN_H) / 2;
        int fireX = x + w - FIRE_W - 8;
        int peekX = fireX - PEEK_W - 4;
        int callX = peekX - CALL_W - 4;

        boolean confirming = s.uuid().equals(confirmFireUuid) && System.currentTimeMillis() < confirmFireUntil;
        boolean canCall = !isCalling;
        drawButton(graphics, mc, callX, by, CALL_W,
                ZooTranslations.get(isCalling ? "zootycoon.ui.staff.manage.btn_calling" : "zootycoon.ui.staff.manage.btn_call"),
                canCall, false, mouseX, mouseY, top, bottom);
        drawButton(graphics, mc, peekX, by, PEEK_W,
                ZooTranslations.get("zootycoon.ui.staff.manage.btn_peek"),
                true, false, mouseX, mouseY, top, bottom);
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
        List<ZooNetworkPayloads.SecuritySyncData> securityList = sortedSecurity();
        int px = panelX();
        int pw = panelW();
        int top = listTop();
        int bottom = listBottom();
        if (mouseX < px || mouseX > px + pw || mouseY < top || mouseY > bottom) return false;

        int y = top - (int) scrollOffset;
        for (var s : securityList) {
            int by = y + (ROW_H - BTN_H) / 2;
            int fireX = px + pw - FIRE_W - 8;
            int peekX = fireX - PEEK_W - 4;
            int callX = peekX - CALL_W - 4;
            if (mouseY >= by && mouseY <= by + BTN_H) {
                if (mouseX >= callX && mouseX <= callX + CALL_W) {
                    boolean isCalling = s.isCalling() || pendingCalledSecurity.contains(s.uuid());
                    if (!isCalling) {
                        pendingCalledSecurity.add(s.uuid());
                        ClientPlayNetworking.send(new ZooNetworkPayloads.ManageStaffGenericPayload("security", s.uuid(), 0));
                    }
                    return true;
                }
                if (mouseX >= peekX && mouseX <= peekX + PEEK_W) {
                    net.zootycoon.client.camera.StaffPeekCameraManager.startRequest("security", s.uuid());
                    return true;
                }
                if (mouseX >= fireX && mouseX <= fireX + FIRE_W) {
                    if (s.uuid().equals(confirmFireUuid) && System.currentTimeMillis() < confirmFireUntil) {
                        confirmFireUuid = null;
                        ClientPlayNetworking.send(new ZooNetworkPayloads.ManageStaffGenericPayload("security", s.uuid(), 1));
                    } else {
                        confirmFireUuid = s.uuid();
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
        int max = maxScroll(ZooTycoonClient.clientSecurityList.size());
        if (max <= 0) return false;
        scrollOffset = Math.clamp(scrollOffset - (float) verticalAmount * 24.0f, 0.0f, (float) max);
        return true;
    }
}


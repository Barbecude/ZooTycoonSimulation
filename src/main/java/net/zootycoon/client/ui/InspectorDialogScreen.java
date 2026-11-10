package net.zootycoon.client.ui;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.zootycoon.entity.ZooInspectorEntity;
import net.zootycoon.license.LicenseTier;
import net.zootycoon.network.ZooNetworkPayloads;
import net.zootycoon.util.ZooTranslations;

import java.util.ArrayList;
import java.util.List;

public class InspectorDialogScreen extends EasyNpcDialogScreen {

    private final int inspectorEntityId;
    private final ZooInspectorEntity.InspectorPhase initialPhase;
    private final int tierLevel;
    private String currentSpecies;
    private int inspectedCount;
    private int totalCount;
    private final boolean passed;
    private boolean letterClaimed;
    private boolean licenseClaimed;
    private final List<String> inspectedSpecies;
    private final List<String> improvementNotes;

    private ZooButton claimLetterBtn;
    private ZooButton claimLicenseBtn;
    private ZooButton thankYouBtn;

    public InspectorDialogScreen(
            int inspectorEntityId,
            int phaseOrdinal,
            int tierLevel,
            String currentSpecies,
            int inspectedCount,
            int totalCount,
            boolean passed,
            boolean letterClaimed,
            boolean licenseClaimed,
            List<String> inspectedSpecies,
            List<String> improvementNotes
    ) {
        super(
                ZooTranslations.component("zootycoon.license.inspector.name"),
                ZooTranslations.component("zootycoon.license.inspector.name"),
                ZooTranslations.component("zootycoon.license.inspector.role"),
                buildSpeechComponent(phaseOrdinal, tierLevel, currentSpecies, inspectedCount, totalCount, passed),
                resolveClientEntity(inspectorEntityId)
        );
        this.inspectorEntityId = inspectorEntityId;
        ZooInspectorEntity.InspectorPhase[] vals = ZooInspectorEntity.InspectorPhase.values();
        this.initialPhase = (phaseOrdinal >= 0 && phaseOrdinal < vals.length)
                ? vals[phaseOrdinal]
                : ZooInspectorEntity.InspectorPhase.WAITING_CONFIRMATION;
        this.tierLevel = Math.clamp(tierLevel, 1, 5);
        this.currentSpecies = currentSpecies != null ? currentSpecies : "";
        this.inspectedCount = inspectedCount;
        this.totalCount = Math.max(1, totalCount);
        this.passed = passed;
        this.letterClaimed = letterClaimed;
        this.licenseClaimed = licenseClaimed;
        this.inspectedSpecies = inspectedSpecies != null ? new ArrayList<>(inspectedSpecies) : new ArrayList<>();
        this.improvementNotes = improvementNotes != null ? new ArrayList<>(improvementNotes) : new ArrayList<>();
    }

    private static LivingEntity resolveClientEntity(int id) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            Entity e = mc.level.getEntity(id);
            if (e instanceof LivingEntity le) {
                return le;
            }
        }
        return null;
    }

    private static Component buildSpeechComponent(int phaseOrd, int tierLevel, String species, int done, int total, boolean passed) {
        LicenseTier tier = LicenseTier.byLevel(tierLevel);
        ZooInspectorEntity.InspectorPhase[] vals = ZooInspectorEntity.InspectorPhase.values();
        ZooInspectorEntity.InspectorPhase phase = (phaseOrd >= 0 && phaseOrd < vals.length)
                ? vals[phaseOrd]
                : ZooInspectorEntity.InspectorPhase.WAITING_CONFIRMATION;

        if (phase == ZooInspectorEntity.InspectorPhase.WAITING_CONFIRMATION
                || phase == ZooInspectorEntity.InspectorPhase.ARRIVING_AT_GATE) {
            return Component.literal(ZooTranslations.get("zootycoon.license.dialog.confirm", tier.getDisplayName()));
        } else if (phase == ZooInspectorEntity.InspectorPhase.WAITING_RESULT_CLAIM) {
            if (passed) {
                return Component.literal(ZooTranslations.get("zootycoon.license.dialog.passed"));
            } else {
                return Component.literal(ZooTranslations.get("zootycoon.license.dialog.failed"));
            }
        } else {
            int rem = Math.max(0, total - done);
            if (species != null && !species.isEmpty()) {
                return Component.literal(ZooTranslations.get("zootycoon.license.dialog.progress_active", species, rem));
            } else {
                return Component.literal(ZooTranslations.get("zootycoon.license.dialog.progress_next", rem));
            }
        }
    }

    @Override
    protected int getContentHeight() {
        if (initialPhase == ZooInspectorEntity.InspectorPhase.WALKING_TO_ANIMAL
                || initialPhase == ZooInspectorEntity.InspectorPhase.WRITING_NOTES
                || initialPhase == ZooInspectorEntity.InspectorPhase.OBSERVING_ANIMAL
                || initialPhase == ZooInspectorEntity.InspectorPhase.APPROACHING_PLAYER) {
            return 58;
        }
        if (initialPhase == ZooInspectorEntity.InspectorPhase.WAITING_RESULT_CLAIM) {
            return passed ? 26 : 56;
        }
        return 0;
    }

    @Override
    protected void initDialogWidgets() {
        int btnY = startY + panelHeight - 32;
        int rightX = startX + 128;
        int rightW = panelWidth - 140;

        if (initialPhase == ZooInspectorEntity.InspectorPhase.WAITING_CONFIRMATION
                || initialPhase == ZooInspectorEntity.InspectorPhase.ARRIVING_AT_GATE) {
            int btnW = (rightW - 8) / 2;
            this.addRenderableWidget(new ZooButton(rightX, btnY, btnW, 22, ZooTranslations.component("zootycoon.license.btn.start"), b -> {
                ClientPlayNetworking.send(new ZooNetworkPayloads.InspectorActionPayload(inspectorEntityId, "START_INSPECTION"));
                this.onClose();
            }).asEmerald());

            this.addRenderableWidget(new ZooButton(rightX + btnW + 8, btnY, btnW, 22, ZooTranslations.component("zootycoon.license.btn.cancel"), b -> {
                ClientPlayNetworking.send(new ZooNetworkPayloads.InspectorActionPayload(inspectorEntityId, "CANCEL_INSPECTION"));
                this.onClose();
            }).asDanger());
        } else if (initialPhase == ZooInspectorEntity.InspectorPhase.WAITING_RESULT_CLAIM) {
            if (passed) {
                int gap = 5;
                int btnW = (rightW - gap * 2) / 3;
                int btnW3 = rightW - (btnW * 2 + gap * 2);

                String letterLabel = ZooTranslations.get(letterClaimed ? "zootycoon.license.btn.letter_claimed" : "zootycoon.license.btn.claim_letter");
                this.claimLetterBtn = new ZooButton(rightX, btnY, btnW, 22, Component.literal(letterLabel), b -> {
                    if (!this.letterClaimed) {
                        this.letterClaimed = true;
                        ClientPlayNetworking.send(new ZooNetworkPayloads.InspectorActionPayload(inspectorEntityId, "CLAIM_LETTER"));
                        refreshClaimButtonsState();
                    }
                }).asPrimary();

                String licLabel = ZooTranslations.get(licenseClaimed ? "zootycoon.license.btn.license_claimed" : "zootycoon.license.btn.claim_license");
                this.claimLicenseBtn = new ZooButton(rightX + btnW + gap, btnY, btnW, 22, Component.literal(licLabel), b -> {
                    if (!this.licenseClaimed) {
                        this.licenseClaimed = true;
                        ClientPlayNetworking.send(new ZooNetworkPayloads.InspectorActionPayload(inspectorEntityId, "CLAIM_LICENSE"));
                        refreshClaimButtonsState();
                    }
                }).asGold();

                this.thankYouBtn = new ZooButton(rightX + (btnW + gap) * 2, btnY, btnW3, 22, ZooTranslations.component("zootycoon.license.btn.thank_you"), b -> {
                    if (this.letterClaimed && this.licenseClaimed) {
                        ClientPlayNetworking.send(new ZooNetworkPayloads.InspectorActionPayload(inspectorEntityId, "THANK_YOU_FINISH"));
                        this.onClose();
                    }
                }).asEmerald();

                refreshClaimButtonsState();
                this.addRenderableWidget(this.claimLetterBtn);
                this.addRenderableWidget(this.claimLicenseBtn);
                this.addRenderableWidget(this.thankYouBtn);
            } else {
                // PERLU PERBAIKAN: Only Ambil Surat & Terima Kasih (NO Ambil Lisensi button)
                int btnW = (rightW - 8) / 2;
                String letterLabel = ZooTranslations.get(letterClaimed ? "zootycoon.license.btn.letter_claimed" : "zootycoon.license.btn.claim_letter");
                this.claimLetterBtn = new ZooButton(rightX, btnY, btnW, 22, Component.literal(letterLabel), b -> {
                    if (!this.letterClaimed) {
                        this.letterClaimed = true;
                        ClientPlayNetworking.send(new ZooNetworkPayloads.InspectorActionPayload(inspectorEntityId, "CLAIM_LETTER"));
                        refreshClaimButtonsState();
                    }
                }).asPrimary();

                this.thankYouBtn = new ZooButton(rightX + btnW + 8, btnY, btnW, 22, ZooTranslations.component("zootycoon.license.btn.thank_you"), b -> {
                    if (this.letterClaimed) {
                        ClientPlayNetworking.send(new ZooNetworkPayloads.InspectorActionPayload(inspectorEntityId, "THANK_YOU_FINISH"));
                        this.onClose();
                    }
                }).asEmerald();

                refreshClaimButtonsState();
                this.addRenderableWidget(this.claimLetterBtn);
                this.addRenderableWidget(this.thankYouBtn);
            }
        } else {
            // Inspecting in progress -> Close button
            this.addRenderableWidget(new ZooButton(rightX, btnY, rightW, 22, ZooTranslations.component("zootycoon.dialog.btn.close"), b -> this.onClose()).asPrimary());
        }
    }

    private void refreshClaimButtonsState() {
        if (this.claimLetterBtn != null) {
            this.claimLetterBtn.active = !this.letterClaimed;
            this.claimLetterBtn.setMessage(ZooTranslations.component(this.letterClaimed ? "zootycoon.license.btn.letter_claimed" : "zootycoon.license.btn.claim_letter"));
        }
        if (this.claimLicenseBtn != null) {
            this.claimLicenseBtn.active = !this.licenseClaimed;
            this.claimLicenseBtn.setMessage(ZooTranslations.component(this.licenseClaimed ? "zootycoon.license.btn.license_claimed" : "zootycoon.license.btn.claim_license"));
        }
        if (this.thankYouBtn != null) {
            this.thankYouBtn.active = passed ? (this.letterClaimed && this.licenseClaimed) : this.letterClaimed;
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        // Sync live progress from entity in world if currently inspecting
        if (this.previewEntity instanceof ZooInspectorEntity liveInspector) {
            ZooInspectorEntity.InspectorPhase livePhase = liveInspector.getPhase();
            if (livePhase == ZooInspectorEntity.InspectorPhase.WALKING_TO_ANIMAL
                    || livePhase == ZooInspectorEntity.InspectorPhase.WRITING_NOTES
                    || livePhase == ZooInspectorEntity.InspectorPhase.OBSERVING_ANIMAL) {
                this.currentSpecies = liveInspector.getCurrentSpeciesName();
                this.inspectedCount = liveInspector.getInspectedCount();
                this.totalCount = liveInspector.getTotalCount();
                this.dialogMessage = buildSpeechComponent(livePhase.ordinal(), tierLevel, currentSpecies, inspectedCount, totalCount, passed);
            }
        }

        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    protected void renderDialogContent(GuiGraphics graphics, int contentX, int contentY, int contentW, int contentH, int mouseX, int mouseY, float delta) {
        if (initialPhase == ZooInspectorEntity.InspectorPhase.WALKING_TO_ANIMAL
                || initialPhase == ZooInspectorEntity.InspectorPhase.WRITING_NOTES
                || initialPhase == ZooInspectorEntity.InspectorPhase.OBSERVING_ANIMAL
                || initialPhase == ZooInspectorEntity.InspectorPhase.APPROACHING_PLAYER) {
            renderCircularProgressSection(graphics, contentX, contentY, contentW);
        } else if (initialPhase == ZooInspectorEntity.InspectorPhase.WAITING_RESULT_CLAIM) {
            renderResultBadgeSection(graphics, contentX, contentY, contentW);
        }
    }

    private void renderCircularProgressSection(GuiGraphics graphics, int x, int y, int width) {
        graphics.fill(x, y, x + width, y + 52, 0xFF161B26);
        graphics.fill(x, y, x + width, y + 1, 0xFF283142);

        float ratio = Mth.clamp((float) inspectedCount / (float) Math.max(1, totalCount), 0.0f, 1.0f);
        int pct = Math.round(ratio * 100.0f);

        int cx = x + 28;
        int cy = y + 26;
        int radius = 18;

        // Draw circular progress ring using 36 radial 2x2 segments
        int segments = 36;
        int filledSegments = Math.round(ratio * segments);
        for (int i = 0; i < segments; i++) {
            double angle = Math.toRadians(i * (360.0 / segments) - 90.0);
            int px = cx + (int) Math.round(Math.cos(angle) * radius);
            int py = cy + (int) Math.round(Math.sin(angle) * radius);
            int color = (i < filledSegments) ? 0xFF38BDF8 : 0xFF2A3244;
            graphics.fill(px - 1, py - 1, px + 2, py + 2, color);
        }

        String pctStr = pct + "%";
        graphics.drawCenteredString(this.font, pctStr, cx + 1, cy - 4, 0xFFFFFFFF);

        int infoX = cx + 28;
        LicenseTier tier = LicenseTier.byLevel(tierLevel);
        graphics.drawString(this.font, tier.getDisplayName(), infoX, y + 8, 0xFFFFD700, false);

        String targetLabel = (currentSpecies != null && !currentSpecies.isEmpty())
                ? ZooTranslations.get("zootycoon.license.progress.current_target", currentSpecies)
                : ZooTranslations.get("zootycoon.license.progress.moving");
        graphics.drawString(this.font, targetLabel, infoX, y + 21, 0xFF81E6D9, false);

        int remaining = Math.max(0, totalCount - inspectedCount);
        String remLabel = ZooTranslations.get("zootycoon.license.progress.remaining", inspectedCount, totalCount, remaining);
        graphics.drawString(this.font, remLabel, infoX, y + 34, 0xFFA0AEC0, false);
    }

    private void renderResultBadgeSection(GuiGraphics graphics, int x, int y, int width) {
        String statusText = ZooTranslations.get(passed ? "zootycoon.license.status.passed" : "zootycoon.license.status.failed");
        int bg = passed ? 0xFF143828 : 0xFF3E181E;
        int fg = passed ? 0xFF4ADE80 : 0xFFF87171;

        graphics.fill(x, y, x + width, y + 18, bg);
        graphics.drawCenteredString(this.font, statusText, x + width / 2, y + 5, fg);

        if (!passed && !improvementNotes.isEmpty()) {
            int issueY = y + 22;
            int maxShow = Math.min(2, improvementNotes.size());
            for (int i = 0; i < maxShow; i++) {
                String issue = this.font.plainSubstrByWidth("- " + improvementNotes.get(i), width - 6);
                graphics.drawString(this.font, issue, x + 2, issueY, 0xFFFCA5A5, false);
                issueY += 11;
            }
            if (improvementNotes.size() > 2) {
                String more = ZooTranslations.get("zootycoon.license.dialog.more_issues", improvementNotes.size() - 2);
                graphics.drawString(this.font, more, x + 2, issueY, 0xFF9CA3AF, false);
            }
        }
    }
}

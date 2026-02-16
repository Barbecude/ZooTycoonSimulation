package com.example.simulation;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class AnimalNamingScreen extends Screen {
    private final int entityId;
    private EditBox nameInput;

    public AnimalNamingScreen(int entityId) {
        super(Component.literal("Naming Animal"));
        this.entityId = entityId;
    }

    @Override
    protected void init() {
        super.init();
        int width = this.width;
        int height = this.height;

        this.nameInput = new EditBox(this.font, width / 2 - 100, height / 2 - 20, 200, 20,
                Component.literal("Animal Name"));
        this.nameInput.setMaxLength(32);
        this.nameInput.setCanLoseFocus(false);
        this.addWidget(this.nameInput);
        this.setInitialFocus(this.nameInput);

        // Submit Button
        this.addRenderableWidget(Button.builder(Component.literal("Tag Animal"), (btn) -> {
            submitName(nameInput.getValue());
        }).bounds(width / 2 - 50, height / 2 + 10, 100, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        guiGraphics.drawCenteredString(this.font, "Enter Animal Name", this.width / 2, this.height / 2 - 40, 0xFFFFFF);
        this.nameInput.render(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void submitName(String name) {
        if (name.isEmpty())
            name = "Unnamed Animal";
        PacketHandler.INSTANCE.sendToServer(new TagAnimalPacket(entityId, name));
        this.onClose();
    }
}

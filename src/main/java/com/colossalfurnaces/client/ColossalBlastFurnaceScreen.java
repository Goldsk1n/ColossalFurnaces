package com.colossalfurnaces.client;

import com.colossalfurnaces.ColossalFurnacesMod;
import com.colossalfurnaces.menu.ColossalBlastFurnaceMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class ColossalBlastFurnaceScreen extends AbstractContainerScreen<ColossalBlastFurnaceMenu> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(ColossalFurnacesMod.MOD_ID, "textures/gui/colossal_furnace.png");
    private static final Identifier INPUT_SLOT_OVERLAY = Identifier.fromNamespaceAndPath(ColossalFurnacesMod.MOD_ID, "textures/gui/slot_overlay_input.png");
    private static final Identifier FUEL_SLOT_OVERLAY = Identifier.fromNamespaceAndPath(ColossalFurnacesMod.MOD_ID, "textures/gui/slot_overlay_fuel.png");
    private static final Identifier OUTPUT_SLOT_OVERLAY = Identifier.fromNamespaceAndPath(ColossalFurnacesMod.MOD_ID, "textures/gui/slot_overlay_output.png");
    private static final int[][] INPUT_SLOT_POSITIONS = {
            {16, 18},
            {34, 18},
            {52, 18},
            {16, 36},
            {34, 36},
            {52, 36},
            {16, 54},
            {34, 54},
            {52, 54}
    };
    private static final int[][] FUEL_SLOT_POSITIONS = {
            {16, 90},
            {34, 90},
            {52, 90}
    };
    private static final int[][] OUTPUT_SLOT_POSITIONS = {
            {108, 36},
            {126, 36},
            {144, 36},
            {108, 54},
            {126, 54},
            {144, 54},
            {108, 72},
            {126, 72},
            {144, 72}
    };
    private static final int FLAME_X = 34;
    private static final int FLAME_Y = 73;
    private static final int FLAME_WIDTH = 14;
    private static final int FLAME_HEIGHT = 14;
    private static final int FLAME_TEXTURE_X = 176;
    private static final int FLAME_TEXTURE_Y = 0;
    private static final int ARROW_X = 75;
    private static final int ARROW_Y = 54;
    private static final int ARROW_WIDTH = 24;
    private static final int ARROW_HEIGHT = 16;
    private static final int ARROW_TEXTURE_X = 176;
    private static final int ARROW_TEXTURE_Y = 14;

    public ColossalBlastFurnaceScreen(ColossalBlastFurnaceMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 205);
        this.inventoryLabelY = 110;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
        this.inventoryLabelX = 8;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        int left = this.leftPos;
        int top = this.topPos;
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, left, top, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
        this.renderSlotOverlays(graphics, left, top);

        if (this.menu.isLit()) {
            int litProgress = this.menu.getLitProgress();
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, left + FLAME_X, top + FLAME_Y + 12 - litProgress, FLAME_TEXTURE_X, FLAME_TEXTURE_Y + 12 - litProgress, FLAME_WIDTH, litProgress + 1, 256, 256);
        }

        int burnProgress = this.menu.getBurnProgress();
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, left + ARROW_X, top + ARROW_Y, ARROW_TEXTURE_X, ARROW_TEXTURE_Y, burnProgress + 1, ARROW_HEIGHT, 256, 256);
    }

    private void renderSlotOverlays(GuiGraphicsExtractor graphics, int left, int top) {
        this.renderSlotOverlayGroup(graphics, left, top, INPUT_SLOT_POSITIONS, INPUT_SLOT_OVERLAY);
        this.renderSlotOverlayGroup(graphics, left, top, FUEL_SLOT_POSITIONS, FUEL_SLOT_OVERLAY);
        this.renderSlotOverlayGroup(graphics, left, top, OUTPUT_SLOT_POSITIONS, OUTPUT_SLOT_OVERLAY);
    }

    private void renderSlotOverlayGroup(GuiGraphicsExtractor graphics, int left, int top, int[][] positions, Identifier texture) {
        for (int[] position : positions) {
            this.renderSlotOverlay(graphics, left + position[0], top + position[1], texture);
        }
    }

    private void renderSlotOverlay(GuiGraphicsExtractor graphics, int x, int y, Identifier texture) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x - 1, y - 1, 0, 0, 18, 18, 18, 18);
    }
}

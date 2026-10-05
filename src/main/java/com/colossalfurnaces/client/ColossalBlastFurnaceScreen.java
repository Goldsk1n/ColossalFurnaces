package com.colossalfurnaces.client;

import com.colossalfurnaces.ColossalFurnacesMod;
import com.colossalfurnaces.menu.ColossalBlastFurnaceMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ColossalBlastFurnaceScreen extends AbstractContainerScreen<ColossalBlastFurnaceMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(ColossalFurnacesMod.MOD_ID, "textures/gui/colossal_furnace.png");
    private static final ResourceLocation INPUT_SLOT_OVERLAY = ResourceLocation.fromNamespaceAndPath(ColossalFurnacesMod.MOD_ID, "textures/gui/slot_overlay_input.png");
    private static final ResourceLocation FUEL_SLOT_OVERLAY = ResourceLocation.fromNamespaceAndPath(ColossalFurnacesMod.MOD_ID, "textures/gui/slot_overlay_fuel.png");
    private static final ResourceLocation OUTPUT_SLOT_OVERLAY = ResourceLocation.fromNamespaceAndPath(ColossalFurnacesMod.MOD_ID, "textures/gui/slot_overlay_output.png");
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
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 205;
        this.inventoryLabelY = 110;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
        this.inventoryLabelX = 8;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int left = this.leftPos;
        int top = this.topPos;
        graphics.blit(TEXTURE, left, top, 0, 0, this.imageWidth, this.imageHeight);
        this.renderSlotOverlays(graphics, left, top);

        if (this.menu.isLit()) {
            int litProgress = this.menu.getLitProgress();
            graphics.blit(TEXTURE, left + FLAME_X, top + FLAME_Y + 12 - litProgress, FLAME_TEXTURE_X, FLAME_TEXTURE_Y + 12 - litProgress, FLAME_WIDTH, litProgress + 1);
        }

        int burnProgress = this.menu.getBurnProgress();
        graphics.blit(TEXTURE, left + ARROW_X, top + ARROW_Y, ARROW_TEXTURE_X, ARROW_TEXTURE_Y, burnProgress + 1, ARROW_HEIGHT);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x404040, false);
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);
    }

    private void renderSlotOverlays(GuiGraphics graphics, int left, int top) {
        this.renderSlotOverlayGroup(graphics, left, top, INPUT_SLOT_POSITIONS, INPUT_SLOT_OVERLAY);
        this.renderSlotOverlayGroup(graphics, left, top, FUEL_SLOT_POSITIONS, FUEL_SLOT_OVERLAY);
        this.renderSlotOverlayGroup(graphics, left, top, OUTPUT_SLOT_POSITIONS, OUTPUT_SLOT_OVERLAY);
    }

    private void renderSlotOverlayGroup(GuiGraphics graphics, int left, int top, int[][] positions, ResourceLocation texture) {
        for (int[] position : positions) {
            this.renderSlotOverlay(graphics, left + position[0], top + position[1], texture);
        }
    }

    private void renderSlotOverlay(GuiGraphics graphics, int x, int y, ResourceLocation texture) {
        graphics.blit(texture, x - 1, y - 1, 0, 0, 18, 18, 18, 18);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
}

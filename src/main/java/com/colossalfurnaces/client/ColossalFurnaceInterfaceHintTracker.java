package com.colossalfurnaces.client;

import com.colossalfurnaces.ColossalFurnacesMod;
import com.colossalfurnaces.block.ColossalFurnaceInterfaceBlock;
import com.colossalfurnaces.blockentity.ColossalFurnaceInterfaceBlockEntity;
import com.colossalfurnaces.blockentity.ColossalFurnaceInterfaceMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = ColossalFurnacesMod.MOD_ID, value = Dist.CLIENT)
public final class ColossalFurnaceInterfaceHintTracker {
    private static final long FIRST_HINT_DELAY = 12L;
    private static final long LOOK_WINDOW = 80L;
    private static final int BOX_PADDING_X = 6;
    private static final int BOX_PADDING_Y = 4;
    private static final int BOX_SPACING_Y = 2;
    private static final int BOTTOM_OFFSET = 64;
    private static long suppressUntilGameTime = 0L;
    private static long modeChangeUntilGameTime = 0L;
    private static ColossalFurnaceInterfaceMode modeChangeMode = ColossalFurnaceInterfaceMode.UNIVERSAL;
    private static BlockPos focusedInterfacePos = null;
    private static long focusStartGameTime = -1L;

    private ColossalFurnaceInterfaceHintTracker() {
    }

    public static void suppressForTicks(long ticks) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        suppressUntilGameTime = Math.max(suppressUntilGameTime, minecraft.level.getGameTime() + ticks);
    }

    public static void showModeChanged(String modeName, long ticks) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        modeChangeMode = ColossalFurnaceInterfaceMode.byName(modeName);
        modeChangeUntilGameTime = minecraft.level.getGameTime() + ticks;
        suppressUntilGameTime = Math.max(suppressUntilGameTime, modeChangeUntilGameTime);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || minecraft.screen != null) {
            clearFocus();
            return;
        }

        HitResult hitResult = minecraft.hitResult;
        if (!(hitResult instanceof BlockHitResult blockHitResult) || hitResult.getType() != HitResult.Type.BLOCK) {
            clearFocus();
            return;
        }

        BlockPos hitPos = blockHitResult.getBlockPos();
        if (!minecraft.level.getBlockState(hitPos).hasProperty(ColossalFurnaceInterfaceBlock.FORMED)
                || !minecraft.level.getBlockState(hitPos).getValue(ColossalFurnaceInterfaceBlock.FORMED)
                || !(minecraft.level.getBlockEntity(hitPos) instanceof ColossalFurnaceInterfaceBlockEntity interfaceBlockEntity)) {
            clearFocus();
            return;
        }

        long gameTime = minecraft.level.getGameTime();
        if (!hitPos.equals(focusedInterfacePos)) {
            focusedInterfacePos = hitPos.immutable();
            focusStartGameTime = gameTime;
        }
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.screen != null) {
            return;
        }

        long gameTime = minecraft.level.getGameTime();
        if (gameTime < modeChangeUntilGameTime) {
            float alphaFactor = Mth.clamp((modeChangeUntilGameTime - gameTime) / 8.0F, 0.0F, 1.0F);
            renderHintBox(
                    event.getGuiGraphics(),
                    minecraft,
                    Component.translatable("message.colossalfurnaces.interface.mode_changed_title"),
                    Component.translatable("message.colossalfurnaces.interface.current_mode"),
                    modeChangeMode.createDisplayComponent(),
                    modeChangeMode.getTextColor(),
                    alphaFactor
            );
            return;
        }

        ColossalFurnaceInterfaceBlockEntity interfaceBlockEntity = getFocusedInterface(minecraft);
        if (interfaceBlockEntity == null) {
            return;
        }

        long focusTicks = gameTime - focusStartGameTime;
        if (gameTime < suppressUntilGameTime || focusTicks < FIRST_HINT_DELAY || focusTicks > LOOK_WINDOW) {
            return;
        }

        float fadeIn = Mth.clamp((focusTicks - FIRST_HINT_DELAY + 1) / 8.0F, 0.0F, 1.0F);
        float fadeOut = Mth.clamp((LOOK_WINDOW - focusTicks + 1) / 10.0F, 0.0F, 1.0F);
        float alphaFactor = Math.min(fadeIn, fadeOut);
        if (alphaFactor <= 0.0F) {
            return;
        }

        Component line1 = Component.translatable("message.colossalfurnaces.interface.mode_hint");
        Component line2Label = Component.translatable("message.colossalfurnaces.interface.current_mode");
        Component line2Mode = interfaceBlockEntity.getMode().createDisplayComponent();

        renderHintBox(event.getGuiGraphics(), minecraft, line1, line2Label, line2Mode, interfaceBlockEntity.getMode().getTextColor(), alphaFactor);
    }

    private static void renderHintBox(GuiGraphics graphics, Minecraft minecraft, Component line1, Component line2Label, Component line2Mode, int modeColor, float alphaFactor) {
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();
        int line1Width = minecraft.font.width(line1);
        int line2LabelWidth = minecraft.font.width(line2Label);
        int line2ModeWidth = minecraft.font.width(line2Mode);
        int line2Width = line2LabelWidth + 4 + line2ModeWidth;
        int contentWidth = Math.max(line1Width, line2Width);
        int boxWidth = contentWidth + BOX_PADDING_X * 2;
        int boxHeight = minecraft.font.lineHeight * 2 + BOX_PADDING_Y * 2 + BOX_SPACING_Y;
        int x = (screenWidth - boxWidth) / 2;
        int y = screenHeight - BOTTOM_OFFSET - boxHeight;

        int backgroundAlpha = Mth.clamp((int) (alphaFactor * 170.0F), 0, 255);
        int borderAlpha = Mth.clamp((int) (alphaFactor * 220.0F), 0, 255);
        int textAlpha = Mth.clamp((int) (alphaFactor * 255.0F), 0, 255);
        int backgroundColor = (backgroundAlpha << 24);
        int borderColor = (borderAlpha << 24) | 0xD0D0D0;
        int textColor = (textAlpha << 24) | 0xE0E0E0;

        graphics.fill(x, y, x + boxWidth, y + boxHeight, backgroundColor);
        graphics.fill(x, y, x + boxWidth, y + 1, borderColor);
        graphics.fill(x, y + boxHeight - 1, x + boxWidth, y + boxHeight, borderColor);
        graphics.fill(x, y, x + 1, y + boxHeight, borderColor);
        graphics.fill(x + boxWidth - 1, y, x + boxWidth, y + boxHeight, borderColor);

        int line1X = x + (boxWidth - line1Width) / 2;
        int line2X = x + (boxWidth - line2Width) / 2;
        int line1Y = y + BOX_PADDING_Y;
        int line2Y = line1Y + minecraft.font.lineHeight + BOX_SPACING_Y;

        graphics.drawString(minecraft.font, line1, line1X, line1Y, textColor, false);
        graphics.drawString(minecraft.font, line2Label, line2X, line2Y, textColor, false);
        graphics.drawString(minecraft.font, line2Mode, line2X + line2LabelWidth + 4, line2Y, (textAlpha << 24) | (modeColor & 0x00FFFFFF), false);
    }

    private static ColossalFurnaceInterfaceBlockEntity getFocusedInterface(Minecraft minecraft) {
        if (minecraft.player == null || minecraft.level == null || focusedInterfacePos == null) {
            return null;
        }
        if (!(minecraft.level.getBlockEntity(focusedInterfacePos) instanceof ColossalFurnaceInterfaceBlockEntity interfaceBlockEntity)) {
            return null;
        }
        return interfaceBlockEntity;
    }

    private static void clearFocus() {
        focusedInterfacePos = null;
        focusStartGameTime = -1L;
    }
}

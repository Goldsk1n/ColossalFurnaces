package com.colossalfurnaces.blockentity;

import net.minecraft.network.chat.Component;

public enum ColossalFurnaceInterfaceMode {
    UNIVERSAL("universal", 0xFFACACAC),
    INPUT("input", 0xFFFFD700),
    FUEL("fuel", 0xFFDD0000),
    OUTPUT("output", 0xFF00BB00);

    private final String serializedName;
    private final int textColor;

    ColossalFurnaceInterfaceMode(String serializedName, int textColor) {
        this.serializedName = serializedName;
        this.textColor = textColor;
    }

    public String getSerializedName() {
        return this.serializedName;
    }

    public int getTextColor() {
        return this.textColor;
    }

    public Component createDisplayComponent() {
        return Component.translatable("message.colossalfurnaces.interface.mode." + this.serializedName)
                .withStyle(style -> style.withColor(this.textColor));
    }

    public ColossalFurnaceInterfaceMode next() {
        return switch (this) {
            case UNIVERSAL -> INPUT;
            case INPUT -> FUEL;
            case FUEL -> OUTPUT;
            case OUTPUT -> UNIVERSAL;
        };
    }

    public static ColossalFurnaceInterfaceMode byName(String name) {
        for (ColossalFurnaceInterfaceMode mode : values()) {
            if (mode.serializedName.equals(name)) {
                return mode;
            }
        }
        return UNIVERSAL;
    }
}

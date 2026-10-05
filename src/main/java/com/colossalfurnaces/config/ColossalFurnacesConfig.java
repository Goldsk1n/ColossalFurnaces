package com.colossalfurnaces.config;

import com.colossalfurnaces.ColossalFurnacesMod;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = ColossalFurnacesMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ColossalFurnacesConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.IntValue MAX_COLOSSAL_FURNACE_SIZE = BUILDER
            .comment("Maximum outer cubic size for a colossal furnace.")
            .defineInRange("maxColossalFurnaceSize", 5, 2, 5);

    public static final ForgeConfigSpec.BooleanValue AUTO_REVALIDATE_STRUCTURE = BUILDER
            .comment("Revalidate formed colossal furnaces when shell blocks change.")
            .define("autoRevalidateStructure", true);

    public static final ForgeConfigSpec.BooleanValue SHOW_STRUCTURE_ERROR_MESSAGES = BUILDER
            .comment("Show player-facing structure validation errors.")
            .define("showStructureErrorMessages", true);

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    public static int maxColossalFurnaceSize;
    public static boolean autoRevalidateStructure;
    public static boolean showStructureErrorMessages;

    private ColossalFurnacesConfig() {
    }

    @SubscribeEvent
    public static void onLoad(ModConfigEvent event) {
        maxColossalFurnaceSize = MAX_COLOSSAL_FURNACE_SIZE.get();
        autoRevalidateStructure = AUTO_REVALIDATE_STRUCTURE.get();
        showStructureErrorMessages = SHOW_STRUCTURE_ERROR_MESSAGES.get();
    }
}

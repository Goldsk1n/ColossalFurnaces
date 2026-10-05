package com.colossalfurnaces.config;

import com.colossalfurnaces.ColossalFurnacesMod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = ColossalFurnacesMod.MOD_ID)
public final class ColossalFurnacesConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue MAX_COLOSSAL_FURNACE_SIZE = BUILDER
            .comment("Maximum outer cubic size for a colossal furnace.")
            .defineInRange("maxColossalFurnaceSize", 5, 2, 5);

    public static final ModConfigSpec.BooleanValue AUTO_REVALIDATE_STRUCTURE = BUILDER
            .comment("Revalidate formed colossal furnaces when shell blocks change.")
            .define("autoRevalidateStructure", true);

    public static final ModConfigSpec.BooleanValue SHOW_STRUCTURE_ERROR_MESSAGES = BUILDER
            .comment("Show player-facing structure validation errors.")
            .define("showStructureErrorMessages", true);

    public static final ModConfigSpec SPEC = BUILDER.build();

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

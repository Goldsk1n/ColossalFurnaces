package com.colossalfurnaces.registry;

import com.colossalfurnaces.ColossalFurnacesMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> REGISTER = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ColossalFurnacesMod.MOD_ID);

    public static final RegistryObject<CreativeModeTab> COLOSSAL_FURNACES = REGISTER.register("colossal_furnaces",
            () -> CreativeModeTab.builder()
                    .withTabsBefore(CreativeModeTabs.FUNCTIONAL_BLOCKS)
                    .icon(() -> ModItems.COLOSSAL_FURNACE_CORE.get().getDefaultInstance())
                    .title(Component.translatable("itemGroup.colossalfurnaces"))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.COLOSSAL_FURNACE_CORE.get());
                        output.accept(ModItems.COLOSSAL_FURNACE_WALL.get());
                        output.accept(ModItems.COLOSSAL_FURNACE_INTERFACE.get());
                        output.accept(ModItems.COLOSSAL_SMOKER_CORE.get());
                        output.accept(ModItems.COLOSSAL_BLAST_FURNACE_CORE.get());
                    })
                    .build());

    private ModCreativeTabs() {
    }
}

package com.colossalfurnaces;

import com.colossalfurnaces.client.ColossalFurnaceScreen;
import com.colossalfurnaces.client.ColossalFurnaceParticles;
import com.colossalfurnaces.client.ColossalBlastFurnaceScreen;
import com.colossalfurnaces.client.ColossalSmokerScreen;
import com.colossalfurnaces.client.render.ColossalBlastFurnaceRenderer;
import com.colossalfurnaces.client.render.ColossalFurnaceRenderer;
import com.colossalfurnaces.client.render.ColossalSmokerRenderer;
import com.colossalfurnaces.config.ColossalFurnacesConfig;
import com.colossalfurnaces.registry.ModBlockEntities;
import com.colossalfurnaces.registry.ModBlocks;
import com.colossalfurnaces.registry.ModCreativeTabs;
import com.colossalfurnaces.registry.ModItems;
import com.colossalfurnaces.registry.ModMenus;
import com.colossalfurnaces.registry.ModParticles;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ColossalFurnacesMod.MOD_ID)
public final class ColossalFurnacesMod {
    public static final String MOD_ID = "colossalfurnaces";

    public ColossalFurnacesMod(FMLJavaModLoadingContext context) {
        IEventBus modBus = context.getModEventBus();
        ModBlocks.REGISTER.register(modBus);
        ModItems.REGISTER.register(modBus);
        ModBlockEntities.REGISTER.register(modBus);
        ModMenus.REGISTER.register(modBus);
        ModCreativeTabs.REGISTER.register(modBus);
        ModParticles.REGISTER.register(modBus);
        MinecraftForge.EVENT_BUS.register(this);
        context.registerConfig(ModConfig.Type.COMMON, ColossalFurnacesConfig.SPEC);
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class ClientEvents {
        private ClientEvents() {
        }

        @SubscribeEvent
        public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
            ColossalFurnaceParticles.registerProviders(event);
        }

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                MenuScreens.register(ModMenus.COLOSSAL_FURNACE.get(), ColossalFurnaceScreen::new);
                BlockEntityRenderers.register(ModBlockEntities.COLOSSAL_FURNACE_CORE.get(), ColossalFurnaceRenderer::new);
                MenuScreens.register(ModMenus.COLOSSAL_SMOKER.get(), ColossalSmokerScreen::new);
                BlockEntityRenderers.register(ModBlockEntities.COLOSSAL_SMOKER_CORE.get(), ColossalSmokerRenderer::new);
                MenuScreens.register(ModMenus.COLOSSAL_BLAST_FURNACE.get(), ColossalBlastFurnaceScreen::new);
                BlockEntityRenderers.register(ModBlockEntities.COLOSSAL_BLAST_FURNACE_CORE.get(), ColossalBlastFurnaceRenderer::new);
            });
        }

    }
}

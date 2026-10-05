package com.colossalfurnaces;

import com.colossalfurnaces.client.ColossalFurnaceScreen;
import com.colossalfurnaces.client.ColossalFurnaceParticles;
import com.colossalfurnaces.client.ColossalBlastFurnaceScreen;
import com.colossalfurnaces.client.ColossalSmokerScreen;
import com.colossalfurnaces.client.render.ColossalBlastFurnaceRenderer;
import com.colossalfurnaces.client.render.ColossalFurnaceRenderer;
import com.colossalfurnaces.client.render.ColossalFurnaceRenderTypes;
import com.colossalfurnaces.client.render.ColossalSmokerRenderer;
import com.colossalfurnaces.config.ColossalFurnacesConfig;
import com.colossalfurnaces.gametest.ColossalFurnaceGameTests;
import com.colossalfurnaces.registry.ModBlockEntities;
import com.colossalfurnaces.registry.ModBlocks;
import com.colossalfurnaces.registry.ModCreativeTabs;
import com.colossalfurnaces.registry.ModItems;
import com.colossalfurnaces.registry.ModMenus;
import com.colossalfurnaces.registry.ModParticles;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;

@Mod(ColossalFurnacesMod.MOD_ID)
public final class ColossalFurnacesMod {
    public static final String MOD_ID = "colossalfurnaces";

    public ColossalFurnacesMod(IEventBus modBus, ModContainer modContainer) {
        ModBlocks.REGISTER.register(modBus);
        ModItems.REGISTER.register(modBus);
        ModBlockEntities.REGISTER.register(modBus);
        ModMenus.REGISTER.register(modBus);
        ModCreativeTabs.REGISTER.register(modBus);
        ModParticles.REGISTER.register(modBus);
        modBus.addListener(this::registerCapabilities);
        ColossalFurnaceGameTests.registerFunctions(modBus);
        modBus.addListener(ColossalFurnaceGameTests::registerTests);
        modContainer.registerConfig(ModConfig.Type.COMMON, ColossalFurnacesConfig.SPEC);
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.COLOSSAL_FURNACE_CORE.get(),
                (blockEntity, side) -> blockEntity.getAutomationTransferHandler());
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.COLOSSAL_SMOKER_CORE.get(),
                (blockEntity, side) -> blockEntity.getAutomationTransferHandler());
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.COLOSSAL_BLAST_FURNACE_CORE.get(),
                (blockEntity, side) -> blockEntity.getAutomationTransferHandler());
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.COLOSSAL_FURNACE_INTERFACE.get(),
                (blockEntity, side) -> blockEntity.getTransferHandler());
    }

    @EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
    public static final class ClientEvents {
        private ClientEvents() {
        }

        @SubscribeEvent
        public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
            ColossalFurnaceParticles.registerProviders(event);
        }

        @SubscribeEvent
        public static void registerScreens(RegisterMenuScreensEvent event) {
            event.register(ModMenus.COLOSSAL_FURNACE.get(), ColossalFurnaceScreen::new);
            event.register(ModMenus.COLOSSAL_SMOKER.get(), ColossalSmokerScreen::new);
            event.register(ModMenus.COLOSSAL_BLAST_FURNACE.get(), ColossalBlastFurnaceScreen::new);
        }

        @SubscribeEvent
        public static void registerRenderPipelines(RegisterRenderPipelinesEvent event) {
            event.registerPipeline(ColossalFurnaceRenderTypes.BLOCK_SHADED_CUTOUT_PIPELINE);
        }

        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(ModBlockEntities.COLOSSAL_FURNACE_CORE.get(), ColossalFurnaceRenderer::new);
            event.registerBlockEntityRenderer(ModBlockEntities.COLOSSAL_SMOKER_CORE.get(), ColossalSmokerRenderer::new);
            event.registerBlockEntityRenderer(ModBlockEntities.COLOSSAL_BLAST_FURNACE_CORE.get(), ColossalBlastFurnaceRenderer::new);
        }

    }
}

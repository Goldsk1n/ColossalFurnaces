package com.colossalfurnaces.client.render;

import com.colossalfurnaces.ColossalFurnacesMod;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import java.util.function.Function;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

public final class ColossalFurnaceRenderTypes {
    // Keep cutout/depth/lightmap behavior, but apply block face shading only in vertex colors.
    public static final RenderPipeline BLOCK_SHADED_CUTOUT_PIPELINE = RenderPipeline.builder(RenderPipelines.ENTITY_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath(ColossalFurnacesMod.MOD_ID, "pipeline/block_shaded_cutout"))
            .withShaderDefine("ALPHA_CUTOUT", 0.1F)
            .withShaderDefine("NO_CARDINAL_LIGHTING")
            .withSampler("Sampler1")
            .withCull(false)
            .build();
    private static final Function<Identifier, RenderType> BLOCK_SHADED_CUTOUT = Util.memoize(texture ->
            RenderType.create("colossalfurnaces_block_shaded_cutout",
                    RenderSetup.builder(BLOCK_SHADED_CUTOUT_PIPELINE)
                            .withTexture("Sampler0", texture)
                            .useLightmap()
                            .useOverlay()
                            .affectsCrumbling()
                            .setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE)
                            .createRenderSetup()));

    private ColossalFurnaceRenderTypes() {
    }

    public static RenderType blockShadedCutout(Identifier texture) {
        return BLOCK_SHADED_CUTOUT.apply(texture);
    }
}

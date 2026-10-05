package com.colossalfurnaces.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.function.Function;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.ClientHooks;

public final class ColossalFurnaceRenderTypes {
    private static final Function<ResourceLocation, RenderType> BLOCK_SHADED_CUTOUT = Util.memoize(texture -> {
        // The loader's unlit shader still samples the lightmap; only entity diffuse shading is omitted.
        RenderType.CompositeState state = RenderType.CompositeState.builder()
                .setShaderState(new RenderStateShard.ShaderStateShard(ClientHooks.ClientEvents::getEntityTranslucentUnlitShader))
                .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                .setTransparencyState(RenderType.NO_TRANSPARENCY)
                .setCullState(RenderType.NO_CULL)
                .setLightmapState(RenderType.LIGHTMAP)
                .setOverlayState(RenderType.OVERLAY)
                .createCompositeState(true);
        return RenderType.create("colossalfurnaces_block_shaded_cutout", DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.QUADS, 256, true, false, state);
    });

    private ColossalFurnaceRenderTypes() {
    }

    public static RenderType blockShadedCutout(ResourceLocation texture) {
        return BLOCK_SHADED_CUTOUT.apply(texture);
    }
}

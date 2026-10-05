package com.colossalfurnaces.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.function.Function;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.ForgeHooksClient;

public final class ColossalFurnaceRenderTypes extends RenderType {
    private static final Function<ResourceLocation, RenderType> BLOCK_SHADED_CUTOUT = Util.memoize(texture -> {
        // The loader's unlit shader still samples the lightmap; only entity diffuse shading is omitted.
        CompositeState state = CompositeState.builder()
                .setShaderState(new ShaderStateShard(ForgeHooksClient.ClientEvents::getEntityTranslucentUnlitShader))
                .setTextureState(new TextureStateShard(texture, false, false))
                .setTransparencyState(NO_TRANSPARENCY)
                .setCullState(NO_CULL)
                .setLightmapState(LIGHTMAP)
                .setOverlayState(OVERLAY)
                .createCompositeState(true);
        return create("colossalfurnaces_block_shaded_cutout", DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.QUADS, 256, true, false, state);
    });

    private ColossalFurnaceRenderTypes() {
        super("colossalfurnaces_render_types", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS,
                256, false, false, () -> {}, () -> {});
    }

    public static RenderType blockShadedCutout(ResourceLocation texture) {
        return BLOCK_SHADED_CUTOUT.apply(texture);
    }
}

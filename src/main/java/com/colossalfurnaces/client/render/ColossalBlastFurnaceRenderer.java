package com.colossalfurnaces.client.render;

import com.colossalfurnaces.blockentity.ColossalBlastFurnaceControllerBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

public class ColossalBlastFurnaceRenderer extends ColossalFurnaceRenderer<ColossalBlastFurnaceControllerBlockEntity> {
    private static final ResourceLocation BLAST_FRONT_OFF_TEXTURE = ResourceLocation.withDefaultNamespace("textures/block/blast_furnace_front.png");
    private static final ResourceLocation BLAST_FRONT_ON_TEXTURE = ResourceLocation.withDefaultNamespace("textures/block/blast_furnace_front_on.png");
    private static final ResourceLocation BLAST_SIDE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/block/blast_furnace_side.png");
    private static final ResourceLocation BLAST_TOP_TEXTURE = ResourceLocation.withDefaultNamespace("textures/block/blast_furnace_top.png");

    public ColossalBlastFurnaceRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected ResourceLocation getFaceTexture(Direction faceDirection, Direction front, boolean lit) {
        if (faceDirection == Direction.UP || faceDirection == Direction.DOWN) {
            return BLAST_TOP_TEXTURE;
        }
        if (faceDirection == front) {
            return lit ? BLAST_FRONT_ON_TEXTURE : BLAST_FRONT_OFF_TEXTURE;
        }
        return BLAST_SIDE_TEXTURE;
    }
}

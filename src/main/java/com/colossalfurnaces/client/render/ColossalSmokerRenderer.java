package com.colossalfurnaces.client.render;

import com.colossalfurnaces.blockentity.ColossalSmokerControllerBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;

public class ColossalSmokerRenderer extends ColossalFurnaceRenderer<ColossalSmokerControllerBlockEntity> {
    private static final Identifier SMOKER_FRONT_OFF_TEXTURE = Identifier.withDefaultNamespace("textures/block/smoker_front.png");
    private static final Identifier SMOKER_FRONT_ON_TEXTURE = Identifier.withDefaultNamespace("textures/block/smoker_front_on.png");
    private static final Identifier SMOKER_SIDE_TEXTURE = Identifier.withDefaultNamespace("textures/block/smoker_side.png");
    private static final Identifier SMOKER_TOP_TEXTURE = Identifier.withDefaultNamespace("textures/block/smoker_top.png");
    private static final Identifier SMOKER_BOTTOM_TEXTURE = Identifier.withDefaultNamespace("textures/block/smoker_bottom.png");

    public ColossalSmokerRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected Identifier getFaceTexture(Direction faceDirection, Direction front, boolean lit) {
        if (faceDirection == Direction.UP) {
            return SMOKER_TOP_TEXTURE;
        }
        if (faceDirection == Direction.DOWN) {
            return SMOKER_BOTTOM_TEXTURE;
        }
        if (faceDirection == front) {
            return lit ? SMOKER_FRONT_ON_TEXTURE : SMOKER_FRONT_OFF_TEXTURE;
        }
        return SMOKER_SIDE_TEXTURE;
    }
}

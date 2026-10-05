package com.colossalfurnaces.client.render;

import com.colossalfurnaces.ColossalFurnacesMod;
import com.colossalfurnaces.block.ColossalFurnaceControllerBlock;
import com.colossalfurnaces.blockentity.ColossalFurnaceControllerBlockEntity;
import com.colossalfurnaces.blockentity.ColossalFurnaceInterfaceBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class ColossalFurnaceRenderer<T extends ColossalFurnaceControllerBlockEntity> implements BlockEntityRenderer<T> {
    protected static final ResourceLocation FRONT_OFF_TEXTURE = ResourceLocation.fromNamespaceAndPath(ColossalFurnacesMod.MOD_ID, "textures/entity/colossal_furnace_front_off.png");
    protected static final ResourceLocation FRONT_ON_TEXTURE = ResourceLocation.fromNamespaceAndPath(ColossalFurnacesMod.MOD_ID, "textures/entity/colossal_furnace_front_on.png");
    protected static final ResourceLocation SIDE_TEXTURE = ResourceLocation.fromNamespaceAndPath(ColossalFurnacesMod.MOD_ID, "textures/entity/colossal_furnace_side.png");
    protected static final ResourceLocation TOP_TEXTURE = ResourceLocation.fromNamespaceAndPath(ColossalFurnacesMod.MOD_ID, "textures/entity/colossal_furnace_top.png");
    private static final ResourceLocation INTERFACE_MARKER_UNIVERSAL = ResourceLocation.fromNamespaceAndPath(ColossalFurnacesMod.MOD_ID, "textures/block/interface_marker_universal.png");
    private static final ResourceLocation INTERFACE_MARKER_INPUT = ResourceLocation.fromNamespaceAndPath(ColossalFurnacesMod.MOD_ID, "textures/block/interface_marker_input.png");
    private static final ResourceLocation INTERFACE_MARKER_FUEL = ResourceLocation.fromNamespaceAndPath(ColossalFurnacesMod.MOD_ID, "textures/block/interface_marker_fuel.png");
    private static final ResourceLocation INTERFACE_MARKER_OUTPUT = ResourceLocation.fromNamespaceAndPath(ColossalFurnacesMod.MOD_ID, "textures/block/interface_marker_output.png");
    private static final float MARKER_OFFSET = 0.002F;
    private static final float MARKER_SIZE = 1.0F;
    private static final float MARKER_INSET = (1.0F - MARKER_SIZE) * 0.5F;

    public ColossalFurnaceRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(T blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (!blockEntity.isFormed()) {
            return;
        }

        BlockPos origin = blockEntity.getBlockPos();
        BlockPos min = blockEntity.getMinPos();
        BlockPos max = blockEntity.getMaxPos();
        boolean lit = blockEntity.getBlockState().hasProperty(ColossalFurnaceControllerBlock.LIT)
                && blockEntity.getBlockState().getValue(ColossalFurnaceControllerBlock.LIT);
        Direction front = blockEntity.getStructureFacing();
        Level level = blockEntity.getLevel();

        poseStack.pushPose();
        this.renderBufferedTexturedBodyQuads(poseStack, bufferSource, level, packedLight, packedOverlay, origin, min, max,
                front, lit, 1.0F, 1.0F, 1.0F, 1.0F);
        for (BlockPos interfacePos : blockEntity.getLinkedInterfacePositions()) {
            this.renderMarker(poseStack, bufferSource, level, packedLight, packedOverlay, interfacePos, origin, min, max,
                    this.getInterfaceMarkerTexture(level, interfacePos), 1.0F, 1.0F, 1.0F);
        }
        poseStack.popPose();
    }

    private void renderBufferedTexturedBodyQuads(PoseStack poseStack, MultiBufferSource bufferSource, Level level,
                                                 int fallbackLight, int packedOverlay, BlockPos origin, BlockPos min, BlockPos max,
                                                 Direction front, boolean lit,
                                                 float red, float green, float blue, float alpha) {
        this.renderBufferedTexturedFaceGrid(poseStack, bufferSource, level, fallbackLight, packedOverlay,
                this.getFaceTexture(Direction.NORTH, front, lit),
                this.applyFaceShade(level, Direction.NORTH, red), this.applyFaceShade(level, Direction.NORTH, green),
                this.applyFaceShade(level, Direction.NORTH, blue), alpha,
                Direction.NORTH, origin, min, max, lit);
        this.renderBufferedTexturedFaceGrid(poseStack, bufferSource, level, fallbackLight, packedOverlay,
                this.getFaceTexture(Direction.SOUTH, front, lit),
                this.applyFaceShade(level, Direction.SOUTH, red), this.applyFaceShade(level, Direction.SOUTH, green),
                this.applyFaceShade(level, Direction.SOUTH, blue), alpha,
                Direction.SOUTH, origin, min, max, lit);
        this.renderBufferedTexturedFaceGrid(poseStack, bufferSource, level, fallbackLight, packedOverlay,
                this.getFaceTexture(Direction.WEST, front, lit),
                this.applyFaceShade(level, Direction.WEST, red), this.applyFaceShade(level, Direction.WEST, green),
                this.applyFaceShade(level, Direction.WEST, blue), alpha,
                Direction.WEST, origin, min, max, lit);
        this.renderBufferedTexturedFaceGrid(poseStack, bufferSource, level, fallbackLight, packedOverlay,
                this.getFaceTexture(Direction.EAST, front, lit),
                this.applyFaceShade(level, Direction.EAST, red), this.applyFaceShade(level, Direction.EAST, green),
                this.applyFaceShade(level, Direction.EAST, blue), alpha,
                Direction.EAST, origin, min, max, lit);
        this.renderBufferedTexturedFaceGrid(poseStack, bufferSource, level, fallbackLight, packedOverlay,
                this.getFaceTexture(Direction.UP, front, lit),
                this.applyFaceShade(level, Direction.UP, red), this.applyFaceShade(level, Direction.UP, green),
                this.applyFaceShade(level, Direction.UP, blue), alpha,
                Direction.UP, origin, min, max, lit);
        this.renderBufferedTexturedFaceGrid(poseStack, bufferSource, level, fallbackLight, packedOverlay,
                this.getFaceTexture(Direction.DOWN, front, lit),
                this.applyFaceShade(level, Direction.DOWN, red), this.applyFaceShade(level, Direction.DOWN, green),
                this.applyFaceShade(level, Direction.DOWN, blue), alpha,
                Direction.DOWN, origin, min, max, lit);
    }

    private void renderBufferedTexturedFaceGrid(PoseStack poseStack, MultiBufferSource bufferSource, Level level,
                                                int fallbackLight, int packedOverlay, ResourceLocation texture,
                                                float red, float green, float blue, float alpha,
                                                Direction direction, BlockPos origin, BlockPos min, BlockPos max,
                                                boolean selfLit) {
        TextureAtlasSprite sprite = this.getBlockAtlasSprite(texture);
        ResourceLocation renderTexture = sprite == null ? texture : TextureAtlas.LOCATION_BLOCKS;
        VertexConsumer consumer = bufferSource.getBuffer(ColossalFurnaceRenderTypes.blockShadedCutout(renderTexture));
        int sizeX = max.getX() - min.getX() + 1;
        int sizeY = max.getY() - min.getY() + 1;
        int sizeZ = max.getZ() - min.getZ() + 1;

        switch (direction) {
            case NORTH, SOUTH -> {
                int worldZ = direction == Direction.NORTH ? min.getZ() : max.getZ();
                float localZ = worldZ - origin.getZ() + (direction == Direction.SOUTH ? 1.0F : 0.0F);
                for (int x = 0; x < sizeX; x++) {
                    for (int y = 0; y < sizeY; y++) {
                        float x0 = min.getX() + x - origin.getX();
                        float y0 = min.getY() + y - origin.getY();
                        float uMin = direction == Direction.NORTH ? 1.0F - (x + 1.0F) / sizeX : x / (float) sizeX;
                        float uMax = direction == Direction.NORTH ? 1.0F - x / (float) sizeX : (x + 1.0F) / sizeX;
                        float vMin = 1.0F - (y + 1.0F) / sizeY;
                        float vMax = 1.0F - y / (float) sizeY;
                        float[] uv = this.resolveUv(sprite, uMin, uMax, vMin, vMax);
                        int packedLight = this.getFaceLight(level, new BlockPos(min.getX() + x, min.getY() + y, worldZ),
                                direction, fallbackLight, selfLit);
                        this.renderBufferedTexturedQuad(consumer, poseStack, packedLight, packedOverlay,
                                red, green, blue, alpha, direction,
                                x0, y0, localZ, x0 + 1.0F, y0 + 1.0F, localZ,
                                uv[0], uv[1], uv[2], uv[3]);
                    }
                }
            }
            case WEST, EAST -> {
                int worldX = direction == Direction.WEST ? min.getX() : max.getX();
                float localX = worldX - origin.getX() + (direction == Direction.EAST ? 1.0F : 0.0F);
                for (int z = 0; z < sizeZ; z++) {
                    for (int y = 0; y < sizeY; y++) {
                        float z0 = min.getZ() + z - origin.getZ();
                        float y0 = min.getY() + y - origin.getY();
                        float uMin = direction == Direction.WEST ? z / (float) sizeZ : 1.0F - (z + 1.0F) / sizeZ;
                        float uMax = direction == Direction.WEST ? (z + 1.0F) / sizeZ : 1.0F - z / (float) sizeZ;
                        float vMin = 1.0F - (y + 1.0F) / sizeY;
                        float vMax = 1.0F - y / (float) sizeY;
                        float[] uv = this.resolveUv(sprite, uMin, uMax, vMin, vMax);
                        int packedLight = this.getFaceLight(level, new BlockPos(worldX, min.getY() + y, min.getZ() + z),
                                direction, fallbackLight, selfLit);
                        this.renderBufferedTexturedQuad(consumer, poseStack, packedLight, packedOverlay,
                                red, green, blue, alpha, direction,
                                localX, y0, z0, localX, y0 + 1.0F, z0 + 1.0F,
                                uv[0], uv[1], uv[2], uv[3]);
                    }
                }
            }
            case UP, DOWN -> {
                int worldY = direction == Direction.UP ? max.getY() : min.getY();
                float localY = worldY - origin.getY() + (direction == Direction.UP ? 1.0F : 0.0F);
                for (int x = 0; x < sizeX; x++) {
                    for (int z = 0; z < sizeZ; z++) {
                        float x0 = min.getX() + x - origin.getX();
                        float z0 = min.getZ() + z - origin.getZ();
                        float uMin = 1.0F - (x + 1.0F) / sizeX;
                        float uMax = 1.0F - x / (float) sizeX;
                        float vMin = direction == Direction.UP ? 1.0F - (z + 1.0F) / sizeZ : z / (float) sizeZ;
                        float vMax = direction == Direction.UP ? 1.0F - z / (float) sizeZ : (z + 1.0F) / sizeZ;
                        float[] uv = this.resolveUv(sprite, uMin, uMax, vMin, vMax);
                        int packedLight = this.getFaceLight(level, new BlockPos(min.getX() + x, worldY, min.getZ() + z),
                                direction, fallbackLight, selfLit);
                        this.renderBufferedTexturedQuad(consumer, poseStack, packedLight, packedOverlay,
                                red, green, blue, alpha, direction,
                                x0, localY, z0, x0 + 1.0F, localY, z0 + 1.0F,
                                uv[0], uv[1], uv[2], uv[3]);
                    }
                }
            }
        }
    }

    private TextureAtlasSprite getBlockAtlasSprite(ResourceLocation texture) {
        String path = texture.getPath();
        if (!texture.getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE)
                || !path.startsWith("textures/block/") || !path.endsWith(".png")) {
            return null;
        }

        ResourceLocation spriteId = ResourceLocation.fromNamespaceAndPath(
                texture.getNamespace(), path.substring("textures/".length(), path.length() - ".png".length()));
        return Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(spriteId);
    }

    private float[] resolveUv(TextureAtlasSprite sprite, float uMin, float uMax, float vMin, float vMax) {
        if (sprite == null) {
            return new float[]{uMin, uMax, vMin, vMax};
        }
        // Minecraft 1.21 sprite interpolation takes normalized 0-1 coordinates, not 0-16 pixels.
        return new float[]{
                sprite.getU(uMin), sprite.getU(uMax),
                sprite.getV(vMin), sprite.getV(vMax)
        };
    }

    private void renderBufferedTexturedQuad(VertexConsumer consumer, PoseStack poseStack, int packedLight, int packedOverlay,
                                            float red, float green, float blue, float alpha,
                                            Direction direction, float x0, float y0, float z0, float x1, float y1, float z1,
                                            float uMin, float uMax, float vMin, float vMax) {
        PoseStack.Pose pose = poseStack.last();
        Matrix4f poseMatrix = pose.pose();
        Matrix3f normalMatrix = pose.normal();

        switch (direction) {
            case NORTH -> {
                this.vertex(consumer, poseMatrix, normalMatrix, x0, y0, z0, uMax, vMax, direction, packedLight, packedOverlay, red, green, blue, alpha);
                this.vertex(consumer, poseMatrix, normalMatrix, x1, y0, z0, uMin, vMax, direction, packedLight, packedOverlay, red, green, blue, alpha);
                this.vertex(consumer, poseMatrix, normalMatrix, x1, y1, z0, uMin, vMin, direction, packedLight, packedOverlay, red, green, blue, alpha);
                this.vertex(consumer, poseMatrix, normalMatrix, x0, y1, z0, uMax, vMin, direction, packedLight, packedOverlay, red, green, blue, alpha);
            }
            case SOUTH -> {
                this.vertex(consumer, poseMatrix, normalMatrix, x1, y0, z1, uMax, vMax, direction, packedLight, packedOverlay, red, green, blue, alpha);
                this.vertex(consumer, poseMatrix, normalMatrix, x0, y0, z1, uMin, vMax, direction, packedLight, packedOverlay, red, green, blue, alpha);
                this.vertex(consumer, poseMatrix, normalMatrix, x0, y1, z1, uMin, vMin, direction, packedLight, packedOverlay, red, green, blue, alpha);
                this.vertex(consumer, poseMatrix, normalMatrix, x1, y1, z1, uMax, vMin, direction, packedLight, packedOverlay, red, green, blue, alpha);
            }
            case WEST -> {
                this.vertex(consumer, poseMatrix, normalMatrix, x0, y0, z1, uMax, vMax, direction, packedLight, packedOverlay, red, green, blue, alpha);
                this.vertex(consumer, poseMatrix, normalMatrix, x0, y0, z0, uMin, vMax, direction, packedLight, packedOverlay, red, green, blue, alpha);
                this.vertex(consumer, poseMatrix, normalMatrix, x0, y1, z0, uMin, vMin, direction, packedLight, packedOverlay, red, green, blue, alpha);
                this.vertex(consumer, poseMatrix, normalMatrix, x0, y1, z1, uMax, vMin, direction, packedLight, packedOverlay, red, green, blue, alpha);
            }
            case EAST -> {
                this.vertex(consumer, poseMatrix, normalMatrix, x1, y0, z0, uMax, vMax, direction, packedLight, packedOverlay, red, green, blue, alpha);
                this.vertex(consumer, poseMatrix, normalMatrix, x1, y0, z1, uMin, vMax, direction, packedLight, packedOverlay, red, green, blue, alpha);
                this.vertex(consumer, poseMatrix, normalMatrix, x1, y1, z1, uMin, vMin, direction, packedLight, packedOverlay, red, green, blue, alpha);
                this.vertex(consumer, poseMatrix, normalMatrix, x1, y1, z0, uMax, vMin, direction, packedLight, packedOverlay, red, green, blue, alpha);
            }
            case UP -> {
                this.vertex(consumer, poseMatrix, normalMatrix, x0, y0, z0, uMax, vMax, direction, packedLight, packedOverlay, red, green, blue, alpha);
                this.vertex(consumer, poseMatrix, normalMatrix, x1, y0, z0, uMin, vMax, direction, packedLight, packedOverlay, red, green, blue, alpha);
                this.vertex(consumer, poseMatrix, normalMatrix, x1, y0, z1, uMin, vMin, direction, packedLight, packedOverlay, red, green, blue, alpha);
                this.vertex(consumer, poseMatrix, normalMatrix, x0, y0, z1, uMax, vMin, direction, packedLight, packedOverlay, red, green, blue, alpha);
            }
            case DOWN -> {
                this.vertex(consumer, poseMatrix, normalMatrix, x0, y0, z1, uMax, vMax, direction, packedLight, packedOverlay, red, green, blue, alpha);
                this.vertex(consumer, poseMatrix, normalMatrix, x1, y0, z1, uMin, vMax, direction, packedLight, packedOverlay, red, green, blue, alpha);
                this.vertex(consumer, poseMatrix, normalMatrix, x1, y0, z0, uMin, vMin, direction, packedLight, packedOverlay, red, green, blue, alpha);
                this.vertex(consumer, poseMatrix, normalMatrix, x0, y0, z0, uMax, vMin, direction, packedLight, packedOverlay, red, green, blue, alpha);
            }
        }
    }

    protected ResourceLocation getFaceTexture(Direction faceDirection, Direction front, boolean lit) {
        if (faceDirection == Direction.UP) {
            return this.getTopTexture();
        }
        if (faceDirection == Direction.DOWN) {
            return this.getBottomTexture();
        }
        if (faceDirection == front) {
            return this.getFrontTexture(lit);
        }
        return this.getSideTexture();
    }

    protected ResourceLocation getFrontTexture(boolean lit) {
        return lit ? FRONT_ON_TEXTURE : FRONT_OFF_TEXTURE;
    }

    protected ResourceLocation getSideTexture() {
        return SIDE_TEXTURE;
    }

    protected ResourceLocation getTopTexture() {
        return TOP_TEXTURE;
    }

    protected ResourceLocation getBottomTexture() {
        return this.getTopTexture();
    }

    private void renderMarker(PoseStack poseStack, MultiBufferSource bufferSource, Level level, int fallbackLight, int packedOverlay,
                              BlockPos markerPos, BlockPos origin, BlockPos min, BlockPos max, ResourceLocation texture,
                              float red, float green, float blue) {
        VertexConsumer consumer = bufferSource.getBuffer(ColossalFurnaceRenderTypes.blockShadedCutout(texture));
        float x = markerPos.getX() - origin.getX();
        float y = markerPos.getY() - origin.getY();
        float z = markerPos.getZ() - origin.getZ();
        float minInset = MARKER_INSET;
        float maxInset = 1.0F - MARKER_INSET;

        if (markerPos.getZ() == min.getZ()) {
            int packedLight = this.getFaceLight(level, markerPos, Direction.NORTH, fallbackLight);
            this.renderMarkerQuad(poseStack, consumer, level, packedLight, packedOverlay, Direction.NORTH,
                    x + minInset, y + minInset, z - MARKER_OFFSET, x + maxInset, y + maxInset, z - MARKER_OFFSET,
                    red, green, blue);
        }
        if (markerPos.getZ() == max.getZ()) {
            int packedLight = this.getFaceLight(level, markerPos, Direction.SOUTH, fallbackLight);
            this.renderMarkerQuad(poseStack, consumer, level, packedLight, packedOverlay, Direction.SOUTH,
                    x + minInset, y + minInset, z + 1.0F + MARKER_OFFSET, x + maxInset, y + maxInset, z + 1.0F + MARKER_OFFSET,
                    red, green, blue);
        }
        if (markerPos.getX() == min.getX()) {
            int packedLight = this.getFaceLight(level, markerPos, Direction.WEST, fallbackLight);
            this.renderMarkerQuad(poseStack, consumer, level, packedLight, packedOverlay, Direction.WEST,
                    x - MARKER_OFFSET, y + minInset, z + minInset, x - MARKER_OFFSET, y + maxInset, z + maxInset,
                    red, green, blue);
        }
        if (markerPos.getX() == max.getX()) {
            int packedLight = this.getFaceLight(level, markerPos, Direction.EAST, fallbackLight);
            this.renderMarkerQuad(poseStack, consumer, level, packedLight, packedOverlay, Direction.EAST,
                    x + 1.0F + MARKER_OFFSET, y + minInset, z + minInset, x + 1.0F + MARKER_OFFSET, y + maxInset, z + maxInset,
                    red, green, blue);
        }
        if (markerPos.getY() == max.getY()) {
            int packedLight = this.getFaceLight(level, markerPos, Direction.UP, fallbackLight);
            this.renderMarkerQuad(poseStack, consumer, level, packedLight, packedOverlay, Direction.UP,
                    x + minInset, y + 1.0F + MARKER_OFFSET, z + minInset, x + maxInset, y + 1.0F + MARKER_OFFSET, z + maxInset,
                    red, green, blue);
        }
        if (markerPos.getY() == min.getY()) {
            int packedLight = this.getFaceLight(level, markerPos, Direction.DOWN, fallbackLight);
            this.renderMarkerQuad(poseStack, consumer, level, packedLight, packedOverlay, Direction.DOWN,
                    x + minInset, y - MARKER_OFFSET, z + minInset, x + maxInset, y - MARKER_OFFSET, z + maxInset,
                    red, green, blue);
        }
    }

    private ResourceLocation getInterfaceMarkerTexture(Level level, BlockPos interfacePos) {
        if (level != null && level.getBlockEntity(interfacePos) instanceof ColossalFurnaceInterfaceBlockEntity interfaceBlockEntity) {
            return switch (interfaceBlockEntity.getMode()) {
                case UNIVERSAL -> INTERFACE_MARKER_UNIVERSAL;
                case INPUT -> INTERFACE_MARKER_INPUT;
                case FUEL -> INTERFACE_MARKER_FUEL;
                case OUTPUT -> INTERFACE_MARKER_OUTPUT;
            };
        }
        return INTERFACE_MARKER_UNIVERSAL;
    }

    private int getFaceLight(Level level, BlockPos shellPos, Direction faceDirection, int fallbackLight) {
        return this.getFaceLight(level, shellPos, faceDirection, fallbackLight, false);
    }

    private int getFaceLight(Level level, BlockPos shellPos, Direction faceDirection, int fallbackLight, boolean selfLit) {
        if (level == null) {
            return applyMinimumBlockLight(fallbackLight, selfLit);
        }
        return applyMinimumBlockLight(LevelRenderer.getLightColor(level, shellPos.relative(faceDirection)), selfLit);
    }

    private static int applyMinimumBlockLight(int packedLight, boolean selfLit) {
        if (!selfLit) {
            return packedLight;
        }
        return (packedLight & 0xFFFF0000) | Math.max(packedLight & 0xFFFF, 13 << 4);
    }

    private float applyFaceShade(Level level, Direction faceDirection, float color) {
        if (level == null) {
            return color;
        }
        return color * level.getShade(faceDirection, true);
    }

    private void renderMarkerQuad(PoseStack poseStack, VertexConsumer consumer, Level level, int packedLight, int packedOverlay,
                                  Direction direction, float x0, float y0, float z0, float x1, float y1, float z1,
                                  float red, float green, float blue) {
        red = this.applyFaceShade(level, direction, red);
        green = this.applyFaceShade(level, direction, green);
        blue = this.applyFaceShade(level, direction, blue);
        PoseStack.Pose pose = poseStack.last();
        Matrix4f poseMatrix = pose.pose();
        Matrix3f normalMatrix = pose.normal();
        float u0 = 0.0F;
        float u1 = 1.0F;
        float v0 = 0.0F;
        float v1 = 1.0F;

        switch (direction) {
            case NORTH -> {
                this.vertex(consumer, poseMatrix, normalMatrix, x0, y0, z0, u0, v1, direction, packedLight, packedOverlay, red, green, blue, 1.0F);
                this.vertex(consumer, poseMatrix, normalMatrix, x1, y0, z0, u1, v1, direction, packedLight, packedOverlay, red, green, blue, 1.0F);
                this.vertex(consumer, poseMatrix, normalMatrix, x1, y1, z0, u1, v0, direction, packedLight, packedOverlay, red, green, blue, 1.0F);
                this.vertex(consumer, poseMatrix, normalMatrix, x0, y1, z0, u0, v0, direction, packedLight, packedOverlay, red, green, blue, 1.0F);
            }
            case SOUTH -> {
                this.vertex(consumer, poseMatrix, normalMatrix, x1, y0, z1, u0, v1, direction, packedLight, packedOverlay, red, green, blue, 1.0F);
                this.vertex(consumer, poseMatrix, normalMatrix, x0, y0, z1, u1, v1, direction, packedLight, packedOverlay, red, green, blue, 1.0F);
                this.vertex(consumer, poseMatrix, normalMatrix, x0, y1, z1, u1, v0, direction, packedLight, packedOverlay, red, green, blue, 1.0F);
                this.vertex(consumer, poseMatrix, normalMatrix, x1, y1, z1, u0, v0, direction, packedLight, packedOverlay, red, green, blue, 1.0F);
            }
            case WEST -> {
                this.vertex(consumer, poseMatrix, normalMatrix, x0, y0, z1, u0, v1, direction, packedLight, packedOverlay, red, green, blue, 1.0F);
                this.vertex(consumer, poseMatrix, normalMatrix, x0, y0, z0, u1, v1, direction, packedLight, packedOverlay, red, green, blue, 1.0F);
                this.vertex(consumer, poseMatrix, normalMatrix, x0, y1, z0, u1, v0, direction, packedLight, packedOverlay, red, green, blue, 1.0F);
                this.vertex(consumer, poseMatrix, normalMatrix, x0, y1, z1, u0, v0, direction, packedLight, packedOverlay, red, green, blue, 1.0F);
            }
            case EAST -> {
                this.vertex(consumer, poseMatrix, normalMatrix, x1, y0, z0, u0, v1, direction, packedLight, packedOverlay, red, green, blue, 1.0F);
                this.vertex(consumer, poseMatrix, normalMatrix, x1, y0, z1, u1, v1, direction, packedLight, packedOverlay, red, green, blue, 1.0F);
                this.vertex(consumer, poseMatrix, normalMatrix, x1, y1, z1, u1, v0, direction, packedLight, packedOverlay, red, green, blue, 1.0F);
                this.vertex(consumer, poseMatrix, normalMatrix, x1, y1, z0, u0, v0, direction, packedLight, packedOverlay, red, green, blue, 1.0F);
            }
            case UP -> {
                this.vertex(consumer, poseMatrix, normalMatrix, x0, y0, z0, u0, v1, direction, packedLight, packedOverlay, red, green, blue, 1.0F);
                this.vertex(consumer, poseMatrix, normalMatrix, x1, y0, z0, u1, v1, direction, packedLight, packedOverlay, red, green, blue, 1.0F);
                this.vertex(consumer, poseMatrix, normalMatrix, x1, y0, z1, u1, v0, direction, packedLight, packedOverlay, red, green, blue, 1.0F);
                this.vertex(consumer, poseMatrix, normalMatrix, x0, y0, z1, u0, v0, direction, packedLight, packedOverlay, red, green, blue, 1.0F);
            }
            case DOWN -> {
                this.vertex(consumer, poseMatrix, normalMatrix, x0, y0, z1, u0, v1, direction, packedLight, packedOverlay, red, green, blue, 1.0F);
                this.vertex(consumer, poseMatrix, normalMatrix, x1, y0, z1, u1, v1, direction, packedLight, packedOverlay, red, green, blue, 1.0F);
                this.vertex(consumer, poseMatrix, normalMatrix, x1, y0, z0, u1, v0, direction, packedLight, packedOverlay, red, green, blue, 1.0F);
                this.vertex(consumer, poseMatrix, normalMatrix, x0, y0, z0, u0, v0, direction, packedLight, packedOverlay, red, green, blue, 1.0F);
            }
        }
    }

    private void vertex(VertexConsumer consumer, Matrix4f poseMatrix, Matrix3f normalMatrix, float x, float y, float z,
                        float u, float v, Direction normal, int packedLight, int packedOverlay,
                        float red, float green, float blue, float alpha) {
        consumer.addVertex(poseMatrix, x, y, z)
                .setColor((int) (red * 255.0F), (int) (green * 255.0F), (int) (blue * 255.0F), (int) (alpha * 255.0F))
                .setUv(u, v)
                .setOverlay(packedOverlay)
                .setLight(packedLight)
                .setNormal(normal.getStepX(), normal.getStepY(), normal.getStepZ());
    }

    @Override
    public AABB getRenderBoundingBox(T blockEntity) {
        return blockEntity.isFormed()
                ? new AABB(blockEntity.getMinPos()).minmax(new AABB(blockEntity.getMaxPos()))
                : BlockEntityRenderer.super.getRenderBoundingBox(blockEntity);
    }

    @Override
    public boolean shouldRenderOffScreen(T blockEntity) {
        return blockEntity.isFormed();
    }

    @Override
    public int getViewDistance() {
        return 128;
    }
}

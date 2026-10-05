package com.colossalfurnaces.client.render;

import com.colossalfurnaces.ColossalFurnacesMod;
import com.colossalfurnaces.block.ColossalFurnaceControllerBlock;
import com.colossalfurnaces.blockentity.ColossalFurnaceControllerBlockEntity;
import com.colossalfurnaces.blockentity.ColossalFurnaceInterfaceBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class ColossalFurnaceRenderer<T extends ColossalFurnaceControllerBlockEntity>
        implements BlockEntityRenderer<T, ColossalFurnaceRenderer.RenderState> {
    protected static final Identifier FRONT_OFF_TEXTURE = texture("textures/entity/colossal_furnace_front_off.png");
    protected static final Identifier FRONT_ON_TEXTURE = texture("textures/entity/colossal_furnace_front_on.png");
    protected static final Identifier SIDE_TEXTURE = texture("textures/entity/colossal_furnace_side.png");
    protected static final Identifier TOP_TEXTURE = texture("textures/entity/colossal_furnace_top.png");
    private static final Identifier INTERFACE_MARKER_UNIVERSAL = texture("textures/block/interface_marker_universal.png");
    private static final Identifier INTERFACE_MARKER_INPUT = texture("textures/block/interface_marker_input.png");
    private static final Identifier INTERFACE_MARKER_FUEL = texture("textures/block/interface_marker_fuel.png");
    private static final Identifier INTERFACE_MARKER_OUTPUT = texture("textures/block/interface_marker_output.png");
    private static final float MARKER_OFFSET = 0.002F;

    public ColossalFurnaceRenderer(BlockEntityRendererProvider.Context context) {
    }

    private static Identifier texture(String path) {
        return Identifier.fromNamespaceAndPath(ColossalFurnacesMod.MOD_ID, path);
    }

    @Override
    public RenderState createRenderState() {
        return new RenderState();
    }

    @Override
    public void extractRenderState(T blockEntity, RenderState state, float partialTicks, Vec3 cameraPosition,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        state.quads.clear();
        state.formed = blockEntity.isFormed();
        if (!state.formed) {
            return;
        }

        BlockPos origin = blockEntity.getBlockPos();
        BlockPos min = blockEntity.getMinPos();
        BlockPos max = blockEntity.getMaxPos();
        Level level = blockEntity.getLevel();
        boolean lit = blockEntity.getBlockState().hasProperty(ColossalFurnaceControllerBlock.LIT)
                && blockEntity.getBlockState().getValue(ColossalFurnaceControllerBlock.LIT);
        Direction front = blockEntity.getStructureFacing();

        for (Direction direction : Direction.values()) {
            this.extractBodyFace(state, level, origin, min, max, direction, this.getFaceTexture(direction, front, lit), lit);
        }
        for (BlockPos interfacePos : blockEntity.getLinkedInterfacePositions()) {
            this.extractMarker(state, level, origin, min, max, interfacePos, this.getInterfaceMarkerTexture(level, interfacePos));
        }
    }

    @Override
    public void submit(RenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        if (!state.formed) {
            return;
        }
        state.quads.forEach((texture, quads) -> submitNodeCollector.submitCustomGeometry(
                poseStack, ColossalFurnaceRenderTypes.blockShadedCutout(texture), (pose, consumer) -> {
                    for (Quad quad : quads) {
                        renderQuad(pose, consumer, quad);
                    }
                }));
    }

    private void extractBodyFace(RenderState state, Level level, BlockPos origin, BlockPos min, BlockPos max,
                                 Direction direction, Identifier texture, boolean selfLit) {
        TextureAtlasSprite sprite = getBlockAtlasSprite(texture);
        Identifier renderTexture = sprite == null ? texture : TextureAtlas.LOCATION_BLOCKS;
        int sizeX = max.getX() - min.getX() + 1;
        int sizeY = max.getY() - min.getY() + 1;
        int sizeZ = max.getZ() - min.getZ() + 1;
        float shade = faceShade(direction);

        switch (direction) {
            case NORTH, SOUTH -> {
                int worldZ = direction == Direction.NORTH ? min.getZ() : max.getZ();
                float localZ = worldZ - origin.getZ() + (direction == Direction.SOUTH ? 1.0F : 0.0F);
                for (int x = 0; x < sizeX; x++) {
                    for (int y = 0; y < sizeY; y++) {
                        float x0 = min.getX() + x - origin.getX();
                        float y0 = min.getY() + y - origin.getY();
                        float u0 = direction == Direction.NORTH ? 1.0F - (x + 1.0F) / sizeX : x / (float) sizeX;
                        float u1 = direction == Direction.NORTH ? 1.0F - x / (float) sizeX : (x + 1.0F) / sizeX;
                        float v0 = 1.0F - (y + 1.0F) / sizeY;
                        float v1 = 1.0F - y / (float) sizeY;
                        BlockPos shell = new BlockPos(min.getX() + x, min.getY() + y, worldZ);
                        float[] uv = resolveUv(sprite, u0, u1, v0, v1);
                        add(state, renderTexture, new Quad(direction, x0, y0, localZ, x0 + 1, y0 + 1, localZ,
                                uv[0], uv[1], uv[2], uv[3], faceLight(level, shell, direction, state.lightCoords, selfLit), shade));
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
                        float u0 = direction == Direction.WEST ? z / (float) sizeZ : 1.0F - (z + 1.0F) / sizeZ;
                        float u1 = direction == Direction.WEST ? (z + 1.0F) / sizeZ : 1.0F - z / (float) sizeZ;
                        float v0 = 1.0F - (y + 1.0F) / sizeY;
                        float v1 = 1.0F - y / (float) sizeY;
                        BlockPos shell = new BlockPos(worldX, min.getY() + y, min.getZ() + z);
                        float[] uv = resolveUv(sprite, u0, u1, v0, v1);
                        add(state, renderTexture, new Quad(direction, localX, y0, z0, localX, y0 + 1, z0 + 1,
                                uv[0], uv[1], uv[2], uv[3], faceLight(level, shell, direction, state.lightCoords, selfLit), shade));
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
                        float u0 = 1.0F - (x + 1.0F) / sizeX;
                        float u1 = 1.0F - x / (float) sizeX;
                        float v0 = direction == Direction.UP ? 1.0F - (z + 1.0F) / sizeZ : z / (float) sizeZ;
                        float v1 = direction == Direction.UP ? 1.0F - z / (float) sizeZ : (z + 1.0F) / sizeZ;
                        BlockPos shell = new BlockPos(min.getX() + x, worldY, min.getZ() + z);
                        float[] uv = resolveUv(sprite, u0, u1, v0, v1);
                        add(state, renderTexture, new Quad(direction, x0, localY, z0, x0 + 1, localY, z0 + 1,
                                uv[0], uv[1], uv[2], uv[3], faceLight(level, shell, direction, state.lightCoords, selfLit), shade));
                    }
                }
            }
        }
    }

    private static TextureAtlasSprite getBlockAtlasSprite(Identifier texture) {
        String path = texture.getPath();
        if (!texture.getNamespace().equals(Identifier.DEFAULT_NAMESPACE)
                || !path.startsWith("textures/block/") || !path.endsWith(".png")) {
            return null;
        }

        Identifier spriteId = Identifier.fromNamespaceAndPath(
                texture.getNamespace(), path.substring("textures/".length(), path.length() - ".png".length()));
        // AtlasManager looks up definition IDs; the texture path is used only for the render buffer.
        return Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS).getSprite(spriteId);
    }

    private static float[] resolveUv(TextureAtlasSprite sprite, float u0, float u1, float v0, float v1) {
        if (sprite == null) {
            return new float[]{u0, u1, v0, v1};
        }
        // Sprite interpolation takes normalized 0-1 coordinates, not 0-16 pixels.
        return new float[]{
                sprite.getU(u0), sprite.getU(u1),
                sprite.getV(v0), sprite.getV(v1)
        };
    }

    private void extractMarker(RenderState state, Level level, BlockPos origin, BlockPos min, BlockPos max,
                               BlockPos marker, Identifier texture) {
        float x = marker.getX() - origin.getX();
        float y = marker.getY() - origin.getY();
        float z = marker.getZ() - origin.getZ();
        if (marker.getZ() == min.getZ()) addMarker(state, level, marker, texture, Direction.NORTH, x, y, z - MARKER_OFFSET, x + 1, y + 1, z - MARKER_OFFSET);
        if (marker.getZ() == max.getZ()) addMarker(state, level, marker, texture, Direction.SOUTH, x, y, z + 1 + MARKER_OFFSET, x + 1, y + 1, z + 1 + MARKER_OFFSET);
        if (marker.getX() == min.getX()) addMarker(state, level, marker, texture, Direction.WEST, x - MARKER_OFFSET, y, z, x - MARKER_OFFSET, y + 1, z + 1);
        if (marker.getX() == max.getX()) addMarker(state, level, marker, texture, Direction.EAST, x + 1 + MARKER_OFFSET, y, z, x + 1 + MARKER_OFFSET, y + 1, z + 1);
        if (marker.getY() == max.getY()) addMarker(state, level, marker, texture, Direction.UP, x, y + 1 + MARKER_OFFSET, z, x + 1, y + 1 + MARKER_OFFSET, z + 1);
        if (marker.getY() == min.getY()) addMarker(state, level, marker, texture, Direction.DOWN, x, y - MARKER_OFFSET, z, x + 1, y - MARKER_OFFSET, z + 1);
    }

    private static void addMarker(RenderState state, Level level, BlockPos marker, Identifier texture, Direction direction,
                                  float x0, float y0, float z0, float x1, float y1, float z1) {
        add(state, texture, new Quad(direction, x0, y0, z0, x1, y1, z1, 0, 1, 0, 1,
                faceLight(level, marker, direction, state.lightCoords), faceShade(direction)));
    }

    protected Identifier getFaceTexture(Direction faceDirection, Direction front, boolean lit) {
        if (faceDirection == Direction.UP || faceDirection == Direction.DOWN) return TOP_TEXTURE;
        if (faceDirection == front) return lit ? FRONT_ON_TEXTURE : FRONT_OFF_TEXTURE;
        return SIDE_TEXTURE;
    }

    private Identifier getInterfaceMarkerTexture(Level level, BlockPos pos) {
        if (level != null && level.getBlockEntity(pos) instanceof ColossalFurnaceInterfaceBlockEntity interfaceBlockEntity) {
            return switch (interfaceBlockEntity.getMode()) {
                case UNIVERSAL -> INTERFACE_MARKER_UNIVERSAL;
                case INPUT -> INTERFACE_MARKER_INPUT;
                case FUEL -> INTERFACE_MARKER_FUEL;
                case OUTPUT -> INTERFACE_MARKER_OUTPUT;
            };
        }
        return INTERFACE_MARKER_UNIVERSAL;
    }

    private static int faceLight(Level level, BlockPos shell, Direction direction, int fallback) {
        return faceLight(level, shell, direction, fallback, false);
    }

    private static int faceLight(Level level, BlockPos shell, Direction direction, int fallback, boolean selfLit) {
        int packedLight = level == null ? fallback : LevelRenderer.getLightCoords(level, shell.relative(direction));
        if (!selfLit) {
            return packedLight;
        }
        return (packedLight & 0xFFFF0000) | Math.max(packedLight & 0xFFFF, 13 << 4);
    }

    private static float faceShade(Direction direction) {
        return switch (direction) {
            case DOWN -> 0.5F;
            case UP -> 1.0F;
            case NORTH, SOUTH -> 0.8F;
            case WEST, EAST -> 0.6F;
        };
    }

    private static void add(RenderState state, Identifier texture, Quad quad) {
        state.quads.computeIfAbsent(texture, ignored -> new ArrayList<>()).add(quad);
    }

    private static void renderQuad(PoseStack.Pose pose, VertexConsumer consumer, Quad q) {
        switch (q.direction) {
            case NORTH -> {
                vertex(pose, consumer, q.x0, q.y0, q.z0, q.u1, q.v1, q);
                vertex(pose, consumer, q.x1, q.y0, q.z0, q.u0, q.v1, q);
                vertex(pose, consumer, q.x1, q.y1, q.z0, q.u0, q.v0, q);
                vertex(pose, consumer, q.x0, q.y1, q.z0, q.u1, q.v0, q);
            }
            case SOUTH -> {
                vertex(pose, consumer, q.x1, q.y0, q.z1, q.u1, q.v1, q);
                vertex(pose, consumer, q.x0, q.y0, q.z1, q.u0, q.v1, q);
                vertex(pose, consumer, q.x0, q.y1, q.z1, q.u0, q.v0, q);
                vertex(pose, consumer, q.x1, q.y1, q.z1, q.u1, q.v0, q);
            }
            case WEST -> {
                vertex(pose, consumer, q.x0, q.y0, q.z1, q.u1, q.v1, q);
                vertex(pose, consumer, q.x0, q.y0, q.z0, q.u0, q.v1, q);
                vertex(pose, consumer, q.x0, q.y1, q.z0, q.u0, q.v0, q);
                vertex(pose, consumer, q.x0, q.y1, q.z1, q.u1, q.v0, q);
            }
            case EAST -> {
                vertex(pose, consumer, q.x1, q.y0, q.z0, q.u1, q.v1, q);
                vertex(pose, consumer, q.x1, q.y0, q.z1, q.u0, q.v1, q);
                vertex(pose, consumer, q.x1, q.y1, q.z1, q.u0, q.v0, q);
                vertex(pose, consumer, q.x1, q.y1, q.z0, q.u1, q.v0, q);
            }
            case UP -> {
                vertex(pose, consumer, q.x0, q.y0, q.z0, q.u1, q.v1, q);
                vertex(pose, consumer, q.x1, q.y0, q.z0, q.u0, q.v1, q);
                vertex(pose, consumer, q.x1, q.y0, q.z1, q.u0, q.v0, q);
                vertex(pose, consumer, q.x0, q.y0, q.z1, q.u1, q.v0, q);
            }
            case DOWN -> {
                vertex(pose, consumer, q.x0, q.y0, q.z1, q.u1, q.v1, q);
                vertex(pose, consumer, q.x1, q.y0, q.z1, q.u0, q.v1, q);
                vertex(pose, consumer, q.x1, q.y0, q.z0, q.u0, q.v0, q);
                vertex(pose, consumer, q.x0, q.y0, q.z0, q.u1, q.v0, q);
            }
        }
    }

    private static void vertex(PoseStack.Pose pose, VertexConsumer consumer, float x, float y, float z, float u, float v, Quad q) {
        int color = (int) (q.shade * 255.0F);
        consumer.addVertex(pose, x, y, z).setColor(color, color, color, 255).setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(q.light)
                .setNormal(pose, q.direction.getStepX(), q.direction.getStepY(), q.direction.getStepZ());
    }

    @Override
    public AABB getRenderBoundingBox(T blockEntity) {
        return blockEntity.isFormed()
                ? new AABB(blockEntity.getMinPos()).minmax(new AABB(blockEntity.getMaxPos()))
                : BlockEntityRenderer.super.getRenderBoundingBox(blockEntity);
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 128;
    }

    public static class RenderState extends BlockEntityRenderState {
        private boolean formed;
        private final Map<Identifier, List<Quad>> quads = new LinkedHashMap<>();
    }

    private record Quad(Direction direction, float x0, float y0, float z0, float x1, float y1, float z1,
                        float u0, float u1, float v0, float v1, int light, float shade) {
    }
}

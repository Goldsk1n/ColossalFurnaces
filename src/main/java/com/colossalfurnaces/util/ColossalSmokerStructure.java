package com.colossalfurnaces.util;

import com.colossalfurnaces.config.ColossalFurnacesConfig;
import com.colossalfurnaces.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

public final class ColossalSmokerStructure {
    private ColossalSmokerStructure() {
    }

    public static MultiblockValidationResult validate(Level level, BlockPos startPos) {
        return validate(level, startPos, ColossalFurnacesConfig.maxColossalFurnaceSize);
    }

    public static MultiblockValidationResult validate(Level level, BlockPos startPos, int maximumSize) {
        BlockState startState = level.getBlockState(startPos);
        if (!isShell(startState)) {
            return MultiblockValidationResult.invalid(StructureError.NOT_SHELL, startPos);
        }

        int radius = maximumSize;
        Set<BlockPos> shellPositions = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(startPos);
        shellPositions.add(startPos);

        while (!queue.isEmpty()) {
            BlockPos current = queue.removeFirst();
            for (Direction direction : Direction.values()) {
                BlockPos next = current.relative(direction);
                if (Math.abs(next.getX() - startPos.getX()) > radius
                        || Math.abs(next.getY() - startPos.getY()) > radius
                        || Math.abs(next.getZ() - startPos.getZ()) > radius
                        || shellPositions.contains(next)
                        || !isShell(level.getBlockState(next))) {
                    continue;
                }
                shellPositions.add(next.immutable());
                queue.addLast(next.immutable());
            }
        }

        if (shellPositions.size() == 1) {
            return MultiblockValidationResult.invalid(StructureError.TOO_SMALL, startPos);
        }

        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;
        int controllerCount = 0;
        BlockPos controllerPos = null;
        Set<BlockPos> interfacePositions = new HashSet<>();

        for (BlockPos shellPos : shellPositions) {
            minX = Math.min(minX, shellPos.getX());
            minY = Math.min(minY, shellPos.getY());
            minZ = Math.min(minZ, shellPos.getZ());
            maxX = Math.max(maxX, shellPos.getX());
            maxY = Math.max(maxY, shellPos.getY());
            maxZ = Math.max(maxZ, shellPos.getZ());

            BlockState shellState = level.getBlockState(shellPos);
            if (shellState.is(ModBlocks.COLOSSAL_SMOKER_CORE.get())) {
                controllerCount++;
                controllerPos = shellPos.immutable();
            } else if (shellState.is(ModBlocks.COLOSSAL_FURNACE_INTERFACE.get())) {
                interfacePositions.add(shellPos.immutable());
            }
        }

        if (controllerCount == 0) {
            return MultiblockValidationResult.invalid(StructureError.MISSING_CONTROLLER, startPos);
        }
        if (controllerCount > 1) {
            return MultiblockValidationResult.invalid(StructureError.MULTIPLE_CONTROLLERS, controllerPos);
        }

        int sizeX = maxX - minX + 1;
        int sizeY = maxY - minY + 1;
        int sizeZ = maxZ - minZ + 1;
        if (sizeX != sizeY || sizeX != sizeZ) {
            return MultiblockValidationResult.invalid(StructureError.NOT_CUBE, startPos);
        }
        if (sizeX < 2) {
            return MultiblockValidationResult.invalid(StructureError.TOO_SMALL, startPos);
        }
        if (sizeX > maximumSize) {
            return MultiblockValidationResult.invalid(StructureError.TOO_LARGE, startPos);
        }

        BlockPos minPos = new BlockPos(minX, minY, minZ);
        BlockPos maxPos = new BlockPos(maxX, maxY, maxZ);
        Set<BlockPos> expectedShellPositions = new HashSet<>();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos currentPos = new BlockPos(x, y, z);
                    boolean isBoundary = x == minX || x == maxX || y == minY || y == maxY || z == minZ || z == maxZ;
                    BlockState state = level.getBlockState(currentPos);
                    if (isBoundary) {
                        expectedShellPositions.add(currentPos);
                        if (!isShell(state)) {
                            return MultiblockValidationResult.invalid(StructureError.MISSING_BOUNDARY_BLOCK, currentPos);
                        }
                    } else if (!state.isAir()) {
                        return MultiblockValidationResult.invalid(StructureError.INTERIOR_NOT_EMPTY, currentPos);
                    }
                }
            }
        }

        if (!expectedShellPositions.equals(shellPositions)) {
            return MultiblockValidationResult.invalid(StructureError.NOT_CONNECTED, startPos);
        }

        return new MultiblockValidationResult(true, null, null, minPos, maxPos, sizeX, controllerPos, Set.copyOf(interfacePositions), Set.copyOf(shellPositions));
    }

    public static boolean isShell(BlockState state) {
        return state.is(ModBlocks.COLOSSAL_SMOKER_CORE.get())
                || state.is(ModBlocks.COLOSSAL_FURNACE_INTERFACE.get())
                || state.is(ModBlocks.COLOSSAL_FURNACE_WALL.get());
    }
}

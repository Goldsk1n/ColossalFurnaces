package com.colossalfurnaces.util;

import net.minecraft.core.BlockPos;

import java.util.Set;

public record MultiblockValidationResult(
        boolean valid,
        StructureError error,
        BlockPos errorPos,
        BlockPos minPos,
        BlockPos maxPos,
        int outerSize,
        BlockPos controllerPos,
        Set<BlockPos> interfacePositions,
        Set<BlockPos> shellPositions
) {
    public static MultiblockValidationResult invalid(StructureError error, BlockPos errorPos) {
        return new MultiblockValidationResult(false, error, errorPos, BlockPos.ZERO, BlockPos.ZERO, 0, BlockPos.ZERO, Set.of(), Set.of());
    }

    public int parallelism() {
        return this.outerSize < 2 ? 0 : this.outerSize * this.outerSize;
    }

    public boolean contains(BlockPos pos) {
        return pos.getX() >= this.minPos.getX() && pos.getX() <= this.maxPos.getX()
                && pos.getY() >= this.minPos.getY() && pos.getY() <= this.maxPos.getY()
                && pos.getZ() >= this.minPos.getZ() && pos.getZ() <= this.maxPos.getZ();
    }
}

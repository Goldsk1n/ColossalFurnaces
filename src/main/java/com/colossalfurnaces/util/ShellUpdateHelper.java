package com.colossalfurnaces.util;

import com.colossalfurnaces.blockentity.ColossalFurnaceControllerBlockEntity;
import com.colossalfurnaces.config.ColossalFurnacesConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class ShellUpdateHelper {
    private ShellUpdateHelper() {
    }

    public static void notifyNearbyControllers(Level level, BlockPos changedPos) {
        if (level.isClientSide || !ColossalFurnacesConfig.autoRevalidateStructure) {
            return;
        }

        int radius = ColossalFurnacesConfig.maxColossalFurnaceSize;
        BlockPos min = changedPos.offset(-radius, -radius, -radius);
        BlockPos max = changedPos.offset(radius, radius, radius);
        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof ColossalFurnaceControllerBlockEntity controller) {
                controller.revalidateStructure(null, true);
            }
        }
    }
}

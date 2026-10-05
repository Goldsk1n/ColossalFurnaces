package com.colossalfurnaces.util;

import com.colossalfurnaces.blockentity.ColossalFurnaceControllerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public final class ColossalFurnaceInteractionHelper {
    private ColossalFurnaceInteractionHelper() {
    }

    public static InteractionResult tryOpenAssembledMenu(Level level, BlockPos pos, Player player) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockState state = level.getBlockState(pos);
        if (!ColossalFurnaceStructure.isShell(state)
                && !ColossalSmokerStructure.isShell(state)
                && !ColossalBlastFurnaceStructure.isShell(state)) {
            return InteractionResult.PASS;
        }
        MultiblockValidationResult[] candidates = {
                ColossalFurnaceStructure.validate(level, pos),
                ColossalSmokerStructure.validate(level, pos),
                ColossalBlastFurnaceStructure.validate(level, pos)
        };
        for (MultiblockValidationResult validationResult : candidates) {
            if (validationResult.valid()
                    && validationResult.controllerPos() != null
                    && level.getBlockEntity(validationResult.controllerPos()) instanceof ColossalFurnaceControllerBlockEntity controller
                    && controller.revalidateStructure(player, false)) {
                controller.openMenu(player);
                return InteractionResult.CONSUME;
            }
        }

        return InteractionResult.PASS;
    }
}

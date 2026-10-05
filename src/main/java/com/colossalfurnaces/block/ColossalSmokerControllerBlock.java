package com.colossalfurnaces.block;

import com.colossalfurnaces.blockentity.ColossalFurnaceControllerBlockEntity;
import com.colossalfurnaces.blockentity.ColossalSmokerControllerBlockEntity;
import com.colossalfurnaces.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class ColossalSmokerControllerBlock extends ColossalFurnaceControllerBlock {
    public ColossalSmokerControllerBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ColossalSmokerControllerBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(
                blockEntityType,
                ModBlockEntities.COLOSSAL_SMOKER_CORE.get(),
                level.isClientSide
                        ? (tickLevel, tickPos, tickState, blockEntity) -> ColossalFurnaceControllerBlockEntity.clientTick(tickLevel, tickPos, tickState, blockEntity)
                        : (tickLevel, tickPos, tickState, blockEntity) -> ColossalFurnaceControllerBlockEntity.serverTick(tickLevel, tickPos, tickState, blockEntity)
        );
    }
}

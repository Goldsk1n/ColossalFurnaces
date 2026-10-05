package com.colossalfurnaces.registry;

import com.colossalfurnaces.ColossalFurnacesMod;
import com.colossalfurnaces.block.ColossalFurnaceControllerBlock;
import com.colossalfurnaces.block.ColossalFurnaceInterfaceBlock;
import com.colossalfurnaces.block.ColossalFurnaceWallBlock;
import com.colossalfurnaces.block.ColossalBlastFurnaceControllerBlock;
import com.colossalfurnaces.block.ColossalSmokerControllerBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks REGISTER = DeferredRegister.createBlocks(ColossalFurnacesMod.MOD_ID);

    public static final DeferredBlock<ColossalFurnaceControllerBlock> COLOSSAL_FURNACE_CORE = REGISTER.registerBlock("colossal_furnace_core",
            ColossalFurnaceControllerBlock::new, ModBlocks::shellProperties);
    public static final DeferredBlock<ColossalFurnaceWallBlock> COLOSSAL_FURNACE_WALL = REGISTER.registerBlock("colossal_furnace_wall",
            ColossalFurnaceWallBlock::new, () -> shellProperties().lightLevel(state -> state.getValue(ColossalFurnaceWallBlock.FORMED) && state.getValue(ColossalFurnaceWallBlock.LIT) ? 13 : 0));
    public static final DeferredBlock<ColossalFurnaceInterfaceBlock> COLOSSAL_FURNACE_INTERFACE = REGISTER.registerBlock("colossal_furnace_interface",
            ColossalFurnaceInterfaceBlock::new, () -> shellProperties().lightLevel(state -> state.getValue(ColossalFurnaceInterfaceBlock.FORMED) && state.getValue(ColossalFurnaceInterfaceBlock.LIT) ? 13 : 0));
    public static final DeferredBlock<ColossalSmokerControllerBlock> COLOSSAL_SMOKER_CORE = REGISTER.registerBlock("colossal_smoker_core",
            ColossalSmokerControllerBlock::new, ModBlocks::shellProperties);
    public static final DeferredBlock<ColossalBlastFurnaceControllerBlock> COLOSSAL_BLAST_FURNACE_CORE = REGISTER.registerBlock("colossal_blast_furnace_core",
            ColossalBlastFurnaceControllerBlock::new, ModBlocks::shellProperties);

    private ModBlocks() {
    }

    private static BlockBehaviour.Properties shellProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(3.5F, 6.0F)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops();
    }
}

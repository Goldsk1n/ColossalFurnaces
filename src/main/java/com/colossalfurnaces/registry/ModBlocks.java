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
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlocks {
    public static final DeferredRegister<Block> REGISTER = DeferredRegister.create(ForgeRegistries.BLOCKS, ColossalFurnacesMod.MOD_ID);

    public static final RegistryObject<Block> COLOSSAL_FURNACE_CORE = REGISTER.register("colossal_furnace_core",
            () -> new ColossalFurnaceControllerBlock(shellProperties()));
    public static final RegistryObject<Block> COLOSSAL_FURNACE_WALL = REGISTER.register("colossal_furnace_wall",
            () -> new ColossalFurnaceWallBlock(shellProperties().lightLevel(state ->
                    state.getValue(ColossalFurnaceWallBlock.FORMED) && state.getValue(ColossalFurnaceWallBlock.LIT) ? 13 : 0)));
    public static final RegistryObject<Block> COLOSSAL_FURNACE_INTERFACE = REGISTER.register("colossal_furnace_interface",
            () -> new ColossalFurnaceInterfaceBlock(shellProperties().lightLevel(state ->
                    state.getValue(ColossalFurnaceInterfaceBlock.FORMED) && state.getValue(ColossalFurnaceInterfaceBlock.LIT) ? 13 : 0)));
    public static final RegistryObject<Block> COLOSSAL_SMOKER_CORE = REGISTER.register("colossal_smoker_core",
            () -> new ColossalSmokerControllerBlock(shellProperties()));
    public static final RegistryObject<Block> COLOSSAL_BLAST_FURNACE_CORE = REGISTER.register("colossal_blast_furnace_core",
            () -> new ColossalBlastFurnaceControllerBlock(shellProperties()));

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

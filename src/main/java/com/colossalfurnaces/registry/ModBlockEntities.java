package com.colossalfurnaces.registry;

import com.colossalfurnaces.ColossalFurnacesMod;
import com.colossalfurnaces.blockentity.ColossalBlastFurnaceControllerBlockEntity;
import com.colossalfurnaces.blockentity.ColossalFurnaceControllerBlockEntity;
import com.colossalfurnaces.blockentity.ColossalFurnaceInterfaceBlockEntity;
import com.colossalfurnaces.blockentity.ColossalSmokerControllerBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> REGISTER = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ColossalFurnacesMod.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ColossalFurnaceControllerBlockEntity>> COLOSSAL_FURNACE_CORE =
            REGISTER.register("colossal_furnace_core",
                    () -> BlockEntityType.Builder.of(ColossalFurnaceControllerBlockEntity::new, ModBlocks.COLOSSAL_FURNACE_CORE.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ColossalFurnaceInterfaceBlockEntity>> COLOSSAL_FURNACE_INTERFACE =
            REGISTER.register("colossal_furnace_interface",
                    () -> BlockEntityType.Builder.of(ColossalFurnaceInterfaceBlockEntity::new, ModBlocks.COLOSSAL_FURNACE_INTERFACE.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ColossalSmokerControllerBlockEntity>> COLOSSAL_SMOKER_CORE =
            REGISTER.register("colossal_smoker_core",
                    () -> BlockEntityType.Builder.of(ColossalSmokerControllerBlockEntity::new, ModBlocks.COLOSSAL_SMOKER_CORE.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ColossalBlastFurnaceControllerBlockEntity>> COLOSSAL_BLAST_FURNACE_CORE =
            REGISTER.register("colossal_blast_furnace_core",
                    () -> BlockEntityType.Builder.of(ColossalBlastFurnaceControllerBlockEntity::new, ModBlocks.COLOSSAL_BLAST_FURNACE_CORE.get()).build(null));

    private ModBlockEntities() {
    }
}

package com.colossalfurnaces.registry;

import com.colossalfurnaces.ColossalFurnacesMod;
import com.colossalfurnaces.blockentity.ColossalBlastFurnaceControllerBlockEntity;
import com.colossalfurnaces.blockentity.ColossalFurnaceControllerBlockEntity;
import com.colossalfurnaces.blockentity.ColossalFurnaceInterfaceBlockEntity;
import com.colossalfurnaces.blockentity.ColossalSmokerControllerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> REGISTER = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ColossalFurnacesMod.MOD_ID);

    public static final RegistryObject<BlockEntityType<ColossalFurnaceControllerBlockEntity>> COLOSSAL_FURNACE_CORE =
            REGISTER.register("colossal_furnace_core",
                    () -> BlockEntityType.Builder.of(ColossalFurnaceControllerBlockEntity::new, ModBlocks.COLOSSAL_FURNACE_CORE.get()).build(null));

    public static final RegistryObject<BlockEntityType<ColossalFurnaceInterfaceBlockEntity>> COLOSSAL_FURNACE_INTERFACE =
            REGISTER.register("colossal_furnace_interface",
                    () -> BlockEntityType.Builder.of(ColossalFurnaceInterfaceBlockEntity::new, ModBlocks.COLOSSAL_FURNACE_INTERFACE.get()).build(null));

    public static final RegistryObject<BlockEntityType<ColossalSmokerControllerBlockEntity>> COLOSSAL_SMOKER_CORE =
            REGISTER.register("colossal_smoker_core",
                    () -> BlockEntityType.Builder.of(ColossalSmokerControllerBlockEntity::new, ModBlocks.COLOSSAL_SMOKER_CORE.get()).build(null));

    public static final RegistryObject<BlockEntityType<ColossalBlastFurnaceControllerBlockEntity>> COLOSSAL_BLAST_FURNACE_CORE =
            REGISTER.register("colossal_blast_furnace_core",
                    () -> BlockEntityType.Builder.of(ColossalBlastFurnaceControllerBlockEntity::new, ModBlocks.COLOSSAL_BLAST_FURNACE_CORE.get()).build(null));

    private ModBlockEntities() {
    }
}

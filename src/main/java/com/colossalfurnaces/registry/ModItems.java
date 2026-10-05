package com.colossalfurnaces.registry;

import com.colossalfurnaces.ColossalFurnacesMod;
import com.colossalfurnaces.item.ColossalFurnaceInterfaceBlockItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items REGISTER = DeferredRegister.createItems(ColossalFurnacesMod.MOD_ID);

    public static final DeferredItem<BlockItem> COLOSSAL_FURNACE_CORE = REGISTER.registerSimpleBlockItem(ModBlocks.COLOSSAL_FURNACE_CORE);
    public static final DeferredItem<BlockItem> COLOSSAL_FURNACE_WALL = REGISTER.registerSimpleBlockItem(ModBlocks.COLOSSAL_FURNACE_WALL);
    public static final DeferredItem<ColossalFurnaceInterfaceBlockItem> COLOSSAL_FURNACE_INTERFACE = REGISTER.registerItem("colossal_furnace_interface",
            properties -> new ColossalFurnaceInterfaceBlockItem(ModBlocks.COLOSSAL_FURNACE_INTERFACE.get(), properties), Item.Properties::useBlockDescriptionPrefix);
    public static final DeferredItem<BlockItem> COLOSSAL_SMOKER_CORE = REGISTER.registerSimpleBlockItem(ModBlocks.COLOSSAL_SMOKER_CORE);
    public static final DeferredItem<BlockItem> COLOSSAL_BLAST_FURNACE_CORE = REGISTER.registerSimpleBlockItem(ModBlocks.COLOSSAL_BLAST_FURNACE_CORE);

    private ModItems() {
    }
}

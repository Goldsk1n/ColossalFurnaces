package com.colossalfurnaces.registry;

import com.colossalfurnaces.ColossalFurnacesMod;
import com.colossalfurnaces.item.ColossalFurnaceInterfaceBlockItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> REGISTER = DeferredRegister.create(ForgeRegistries.ITEMS, ColossalFurnacesMod.MOD_ID);

    public static final RegistryObject<Item> COLOSSAL_FURNACE_CORE = REGISTER.register("colossal_furnace_core",
            () -> new BlockItem(ModBlocks.COLOSSAL_FURNACE_CORE.get(), new Item.Properties()));
    public static final RegistryObject<Item> COLOSSAL_FURNACE_WALL = REGISTER.register("colossal_furnace_wall",
            () -> new BlockItem(ModBlocks.COLOSSAL_FURNACE_WALL.get(), new Item.Properties()));
    public static final RegistryObject<Item> COLOSSAL_FURNACE_INTERFACE = REGISTER.register("colossal_furnace_interface",
            () -> new ColossalFurnaceInterfaceBlockItem(ModBlocks.COLOSSAL_FURNACE_INTERFACE.get(), new Item.Properties()));
    public static final RegistryObject<Item> COLOSSAL_SMOKER_CORE = REGISTER.register("colossal_smoker_core",
            () -> new BlockItem(ModBlocks.COLOSSAL_SMOKER_CORE.get(), new Item.Properties()));
    public static final RegistryObject<Item> COLOSSAL_BLAST_FURNACE_CORE = REGISTER.register("colossal_blast_furnace_core",
            () -> new BlockItem(ModBlocks.COLOSSAL_BLAST_FURNACE_CORE.get(), new Item.Properties()));

    private ModItems() {
    }
}

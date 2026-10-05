package com.colossalfurnaces.menu;

import com.colossalfurnaces.blockentity.ColossalBlastFurnaceControllerBlockEntity;
import com.colossalfurnaces.registry.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;

public class ColossalBlastFurnaceMenu extends ColossalFurnaceMenu {
    public ColossalBlastFurnaceMenu(int containerId, Inventory inventory, FriendlyByteBuf buffer) {
        super(ModMenus.COLOSSAL_BLAST_FURNACE.get(), containerId, inventory, getControllerFromBuffer(inventory, buffer, ColossalBlastFurnaceControllerBlockEntity.class));
    }

    public ColossalBlastFurnaceMenu(int containerId, Inventory inventory, ColossalBlastFurnaceControllerBlockEntity controller, ContainerData data) {
        super(ModMenus.COLOSSAL_BLAST_FURNACE.get(), containerId, inventory, controller, data);
    }
}

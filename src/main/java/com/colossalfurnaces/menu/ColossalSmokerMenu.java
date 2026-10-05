package com.colossalfurnaces.menu;

import com.colossalfurnaces.blockentity.ColossalSmokerControllerBlockEntity;
import com.colossalfurnaces.registry.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;

public class ColossalSmokerMenu extends ColossalFurnaceMenu {
    public ColossalSmokerMenu(int containerId, Inventory inventory, FriendlyByteBuf buffer) {
        super(ModMenus.COLOSSAL_SMOKER.get(), containerId, inventory, getControllerFromBuffer(inventory, buffer, ColossalSmokerControllerBlockEntity.class));
    }

    public ColossalSmokerMenu(int containerId, Inventory inventory, ColossalSmokerControllerBlockEntity controller, ContainerData data) {
        super(ModMenus.COLOSSAL_SMOKER.get(), containerId, inventory, controller, data);
    }
}

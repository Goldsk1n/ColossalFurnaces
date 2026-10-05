package com.colossalfurnaces.registry;

import com.colossalfurnaces.ColossalFurnacesMod;
import com.colossalfurnaces.menu.ColossalBlastFurnaceMenu;
import com.colossalfurnaces.menu.ColossalFurnaceMenu;
import com.colossalfurnaces.menu.ColossalSmokerMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> REGISTER = DeferredRegister.create(Registries.MENU, ColossalFurnacesMod.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<ColossalFurnaceMenu>> COLOSSAL_FURNACE =
            REGISTER.register("colossal_furnace", () -> IMenuTypeExtension.create(ColossalFurnaceMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<ColossalSmokerMenu>> COLOSSAL_SMOKER =
            REGISTER.register("colossal_smoker", () -> IMenuTypeExtension.create(ColossalSmokerMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<ColossalBlastFurnaceMenu>> COLOSSAL_BLAST_FURNACE =
            REGISTER.register("colossal_blast_furnace", () -> IMenuTypeExtension.create(ColossalBlastFurnaceMenu::new));

    private ModMenus() {
    }
}

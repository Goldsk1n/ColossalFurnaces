package com.colossalfurnaces.registry;

import com.colossalfurnaces.ColossalFurnacesMod;
import com.colossalfurnaces.menu.ColossalBlastFurnaceMenu;
import com.colossalfurnaces.menu.ColossalFurnaceMenu;
import com.colossalfurnaces.menu.ColossalSmokerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> REGISTER = DeferredRegister.create(ForgeRegistries.MENU_TYPES, ColossalFurnacesMod.MOD_ID);

    public static final RegistryObject<MenuType<ColossalFurnaceMenu>> COLOSSAL_FURNACE =
            REGISTER.register("colossal_furnace", () -> IForgeMenuType.create(ColossalFurnaceMenu::new));
    public static final RegistryObject<MenuType<ColossalSmokerMenu>> COLOSSAL_SMOKER =
            REGISTER.register("colossal_smoker", () -> IForgeMenuType.create(ColossalSmokerMenu::new));
    public static final RegistryObject<MenuType<ColossalBlastFurnaceMenu>> COLOSSAL_BLAST_FURNACE =
            REGISTER.register("colossal_blast_furnace", () -> IForgeMenuType.create(ColossalBlastFurnaceMenu::new));

    private ModMenus() {
    }
}

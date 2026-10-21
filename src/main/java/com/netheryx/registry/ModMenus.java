package com.netheryx.registry;

import com.netheryx.NetheryX;
import com.netheryx.menu.SoulAltarMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Menus (interfaces avec inventaire) du mod. */
public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, NetheryX.MOD_ID);

    public static final RegistryObject<MenuType<SoulAltarMenu>> SOUL_ALTAR = MENUS.register("soul_altar",
            () -> IForgeMenuType.create(SoulAltarMenu::new));

    private ModMenus() {
    }
}

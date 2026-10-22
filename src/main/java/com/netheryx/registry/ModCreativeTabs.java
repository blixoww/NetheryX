package com.netheryx.registry;

import com.netheryx.NetheryX;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/** Un seul onglet creatif : tout le contenu de NetheryX y est regroupe (rien n'est ajoute aux onglets vanilla). */
public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, NetheryX.MOD_ID);

    public static final RegistryObject<CreativeModeTab> NETHERYX = TABS.register("netheryx", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.netheryx"))
            .icon(() -> new ItemStack(ModItems.ONYX.get()))
            .displayItems((parameters, output) -> orderedItems().forEach(item -> output.accept(item.get())))
            .build());

    /**
     * Tous les objets et blocs du mod, dans l'ordre d'inscription, sauf que chaque ensemble d'armure reste groupe
     * (le plastron de carapace, plus ancien, est replace avec les autres pieces).
     */
    private static java.util.List<RegistryObject<net.minecraft.world.item.Item>> orderedItems() {
        java.util.List<RegistryObject<net.minecraft.world.item.Item>> items = new java.util.ArrayList<>(ModItems.ITEMS.getEntries());
        items.remove(ModItems.CARAPACE_CHESTPLATE);
        items.add(items.indexOf(ModItems.CARAPACE_HELMET) + 1, ModItems.CARAPACE_CHESTPLATE);
        return items;
    }

    private ModCreativeTabs() {
    }
}

package com.netheryx.registry;

import com.netheryx.NetheryX;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, NetheryX.MOD_ID);

    public static final RegistryObject<CreativeModeTab> NETHERYX = TABS.register("netheryx", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.netheryx"))
            .icon(() -> new ItemStack(ModItems.ONYX.get()))
            .displayItems((parameters, output) -> ModItems.ITEMS.getEntries().forEach(item -> output.accept(item.get())))
            .build());

    /** Les oeufs et l'equipement apparaissent aussi dans les onglets vanilla correspondants. */
    @Mod.EventBusSubscriber(modid = NetheryX.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class VanillaTabs {
        private VanillaTabs() {
        }

        @SubscribeEvent
        public static void onBuildTabs(BuildCreativeModeTabContentsEvent event) {
            if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
                ModItems.ITEMS.getEntries().stream().map(RegistryObject::get)
                        .filter(item -> item instanceof ForgeSpawnEggItem).forEach(event::accept);
            } else if (event.getTabKey() == CreativeModeTabs.COMBAT) {
                event.accept(ModItems.ONYX_SWORD);
                event.accept(ModItems.ONYX_BOW);
                event.accept(ModItems.ONYX_HELMET);
                event.accept(ModItems.ONYX_CHESTPLATE);
                event.accept(ModItems.ONYX_LEGGINGS);
                event.accept(ModItems.ONYX_BOOTS);
            } else if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
                event.accept(ModItems.ONYX_PICKAXE);
                event.accept(ModItems.ONYX_AXE);
                event.accept(ModItems.ONYX_SHOVEL);
                event.accept(ModItems.ONYX_HOE);
                event.accept(ModItems.ONYX_HAMMER);
                event.accept(ModItems.SOUL_COMPASS);
                event.accept(ModItems.RECALL_STONE);
            }
        }
    }

    private ModCreativeTabs() {
    }
}

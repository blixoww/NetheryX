package com.netheryx.registry;

import com.mojang.serialization.Codec;
import com.netheryx.NetheryX;
import com.netheryx.loot.AddItemLootModifier;
import com.netheryx.loot.InfusionSmeltingModifier;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Modificateurs de butin globaux : injectent les objets NetheryX dans les coffres vanilla sans les remplacer. */
public final class ModLootModifiers {
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIERS =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, NetheryX.MOD_ID);

    public static final RegistryObject<Codec<AddItemLootModifier>> ADD_ITEM = LOOT_MODIFIERS.register("add_item", () -> AddItemLootModifier.CODEC);
    public static final RegistryObject<Codec<InfusionSmeltingModifier>> INFUSION_SMELTING = LOOT_MODIFIERS.register("infusion_smelting",
            () -> InfusionSmeltingModifier.CODEC);

    private ModLootModifiers() {
    }
}

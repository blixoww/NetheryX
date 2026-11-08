package com.netheryx.registry;

import com.netheryx.NetheryX;
import com.netheryx.worldgen.placement.OutsideStructuresFilter;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/** Modificateurs de placement des decors du mod. */
public final class ModPlacements {
    public static final DeferredRegister<PlacementModifierType<?>> PLACEMENT_MODIFIERS =
            DeferredRegister.create(Registries.PLACEMENT_MODIFIER_TYPE, NetheryX.MOD_ID);

    public static final RegistryObject<PlacementModifierType<OutsideStructuresFilter>> OUTSIDE_STRUCTURES =
            PLACEMENT_MODIFIERS.register("outside_structures", () -> () -> OutsideStructuresFilter.CODEC);

    private ModPlacements() {
    }
}

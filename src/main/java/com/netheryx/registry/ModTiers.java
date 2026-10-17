package com.netheryx.registry;

import com.netheryx.NetheryX;
import com.netheryx.util.ModTags;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.ForgeTier;
import net.minecraftforge.common.TierSortingRegistry;

import java.util.List;

/**
 * Paliers d'outils. L'Onyx se place entre le diamant et la netherite grace au
 * {@link TierSortingRegistry}, ce qui le rend compatible avec les paliers ajoutes par d'autres mods.
 */
public final class ModTiers {
    public static final Tier ONYX = TierSortingRegistry.registerTier(
            new ForgeTier(4, 4500, 9.5F, 3.5F, 18, ModTags.Blocks.NEEDS_ONYX_TOOL,
                    () -> Ingredient.of(ModItems.ONYX.get())),
            NetheryX.id("onyx"), List.<Object>of(Tiers.DIAMOND), List.<Object>of(Tiers.NETHERITE));

    private ModTiers() {
    }

    /** Force le chargement de la classe pendant la construction du mod. */
    public static void init() {
    }
}

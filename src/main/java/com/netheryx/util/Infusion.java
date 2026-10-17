package com.netheryx.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;

/**
 * Infusions d'onyx : un bonus permanent ajoute a un outil, une arme ou une armure a la table de forgeron
 * (gabarit d'infusion d'onyx + objet + ingredient). Stocke dans le NBT {@code NetheryXInfusion}.
 */
public enum Infusion {
    /** Creme de magma : les blocs mines sont directement fondus. */
    SMELTING,
    /** Croc de fouisseur : +1,5 bloc de portee. */
    REACH,
    /** Onyx : l'objet se repare lentement tout seul. */
    RESILIENCE;

    public static final String TAG = "NetheryXInfusion";

    public String id() {
        return this.name().toLowerCase(Locale.ROOT);
    }

    public static Infusion byId(String id) {
        for (Infusion infusion : values()) {
            if (infusion.id().equals(id)) {
                return infusion;
            }
        }
        return null;
    }

    public static Infusion of(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null || !tag.contains(TAG) ? null : byId(tag.getString(TAG));
    }

    public boolean isOn(ItemStack stack) {
        return of(stack) == this;
    }

    public ItemStack applyTo(ItemStack stack) {
        stack.getOrCreateTag().putString(TAG, this.id());
        return stack;
    }
}

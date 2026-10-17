package com.netheryx.util;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.ModList;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Integration optionnelle de Curios API, par reflexion : aucune dependance n'est necessaire a la compilation
 * et le mod fonctionne normalement sans Curios. Gere l'API recente ({@code CuriosApi.getCuriosInventory})
 * et l'ancienne ({@code CuriosApi.getCuriosHelper().findFirstCurio}).
 */
public final class CuriosCompat {
    private static Boolean loaded;
    private static boolean broken;

    private CuriosCompat() {
    }

    private static boolean isLoaded() {
        if (loaded == null) {
            loaded = ModList.get() != null && ModList.get().isLoaded("curios");
        }
        return loaded && !broken;
    }

    /** Vrai si l'objet est equipe dans un emplacement Curios de l'entite. */
    public static boolean isEquipped(LivingEntity entity, Item item) {
        if (!isLoaded()) {
            return false;
        }
        try {
            Class<?> api = Class.forName("top.theillusivec4.curios.api.CuriosApi");
            try {
                Method inventory = api.getMethod("getCuriosInventory", LivingEntity.class);
                Object lazy = inventory.invoke(null, entity);
                Method resolve = Class.forName("net.minecraftforge.common.util.LazyOptional").getMethod("resolve");
                Optional<?> handler = (Optional<?>) resolve.invoke(lazy);
                if (handler.isEmpty()) {
                    return false;
                }
                Method find = Class.forName("top.theillusivec4.curios.api.type.capability.ICuriosItemHandler")
                        .getMethod("findFirstCurio", Item.class);
                return ((Optional<?>) find.invoke(handler.get(), item)).isPresent();
            } catch (NoSuchMethodException newApiMissing) {
                Object helper = api.getMethod("getCuriosHelper").invoke(null);
                Method find = Class.forName("top.theillusivec4.curios.api.type.util.ICuriosHelper")
                        .getMethod("findFirstCurio", LivingEntity.class, Item.class);
                return ((Optional<?>) find.invoke(helper, entity, item)).isPresent();
            }
        } catch (ReflectiveOperationException | RuntimeException | LinkageError error) {
            broken = true;
            return false;
        }
    }
}

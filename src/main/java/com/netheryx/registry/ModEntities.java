package com.netheryx.registry;

import com.netheryx.NetheryX;
import com.netheryx.entity.*;
import cpw.mods.fml.common.registry.EntityRegistry;
import net.minecraft.entity.Entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Creatures du mod. Chacune a un oeuf d'apparition (couleurs ci-dessous) ; leur nom interne est « netheryx.&lt;nom&gt; ».
 */
public final class ModEntities {
    public static final class Info {
        public final String name;
        public final Class<? extends Entity> type;
        public final int primary;
        public final int secondary;

        Info(String name, Class<? extends Entity> type, int primary, int secondary) {
            this.name = name;
            this.type = type;
            this.primary = primary;
            this.secondary = secondary;
        }
    }

    /** Creatures qui ont un oeuf d'apparition, dans l'ordre des metadonnees de l'oeuf. */
    public static final List<Info> EGGS = new ArrayList<Info>();
    private static int nextId;

    private ModEntities() {
    }

    public static void register() {
        mob("ember_stalker", EntityEmberStalker.class, 0x8A2A0E, 0xFFB020);
        mob("ash_wraith", EntityAshWraith.class, 0x5A5A60, 0xC8C8D0);
        mob("withered_knight", EntityWitheredKnight.class, 0x2A3A3A, 0x6A8A8A);
        mob("fire_ghast", EntityFireGhast.class, 0xF0E0D0, 0xFF6A1A);
        mob("onyx_cube", EntityOnyxCube.class, 0x2C2042, 0xB48CFF);
        mob("basalt_burrower", EntityBasaltBurrower.class, 0x3A3A44, 0xFF7A1A);
        mob("mimic", EntityMimic.class, 0x6A4A24, 0xD8A43A);
        mob("ember_cow", EntityEmberCow.class, 0x8A3418, 0xFFB04A);
        mob("cinder_hen", EntityCinderHen.class, 0x6A6A6A, 0xE0601A);
        mob("blaze_pig", EntityBlazePig.class, 0xC05A20, 0xFFD040);
        mob("crystal_snail", EntityCrystalSnail.class, 0x463E50, 0xC9A6FF);
        mob("magma_crab", EntityMagmaCrab.class, 0xA8300E, 0xFF9A2A);
        mob("basalt_beetle", EntityBasaltBeetle.class, 0x24242A, 0x6E6E7A);
        mob("magmafish", EntityMagmafish.class, 0xB0380E, 0xFFE27A);
        mob("lantern_bearer", EntityLanternBearer.class, 0x123A44, 0x7FE8FF);
        mob("cinder_lizard", EntityCinderLizard.class, 0x5A2A10, 0xE07C28);
        mob("piglin_merchant", EntityPiglinMerchant.class, 0xD07A60, 0xF6D670);
        mob("ember_firefly", EntityEmberFirefly.class, 0x3A1A0A, 0xFFC040);
        mob("soul_firefly", EntitySoulFirefly.class, 0x0E1E24, 0x6AF0FF);
        mob("soul_moth", EntitySoulMoth.class, 0xC8D0D4, 0x4AD8F0);
        mob("ash_wyvern", EntityAshWyvern.class, 0x4A4A50, 0xE0601A);
        mob("onyx_sentinel", EntityOnyxSentinel.class, 0x140D1E, 0x9A6AEA);
        mob("infernal_warlock", EntityInfernalWarlock.class, 0x5A0C0A, 0xFF8A2A);
        mob("withered_champion", EntityWitheredChampion.class, 0x1E2A2E, 0x1A9A80);
        mob("basalt_golem", EntityBasaltGolem.class, 0x26262C, 0xFF7A1A);
        mob("nether_heart", EntityNetherHeart.class, 0x3A0B12, 0xFF6A2A);
        EntityRegistry.registerModEntity(EntitySulfurBomb.class, "sulfur_bomb", nextId++, NetheryX.instance, 64, 10, true);
        EntityRegistry.registerModEntity(com.netheryx.item.ItemWyvernCrystal.SafeItem.class, "wyvern_crystal_item", nextId++,
                NetheryX.instance, 64, 20, true);
    }

    private static void mob(String name, Class<? extends Entity> type, int primary, int secondary) {
        EntityRegistry.registerModEntity(type, name, nextId++, NetheryX.instance, 80, 3, true);
        EGGS.add(new Info(name, type, primary, secondary));
    }
}

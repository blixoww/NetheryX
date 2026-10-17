package com.netheryx.util;

import com.netheryx.NetheryX;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.Structure;

/** Tags utilises par le code. Leur contenu est defini en JSON pour rester modifiable par les packs. */
public final class ModTags {
    private ModTags() {
    }

    public static final class Blocks {
        public static final TagKey<Block> NEEDS_ONYX_TOOL = mod("needs_onyx_tool");
        /** Blocs sur lesquels poussent les plantes du Nether de NetheryX. */
        public static final TagKey<Block> NETHER_PLANT_SOIL = mod("nether_plant_soil");
        public static final TagKey<Block> BLAZE_PEPPER_SOIL = mod("blaze_pepper_soil");
        public static final TagKey<Block> SOUL_TUBER_SOIL = mod("soul_tuber_soil");
        public static final TagKey<Block> MAGMA_CRAB_SPAWNABLE_ON = mod("magma_crab_spawnable_on");
        public static final TagKey<Block> BASALT_BEETLE_SPAWNABLE_ON = mod("basalt_beetle_spawnable_on");
        public static final TagKey<Block> CINDER_WHEAT_SOIL = mod("cinder_wheat_soil");
        /** Sols sur lesquels les animaux NetheryX (vaches, poules) peuvent apparaitre. */
        public static final TagKey<Block> NETHER_ANIMALS_SPAWNABLE_ON = mod("nether_animals_spawnable_on");
        /** Cultures accelerees par le soufre. */
        public static final TagKey<Block> SULFUR_FERTILIZABLE = mod("sulfur_fertilizable");
        /** Blocs que le Fouisseur de basalte peut creuser. */
        public static final TagKey<Block> BURROWER_DIGGABLE = mod("burrower_diggable");
        /** Sols ou le Lezard de cendre court plus vite. */
        public static final TagKey<Block> CINDER_LIZARD_FAST_GROUND = mod("cinder_lizard_fast_ground");
        /** Sols du Nether acceleres par l'enchantement Pas de braise. */
        public static final TagKey<Block> EMBER_STRIDE_GROUND = mod("ember_stride_ground");
        /** Sols vegetaux (nylium, herbe) acceleres par l'armure de braise. */
        public static final TagKey<Block> NETHER_VEGETATION_GROUND = mod("nether_vegetation_ground");

        private static TagKey<Block> mod(String name) {
            return TagKey.create(Registries.BLOCK, NetheryX.id(name));
        }

        private Blocks() {
        }
    }

    public static final class Items {
        public static final TagKey<Item> GEMS_ONYX = forge("gems/onyx");
        public static final TagKey<Item> MAGMA_CRAB_FOOD = TagKey.create(Registries.ITEM, NetheryX.id("magma_crab_food"));
        public static final TagKey<Item> EMBER_COW_FOOD = TagKey.create(Registries.ITEM, NetheryX.id("ember_cow_food"));
        public static final TagKey<Item> CINDER_HEN_FOOD = TagKey.create(Registries.ITEM, NetheryX.id("cinder_hen_food"));
        public static final TagKey<Item> CINDER_LIZARD_FOOD = TagKey.create(Registries.ITEM, NetheryX.id("cinder_lizard_food"));
        public static final TagKey<Item> LANTERN_BEARER_FOOD = TagKey.create(Registries.ITEM, NetheryX.id("lantern_bearer_food"));
        public static final TagKey<Item> LAVA_TRAP_BAIT = TagKey.create(Registries.ITEM, NetheryX.id("lava_trap_bait"));

        private static TagKey<Item> forge(String name) {
            return TagKey.create(Registries.ITEM, new ResourceLocation("forge", name));
        }

        private Items() {
        }
    }

    public static final class Structures {
        public static final TagKey<Structure> SOUL_COMPASS_TARGETS =
                TagKey.create(Registries.STRUCTURE, NetheryX.id("soul_compass_targets"));

        private Structures() {
        }
    }
}

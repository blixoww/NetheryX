package com.netheryx;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

/** Reglages du fichier config/netheryx.cfg : frequence des minerais, des arbres et de la flore dans le Nether. */
public final class NetheryXConfig {
    public static int onyxVeinsPerChunk = 6;
    public static int sulfurVeinsPerChunk = 10;
    public static int richQuartzVeinsPerChunk = 6;
    public static int rimestoneBlobsPerChunk = 2;
    public static int treeAttemptsPerChunk = 3;
    public static int floraPatchesPerChunk = 4;
    public static int biomeIdStart = 200;
    public static boolean netherBiomes = true;
    public static int structureSpacing = 14;
    public static int recallCooldown = 60;
    public static int enchantmentIdStart = 120;

    private NetheryXConfig() {
    }

    public static void load(File file) {
        Configuration config = new Configuration(file);
        config.load();
        String ores = "minerais";
        onyxVeinsPerChunk = config.getInt("onyxVeinsPerChunk", ores, onyxVeinsPerChunk, 0, 64, "Filons d'onyx par chunk du Nether");
        sulfurVeinsPerChunk = config.getInt("sulfurVeinsPerChunk", ores, sulfurVeinsPerChunk, 0, 64, "Filons de soufre par chunk du Nether");
        richQuartzVeinsPerChunk = config.getInt("richQuartzVeinsPerChunk", ores, richQuartzVeinsPerChunk, 0, 64,
                "Filons de quartz riche par chunk du Nether");
        rimestoneBlobsPerChunk = config.getInt("rimestoneBlobsPerChunk", ores, rimestoneBlobsPerChunk, 0, 32,
                "Poches de pierre de givre par chunk du Nether");
        String nature = "nature";
        treeAttemptsPerChunk = config.getInt("treeAttemptsPerChunk", nature, treeAttemptsPerChunk, 0, 32,
                "Tentatives d'arbres (braise, fletris) par chunk du Nether");
        floraPatchesPerChunk = config.getInt("floraPatchesPerChunk", nature, floraPatchesPerChunk, 0, 32,
                "Massifs de fleurs et d'herbe du Nether par chunk");
        String world = "monde";
        biomeIdStart = config.getInt("biomeIdStart", world, biomeIdStart, 40, 243,
                "Premier identifiant de biome utilise par les 12 biomes du Nether (a changer en cas de conflit avec un autre mod)");
        netherBiomes = config.getBoolean("netherBiomes", world, netherBiomes,
                "Remplace le Nether vanilla par le Nether a biomes de NetheryX (desactiver en cas d'incompatibilite)");
        structureSpacing = config.getInt("structureSpacing", world, structureSpacing, 8, 128,
                "Espacement moyen des structures du Nether, en chunks (plus petit = plus de structures)");
        String gameplay = "gameplay";
        recallCooldown = config.getInt("recallCooldown", gameplay, recallCooldown, 0, 3600,
                "Temps de recharge de la pierre de rappel, en secondes");
        enchantmentIdStart = config.getInt("enchantmentIdStart", gameplay, enchantmentIdStart, 63, 253,
                "Premier identifiant des 3 enchantements de l'autel des ames (a changer en cas de conflit avec un autre mod)");
        if (config.hasChanged()) {
            config.save();
        }
    }
}

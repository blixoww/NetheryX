package com.netheryx.config;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Configuration commune (serveur + client) de NetheryX.
 * Fichier genere : {@code config/netheryx-common.toml}.
 */
public final class NetheryXConfig {
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.IntValue REGION_WEIGHT;
    public static final ForgeConfigSpec.BooleanValue ONYX_SET_BONUS;
    public static final ForgeConfigSpec.BooleanValue HAMMER_AREA_MINING;
    public static final ForgeConfigSpec.IntValue SOUL_COMPASS_SEARCH_RADIUS;
    public static final ForgeConfigSpec.BooleanValue SULFUR_BOMB_BREAKS_BLOCKS;
    public static final ForgeConfigSpec.DoubleValue SENTINEL_MAX_HEALTH;
    public static final ForgeConfigSpec.IntValue MAGMA_TREADS_RADIUS;
    public static final ForgeConfigSpec.BooleanValue GIVE_GUIDE_ON_FIRST_JOIN;
    public static final ForgeConfigSpec.IntValue RECALL_STONE_COOLDOWN;
    public static final ForgeConfigSpec.DoubleValue WARLOCK_MAX_HEALTH;
    public static final ForgeConfigSpec.DoubleValue CHAMPION_MAX_HEALTH;
    public static final ForgeConfigSpec.BooleanValue BURROWER_DIGS;
    public static final ForgeConfigSpec.BooleanValue NETHER_EVENTS;
    public static final ForgeConfigSpec.IntValue NETHER_EVENT_MIN_DELAY;
    public static final ForgeConfigSpec.IntValue NETHER_EVENT_MAX_DELAY;
    public static final ForgeConfigSpec.IntValue WARD_LANTERN_RADIUS;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment("Generation du monde").push("worldgen");
        REGION_WEIGHT = builder
                .comment("Poids de la region NetheryX dans le Nether (TerraBlender). 0 desactive les biomes NetheryX.")
                .worldRestart()
                .defineInRange("netherRegionWeight", 10, 0, 100);
        builder.pop();

        builder.comment("Equipements et objets").push("items");
        ONYX_SET_BONUS = builder
                .comment("L'armure complete en Onyx donne l'effet Resistance I.")
                .define("onyxSetBonus", true);
        HAMMER_AREA_MINING = builder
                .comment("Le marteau en Onyx mine une zone de 3x3 (s'accroupir pour miner un seul bloc).")
                .define("hammerAreaMining", true);
        SOUL_COMPASS_SEARCH_RADIUS = builder
                .comment("Rayon de recherche (en chunks) de la Boussole des ames.")
                .defineInRange("soulCompassSearchRadius", 64, 8, 256);
        SULFUR_BOMB_BREAKS_BLOCKS = builder
                .comment("Les bombes de soufre detruisent les blocs.")
                .define("sulfurBombBreaksBlocks", true);
        MAGMA_TREADS_RADIUS = builder
                .comment("Rayon de solidification de la lave par les Semelles de magma.")
                .defineInRange("magmaTreadsRadius", 2, 1, 6);
        GIVE_GUIDE_ON_FIRST_JOIN = builder
                .comment("Donne le Codex NetheryX (livre-guide Patchouli) a la premiere connexion d'un joueur.")
                .define("giveGuideOnFirstJoin", true);
        RECALL_STONE_COOLDOWN = builder
                .comment("Temps de recharge (en secondes) de la Pierre de rappel.")
                .defineInRange("recallStoneCooldown", 60, 0, 3600);
        builder.pop();

        builder.comment("Creatures").push("entities");
        SENTINEL_MAX_HEALTH = builder
                .comment("Points de vie de la Sentinelle d'Onyx (boss).")
                .defineInRange("sentinelMaxHealth", 250.0D, 50.0D, 2048.0D);
        WARLOCK_MAX_HEALTH = builder
                .comment("Points de vie du Sorcier infernal (mini-boss).")
                .defineInRange("warlockMaxHealth", 150.0D, 20.0D, 2048.0D);
        CHAMPION_MAX_HEALTH = builder
                .comment("Points de vie du Champion fletri (boss des Catacombes fletries).")
                .defineInRange("championMaxHealth", 200.0D, 50.0D, 2048.0D);
        BURROWER_DIGS = builder
                .comment("Le Fouisseur de basalte creuse des tunnels (respecte aussi la regle mobGriefing).")
                .define("burrowerDigs", true);
        builder.pop();

        builder.comment("Evenements du Nether (pluie de cendres, tempete de soufre, eruption)").push("events");
        NETHER_EVENTS = builder
                .comment("Active les evenements dynamiques du Nether.")
                .define("netherEvents", true);
        NETHER_EVENT_MIN_DELAY = builder
                .comment("Delai minimal (en minutes) entre deux evenements.")
                .defineInRange("minDelayMinutes", 20, 1, 600);
        NETHER_EVENT_MAX_DELAY = builder
                .comment("Delai maximal (en minutes) entre deux evenements.")
                .defineInRange("maxDelayMinutes", 45, 1, 600);
        builder.pop();

        builder.comment("Blocs").push("blocks");
        WARD_LANTERN_RADIUS = builder
                .comment("Rayon (en blocs) dans lequel une Lanterne de garde empeche l'apparition naturelle des monstres.")
                .defineInRange("wardLanternRadius", 24, 4, 64);
        builder.pop();

        SPEC = builder.build();
    }

    private NetheryXConfig() {
    }
}

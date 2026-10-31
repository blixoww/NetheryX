package com.netheryx.registry;

import com.netheryx.NetheryXConfig;
import com.netheryx.entity.*;
import com.netheryx.world.NetherDecor;
import com.netheryx.world.WorldGenNetherTree;
import com.netheryx.world.biome.BiomeNether;
import com.netheryx.world.biome.NetherWorldProvider;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.init.Blocks;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.DimensionManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Les 12 biomes du Nether de NetheryX (et le Nether vanilla, qui reste l'un d'eux). Chacun a son sol, sa decoration
 * et ses creatures.
 */
public final class ModBiomes {
    private static final List<BiomeGenBase> ALL = new ArrayList<BiomeGenBase>();

    public static BiomeNether emberMeadow;
    public static BiomeNether emberGrove;
    public static BiomeNether ashenWastes;
    public static BiomeNether sulfurSprings;
    public static BiomeNether soulfireMarsh;
    public static BiomeNether witheredForest;
    public static BiomeNether magmaRift;
    public static BiomeNether marrowThicket;
    public static BiomeNether rimefrostChasm;
    public static BiomeNether wartvineTangle;
    public static BiomeNether quartzReef;
    public static BiomeNether petrifiedForest;

    private ModBiomes() {
    }

    public static BiomeGenBase[] all() {
        return ALL.toArray(new BiomeGenBase[0]);
    }

    public static void register() {
        int id = NetheryXConfig.biomeIdStart;
        final WorldGenNetherTree ember = new WorldGenNetherTree(ModBlocks.emberStem, ModBlocks.emberWartBlock, null,
                ModBlocks.emberNylium, Blocks.glowstone, false);
        final WorldGenNetherTree withered = new WorldGenNetherTree(ModBlocks.witheredStem, ModBlocks.witheredWartBlock, null,
                null, null, true);
        final WorldGenNetherTree marrow = new WorldGenNetherTree(ModBlocks.marrowStem, ModBlocks.marrowCap, null,
                ModBlocks.ossifiedNylium, null, false);
        final WorldGenNetherTree wartstalk = new WorldGenNetherTree(ModBlocks.wartstalkStem, ModBlocks.wartMoss, null,
                null, ModBlocks.wartBulb, true);
        final WorldGenNetherTree petrified = new WorldGenNetherTree(ModBlocks.petrifiedLog, ModBlocks.onyxCrystalLeaves, null,
                null, null, false);

        emberMeadow = add(new BiomeNether(id++, "Ember Meadow", ModBlocks.netherGrassBlock, Blocks.netherrack, 0xC8501E)
                .decorator((w, r, x, z) -> {
                    NetherDecor.plants(w, r, x, z, 5, ModBlocks.netherGrass, ModBlocks.netherGrass, ModBlocks.emberBloom,
                            ModBlocks.crimsonBellflower, ModBlocks.sulfurTulip, ModBlocks.blazingGrass);
                    NetherDecor.plants(w, r, x, z, 1, ModBlocks.fireBerryBush);
                    NetherDecor.trees(w, r, x, z, 1, ember);
                })
                .creature(EntityEmberCow.class, 12, 2, 4).creature(EntityCinderHen.class, 10, 2, 4)
                .creature(EntityCinderLizard.class, 4, 1, 2).ambient(EntityEmberFirefly.class, 10, 4, 8));
        emberGrove = add(new BiomeNether(id++, "Ember Grove", ModBlocks.emberNylium, Blocks.netherrack, 0xA83C12)
                .decorator((w, r, x, z) -> {
                    NetherDecor.trees(w, r, x, z, 6, ember);
                    NetherDecor.plants(w, r, x, z, 3, ModBlocks.emberRoots, ModBlocks.emberFungus, ModBlocks.emberBloom);
                })
                .monster(EntityEmberStalker.class, 40, 1, 3).creature(EntityBlazePig.class, 10, 2, 4)
                .ambient(EntityEmberFirefly.class, 12, 4, 8));
        ashenWastes = add(new BiomeNether(id++, "Ashen Wastes", ModBlocks.ashBlock, Blocks.netherrack, 0x8A8480)
                .decorator((w, r, x, z) -> {
                    NetherDecor.plants(w, r, x, z, 3, ModBlocks.charredShrub, ModBlocks.ashDaisy);
                    NetherDecor.pools(w, r, x, z, 8, Blocks.lava);
                })
                .monster(EntityAshWraith.class, 30, 1, 2).creature(EntityBasaltBeetle.class, 10, 2, 3)
                .monster(EntityAshWyvern.class, 2, 1, 1));
        sulfurSprings = add(new BiomeNether(id++, "Sulfur Springs", ModBlocks.sulfurRock, ModBlocks.sulfurRock, 0xD8C254)
                .decorator((w, r, x, z) -> {
                    NetherDecor.crystals(w, r, x, z, 6, ModBlocks.sulfurCluster);
                    NetherDecor.plants(w, r, x, z, 2, ModBlocks.sulfurTulip);
                    NetherDecor.pools(w, r, x, z, 4, Blocks.lava);
                })
                .creature(EntityMagmaCrab.class, 12, 2, 4).monster(EntityOnyxCube.class, 15, 1, 3));
        soulfireMarsh = add(new BiomeNether(id++, "Soulfire Marsh", Blocks.soul_sand, Blocks.soul_sand, 0x2A8CA0)
                .decorator((w, r, x, z) -> NetherDecor.plants(w, r, x, z, 4, ModBlocks.soulLily, ModBlocks.soulLily, ModBlocks.glowcap))
                .monster(EntityAshWraith.class, 20, 1, 2).ambient(EntitySoulFirefly.class, 12, 4, 8)
                .ambient(EntitySoulMoth.class, 6, 1, 3).creature(EntityLanternBearer.class, 4, 1, 2));
        witheredForest = add(new BiomeNether(id++, "Withered Forest", ModBlocks.witheredSoil, Blocks.soul_sand, 0x4A6A6A)
                .decorator((w, r, x, z) -> {
                    NetherDecor.trees(w, r, x, z, 5, withered);
                    NetherDecor.plants(w, r, x, z, 2, ModBlocks.witheredFungus, ModBlocks.ashDaisy);
                })
                .monster(EntityWitheredKnight.class, 30, 1, 2).monster(EntitySkeleton.class, 10, 1, 2));
        magmaRift = add(new BiomeNether(id++, "Magma Rift", Blocks.netherrack, Blocks.netherrack, 0xB0380E)
                .decorator((w, r, x, z) -> {
                    NetherDecor.pools(w, r, x, z, 2, Blocks.lava);
                    NetherDecor.plants(w, r, x, z, 1, ModBlocks.blazingGrass, ModBlocks.charredShrub);
                })
                .creature(EntityMagmaCrab.class, 12, 2, 4).creature(EntityCinderLizard.class, 6, 1, 2)
                .monster(EntityBasaltGolem.class, 1, 1, 1).monster(EntityBlaze.class, 10, 1, 2));
        marrowThicket = add(new BiomeNether(id++, "Marrow Thicket", ModBlocks.ossifiedNylium, Blocks.netherrack, 0xD8CCB0)
                .decorator((w, r, x, z) -> {
                    NetherDecor.trees(w, r, x, z, 5, marrow);
                    NetherDecor.plants(w, r, x, z, 3, ModBlocks.marrowSprouts);
                })
                .monster(EntityBasaltBurrower.class, 15, 1, 1).creature(EntityCrystalSnail.class, 6, 1, 2));
        rimefrostChasm = add(new BiomeNether(id++, "Rimefrost Chasm", ModBlocks.rimestone, ModBlocks.rimestone, 0x6AD0F0)
                .decorator((w, r, x, z) -> {
                    NetherDecor.spikes(w, r, x, z, 2, ModBlocks.rimeIce, 3, 8);
                    NetherDecor.crystals(w, r, x, z, 5, ModBlocks.rimeCrystal);
                })
                .monster(EntityAshWraith.class, 15, 1, 2).creature(EntityCrystalSnail.class, 6, 1, 2));
        wartvineTangle = add(new BiomeNether(id++, "Wartvine Tangle", ModBlocks.wartMoss, Blocks.netherrack, 0x8A1A24)
                .decorator((w, r, x, z) -> {
                    NetherDecor.trees(w, r, x, z, 4, wartstalk);
                    NetherDecor.plants(w, r, x, z, 2, ModBlocks.crimsonBellflower, ModBlocks.emberRoots);
                })
                .creature(EntityBlazePig.class, 10, 2, 4).monster(EntityEmberStalker.class, 20, 1, 2)
                .creature(EntityPiglinMerchant.class, 3, 1, 1));
        quartzReef = add(new BiomeNether(id++, "Quartz Reef", Blocks.netherrack, Blocks.netherrack, 0xF4ECE0)
                .decorator((w, r, x, z) -> {
                    NetherDecor.spikes(w, r, x, z, 2, Blocks.quartz_block, 4, 10);
                    NetherDecor.crystals(w, r, x, z, 6, ModBlocks.quartzCluster);
                })
                .monster(EntityOnyxCube.class, 10, 1, 2).creature(EntityPiglinMerchant.class, 2, 1, 1));
        petrifiedForest = add(new BiomeNether(id++, "Petrified Onyx Forest", ModBlocks.polishedOnyx, Blocks.netherrack, 0x574276)
                .decorator((w, r, x, z) -> {
                    NetherDecor.trees(w, r, x, z, 4, petrified);
                    NetherDecor.crystals(w, r, x, z, 5, ModBlocks.onyxCluster);
                })
                .creature(EntityCrystalSnail.class, 10, 1, 3).monster(EntityOnyxCube.class, 15, 1, 3)
                .ambient(EntitySoulFirefly.class, 8, 3, 6));
        ALL.add(BiomeGenBase.hell);

        for (BiomeGenBase biome : ALL) {
            BiomeDictionary.registerBiomeType(biome, BiomeDictionary.Type.NETHER, BiomeDictionary.Type.HOT, BiomeDictionary.Type.DRY);
        }
        if (NetheryXConfig.netherBiomes) {
            DimensionManager.unregisterProviderType(-1);
            DimensionManager.registerProviderType(-1, NetherWorldProvider.class, true);
        }
    }

    private static BiomeNether add(BiomeNether biome) {
        ALL.add(biome);
        return biome;
    }
}

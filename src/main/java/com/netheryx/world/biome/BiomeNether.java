package com.netheryx.world.biome;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

import java.util.Random;

/**
 * Biome du Nether de NetheryX. Le relief reste celui du Nether vanilla ; le biome fixe le sol (bloc de surface et
 * sous-couche), la decoration (posee par {@link com.netheryx.world.NetherWorldGenerator}) et les creatures. Les
 * creatures vanilla du Nether (cochons-zombies, ghasts, cubes de magma) restent presentes partout.
 */
public class BiomeNether extends BiomeGenBase {
    public interface Decorator {
        void decorate(World world, Random random, int x, int z);
    }

    public final Block surface;
    public final Block filler;
    public final int fogColor;
    private Decorator decorator;

    public BiomeNether(int id, String name, Block surface, Block filler, int color) {
        super(id);
        this.surface = surface;
        this.filler = filler;
        this.fogColor = color;
        setBiomeName(name);
        setColor(color);
        setDisableRain();
        setTemperatureRainfall(2.0F, 0.0F);
        this.topBlock = surface;
        this.fillerBlock = filler;
        this.spawnableMonsterList.clear();
        this.spawnableCreatureList.clear();
        this.spawnableWaterCreatureList.clear();
        this.spawnableCaveCreatureList.clear();
        monster(EntityGhast.class, 30, 4, 4);
        monster(EntityPigZombie.class, 60, 4, 4);
        monster(EntityMagmaCube.class, 10, 4, 4);
    }

    public BiomeNether decorator(Decorator decorator) {
        this.decorator = decorator;
        return this;
    }

    public void decorateChunk(World world, Random random, int x, int z) {
        if (this.decorator != null) {
            this.decorator.decorate(world, random, x, z);
        }
    }

    @SuppressWarnings("unchecked")
    public BiomeNether monster(Class<? extends EntityLiving> entity, int weight, int min, int max) {
        this.spawnableMonsterList.add(new SpawnListEntry(entity, weight, min, max));
        return this;
    }

    @SuppressWarnings("unchecked")
    public BiomeNether creature(Class<? extends EntityLiving> entity, int weight, int min, int max) {
        this.spawnableCreatureList.add(new SpawnListEntry(entity, weight, min, max));
        return this;
    }

    @SuppressWarnings("unchecked")
    public BiomeNether ambient(Class<? extends EntityLiving> entity, int weight, int min, int max) {
        this.spawnableCaveCreatureList.add(new SpawnListEntry(entity, weight, min, max));
        return this;
    }

    /** Surface vanilla par defaut pour le sous-sol. */
    public static Block netherrack() {
        return Blocks.netherrack;
    }
}

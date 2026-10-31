package com.netheryx.world.structure;

import com.netheryx.NetheryXConfig;
import com.netheryx.registry.ModBiomes;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Structures du Nether : le monde est decoupe en regions carrees ({@code structureSpacing} chunks de cote) ; chacune
 * recoit au plus une structure, tiree d'apres la graine du monde, placee au chunk choisi si le terrain s'y prete
 * (sol assez plat, place libre). Les structures posees sont memorisees (boussole des ames).
 */
public final class NetherStructures {
    public static final class Type {
        public final String name;
        public final int weight;
        public final int sink;
        final BiomeGenBase[] biomes;

        Type(String name, int weight, int sink, BiomeGenBase... biomes) {
            this.name = name;
            this.weight = weight;
            this.sink = sink;
            this.biomes = biomes;
        }

        boolean allows(BiomeGenBase biome) {
            if (this.biomes.length == 0) {
                return true;
            }
            for (BiomeGenBase b : this.biomes) {
                if (b == biome) {
                    return true;
                }
            }
            return false;
        }
    }

    private static Type[] types;
    private static final Map<String, StructureTemplate> TEMPLATES = new HashMap<String, StructureTemplate>();

    private NetherStructures() {
    }

    public static void register() {
        types = new Type[]{
                new Type("onyx_citadel", 2, 0, ModBiomes.petrifiedForest, ModBiomes.quartzReef, ModBiomes.emberMeadow, BiomeGenBase.hell),
                new Type("infernal_sanctum", 2, 10, ModBiomes.magmaRift, ModBiomes.emberGrove, ModBiomes.sulfurSprings, BiomeGenBase.hell),
                new Type("withered_catacombs", 2, 16, ModBiomes.witheredForest, ModBiomes.soulfireMarsh, ModBiomes.ashenWastes),
                new Type("basalt_forge", 3, 0, ModBiomes.ashenWastes, ModBiomes.magmaRift, ModBiomes.sulfurSprings),
                new Type("wyvern_nest", 3, 1, ModBiomes.ashenWastes, ModBiomes.magmaRift, ModBiomes.sulfurSprings, ModBiomes.rimefrostChasm),
                new Type("traveler_camp", 6, 0),
                new Type("nether_garden", 4, 0, ModBiomes.emberMeadow, ModBiomes.emberGrove, ModBiomes.wartvineTangle, ModBiomes.marrowThicket),
                new Type("hidden_cache", 6, 7),
                new Type("nomad_caravan", 4, 0),
        };
    }

    private static StructureTemplate template(String name) {
        if (!TEMPLATES.containsKey(name)) {
            TEMPLATES.put(name, StructureTemplate.load(name));
        }
        return TEMPLATES.get(name);
    }

    private static long hash(long seed, int a, int b) {
        long h = seed ^ (a * 341873128712L) ^ (b * 132897987541L);
        h ^= h >>> 33;
        h *= 0xff51afd7ed558ccdL;
        h ^= h >>> 33;
        return h & Long.MAX_VALUE;
    }

    /** Appele pour chaque chunk du Nether decore : pose la structure de sa region si c'est son chunk de depart. */
    public static void generate(World world, Random random, int chunkX, int chunkZ) {
        int spacing = NetheryXConfig.structureSpacing;
        int rx = Math.floorDiv(chunkX, spacing);
        int rz = Math.floorDiv(chunkZ, spacing);
        long h = hash(world.getSeed(), rx, rz);
        int margin = Math.max(1, spacing - 4);
        if (rx * spacing + (int) (h % margin) != chunkX || rz * spacing + (int) ((h >> 16) % margin) != chunkZ) {
            return;
        }
        int x = chunkX * 16 + 8;
        int z = chunkZ * 16 + 8;
        BiomeGenBase biome = world.getBiomeGenForCoords(x + 8, z + 8);
        int total = 0;
        for (Type type : types) {
            if (type.allows(biome)) {
                total += type.weight;
            }
        }
        if (total == 0) {
            return;
        }
        int roll = (int) ((h >> 32) % total);
        for (Type type : types) {
            if (!type.allows(biome)) {
                continue;
            }
            roll -= type.weight;
            if (roll < 0) {
                tryPlace(world, random, type, x, z);
                return;
            }
        }
    }

    private static boolean tryPlace(World world, Random random, Type type, int x, int z) {
        StructureTemplate template = template(type.name);
        if (template == null) {
            return false;
        }
        int cx = x + template.sizeX / 2;
        int cz = z + template.sizeZ / 2;
        int above = Math.min(10, template.sizeY - type.sink);
        int floor = findFloor(world, cx, cz, above);
        if (floor < 0) {
            return false;
        }
        // Sol a peu pres plat sous les quatre coins (sauf structures enfouies)
        if (type.sink < 5) {
            int[][] corners = {{x, z}, {x + template.sizeX - 1, z}, {x, z + template.sizeZ - 1}, {x + template.sizeX - 1, z + template.sizeZ - 1}};
            for (int[] c : corners) {
                if (!hasFloorNear(world, c[0], c[1], floor, 5)) {
                    return false;
                }
            }
        }
        int y = floor + 1 - type.sink;
        template.place(world, random, x, y, z);
        StructureData.get(world).add(type.name, cx, y, cz);
        return true;
    }

    /** Sol solide avec assez d'air au-dessus, en partant du haut (sous le plafond du Nether). */
    private static int findFloor(World world, int x, int z, int air) {
        int run = 0;
        for (int y = 110; y > 32; y--) {
            Block block = world.getBlock(x, y, z);
            if (block.isAir(world, x, y, z)) {
                run++;
            } else {
                if (run >= air && block.getMaterial().isSolid() && !block.getMaterial().isLiquid()) {
                    return y;
                }
                run = 0;
            }
        }
        return -1;
    }

    private static boolean hasFloorNear(World world, int x, int z, int floor, int tolerance) {
        for (int y = floor - tolerance; y <= floor + tolerance; y++) {
            Block block = world.getBlock(x, y, z);
            if (block.getMaterial().isSolid() && world.isAirBlock(x, y + 1, z)) {
                return true;
            }
        }
        return false;
    }

    public static String[] names() {
        String[] names = new String[types.length];
        for (int i = 0; i < types.length; i++) {
            names[i] = types[i].name;
        }
        return names;
    }
}

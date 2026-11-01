package com.netheryx.gametest;

import com.mojang.datafixers.util.Pair;
import com.netheryx.NetheryX;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Sonde de generation du Nether : part de chaque biome, taille des taches de biomes, distance aux structures. Sert a
 * mesurer l'effet d'un reglage de generation au lieu de le juger a l'oeil.
 */
final class WorldgenProbe {
    /** Taille du releve de biomes, en cellules de STEP blocs. */
    private static final int GRID = 256;
    private static final int STEP = 16;
    private static final int BIOME_Y = 64;

    private WorldgenProbe() {
    }

    /** Part de chaque biome (en %) et taille moyenne de ses taches (en blocs de cote). */
    static Map<String, double[]> biomes(ServerLevel nether) {
        BiomeSource source = nether.getChunkSource().getGenerator().getBiomeSource();
        Climate.Sampler sampler = nether.getChunkSource().randomState().sampler();
        String[][] map = new String[GRID][GRID];
        for (int x = 0; x < GRID; x++) {
            for (int z = 0; z < GRID; z++) {
                Holder<Biome> biome = source.getNoiseBiome((x * STEP - GRID * STEP / 2) >> 2, BIOME_Y >> 2,
                        (z * STEP - GRID * STEP / 2) >> 2, sampler);
                map[x][z] = biome.unwrapKey().map(key -> key.location().toString()).orElse("?");
            }
        }
        Map<String, Integer> cells = new HashMap<>();
        Map<String, Integer> patches = new HashMap<>();
        boolean[][] seen = new boolean[GRID][GRID];
        ArrayDeque<int[]> queue = new ArrayDeque<>();
        for (int x = 0; x < GRID; x++) {
            for (int z = 0; z < GRID; z++) {
                if (seen[x][z]) {
                    continue;
                }
                String name = map[x][z];
                patches.merge(name, 1, Integer::sum);
                seen[x][z] = true;
                queue.add(new int[]{x, z});
                while (!queue.isEmpty()) {
                    int[] cell = queue.poll();
                    cells.merge(name, 1, Integer::sum);
                    for (int[] d : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
                        int nx = cell[0] + d[0];
                        int nz = cell[1] + d[1];
                        if (nx >= 0 && nz >= 0 && nx < GRID && nz < GRID && !seen[nx][nz] && map[nx][nz].equals(name)) {
                            seen[nx][nz] = true;
                            queue.add(new int[]{nx, nz});
                        }
                    }
                }
            }
        }
        Map<String, double[]> result = new LinkedHashMap<>();
        cells.entrySet().stream().sorted((a, b) -> b.getValue() - a.getValue()).forEach(entry -> {
            double share = entry.getValue() * 100.0D / (GRID * GRID);
            double side = Math.sqrt(entry.getValue() / (double) patches.get(entry.getKey())) * STEP;
            result.put(entry.getKey(), new double[]{share, side});
        });
        return result;
    }

    /** Distance moyenne (en blocs) a la structure la plus proche depuis quelques points, ou -1 si introuvable. */
    static double meanDistance(ServerLevel nether, String structure, int origins, int searchChunks) {
        Registry<Structure> registry = nether.registryAccess().registryOrThrow(Registries.STRUCTURE);
        Optional<Holder.Reference<Structure>> holder = registry.getHolder(ResourceKey.create(Registries.STRUCTURE, structure.contains(":") ? new net.minecraft.resources.ResourceLocation(structure) : NetheryX.id(structure)));
        if (holder.isEmpty()) {
            return -2.0D;
        }
        double total = 0.0D;
        int found = 0;
        for (int i = 0; i < origins; i++) {
            BlockPos origin = new BlockPos(i * 3000 - 4500, 64, (i % 2) * 2500 - 1250);
            Pair<BlockPos, Holder<Structure>> nearest = nether.getChunkSource().getGenerator().findNearestMapStructure(nether,
                    HolderSet.direct(holder.get()), origin, searchChunks, false);
            if (nearest != null) {
                total += Math.sqrt(nearest.getFirst().distSqr(new BlockPos(origin.getX(), nearest.getFirst().getY(), origin.getZ())));
                found++;
            }
        }
        return found == 0 ? -1.0D : total / found;
    }

    /** Premier point du releve ou chaque biome occupe tout le voisinage : le centre d'une tache, pas sa bordure. */
    static Map<String, BlockPos> biomeCenters(ServerLevel nether) {
        BiomeSource source = nether.getChunkSource().getGenerator().getBiomeSource();
        Climate.Sampler sampler = nether.getChunkSource().randomState().sampler();
        Map<String, BlockPos> centers = new java.util.TreeMap<>();
        for (int ring = 0; ring < 400; ring++) {
            for (int side = 0; side < 40; side++) {
                double angle = side * Math.PI * 2.0D / 40.0D;
                int x = (int) (Math.cos(angle) * ring * 48.0D);
                int z = (int) (Math.sin(angle) * ring * 48.0D);
                String name = name(source, sampler, x, z);
                if (!centers.containsKey(name) && name(source, sampler, x + 32, z) .equals(name) && name(source, sampler, x - 32, z).equals(name)
                        && name(source, sampler, x, z + 32).equals(name) && name(source, sampler, x, z - 32).equals(name)) {
                    centers.put(name, new BlockPos(x, 64, z));
                }
            }
        }
        return centers;
    }

    private static String name(BiomeSource source, Climate.Sampler sampler, int x, int z) {
        return source.getNoiseBiome(x >> 2, BIOME_Y >> 2, z >> 2, sampler).unwrapKey().map(key -> key.location().toString()).orElse("?");
    }

    /** Copie un volume du monde dans un fichier : tools/render_preview.py en fait une image. */
    static void dump(ServerLevel nether, BlockPos center, int size, Path out) throws java.io.IOException {
        int height = 112;
        Map<String, Integer> palette = new LinkedHashMap<>();
        byte[] data = new byte[size * size * height * 2];
        int i = 0;
        int floorTotal = 0;
        int floorCount = 0;
        int minX = center.getX() - size / 2;
        int minZ = center.getZ() - size / 2;
        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                int floor = -1;
                for (int y = 0; y < height; y++) {
                    BlockState state = nether.getBlockState(new BlockPos(minX + x, y + 8, minZ + z));
                    String key = net.minecraftforge.registries.ForgeRegistries.BLOCKS.getKey(state.getBlock()).toString();
                    int index = palette.computeIfAbsent(key, k -> palette.size());
                    data[i++] = (byte) (index >> 8);
                    data[i++] = (byte) index;
                    if (floor < 0 && y > 24 && state.isAir() && y < 90) {
                        floor = y;
                    }
                }
                if (floor > 0) {
                    floorTotal += floor;
                    floorCount++;
                }
            }
        }
        Files.createDirectories(out.getParent());
        Files.write(out, data);
        String names = palette.keySet().stream().map(n -> "\"" + n + "\"").collect(java.util.stream.Collectors.joining(","));
        Files.writeString(Path.of(out + ".json"), "{\"size\":[" + size + "," + height + "," + size + "],\"floor\":" + (floorCount == 0 ? 40 : floorTotal / floorCount)
                + ",\"palette\":[" + names + "]}");
    }

    static void report(ServerLevel overworld, Consumer<String> out, String... structures) {
        ServerLevel nether = overworld.getServer().getLevel(Level.NETHER);
        if (nether == null) {
            out.accept("SONDE : pas de Nether");
            return;
        }
        biomes(nether).forEach((name, value) -> out.accept(String.format("SONDE biome %-38s %5.1f %%  tache ~%4.0f blocs", name, value[0], value[1])));
        for (String structure : structures) {
            out.accept(String.format("SONDE structure %-24s distance moyenne %6.0f blocs", structure, meanDistance(nether, structure, 4, 64)));
        }
    }
}

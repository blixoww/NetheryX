package com.netheryx.world.structure;

import com.netheryx.NetheryX;
import com.netheryx.entity.EntityAshWyvern;
import com.netheryx.entity.EntityNetheryXBoss;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.world.World;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Gabarit de structure lu depuis assets/netheryx/structures/&lt;nom&gt;.txt (converti depuis les structures de la version
 * 1.20.1) : palette de blocs, blocs encodes par plages, coffres (table de butin), generateurs et creatures.
 */
public class StructureTemplate {
    public final String name;
    public int sizeX;
    public int sizeY;
    public int sizeZ;
    private final List<Block> blocks = new ArrayList<Block>();
    private final List<Integer> metas = new ArrayList<Integer>();
    private int[] data;
    private final List<String[]> chests = new ArrayList<String[]>();
    private final List<String[]> spawners = new ArrayList<String[]>();
    private final List<String[]> entities = new ArrayList<String[]>();

    private StructureTemplate(String name) {
        this.name = name;
    }

    public static StructureTemplate load(String name) {
        StructureTemplate t = new StructureTemplate(name);
        InputStream in = StructureTemplate.class.getResourceAsStream("/assets/netheryx/structures/" + name + ".txt");
        if (in == null) {
            NetheryX.LOG.error("Structure introuvable : {}", name);
            return null;
        }
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            String line = reader.readLine();
            String[] size = line.split(" ");
            t.sizeX = Integer.parseInt(size[1]);
            t.sizeY = Integer.parseInt(size[2]);
            t.sizeZ = Integer.parseInt(size[3]);
            int paletteSize = Integer.parseInt(reader.readLine().split(" ")[1]);
            for (int i = 0; i < paletteSize; i++) {
                String[] entry = reader.readLine().split(" ");
                Block block = Block.getBlockFromName(entry[0]);
                if (block == null) {
                    NetheryX.LOG.warn("Bloc inconnu dans {} : {}", name, entry[0]);
                }
                t.blocks.add(block);
                t.metas.add(Integer.parseInt(entry[1]));
            }
            t.data = new int[t.sizeX * t.sizeY * t.sizeZ];
            int pos = 0;
            for (String run : reader.readLine().substring(5).split(",")) {
                int star = run.indexOf('*');
                int value = Integer.parseInt(star < 0 ? run : run.substring(0, star));
                int count = star < 0 ? 1 : Integer.parseInt(run.substring(star + 1));
                for (int i = 0; i < count && pos < t.data.length; i++) {
                    t.data[pos++] = value;
                }
            }
            while ((line = reader.readLine()) != null) {
                String[] parts = line.trim().split(" ");
                if (parts[0].equals("chest")) {
                    t.chests.add(parts);
                } else if (parts[0].equals("spawner")) {
                    t.spawners.add(parts);
                } else if (parts[0].equals("entity")) {
                    t.entities.add(parts);
                }
            }
            reader.close();
        } catch (Exception e) {
            NetheryX.LOG.error("Structure illisible : " + name, e);
            return null;
        }
        return t;
    }

    /** Pose la structure, coin nord-ouest en bas en (x, y, z). Les cases vides du gabarit gardent le terrain. */
    public void place(World world, Random random, int x, int y, int z) {
        int i = 0;
        for (int dy = 0; dy < this.sizeY; dy++) {
            for (int dz = 0; dz < this.sizeZ; dz++) {
                for (int dx = 0; dx < this.sizeX; dx++) {
                    int index = this.data[i++];
                    if (index < 0 || this.blocks.get(index) == null) {
                        continue;
                    }
                    int by = y + dy;
                    if (by > 0 && by < 255) {
                        world.setBlock(x + dx, by, z + dz, this.blocks.get(index), this.metas.get(index), 2);
                    }
                }
            }
        }
        for (String[] chest : this.chests) {
            int cx = x + Integer.parseInt(chest[1]);
            int cy = y + Integer.parseInt(chest[2]);
            int cz = z + Integer.parseInt(chest[3]);
            TileEntity te = world.getTileEntity(cx, cy, cz);
            if (te instanceof IInventory) {
                LootTables.fill((IInventory) te, chest[4], random);
            }
        }
        for (String[] spawner : this.spawners) {
            TileEntity te = world.getTileEntity(x + Integer.parseInt(spawner[1]), y + Integer.parseInt(spawner[2]),
                    z + Integer.parseInt(spawner[3]));
            if (te instanceof TileEntityMobSpawner) {
                ((TileEntityMobSpawner) te).func_145881_a().setEntityName(spawner[4]);
            }
        }
        for (String[] spec : this.entities) {
            Entity entity = EntityList.createEntityByName(spec[4], world);
            if (entity == null) {
                continue;
            }
            double ex = x + Double.parseDouble(spec[1]);
            double ey = y + Double.parseDouble(spec[2]);
            double ez = z + Double.parseDouble(spec[3]);
            entity.setLocationAndAngles(ex, ey, ez, random.nextFloat() * 360.0F, 0.0F);
            if (entity instanceof EntityLiving) {
                ((EntityLiving) entity).func_110163_bv(); // ne disparait jamais
            }
            if (entity instanceof EntityNetheryXBoss) {
                ((EntityNetheryXBoss) entity).setHome((int) ex, (int) ey, (int) ez);
            }
            if (entity instanceof EntityAshWyvern && spec.length > 5 && spec[5].equals("guardian")) {
                ((EntityAshWyvern) entity).setNestGuardian(true);
            }
            world.spawnEntityInWorld(entity);
        }
    }
}

package com.netheryx.world.structure;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;

import java.util.ArrayList;
import java.util.List;

/** Structures deja posees dans le Nether (sauvegardees avec le monde) : la boussole des ames s'en sert. */
public class StructureData extends WorldSavedData {
    private static final String NAME = "netheryx_structures";

    public static final class Entry {
        public final String type;
        public final int x;
        public final int y;
        public final int z;

        Entry(String type, int x, int y, int z) {
            this.type = type;
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    private final List<Entry> entries = new ArrayList<Entry>();

    public StructureData(String name) {
        super(name);
    }

    public static StructureData get(World world) {
        StructureData data = (StructureData) world.perWorldStorage.loadData(StructureData.class, NAME);
        if (data == null) {
            data = new StructureData(NAME);
            world.perWorldStorage.setData(NAME, data);
        }
        return data;
    }

    public void add(String type, int x, int y, int z) {
        this.entries.add(new Entry(type, x, y, z));
        markDirty();
    }

    /** Structure la plus proche du point (x, z), de ce type (null : tous types), ou null. */
    public Entry nearest(int x, int z, String type) {
        Entry best = null;
        long bestDistance = Long.MAX_VALUE;
        for (Entry entry : this.entries) {
            if (type != null && !type.equals(entry.type)) {
                continue;
            }
            long d = (long) (entry.x - x) * (entry.x - x) + (long) (entry.z - z) * (entry.z - z);
            if (d < bestDistance) {
                bestDistance = d;
                best = entry;
            }
        }
        return best;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        this.entries.clear();
        NBTTagList list = tag.getTagList("Structures", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound e = list.getCompoundTagAt(i);
            this.entries.add(new Entry(e.getString("Type"), e.getInteger("X"), e.getInteger("Y"), e.getInteger("Z")));
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        NBTTagList list = new NBTTagList();
        for (Entry entry : this.entries) {
            NBTTagCompound e = new NBTTagCompound();
            e.setString("Type", entry.type);
            e.setInteger("X", entry.x);
            e.setInteger("Y", entry.y);
            e.setInteger("Z", entry.z);
            list.appendTag(e);
        }
        tag.setTag("Structures", list);
    }
}

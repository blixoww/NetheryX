package com.netheryx.world.biome;

import com.netheryx.registry.ModBiomes;
import net.minecraft.world.WorldProviderHell;

/** Nether de NetheryX : le relief vanilla, avec les biomes du mod. */
public class NetherWorldProvider extends WorldProviderHell {
    @Override
    public void registerWorldChunkManager() {
        super.registerWorldChunkManager();
        this.worldChunkMgr = new NetherBiomeManager(this.worldObj.getSeed(), ModBiomes.all());
    }
}

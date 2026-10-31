package com.netheryx.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;

/**
 * Maconnerie scellee et sceau du gardien : indestructibles tant que le boss du donjon est en vie. A sa mort, le boss
 * appelle {@link #unseal} : la maconnerie redevient de la brique ordinaire et les sceaux disparaissent.
 */
public class BlockWarded extends BlockBasic {
    private final boolean seal;

    public BlockWarded(String name, boolean seal) {
        super(name, Material.rock, -1.0F, 6000000.0F, soundTypeStone);
        this.seal = seal;
        setBlockUnbreakable();
        if (seal) {
            setLightLevel(0.5F);
        }
    }

    /** Leve les sceaux dans un rayon autour du boss vaincu. */
    public static void unseal(World world, int cx, int cy, int cz, int radius, Block warded, Block seal, Block plain) {
        for (int x = cx - radius; x <= cx + radius; x++) {
            for (int y = Math.max(1, cy - radius); y <= Math.min(254, cy + radius); y++) {
                for (int z = cz - radius; z <= cz + radius; z++) {
                    Block block = world.getBlock(x, y, z);
                    if (block == seal) {
                        world.setBlockToAir(x, y, z);
                    } else if (block == warded) {
                        world.setBlock(x, y, z, plain, 0, 2);
                    }
                }
            }
        }
    }

    public boolean isSeal() {
        return this.seal;
    }
}

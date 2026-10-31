package com.netheryx.block;

import com.netheryx.NetheryX;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

/**
 * Tronc oriente comme les buches vanilla : la face « coupe » suit l'axe de pose (metadonnees 0 = vertical,
 * 4 = est-ouest, 8 = nord-sud).
 */
public class BlockPillar extends BlockBasic {
    private final String name;
    @SideOnly(Side.CLIENT)
    private IIcon side;
    @SideOnly(Side.CLIENT)
    private IIcon top;

    public BlockPillar(String name, Material material, float hardness, SoundType sound) {
        super(name, material, hardness, hardness * 2.0F, sound);
        this.name = name;
    }

    @Override
    public int getRenderType() {
        return 31; // rendu des buches : la texture suit l'axe
    }

    @Override
    public int onBlockPlaced(World world, int x, int y, int z, int face, float hitX, float hitY, float hitZ, int meta) {
        int axis = 0;
        if (face == 2 || face == 3) {
            axis = 8;
        } else if (face == 4 || face == 5) {
            axis = 4;
        }
        return (meta & 3) | axis;
    }

    @Override
    public int damageDropped(int meta) {
        return 0;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        this.side = register.registerIcon(NetheryX.id(this.name));
        this.top = register.registerIcon(NetheryX.id(this.name + "_top"));
        this.blockIcon = this.side;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int face, int meta) {
        int axis = meta & 12;
        boolean cut = (axis == 0 && (face == 0 || face == 1)) || (axis == 4 && (face == 4 || face == 5))
                || (axis == 8 && (face == 2 || face == 3));
        return cut ? this.top : this.side;
    }
}

package com.netheryx.block;

import com.netheryx.NetheryX;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.IIcon;

import java.util.Random;

/**
 * Sol vegetal du Nether pose sur la netherrack (nylium de braise, bloc d'herbe du Nether) : texture propre dessus et
 * sur les cotes, netherrack dessous. Casse sans Toucher de soie, il redonne de la netherrack.
 */
public class BlockNetherSoil extends BlockBasic {
    private final String name;
    @SideOnly(Side.CLIENT)
    private IIcon top;
    @SideOnly(Side.CLIENT)
    private IIcon side;

    public BlockNetherSoil(String name) {
        super(name, Material.rock, 0.4F, 0.4F, soundTypeGrass);
        tool("pickaxe", 0);
        this.name = name;
    }

    @Override
    public Item getItemDropped(int meta, Random random, int fortune) {
        return Item.getItemFromBlock(Blocks.netherrack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        this.top = register.registerIcon(NetheryX.id(this.name));
        this.side = register.registerIcon(NetheryX.id(this.name + "_side"));
        this.blockIcon = this.top;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int face, int meta) {
        if (face == 1) {
            return this.top;
        }
        return face == 0 ? Blocks.netherrack.getBlockTextureFromSide(0) : this.side;
    }
}

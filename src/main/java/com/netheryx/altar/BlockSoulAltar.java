package com.netheryx.altar;

import com.netheryx.NetheryX;
import com.netheryx.NetheryXTab;
import com.netheryx.registry.ModBlocks;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import java.util.Random;

/**
 * Autel des ames : clic droit pour l'ouvrir. On y pose l'objet a enchanter et une offrande ; il faut au moins deux
 * flammes autour (feu, lampe de braise) dans un rayon de 3 blocs, puis appuyer sur « Infuser ».
 */
public class BlockSoulAltar extends BlockContainer {
    public static final int GUI_ID = 0;
    @SideOnly(Side.CLIENT)
    private IIcon top;
    @SideOnly(Side.CLIENT)
    private IIcon bottom;

    public BlockSoulAltar(String name) {
        super(Material.rock);
        setHardness(5.0F);
        setResistance(1200.0F);
        setLightLevel(0.5F);
        setHarvestLevel("pickaxe", 2);
        setStepSound(soundTypeStone);
        setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.75F, 1.0F);
        setBlockName(NetheryX.MOD_ID + "." + name);
        setBlockTextureName(NetheryX.id(name));
        setCreativeTab(NetheryXTab.TAB);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileSoulAltar();
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hx, float hy, float hz) {
        if (!world.isRemote) {
            player.openGui(NetheryX.instance, GUI_ID, world, x, y, z);
        }
        return true;
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile instanceof TileSoulAltar) {
            TileSoulAltar altar = (TileSoulAltar) tile;
            for (int i = 0; i < altar.getSizeInventory(); i++) {
                ItemStack stack = altar.getStackInSlotOnClosing(i);
                if (stack != null) {
                    world.spawnEntityInWorld(new EntityItem(world, x + 0.5D, y + 0.8D, z + 0.5D, stack));
                }
            }
        }
        super.breakBlock(world, x, y, z, block, meta);
    }

    /** Flammes autour de l'autel : feux et lampes de braise dans un rayon de 3 blocs. */
    public static int countFlames(World world, int x, int y, int z) {
        int flames = 0;
        for (int dx = -3; dx <= 3; dx++) {
            for (int dy = -1; dy <= 2; dy++) {
                for (int dz = -3; dz <= 3; dz++) {
                    Block block = world.getBlock(x + dx, y + dy, z + dz);
                    if (block == Blocks.fire || block == ModBlocks.emberLamp || block == Blocks.lit_pumpkin) {
                        flames++;
                    }
                }
            }
        }
        return flames;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(World world, int x, int y, int z, Random random) {
        TileEntity tile = world.getTileEntity(x, y, z);
        int count = tile instanceof TileSoulAltar && ((TileSoulAltar) tile).getProgress() > 0 ? 6 : 1;
        for (int i = 0; i < count; i++) {
            world.spawnParticle(count > 1 ? "witchMagic" : "portal", x + 0.2D + random.nextDouble() * 0.6D, y + 0.8D,
                    z + 0.2D + random.nextDouble() * 0.6D, 0.0D, 0.1D, 0.0D);
        }
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public int getRenderType() {
        return 0;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        this.blockIcon = register.registerIcon(getTextureName() + "_side");
        this.top = register.registerIcon(getTextureName() + "_top");
        this.bottom = register.registerIcon(getTextureName() + "_bottom");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return side == 1 ? this.top : side == 0 ? this.bottom : this.blockIcon;
    }
}

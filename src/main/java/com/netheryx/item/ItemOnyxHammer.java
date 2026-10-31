package com.netheryx.item;

import com.netheryx.NetheryX;
import com.netheryx.NetheryXTab;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

import java.util.List;

/**
 * Marteau en onyx : mine une zone de 3 x 3 perpendiculaire a la face visee. Les blocs voisins passent par le minage
 * normal du joueur (evenements Forge, protections des autres mods) ; seuls ceux que le marteau peut miner et qui ne
 * sont pas beaucoup plus durs que le bloc central sont casses.
 */
public class ItemOnyxHammer extends ItemPickaxe {
    private static boolean breaking;

    public ItemOnyxHammer(ToolMaterial material, String name) {
        super(material);
        setMaxDamage(7000);
        setUnlocalizedName(NetheryX.MOD_ID + "." + name);
        setTextureName(NetheryX.id(name));
        setCreativeTab(NetheryXTab.TAB);
    }

    @Override
    public float getDigSpeed(ItemStack stack, Block block, int meta) {
        // Un peu plus lent qu'une pioche : il mine neuf blocs a la fois
        return super.getDigSpeed(stack, block, meta) * 0.6F;
    }

    @Override
    public boolean onBlockDestroyed(ItemStack stack, World world, Block block, int x, int y, int z, EntityLivingBase living) {
        boolean result = super.onBlockDestroyed(stack, world, block, x, y, z, living);
        if (breaking || world.isRemote || !(living instanceof EntityPlayerMP) || living.isSneaking()) {
            return result;
        }
        EntityPlayerMP player = (EntityPlayerMP) living;
        MovingObjectPosition hit = getMovingObjectPositionFromPlayer(world, player, false);
        if (hit == null || hit.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) {
            return result;
        }
        float centerHardness = block.getBlockHardness(world, x, y, z);
        int axis = hit.sideHit / 2; // 0 : haut/bas, 1 : nord/sud, 2 : ouest/est
        breaking = true;
        try {
            for (int a = -1; a <= 1; a++) {
                for (int b = -1; b <= 1; b++) {
                    if (a == 0 && b == 0) {
                        continue;
                    }
                    int bx = x + (axis == 2 ? 0 : a);
                    int by = y + (axis == 0 ? 0 : b);
                    int bz = z + (axis == 0 ? b : axis == 1 ? 0 : a);
                    Block other = world.getBlock(bx, by, bz);
                    float hardness = other.getBlockHardness(world, bx, by, bz);
                    if (other.isAir(world, bx, by, bz) || hardness < 0.0F || hardness > centerHardness + 3.0F
                            || !canHarvestBlock(other, stack) && !other.getMaterial().isToolNotRequired()) {
                        continue;
                    }
                    if (player.getCurrentEquippedItem() != stack) {
                        return result;
                    }
                    player.theItemInWorldManager.tryHarvestBlock(bx, by, bz);
                }
            }
        } finally {
            breaking = false;
        }
        return result;
    }

    @Override
    public boolean canHarvestBlock(Block block, ItemStack stack) {
        return super.canHarvestBlock(block, stack) || block.getMaterial() == net.minecraft.block.material.Material.rock
                || block.getMaterial() == net.minecraft.block.material.Material.iron;
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("rawtypes")
    public void addInformation(ItemStack stack, EntityPlayer player, List lines, boolean advanced) {
        ItemBasic.addTooltip(lines, getUnlocalizedName() + ".tooltip");
    }
}

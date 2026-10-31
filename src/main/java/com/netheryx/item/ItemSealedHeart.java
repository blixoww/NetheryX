package com.netheryx.item;

import com.netheryx.entity.EntityNetherHeart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

/**
 * Coeur scelle : fabrique avec les trophees des quatre gardiens. Un clic droit dans le Nether l'ouvre et reveille le
 * Coeur du Nether au-dessus du joueur (il faut de la place : 8 blocs d'air au-dessus).
 */
public class ItemSealedHeart extends ItemBasic {
    public ItemSealedHeart(String name) {
        super(name);
        stack(1);
        rarity(EnumRarity.epic);
        tooltip();
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (world.isRemote) {
            return stack;
        }
        if (world.provider.dimensionId != -1) {
            player.addChatMessage(new ChatComponentTranslation("message.netheryx.sealed_heart.not_nether"));
            return stack;
        }
        int x = MathHelper.floor_double(player.posX);
        int y = MathHelper.floor_double(player.posY);
        int z = MathHelper.floor_double(player.posZ);
        for (int dy = 1; dy <= 8; dy++) {
            if (!world.isAirBlock(x, y + dy, z)) {
                player.addChatMessage(new ChatComponentTranslation("message.netheryx.sealed_heart.no_room"));
                return stack;
            }
        }
        EntityNetherHeart heart = new EntityNetherHeart(world);
        heart.setLocationAndAngles(x + 0.5D, y + 5.0D, z + 0.5D, player.rotationYaw + 180.0F, 0.0F);
        heart.setHome(x, y, z);
        world.spawnEntityInWorld(heart);
        world.playSoundAtEntity(player, "mob.wither.spawn", 1.0F, 0.8F);
        world.newExplosion(null, x + 0.5D, y + 6.0D, z + 0.5D, 0.0F, false, false);
        if (!player.capabilities.isCreativeMode) {
            stack.stackSize--;
        }
        return stack;
    }
}

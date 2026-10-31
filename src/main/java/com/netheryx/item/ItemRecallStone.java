package com.netheryx.item;

import com.netheryx.NetheryX;
import com.netheryx.NetheryXConfig;
import com.netheryx.NetheryXTab;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumAction;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.List;

/**
 * Pierre de rappel : apres 3 secondes de concentration, ramene le joueur dans le Monde normal, a cote du dernier portail
 * du Nether qu'il a emprunte pour en partir (memorise par {@code NetheryXEvents}) ; a defaut, a son lit ou au point
 * d'apparition du monde. Reutilisable, avec un temps de recharge (configurable).
 */
public class ItemRecallStone extends Item {
    public static final String MEMORY = "NetheryXRecall";

    public ItemRecallStone(String name) {
        setMaxStackSize(1);
        setUnlocalizedName(NetheryX.MOD_ID + "." + name);
        setTextureName(NetheryX.id(name));
        setCreativeTab(NetheryXTab.TAB);
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 60;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.bow;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        long ready = stack.hasTagCompound() ? stack.getTagCompound().getLong("Ready") : 0L;
        if (world.getTotalWorldTime() < ready && !player.capabilities.isCreativeMode) {
            if (!world.isRemote) {
                player.addChatMessage(new ChatComponentTranslation("message.netheryx.recall.cooldown",
                        (ready - world.getTotalWorldTime()) / 20L + 1L));
            }
            return stack;
        }
        player.setItemInUse(stack, getMaxItemUseDuration(stack));
        return stack;
    }

    @Override
    public void onUsingTick(ItemStack stack, EntityPlayer player, int count) {
        if (player.worldObj.isRemote) {
            player.worldObj.spawnParticle("portal", player.posX + (itemRand.nextDouble() - 0.5D), player.posY - 1.0D + itemRand.nextDouble() * 2.0D,
                    player.posZ + (itemRand.nextDouble() - 0.5D), 0.0D, 0.3D, 0.0D);
        }
    }

    @Override
    public ItemStack onEaten(ItemStack stack, World world, EntityPlayer player) {
        if (world.isRemote || !(player instanceof EntityPlayerMP)) {
            return stack;
        }
        EntityPlayerMP mp = (EntityPlayerMP) player;
        NBTTagCompound persisted = mp.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
        double x;
        double y;
        double z;
        if (persisted.hasKey(MEMORY)) {
            NBTTagCompound memory = persisted.getCompoundTag(MEMORY);
            x = memory.getDouble("X");
            y = memory.getDouble("Y");
            z = memory.getDouble("Z");
        } else {
            WorldServer overworld = mp.mcServer.worldServerForDimension(0);
            ChunkCoordinates bed = mp.getBedLocation(0);
            ChunkCoordinates spot = bed != null ? EntityPlayer.verifyRespawnCoordinates(overworld, bed, mp.isSpawnForced(0)) : null;
            if (spot == null) {
                spot = overworld.getSpawnPoint();
                spot.posY = overworld.getTopSolidOrLiquidBlock(spot.posX, spot.posZ);
            }
            x = spot.posX + 0.5D;
            y = spot.posY + 0.1D;
            z = spot.posZ + 0.5D;
        }
        if (mp.ridingEntity != null) {
            mp.mountEntity(null);
        }
        if (mp.dimension != 0) {
            WorldServer target = mp.mcServer.worldServerForDimension(0);
            mp.mcServer.getConfigurationManager().transferPlayerToDimension(mp, 0, new Teleporter(target) {
                @Override
                public void placeInPortal(net.minecraft.entity.Entity entity, double px, double py, double pz, float yaw) {
                }

                @Override
                public boolean placeInExistingPortal(net.minecraft.entity.Entity entity, double px, double py, double pz, float yaw) {
                    return true;
                }

                @Override
                public boolean makePortal(net.minecraft.entity.Entity entity) {
                    return true;
                }
            });
        }
        mp.playerNetServerHandler.setPlayerLocation(x, y, z, mp.rotationYaw, mp.rotationPitch);
        mp.fallDistance = 0.0F;
        mp.worldObj.playSoundEffect(x, y, z, "mob.endermen.portal", 1.0F, 0.8F);
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        stack.getTagCompound().setLong("Ready", mp.worldObj.getTotalWorldTime() + NetheryXConfig.recallCooldown * 20L);
        return stack;
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.uncommon;
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("rawtypes")
    public void addInformation(ItemStack stack, EntityPlayer player, List lines, boolean advanced) {
        ItemBasic.addTooltip(lines, getUnlocalizedName() + ".tooltip");
    }
}

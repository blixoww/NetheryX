package com.netheryx.item;

import com.netheryx.NetheryX;
import com.netheryx.NetheryXTab;
import com.netheryx.world.structure.StructureData;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import java.util.List;

/**
 * Boussole des ames (Nether) : clic droit, indique la direction et la distance de la structure NetheryX la plus proche
 * du type choisi ; accroupi + clic droit, change de type (toutes, citadelle, sanctuaire, catacombes...). Seules les
 * structures deja generees sont connues : explorer en fait apparaitre de nouvelles.
 */
public class ItemSoulCompass extends Item {
    public static final String[] TYPES = {"any", "onyx_citadel", "infernal_sanctum", "withered_catacombs", "basalt_forge",
            "wyvern_nest", "nomad_caravan", "traveler_camp", "nether_garden", "hidden_cache"};
    private static final String[] DIRECTIONS = {"south", "southwest", "west", "northwest", "north", "northeast", "east", "southeast"};

    public ItemSoulCompass(String name) {
        setMaxStackSize(1);
        setUnlocalizedName(NetheryX.MOD_ID + "." + name);
        setTextureName(NetheryX.id(name));
        setCreativeTab(NetheryXTab.TAB);
    }

    private static int typeIndex(ItemStack stack) {
        return stack.hasTagCompound() ? Math.floorMod(stack.getTagCompound().getInteger("Target"), TYPES.length) : 0;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (world.isRemote) {
            return stack;
        }
        int index = typeIndex(stack);
        if (player.isSneaking()) {
            index = (index + 1) % TYPES.length;
            if (!stack.hasTagCompound()) {
                stack.setTagCompound(new NBTTagCompound());
            }
            stack.getTagCompound().setInteger("Target", index);
            player.addChatMessage(new ChatComponentTranslation("message.netheryx.compass.target",
                    new ChatComponentTranslation("structure.netheryx." + TYPES[index])));
            return stack;
        }
        if (world.provider.dimensionId != -1) {
            player.addChatMessage(new ChatComponentTranslation("message.netheryx.compass.not_nether"));
            return stack;
        }
        StructureData.Entry entry = StructureData.get(world).nearest(MathHelper.floor_double(player.posX),
                MathHelper.floor_double(player.posZ), index == 0 ? null : TYPES[index]);
        if (entry == null) {
            player.addChatMessage(new ChatComponentTranslation("message.netheryx.compass.none"));
            return stack;
        }
        double dx = entry.x - player.posX;
        double dz = entry.z - player.posZ;
        int distance = (int) Math.sqrt(dx * dx + dz * dz);
        // Angle Minecraft : 0 = sud, 90 = ouest, 180 = nord, 270 = est
        double angle = Math.toDegrees(Math.atan2(-dx, dz));
        int sector = Math.floorMod((int) Math.round(angle / 45.0D), 8);
        ChatComponentTranslation message = new ChatComponentTranslation("message.netheryx.compass.found",
                new ChatComponentTranslation("structure.netheryx." + entry.type), distance,
                new ChatComponentTranslation("direction.netheryx." + DIRECTIONS[sector]), entry.y);
        message.getChatStyle().setColor(EnumChatFormatting.AQUA);
        player.addChatMessage(message);
        world.playSoundAtEntity(player, "random.orb", 0.4F, 0.6F);
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
        ItemBasic.addTooltip(lines, "item.netheryx.soul_compass.current", net.minecraft.util.StatCollector.translateToLocal(
                "structure.netheryx." + TYPES[typeIndex(stack)]));
        ItemBasic.addTooltip(lines, getUnlocalizedName() + ".tooltip");
    }
}

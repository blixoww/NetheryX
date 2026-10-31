package com.netheryx.item;

import com.netheryx.NetheryX;
import com.netheryx.NetheryXTab;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import java.util.List;

/**
 * Armes speciales :
 * <ul>
 *     <li>faux des ames : chaque coup rend un demi-coeur au porteur ;</li>
 *     <li>lame fletrie : chaque coup inflige le Fletrissement ;</li>
 *     <li>lame abyssale : embrase et fletrit la cible, rend un coeur ; clic droit, une vague abyssale fauche tout ce qui
 *     se trouve devant le porteur (recharge 8 s).</li>
 * </ul>
 */
public class ItemModSword extends ItemSword {
    public enum Kind {
        SOUL_SCYTHE, WITHERED_BLADE, ABYSSAL_BLADE
    }

    private static final String COOLDOWN = "NetheryXWave";
    private final Kind kind;
    private final EnumRarity rarity;

    public ItemModSword(ToolMaterial material, String name, Kind kind, EnumRarity rarity) {
        super(material);
        this.kind = kind;
        this.rarity = rarity;
        setUnlocalizedName(NetheryX.MOD_ID + "." + name);
        setTextureName(NetheryX.id(name));
        setCreativeTab(NetheryXTab.TAB);
    }

    @Override
    public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
        switch (this.kind) {
            case SOUL_SCYTHE:
                attacker.heal(1.0F);
                break;
            case WITHERED_BLADE:
                target.addPotionEffect(new PotionEffect(Potion.wither.id, 100, 0));
                break;
            case ABYSSAL_BLADE:
                target.setFire(5);
                target.addPotionEffect(new PotionEffect(Potion.wither.id, 100, 1));
                attacker.heal(2.0F);
                break;
            default:
                break;
        }
        return super.hitEntity(stack, target, attacker);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (this.kind != Kind.ABYSSAL_BLADE || player.isSneaking()) {
            return super.onItemRightClick(stack, world, player);
        }
        NBTTagCompound tag = stack.hasTagCompound() ? stack.getTagCompound() : new NBTTagCompound();
        long now = world.getTotalWorldTime();
        if (now < tag.getLong(COOLDOWN)) {
            return super.onItemRightClick(stack, world, player);
        }
        tag.setLong(COOLDOWN, now + 160L);
        stack.setTagCompound(tag);
        player.swingItem();
        world.playSoundAtEntity(player, "mob.wither.shoot", 1.0F, 0.6F);
        if (!world.isRemote) {
            wave(world, player);
            stack.damageItem(5, player);
        }
        return stack;
    }

    /** Vague abyssale : tout ce qui se trouve dans un cone de 6 blocs devant le joueur est frappe et repousse. */
    @SuppressWarnings("unchecked")
    private static void wave(World world, EntityPlayer player) {
        Vec3 look = player.getLookVec();
        AxisAlignedBB box = player.boundingBox.expand(6.0D, 2.0D, 6.0D);
        List<EntityLivingBase> targets = world.getEntitiesWithinAABB(EntityLivingBase.class, box);
        for (EntityLivingBase target : targets) {
            if (target == player || target.riddenByEntity == player || player.ridingEntity == target) {
                continue;
            }
            double dx = target.posX - player.posX;
            double dz = target.posZ - player.posZ;
            double distance = Math.sqrt(dx * dx + dz * dz);
            if (distance > 6.0D || distance < 0.01D || (dx * look.xCoord + dz * look.zCoord) / distance < 0.5D) {
                continue;
            }
            target.attackEntityFrom(DamageSource.causePlayerDamage(player).setMagicDamage(), 10.0F);
            target.setFire(4);
            target.addVelocity(dx / distance * 1.2D, 0.35D, dz / distance * 1.2D);
        }
        for (int i = 1; i <= 6; i++) {
            double px = player.posX + look.xCoord * i;
            double pz = player.posZ + look.zCoord * i;
            for (int p = 0; p < 4; p++) {
                world.spawnParticle("largesmoke", px, player.posY + 0.5D + world.rand.nextDouble(), pz, 0.0D, 0.05D, 0.0D);
            }
        }
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return this.kind == Kind.ABYSSAL_BLADE ? EnumAction.none : super.getItemUseAction(stack);
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return this.rarity;
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("rawtypes")
    public void addInformation(ItemStack stack, EntityPlayer player, List lines, boolean advanced) {
        ItemBasic.addTooltip(lines, getUnlocalizedName() + ".tooltip");
    }
}

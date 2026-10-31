package com.netheryx.event;

import com.netheryx.entity.EntityMagmafish;
import com.netheryx.item.ItemInfusionTemplate.Infusion;
import com.netheryx.item.ItemModArmor;
import com.netheryx.item.ItemRecallStone;
import com.netheryx.registry.ModBlocks;
import com.netheryx.registry.ModEnchantments;
import com.netheryx.registry.ModItems;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.block.Block;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BlockEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * Regles de jeu de NetheryX (bus Forge et bus FML) :
 * <ul>
 *     <li>bonus des ensembles d'armure complets, voile ignifuge, coeur infernal, semelles de magma ;</li>
 *     <li>enchantements de l'autel des ames ;</li>
 *     <li>infusions d'onyx (fonte, celerite, resilience) et leur application a l'enclume ;</li>
 *     <li>memoire du portail pour la pierre de rappel ;</li>
 *     <li>apparition des poissons de magme dans les lacs de lave.</li>
 * </ul>
 */
public class NetheryXEvents {
    private static final UUID ANCHORING_ID = UUID.fromString("5c1e3a72-8d4b-4f0e-9a61-2b7d0c4e8f13");
    /** Derniere position de chaque joueur dans le Monde normal (pour la pierre de rappel). */
    private final Map<EntityPlayer, double[]> lastOverworld = new WeakHashMap<EntityPlayer, double[]>();

    /** Ensemble complet porte par le joueur, ou null. */
    public static String fullSet(EntityLivingBase living) {
        String set = null;
        for (int slot = 1; slot <= 4; slot++) {
            ItemStack stack = living.getEquipmentInSlot(slot);
            if (stack == null || !(stack.getItem() instanceof ItemModArmor)) {
                return null;
            }
            String piece = ((ItemModArmor) stack.getItem()).getSet();
            if (set == null) {
                set = piece;
            } else if (!set.equals(piece)) {
                return null;
            }
        }
        return set;
    }

    private static void effect(EntityPlayer player, Potion potion, int amplifier) {
        // Duree courte, renouvelee : l'effet s'arrete peu apres avoir retire l'armure
        player.addPotionEffect(new PotionEffect(potion.id, potion == Potion.nightVision ? 260 : 60, amplifier, true));
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        EntityPlayer player = event.player;
        if (event.phase != TickEvent.Phase.END || player.worldObj.isRemote) {
            return;
        }
        World world = player.worldObj;
        boolean nether = world.provider.dimensionId == -1;
        if (world.provider.dimensionId == 0) {
            this.lastOverworld.put(player, new double[]{player.posX, player.posY, player.posZ, player.rotationYaw});
        }
        ItemStack boots = player.getEquipmentInSlot(1);
        if (boots != null && boots.getItem() == ModItems.magmaTreads) {
            crustLava(player);
        }
        if (player.ticksExisted % 10 != 0) {
            return;
        }
        String set = fullSet(player);
        if ("onyx".equals(set)) {
            effect(player, Potion.resistance, 0);
            effect(player, Potion.fireResistance, 0);
        } else if ("ember".equals(set)) {
            effect(player, Potion.fireResistance, 0);
            if (nether && player.onGround) {
                effect(player, Potion.moveSpeed, 0);
            }
        } else if ("abyssal".equals(set)) {
            effect(player, Potion.fireResistance, 0);
            effect(player, Potion.resistance, 0);
            player.removePotionEffect(Potion.wither.id);
            if (nether) {
                effect(player, Potion.damageBoost, 0);
            }
        } else if ("wraith".equals(set)) {
            effect(player, Potion.nightVision, 0);
        }
        // Objets portes dans l'inventaire
        if (player.inventory.hasItem(ModItems.fireVeil) || nether && player.inventory.hasItem(ModItems.infernalHeart)) {
            effect(player, Potion.fireResistance, 0);
        }
        // Foulee de braise : plus rapide sur le sol du Nether
        int stride = boots == null || ModEnchantments.emberStride == null ? 0
                : EnchantmentHelper.getEnchantmentLevel(ModEnchantments.emberStride.effectId, boots);
        if (stride > 0 && nether && player.onGround) {
            effect(player, Potion.moveSpeed, stride - 1);
        }
        // Ancrage : resistance au recul (25 % par niveau, plastron et jambieres cumules)
        int anchoring = 0;
        if (ModEnchantments.anchoring != null) {
            for (int slot = 2; slot <= 3; slot++) {
                ItemStack piece = player.getEquipmentInSlot(slot);
                if (piece != null) {
                    anchoring += EnchantmentHelper.getEnchantmentLevel(ModEnchantments.anchoring.effectId, piece);
                }
            }
        }
        IAttributeInstance knockback = player.getEntityAttribute(SharedMonsterAttributes.knockbackResistance);
        AttributeModifier current = knockback.getModifier(ANCHORING_ID);
        double wanted = Math.min(1.0D, anchoring * 0.25D);
        if (current != null && current.getAmount() != wanted) {
            knockback.removeModifier(current);
            current = null;
        }
        if (current == null && wanted > 0.0D) {
            knockback.applyModifier(new AttributeModifier(ANCHORING_ID, "NetheryX anchoring", wanted, 0));
        }
        // Infusion de resilience : les objets infuses se reparent lentement
        if (player.ticksExisted % 100 == 0) {
            for (ItemStack stack : player.inventory.mainInventory) {
                repair(stack);
            }
            for (ItemStack stack : player.inventory.armorInventory) {
                repair(stack);
            }
        }
        if (nether && player.ticksExisted % 200 == 0) {
            spawnMagmafish(player);
        }
    }

    private static void repair(ItemStack stack) {
        if (stack != null && stack.isItemDamaged() && Infusion.RESILIENCE.isOn(stack)) {
            stack.setItemDamage(stack.getItemDamage() - 1);
        }
    }

    /** Semelles de magma : la lave immobile sous les pas du porteur se croute. */
    private static void crustLava(EntityPlayer player) {
        if (!player.onGround && player.motionY < -0.1D) {
            return;
        }
        int px = MathHelper.floor_double(player.posX);
        int py = MathHelper.floor_double(player.boundingBox.minY) - 1;
        int pz = MathHelper.floor_double(player.posZ);
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (dx * dx + dz * dz > 5) {
                    continue;
                }
                Block block = player.worldObj.getBlock(px + dx, py, pz + dz);
                if ((block == Blocks.lava || block == Blocks.flowing_lava) && player.worldObj.getBlockMetadata(px + dx, py, pz + dz) == 0
                        && player.worldObj.isAirBlock(px + dx, py + 1, pz + dz)) {
                    player.worldObj.setBlock(px + dx, py, pz + dz, ModBlocks.crustedLava);
                }
            }
        }
    }

    /** Poissons de magme : apparaissent de temps en temps dans la lave autour des joueurs du Nether. */
    private static void spawnMagmafish(EntityPlayer player) {
        World world = player.worldObj;
        int x = MathHelper.floor_double(player.posX) + world.rand.nextInt(33) - 16;
        int z = MathHelper.floor_double(player.posZ) + world.rand.nextInt(33) - 16;
        int y = 31 + world.rand.nextInt(4);
        if (world.getBlock(x, y, z) != Blocks.lava || world.getBlock(x, y - 1, z) != Blocks.lava
                || world.getEntitiesWithinAABB(EntityMagmafish.class, player.boundingBox.expand(32.0D, 16.0D, 32.0D)).size() >= 6) {
            return;
        }
        int count = 1 + world.rand.nextInt(3);
        for (int i = 0; i < count; i++) {
            EntityMagmafish fish = new EntityMagmafish(world);
            fish.setLocationAndAngles(x + 0.5D, y - 0.5D, z + 0.5D, world.rand.nextFloat() * 360.0F, 0.0F);
            world.spawnEntityInWorld(fish);
        }
    }

    /** Carapace : renvoie 25 % des degats de melee a l'attaquant. */
    @SubscribeEvent
    public void onHurt(LivingHurtEvent event) {
        if (event.entityLiving instanceof EntityPlayer && "carapace".equals(fullSet(event.entityLiving))
                && event.source.getSourceOfDamage() instanceof EntityLivingBase && event.source.getEntity() == event.source.getSourceOfDamage()
                && !event.source.isProjectile() && !event.source.isMagicDamage()) {
            EntityLivingBase attacker = (EntityLivingBase) event.source.getSourceOfDamage();
            attacker.attackEntityFrom(DamageSource.causeThornsDamage(event.entityLiving), event.ammount * 0.25F);
        }
    }

    /** Robe du spectre : les creatures ne reperent le porteur que de pres (8 blocs). */
    @SubscribeEvent
    public void onTarget(LivingSetAttackTargetEvent event) {
        if (event.target instanceof EntityPlayer && event.entityLiving instanceof EntityLiving && "wraith".equals(fullSet(event.target))
                && event.entityLiving.getDistanceSqToEntity(event.target) > 64.0D && event.entityLiving.getAITarget() != event.target) {
            ((EntityLiving) event.entityLiving).setAttackTarget(null);
        }
    }

    /** Moisson des ames : experience supplementaire et, parfois, poussiere d'ame. */
    @SubscribeEvent
    public void onDeath(LivingDeathEvent event) {
        if (ModEnchantments.soulHarvest == null || !(event.source.getEntity() instanceof EntityPlayer) || event.entityLiving instanceof EntityPlayer
                || event.entityLiving.worldObj.isRemote) {
            return;
        }
        EntityPlayer player = (EntityPlayer) event.source.getEntity();
        ItemStack weapon = player.getCurrentEquippedItem();
        int level = weapon == null ? 0 : EnchantmentHelper.getEnchantmentLevel(ModEnchantments.soulHarvest.effectId, weapon);
        if (level <= 0) {
            return;
        }
        EntityLivingBase dead = event.entityLiving;
        dead.worldObj.spawnEntityInWorld(new EntityXPOrb(dead.worldObj, dead.posX, dead.posY + 0.5D, dead.posZ, 2 * level + dead.worldObj.rand.nextInt(3)));
        if (dead.worldObj.rand.nextInt(10) < level) {
            dead.entityDropItem(new ItemStack(ModItems.soulDust), 0.5F);
        }
    }

    /** Infusion de fonte : les blocs mines sont directement fondus. */
    @SubscribeEvent
    public void onHarvest(BlockEvent.HarvestDropsEvent event) {
        if (event.harvester == null || event.isSilkTouching || !Infusion.SMELTING.isOn(event.harvester.getCurrentEquippedItem())) {
            return;
        }
        List<ItemStack> smelted = new ArrayList<ItemStack>();
        boolean changed = false;
        for (ItemStack drop : event.drops) {
            ItemStack result = FurnaceRecipes.smelting().getSmeltingResult(drop);
            if (result != null) {
                ItemStack copy = result.copy();
                copy.stackSize = Math.max(1, drop.stackSize * result.stackSize);
                smelted.add(copy);
                changed = true;
            } else {
                smelted.add(drop);
            }
        }
        if (changed) {
            event.drops.clear();
            event.drops.addAll(smelted);
            event.world.playSoundEffect(event.x + 0.5D, event.y + 0.5D, event.z + 0.5D, "random.fizz", 0.3F, 1.6F);
        }
    }

    /** Infusion de celerite : les outils minent 40 % plus vite. */
    @SubscribeEvent
    public void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (Infusion.CELERITY.isOn(event.entityPlayer.getCurrentEquippedItem())) {
            event.newSpeed = event.newSpeed * 1.4F;
        }
    }

    /** Enclume : outil, arme ou armure + gabarit d'infusion charge = objet infuse (5 niveaux). */
    @SubscribeEvent
    public void onAnvil(AnvilUpdateEvent event) {
        if (event.left == null || event.right == null || event.right.getItem() != ModItems.onyxInfusionTemplate) {
            return;
        }
        Infusion infusion = Infusion.byMeta(event.right.getItemDamage());
        ItemStack left = event.left;
        if (infusion == null || left.stackSize != 1 || !left.getItem().isDamageable() || infusion.isOn(left)
                || infusion == Infusion.SMELTING && left.getItem() instanceof ItemArmor) {
            return;
        }
        ItemStack result = infusion.applyTo(left.copy());
        if (event.name != null && !event.name.isEmpty() && !event.name.equals(left.getDisplayName())) {
            result.setStackDisplayName(event.name);
        }
        event.output = result;
        event.cost = 5;
        event.materialCost = 1;
    }

    /**
     * Pierre de rappel : quand le joueur passe du Monde normal au Nether, memorise sa derniere position dans le Monde
     * normal (devant le portail emprunte). La 1.7.10 n'a que cet evenement, declenche apres le changement de dimension.
     */
    @SubscribeEvent
    public void onChangedDimension(cpw.mods.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent event) {
        double[] last = this.lastOverworld.get(event.player);
        if (event.fromDim != 0 || event.toDim != -1 || last == null) {
            return;
        }
        EntityPlayer player = event.player;
        NBTTagCompound data = player.getEntityData();
        NBTTagCompound persisted = data.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
        NBTTagCompound memory = new NBTTagCompound();
        // Un pas en arriere du portail, pour ne pas y retomber aussitot
        double yaw = Math.toRadians(last[3]);
        memory.setDouble("X", last[0] + Math.sin(yaw) * 1.5D);
        memory.setDouble("Y", last[1]);
        memory.setDouble("Z", last[2] - Math.cos(yaw) * 1.5D);
        persisted.setTag(ItemRecallStone.MEMORY, memory);
        data.setTag(EntityPlayer.PERSISTED_NBT_TAG, persisted);
    }
}

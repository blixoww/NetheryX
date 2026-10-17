package com.netheryx.event;

import com.netheryx.NetheryX;
import com.netheryx.entity.ObsidianBoat;
import com.netheryx.item.SetBonusArmorItem;
import com.netheryx.registry.ModArmorMaterials;
import com.netheryx.registry.ModEffects;
import com.netheryx.registry.ModEnchantments;
import com.netheryx.util.Infusion;
import com.netheryx.util.ModTags;
import com.netheryx.util.WardLanterns;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

/**
 * Effets de jeu de la 1.2.0 : bonus d'ensembles d'armure, enchantements du Nether, infusions d'onyx,
 * Lanternes de garde et vision de lave.
 */
@Mod.EventBusSubscriber(modid = NetheryX.MOD_ID)
public final class GameplayEvents {
    private static final UUID GROUND_SPEED_ID = UUID.fromString("6c1e3f0a-4b8e-4d2a-9a3b-2f6a1c0e9b71");
    private static final UUID REACH_BLOCK_ID = UUID.fromString("9a7d2c41-0f3e-4f61-b0c2-55a1e8d3c702");
    private static final UUID REACH_ENTITY_ID = UUID.fromString("1f4b8e62-7c3a-4b9d-8e15-c0d2a6f4b913");

    private GameplayEvents() {
    }

    // --- Vitesse au sol (armure de braise, Pas de braise), reparation des objets resilients -------

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        // En fin de tick (apres que la lave a mis le feu) : un passager du bateau d'obsidienne ne brule jamais
        if (player.getVehicle() instanceof ObsidianBoat) {
            player.clearFire();
        }
        if (player.level().isClientSide) {
            return;
        }
        if (player.tickCount % 10 == 0) {
            updateGroundSpeed(player);
        }
        if (player.tickCount % 100 == 0) {
            repairResilient(player);
        }
        if (player.tickCount % 20 == 0 && SetBonusArmorItem.hasFullSet(player, ModArmorMaterials.ABYSSAL)) {
            // Armure abyssale : resistance au feu, Resistance I, et Force I dans le Nether
            keep(player, net.minecraft.world.effect.MobEffects.FIRE_RESISTANCE, 0);
            keep(player, net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE, 0);
            if (player.level().dimension() == net.minecraft.world.level.Level.NETHER) {
                keep(player, net.minecraft.world.effect.MobEffects.DAMAGE_BOOST, 0);
            }
            player.removeEffect(net.minecraft.world.effect.MobEffects.WITHER);
        }
    }

    private static void keep(Player player, net.minecraft.world.effect.MobEffect effect, int amplifier) {
        net.minecraft.world.effect.MobEffectInstance current = player.getEffect(effect);
        if (current == null || current.getDuration() < 60) {
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(effect, 120, amplifier, true, false, true));
        }
    }

    private static void updateGroundSpeed(Player player) {
        AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) {
            return;
        }
        BlockState ground = player.getBlockStateOn();
        double bonus = 0.0D;
        if (ground.is(ModTags.Blocks.EMBER_STRIDE_GROUND)) {
            bonus += 0.12D * EnchantmentHelper.getEnchantmentLevel(ModEnchantments.EMBER_STRIDE.get(), player);
        }
        if (ground.is(ModTags.Blocks.NETHER_VEGETATION_GROUND) && SetBonusArmorItem.hasFullSet(player, ModArmorMaterials.EMBER)) {
            bonus += 0.2D;
        }
        AttributeModifier current = speed.getModifier(GROUND_SPEED_ID);
        if (current != null && current.getAmount() == bonus) {
            return;
        }
        speed.removeModifier(GROUND_SPEED_ID);
        if (bonus > 0.0D) {
            speed.addTransientModifier(new AttributeModifier(GROUND_SPEED_ID, "NetheryX ground speed", bonus,
                    AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
    }

    private static void repairResilient(Player player) {
        for (ItemStack stack : player.getInventory().items) {
            repair(stack);
        }
        for (ItemStack stack : player.getInventory().armor) {
            repair(stack);
        }
        for (ItemStack stack : player.getInventory().offhand) {
            repair(stack);
        }
    }

    private static void repair(ItemStack stack) {
        if (stack.isDamaged() && Infusion.RESILIENCE.isOn(stack)) {
            stack.setDamageValue(stack.getDamageValue() - 1);
        }
    }

    // --- Protection contre le feu des blocs -------------------------------------------------------

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        LivingEntity entity = event.getEntity();
        DamageSource source = event.getSource();
        // Passagers du bateau d'obsidienne : la lave et le feu ne les touchent pas
        if (entity.getVehicle() instanceof ObsidianBoat && (source.is(DamageTypeTags.IS_FIRE) || source.is(DamageTypes.LAVA))) {
            entity.clearFire();
            event.setCanceled(true);
            return;
        }
        boolean hotFloor = source.is(DamageTypes.HOT_FLOOR);
        if (hotFloor && EnchantmentHelper.getEnchantmentLevel(ModEnchantments.EMBER_STRIDE.get(), entity) > 0) {
            event.setCanceled(true);
            return;
        }
        if ((hotFloor || source.is(DamageTypes.IN_FIRE) || source.is(DamageTypes.ON_FIRE))
                && SetBonusArmorItem.hasFullSet(entity, ModArmorMaterials.EMBER)) {
            entity.clearFire();
            event.setCanceled(true);
        }
    }

    /** Armure de carapace complete : renvoie 25 % des degats de melee a l'attaquant. */
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity victim = event.getEntity();
        DamageSource source = event.getSource();
        if (source.getDirectEntity() instanceof LivingEntity attacker && attacker != victim && !source.is(DamageTypeTags.IS_PROJECTILE)
                && !source.is(DamageTypes.THORNS) && SetBonusArmorItem.hasFullSet(victim, ModArmorMaterials.CARAPACE)) {
            attacker.hurt(victim.damageSources().thorns(victim), Math.max(1.0F, event.getAmount() * 0.25F));
        }
    }

    /** Ancrage : chaque niveau (plastron et jambieres cumules) retire 30 % du recul. */
    @SubscribeEvent
    public static void onKnockBack(LivingKnockBackEvent event) {
        LivingEntity entity = event.getEntity();
        int level = entity.getItemBySlot(EquipmentSlot.CHEST).getEnchantmentLevel(ModEnchantments.ANCHORING.get())
                + entity.getItemBySlot(EquipmentSlot.LEGS).getEnchantmentLevel(ModEnchantments.ANCHORING.get());
        if (level > 0) {
            event.setStrength(event.getStrength() * Math.max(0.0F, 1.0F - 0.3F * level));
        }
    }

    /** Recolte d'ames : +50 % d'experience par niveau. */
    @SubscribeEvent
    public static void onExperienceDrop(LivingExperienceDropEvent event) {
        Player player = event.getAttackingPlayer();
        if (player != null) {
            int level = player.getMainHandItem().getEnchantmentLevel(ModEnchantments.SOUL_HARVEST.get());
            if (level > 0) {
                event.setDroppedExperience(Math.round(event.getDroppedExperience() * (1.0F + 0.5F * level)));
            }
        }
    }

    /** Armure de spectre complete : les creatures ne vous reperent que de tres pres. */
    @SubscribeEvent
    public static void onVisibility(LivingEvent.LivingVisibilityEvent event) {
        if (SetBonusArmorItem.hasFullSet(event.getEntity(), ModArmorMaterials.WRAITH)) {
            event.modifyVisibility(0.35D);
        }
    }

    /** Infusion de portee. */
    @SubscribeEvent
    public static void onItemAttributes(ItemAttributeModifierEvent event) {
        if (event.getSlotType() == EquipmentSlot.MAINHAND && Infusion.REACH.isOn(event.getItemStack())) {
            event.addModifier(ForgeMod.BLOCK_REACH.get(), new AttributeModifier(REACH_BLOCK_ID, "Reach infusion", 1.5D,
                    AttributeModifier.Operation.ADDITION));
            event.addModifier(ForgeMod.ENTITY_REACH.get(), new AttributeModifier(REACH_ENTITY_ID, "Reach infusion", 1.0D,
                    AttributeModifier.Operation.ADDITION));
        }
    }

    /** Vision de lave : on nage aussi vite dans la lave que dans l'eau. */
    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.isInLava() && entity.hasEffect(ModEffects.LAVA_VISION.get())) {
            Vec3 motion = entity.getDeltaMovement();
            entity.setDeltaMovement(motion.x * 1.35D, motion.y, motion.z * 1.35D);
        }
    }

    /** Lanternes de garde : pas d'apparition naturelle de monstres a proximite. */
    @SubscribeEvent
    public static void onFinalizeSpawn(MobSpawnEvent.FinalizeSpawn event) {
        if (event.getSpawnType() == MobSpawnType.NATURAL && event.getEntity() instanceof Enemy
                && WardLanterns.isProtected(event.getEntity().level(), event.getX(), event.getY(), event.getZ())) {
            event.setSpawnCancelled(true);
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        WardLanterns.clear();
    }
}

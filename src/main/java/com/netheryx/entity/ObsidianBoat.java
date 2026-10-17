package com.netheryx.entity;

import com.netheryx.registry.ModEntities;
import com.netheryx.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.fluids.FluidType;

/**
 * Bateau d'obsidienne : navigue sur l'eau comme sur la lave, ne se brise jamais (ni en tombant, ni sous les coups)
 * et retourne directement dans l'inventaire du joueur qui le frappe. Ses passagers sont proteges du feu.
 */
public class ObsidianBoat extends Boat {
    /** Hauteur maximale dont la coque est rehaussee sur la lave (rendu et position des passagers). */
    public static final float MAX_LAVA_LIFT = 0.3F;

    private float lavaLift;
    private float lavaLiftO;

    public ObsidianBoat(EntityType<? extends Boat> type, Level level) {
        super(type, level);
    }

    public ObsidianBoat(Level level, double x, double y, double z) {
        this(ModEntities.OBSIDIAN_BOAT.get(), level);
        this.setPos(x, y, z);
        this.xo = x;
        this.yo = y;
        this.zo = z;
    }

    @Override
    public boolean canBoatInFluid(FluidState state) {
        return state.is(FluidTags.LAVA) || super.canBoatInFluid(state);
    }

    @Override
    public boolean canBoatInFluid(FluidType type) {
        return type == ForgeMod.LAVA_TYPE.get() || super.canBoatInFluid(type);
    }

    @Override
    public void tick() {
        super.tick();
        boolean onLava = this.isInLava();
        this.lavaLiftO = this.lavaLift;
        this.lavaLift = Mth.approach(this.lavaLift, onLava ? MAX_LAVA_LIFT : 0.0F, 0.04F);
        if (!this.level().isClientSide && onLava) {
            for (Entity passenger : this.getPassengers()) {
                if (passenger instanceof LivingEntity living) {
                    MobEffectInstance current = living.getEffect(MobEffects.FIRE_RESISTANCE);
                    if (current == null || current.getDuration() < 30) {
                        living.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 60, 0, true, false, true));
                    }
                }
                passenger.clearFire();
            }
        }
    }

    /** Rehaussement interpole, utilise par le rendu pour que la lave n'apparaisse pas dans la coque. */
    public float getLavaLift(float partialTick) {
        return Mth.lerp(partialTick, this.lavaLiftO, this.lavaLift);
    }

    @Override
    public double getPassengersRidingOffset() {
        return super.getPassengersRidingOffset() + this.lavaLift;
    }

    /** Ne se brise jamais en tombant : la distance de chute est remise a zero avant le calcul vanilla. */
    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
        this.resetFallDistance();
        super.checkFallDamage(y, onGround, state, pos);
    }

    /**
     * Insensible a tout, sauf au coup direct d'un joueur : le bateau retourne alors immediatement dans son inventaire
     * (ou tombe au sol si l'inventaire est plein).
     */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source) || !(source.getDirectEntity() instanceof Player player)) {
            return false;
        }
        if (!this.level().isClientSide && !this.isRemoved()) {
            ItemStack stack = new ItemStack(this.getDropItem());
            if (this.hasCustomName()) {
                stack.setHoverName(this.getCustomName());
            }
            boolean creative = player.getAbilities().instabuild;
            if (!creative || !player.getInventory().contains(stack)) {
                if (!player.getInventory().add(stack)) {
                    this.spawnAtLocation(stack);
                }
            }
            this.ejectPassengers();
            this.gameEvent(GameEvent.ENTITY_DIE, player);
            this.discard();
        }
        return true;
    }

    @Override
    public Item getDropItem() {
        return ModItems.OBSIDIAN_BOAT.get();
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(this.getDropItem());
    }
}

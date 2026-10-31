package com.netheryx.entity;

import com.netheryx.block.BlockWarded;
import com.netheryx.registry.ModBlocks;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

/**
 * Boss de donjon : barre de vie, confine a son arene (il y revient s'il s'en eloigne trop), attaque speciale
 * periodique ; a sa mort, les sceaux du coffre-fort du donjon se levent.
 */
public abstract class EntityNetheryXBoss extends EntityNetheryXMonster implements IBossDisplayData {
    private int homeX;
    private int homeY;
    private int homeZ;
    private boolean hasHome;
    private int specialCooldown = 60;
    protected int arenaRadius = 14;
    protected int arenaHeight = 12;

    public EntityNetheryXBoss(World world) {
        super(world);
        this.experienceValue = 200;
    }

    public void setHome(int x, int y, int z) {
        this.homeX = x;
        this.homeY = y;
        this.homeZ = z;
        this.hasHome = true;
    }

    /** Intervalle entre deux attaques speciales (en ticks), plus court quand le boss est blesse. */
    protected int specialInterval() {
        return getHealth() < getMaxHealth() * 0.5F ? 70 : 110;
    }

    /** Attaque speciale du boss contre sa cible. */
    protected abstract void special(EntityLivingBase target);

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (this.worldObj.isRemote) {
            return;
        }
        if (!this.hasHome) {
            setHome(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY), MathHelper.floor_double(this.posZ));
        }
        double dx = this.posX - this.homeX;
        double dz = this.posZ - this.homeZ;
        if (dx * dx + dz * dz > (this.arenaRadius + 6) * (this.arenaRadius + 6) || Math.abs(this.posY - this.homeY) > this.arenaHeight) {
            setPositionAndUpdate(this.homeX + 0.5D, this.homeY, this.homeZ + 0.5D);
            this.worldObj.playSoundAtEntity(this, "mob.endermen.portal", 1.0F, 0.6F);
        }
        EntityLivingBase target = getAttackTarget();
        if (target instanceof EntityPlayer && target.getDistanceSq(this.homeX, this.homeY, this.homeZ) > (this.arenaRadius + 10) * (this.arenaRadius + 10)) {
            setAttackTarget(null);
            target = null;
        }
        if (target == null && this.ticksExisted % 40 == 0 && getHealth() < getMaxHealth()) {
            heal(1.0F);
        }
        if (target != null && --this.specialCooldown <= 0) {
            this.specialCooldown = specialInterval();
            special(target);
        }
    }

    @Override
    public void onDeath(DamageSource source) {
        super.onDeath(source);
        if (!this.worldObj.isRemote) {
            int x = this.hasHome ? this.homeX : MathHelper.floor_double(this.posX);
            int y = this.hasHome ? this.homeY : MathHelper.floor_double(this.posY);
            int z = this.hasHome ? this.homeZ : MathHelper.floor_double(this.posZ);
            BlockWarded.unseal(this.worldObj, x, y, z, 32, ModBlocks.wardedBricks, ModBlocks.guardianSeal, ModBlocks.onyxBricks);
            this.worldObj.playSoundAtEntity(this, "random.levelup", 2.0F, 0.6F);
        }
    }

    @Override
    protected boolean canDespawn() {
        return false;
    }

    @Override
    public boolean canBePushed() {
        return false;
    }

    @Override
    protected void addRandomArmor() {
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound tag) {
        super.writeEntityToNBT(tag);
        if (this.hasHome) {
            tag.setIntArray("NetheryXHome", new int[]{this.homeX, this.homeY, this.homeZ});
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound tag) {
        super.readEntityFromNBT(tag);
        if (tag.hasKey("NetheryXHome")) {
            int[] home = tag.getIntArray("NetheryXHome");
            setHome(home[0], home[1], home[2]);
        }
    }

    /** Repousse et blesse tout ce qui est autour (onde de choc). */
    protected void shockwave(double radius, float damage, double push) {
        for (Object o : this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.boundingBox.expand(radius, 2.0D, radius))) {
            if (o instanceof EntityLivingBase && !(o instanceof EntityNetheryXBoss)) {
                EntityLivingBase victim = (EntityLivingBase) o;
                victim.attackEntityFrom(DamageSource.causeMobDamage(this), damage);
                double dx = victim.posX - this.posX;
                double dz = victim.posZ - this.posZ;
                double length = Math.max(0.1D, Math.sqrt(dx * dx + dz * dz));
                victim.addVelocity(dx / length * push, 0.5D, dz / length * push);
            }
        }
        this.worldObj.playSoundAtEntity(this, "random.explode", 1.2F, 0.7F);
    }
}

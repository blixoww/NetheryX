package com.netheryx.entity;

import com.netheryx.registry.ModItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.EntityAIFollowOwner;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIOwnerHurtByTarget;
import net.minecraft.entity.ai.EntityAIOwnerHurtTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

/**
 * Wyverne de cendre : monture volante. Sauvage, elle s'apprivoise avec de la viande crue ; la gardienne d'un nid ne
 * s'apprivoise pas mais laisse son oeuf a sa mort. Apprivoisee, elle suit son maitre, crache du feu sur ses ennemis et
 * se monte : avancez en regardant vers le haut pour decoller, vers le bas pour piquer, relachez pour planer.
 */
public class EntityAshWyvern extends EntityTameable implements IAnimatedMob {
    private boolean nestGuardian;
    private int breathCooldown = 40;
    private int breathAnim;

    public EntityAshWyvern(World world) {
        super(world);
        setSize(2.4F, 1.9F);
        this.isImmuneToFire = true;
        this.stepHeight = 1.0F;
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, this.aiSit);
        this.tasks.addTask(2, new EntityAIAttackOnCollide(this, 1.2D, true));
        this.tasks.addTask(3, new EntityAIFollowOwner(this, 1.2D, 12.0F, 4.0F));
        this.tasks.addTask(5, new EntityAIWander(this, 0.9D));
        this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 10.0F));
        this.tasks.addTask(7, new EntityAILookIdle(this));
        this.targetTasks.addTask(1, new EntityAIOwnerHurtByTarget(this));
        this.targetTasks.addTask(2, new EntityAIOwnerHurtTarget(this));
        this.targetTasks.addTask(3, new EntityAIHurtByTarget(this, true));
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataWatcher.addObject(20, Byte.valueOf((byte) 0));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getAttributeMap().registerAttribute(SharedMonsterAttributes.attackDamage);
        getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(50.0D);
        getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.3D);
        getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(8.0D);
        getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(32.0D);
    }

    @Override
    protected boolean isAIEnabled() {
        return true;
    }

    /** Se releve (sifflet, sortie du cristal). */
    public void stand() {
        this.aiSit.setSitting(false);
        setPathToEntity(null);
    }

    public void setNestGuardian(boolean guardian) {
        this.nestGuardian = guardian;
        if (guardian) {
            getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(100.0D);
            setHealth(100.0F);
            this.targetTasks.addTask(4, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 0, true));
        }
    }

    @Override
    public void setTamed(boolean tamed) {
        super.setTamed(tamed);
        if (tamed) {
            getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(80.0D);
        }
    }

    @Override
    public boolean interact(EntityPlayer player) {
        ItemStack stack = player.inventory.getCurrentItem();
        if (!isTamed()) {
            if (!this.nestGuardian && stack != null && isMeat(stack.getItem())) {
                if (!player.capabilities.isCreativeMode) {
                    stack.stackSize--;
                }
                if (!this.worldObj.isRemote) {
                    if (this.rand.nextInt(3) == 0) {
                        TameHelper.tame(this, player);
                        this.aiSit.setSitting(false);
                    } else {
                        this.worldObj.setEntityState(this, (byte) 6);
                    }
                }
                return true;
            }
            return super.interact(player);
        }
        if (getOwner() != player) {
            return super.interact(player);
        }
        if (stack != null && isMeat(stack.getItem()) && getHealth() < getMaxHealth()) {
            if (!player.capabilities.isCreativeMode) {
                stack.stackSize--;
            }
            heal(10.0F);
            return true;
        }
        if (player.isSneaking()) {
            if (!this.worldObj.isRemote) {
                this.aiSit.setSitting(!isSitting());
            }
            return true;
        }
        if (this.riddenByEntity == null && (stack == null || !(stack.getItem() instanceof com.netheryx.item.ItemWyvernCrystal))) {
            if (!this.worldObj.isRemote) {
                this.aiSit.setSitting(false);
                player.mountEntity(this);
            }
            return true;
        }
        return super.interact(player);
    }

    private static boolean isMeat(Item item) {
        return item == Items.porkchop || item == Items.beef || item == Items.chicken || item == Items.fish
                || item == ModItems.stalkerMeat || item == ModItems.infernalBeef || item == ModItems.blazePork
                || item == ModItems.infernalChicken;
    }

    // --- Vol monte -------------------------------------------------------------------------------

    public boolean isFlying() {
        return this.dataWatcher.getWatchableObjectByte(20) == 1;
    }

    private void setFlying(boolean flying) {
        this.dataWatcher.updateObject(20, Byte.valueOf((byte) (flying ? 1 : 0)));
    }

    @Override
    public void moveEntityWithHeading(float strafe, float forward) {
        if (this.riddenByEntity instanceof EntityPlayer) {
            EntityPlayer rider = (EntityPlayer) this.riddenByEntity;
            this.prevRotationYaw = this.rotationYaw = rider.rotationYaw;
            this.rotationPitch = rider.rotationPitch * 0.5F;
            setRotation(this.rotationYaw, this.rotationPitch);
            this.rotationYawHead = this.renderYawOffset = this.rotationYaw;
            this.fallDistance = 0.0F;
            float move = rider.moveForward;
            boolean up = move > 0.0F && rider.rotationPitch < -15.0F;
            if (!this.worldObj.isRemote) {
                if (up || (!this.onGround && isFlying())) {
                    setFlying(true);
                    Vec3 look = rider.getLookVec();
                    double speed = 0.7D;
                    this.motionX = look.xCoord * speed * move;
                    this.motionZ = look.zCoord * speed * move;
                    this.motionY = move != 0.0F ? look.yCoord * speed * move : -0.06D; // vol plane si on relache
                    moveEntity(this.motionX, this.motionY, this.motionZ);
                    if (this.onGround) {
                        setFlying(false);
                    }
                } else {
                    setFlying(false);
                    setAIMoveSpeed((float) getEntityAttribute(SharedMonsterAttributes.movementSpeed).getAttributeValue());
                    super.moveEntityWithHeading(rider.moveStrafing * 0.5F, move > 0.0F ? move : move * 0.25F);
                }
            }
            return;
        }
        if (isFlying() && !this.worldObj.isRemote) {
            setFlying(false);
        }
        super.moveEntityWithHeading(strafe, forward);
    }

    @Override
    protected void fall(float distance) {
    }

    @Override
    public double getMountedYOffset() {
        return this.height * 0.95D;
    }

    // --- Combat ----------------------------------------------------------------------------------

    @Override
    public boolean attackEntityAsMob(Entity target) {
        this.breathAnim = 10;
        return target.attackEntityFrom(DamageSource.causeMobDamage(this),
                (float) getEntityAttribute(SharedMonsterAttributes.attackDamage).getAttributeValue());
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (this.breathAnim > 0) {
            this.breathAnim--;
        }
        if (this.worldObj.isRemote) {
            return;
        }
        if (this.breathCooldown > 0) {
            this.breathCooldown--;
        }
        EntityLivingBase target = getAttackTarget();
        if (target != null && target.isEntityAlive() && this.breathCooldown <= 0 && getDistanceToEntity(target) < 12.0F
                && target != getOwner() && target != this.riddenByEntity) {
            breathe(target);
        }
        if (isTamed() && this.ticksExisted % 60 == 0 && getHealth() < getMaxHealth() && target == null) {
            heal(1.0F);
        }
    }

    /** Souffle de feu : une gerbe de boules de feu vers la cible (elles n'abiment pas le terrain plus que le feu). */
    public void breathe(EntityLivingBase target) {
        this.breathCooldown = 60 + this.rand.nextInt(40);
        this.breathAnim = 20;
        Vec3 look = getLookVec();
        for (int i = 0; i < 4; i++) {
            double dx = target.posX - this.posX + this.rand.nextGaussian() * 0.4D;
            double dy = target.boundingBox.minY + target.height / 2.0F - (this.posY + 1.2D);
            double dz = target.posZ - this.posZ + this.rand.nextGaussian() * 0.4D;
            EntitySmallFireball fireball = new EntitySmallFireball(this.worldObj, this, dx, dy, dz);
            fireball.setPosition(this.posX + look.xCoord * 2.0D, this.posY + 1.2D, this.posZ + look.zCoord * 2.0D);
            this.worldObj.spawnEntityInWorld(fireball);
        }
        playSound("mob.enderdragon.growl", 1.0F, 1.4F);
    }

    @Override
    public void onDeath(DamageSource source) {
        super.onDeath(source);
        if (!this.worldObj.isRemote && (this.nestGuardian || isTamed())) {
            // La gardienne laisse son oeuf ; une wyverne apprivoisee aussi : on ne la perd jamais
            ItemStack egg = new ItemStack(ModItems.ashWyvernEgg);
            if (hasCustomNameTag()) {
                egg.setStackDisplayName(getCustomNameTag());
            }
            EntityLivingBase owner = getOwner();
            if (owner instanceof EntityPlayer && ((EntityPlayer) owner).inventory.addItemStackToInventory(egg)) {
                return;
            }
            entityDropItem(egg, 1.0F);
        }
    }

    @Override
    protected void dropFewItems(boolean hit, int looting) {
        dropItem(Items.leather, 1 + this.rand.nextInt(2 + looting));
    }

    @Override
    protected boolean canDespawn() {
        return !isTamed() && !this.nestGuardian;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound tag) {
        super.writeEntityToNBT(tag);
        tag.setBoolean("NestGuardian", this.nestGuardian);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound tag) {
        super.readEntityFromNBT(tag);
        if (tag.getBoolean("NestGuardian")) {
            setNestGuardian(true);
        }
    }

    @Override
    public EntityAgeable createChild(EntityAgeable partner) {
        return null;
    }

    @Override
    public boolean getCanSpawnHere() {
        return this.worldObj.checkNoEntityCollision(this.boundingBox) && this.worldObj.getCollidingBoundingBoxes(this, this.boundingBox).isEmpty()
                && !this.worldObj.isAnyLiquid(this.boundingBox);
    }

    @Override
    public boolean isFlyingAnimation() {
        return isFlying() || !this.onGround;
    }

    @Override
    public float getActionProgress() {
        return Math.min(1.0F, this.breathAnim / 10.0F);
    }

    @Override
    protected String getLivingSound() {
        return "mob.enderdragon.growl";
    }

    @Override
    protected float getSoundVolume() {
        return 0.5F;
    }

    @Override
    protected String getHurtSound() {
        return "mob.enderdragon.hit";
    }

    @Override
    protected String getDeathSound() {
        return "mob.enderdragon.end";
    }
}

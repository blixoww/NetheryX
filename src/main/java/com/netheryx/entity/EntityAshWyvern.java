package com.netheryx.entity;

import com.netheryx.registry.ModItems;
import cpw.mods.fml.relauncher.ReflectionHelper;
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
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

/**
 * Wyverne de cendre : monture volante. Sauvage, elle s'apprivoise avec de la viande crue ; la gardienne d'un nid ne
 * s'apprivoise pas mais laisse son oeuf a sa mort. Apprivoisee, elle suit son maitre, crache du feu sur ses ennemis et
 * se monte : saut (espace) ou avancer en regardant vers le haut pour decoller, avancer pour voler dans la direction du
 * regard (vers le bas pour piquer), relacher pour planer. Sans cavalier, elle vole aussi : pour rejoindre son maitre,
 * fondre sur une cible, ou (sauvage) pour de longues promenades aeriennes.
 */
public class EntityAshWyvern extends EntityTameable implements IAnimatedMob {
    private boolean nestGuardian;
    /** Promenade aerienne d'une wyverne sauvage : ticks restants et point vise. */
    private int wanderFlight;
    private Vec3 wanderGoal;
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

    /** Touche de saut du cavalier (champ protege de la 1.7.10, lu par reflexion ; mis a jour cote serveur par le paquet d'entrees). */
    private static boolean isJumping(EntityLivingBase rider) {
        try {
            Boolean jumping = ReflectionHelper.getPrivateValue(EntityLivingBase.class, rider, "field_70703_bu", "isJumping");
            return jumping != null && jumping;
        } catch (RuntimeException e) {
            return false;
        }
    }

    @Override
    public void moveEntityWithHeading(float strafe, float forward) {
        if (this.riddenByEntity instanceof EntityPlayer) {
            if (!this.worldObj.isRemote) {
                rideTick((EntityPlayer) this.riddenByEntity);
            }
            return;
        }
        if (!this.worldObj.isRemote && flightTick()) {
            return;
        }
        super.moveEntityWithHeading(strafe, forward);
    }

    /**
     * Vol monte : la wyverne suit le regard du cavalier. Saut (espace) ou avancer en regardant vers le haut : decoller /
     * monter ; avancer : voler dans la direction du regard (vers le bas pour piquer) ; rien : planer en descendant.
     */
    private void rideTick(EntityPlayer rider) {
        this.prevRotationYaw = this.rotationYaw = rider.rotationYaw;
        this.rotationPitch = rider.rotationPitch * 0.5F;
        setRotation(this.rotationYaw, this.rotationPitch);
        this.rotationYawHead = this.renderYawOffset = this.rotationYaw;
        this.fallDistance = 0.0F;
        getNavigator().clearPathEntity();
        float move = rider.moveForward;
        boolean jump = isJumping(rider);
        boolean takeOff = jump || move > 0.0F && rider.rotationPitch < -15.0F;
        if (takeOff || !this.onGround && isFlying()) {
            setFlying(true);
            Vec3 look = rider.getLookVec();
            double speed = 0.75D;
            double tx = move > 0.0F ? look.xCoord * speed : move < 0.0F ? -look.xCoord * 0.2D : 0.0D;
            double tz = move > 0.0F ? look.zCoord * speed : move < 0.0F ? -look.zCoord * 0.2D : 0.0D;
            double ty = move > 0.0F ? look.yCoord * speed : -0.08D; // plane si on relache
            if (jump) {
                ty = Math.max(ty, 0.0D) + 0.35D;
            }
            // Un peu d'inertie : les virages restent souples
            this.motionX += (tx - this.motionX) * 0.3D;
            this.motionY += (ty - this.motionY) * 0.4D;
            this.motionZ += (tz - this.motionZ) * 0.3D;
            moveEntity(this.motionX, this.motionY, this.motionZ);
            if (this.isCollidedHorizontally) {
                this.motionY = 0.25D;
            }
            if (this.onGround && !takeOff) {
                setFlying(false);
            }
            return;
        }
        setFlying(false);
        setAIMoveSpeed((float) getEntityAttribute(SharedMonsterAttributes.movementSpeed).getAttributeValue());
        super.moveEntityWithHeading(rider.moveStrafing * 0.5F, move > 0.0F ? move : move * 0.25F);
    }

    /** Point vise en vol libre, ou null pour se poser. */
    private Vec3 flightGoal() {
        EntityLivingBase target = getAttackTarget();
        if (target != null && target.isEntityAlive()) {
            double dx = target.posX - this.posX;
            double dz = target.posZ - this.posZ;
            double horizontal = Math.sqrt(dx * dx + dz * dz);
            // Proche et a sa hauteur : elle se pose pour mordre ; sinon elle fond sur sa cible par les airs
            if (horizontal < 3.5D && Math.abs(target.posY - this.posY) < 2.5D) {
                return null;
            }
            if (horizontal > 5.0D || target.posY - this.posY > 2.5D || isFlying()) {
                return Vec3.createVectorHelper(target.posX, target.posY + 2.5D, target.posZ);
            }
            return null;
        }
        if (isTamed()) {
            EntityLivingBase owner = getOwner();
            if (isSitting() || owner == null || owner.worldObj != this.worldObj) {
                return null;
            }
            double distance = getDistanceToEntity(owner);
            boolean ownerAirborne = owner.posY - this.posY > 4.0D || owner instanceof EntityPlayer && ((EntityPlayer) owner).capabilities.isFlying;
            if (distance > 14.0D || ownerAirborne || isFlying() && distance > 5.0D) {
                return Vec3.createVectorHelper(owner.posX, owner.posY + 3.0D, owner.posZ);
            }
            return null;
        }
        if (this.nestGuardian) {
            return null;
        }
        // Sauvage : de temps en temps, une longue promenade dans les airs
        if (this.wanderFlight <= 0) {
            if (this.onGround && this.rand.nextInt(500) == 0) {
                this.wanderFlight = 200 + this.rand.nextInt(300);
                this.wanderGoal = null;
            } else {
                return null;
            }
        }
        this.wanderFlight--;
        if (this.wanderFlight <= 0) {
            return null;
        }
        if (this.wanderGoal == null || this.wanderGoal.squareDistanceTo(this.posX, this.posY, this.posZ) < 9.0D) {
            double gx = this.posX + (this.rand.nextDouble() - 0.5D) * 48.0D;
            double gz = this.posZ + (this.rand.nextDouble() - 0.5D) * 48.0D;
            int ground = MathHelper.floor_double(this.posY);
            while (ground > 1 && this.worldObj.isAirBlock(MathHelper.floor_double(gx), ground, MathHelper.floor_double(gz))) {
                ground--;
            }
            this.wanderGoal = Vec3.createVectorHelper(gx, Math.min(ground + 8 + this.rand.nextInt(8), 118), gz);
        }
        return this.wanderGoal;
    }

    /** Vol libre (sans cavalier) : decolle, file vers son but, puis se pose en planant. Renvoie false au sol. */
    private boolean flightTick() {
        Vec3 goal = flightGoal();
        if (goal == null && (this.onGround || !isFlying())) {
            setFlying(false);
            return false;
        }
        setFlying(true);
        getNavigator().clearPathEntity();
        this.fallDistance = 0.0F;
        double tx;
        double ty;
        double tz;
        if (goal != null) {
            double dx = goal.xCoord - this.posX;
            double dy = goal.yCoord - this.posY;
            double dz = goal.zCoord - this.posZ;
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            double speed = distance < 2.0D ? 0.1D : 0.5D;
            tx = dx / Math.max(distance, 0.01D) * speed;
            ty = dy / Math.max(distance, 0.01D) * speed;
            tz = dz / Math.max(distance, 0.01D) * speed;
        } else {
            // Atterrissage : descente planee
            tx = this.motionX * 0.9D;
            ty = -0.18D;
            tz = this.motionZ * 0.9D;
        }
        this.motionX += (tx - this.motionX) * 0.15D;
        this.motionY += (ty - this.motionY) * 0.2D;
        this.motionZ += (tz - this.motionZ) * 0.15D;
        moveEntity(this.motionX, this.motionY, this.motionZ);
        if (this.isCollidedHorizontally) {
            this.motionY = 0.3D;
        }
        if (this.motionX * this.motionX + this.motionZ * this.motionZ > 0.001D) {
            float yaw = (float) (Math.atan2(this.motionZ, this.motionX) * 180.0D / Math.PI) - 90.0F;
            this.rotationYaw = this.renderYawOffset = this.rotationYawHead = yaw;
        }
        if (goal == null && this.onGround) {
            setFlying(false);
        }
        return true;
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
        this.breathAnim = 20;
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
        return Math.min(1.0F, this.breathAnim / 20.0F);
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

package com.netheryx.entity;

import com.netheryx.registry.ModItems;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

/** Champion fletri : boss des Catacombes ; coups fletrissants, et appelle des squelettes wither une fois blesse. */
public class EntityWitheredChampion extends EntityNetheryXBoss {
    public EntityWitheredChampion(World world) {
        super(world);
        setSize(0.8F, 2.6F);
        this.arenaRadius = 9;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        stats(320.0D, 0.3D, 10.0D);
    }

    @Override
    protected void onHit(EntityLivingBase target) {
        target.addPotionEffect(new PotionEffect(Potion.wither.id, 120, 1));
    }

    @Override
    protected void special(EntityLivingBase target) {
        if (getHealth() > getMaxHealth() * 0.6F) {
            shockwave(4.0D, 5.0F, 0.8D);
            return;
        }
        for (int i = 0; i < 2; i++) {
            EntitySkeleton skeleton = new EntitySkeleton(this.worldObj);
            skeleton.setSkeletonType(1);
            skeleton.setCurrentItemOrArmor(0, new ItemStack(Items.stone_sword));
            skeleton.setLocationAndAngles(this.posX + this.rand.nextInt(5) - 2, this.posY, this.posZ + this.rand.nextInt(5) - 2, 0.0F, 0.0F);
            skeleton.setAttackTarget(target);
            this.worldObj.spawnEntityInWorld(skeleton);
        }
        playSound("mob.wither.spawn", 0.6F, 1.4F);
    }

    @Override
    protected void dropFewItems(boolean hit, int looting) {
        dropItem(ModItems.championCrown, 1);
        drop(ModItems.witheredBone, 3, 6, looting);
    }

    @Override
    protected String getLivingSound() {
        return "mob.skeleton.say";
    }

    @Override
    protected String getHurtSound() {
        return "mob.skeleton.hurt";
    }

    @Override
    protected String getDeathSound() {
        return "mob.wither.death";
    }
}

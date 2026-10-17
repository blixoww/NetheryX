package com.netheryx.entity;

import com.netheryx.config.NetheryXConfig;
import com.netheryx.registry.ModEntities;
import com.netheryx.registry.ModItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

public class SulfurBombEntity extends ThrowableItemProjectile {
    private static final float EXPLOSION_POWER = 2.5F;

    public SulfurBombEntity(EntityType<? extends SulfurBombEntity> type, Level level) {
        super(type, level);
    }

    public SulfurBombEntity(Level level, LivingEntity shooter) {
        super(ModEntities.SULFUR_BOMB.get(), shooter, level);
    }

    public SulfurBombEntity(Level level, double x, double y, double z) {
        super(ModEntities.SULFUR_BOMB.get(), x, y, z, level);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.SULFUR_BOMB.get();
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide) {
            Level.ExplosionInteraction interaction = NetheryXConfig.SULFUR_BOMB_BREAKS_BLOCKS.get()
                    ? Level.ExplosionInteraction.TNT : Level.ExplosionInteraction.NONE;
            this.level().explode(this, this.getX(), this.getY(), this.getZ(), EXPLOSION_POWER, interaction);
            this.discard();
        }
    }
}

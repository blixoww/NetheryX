package com.netheryx.entity;

import com.netheryx.registry.ModParticles;
import com.netheryx.util.MobParticles;
import com.netheryx.config.NetheryXConfig;
import com.netheryx.registry.ModEntities;
import com.netheryx.util.GuardianSeals;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;

/**
 * Champion fletri : boss des Catacombes fletries. Version geante du Chevalier fletri qui appelle
 * des renforts a 66 % et 33 % de vie. Sa mort brise les sceaux de la salle au tresor.
 */
public class WitheredChampion extends WitheredKnight {
    private static final String TAG_WAVES = "SummonWaves";

    private final ServerBossEvent bossEvent = (ServerBossEvent) new ServerBossEvent(this.getDisplayName(),
            BossEvent.BossBarColor.WHITE, BossEvent.BossBarOverlay.NOTCHED_6).setDarkenScreen(true);
    private int summonWaves;
    private int dashCooldown = 100;
    private int dashTicks;

    public WitheredChampion(EntityType<? extends WitheredChampion> type, Level level) {
        super(type, level);
        this.xpReward = 150;
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 200.0D)
                .add(Attributes.ARMOR, 12.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 4.0D)
                .add(Attributes.ATTACK_DAMAGE, 13.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8D)
                .add(Attributes.FOLLOW_RANGE, 40.0D);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
                                        @Nullable SpawnGroupData spawnData, @Nullable CompoundTag dataTag) {
        var maxHealth = this.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.setBaseValue(NetheryXConfig.CHAMPION_MAX_HEALTH.get());
            this.setHealth(this.getMaxHealth());
        }
        return super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        float ratio = this.getHealth() / this.getMaxHealth();
        this.tickSoulDash();
        this.bossEvent.setProgress(ratio);
        int expectedWaves = ratio < 0.33F ? 2 : (ratio < 0.66F ? 1 : 0);
        if (this.summonWaves < expectedWaves && this.level() instanceof ServerLevel server) {
            this.summonWaves++;
            this.triggerAnim("attack", "attack");
            for (int i = 0; i < 2; i++) {
                WitheredKnight knight = ModEntities.WITHERED_KNIGHT.get().create(server);
                if (knight != null) {
                    knight.moveTo(this.getX() + (i == 0 ? 2.5D : -2.5D), this.getY(), this.getZ(), this.getYRot(), 0.0F);
                    knight.setTarget(this.getTarget());
                    server.addFreshEntity(knight);
                    server.sendParticles(ParticleTypes.SOUL, knight.getX(), knight.getY(0.5D), knight.getZ(), 30, 0.4D, 0.8D, 0.4D, 0.05D);
                }
            }
            this.playSound(SoundEvents.WITHER_AMBIENT, 1.5F, 0.7F);
        }
    }

    /** Ruee des ames : le Champion fonce sur une cible eloignee en laissant une trainee d'ames. */
    private void tickSoulDash() {
        net.minecraft.world.entity.LivingEntity target = this.getTarget();
        if (this.dashTicks > 0) {
            this.dashTicks--;
            if (this.level() instanceof ServerLevel server) {
                server.sendParticles(ParticleTypes.SOUL, this.getX(), this.getY(0.5D), this.getZ(), 4, 0.3D, 0.5D, 0.3D, 0.01D);
            }
            if (target != null && this.distanceToSqr(target) < 6.0D) {
                this.doHurtTarget(target);
                this.dashTicks = 0;
            }
            return;
        }
        if (target != null && --this.dashCooldown <= 0) {
            double distance = this.distanceToSqr(target);
            if (distance > 36.0D && distance < 400.0D) {
                net.minecraft.world.phys.Vec3 direction = target.position().subtract(this.position()).normalize();
                this.setDeltaMovement(direction.x * 1.6D, 0.35D, direction.z * 1.6D);
                this.hurtMarked = true;
                this.dashTicks = 12;
                this.playSound(SoundEvents.SOUL_ESCAPE, 2.0F, 0.6F);
            }
            this.dashCooldown = 100 + this.random.nextInt(60);
        }
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (this.level() instanceof ServerLevel server) {
            GuardianSeals.breakSealsAround(server, this.blockPosition());
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt(TAG_WAVES, this.summonWaves);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.summonWaves = tag.getInt(TAG_WAVES);
        if (this.hasCustomName()) {
            this.bossEvent.setName(this.getDisplayName());
        }
    }

    @Override
    public void setCustomName(@Nullable Component name) {
        super.setCustomName(name);
        this.bossEvent.setName(this.getDisplayName());
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public float getVoicePitch() {
        return 0.6F;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        MobParticles.ambient(this, ModParticles.WITHER_SPORE.get(), 2, 1.0D, -0.01D);
        MobParticles.ambient(this, ModParticles.SOUL_WISP.get(), 6, 0.8D, 0.02D);
    }
}

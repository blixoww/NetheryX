package com.netheryx.entity.boss;

import com.netheryx.config.NetheryXConfig;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;
import java.util.function.IntConsumer;

/**
 * Deroulement commun des combats de boss : reveil (titre, rugissement), points de vie adaptes au nombre de joueurs,
 * phases separees par une courte transition (le boss est invulnerable, l'arene tremble), dangers annonces, et fatigue
 * de minage autour du boss vivant (comme le gardien ancien : on n'entre pas dans l'arene en creusant).
 */
public final class BossEncounter {
    private static final UUID PLAYER_SCALE_ID = UUID.fromString("5d9b0f21-7a44-4c3e-9f16-0a7d2b6c8e11");
    /** Chaque joueur en plus ajoute 60 % de points de vie : plus de degats, mais plus de cibles a gerer pour le boss. */
    private static final double HEALTH_PER_EXTRA_PLAYER = 0.6D;
    private static final int MAX_EXTRA_PLAYERS = 5;
    private static final int TRANSITION_TICKS = 50;
    private static final double ENGAGE_RANGE = 14.0D;
    private static final double FATIGUE_RANGE = 44.0D;

    private final Mob boss;
    private final int color;
    private final float[] thresholds;
    private final IntConsumer onPhase;
    private final BossHazards hazards;
    private int phase;
    private int transition;
    private boolean engaged;

    /** thresholds : part de vie (decroissante) a laquelle chaque nouvelle phase commence ; onPhase recoit son numero (1, 2...). */
    public BossEncounter(Mob boss, int color, float[] thresholds, IntConsumer onPhase) {
        this.boss = boss;
        this.color = color;
        this.thresholds = thresholds;
        this.onPhase = onPhase;
        this.hazards = new BossHazards(boss);
    }

    public BossHazards hazards() {
        return this.hazards;
    }

    /** Numero de la phase en cours (0 au debut du combat). */
    public int phase() {
        return this.phase;
    }

    /** Vrai pendant une transition de phase : le boss n'agit plus et ne subit rien. */
    public boolean inTransition() {
        return this.transition > 0;
    }

    public boolean isEngaged() {
        return this.engaged;
    }

    /** A appeler a chaque tick serveur du boss. */
    public void tick(ServerLevel level) {
        this.hazards.tick(level);
        if (this.boss.tickCount % 40 == 0 && NetheryXConfig.BOSS_MINING_FATIGUE.get()) {
            for (Player player : this.near(level, FATIGUE_RANGE)) {
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 100, 2, true, false, true));
            }
        }
        if (!this.engaged) {
            if (!this.near(level, ENGAGE_RANGE).isEmpty()) {
                this.engage(level);
            }
            return;
        }
        if (this.transition > 0) {
            this.tickTransition(level);
            return;
        }
        float ratio = this.boss.getHealth() / this.boss.getMaxHealth();
        if (this.phase < this.thresholds.length && ratio < this.thresholds[this.phase]) {
            this.phase++;
            this.transition = TRANSITION_TICKS;
            this.boss.getNavigation().stop();
            this.onPhase.accept(this.phase);
            level.playSound(null, this.boss.blockPosition(), SoundEvents.WITHER_SPAWN, this.boss.getSoundSource(), 1.2F, 0.7F + this.phase * 0.2F);
            BossFx.shake(level, this.boss.position(), 40.0D, 0.9F, 30);
        }
    }

    private void tickTransition(ServerLevel level) {
        this.transition--;
        Vec3 feet = BossFx.onGround(level, this.boss.position());
        BossFx.runes(level, feet, 2.5D + (TRANSITION_TICKS - this.transition) * 0.06D, 4, this.boss.tickCount);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, this.boss.getX(), this.boss.getY(0.5D), this.boss.getZ(), 6, 1.0D, 1.4D, 1.0D, 0.2D);
        if (this.transition == 0) {
            // Fin de la transition : l'onde repousse ceux qui se sont approches pour profiter de la pause
            this.hazards.wave(level, this.boss.position(), 12.0D, 0.55D, 6.0F, this.color);
            level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, this.boss.getX(), this.boss.getY(0.5D), this.boss.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
            level.playSound(null, this.boss.blockPosition(), SoundEvents.GENERIC_EXPLODE, this.boss.getSoundSource(), 1.5F, 0.6F);
            BossFx.shake(level, this.boss.position(), 40.0D, 1.3F, 18);
        }
    }

    private void engage(ServerLevel level) {
        this.engaged = true;
        List<Player> players = this.near(level, 40.0D);
        this.scaleHealth(players.size());
        level.playSound(null, this.boss.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL, this.boss.getSoundSource(), 1.5F, 0.8F);
        BossFx.shake(level, this.boss.position(), 40.0D, 0.8F, 25);
        Component name = this.boss.getDisplayName();
        Component subtitle = Component.translatable("boss.netheryx.awakens");
        for (Player player : players) {
            if (player instanceof ServerPlayer server && server.connection != null) {
                server.connection.send(new ClientboundSetTitlesAnimationPacket(10, 50, 15));
                server.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
                server.connection.send(new ClientboundSetTitleTextPacket(name));
            }
        }
    }

    private void scaleHealth(int players) {
        AttributeInstance health = this.boss.getAttribute(Attributes.MAX_HEALTH);
        int extra = Math.min(MAX_EXTRA_PLAYERS, Math.max(0, players - 1));
        if (health == null || extra == 0 || health.getModifier(PLAYER_SCALE_ID) != null) {
            return;
        }
        float ratio = this.boss.getHealth() / this.boss.getMaxHealth();
        health.addPermanentModifier(new AttributeModifier(PLAYER_SCALE_ID, "NetheryX boss players",
                extra * HEALTH_PER_EXTRA_PLAYER, AttributeModifier.Operation.MULTIPLY_TOTAL));
        this.boss.setHealth(this.boss.getMaxHealth() * ratio);
    }

    private List<Player> near(ServerLevel level, double range) {
        return level.getEntitiesOfClass(Player.class, new AABB(this.boss.blockPosition()).inflate(range, Math.min(range, 32.0D), range),
                EntitySelector.NO_CREATIVE_OR_SPECTATOR);
    }

    /** Grand final : ondes, explosions et lumiere. A appeler depuis die() du boss. */
    public void deathBurst(ServerLevel level) {
        Vec3 feet = BossFx.onGround(level, this.boss.position());
        BossFx.shake(level, feet, 48.0D, 1.6F, 40);
        for (int i = 1; i <= 3; i++) {
            BossFx.shockwave(level, feet, 6.0D * i, 10 + i * 8, this.color);
        }
        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, this.boss.getX(), this.boss.getY(0.5D), this.boss.getZ(), 2, 0.8D, 0.8D, 0.8D, 0.0D);
        level.sendParticles(ParticleTypes.END_ROD, this.boss.getX(), this.boss.getY(0.5D), this.boss.getZ(), 120, 0.4D, 0.6D, 0.4D, 0.35D);
        BossFx.column(level, ParticleTypes.SOUL_FIRE_FLAME, feet, 12.0D, 60, 0.3D);
        level.playSound(null, this.boss.blockPosition(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, this.boss.getSoundSource(), 1.2F, 1.0F);
    }

    public void save(CompoundTag tag) {
        tag.putInt("BossPhase", this.phase);
        tag.putBoolean("BossEngaged", this.engaged);
    }

    public void load(CompoundTag tag) {
        this.phase = tag.getInt("BossPhase");
        this.engaged = tag.getBoolean("BossEngaged");
    }
}

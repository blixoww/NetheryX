package com.netheryx.blockentity;

import com.netheryx.block.DungeonTrapBlock;
import com.netheryx.entity.boss.BossFx;
import com.netheryx.entity.boss.BossHazards;
import com.netheryx.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Mecanisme de donjon : rejoue une attaque du boss de ce donjon, avec les memes annonces au sol. On apprend donc a esquiver en
 * explorant. Il ne s'active que si un joueur est a portee et a peu pres au meme etage, et il se casse a la pioche.
 * Le gabarit fournit le reglage : Mode, Period, Damage, Length, Width, Color, Charges, Entity.
 */
public class DungeonTrapBlockEntity extends BlockEntity {
    private static final double RANGE = 18.0D;
    private static final double FLOOR_GAP = 6.0D;
    private static final int AMBUSH_DELAY = 24;

    private String mode = "wave";
    private int period = 100;
    private float damage = 4.0F;
    private int length = 12;
    private int width = 3;
    private int color = BossFx.FIRE;
    private int charges = 3;
    private String spawned = "netheryx:withered_knight";
    private int timer = -1;
    private int ambushTimer;
    private Vec3 ambushSpot = Vec3.ZERO;
    private final BossHazards hazards = new BossHazards();

    public DungeonTrapBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DUNGEON_TRAP.get(), pos, state);
    }

    public BossHazards hazards() {
        return this.hazards;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, DungeonTrapBlockEntity trap) {
        if (level instanceof ServerLevel server) {
            trap.tick(server, pos, state);
        }
    }

    private void tick(ServerLevel level, BlockPos pos, BlockState state) {
        this.hazards.tick(level);
        if (this.ambushTimer > 0 && --this.ambushTimer == 0) {
            this.spawnAmbusher(level);
        }
        if (this.timer < 0) {
            // Des mecanismes voisins ne se declenchent pas tous au meme instant
            this.timer = level.random.nextInt(Math.max(1, this.period));
        }
        if (--this.timer > 0) {
            return;
        }
        this.timer = this.period;
        Player target = this.nearestPlayer(level, pos);
        if (target != null) {
            this.hazards.anchor(target);
            this.fire(level, pos, state.getValue(DungeonTrapBlock.FACING), target);
        }
    }

    private Player nearestPlayer(ServerLevel level, BlockPos pos) {
        Vec3 center = Vec3.atCenterOf(pos);
        Player nearest = null;
        double best = RANGE * RANGE;
        for (Player player : level.getEntitiesOfClass(Player.class, new AABB(center, center).inflate(RANGE, FLOOR_GAP, RANGE), EntitySelector.NO_CREATIVE_OR_SPECTATOR)) {
            double distance = player.distanceToSqr(center);
            if (distance < best) {
                best = distance;
                nearest = player;
            }
        }
        return nearest;
    }

    private void fire(ServerLevel level, BlockPos pos, Direction facing, Player target) {
        Vec3 above = Vec3.atBottomCenterOf(pos.above());
        Vec3 direction = Vec3.atLowerCornerOf(facing.getNormal());
        switch (this.mode) {
            case "wave" -> this.hazards.wave(level, above, this.length, 0.45D, this.damage, this.color);
            // A hauteur de poitrine, au-dessus du mecanisme : les piliers de la salle arretent le faisceau
            case "beam" -> this.hazards.beam(level, above.add(0.0D, 0.9D, 0.0D), level.random.nextDouble() * Math.PI * 2.0D, 2.0D, 30,
                    Math.max(40, this.period - 50), this.length, this.damage, this.color);
            case "line" -> this.hazards.line(level, above.add(direction), direction, this.length, 1.0D, 26, this.damage, this.color);
            case "spikes" -> this.hazards.line(level, above, target.position().subtract(above), this.length, 1.0D, 22, this.damage, this.color);
            case "circles" -> {
                this.hazards.circle(level, target.position(), 2.6D, 34, this.damage, this.color);
                for (int i = 0; i < this.width; i++) {
                    Vec3 around = target.position().add(level.random.nextGaussian() * 4.0D, 0.0D, level.random.nextGaussian() * 4.0D);
                    this.hazards.circle(level, around, 2.6D, 40 + i * 7, this.damage, this.color);
                }
            }
            case "flame_wall" -> this.flameWall(level, above, direction);
            case "gloom" -> {
                for (Player player : level.getEntitiesOfClass(Player.class, new AABB(pos).inflate(RANGE, FLOOR_GAP, RANGE), EntitySelector.NO_CREATIVE_OR_SPECTATOR)) {
                    player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 100, 0));
                }
                level.playSound(null, pos, SoundEvents.SCULK_SHRIEKER_SHRIEK, SoundSource.HOSTILE, 0.6F, 0.6F);
                this.hazards.circle(level, target.position(), 2.6D, 36, this.damage, this.color);
            }
            case "ambush" -> {
                if (this.charges > 0 && this.ambushTimer == 0) {
                    this.charges--;
                    this.setChanged();
                    // Le dos de la cible : le cercle laisse le temps de se retourner, comme la danse macabre du Champion
                    this.ambushSpot = target.position().subtract(target.getLookAngle().multiply(2.5D, 0.0D, 2.5D));
                    this.ambushTimer = AMBUSH_DELAY;
                    BossFx.telegraph(level, BossFx.onGround(level, this.ambushSpot), 1.5D, AMBUSH_DELAY, this.color);
                    level.playSound(null, pos, SoundEvents.WITHER_AMBIENT, SoundSource.HOSTILE, 0.8F, 0.6F);
                }
            }
            default -> {
            }
        }
    }

    /** Rangees de flammes qui avancent vers l'avant du mecanisme, avec une breche de deux blocs a trouver. */
    private void flameWall(ServerLevel level, Vec3 above, Vec3 direction) {
        Vec3 side = new Vec3(-direction.z, 0.0D, direction.x);
        int rows = Math.max(1, this.length / 2);
        for (int row = 0; row < rows; row++) {
            int gap = level.random.nextInt(Math.max(1, this.width * 2 - 1)) - this.width + 1;
            Vec3 middle = above.add(direction.scale(1.0D + row * 2.0D));
            int delay = 20 + row * 7;
            double leftLength = gap - 1 + this.width;
            double rightLength = this.width - gap - 1;
            if (leftLength > 0.5D) {
                this.hazards.line(level, middle.add(side.scale(-this.width)), side, leftLength, 0.7D, delay, this.damage, this.color);
            }
            if (rightLength > 0.5D) {
                this.hazards.line(level, middle.add(side.scale(this.width)), side.scale(-1.0D), rightLength, 0.7D, delay, this.damage, this.color);
            }
        }
    }

    private void spawnAmbusher(ServerLevel level) {
        ResourceLocation id = ResourceLocation.tryParse(this.spawned);
        if (id == null) {
            return;
        }
        Entity entity = BuiltInRegistries.ENTITY_TYPE.get(id).create(level);
        if (entity instanceof Mob mob) {
            Vec3 ground = BossFx.onGround(level, this.ambushSpot);
            // Pas d'apparition dans un mur : sans place libre, l'embuscade s'annule
            mob.moveTo(ground.x, ground.y, ground.z, level.random.nextFloat() * 360.0F, 0.0F);
            if (level.noCollision(mob)) {
                level.addFreshEntity(mob);
            }
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.mode = tag.getString("Mode");
        this.period = Math.max(20, tag.getInt("Period"));
        this.damage = tag.getFloat("Damage");
        this.length = tag.getInt("Length");
        this.width = tag.getInt("Width");
        this.color = tag.getInt("Color");
        this.charges = tag.getInt("Charges");
        if (tag.contains("Entity")) {
            this.spawned = tag.getString("Entity");
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString("Mode", this.mode);
        tag.putInt("Period", this.period);
        tag.putFloat("Damage", this.damage);
        tag.putInt("Length", this.length);
        tag.putInt("Width", this.width);
        tag.putInt("Color", this.color);
        tag.putInt("Charges", this.charges);
        tag.putString("Entity", this.spawned);
    }
}

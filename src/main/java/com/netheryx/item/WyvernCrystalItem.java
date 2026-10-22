package com.netheryx.item;

import com.netheryx.entity.AshWyvern;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Cristal de wyverne : range sa wyverne pour l'emporter partout. Clic droit sur sa wyverne apprivoisee : elle entre
 * dans le cristal (avec son nom, sa vie et son maitre). Clic droit sur un bloc : elle en ressort. Rangee, elle reste
 * la wyverne de son maitre (une seule par joueur). Si le cristal est detruit (explosion, vide), il se brise et libere
 * la wyverne : on ne peut pas la perdre.
 */
public class WyvernCrystalItem extends Item {
    private static final String TAG = "StoredWyvern";

    public WyvernCrystalItem(Properties properties) {
        super(properties);
    }

    public static boolean isFull(ItemStack stack) {
        return stack.getTag() != null && stack.getTag().contains(TAG, Tag.TAG_COMPOUND);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!(target instanceof AshWyvern wyvern) || isFull(stack)) {
            return InteractionResult.PASS;
        }
        if (!wyvern.isTame() || !wyvern.isOwnedBy(player)) {
            if (!player.level().isClientSide) {
                player.displayClientMessage(Component.translatable("message.netheryx.wyvern_crystal_not_yours").withStyle(ChatFormatting.RED), true);
            }
            return InteractionResult.FAIL;
        }
        if (player.level() instanceof ServerLevel level) {
            Vec3 at = wyvern.position();
            CompoundTag data = wyvern.store();
            // Le cristal en main (en creatif, la pile recue peut etre une copie)
            ItemStack held = player.getItemInHand(hand);
            held.getOrCreateTag().put(TAG, data);
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, at.x, at.y + 1.0D, at.z, 60, 1.0D, 1.0D, 1.0D, 0.1D);
            level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.2F, 0.8F);
            level.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.8F, 1.4F);
        }
        return InteractionResult.sidedSuccess(player.level().isClientSide);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        if (!isFull(stack)) {
            return InteractionResult.PASS;
        }
        if (context.getLevel() instanceof ServerLevel level) {
            BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
            float yaw = context.getPlayer() != null ? context.getPlayer().getYRot() + 180.0F : 0.0F;
            if (release(level, stack, Vec3.atBottomCenterOf(pos), yaw)) {
                level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.PLAYERS, 1.2F, 0.7F);
            }
        }
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
    }

    /** Fait sortir la wyverne du cristal a cette position ; le cristal redevient vide. */
    private static boolean release(ServerLevel level, ItemStack stack, Vec3 pos, float yaw) {
        CompoundTag data = stack.getTag() == null ? null : stack.getTag().getCompound(TAG);
        if (data == null || data.isEmpty()) {
            return false;
        }
        Entity entity = EntityType.loadEntityRecursive(data, level, e -> {
            e.moveTo(pos.x, pos.y, pos.z, yaw, 0.0F);
            return e;
        });
        if (entity == null || !level.tryAddFreshEntityWithPassengers(entity)) {
            return false;
        }
        stack.removeTagKey(TAG);
        if (entity instanceof AshWyvern wyvern) {
            wyvern.setOrderedToSit(false);
            wyvern.onReleased();
        }
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, pos.x, pos.y + 1.0D, pos.z, 60, 1.0D, 1.0D, 1.0D, 0.1D);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, pos.x, pos.y + 0.5D, pos.z, 20, 0.8D, 0.5D, 0.8D, 0.02D);
        return true;
    }

    /** Detruit (explosion, cactus...) : le cristal se brise et la wyverne en sort. */
    @Override
    public void onDestroyed(ItemEntity itemEntity) {
        if (itemEntity.level() instanceof ServerLevel level && isFull(itemEntity.getItem())) {
            release(level, itemEntity.getItem(), itemEntity.position(), 0.0F);
        }
    }

    /** Tombe dans le vide : le cristal revient a son maitre s'il est connecte, sinon la wyverne sort au point d'apparition. */
    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        if (entity.level() instanceof ServerLevel level && isFull(stack) && entity.getY() < level.getMinBuildHeight() + 1) {
            CompoundTag data = stack.getTag().getCompound(TAG);
            Player owner = data.hasUUID("Owner") ? level.getServer().getPlayerList().getPlayer(data.getUUID("Owner")) : null;
            if (owner != null && owner.getInventory().add(stack.copy())) {
                owner.displayClientMessage(Component.translatable("message.netheryx.wyvern_crystal_returned").withStyle(ChatFormatting.AQUA), false);
            } else {
                release(level, stack, Vec3.atBottomCenterOf(level.getSharedSpawnPos()), 0.0F);
            }
            entity.discard();
            return true;
        }
        return false;
    }

    /** Un cristal plein ne disparait jamais au sol. */
    @Override
    public int getEntityLifespan(ItemStack stack, Level level) {
        return isFull(stack) ? Integer.MAX_VALUE : super.getEntityLifespan(stack, level);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return isFull(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (!isFull(stack)) {
            tooltip.add(Component.translatable("item.netheryx.wyvern_crystal.empty").withStyle(ChatFormatting.GRAY));
            return;
        }
        CompoundTag data = stack.getTag().getCompound(TAG);
        Component name = data.contains("CustomName") ? Component.Serializer.fromJson(data.getString("CustomName")) : null;
        tooltip.add(Component.translatable("item.netheryx.wyvern_crystal.full",
                name != null ? name : Component.translatable("entity.netheryx.ash_wyvern")).withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("item.netheryx.wyvern_crystal.health", Math.round(data.getFloat("Health")),
                Math.round(AshWyvern.TAMED_HEALTH)).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.netheryx.wyvern_crystal.release").withStyle(ChatFormatting.GRAY));
    }
}

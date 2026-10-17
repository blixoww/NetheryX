package com.netheryx.blockentity;

import com.netheryx.block.SoulAltarBlock;
import com.netheryx.registry.ModBlockEntities;
import com.netheryx.registry.ModEnchantments;
import com.netheryx.registry.ModItems;
import com.netheryx.registry.ModParticles;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;
import java.util.function.Supplier;

/** Objet pose sur l'Autel des ames et logique des offrandes. */
public class SoulAltarBlockEntity extends BlockEntity {
    /** Offrande -> enchantement confere, quantite consommee. */
    private record Offering(Supplier<Item> item, Supplier<Enchantment> enchantment, int count) {
    }

    private static final Offering[] OFFERINGS = {
            new Offering(ModItems.MAGMA_SHELL, ModEnchantments.EMBER_STRIDE, 2),
            new Offering(ModItems.SOUL_DUST, ModEnchantments.SOUL_HARVEST, 4),
            new Offering(ModItems.BASALT_CARAPACE, ModEnchantments.ANCHORING, 2),
    };
    private static final int REQUIRED_FLAMES = 2;
    private static final int LEVELS_PER_ENCHANT_LEVEL = 5;

    private ItemStack item = ItemStack.EMPTY;
    /** Angle de depart aleatoire pour l'animation de l'objet flottant (client). */
    public final float spin = (float) (Math.random() * 360.0D);

    public SoulAltarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SOUL_ALTAR.get(), pos, state);
    }

    public ItemStack getItem() {
        return this.item;
    }

    private void setItem(ItemStack stack) {
        this.item = stack;
        this.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_ALL);
        }
    }

    public InteractionResult interact(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        boolean client = this.level == null || this.level.isClientSide;
        for (Offering offering : OFFERINGS) {
            if (held.is(offering.item().get()) && !this.item.isEmpty()) {
                if (!client) {
                    this.offer(player, held, offering);
                }
                return InteractionResult.sidedSuccess(client);
            }
        }
        if (this.item.isEmpty() && !held.isEmpty() && (held.isEnchantable() || held.isEnchanted() || held.is(Items.BOOK))) {
            if (!client) {
                this.setItem(held.split(1));
                this.level.playSound(null, this.worldPosition, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1.0F, 0.8F);
            }
            return InteractionResult.sidedSuccess(client);
        }
        if (!this.item.isEmpty() && held.isEmpty()) {
            if (!client) {
                player.setItemInHand(hand, this.item);
                this.setItem(ItemStack.EMPTY);
                this.level.playSound(null, this.worldPosition, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 1.0F, 0.8F);
            }
            return InteractionResult.sidedSuccess(client);
        }
        return InteractionResult.PASS;
    }

    private void offer(Player player, ItemStack held, Offering offering) {
        Enchantment enchantment = offering.enchantment().get();
        boolean book = this.item.is(Items.BOOK) || this.item.is(Items.ENCHANTED_BOOK);
        Map<Enchantment, Integer> current = EnchantmentHelper.getEnchantments(this.item);
        int level = current.getOrDefault(enchantment, 0) + 1;
        Component problem = null;
        if (level > enchantment.getMaxLevel()) {
            problem = Component.translatable("block.netheryx.soul_altar.max_level");
        } else if (!book && !enchantment.canEnchant(this.item)) {
            problem = Component.translatable("block.netheryx.soul_altar.incompatible");
        } else if (current.keySet().stream().anyMatch(other -> other != enchantment && !other.isCompatibleWith(enchantment))) {
            problem = Component.translatable("block.netheryx.soul_altar.incompatible");
        } else if (SoulAltarBlock.countSoulFlames(this.level, this.worldPosition) < REQUIRED_FLAMES) {
            problem = Component.translatable("block.netheryx.soul_altar.no_flames", REQUIRED_FLAMES);
        } else if (held.getCount() < offering.count() && !player.getAbilities().instabuild) {
            problem = Component.translatable("block.netheryx.soul_altar.offering", offering.count(), held.getHoverName());
        } else if (player.experienceLevel < level * LEVELS_PER_ENCHANT_LEVEL && !player.getAbilities().instabuild) {
            problem = Component.translatable("block.netheryx.soul_altar.experience", level * LEVELS_PER_ENCHANT_LEVEL);
        }
        if (problem != null) {
            player.displayClientMessage(problem.copy().withStyle(ChatFormatting.RED), true);
            return;
        }
        if (!player.getAbilities().instabuild) {
            held.shrink(offering.count());
            player.giveExperienceLevels(-level * LEVELS_PER_ENCHANT_LEVEL);
        }
        if (book) {
            // Un livre (vierge ou deja enchante) devient un livre enchante contenant aussi le nouvel enchantement
            current.put(enchantment, level);
            ItemStack result = new ItemStack(Items.ENCHANTED_BOOK);
            for (Map.Entry<Enchantment, Integer> entry : current.entrySet()) {
                EnchantedBookItem.addEnchantment(result, new EnchantmentInstance(entry.getKey(), entry.getValue()));
            }
            this.setItem(result);
        } else {
            ItemStack result = this.item.copy();
            current.put(enchantment, level);
            EnchantmentHelper.setEnchantments(current, result);
            this.setItem(result);
        }
        this.level.playSound(null, this.worldPosition, SoundEvents.SOUL_ESCAPE, SoundSource.BLOCKS, 1.2F, 0.9F);
        this.level.playSound(null, this.worldPosition, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, 0.7F);
        if (this.level instanceof ServerLevel server) {
            server.sendParticles(ModParticles.SOUL_WISP.get(), this.worldPosition.getX() + 0.5D, this.worldPosition.getY() + 1.3D,
                    this.worldPosition.getZ() + 0.5D, 30, 0.4D, 0.4D, 0.4D, 0.02D);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.item = ItemStack.of(tag.getCompound("Item"));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Item", this.item.save(new CompoundTag()));
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}

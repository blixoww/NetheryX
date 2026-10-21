package com.netheryx.blockentity;

import com.netheryx.block.SoulAltarBlock;
import com.netheryx.menu.SoulAltarMenu;
import com.netheryx.registry.ModBlockEntities;
import com.netheryx.registry.ModEnchantments;
import com.netheryx.registry.ModItems;
import com.netheryx.registry.ModParticles;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Autel des ames, gere comme une machine : un emplacement pour l'objet a enchanter, un pour l'offrande. Quand tout est
 * pret (offrande suffisante, flammes des ames autour, experience), le bouton « Infuser » lance un rituel de quelques
 * secondes ; a la fin, l'offrande et l'experience sont consommees et l'enchantement est applique.
 */
public class SoulAltarBlockEntity extends BlockEntity implements MenuProvider {
    /** Offrande -> enchantement confere, quantite consommee et cle de traduction des objets acceptes. */
    public record Offering(Supplier<Item> item, Supplier<Enchantment> enchantment, int count, String targets) {
    }

    public static final Offering[] OFFERINGS = {
            new Offering(ModItems.MAGMA_SHELL, ModEnchantments.EMBER_STRIDE, 2, "boots"),
            new Offering(ModItems.SOUL_DUST, ModEnchantments.SOUL_HARVEST, 4, "weapons"),
            new Offering(ModItems.BASALT_CARAPACE, ModEnchantments.ANCHORING, 2, "chest_legs"),
    };
    public static final int SLOT_ITEM = 0;
    public static final int SLOT_OFFERING = 1;
    public static final int REQUIRED_FLAMES = 2;
    public static final int LEVELS_PER_ENCHANT_LEVEL = 5;
    public static final int RITUAL_TICKS = 60;

    /** Etat affiche par l'interface (voir {@link #check}). */
    public enum Status {
        NO_ITEM, NO_OFFERING, UNKNOWN_OFFERING, MAX_LEVEL, INCOMPATIBLE, NOT_ENOUGH_OFFERING, NO_FLAMES, NO_EXPERIENCE, READY,
        IN_PROGRESS
    }

    private final SimpleContainer inventory = new SimpleContainer(2) {
        @Override
        public void setChanged() {
            super.setChanged();
            if (!SoulAltarBlockEntity.this.loading) {
                SoulAltarBlockEntity.this.sync();
            }
        }
    };
    private int progress;
    private boolean loading;
    @Nullable
    private UUID ritualist;
    /** Angle de depart aleatoire pour l'animation de l'objet flottant (client). */
    public final float spin = (float) (Math.random() * 360.0D);

    public SoulAltarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SOUL_ALTAR.get(), pos, state);
    }

    public SimpleContainer getInventory() {
        return this.inventory;
    }

    /** Objet pose sur l'autel (affiche en flottant au-dessus). */
    public ItemStack getItem() {
        return this.inventory.getItem(SLOT_ITEM);
    }

    public int getProgress() {
        return this.progress;
    }

    public boolean isWorking() {
        return this.progress > 0;
    }

    private void sync() {
        this.setChanged();
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_ALL);
        }
    }

    public static boolean acceptsItem(ItemStack stack) {
        return stack.isEnchantable() || stack.isEnchanted() || stack.is(Items.BOOK) || stack.is(Items.ENCHANTED_BOOK);
    }

    public static int offeringIndex(ItemStack stack) {
        for (int i = 0; i < OFFERINGS.length; i++) {
            if (stack.is(OFFERINGS[i].item().get())) {
                return i;
            }
        }
        return -1;
    }

    public int countFlames() {
        return this.level == null ? 0 : SoulAltarBlock.countSoulFlames(this.level, this.worldPosition);
    }

    /** Niveau d'enchantement que l'infusion donnerait (0 sans offrande reconnue). */
    public int resultLevel() {
        int index = offeringIndex(this.inventory.getItem(SLOT_OFFERING));
        if (index < 0) {
            return 0;
        }
        return EnchantmentHelper.getEnchantments(this.getItem()).getOrDefault(OFFERINGS[index].enchantment().get(), 0) + 1;
    }

    /** Verifie tout ce qui manque, dans l'ordre ou le joueur doit s'en occuper. */
    public Status check(@Nullable Player player) {
        if (this.isWorking()) {
            return Status.IN_PROGRESS;
        }
        ItemStack item = this.getItem();
        ItemStack offeringStack = this.inventory.getItem(SLOT_OFFERING);
        if (item.isEmpty()) {
            return Status.NO_ITEM;
        }
        if (offeringStack.isEmpty()) {
            return Status.NO_OFFERING;
        }
        int index = offeringIndex(offeringStack);
        if (index < 0) {
            return Status.UNKNOWN_OFFERING;
        }
        Offering offering = OFFERINGS[index];
        Enchantment enchantment = offering.enchantment().get();
        boolean book = item.is(Items.BOOK) || item.is(Items.ENCHANTED_BOOK);
        Map<Enchantment, Integer> current = EnchantmentHelper.getEnchantments(item);
        int level = current.getOrDefault(enchantment, 0) + 1;
        boolean creative = player != null && player.getAbilities().instabuild;
        if (level > enchantment.getMaxLevel()) {
            return Status.MAX_LEVEL;
        }
        if ((!book && !enchantment.canEnchant(item))
                || current.keySet().stream().anyMatch(other -> other != enchantment && !other.isCompatibleWith(enchantment))) {
            return Status.INCOMPATIBLE;
        }
        if (offeringStack.getCount() < offering.count() && !creative) {
            return Status.NOT_ENOUGH_OFFERING;
        }
        if (this.countFlames() < REQUIRED_FLAMES) {
            return Status.NO_FLAMES;
        }
        if (player != null && player.experienceLevel < level * LEVELS_PER_ENCHANT_LEVEL && !creative) {
            return Status.NO_EXPERIENCE;
        }
        return Status.READY;
    }

    /** Bouton « Infuser » : lance le rituel si tout est pret. */
    public boolean startRitual(Player player) {
        if (this.check(player) != Status.READY || this.level == null) {
            return false;
        }
        this.progress = 1;
        this.ritualist = player.getUUID();
        this.level.playSound(null, this.worldPosition, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 0.8F, 1.4F);
        this.sync();
        return true;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SoulAltarBlockEntity altar) {
        if (altar.progress <= 0 || !(level instanceof ServerLevel server)) {
            return;
        }
        altar.progress++;
        double angle = altar.progress * 0.35D;
        for (int k = 0; k < 2; k++) {
            double a = angle + k * Math.PI;
            server.sendParticles(ModParticles.SOUL_WISP.get(), pos.getX() + 0.5D + Math.cos(a) * 0.9D, pos.getY() + 0.9D + altar.progress * 0.012D,
                    pos.getZ() + 0.5D + Math.sin(a) * 0.9D, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
        if (altar.progress % 4 == 0) {
            server.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, pos.getX() + 0.5D, pos.getY() + 1.4D, pos.getZ() + 0.5D, 2, 0.15D, 0.15D, 0.15D, 0.01D);
        }
        if (altar.progress % 20 == 0) {
            level.playSound(null, pos, SoundEvents.SOUL_ESCAPE, SoundSource.BLOCKS, 0.8F, 0.8F + altar.progress * 0.005F);
        }
        if (altar.progress < RITUAL_TICKS) {
            return;
        }
        Player player = altar.ritualist == null ? null : level.getPlayerByUUID(altar.ritualist);
        altar.progress = 0;
        altar.ritualist = null;
        Status status = player == null ? Status.NO_EXPERIENCE : altar.check(player);
        if (status != Status.READY) {
            // Quelque chose a change pendant le rituel (objet retire, flamme eteinte...) : rien n'est consomme
            if (player != null) {
                player.displayClientMessage(Component.translatable("screen.netheryx.soul_altar.failed").withStyle(ChatFormatting.RED), true);
            }
            level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.8F, 0.8F);
            altar.sync();
            return;
        }
        altar.infuse(player);
    }

    private void infuse(Player player) {
        ItemStack offeringStack = this.inventory.getItem(SLOT_OFFERING);
        Offering offering = OFFERINGS[offeringIndex(offeringStack)];
        Enchantment enchantment = offering.enchantment().get();
        ItemStack item = this.getItem();
        Map<Enchantment, Integer> current = EnchantmentHelper.getEnchantments(item);
        int level = current.getOrDefault(enchantment, 0) + 1;
        if (!player.getAbilities().instabuild) {
            offeringStack.shrink(offering.count());
            player.giveExperienceLevels(-level * LEVELS_PER_ENCHANT_LEVEL);
        }
        current.put(enchantment, level);
        ItemStack result;
        if (item.is(Items.BOOK) || item.is(Items.ENCHANTED_BOOK)) {
            // Un livre (vierge ou deja enchante) devient un livre enchante contenant aussi le nouvel enchantement
            result = new ItemStack(Items.ENCHANTED_BOOK);
            for (Map.Entry<Enchantment, Integer> entry : current.entrySet()) {
                EnchantedBookItem.addEnchantment(result, new EnchantmentInstance(entry.getKey(), entry.getValue()));
            }
        } else {
            result = item.copy();
            EnchantmentHelper.setEnchantments(current, result);
        }
        this.inventory.setItem(SLOT_ITEM, result);
        this.inventory.setChanged();
        this.level.playSound(null, this.worldPosition, SoundEvents.SOUL_ESCAPE, SoundSource.BLOCKS, 1.2F, 0.9F);
        this.level.playSound(null, this.worldPosition, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, 0.7F);
        if (this.level instanceof ServerLevel server) {
            server.sendParticles(ModParticles.SOUL_WISP.get(), this.worldPosition.getX() + 0.5D, this.worldPosition.getY() + 1.3D,
                    this.worldPosition.getZ() + 0.5D, 30, 0.4D, 0.4D, 0.4D, 0.02D);
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.netheryx.soul_altar");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new SoulAltarMenu(id, playerInventory, this);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.loading = true;
        this.inventory.removeAllItems();
        // Ancien format (une seule pile « Item ») et nouveau (« Items »)
        if (tag.contains("Item")) {
            this.inventory.setItem(SLOT_ITEM, ItemStack.of(tag.getCompound("Item")));
        }
        if (tag.contains("Offering")) {
            this.inventory.setItem(SLOT_OFFERING, ItemStack.of(tag.getCompound("Offering")));
        }
        this.loading = false;
        this.progress = tag.getInt("Progress");
        this.ritualist = tag.hasUUID("Ritualist") ? tag.getUUID("Ritualist") : null;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Item", this.getItem().save(new CompoundTag()));
        tag.put("Offering", this.inventory.getItem(SLOT_OFFERING).save(new CompoundTag()));
        tag.putInt("Progress", this.progress);
        if (this.ritualist != null) {
            tag.putUUID("Ritualist", this.ritualist);
        }
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

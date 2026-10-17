package com.netheryx.blockentity;

import com.netheryx.NetheryX;
import com.netheryx.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.HopperMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

/** Contenu et logique de la Nasse a magma (5 emplacements de prises, compteur d'appats). */
public class LavaTrapBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {
    public static final ResourceLocation LOOT = NetheryX.id("gameplay/lava_trap");
    private static final int SIZE = 5;
    private static final int MAX_BAIT = 16;
    private static final int MIN_LAVA = 6;
    private static final int[] SLOTS = {0, 1, 2, 3, 4};

    private NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
    private int progress;
    private int bait;
    private int lavaCache;

    public LavaTrapBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LAVA_TRAP.get(), pos, state);
    }

    public boolean addBait() {
        if (this.bait >= MAX_BAIT) {
            return false;
        }
        this.bait++;
        this.setChanged();
        return true;
    }

    /** Nombre de sources de lave dans un cube de 5x5x5 autour de la nasse. */
    private static int countLava(Level level, BlockPos center) {
        int lava = 0;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-2, -2, -2), center.offset(2, 2, 2))) {
            if (level.getFluidState(pos).is(FluidTags.LAVA) && level.getFluidState(pos).isSource()) {
                lava++;
            }
        }
        return lava;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, LavaTrapBlockEntity trap) {
        if (level.getGameTime() % 100L == pos.asLong() % 100L) {
            trap.lavaCache = countLava(level, pos);
        }
        if (trap.lavaCache < MIN_LAVA) {
            return;
        }
        // Environ une prise toutes les 2 minutes avec 60 blocs de lave, deux fois plus vite avec un appat
        trap.progress += trap.lavaCache / 6 * (trap.bait > 0 ? 2 : 1);
        if (trap.progress < 2400) {
            return;
        }
        trap.progress = 0;
        if (level instanceof ServerLevel server && trap.catchSomething(server)) {
            if (trap.bait > 0) {
                trap.bait--;
            }
            server.playSound(null, pos, SoundEvents.FISHING_BOBBER_SPLASH, SoundSource.BLOCKS, 0.6F, 0.6F);
            server.sendParticles(ParticleTypes.LAVA, pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, 6, 0.3D, 0.2D, 0.3D, 0.0D);
        }
        trap.setChanged();
    }

    private boolean catchSomething(ServerLevel level) {
        LootTable table = level.getServer().getLootData().getLootTable(LOOT);
        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(this.worldPosition))
                .create(LootContextParamSets.CHEST);
        boolean caught = false;
        for (ItemStack stack : table.getRandomItems(params)) {
            ItemStack rest = this.insert(stack);
            caught |= rest.getCount() != stack.getCount();
        }
        return caught;
    }

    private ItemStack insert(ItemStack stack) {
        for (int i = 0; i < SIZE && !stack.isEmpty(); i++) {
            ItemStack slot = this.items.get(i);
            if (slot.isEmpty()) {
                this.items.set(i, stack.copy());
                return ItemStack.EMPTY;
            }
            if (ItemStack.isSameItemSameTags(slot, stack) && slot.getCount() < slot.getMaxStackSize()) {
                int moved = Math.min(stack.getCount(), slot.getMaxStackSize() - slot.getCount());
                slot.grow(moved);
                stack.shrink(moved);
            }
        }
        return stack;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, this.items);
        this.progress = tag.getInt("Progress");
        this.bait = tag.getInt("Bait");
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, this.items);
        tag.putInt("Progress", this.progress);
        tag.putInt("Bait", this.bait);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.netheryx.lava_trap");
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new HopperMenu(id, inventory, this);
    }

    @Override
    public int getContainerSize() {
        return SIZE;
    }

    @Override
    public boolean isEmpty() {
        return this.items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(this.items, slot, amount);
        if (!result.isEmpty()) {
            this.setChanged();
        }
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(this.items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        this.items.set(slot, stack);
        this.setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return net.minecraft.world.Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        this.items.clear();
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return SLOTS;
    }

    /** Les prises ne peuvent qu'etre retirees (pas d'insertion par entonnoir). */
    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction side) {
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return true;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return false;
    }
}

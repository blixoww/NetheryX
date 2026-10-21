package com.netheryx.menu;

import com.netheryx.blockentity.SoulAltarBlockEntity;
import com.netheryx.registry.ModBlocks;
import com.netheryx.registry.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Menu de l'Autel des ames : objet a enchanter, offrande, inventaire du joueur, et l'etat de l'autel synchronise
 * (flammes, ce qui manque, enchantement obtenu, cout, avancement du rituel). Le bouton 0 lance l'infusion.
 */
public class SoulAltarMenu extends AbstractContainerMenu {
    public static final int DATA_FLAMES = 0;
    public static final int DATA_STATUS = 1;
    public static final int DATA_OFFERING = 2;
    public static final int DATA_LEVEL = 3;
    public static final int DATA_PROGRESS = 4;
    private static final int DATA_COUNT = 5;
    /** Decalage de la colonne principale : le guide des offrandes occupe la gauche de l'interface. */
    public static final int MAIN_X = 108;

    @Nullable
    private final SoulAltarBlockEntity altar;
    private final ContainerLevelAccess access;
    private final ContainerData data;

    /** Cote client : la position de l'autel arrive avec le paquet d'ouverture. */
    public SoulAltarMenu(int id, Inventory playerInventory, FriendlyByteBuf buf) {
        this(id, playerInventory, playerInventory.player.level().getBlockEntity(buf.readBlockPos()) instanceof SoulAltarBlockEntity altar
                ? altar : null, new SimpleContainerData(DATA_COUNT));
    }

    public SoulAltarMenu(int id, Inventory playerInventory, SoulAltarBlockEntity altar) {
        this(id, playerInventory, altar, new ContainerData() {
            @Override
            public int get(int index) {
                Player player = playerInventory.player;
                return switch (index) {
                    case DATA_FLAMES -> altar.countFlames();
                    case DATA_STATUS -> altar.check(player).ordinal();
                    case DATA_OFFERING -> SoulAltarBlockEntity.offeringIndex(altar.getInventory().getItem(SoulAltarBlockEntity.SLOT_OFFERING));
                    case DATA_LEVEL -> altar.resultLevel();
                    case DATA_PROGRESS -> altar.getProgress();
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
            }

            @Override
            public int getCount() {
                return DATA_COUNT;
            }
        });
    }

    private SoulAltarMenu(int id, Inventory playerInventory, @Nullable SoulAltarBlockEntity altar, ContainerData data) {
        super(ModMenus.SOUL_ALTAR.get(), id);
        this.altar = altar;
        this.access = altar == null || altar.getLevel() == null ? ContainerLevelAccess.NULL
                : ContainerLevelAccess.create(altar.getLevel(), altar.getBlockPos());
        this.data = data;
        Container container = altar != null ? altar.getInventory() : new SimpleContainer(2);
        this.addSlot(new Slot(container, SoulAltarBlockEntity.SLOT_ITEM, MAIN_X + 17, 20) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return SoulAltarBlockEntity.acceptsItem(stack) && !SoulAltarMenu.this.working();
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }

            @Override
            public boolean mayPickup(Player player) {
                return !SoulAltarMenu.this.working();
            }
        });
        this.addSlot(new Slot(container, SoulAltarBlockEntity.SLOT_OFFERING, MAIN_X + 17, 62) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return SoulAltarBlockEntity.offeringIndex(stack) >= 0 && !SoulAltarMenu.this.working();
            }

            @Override
            public boolean mayPickup(Player player) {
                return !SoulAltarMenu.this.working();
            }
        });
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, MAIN_X + 8 + col * 18, 104 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, MAIN_X + 8 + col * 18, 162));
        }
        this.addDataSlots(data);
    }

    private boolean working() {
        return this.data.get(DATA_PROGRESS) > 0;
    }

    public int getFlames() {
        return this.data.get(DATA_FLAMES);
    }

    public SoulAltarBlockEntity.Status getStatus() {
        SoulAltarBlockEntity.Status[] values = SoulAltarBlockEntity.Status.values();
        int ordinal = this.data.get(DATA_STATUS);
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : SoulAltarBlockEntity.Status.NO_ITEM;
    }

    /** Offrande reconnue dans l'emplacement (-1 sinon). */
    public int getOfferingIndex() {
        return (short) this.data.get(DATA_OFFERING);
    }

    public int getResultLevel() {
        return this.data.get(DATA_LEVEL);
    }

    public int getCost() {
        return this.getResultLevel() * SoulAltarBlockEntity.LEVELS_PER_ENCHANT_LEVEL;
    }

    public float getProgress() {
        return Math.min(1.0F, this.data.get(DATA_PROGRESS) / (float) SoulAltarBlockEntity.RITUAL_TICKS);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        return id == 0 && this.altar != null && this.altar.startRitual(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (index < 2) {
            if (!this.moveItemStackTo(stack, 2, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (SoulAltarBlockEntity.offeringIndex(stack) >= 0) {
            if (!this.moveItemStackTo(stack, 1, 2, false)) {
                return ItemStack.EMPTY;
            }
        } else if (SoulAltarBlockEntity.acceptsItem(stack)) {
            if (!this.moveItemStackTo(stack, 0, 1, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, ModBlocks.SOUL_ALTAR.get());
    }
}

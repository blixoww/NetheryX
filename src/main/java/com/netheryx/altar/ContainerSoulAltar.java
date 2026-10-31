package com.netheryx.altar;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/** Conteneur de l'autel : objet, offrande et inventaire du joueur ; le bouton « Infuser » passe par enchantItem. */
public class ContainerSoulAltar extends Container {
    /** Decalage horizontal du panneau principal (le guide des offrandes est a gauche). */
    public static final int MAIN_X = 104;
    private final TileSoulAltar altar;
    private int lastProgress = -1;

    public ContainerSoulAltar(InventoryPlayer inventory, TileSoulAltar altar) {
        this.altar = altar;
        addSlotToContainer(new Slot(altar, TileSoulAltar.SLOT_ITEM, MAIN_X + 17, 20) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return TileSoulAltar.acceptsItem(stack);
            }

            @Override
            public int getSlotStackLimit() {
                return 1;
            }
        });
        addSlotToContainer(new Slot(altar, TileSoulAltar.SLOT_OFFERING, MAIN_X + 17, 62) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return TileSoulAltar.offeringIndex(stack) >= 0;
            }
        });
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlotToContainer(new Slot(inventory, col + row * 9 + 9, MAIN_X + 8 + col * 18, 104 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlotToContainer(new Slot(inventory, col, MAIN_X + 8 + col * 18, 162));
        }
    }

    public TileSoulAltar getAltar() {
        return this.altar;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return this.altar.isUseableByPlayer(player);
    }

    @Override
    public boolean enchantItem(EntityPlayer player, int id) {
        return id == 0 && this.altar.startRitual(player);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        int progress = this.altar.getProgress();
        if (progress != this.lastProgress) {
            for (Object crafter : this.crafters) {
                ((ICrafting) crafter).sendProgressBarUpdate(this, 0, progress);
            }
            this.lastProgress = progress;
        }
    }

    @Override
    public void updateProgressBar(int id, int value) {
        if (id == 0) {
            this.altar.setProgress(value);
        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        Slot slot = (Slot) this.inventorySlots.get(index);
        if (slot == null || !slot.getHasStack()) {
            return null;
        }
        ItemStack stack = slot.getStack();
        ItemStack copy = stack.copy();
        if (index < 2) {
            if (!mergeItemStack(stack, 2, 38, true)) {
                return null;
            }
        } else if (TileSoulAltar.offeringIndex(stack) >= 0) {
            if (!mergeItemStack(stack, 1, 2, false)) {
                return null;
            }
        } else if (TileSoulAltar.acceptsItem(stack) && !((Slot) this.inventorySlots.get(0)).getHasStack()) {
            ItemStack one = stack.splitStack(1);
            ((Slot) this.inventorySlots.get(0)).putStack(one);
        } else {
            return null;
        }
        if (stack.stackSize == 0) {
            slot.putStack(null);
        } else {
            slot.onSlotChanged();
        }
        return copy;
    }
}

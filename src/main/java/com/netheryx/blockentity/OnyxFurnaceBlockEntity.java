package com.netheryx.blockentity;

import com.netheryx.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.FurnaceMenu;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Four en onyx : un four classique (toutes les recettes de cuisson) dont le bloc fait deux cycles de cuisson
 * par tick (voir {@link com.netheryx.block.OnyxFurnaceBlock}), donc deux fois plus rapide.
 */
public class OnyxFurnaceBlockEntity extends AbstractFurnaceBlockEntity {
    public OnyxFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ONYX_FURNACE.get(), pos, state, RecipeType.SMELTING);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.netheryx.onyx_furnace");
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new FurnaceMenu(containerId, inventory, this, this.dataAccess);
    }
}

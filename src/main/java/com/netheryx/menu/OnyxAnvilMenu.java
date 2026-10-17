package com.netheryx.menu;

import com.netheryx.registry.ModBlocks;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Menu de l'enclume en onyx. Il reutilise l'ecran vanilla de l'enclume (meme type de menu) :
 * seuls le cout en experience (divise par deux) et le bloc valide changent.
 */
public class OnyxAnvilMenu extends AnvilMenu {
    public OnyxAnvilMenu(int containerId, Inventory inventory, ContainerLevelAccess access) {
        super(containerId, inventory, access);
    }

    @Override
    public void createResult() {
        super.createResult();
        int cost = this.getCost();
        if (cost > 1) {
            this.setMaximumCost(Math.max(1, cost / 2));
        }
    }

    @Override
    protected boolean isValidBlock(BlockState state) {
        return state.is(ModBlocks.ONYX_ANVIL.get());
    }
}

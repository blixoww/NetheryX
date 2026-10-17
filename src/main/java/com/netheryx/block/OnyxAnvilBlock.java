package com.netheryx.block;

import com.netheryx.menu.OnyxAnvilMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Enclume en onyx : elle ne s'abime jamais (elle n'est pas dans le tag {@code minecraft:anvil}, que l'enclume
 * vanilla utilise pour se degrader) et chaque operation coute deux fois moins de niveaux d'experience.
 */
public class OnyxAnvilBlock extends AnvilBlock {
    private static final Component TITLE = Component.translatable("container.netheryx.onyx_anvil");

    public OnyxAnvilBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return new SimpleMenuProvider((id, inventory, player) ->
                new OnyxAnvilMenu(id, inventory, ContainerLevelAccess.create(level, pos)), TITLE);
    }
}

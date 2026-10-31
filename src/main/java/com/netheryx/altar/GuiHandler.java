package com.netheryx.altar;

import cpw.mods.fml.common.network.IGuiHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/** Interfaces du mod (une seule : l'autel des ames). */
public class GuiHandler implements IGuiHandler {
    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(x, y, z);
        return id == BlockSoulAltar.GUI_ID && tile instanceof TileSoulAltar ? new ContainerSoulAltar(player.inventory, (TileSoulAltar) tile) : null;
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(x, y, z);
        return id == BlockSoulAltar.GUI_ID && tile instanceof TileSoulAltar ? new GuiSoulAltar(player.inventory, (TileSoulAltar) tile) : null;
    }
}

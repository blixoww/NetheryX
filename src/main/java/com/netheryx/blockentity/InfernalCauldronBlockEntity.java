package com.netheryx.blockentity;

import com.netheryx.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Potion contenue dans le Chaudron infernal (le nombre de doses est porte par l'etat du bloc). */
public class InfernalCauldronBlockEntity extends BlockEntity {
    private Potion potion = Potions.EMPTY;

    public InfernalCauldronBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.INFERNAL_CAULDRON.get(), pos, state);
    }

    public Potion getPotion() {
        return this.potion;
    }

    public void setPotion(Potion potion) {
        this.potion = potion;
        this.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    /** Couleur du liquide (teinte du modele). */
    public int getColor() {
        return this.potion == Potions.EMPTY ? 0x3F76E4 : PotionUtils.getColor(this.potion);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        ResourceLocation id = ResourceLocation.tryParse(tag.getString("Potion"));
        this.potion = id == null ? Potions.EMPTY : BuiltInRegistries.POTION.get(id);
        if (this.level != null && this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString("Potion", BuiltInRegistries.POTION.getKey(this.potion).toString());
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

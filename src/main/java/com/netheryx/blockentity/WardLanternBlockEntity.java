package com.netheryx.blockentity;

import com.netheryx.registry.ModBlockEntities;
import com.netheryx.util.WardLanterns;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Inscrit la lanterne aupres de {@link WardLanterns} tant que son chunk est charge. */
public class WardLanternBlockEntity extends BlockEntity {
    public WardLanternBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WARD_LANTERN.get(), pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (this.level != null && !this.level.isClientSide) {
            WardLanterns.add(this.level, this.worldPosition);
        }
    }

    @Override
    public void setRemoved() {
        if (this.level != null && !this.level.isClientSide) {
            WardLanterns.remove(this.level, this.worldPosition);
        }
        super.setRemoved();
    }

    @Override
    public void onChunkUnloaded() {
        if (this.level != null && !this.level.isClientSide) {
            WardLanterns.remove(this.level, this.worldPosition);
        }
        super.onChunkUnloaded();
    }
}

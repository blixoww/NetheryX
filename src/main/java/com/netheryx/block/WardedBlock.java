package com.netheryx.block;

import com.netheryx.registry.ModBlocks;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;

import java.util.Locale;

/**
 * Maconnerie scellee : enveloppe indestructible des salles au tresor des donjons (murs, sol, plafond, tour).
 * Elle reprend l'aspect des briques du donjon, parcourues de runes, et redevient de la maconnerie normale
 * a la mort du boss (voir {@code GuardianSeals}). Impossible donc de contourner le boss en creusant.
 */
public class WardedBlock extends Block {
    public enum Variant implements StringRepresentable {
        ONYX, MAGMA, BLACKSTONE;

        @Override
        public String getSerializedName() {
            return this.name().toLowerCase(Locale.ROOT);
        }

        public BlockState plain() {
            return switch (this) {
                case ONYX -> ModBlocks.ONYX_BRICKS.get().defaultBlockState();
                case MAGMA -> ModBlocks.MAGMA_BRICKS.get().defaultBlockState();
                case BLACKSTONE -> Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
            };
        }
    }

    public static final EnumProperty<Variant> VARIANT = EnumProperty.create("variant", Variant.class);

    public WardedBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(VARIANT, Variant.ONYX));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(VARIANT);
    }
}

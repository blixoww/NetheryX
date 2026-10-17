package com.netheryx.item;

import com.netheryx.block.CrustedLavaBlock;
import com.netheryx.config.NetheryXConfig;
import com.netheryx.registry.ModArmorMaterials;
import com.netheryx.registry.ModBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Semelles de magma : solidifient la lave sous les pas du porteur (le "Frost Walker" du Nether). */
public class MagmaTreadsItem extends ArmorItem {
    public MagmaTreadsItem(Properties properties) {
        super(ModArmorMaterials.MAGMA, Type.BOOTS, properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!level.isClientSide && entity instanceof LivingEntity living
                && living.getItemBySlot(EquipmentSlot.FEET) == stack && living.onGround()) {
            solidifyLava(living, level, living.blockPosition(), NetheryXConfig.MAGMA_TREADS_RADIUS.get());
        }
    }

    public static void solidifyLava(LivingEntity living, Level level, BlockPos origin, int radius) {
        BlockState crust = ModBlocks.CRUSTED_LAVA.get().defaultBlockState();
        BlockPos.MutableBlockPos above = new BlockPos.MutableBlockPos();
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-radius, -1, -radius), origin.offset(radius, -1, radius))) {
            if (!pos.closerToCenterThan(living.position(), radius)) {
                continue;
            }
            above.set(pos.getX(), pos.getY() + 1, pos.getZ());
            if (!level.getBlockState(above).isAir()) {
                continue;
            }
            BlockState current = level.getBlockState(pos);
            if (current.is(ModBlocks.CRUSTED_LAVA.get())) {
                // Le chemin reste solide tant que le porteur marche dessus.
                if (current.getValue(CrustedLavaBlock.AGE) > 0) {
                    level.setBlock(pos, crust, Block.UPDATE_CLIENTS);
                }
            } else if (current == CrustedLavaBlock.meltsInto() && level.isUnobstructed(crust, pos, CollisionContext.empty())) {
                level.setBlockAndUpdate(pos, crust);
                level.scheduleTick(pos, ModBlocks.CRUSTED_LAVA.get(), Mth.nextInt(living.getRandom(), 60, 120));
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.netheryx.magma_treads.tooltip").withStyle(ChatFormatting.GOLD));
    }
}

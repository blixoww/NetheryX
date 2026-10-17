package com.netheryx.registry;

import com.netheryx.NetheryX;
import com.netheryx.blockentity.OnyxFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, NetheryX.MOD_ID);

    @SuppressWarnings("DataFlowIssue")
    public static final RegistryObject<BlockEntityType<OnyxFurnaceBlockEntity>> ONYX_FURNACE = BLOCK_ENTITIES.register("onyx_furnace",
            () -> BlockEntityType.Builder.of(OnyxFurnaceBlockEntity::new, ModBlocks.ONYX_FURNACE.get()).build(null));

    private ModBlockEntities() {
    }
}

package com.netheryx.registry;

import com.netheryx.NetheryX;
import com.netheryx.blockentity.InfernalCauldronBlockEntity;
import com.netheryx.blockentity.LavaTrapBlockEntity;
import com.netheryx.blockentity.OnyxFurnaceBlockEntity;
import com.netheryx.blockentity.SoulAltarBlockEntity;
import com.netheryx.blockentity.WardLanternBlockEntity;
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

    @SuppressWarnings("DataFlowIssue")
    public static final RegistryObject<BlockEntityType<LavaTrapBlockEntity>> LAVA_TRAP = BLOCK_ENTITIES.register("lava_trap",
            () -> BlockEntityType.Builder.of(LavaTrapBlockEntity::new, ModBlocks.LAVA_TRAP.get()).build(null));
    @SuppressWarnings("DataFlowIssue")
    public static final RegistryObject<BlockEntityType<InfernalCauldronBlockEntity>> INFERNAL_CAULDRON = BLOCK_ENTITIES.register("infernal_cauldron",
            () -> BlockEntityType.Builder.of(InfernalCauldronBlockEntity::new, ModBlocks.INFERNAL_CAULDRON.get()).build(null));
    @SuppressWarnings("DataFlowIssue")
    public static final RegistryObject<BlockEntityType<SoulAltarBlockEntity>> SOUL_ALTAR = BLOCK_ENTITIES.register("soul_altar",
            () -> BlockEntityType.Builder.of(SoulAltarBlockEntity::new, ModBlocks.SOUL_ALTAR.get()).build(null));
    @SuppressWarnings("DataFlowIssue")
    public static final RegistryObject<BlockEntityType<WardLanternBlockEntity>> WARD_LANTERN = BLOCK_ENTITIES.register("ward_lantern",
            () -> BlockEntityType.Builder.of(WardLanternBlockEntity::new, ModBlocks.WARD_LANTERN.get()).build(null));

    private ModBlockEntities() {
    }
}

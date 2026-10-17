package com.netheryx.registry;

import com.netheryx.NetheryX;
import com.netheryx.worldgen.structure.NetherFloorStructure;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModStructureTypes {
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES = DeferredRegister.create(Registries.STRUCTURE_TYPE, NetheryX.MOD_ID);

    public static final RegistryObject<StructureType<NetherFloorStructure>> NETHER_FLOOR =
            STRUCTURE_TYPES.register("nether_floor", () -> () -> NetherFloorStructure.CODEC);

    private ModStructureTypes() {
    }
}

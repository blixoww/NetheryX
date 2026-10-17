package com.netheryx.registry;

import com.netheryx.NetheryX;
import com.netheryx.entity.AshWraith;
import com.netheryx.entity.BasaltBeetle;
import com.netheryx.entity.CinderHen;
import com.netheryx.entity.EmberCow;
import com.netheryx.entity.FireGhast;
import com.netheryx.entity.OnyxCube;
import com.netheryx.entity.InfernalWarlock;
import com.netheryx.entity.SoulMoth;
import com.netheryx.entity.WitheredChampion;
import com.netheryx.entity.WitheredKnight;
import com.netheryx.entity.EmberStalker;
import com.netheryx.entity.MagmaCrab;
import com.netheryx.entity.OnyxSentinel;
import com.netheryx.entity.SulfurBombEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, NetheryX.MOD_ID);

    public static final RegistryObject<EntityType<EmberStalker>> EMBER_STALKER = register("ember_stalker",
            EntityType.Builder.of(EmberStalker::new, MobCategory.MONSTER).sized(0.9F, 1.1F).fireImmune().clientTrackingRange(8));
    public static final RegistryObject<EntityType<AshWraith>> ASH_WRAITH = register("ash_wraith",
            EntityType.Builder.of(AshWraith::new, MobCategory.MONSTER).sized(0.7F, 1.8F).fireImmune().clientTrackingRange(8));
    public static final RegistryObject<EntityType<OnyxSentinel>> ONYX_SENTINEL = register("onyx_sentinel",
            EntityType.Builder.of(OnyxSentinel::new, MobCategory.MONSTER).sized(1.6F, 3.4F).fireImmune().clientTrackingRange(10));
    public static final RegistryObject<EntityType<MagmaCrab>> MAGMA_CRAB = register("magma_crab",
            EntityType.Builder.of(MagmaCrab::new, MobCategory.CREATURE).sized(0.9F, 0.6F).fireImmune().clientTrackingRange(8));
    public static final RegistryObject<EntityType<SoulMoth>> SOUL_MOTH = register("soul_moth",
            EntityType.Builder.of(SoulMoth::new, MobCategory.AMBIENT).sized(0.6F, 0.5F).fireImmune().clientTrackingRange(6));
    public static final RegistryObject<EntityType<WitheredKnight>> WITHERED_KNIGHT = register("withered_knight",
            EntityType.Builder.of(WitheredKnight::new, MobCategory.MONSTER).sized(0.7F, 2.2F).fireImmune().clientTrackingRange(8));
    public static final RegistryObject<EntityType<BasaltBeetle>> BASALT_BEETLE = register("basalt_beetle",
            EntityType.Builder.of(BasaltBeetle::new, MobCategory.CREATURE).sized(1.3F, 0.9F).fireImmune().clientTrackingRange(8));
    public static final RegistryObject<EntityType<InfernalWarlock>> INFERNAL_WARLOCK = register("infernal_warlock",
            EntityType.Builder.of(InfernalWarlock::new, MobCategory.MONSTER).sized(0.7F, 2.1F).fireImmune().clientTrackingRange(10));
    public static final RegistryObject<EntityType<WitheredChampion>> WITHERED_CHAMPION = register("withered_champion",
            EntityType.Builder.of(WitheredChampion::new, MobCategory.MONSTER).sized(1.0F, 3.1F).fireImmune().clientTrackingRange(10));
    public static final RegistryObject<EntityType<EmberCow>> EMBER_COW = register("ember_cow",
            EntityType.Builder.of(EmberCow::new, MobCategory.CREATURE).sized(0.9F, 1.4F).fireImmune().clientTrackingRange(10));
    public static final RegistryObject<EntityType<CinderHen>> CINDER_HEN = register("cinder_hen",
            EntityType.Builder.of(CinderHen::new, MobCategory.CREATURE).sized(0.4F, 0.7F).fireImmune().clientTrackingRange(10));
    public static final RegistryObject<EntityType<FireGhast>> FIRE_GHAST = register("fire_ghast",
            EntityType.Builder.of(FireGhast::new, MobCategory.MONSTER).sized(4.0F, 4.0F).fireImmune().clientTrackingRange(10));
    public static final RegistryObject<EntityType<OnyxCube>> ONYX_CUBE = register("onyx_cube",
            EntityType.Builder.of(OnyxCube::new, MobCategory.MONSTER).sized(0.52F, 0.52F).fireImmune().clientTrackingRange(8));
    public static final RegistryObject<EntityType<SulfurBombEntity>> SULFUR_BOMB = register("sulfur_bomb",
            EntityType.Builder.<SulfurBombEntity>of(SulfurBombEntity::new, MobCategory.MISC).sized(0.25F, 0.25F)
                    .clientTrackingRange(4).updateInterval(10));

    private ModEntities() {
    }

    private static <T extends Entity> RegistryObject<EntityType<T>> register(String name, EntityType.Builder<T> builder) {
        return ENTITY_TYPES.register(name, () -> builder.build(NetheryX.id(name).toString()));
    }
}

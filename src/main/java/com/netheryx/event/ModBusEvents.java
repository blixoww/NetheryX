package com.netheryx.event;

import com.netheryx.NetheryX;
import com.netheryx.entity.AshWraith;
import com.netheryx.entity.BasaltBurrower;
import com.netheryx.entity.CinderLizard;
import com.netheryx.entity.LanternBearer;
import com.netheryx.entity.Magmafish;
import com.netheryx.entity.Mimic;
import com.netheryx.entity.NetherHeart;
import com.netheryx.entity.AshWyvern;
import com.netheryx.entity.BasaltGolem;
import com.netheryx.entity.BlazePig;
import com.netheryx.entity.CrystalSnail;
import com.netheryx.entity.Firefly;
import com.netheryx.entity.PiglinMerchant;
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
import com.netheryx.registry.ModEntities;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Evenements du bus du mod (cycle de vie / inscriptions). */
@Mod.EventBusSubscriber(modid = NetheryX.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModBusEvents {
    private ModBusEvents() {
    }

    @SubscribeEvent
    public static void onAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(ModEntities.EMBER_STALKER.get(), EmberStalker.createAttributes().build());
        event.put(ModEntities.ASH_WRAITH.get(), AshWraith.createAttributes().build());
        event.put(ModEntities.ONYX_SENTINEL.get(), OnyxSentinel.createAttributes().build());
        event.put(ModEntities.MAGMA_CRAB.get(), MagmaCrab.createAttributes().build());
        event.put(ModEntities.SOUL_MOTH.get(), SoulMoth.createAttributes().build());
        event.put(ModEntities.WITHERED_KNIGHT.get(), WitheredKnight.createAttributes().build());
        event.put(ModEntities.BASALT_BEETLE.get(), BasaltBeetle.createAttributes().build());
        event.put(ModEntities.INFERNAL_WARLOCK.get(), InfernalWarlock.createAttributes().build());
        event.put(ModEntities.WITHERED_CHAMPION.get(), WitheredChampion.createAttributes().build());
        event.put(ModEntities.EMBER_COW.get(), EmberCow.createAttributes().build());
        event.put(ModEntities.CINDER_HEN.get(), CinderHen.createAttributes().build());
        event.put(ModEntities.FIRE_GHAST.get(), FireGhast.createAttributes().build());
        event.put(ModEntities.ONYX_CUBE.get(), OnyxCube.createAttributes().build());
        event.put(ModEntities.MAGMAFISH.get(), Magmafish.createAttributes().build());
        event.put(ModEntities.PIGLIN_MERCHANT.get(), PiglinMerchant.createAttributes().build());
        event.put(ModEntities.MIMIC.get(), Mimic.createAttributes().build());
        event.put(ModEntities.BASALT_BURROWER.get(), BasaltBurrower.createAttributes().build());
        event.put(ModEntities.LANTERN_BEARER.get(), LanternBearer.createAttributes().build());
        event.put(ModEntities.CINDER_LIZARD.get(), CinderLizard.createAttributes().build());
        event.put(ModEntities.NETHER_HEART.get(), NetherHeart.createAttributes().build());
        event.put(ModEntities.BASALT_GOLEM.get(), BasaltGolem.createAttributes().build());
        event.put(ModEntities.ASH_WYVERN.get(), AshWyvern.createAttributes().build());
        event.put(ModEntities.EMBER_FIREFLY.get(), Firefly.createAttributes().build());
        event.put(ModEntities.SOUL_FIREFLY.get(), Firefly.createAttributes().build());
        event.put(ModEntities.BLAZE_PIG.get(), BlazePig.createAttributes().build());
        event.put(ModEntities.CRYSTAL_SNAIL.get(), CrystalSnail.createAttributes().build());
    }

    @SubscribeEvent
    public static void onSpawnPlacementRegister(SpawnPlacementRegisterEvent event) {
        event.register(ModEntities.EMBER_STALKER.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkAnyLightMonsterSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(ModEntities.ASH_WRAITH.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkAnyLightMonsterSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(ModEntities.ONYX_SENTINEL.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkAnyLightMonsterSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(ModEntities.MAGMA_CRAB.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                MagmaCrab::checkCrabSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(ModEntities.SOUL_MOTH.get(), SpawnPlacements.Type.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Mob::checkMobSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(ModEntities.WITHERED_KNIGHT.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkAnyLightMonsterSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(ModEntities.BASALT_BEETLE.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                BasaltBeetle::checkBeetleSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(ModEntities.EMBER_COW.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                EmberCow::checkNetherAnimalSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(ModEntities.CINDER_HEN.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                EmberCow::checkNetherAnimalSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(ModEntities.FIRE_GHAST.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, reason, pos, random) -> level.getDifficulty() != Difficulty.PEACEFUL && random.nextInt(2) == 0
                        && Mob.checkMobSpawnRules(type, level, reason, pos, random),
                SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(ModEntities.ONYX_CUBE.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, reason, pos, random) -> level.getDifficulty() != Difficulty.PEACEFUL,
                SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(ModEntities.MAGMAFISH.get(), SpawnPlacements.Type.IN_LAVA, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Magmafish::checkMagmafishSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(ModEntities.BASALT_BURROWER.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkAnyLightMonsterSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(ModEntities.LANTERN_BEARER.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, reason, pos, random) -> level.getBlockState(pos.below()).isSolid(),
                SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(ModEntities.CINDER_LIZARD.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, reason, pos, random) -> level.getBlockState(pos.below()).isSolid(),
                SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(ModEntities.INFERNAL_WARLOCK.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkAnyLightMonsterSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(ModEntities.BASALT_GOLEM.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkAnyLightMonsterSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(ModEntities.ASH_WYVERN.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, reason, pos, random) -> level.getBlockState(pos.below()).isSolid(),
                SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(ModEntities.EMBER_FIREFLY.get(), SpawnPlacements.Type.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Mob::checkMobSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(ModEntities.SOUL_FIREFLY.get(), SpawnPlacements.Type.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Mob::checkMobSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(ModEntities.BLAZE_PIG.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, reason, pos, random) -> level.getBlockState(pos.below()).isSolid(),
                SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(ModEntities.CRYSTAL_SNAIL.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, reason, pos, random) -> level.getBlockState(pos.below()).isSolid(),
                SpawnPlacementRegisterEvent.Operation.REPLACE);
    }
}

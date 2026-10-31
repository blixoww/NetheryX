package com.netheryx.gametest;

import com.netheryx.NetheryX;
import com.netheryx.registry.ModFeatures;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.Optional;

/**
 * Tests en jeu ({@code ./gradlew runGameTestServer}) : gabarits des donjons et de leurs indices, structures suspendues
 * sous la voute.
 */
@GameTestHolder(NetheryX.MOD_ID)
@PrefixGameTestTemplate(false)
public final class NetheryXGameTests {
    private static final String AREA = "gametest/column";
    private static final String[] DUNGEONS = {"onyx_citadel", "infernal_sanctum", "withered_catacombs"};
    private static final String[] VARIANTS = {"main", "variant_1", "variant_2", "variant_3"};
    private static final String[] APPROACH = {"ruin", "remains", "signpost"};

    private NetheryXGameTests() {
    }

    /** Chaque variante de donjon et chaque indice d'approche existe et n'est pas vide. */
    @GameTest(template = AREA)
    public static void dungeonTemplatesLoad(GameTestHelper helper) {
        StructureTemplateManager manager = helper.getLevel().getStructureManager();
        for (String dungeon : DUNGEONS) {
            for (String variant : VARIANTS) {
                requireTemplate(helper, manager, dungeon + "/" + variant);
            }
            for (String kind : APPROACH) {
                requireTemplate(helper, manager, dungeon + "_approach/" + kind);
            }
        }
        helper.succeed();
    }

    private static void requireTemplate(GameTestHelper helper, StructureTemplateManager manager, String path) {
        Optional<StructureTemplate> template = manager.get(NetheryX.id(path));
        if (template.isEmpty() || template.get().getSize().getX() == 0) {
            helper.fail("Gabarit manquant ou vide : " + NetheryX.id(path));
        }
    }

    /** Sous un plafond, au-dessus d'un grand vide, la feature pose un nid ou une cage accroche a la voute. */
    @GameTest(template = AREA)
    public static void ceilingHangPlaces(GameTestHelper helper) {
        for (int x = 0; x < 7; x++) {
            for (int z = 0; z < 7; z++) {
                helper.setBlock(x, 25, z, Blocks.BLACKSTONE);
            }
        }
        ServerLevel level = helper.getLevel();
        BlockPos origin = helper.absolutePos(new BlockPos(3, 24, 3));
        boolean placed = ModFeatures.CEILING_HANG.get().place(new FeaturePlaceContext<>(Optional.empty(), level,
                level.getChunkSource().getGenerator(), RandomSource.create(1234L), origin, NoneFeatureConfiguration.INSTANCE));
        if (!placed || level.getBlockState(origin).isAir() && level.getBlockState(origin.below()).isAir()) {
            helper.fail("Rien n'a ete accroche sous la voute");
        }
        helper.succeed();
    }
}

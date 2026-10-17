package com.netheryx.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.netheryx.NetheryX;
import com.netheryx.registry.ModBlocks;
import com.netheryx.util.ModTags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

/**
 * Cote client de la Lanterne du Nether : memorise les minerais trouves et dessine leurs contours a travers les blocs
 * (sans test de profondeur) jusqu'a l'expiration de l'effet.
 */
@Mod.EventBusSubscriber(modid = NetheryX.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class OreRevealRenderer {
    private static final int MAX_ORES = 400;
    private static final List<Revealed> REVEALED = new ArrayList<>();
    private static int remainingTicks;

    private OreRevealRenderer() {
    }

    private record Revealed(BlockPos pos, float r, float g, float b) {
    }

    public static void reveal(Level level, BlockPos center, int radius, int duration) {
        REVEALED.clear();
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -radius, -radius), center.offset(radius, radius, radius))) {
            BlockState state = level.getBlockState(pos);
            if (state.is(Tags.Blocks.ORES) || state.is(ModTags.Blocks.LANTERN_REVEALS)) {
                float[] color = colorOf(state);
                REVEALED.add(new Revealed(pos.immutable(), color[0], color[1], color[2]));
                if (REVEALED.size() >= MAX_ORES) {
                    break;
                }
            }
        }
        remainingTicks = duration;
    }

    private static float[] colorOf(BlockState state) {
        if (state.is(Blocks.ANCIENT_DEBRIS)) {
            return new float[]{0.85F, 0.35F, 0.2F};
        }
        if (state.is(Blocks.NETHER_GOLD_ORE) || state.is(Blocks.GILDED_BLACKSTONE) || state.is(Tags.Blocks.ORES_GOLD)) {
            return new float[]{1.0F, 0.85F, 0.2F};
        }
        if (state.is(ModBlocks.ONYX_ORE.get())) {
            return new float[]{0.75F, 0.45F, 1.0F};
        }
        if (state.is(ModBlocks.SULFUR_ORE.get())) {
            return new float[]{1.0F, 1.0F, 0.35F};
        }
        if (state.is(Tags.Blocks.ORES_QUARTZ)) {
            return new float[]{1.0F, 1.0F, 1.0F};
        }
        return new float[]{0.3F, 1.0F, 0.75F};
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && remainingTicks > 0 && --remainingTicks == 0) {
            REVEALED.clear();
        }
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS || REVEALED.isEmpty()) {
            return;
        }
        Vec3 camera = event.getCamera().getPosition();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer lines = buffers.getBuffer(XrayLines.TYPE);
        // Pulsation douce, plus transparente a la fin de l'effet
        float alpha = Math.min(1.0F, remainingTicks / 40.0F) * (0.7F + 0.3F * (float) Math.sin(remainingTicks * 0.3D));
        poseStack.pushPose();
        poseStack.translate(-camera.x, -camera.y, -camera.z);
        for (Revealed ore : REVEALED) {
            LevelRenderer.renderLineBox(poseStack, lines, new AABB(ore.pos()).deflate(0.02D), ore.r(), ore.g(), ore.b(), alpha);
        }
        poseStack.popPose();
        buffers.endBatch(XrayLines.TYPE);
    }

    /** Type de rendu « lignes » sans test de profondeur : les contours restent visibles a travers la roche. */
    private static final class XrayLines extends RenderType {
        static final RenderType TYPE = RenderType.create(NetheryX.MOD_ID + "_xray_lines", DefaultVertexFormat.POSITION_COLOR_NORMAL,
                VertexFormat.Mode.LINES, 1024, false, false,
                RenderType.CompositeState.builder()
                        .setShaderState(RENDERTYPE_LINES_SHADER)
                        .setLineState(new LineStateShard(OptionalDouble.of(2.5D)))
                        .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setOutputState(ITEM_ENTITY_TARGET)
                        .setWriteMaskState(COLOR_WRITE)
                        .setCullState(NO_CULL)
                        .setDepthTestState(NO_DEPTH_TEST)
                        .createCompositeState(false));

        private XrayLines(String name, VertexFormat format, VertexFormat.Mode mode, int size, boolean affectsCrumbling,
                          boolean sortOnUpload, Runnable setup, Runnable clear) {
            super(name, format, mode, size, affectsCrumbling, sortOnUpload, setup, clear);
        }
    }
}

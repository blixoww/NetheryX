package com.netheryx.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.netheryx.blockentity.SoulAltarBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Objet flottant et tournoyant au-dessus de l'Autel des ames. */
public class SoulAltarRenderer implements BlockEntityRenderer<SoulAltarBlockEntity> {
    public SoulAltarRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(SoulAltarBlockEntity altar, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        ItemStack stack = altar.getItem();
        if (stack.isEmpty() || altar.getLevel() == null) {
            return;
        }
        float time = altar.getLevel().getGameTime() + partialTick;
        poseStack.pushPose();
        poseStack.translate(0.5D, 1.15D + Mth.sin(time * 0.08F) * 0.06D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(altar.spin + time * 2.0F));
        poseStack.scale(0.6F, 0.6F, 0.6F);
        Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.GROUND, 0xF000F0, OverlayTexture.NO_OVERLAY,
                poseStack, buffer, altar.getLevel(), 0);
        poseStack.popPose();
    }
}

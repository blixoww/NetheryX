package com.netheryx.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.netheryx.NetheryX;
import com.netheryx.entity.ObsidianBoat;
import net.minecraft.client.model.BoatModel;
import net.minecraft.client.model.ListModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.vehicle.Boat;

/** Rendu du bateau d'obsidienne : modele du bateau vanilla, texture propre, coque rehaussee sur la lave. */
public class ObsidianBoatRenderer extends BoatRenderer {
    private static final ResourceLocation TEXTURE = NetheryX.id("textures/entity/obsidian_boat.png");
    private final Pair<ResourceLocation, ListModel<Boat>> model;

    public ObsidianBoatRenderer(EntityRendererProvider.Context context) {
        super(context, false);
        this.model = Pair.of(TEXTURE, new BoatModel(context.bakeLayer(ModelLayers.createBoatModelName(Boat.Type.OAK))));
    }

    @Override
    public void render(Boat boat, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int light) {
        float lift = boat instanceof ObsidianBoat obsidian ? obsidian.getLavaLift(partialTick) : 0.0F;
        poseStack.pushPose();
        poseStack.translate(0.0D, lift, 0.0D);
        super.render(boat, yaw, partialTick, poseStack, buffer, light);
        poseStack.popPose();
    }

    @Override
    public Pair<ResourceLocation, ListModel<Boat>> getModelWithLocation(Boat boat) {
        return this.model;
    }
}

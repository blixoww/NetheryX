package com.netheryx.client.renderer;

import com.netheryx.NetheryX;
import com.netheryx.entity.CrystalSnail;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

/** Modele de l'Escargot de cristal : les cristaux de la coquille disparaissent une fois tondus. */
public class CrystalSnailModel extends DefaultedEntityGeoModel<CrystalSnail> {
    public CrystalSnailModel() {
        super(NetheryX.id("crystal_snail"), true);
    }

    @Override
    public void setCustomAnimations(CrystalSnail animatable, long instanceId, AnimationState<CrystalSnail> animationState) {
        super.setCustomAnimations(animatable, instanceId, animationState);
        CoreGeoBone crystals = this.getAnimationProcessor().getBone("crystals");
        if (crystals != null) {
            crystals.setHidden(!animatable.hasCrystals());
        }
    }
}

package com.netheryx.client.renderer;

import com.netheryx.NetheryX;
import com.netheryx.entity.CinderLizard;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

/** Modele du Lezard de cendre : la selle n'est visible que s'il est selle. */
public class CinderLizardModel extends DefaultedEntityGeoModel<CinderLizard> {
    public CinderLizardModel() {
        super(NetheryX.id("cinder_lizard"), true);
    }

    @Override
    public void setCustomAnimations(CinderLizard animatable, long instanceId, AnimationState<CinderLizard> animationState) {
        super.setCustomAnimations(animatable, instanceId, animationState);
        CoreGeoBone saddle = this.getAnimationProcessor().getBone("saddle");
        if (saddle != null) {
            saddle.setHidden(!animatable.isSaddled());
        }
    }
}

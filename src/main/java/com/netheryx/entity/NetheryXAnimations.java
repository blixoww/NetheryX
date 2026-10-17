package com.netheryx.entity;

import software.bernie.geckolib.core.animation.RawAnimation;

/**
 * Fabrique d'animations GeckoLib. Les noms suivent la convention
 * {@code animation.<entite>.<nom>} des fichiers {@code assets/netheryx/animations/entity/*.animation.json}.
 */
public final class NetheryXAnimations {
    private NetheryXAnimations() {
    }

    public static RawAnimation loop(String entity, String name) {
        return RawAnimation.begin().thenLoop(path(entity, name));
    }

    public static RawAnimation once(String entity, String name) {
        return RawAnimation.begin().thenPlay(path(entity, name));
    }

    private static String path(String entity, String name) {
        return "animation." + entity + "." + name;
    }
}

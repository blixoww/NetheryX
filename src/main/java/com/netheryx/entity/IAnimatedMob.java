package com.netheryx.entity;

/** Etat d'animation transmis au modele : vol (battements d'ailes), action en cours (attaque, gueule, couvercle). */
public interface IAnimatedMob {
    boolean isFlyingAnimation();

    /** 0 au repos, jusqu'a 1 au plus fort d'une action (attaque, souffle, ouverture). */
    float getActionProgress();
}

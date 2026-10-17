package com.netheryx.registry;

import com.netheryx.NetheryX;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Particules du mod. Chaque type a son comportement (voir {@code NetheryXParticle}) et sa texture animee ;
 * elles sont utilisees par les biomes (particules d'ambiance), les blocs et les creatures.
 */
public final class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, NetheryX.MOD_ID);

    /** Etincelle de braise : monte en tournoyant puis s'eteint. */
    public static final RegistryObject<SimpleParticleType> EMBER_SPARK = register("ember_spark");
    /** Flocon de cendre : tombe lentement en se balancant. */
    public static final RegistryObject<SimpleParticleType> ASH_FLAKE = register("ash_flake");
    /** Feu follet d'ame : flotte vers le haut en ondulant, lumineux. */
    public static final RegistryObject<SimpleParticleType> SOUL_WISP = register("soul_wisp");
    /** Reflet d'onyx : scintille sur place. */
    public static final RegistryObject<SimpleParticleType> ONYX_GLINT = register("onyx_glint");
    /** Bouffee de soufre : petite fumee jaune qui gonfle en montant. */
    public static final RegistryObject<SimpleParticleType> SULFUR_PUFF = register("sulfur_puff");
    /** Pollen de braise : derive doucement dans l'air des prairies. */
    public static final RegistryObject<SimpleParticleType> EMBER_POLLEN = register("ember_pollen");
    /** Spore fletrie : descend en spirale dans la foret fletrie. */
    public static final RegistryObject<SimpleParticleType> WITHER_SPORE = register("wither_spore");

    private ModParticles() {
    }

    private static RegistryObject<SimpleParticleType> register(String name) {
        return PARTICLES.register(name, () -> new SimpleParticleType(false));
    }
}

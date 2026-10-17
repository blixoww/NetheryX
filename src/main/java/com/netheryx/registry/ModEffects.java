package com.netheryx.registry;

import com.netheryx.NetheryX;
import com.netheryx.effect.LavaVisionEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Effets et potions du mod. */
public final class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, NetheryX.MOD_ID);
    public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(ForgeRegistries.POTIONS, NetheryX.MOD_ID);

    public static final RegistryObject<MobEffect> LAVA_VISION = EFFECTS.register("lava_vision", LavaVisionEffect::new);

    /** Potion de plongee en lave : vision de lave et resistance au feu. */
    public static final RegistryObject<Potion> LAVA_DIVING = POTIONS.register("lava_diving",
            () -> new Potion("lava_diving", new MobEffectInstance(LAVA_VISION.get(), 3600),
                    new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 3600)));
    public static final RegistryObject<Potion> LONG_LAVA_DIVING = POTIONS.register("long_lava_diving",
            () -> new Potion("lava_diving", new MobEffectInstance(LAVA_VISION.get(), 9600),
                    new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 9600)));

    private ModEffects() {
    }
}

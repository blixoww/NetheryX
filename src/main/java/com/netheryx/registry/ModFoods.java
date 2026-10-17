package com.netheryx.registry;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;

public final class ModFoods {
    public static final FoodProperties FIRE_BERRIES = new FoodProperties.Builder()
            .nutrition(2).saturationMod(0.2F).fast()
            .effect(() -> new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 100), 1.0F)
            .build();
    public static final FoodProperties BLAZE_PEPPER = new FoodProperties.Builder()
            .nutrition(3).saturationMod(0.4F)
            .effect(() -> new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 600), 1.0F)
            .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200), 0.5F)
            .build();
    public static final FoodProperties SOUL_TUBER = new FoodProperties.Builder()
            .nutrition(1).saturationMod(0.3F).build();
    public static final FoodProperties BAKED_SOUL_TUBER = new FoodProperties.Builder()
            .nutrition(6).saturationMod(0.7F).build();
    public static final FoodProperties STALKER_MEAT = new FoodProperties.Builder()
            .nutrition(3).saturationMod(0.3F).meat().build();
    public static final FoodProperties COOKED_STALKER_MEAT = new FoodProperties.Builder()
            .nutrition(8).saturationMod(0.9F).meat()
            .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, 300), 0.3F)
            .build();
    public static final FoodProperties CRAB_LEG = new FoodProperties.Builder()
            .nutrition(2).saturationMod(0.2F).meat().build();
    public static final FoodProperties COOKED_CRAB_LEG = new FoodProperties.Builder()
            .nutrition(6).saturationMod(0.8F).meat().build();
    public static final FoodProperties INFERNAL_STEW = new FoodProperties.Builder()
            .nutrition(10).saturationMod(1.0F)
            .effect(() -> new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 1200), 1.0F)
            .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 100), 1.0F)
            .build();

    public static final FoodProperties CINDER_WHEAT = new FoodProperties.Builder()
            .nutrition(1).saturationMod(0.2F).build();
    public static final FoodProperties CINDER_BREAD = new FoodProperties.Builder()
            .nutrition(6).saturationMod(0.7F)
            .effect(() -> new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 200), 0.5F)
            .build();
    public static final FoodProperties GLOWCAP = new FoodProperties.Builder()
            .nutrition(2).saturationMod(0.3F).fast()
            .effect(() -> new MobEffectInstance(MobEffects.NIGHT_VISION, 300), 1.0F)
            .build();
    public static final FoodProperties GLOWCAP_STEW = new FoodProperties.Builder()
            .nutrition(8).saturationMod(0.8F)
            .effect(() -> new MobEffectInstance(MobEffects.NIGHT_VISION, 1800), 1.0F)
            .build();
    public static final FoodProperties INFERNAL_BEEF = new FoodProperties.Builder()
            .nutrition(4).saturationMod(0.4F).meat().build();
    public static final FoodProperties COOKED_INFERNAL_BEEF = new FoodProperties.Builder()
            .nutrition(10).saturationMod(1.0F).meat()
            .effect(() -> new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 400), 1.0F)
            .build();
    public static final FoodProperties INFERNAL_CHICKEN = new FoodProperties.Builder()
            .nutrition(2).saturationMod(0.3F).meat().build();
    public static final FoodProperties COOKED_INFERNAL_CHICKEN = new FoodProperties.Builder()
            .nutrition(7).saturationMod(0.8F).meat().build();
    public static final FoodProperties FRIED_EMBER_EGG = new FoodProperties.Builder()
            .nutrition(5).saturationMod(0.6F)
            .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 300), 0.4F)
            .build();

    private ModFoods() {
    }
}

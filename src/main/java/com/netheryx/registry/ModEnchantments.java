package com.netheryx.registry;

import com.netheryx.NetheryX;
import com.netheryx.enchantment.NetherEnchantment;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Enchantements du Nether, conferes par l'Autel des ames. */
public final class ModEnchantments {
    public static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, NetheryX.MOD_ID);

    /** Torse et jambes. */
    public static final EnchantmentCategory CHEST_OR_LEGS = EnchantmentCategory.create("netheryx_chest_or_legs",
            (Item item) -> item instanceof ArmorItem armor
                    && (armor.getType() == ArmorItem.Type.CHESTPLATE || armor.getType() == ArmorItem.Type.LEGGINGS));

    /** Pas de braise (bottes) : plus rapide sur les sols du Nether, insensible aux blocs de magma. */
    public static final RegistryObject<Enchantment> EMBER_STRIDE = ENCHANTMENTS.register("ember_stride",
            () -> new NetherEnchantment(Enchantment.Rarity.RARE, EnchantmentCategory.ARMOR_FEET, 2, EquipmentSlot.FEET));
    /** Recolte d'ames (armes) : les creatures tuees lachent plus d'experience. */
    public static final RegistryObject<Enchantment> SOUL_HARVEST = ENCHANTMENTS.register("soul_harvest",
            () -> new NetherEnchantment(Enchantment.Rarity.RARE, EnchantmentCategory.WEAPON, 3, EquipmentSlot.MAINHAND));
    /** Ancrage (plastron, jambieres) : reduit fortement le recul subi. */
    public static final RegistryObject<Enchantment> ANCHORING = ENCHANTMENTS.register("anchoring",
            () -> new NetherEnchantment(Enchantment.Rarity.RARE, CHEST_OR_LEGS, 2, EquipmentSlot.CHEST, EquipmentSlot.LEGS));

    private ModEnchantments() {
    }
}

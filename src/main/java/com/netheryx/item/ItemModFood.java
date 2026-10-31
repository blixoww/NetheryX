package com.netheryx.item;

import com.netheryx.NetheryX;
import com.netheryx.NetheryXTab;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

/** Nourriture du mod : valeurs nutritives, effets eventuels, bol rendu (ragouts) ou fiole rendue (elixirs). */
public class ItemModFood extends ItemFood {
    private final List<PotionEffect> effects = new ArrayList<PotionEffect>();
    private final List<Float> chances = new ArrayList<Float>();
    private boolean bowl;
    private boolean drink;
    private int useTicks = 32;

    public ItemModFood(String name, int nutrition, float saturation, boolean meat) {
        super(nutrition, saturation, meat);
        setUnlocalizedName(NetheryX.MOD_ID + "." + name);
        setTextureName(NetheryX.id(name));
        setCreativeTab(NetheryXTab.TAB);
    }

    public ItemModFood effect(int potion, int duration, int amplifier, float chance) {
        this.effects.add(new PotionEffect(potion, duration, amplifier));
        this.chances.add(chance);
        return this;
    }

    public ItemModFood bowl() {
        this.bowl = true;
        setMaxStackSize(1);
        return this;
    }

    /** Se boit (fiole rendue), se consomme meme rassasie. */
    public ItemModFood drink() {
        this.drink = true;
        setAlwaysEdible();
        setMaxStackSize(16);
        return this;
    }

    public ItemModFood fast() {
        this.useTicks = 16;
        return this;
    }

    public ItemModFood always() {
        setAlwaysEdible();
        return this;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return this.useTicks;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return this.drink ? EnumAction.drink : EnumAction.eat;
    }

    @Override
    protected void onFoodEaten(ItemStack stack, World world, EntityPlayer player) {
        if (world.isRemote) {
            return;
        }
        for (int i = 0; i < this.effects.size(); i++) {
            if (world.rand.nextFloat() < this.chances.get(i)) {
                player.addPotionEffect(new PotionEffect(this.effects.get(i)));
            }
        }
    }

    @Override
    public ItemStack onEaten(ItemStack stack, World world, EntityPlayer player) {
        ItemStack result = super.onEaten(stack, world, player);
        if (!this.bowl && !this.drink) {
            return result;
        }
        ItemStack container = new ItemStack(this.bowl ? Items.bowl : Items.glass_bottle);
        if (result.stackSize <= 0) {
            return container;
        }
        if (!player.inventory.addItemStackToInventory(container)) {
            player.dropPlayerItemWithRandomChoice(container, false);
        }
        return result;
    }
}

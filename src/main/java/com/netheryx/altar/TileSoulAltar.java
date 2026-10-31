package com.netheryx.altar;

import com.netheryx.registry.ModEnchantments;
import com.netheryx.registry.ModItems;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;

import java.util.Map;

/**
 * Autel des ames, gere comme une machine : un emplacement pour l'objet a enchanter, un pour l'offrande. Quand tout est
 * pret, le bouton « Infuser » lance un rituel de 3 secondes ; a la fin, l'offrande et l'experience sont consommees et
 * l'enchantement est applique (ou monte d'un niveau). Si quelque chose change pendant le rituel, rien n'est consomme.
 */
public class TileSoulAltar extends TileEntity implements IInventory {
    public static final int SLOT_ITEM = 0;
    public static final int SLOT_OFFERING = 1;
    public static final int REQUIRED_FLAMES = 2;
    public static final int LEVELS_PER_ENCHANT_LEVEL = 5;
    public static final int RITUAL_TICKS = 60;

    /** Offrande -> enchantement confere, quantite consommee et cle des objets acceptes. */
    public static final class Offering {
        public final Item item;
        public final Enchantment enchantment;
        public final int count;
        public final String targets;

        Offering(Item item, Enchantment enchantment, int count, String targets) {
            this.item = item;
            this.enchantment = enchantment;
            this.count = count;
            this.targets = targets;
        }
    }

    private static Offering[] offerings;

    public static Offering[] offerings() {
        if (offerings == null) {
            offerings = new Offering[]{
                    new Offering(ModItems.magmaShell, ModEnchantments.emberStride, 2, "boots"),
                    new Offering(ModItems.soulDust, ModEnchantments.soulHarvest, 4, "weapons"),
                    new Offering(ModItems.basaltCarapace, ModEnchantments.anchoring, 2, "chest_legs")};
        }
        return offerings;
    }

    public enum Status {
        NO_ITEM, NO_OFFERING, UNKNOWN_OFFERING, MAX_LEVEL, INCOMPATIBLE, NOT_ENOUGH_OFFERING, NO_FLAMES, NO_EXPERIENCE, READY, IN_PROGRESS
    }

    private final ItemStack[] slots = new ItemStack[2];
    private int progress;
    private String ritualist;

    public int getProgress() {
        return this.progress;
    }

    /** Cote client : progression recue du conteneur. */
    public void setProgress(int progress) {
        this.progress = progress;
    }

    public static int offeringIndex(ItemStack stack) {
        if (stack == null) {
            return -1;
        }
        Offering[] all = offerings();
        for (int i = 0; i < all.length; i++) {
            if (all[i].enchantment != null && stack.getItem() == all[i].item) {
                return i;
            }
        }
        return -1;
    }

    public static boolean acceptsItem(ItemStack stack) {
        return stack.getItem() == Items.book || stack.getItem() == Items.enchanted_book || stack.getItem().getItemEnchantability() > 0
                || stack.isItemEnchanted();
    }

    /** Niveau d'enchantement que l'infusion donnerait (0 sans offrande reconnue). */
    @SuppressWarnings("unchecked")
    public int resultLevel() {
        int index = offeringIndex(this.slots[SLOT_OFFERING]);
        if (index < 0 || this.slots[SLOT_ITEM] == null) {
            return 0;
        }
        // getEnchantments lit aussi les enchantements stockes d'un livre
        Integer current = ((Map<Integer, Integer>) EnchantmentHelper.getEnchantments(this.slots[SLOT_ITEM]))
                .get(offerings()[index].enchantment.effectId);
        return (current == null ? 0 : current) + 1;
    }

    public int flames() {
        return this.worldObj == null ? 0 : BlockSoulAltar.countFlames(this.worldObj, this.xCoord, this.yCoord, this.zCoord);
    }

    /** Verifie tout ce qui manque, dans l'ordre ou le joueur doit s'en occuper. */
    @SuppressWarnings("unchecked")
    public Status check(EntityPlayer player) {
        if (this.progress > 0) {
            return Status.IN_PROGRESS;
        }
        ItemStack item = this.slots[SLOT_ITEM];
        ItemStack offeringStack = this.slots[SLOT_OFFERING];
        if (item == null) {
            return Status.NO_ITEM;
        }
        if (offeringStack == null) {
            return Status.NO_OFFERING;
        }
        int index = offeringIndex(offeringStack);
        if (index < 0) {
            return Status.UNKNOWN_OFFERING;
        }
        Offering offering = offerings()[index];
        Enchantment enchantment = offering.enchantment;
        boolean book = item.getItem() == Items.book || item.getItem() == Items.enchanted_book;
        int level = resultLevel();
        boolean creative = player != null && player.capabilities.isCreativeMode;
        if (level > enchantment.getMaxLevel()) {
            return Status.MAX_LEVEL;
        }
        if (!book && !enchantment.canApply(item)) {
            return Status.INCOMPATIBLE;
        }
        Map<Integer, Integer> current = EnchantmentHelper.getEnchantments(item);
        for (Integer other : current.keySet()) {
            Enchantment existing = Enchantment.enchantmentsList[other];
            if (existing != null && existing != enchantment && (!existing.canApplyTogether(enchantment) || !enchantment.canApplyTogether(existing))) {
                return Status.INCOMPATIBLE;
            }
        }
        if (offeringStack.stackSize < offering.count && !creative) {
            return Status.NOT_ENOUGH_OFFERING;
        }
        if (flames() < REQUIRED_FLAMES) {
            return Status.NO_FLAMES;
        }
        if (player != null && player.experienceLevel < level * LEVELS_PER_ENCHANT_LEVEL && !creative) {
            return Status.NO_EXPERIENCE;
        }
        return Status.READY;
    }

    /** Bouton « Infuser » : lance le rituel si tout est pret. */
    public boolean startRitual(EntityPlayer player) {
        if (check(player) != Status.READY) {
            return false;
        }
        this.progress = 1;
        this.ritualist = player.getCommandSenderName();
        this.worldObj.playSoundEffect(this.xCoord + 0.5D, this.yCoord + 0.5D, this.zCoord + 0.5D, "portal.trigger", 0.4F, 1.6F);
        markDirty();
        return true;
    }

    @Override
    public void updateEntity() {
        if (this.worldObj.isRemote || this.progress <= 0) {
            return;
        }
        this.progress++;
        if (this.progress % 20 == 0) {
            this.worldObj.playSoundEffect(this.xCoord + 0.5D, this.yCoord + 0.5D, this.zCoord + 0.5D, "mob.blaze.breathe", 0.6F,
                    0.6F + this.progress * 0.01F);
        }
        if (this.progress < RITUAL_TICKS) {
            return;
        }
        EntityPlayer player = this.ritualist == null ? null : this.worldObj.getPlayerEntityByName(this.ritualist);
        this.progress = 0;
        this.ritualist = null;
        if (player == null || check(player) != Status.READY) {
            if (player != null) {
                player.addChatMessage(new ChatComponentTranslation("screen.netheryx.soul_altar.failed"));
            }
            this.worldObj.playSoundEffect(this.xCoord + 0.5D, this.yCoord + 0.5D, this.zCoord + 0.5D, "random.fizz", 0.8F, 0.8F);
            markDirty();
            return;
        }
        infuse(player);
    }

    @SuppressWarnings("unchecked")
    private void infuse(EntityPlayer player) {
        int index = offeringIndex(this.slots[SLOT_OFFERING]);
        Offering offering = offerings()[index];
        int level = resultLevel();
        ItemStack item = this.slots[SLOT_ITEM];
        if (item.getItem() == Items.book) {
            ItemStack book = new ItemStack(Items.enchanted_book);
            Items.enchanted_book.addEnchantment(book, new net.minecraft.enchantment.EnchantmentData(offering.enchantment, level));
            this.slots[SLOT_ITEM] = book;
        } else if (item.getItem() == Items.enchanted_book) {
            Map<Integer, Integer> map = EnchantmentHelper.getEnchantments(item);
            map.put(offering.enchantment.effectId, level);
            // Les enchantements d'un livre sont dans « StoredEnchantments »
            NBTTagList stored = new NBTTagList();
            for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
                NBTTagCompound e = new NBTTagCompound();
                e.setShort("id", entry.getKey().shortValue());
                e.setShort("lvl", entry.getValue().shortValue());
                stored.appendTag(e);
            }
            item.getTagCompound().setTag("StoredEnchantments", stored);
        } else {
            Map<Integer, Integer> map = EnchantmentHelper.getEnchantments(item);
            map.put(offering.enchantment.effectId, level);
            EnchantmentHelper.setEnchantments(map, item);
        }
        if (!player.capabilities.isCreativeMode) {
            this.slots[SLOT_OFFERING].stackSize -= offering.count;
            if (this.slots[SLOT_OFFERING].stackSize <= 0) {
                this.slots[SLOT_OFFERING] = null;
            }
            player.addExperienceLevel(-level * LEVELS_PER_ENCHANT_LEVEL);
        }
        this.worldObj.playSoundEffect(this.xCoord + 0.5D, this.yCoord + 0.5D, this.zCoord + 0.5D, "random.levelup", 1.0F, 0.6F);
        markDirty();
    }

    // --- Inventaire ---------------------------------------------------------------------------

    @Override
    public int getSizeInventory() {
        return this.slots.length;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return this.slots[slot];
    }

    @Override
    public ItemStack decrStackSize(int slot, int count) {
        if (this.slots[slot] == null) {
            return null;
        }
        ItemStack result;
        if (this.slots[slot].stackSize <= count) {
            result = this.slots[slot];
            this.slots[slot] = null;
        } else {
            result = this.slots[slot].splitStack(count);
        }
        markDirty();
        return result;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        ItemStack stack = this.slots[slot];
        this.slots[slot] = null;
        return stack;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        this.slots[slot] = stack;
        markDirty();
    }

    @Override
    public String getInventoryName() {
        return "container.netheryx.soul_altar";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return this.worldObj.getTileEntity(this.xCoord, this.yCoord, this.zCoord) == this
                && player.getDistanceSq(this.xCoord + 0.5D, this.yCoord + 0.5D, this.zCoord + 0.5D) <= 64.0D;
    }

    @Override
    public void openInventory() {
    }

    @Override
    public void closeInventory() {
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return slot == SLOT_ITEM ? acceptsItem(stack) : offeringIndex(stack) >= 0;
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < this.slots.length; i++) {
            if (this.slots[i] != null) {
                NBTTagCompound e = new NBTTagCompound();
                e.setByte("Slot", (byte) i);
                this.slots[i].writeToNBT(e);
                list.appendTag(e);
            }
        }
        tag.setTag("Items", list);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        NBTTagList list = tag.getTagList("Items", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound e = list.getCompoundTagAt(i);
            int slot = e.getByte("Slot");
            if (slot >= 0 && slot < this.slots.length) {
                this.slots[slot] = ItemStack.loadItemStackFromNBT(e);
            }
        }
    }
}

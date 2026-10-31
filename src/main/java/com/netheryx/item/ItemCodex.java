package com.netheryx.item;

import com.netheryx.NetheryX;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

/**
 * Codex de NetheryX : guide du joueur, ouvert comme un livre ecrit. Les pages sont lues dans le fichier de langue
 * ({@code codex.netheryx.page.N}, « \n » pour un retour a la ligne) : le Codex suit la langue du joueur.
 */
public class ItemCodex extends ItemBasic {
    public ItemCodex(String name) {
        super(name);
        stack(1);
        rarity(EnumRarity.uncommon);
        setTextureName("book_written");
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (world.isRemote) {
            NetheryX.proxy.openCodex(player, book());
        }
        return stack;
    }

    /** Livre ecrit construit a partir des pages traduites (cote client). */
    public static ItemStack book() {
        ItemStack book = new ItemStack(Items.written_book);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("title", StatCollector.translateToLocal("item.netheryx.codex.name"));
        tag.setString("author", "NetheryX");
        NBTTagList pages = new NBTTagList();
        for (int i = 1; StatCollector.canTranslate("codex.netheryx.page." + i); i++) {
            pages.appendTag(new NBTTagString(StatCollector.translateToLocal("codex.netheryx.page." + i).replace("\\n", "\n")));
        }
        tag.setTag("pages", pages);
        book.setTagCompound(tag);
        return book;
    }
}

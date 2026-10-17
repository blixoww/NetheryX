package com.netheryx.event;

import com.netheryx.NetheryX;
import com.netheryx.config.NetheryXConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Donne le Codex NetheryX une seule fois a chaque joueur. Le livre est fourni par Patchouli :
 * on construit la pile par identifiant pour ne pas dependre de son API a la compilation.
 */
@Mod.EventBusSubscriber(modid = NetheryX.MOD_ID)
public final class GuideBookHandler {
    public static final ResourceLocation BOOK_ID = NetheryX.id("codex");
    private static final ResourceLocation PATCHOULI_BOOK_ITEM = new ResourceLocation("patchouli", "guide_book");
    private static final String TAG_GIVEN = NetheryX.MOD_ID + ":codex_given";

    private GuideBookHandler() {
    }

    public static ItemStack createCodex() {
        Item item = ForgeRegistries.ITEMS.getValue(PATCHOULI_BOOK_ITEM);
        if (item == null || item == Items.AIR) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = new ItemStack(item);
        stack.getOrCreateTag().putString("patchouli:book", BOOK_ID.toString());
        return stack;
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide || !NetheryXConfig.GIVE_GUIDE_ON_FIRST_JOIN.get()) {
            return;
        }
        CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        if (persisted.getBoolean(TAG_GIVEN)) {
            return;
        }
        ItemStack codex = createCodex();
        if (codex.isEmpty()) {
            return;
        }
        if (!player.getInventory().add(codex)) {
            player.drop(codex, false);
        }
        persisted.putBoolean(TAG_GIVEN, true);
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
    }
}

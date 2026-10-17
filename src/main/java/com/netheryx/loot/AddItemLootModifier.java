package com.netheryx.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Ajoute une pile d'objets a une table de butin existante.
 * Exemple : {@code data/netheryx/loot_modifiers/bastion_onyx.json}.
 */
public class AddItemLootModifier extends LootModifier {
    public static final Codec<AddItemLootModifier> CODEC = RecordCodecBuilder.create(instance -> codecStart(instance)
            .and(ForgeRegistries.ITEMS.getCodec().fieldOf("item").forGetter(modifier -> modifier.item))
            .and(Codec.intRange(1, 64).optionalFieldOf("min_count", 1).forGetter(modifier -> modifier.minCount))
            .and(Codec.intRange(1, 64).optionalFieldOf("max_count", 1).forGetter(modifier -> modifier.maxCount))
            .apply(instance, AddItemLootModifier::new));

    private final Item item;
    private final int minCount;
    private final int maxCount;

    public AddItemLootModifier(LootItemCondition[] conditions, Item item, int minCount, int maxCount) {
        super(conditions);
        this.item = item;
        this.minCount = minCount;
        this.maxCount = Math.max(minCount, maxCount);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        int count = this.minCount + context.getRandom().nextInt(this.maxCount - this.minCount + 1);
        generatedLoot.add(new ItemStack(this.item, count));
        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}

package com.netheryx.entity;

import com.netheryx.item.SoulCompassItem;
import com.netheryx.registry.ModEnchantments;
import com.netheryx.registry.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.InteractGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.LookAtTradingPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.TradeWithPlayerGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Marchand piglin : il voyage avec les caravanes nomades et commerce en <b>or</b> et en <b>onyx</b>.
 * Il rachete le soufre et les ecailles, vend des graines rares, des gabarits d'infusion, des livres d'enchantements
 * du Nether et des boussoles des ames deja accordees sur un donjon proche.
 */
public class PiglinMerchant extends AbstractVillager implements GeoEntity {
    private static final RawAnimation IDLE = NetheryXAnimations.loop("piglin_merchant", "idle");
    private static final RawAnimation WALK = NetheryXAnimations.loop("piglin_merchant", "walk");
    private static final ResourceLocation[] DUNGEONS = {
            new ResourceLocation("netheryx", "onyx_citadel"), new ResourceLocation("netheryx", "infernal_sanctum"),
            new ResourceLocation("netheryx", "withered_catacombs")};

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public PiglinMerchant(EntityType<? extends AbstractVillager> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return AbstractVillager.createMobAttributes().add(Attributes.MAX_HEALTH, 24.0D).add(Attributes.MOVEMENT_SPEED, 0.5D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new TradeWithPlayerGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 0.6D));
        this.goalSelector.addGoal(1, new LookAtTradingPlayerGoal(this));
        this.goalSelector.addGoal(4, new MoveTowardsRestrictionGoal(this, 0.35D));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 0.35D));
        this.goalSelector.addGoal(9, new InteractGoal(this, Player.class, 3.0F, 1.0F));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, net.minecraft.world.entity.Mob.class, 8.0F));
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (this.isAlive() && !this.isTrading() && !this.isBaby()) {
            if (this.getOffers().isEmpty()) {
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }
            if (!this.level().isClientSide) {
                this.setTradingPlayer(player);
                this.openTradingScreen(player, this.getDisplayName(), 1);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    @Override
    protected void updateTrades() {
        MerchantOffers offers = this.getOffers();
        List<VillagerTrades.ItemListing> common = new ArrayList<>(List.of(
                buy(ModItems.SULFUR, 8, Items.GOLD_INGOT, 1),
                buy(ModItems.MAGMAFISH_SCALE, 3, Items.GOLD_INGOT, 1),
                buy(ModItems.WRAITH_ESSENCE, 2, Items.GOLD_INGOT, 2),
                sell(Items.GOLD_INGOT, 2, ModItems.BLAZE_PEPPER_SEEDS, 4),
                sell(Items.GOLD_INGOT, 2, ModItems.CINDER_WHEAT_SEEDS, 4),
                sell(Items.GOLD_INGOT, 3, ModItems.WARTFRUIT, 3),
                sell(Items.GOLD_INGOT, 3, ModItems.SOUL_TUBER, 3),
                sell(Items.GOLD_INGOT, 6, () -> com.netheryx.registry.ModBlocks.LAVA_TRAP.get().asItem(), 1),
                sell(Items.GOLD_INGOT, 5, ModItems.RIME_SHARD, 4)));
        List<VillagerTrades.ItemListing> rare = new ArrayList<>(List.of(
                book(ModEnchantments.EMBER_STRIDE),
                book(ModEnchantments.SOUL_HARVEST),
                book(ModEnchantments.ANCHORING),
                (trader, random) -> new MerchantOffer(new ItemStack(ModItems.ONYX.get(), 1), new ItemStack(Items.GOLD_INGOT, 8),
                        new ItemStack(ModItems.ONYX_INFUSION_TEMPLATE.get()), 2, 15, 0.05F)));
        java.util.Collections.shuffle(common, new java.util.Random(this.random.nextLong()));
        java.util.Collections.shuffle(rare, new java.util.Random(this.random.nextLong()));
        for (VillagerTrades.ItemListing listing : common.subList(0, 5)) {
            add(offers, listing);
        }
        for (VillagerTrades.ItemListing listing : rare.subList(0, 2)) {
            add(offers, listing);
        }
        add(offers, this::compassOffer);
    }

    private void add(MerchantOffers offers, VillagerTrades.ItemListing listing) {
        MerchantOffer offer = listing.getOffer(this, this.random);
        if (offer != null) {
            offers.add(offer);
        }
    }

    /** Boussole des ames accordee sur le donjon NetheryX le plus proche (choisi au hasard parmi les trois). */
    @Nullable
    private MerchantOffer compassOffer(Entity trader, RandomSource random) {
        if (!(this.level() instanceof ServerLevel level)) {
            return null;
        }
        ItemStack compass = SoulCompassItem.attuned(level, this.blockPosition(), DUNGEONS[random.nextInt(DUNGEONS.length)]);
        if (compass.isEmpty()) {
            return null;
        }
        return new MerchantOffer(new ItemStack(ModItems.ONYX.get(), 2), compass, 1, 20, 0.05F);
    }

    private static VillagerTrades.ItemListing buy(Supplier<? extends Item> item, int count, Item price, int priceCount) {
        return (trader, random) -> new MerchantOffer(new ItemStack(item.get(), count), new ItemStack(price, priceCount), 12, 2, 0.05F);
    }

    private static VillagerTrades.ItemListing sell(Item currency, int cost, Supplier<? extends Item> item, int count) {
        return (trader, random) -> new MerchantOffer(new ItemStack(currency, cost), new ItemStack(item.get(), count), 8, 3, 0.05F);
    }

    private static VillagerTrades.ItemListing book(Supplier<Enchantment> enchantment) {
        return (trader, random) -> new MerchantOffer(new ItemStack(ModItems.ONYX.get(), 2), new ItemStack(Items.BOOK),
                EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchantment.get(), 1)), 2, 15, 0.05F);
    }

    @Override
    protected void rewardTradeXp(MerchantOffer offer) {
        if (offer.shouldRewardExp()) {
            int xp = 3 + this.random.nextInt(4);
            this.level().addFreshEntity(new ExperienceOrb(this.level(), this.getX(), this.getY() + 0.5D, this.getZ(), xp));
        }
    }

    @Override
    public boolean showProgressBar() {
        return false;
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return null;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.isTrading() ? SoundEvents.PIGLIN_ADMIRING_ITEM : SoundEvents.PIGLIN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.PIGLIN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PIGLIN_DEATH;
    }

    @Override
    protected SoundEvent getTradeUpdatedSound(boolean accepted) {
        return accepted ? SoundEvents.PIGLIN_CELEBRATE : SoundEvents.PIGLIN_ANGRY;
    }

    @Override
    public SoundEvent getNotifyTradeSound() {
        return SoundEvents.PIGLIN_ADMIRING_ITEM;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "movement", 4, this::predicate));
    }

    private PlayState predicate(AnimationState<PiglinMerchant> state) {
        return state.setAndContinue(state.isMoving() ? WALK : IDLE);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}

package com.clovercraftsmp.clover.datagen.providers;

import com.clovercraftsmp.clover.Clover;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.*;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.*;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.text.BreakIterator;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

//? if >= 26.1 {
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.SimpleFabricLootTableSubProvider;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.EntityTypePredicate;
import net.minecraft.advancements.criterion.NbtPredicate;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.util.CompilableString;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.ClearAllStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.PlaySoundConsumeEffect;
import net.minecraft.world.item.equipment.Equippable;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.JsonOps;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EquipmentSlot;
//? }

//? if <= 1.21.1 {
/*import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.SimpleFabricLootTableProvider;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.EntityTypePredicate;
import net.minecraft.advancements.critereon.NbtPredicate;
*///? }

//? if >= 26.1 {
public class CloverEntityLootProvider extends SimpleFabricLootTableSubProvider {
    public CloverEntityLootProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(output, registryLookup, LootContextParamSets.ENTITY);
        this.registryLookup = registryLookup;
    }
//? }

//? if <= 1.21.1 {
/*public class CloverEntityLootProvider extends SimpleFabricLootTableProvider {
    public CloverEntityLootProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(output, registryLookup, LootContextParamSets.ENTITY);
        this.registryLookup = registryLookup;
    }
*///? }

    private final CompletableFuture<HolderLookup.Provider> registryLookup;
    private static final ResourceKey<LootTable> PLAYER = ResourceKey.create(Registries.LOOT_TABLE, Clover.id("minecraft", "entities/player"));

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output) {
        HolderLookup.Provider registries = registryLookup.join();

        //? if >= 26.1
        CompilableString<EntitySelector> self = EntitySelector.COMPILABLE_CODEC.parse(JsonOps.INSTANCE, new JsonPrimitive("@s")).getOrThrow();
        //? if <= 1.21.1
        //String self = "@s";

        // Main player head item
        LootTable.Builder builder = LootTable.lootTable().withPool(LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .when(killedByPlayer())
                .add(LootItem.lootTableItem(Items.PLAYER_HEAD)
                        .apply(SetNameFunction.setName(
                                Component.translatable("block.minecraft.player_head.named",
                                        Component.selector(self, Optional.empty())),
                                SetNameFunction.Target.CUSTOM_NAME,
                                LootContext.EntityTarget.THIS))
                        .apply(FillPlayerHead.fillPlayerHead(LootContext.EntityTarget.THIS))));

        // Nivarq custom death item
        //? if >= 26.1
        Holder<DamageType> drown = registries.getOrThrow(DamageTypes.DROWN);
        addPlayerDrop(builder, UUID.fromString("e1fe2361-950e-47ca-ad5d-c202940166cb"), Items.TURTLE_SCUTE, 1, DataComponentPatch.builder()
                .set(DataComponents.RARITY, Rarity.EPIC)
                .set(DataComponents.ITEM_NAME, gradient("Turtle Scute", 0x09450F, 0xA8FFA8))
                .set(DataComponents.LORE, ItemLore.EMPTY
                        .withLineAdded(
                                Component.empty()
                                        .append(Component.translatable("enchantment.minecraft.luck_of_the_sea").withStyle(style -> style.withColor(ChatFormatting.GRAY).withItalic(false)))
                                        .append(Component.literal(" "))
                                        .append(Component.translatable("enchantment.level.10").withStyle(style -> style.withColor(ChatFormatting.GRAY).withItalic(false)))
                        )
                        .withLineAdded(
                                Component.literal("a scute from THE turtle").withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_GREEN)
                        )
                )
                .set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)
                //? if >= 26.1 {
                .set(DataComponents.CONSUMABLE, Consumable.builder()
                        .onConsume(new ApplyStatusEffectsConsumeEffect(
                                List.of(new MobEffectInstance(
                                        MobEffects.SLOWNESS,
                                        200,
                                        4
                                ))
                        ))
                        .consumeSeconds(2.0f)
                        .build()
                )
                .set(DataComponents.DAMAGE_TYPE, drown)
                //? }
                //? if <= 1.21.1 {
                /*.set(DataComponents.FOOD, new FoodProperties(
                        0,
                        0.0f,
                        true,
                        2.0f,
                        Optional.empty(),
                        List.of(new FoodProperties.PossibleEffect(
                                new MobEffectInstance(
                                        MobEffects.MOVEMENT_SLOWDOWN,
                                        200,
                                        4
                                ),
                                1.0f
                        ))
                ))
                *///? }
                .build());

        // Deiphai custom death item
        addPlayerDrop(builder, UUID.fromString("11ba326b-af82-48e9-9a6e-2c17a4ebb4b7"), Items.HORN_CORAL_BLOCK, 1, DataComponentPatch.builder()
                .set(DataComponents.ITEM_NAME, Component.literal("Cheese...?").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.GOLD))
                .set(DataComponents.LORE, ItemLore.EMPTY.withLineAdded(Component.literal("Is it... pulsating?").withStyle(ChatFormatting.YELLOW)))
                //? if >= 26.1 {
                .set(DataComponents.CONSUMABLE, Consumable.builder()
                        .onConsume(new ApplyStatusEffectsConsumeEffect(
                                List.of(new MobEffectInstance(
                                        MobEffects.WITHER,
                                        50,
                                        2,
                                        false,
                                        true,
                                        true
                                )),
                                0.7f
                        ))
                        .consumeSeconds(0.5f)
                        .sound(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.CREAKING_HEART_BREAK))
                        .build()
                )
                .set(DataComponents.FOOD, new FoodProperties(3, 2.0f, true))
                //? }
                //? if <= 1.21.1 {
                /*.set(DataComponents.FOOD, new FoodProperties(
                        3,
                        2.0f,
                        true,
                        0.5f,
                        Optional.empty(),
                        List.of(new FoodProperties.PossibleEffect(
                                new MobEffectInstance(
                                        MobEffects.WITHER,
                                        50,
                                        2,
                                        false,
                                        true,
                                        true
                                ),
                                0.7f
                        ))
                ))
                *///? }
                .build());

        // Blackleaf custom death item
        addPlayerDrop(builder, UUID.fromString("d2bee025-28b9-4d4b-a521-199631a821d6"), Items.WOODEN_PICKAXE, 1, DataComponentPatch.builder()
                .set(DataComponents.DAMAGE, 58)
                .set(DataComponents.ITEM_NAME, Component.literal("STRIKE THE EARTH!").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.GOLD).withStyle(ChatFormatting.ITALIC))
                .build());

        // Caylee custom death item - doesn't exist on 1.21.1, as of now undecided
        //? if >= 26.1 {
        addPlayerDrop(builder, UUID.fromString("182457d6-ebf3-4fbf-92af-7117b82a67bf"), Items.SLIME_BLOCK, 1, DataComponentPatch.builder()
                .set(DataComponents.EQUIPPABLE, new Equippable(
                        EquipmentSlot.HEAD,
                        BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.SLIME_ATTACK),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        true,
                        true,
                        true,
                        true,
                        false,
                        BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.SHEARS_SNIP)
                ))
                .set(DataComponents.CUSTOM_NAME, Component.literal("The Cube").withColor(0x39FF14).withStyle(ChatFormatting.BOLD))
                .set(DataComponents.LORE, ItemLore.EMPTY
                        .withLineAdded(
                                Component.empty()
                                        .append(Component.translatable("enchantment.minecraft.protection").withStyle(style -> style.withColor(ChatFormatting.GRAY).withItalic(false)))
                                        .append(Component.literal(" 0").withStyle(style -> style.withColor(ChatFormatting.GRAY).withItalic(false)))
                        )
                        .withLineAdded(
                                Component.literal("Do you want to spend time in the Cube?").withColor(0x39FF14)
                        )
                        .withLineAdded(
                                Component.literal("It's where I go when I'm sad.").withColor(0x39FF14)
                        )
                        .withLineAdded(
                                Component.literal("Or injured.").withColor(0x39FF14)
                        )
                        .withLineAdded(
                                Component.literal("Or just want to be in the Cube.").withColor(0x39FF14)
                        )
                )
                .set(DataComponents.RARITY, Rarity.EPIC)
                .build());
        //? }

        // Snowman custom death item
        addPlayerDrop(builder, UUID.fromString("1ea100a7-80bd-419d-86a1-591dba55c287"), Items.SNOW_BLOCK, 1, DataComponentPatch.builder()
                .set(DataComponents.RARITY, Rarity.EPIC)
                .set(DataComponents.ITEM_NAME,
                        Component.literal("Snov Block")
                                .withStyle(ChatFormatting.BOLD)
                                .withStyle(ChatFormatting.WHITE)
                )
                .set(DataComponents.LORE, ItemLore.EMPTY
                        .withLineAdded(
                                Component.literal("feel snov´s pain around rat")
                                        .withStyle(ChatFormatting.DARK_RED)
                                        .withStyle(ChatFormatting.ITALIC)
                        )
                )
                .set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)
                //? if >= 26.1 {
                .set(DataComponents.CONSUMABLE, Consumable.builder()
                        .consumeSeconds(1.0f)
                        .onConsume(new ApplyStatusEffectsConsumeEffect(
                                List.of(
                                        new MobEffectInstance(
                                                MobEffects.LEVITATION,
                                                2,
                                                255
                                        ),
                                        new MobEffectInstance(
                                                MobEffects.BLINDNESS,
                                                140,
                                                4
                                        )
                                )
                        ))
                        .build())
                //? }
                //? if <= 1.21.1 {
                /*.set(DataComponents.FOOD, new FoodProperties(
                        0,
                        0.0f,
                        true,
                        1.0f,
                        Optional.empty(),
                        List.of(
                                new FoodProperties.PossibleEffect(
                                        new MobEffectInstance(
                                                MobEffects.LEVITATION,
                                                2,
                                                255
                                        ),
                                        1.0f
                                ),
                                new FoodProperties.PossibleEffect(
                                        new MobEffectInstance(
                                                MobEffects.BLINDNESS,
                                                140,
                                                4
                                        ),
                                        1.0f
                                )
                        )
                ))
                *///? }
                .build());

        // Quinn custom death item. Eating interaction doesn't exist on 1.21.1.
        addPlayerDrop(builder, UUID.fromString("43d6490f-a7dd-43a2-a52c-cc6891c09f71"), Items.AMETHYST_SHARD, 1, DataComponentPatch.builder()
                .set(DataComponents.CUSTOM_NAME,
                        Component.literal("Crystal Shard")
                                .withStyle(style ->
                                        style.withColor(ChatFormatting.LIGHT_PURPLE)
                                                .withBold(true)
                                                .withItalic(true)
                                                //? if >= 26.1
                                                .withShadowColor(-5635926)
                                )
                )
                .set(DataComponents.LORE, ItemLore.EMPTY
                        .withLineAdded(
                                Component.literal("A thunderous sound shakes the ground,")
                                        .withStyle(style -> style.withColor(ChatFormatting.DARK_PURPLE).withItalic(true))
                        )
                        .withLineAdded(
                                Component.literal("Where giants' shadows linger 'round.")
                                        .withStyle(style -> style.withColor(ChatFormatting.DARK_PURPLE).withItalic(true))
                        )
                        .withLineAdded(
                                Component.literal("A weary march, a heavy cost,")
                                        .withStyle(style -> style.withColor(ChatFormatting.DARK_PURPLE).withItalic(true))
                        )
                        .withLineAdded(
                                Component.literal("Innocence precious, sadly lost.")
                                        .withStyle(style -> style.withColor(ChatFormatting.DARK_PURPLE).withItalic(true))
                        )
                        .withLineAdded(
                                Component.literal("This shard shines brighter than anything you know.")
                                        .withStyle(style -> style.withColor(ChatFormatting.LIGHT_PURPLE)
                                                //? if >= 26.1
                                                .withShadowColor(-16777046)
                                                .withBold(true)
                                        )
                        )
                        .withLineAdded(Component.literal("     "))
                        .withLineAdded(
                                Component.literal("https://youtu.be/j3j7i7UG-9c")
                                        .withStyle(style -> style.withColor(ChatFormatting.WHITE).withItalic(true))
                        )
                )
                .set(DataComponents.RARITY, Rarity.EPIC)
                .set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)
                //? if >= 26.1 {
                .set(DataComponents.CONSUMABLE, Consumable.builder()
                        .consumeSeconds(10.0f)
                        .animation(ItemUseAnimation.TRIDENT)
                        .sound(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.AMETHYST_BLOCK_CHIME))
                        .hasConsumeParticles(true)
                        .onConsume(new ClearAllStatusEffectsConsumeEffect())
                        .onConsume(new PlaySoundConsumeEffect(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.AMETHYST_BLOCK_RESONATE)))
                        .build()
                )
                //? }
                .build());

        // Ender custom death item.
        addPlayerDrop(builder, UUID.fromString("116b94bd-1068-4b71-9aaa-7563e1339202"), Items.ENDER_EYE, 1, DataComponentPatch.builder()
                .set(DataComponents.ITEM_NAME, Component.literal("Ender's Eye").withStyle(ChatFormatting.DARK_PURPLE))
                .set(DataComponents.LORE, ItemLore.EMPTY
                        .withLineAdded(gradient("A twisted eye from a warped mind.", 0xD359FF, 0x5D2F66))
                        .withLineAdded(gradient("Sometimes, it flashes beyond the sky.", 0xD359FF, 0x5D2F66))
                        .withLineAdded(gradient("What may break him, or set him free?", 0xD359FF, 0x5D2F66))
                        .withLineAdded(gradient("...it seems to start with the letter \"G\".", 0xD359FF, 0x5D2F66))
                        .withLineAdded(
                                Component.literal("Auram Radio Cognitio")
                                        .withStyle(style -> style.withColor(ChatFormatting.DARK_AQUA)
                                                        //? if >= 26.1 {
                                                        .withFont(new FontDescription.Resource(Clover.id("minecraft", "illageralt")))
                                                        .withShadowColor(-11141121)
                                                //? }
                                                //? if <= 1.21.1
                                                //.withFont(Clover.id("minecraft", "illageralt"))
                                        )
                        )
                )
                .set(DataComponents.RARITY, Rarity.EPIC)
                //? if >= 26.1 {
                .set(DataComponents.FOOD, new FoodProperties(1, 2.9f, true))
                .set(DataComponents.CONSUMABLE, Consumable.builder()
                        .consumeSeconds(5.0f)
                        .onConsume(new ApplyStatusEffectsConsumeEffect(
                                List.of(
                                        new MobEffectInstance(MobEffects.DARKNESS, 666, 5),
                                        new MobEffectInstance(MobEffects.MINING_FATIGUE, 576, 1)
                                ),
                                0.9f
                        ))
                        .onConsume(new PlaySoundConsumeEffect(SoundEvents.OMINOUS_BOTTLE_DISPOSE))
                        .build())
                //? }
                //? if <= 1.21.1 {
                /*.set(DataComponents.FOOD, new FoodProperties(
                        1,
                        2.9f,
                        true,
                        5.0f,
                        Optional.empty(),
                        List.of(
                                new FoodProperties.PossibleEffect(new MobEffectInstance(MobEffects.DARKNESS, 666, 5), 0.9f),
                                new FoodProperties.PossibleEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 576, 1), 0.9f)
                        )
                ))
                *///? }
                .build());

        //Valk custom death item
        UUID valkUuid = UUID.fromString("9eebf49b-b3b9-4472-9a21-6b0571332028");
        CompoundTag signature = new CompoundTag();
        signature.putIntArray("clover:verified_owner", UUIDUtil.uuidToIntArray(valkUuid));

        addPlayerDrop(builder, valkUuid, Items.LILY_PAD, 64, DataComponentPatch.builder()
                .set(DataComponents.CUSTOM_NAME, Component.literal("CloverCoin")
                        .withStyle(style -> style.withItalic(false).withBold(false).withColor(ChatFormatting.GOLD)))
                .set(DataComponents.LORE, ItemLore.EMPTY
                        .withLineAdded(
                                Component.literal("A leaf removed from a four-leaf clover.")
                                        .withStyle(style -> style.withItalic(false).withColor(ChatFormatting.GRAY))
                        )
                        .withLineAdded(
                                Component.literal("On its own, virtually useless, ")
                                        .withStyle(style -> style.withItalic(false).withColor(ChatFormatting.GRAY))
                        )
                        .withLineAdded(
                                Component.literal("but a valuable commodity when used at the ")
                                        .withStyle(style -> style.withItalic(false).withColor(ChatFormatting.GRAY))
                                        .append(
                                                cycleColors("Clover Carnival", 16733525, 16777215)
                                                        .withStyle(style -> style.withItalic(false))
                                        )
                                        .append(Component.literal(".").withStyle(style -> style.withItalic(false).withColor(ChatFormatting.GRAY)))
                        )
                        .withLineAdded(
                                Component.literal("Spend wisely.")
                                        .withStyle(style -> style.withItalic(false).withColor(ChatFormatting.GRAY))
                        )
                )
                .set(DataComponents.RARITY, Rarity.UNCOMMON)
                .set(DataComponents.CUSTOM_DATA, CustomData.of(signature))
                .build());

        // Auri custom death item
        addPlayerDrop(builder, UUID.fromString("469d59da-1754-47c4-b122-53d47da59f81"), Items.AMETHYST_SHARD, 1, DataComponentPatch.builder()
                .set(DataComponents.CUSTOM_NAME,
                        Component.literal("Fragment of Memory")
                                .withStyle(style ->
                                        style
                                                .withBold(true)
                                                .withItalic(false)
                                                .withColor(ChatFormatting.LIGHT_PURPLE)
                                )
                )
                .set(DataComponents.LORE, ItemLore.EMPTY
                        .withLineAdded(
                                Component.literal("A strange energy swirls inside...")
                        )
                        .withLineAdded(
                                Component.literal("A memorial for those we've lost")
                                        .withStyle(style -> style
                                                .withItalic(false)
                                                .withColor(ChatFormatting.GRAY)
                                        )
                        )
                )
                .set(DataComponents.RARITY, Rarity.EPIC)
                .build());

        // Syfe custom death item
        CompoundTag syfeTag = new CompoundTag();
        syfeTag.putBoolean("syfe_pendant", true);

        //? if >= 26.1
        Holder<DamageType> inFire = registries.getOrThrow(DamageTypes.IN_FIRE);
        addPlayerDrop(builder, UUID.fromString("4f5469d3-522b-4bf8-ad42-7373ece5317e"), Items.HEART_OF_THE_SEA, 1, DataComponentPatch.builder()
                .set(DataComponents.ITEM_NAME,
                        Component.literal("Tohsaka Heirloom Pendant")
                                .withStyle(ChatFormatting.BOLD)
                                .withStyle(ChatFormatting.RED)
                )
                .set(DataComponents.LORE, ItemLore.EMPTY
                        .withLineAdded(
                                Component.literal("   It once held ten years of mana,")
                                        .withStyle(style -> style.withItalic(false))
                        )
                        .withLineAdded(
                                Component.literal("    spent in a single night to save")
                                        .withStyle(style -> style.withItalic(false))
                        )
                        .withLineAdded(
                                Component.literal("        a life that wasn't hers.")
                                        .withStyle(style -> style.withItalic(false))
                        )
                        .withLineAdded(
                                Component.literal("     Its magic was spent long ago.")
                                        .withStyle(style -> style.withItalic(false))
                        )
                        .withLineAdded(
                                Component.literal("                 ... mostly.")
                                        .withStyle(style -> style.withItalic(false))
                        )
                )
                .set(DataComponents.RARITY, Rarity.EPIC)
                .set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)
                .set(DataComponents.MAX_STACK_SIZE, 1)
                .set(DataComponents.MAX_DAMAGE, 100)
                .set(DataComponents.DAMAGE, 95)
                .set(DataComponents.CUSTOM_DATA, CustomData.of(syfeTag))
                //? if >= 26.1 {
                .set(DataComponents.ITEM_MODEL, Clover.id("minecraft", "fire_charge"))
                .set(DataComponents.DAMAGE_RESISTANT, new DamageResistant(HolderSet.direct(inFire)))
                .set(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT
                        .withHidden(DataComponents.DAMAGE, true)
                        .withHidden(DataComponents.MAX_DAMAGE, true)
                )
                //? }
                .build()
        );

        // Toyota custom death item
        addPlayerDrop(builder, UUID.fromString("93323268-a136-4871-a8a3-4b034d6bb731"),
                //? if >= 26.1
                Items.IRON_CHAIN,
                //? if <= 1.21.1
                //Items.CHAIN,
                1, DataComponentPatch.builder()
                        .set(DataComponents.CUSTOM_NAME, Component.literal("'98 Toyota Corolla Driveshaft").withStyle(ChatFormatting.RED))
                        .set(DataComponents.LORE, ItemLore.EMPTY
                                .withLineAdded(
                                        Component.empty()
                                                .append(Component.translatable("enchantment.minecraft.unbreaking").withStyle(style -> style.withColor(ChatFormatting.GRAY).withItalic(false)))
                                                .append(Component.literal(" "))
                                                .append(Component.translatable("enchantment.level.3").withStyle(style -> style.withColor(ChatFormatting.GRAY).withItalic(false)))
                                )
                                .withLineAdded(
                                        Component.empty()
                                                .append(Component.translatable("enchantment.minecraft.mending").withStyle(style -> style.withColor(ChatFormatting.GRAY).withItalic(false)))
                                )
                                .withLineAdded(
                                        Component.literal("Driveshaft for a 98' Toyota Corolla.")
                                                .withStyle(ChatFormatting.RED)
                                                .withStyle(ChatFormatting.BOLD)
                                                .withStyle(ChatFormatting.ITALIC)
                                )
                                .withLineAdded(
                                        Component.literal("Has been used for over 1 million miles.")
                                                .withStyle(ChatFormatting.DARK_RED)
                                                .withStyle(ChatFormatting.BOLD)
                                                .withStyle(ChatFormatting.ITALIC)
                                )
                                .withLineAdded(
                                        Component.literal("Will last its new owner a million more.")
                                                .withStyle(ChatFormatting.DARK_RED)
                                                .withStyle(ChatFormatting.BOLD)
                                                .withStyle(ChatFormatting.ITALIC)
                                )
                        )
                        .set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)
                        .build());

        // Peii custom death item - not done yet
//        addPlayerDrop(builder, UUID.fromString("c99f780e-52f4-4aa3-bf46-a62a6881097b"), Items.REPEATER, 1, DataComponentPatch.builder()
//                .build());

        output.accept(PLAYER, builder);
    }

    private static MutableComponent cycleColors(String text, int... colors) {
        List<String> segments = splitPrintableCharacters(text);
        MutableComponent line = Component.empty();
        for (int i = 0; i < segments.size(); i++) {
            line.append(Component.literal(segments.get(i)).withColor(colors[i % colors.length]));
        }
        return line;
    }

    private static MutableComponent gradient(String text, int from, int to) {
        List<String> segments = splitPrintableCharacters(text);
        MutableComponent line = Component.empty();
        int n = segments.size();
        for (int i = 0; i < n; i++) {
            line.append(Component.literal(segments.get(i)).withColor(gradientColor(from, to, i, n)));
        }
        return line;
    }

    private static List<String> splitPrintableCharacters(String text) {
        List<String> segments = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean hasGlyph = false;

        BreakIterator it = BreakIterator.getCharacterInstance();
        it.setText(text);
        int start = it.first();

        for (int end = it.next(); end != BreakIterator.DONE; start = end, end = it.next()) {
            String g = text.substring(start, end);
            boolean isSpace = g.equals(" ");
            if (!isSpace && hasGlyph) {
                segments.add(current.toString());
                current.setLength(0);
            }
            current.append(g);
            if (!isSpace) hasGlyph = true;
        }

        if (!current.isEmpty()) segments.add(current.toString());
        return segments;
    }

    private static int gradientColor(int from, int to, int i, int n) {
        if (i == n - 1) return to;
        int r = lerpChannel(from >> 16 & 0xFF, to >> 16 & 0xFF, i, n);
        int g = lerpChannel(from >> 8 & 0xFF, to >> 8 & 0xFF, i, n);
        int b = lerpChannel(from & 0xFF, to & 0xFF, i, n);
        return r << 16 | g << 8 | b;
    }

    private static int lerpChannel(int a, int b, int i, int n) {
        return (int) Math.round(a + (double) ((b - a) * i) / n);
    }

    private static void addPlayerDrop(LootTable.Builder builder, UUID uuid, Item item, int count, DataComponentPatch patch) {
        CompoundTag uuidTag = new CompoundTag();
        uuidTag.put("UUID", new IntArrayTag(UUIDUtil.uuidToIntArray(uuid)));

        LootPoolSingletonContainer.Builder<?> entry = LootItem.lootTableItem(item);
        if (count != 1) {
            entry.apply(SetItemCountFunction.setCount(ConstantValue.exactly(count)));
        }
        for (Map.Entry<DataComponentType<?>, Optional<?>> component : patch.entrySet()) {
            component.getValue().ifPresent(value -> entry.apply(setComponentUnchecked(component.getKey(), value)));
        }

        builder.withPool(LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .when(killedByPlayer())
                .when(LootItemEntityPropertyCondition.hasProperties(
                        LootContext.EntityTarget.THIS,
                        EntityPredicate.Builder.entity().nbt(new NbtPredicate(uuidTag))))
                .add(entry));
    }

    @SuppressWarnings("unchecked")
    private static <T> LootItemConditionalFunction.Builder<?> setComponentUnchecked(DataComponentType<T> type, Object value) {
        return SetComponentsFunction.setComponent(type, (T) value);
    }

    private static LootItemCondition.Builder killedByPlayer() {
        //? if >= 26.1
        EntityTypePredicate isPlayer = EntityTypePredicate.of(BuiltInRegistries.ENTITY_TYPE, EntityType.PLAYER);
        //? if <= 1.21.1
        //EntityTypePredicate isPlayer = EntityTypePredicate.of(EntityType.PLAYER);

        return LootItemEntityPropertyCondition.hasProperties(
                LootContext.EntityTarget.ATTACKER,
                EntityPredicate.Builder.entity().entityType(isPlayer));
    }
}

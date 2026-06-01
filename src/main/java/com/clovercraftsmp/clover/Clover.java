package com.clovercraftsmp.clover;

import com.clovercraftsmp.clover.mixin.loot.EnchantRandomlyFunctionAccessor;
import com.clovercraftsmp.clover.mixin.loot.LootPoolAccessor;
import com.clovercraftsmp.clover.mixin.loot.LootPoolSingletonContainerAccessor;
import com.clovercraftsmp.clover.mixin.loot.LootTableBuilderAccessor;
import com.clovercraftsmp.clover.networking.ClientboundRemoveNoSleepPacket;
import com.clovercraftsmp.clover.networking.ClientboundSetAfkPacket;
import com.clovercraftsmp.clover.util.ItemStackUtil;
import com.google.common.collect.ImmutableList;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Clover implements ModInitializer {
    public static final GameRules.Key<GameRules.BooleanValue> PUNISH_INSOMNIACS =
            GameRuleRegistry.register(
                    "punishInsomniacs",
                    GameRules.Category.MOBS,
                    GameRuleFactory.createBooleanRule(false)
            );

    public static final String MOD_ID = "clover";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final String VERSION = /*$ mod_version*/ "0.3.0";
    public static final String MINECRAFT = /*$ minecraft*/ "1.21.1";

    private static final ResourceLocation END_VAULT = ResourceLocation.fromNamespaceAndPath("enderscape", "end_city/vault");

    @Override
    public void onInitialize() {
        LootTableEvents.MODIFY.register((resourceKey, builder, lootTableSource, provider) -> {
            ItemStackUtil.mendingRegistry(provider);
            LootTableBuilderAccessor builderAccessor = (LootTableBuilderAccessor) builder;

            if (resourceKey.location().equals(END_VAULT)) {
                ArrayList<LootPool> pools = new ArrayList<>(builderAccessor.getPools().build());

                LootPoolAccessor pool = accessorAt(pools, 4);
                ArrayList<LootPoolEntryContainer> entries = new ArrayList<>(pool.getEntries());

                // Decrease the weight of empty from 192 to 104
                LootPoolSingletonContainerAccessor emptyPool = accessorAt(entries, 0);
                emptyPool.setWeight(104);

                // Remove punch, silk touch, and knockback, and add mending. Decrease weight from 48 to 32.
                LootPoolSingletonContainerAccessor bookEntry = accessorAt(entries, 1);
                List<LootItemFunction> functions = new ArrayList<>(bookEntry.getFunctions());
                EnchantRandomlyFunctionAccessor enchantFunction = accessorAt(functions, 0);
                HolderSet<Enchantment> holderSet = enchantFunction.getOptions().orElseThrow();
                enchantFunction.setOptions(Optional.of(add(remove(holderSet, 4, 8, 11), ItemStackUtil.MENDING)));
                bookEntry.setFunctions(functions);
                bookEntry.setWeight(32);

                LootPoolSingletonContainerAccessor otherBookEntry = accessorAt(entries, 2);
                List<LootItemFunction> otherBookFunctions = new ArrayList<>(otherBookEntry.getFunctions());
                EnchantRandomlyFunctionAccessor otherEnchantFunction = accessorAt(otherBookFunctions, 0);
                HolderSet<Enchantment> otherHolderSet = otherEnchantFunction.getOptions().orElseThrow();
                otherEnchantFunction.setOptions(Optional.of(add(otherHolderSet, ItemStackUtil.MENDING)));
                otherBookEntry.setFunctions(otherBookFunctions);

                pool.setEntries(entries);
                pools.set(4, (LootPool) pool);
                ((LootTableBuilderAccessor) builder).setPools(
                        ImmutableList.<LootPool>builder().addAll(pools)
                );

                LOGGER.info("Adjusted enderscapes end vault loot pool!");
            }
        });

        PayloadTypeRegistry.playS2C().register(ClientboundSetAfkPacket.TYPE, ClientboundSetAfkPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(ClientboundRemoveNoSleepPacket.TYPE, ClientboundRemoveNoSleepPacket.CODEC);
    }

    @SafeVarargs
    private static HolderSet<Enchantment> add(HolderSet<Enchantment> original, Holder<Enchantment>... added) {
        ArrayList<Holder<Enchantment>> edited = new ArrayList<>(original.stream().toList());
        edited.addAll(Arrays.asList(added));
        return HolderSet.direct(edited);
    }

    @SuppressWarnings("SameParameterValue")
    private static HolderSet<Enchantment> remove(HolderSet<Enchantment> original, int... indices) {
        ArrayList<Holder<Enchantment>> edited = new ArrayList<>(original.stream().toList());
        int[] sorted = Arrays.stream(indices).boxed().sorted(Comparator.reverseOrder()).mapToInt(Integer::intValue).toArray();
        for (int i : sorted) edited.remove(i);
        return HolderSet.direct(edited);
    }

    @SuppressWarnings("unchecked")
    private static <T> T accessorAt(List<?> list, int index) {
        return (T) list.get(index);
    }

    /**
     * Adapts to the {@link ResourceLocation} changes introduced in 1.21.
     */
    public static ResourceLocation id(String namespace, String path) {
        //? if <1.21 {
        /*return new ResourceLocation(namespace, path);
        *///?} else
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }

    public static ResourceLocation id(String path) {
        return id(MOD_ID, path);
    }
}
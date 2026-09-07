package com.clovercraftsmp.clover.util.filter;
//? if <=1.21.1 {
/*import com.clovercraftsmp.clover.util.ItemStackUtil;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class EnchantmentFilter extends AbstractComparisonFilter<Integer> {
    public static final EnchantmentFilter DEFAULT = new EnchantmentFilter();
    public static final String TYPE = "enchantment";

    private Holder.Reference<Enchantment> enchantmentId;

    public EnchantmentFilter() {
        super(TYPE, 1);
        this.enchantmentId = null;
    }

    public EnchantmentFilter(CompoundTag tag) {
        super(TYPE, tag, tag.getInt("threshold"));
        this.enchantmentId = ItemStackUtil.resolveEnchantment(tag.getString("enchantment"));
    }

    @Override
    public String getFilterName() {
        return "Enchantment Filter";
    }

    @Override
    public int getColor() {
        return 0x3FFF3F;
    }

    @Override
    public Item getSmithingItem() {
        return Items.LIME_DYE;
    }

    @Override
    public List<Mode> allowedModes() {
        return List.of(Mode.AT_LEAST, Mode.AT_MOST, Mode.EXACT);
    }

    @Override
    public String getModeDescription(Mode mode) {
        return switch (mode) {
            case AT_LEAST -> "Passes if the target enchantment is ≥ the specified level.";
            case AT_MOST -> "Passes if the target enchantment is ≤ the specified level.";
            case EXACT -> "Passes if the target enchantment = the specified level.";
            default -> throw new IllegalStateException("Unexpected value: " + mode);
        };
    }

    @Override
    public boolean changeFailed(String entry) {
        if (!super.changeFailed(entry)) return false;

        if (entry.toLowerCase().startsWith("enchant=")) return !applyEnchantment(entry.substring(8));
        if (entry.toLowerCase().startsWith("level=")) return !applyLevel(entry.substring(6));
        return true;
    }

    private boolean applyEnchantment(String trimmed) {
        Holder.Reference<Enchantment> enchantment = ItemStackUtil.resolveEnchantment(trimmed);
        if (enchantment == null) return false;
        enchantmentId = enchantment;
        return true;
    }

    private boolean applyLevel(String trimmed) {
        int newThreshold;
        try {
            newThreshold = Integer.parseInt(trimmed);
            if (newThreshold < 0) return false;
        } catch (NumberFormatException e) {
            return false;
        }
        threshold = newThreshold;
        return true;
    }

    @Override
    public void formatItem(ItemStack stack) {
        super.formatItem(stack);
        ArrayList<Component> loreList = new ArrayList<>(Objects.requireNonNull(stack.get(DataComponents.LORE)).lines());

        String comparison = switch (mode) {
            case AT_LEAST -> "≥";
            case AT_MOST -> "≤";
            case EXACT -> "=";
            default -> throw new IllegalStateException("Unexpected value: " + mode);
        };

        MutableComponent enchantmentThreshold = Component.literal("Enchantment threshold: ");

        if (enchantmentId != null) {
            Identifier loc = enchantmentId.key().location();
            String modName = FabricLoader.getInstance().getModContainer(loc.getNamespace())
                    .map(e -> e.getMetadata().getName())
                    .orElse(loc.getNamespace());

            String translate = String.format("enchantment.%s.%s", loc.getNamespace(), loc.getPath());
            enchantmentThreshold = enchantmentThreshold
                    .append(Component.translatableWithFallback(translate, loc.toString()))
                    .append(Component.literal(modName.equals("Minecraft") ? "" : String.format(" (%s)", modName)));
        } else {
            enchantmentThreshold = enchantmentThreshold.append(Component.literal("Unset"));
        }

        enchantmentThreshold = enchantmentThreshold.append(String.format(" %s %s", comparison, threshold));

        loreList.add(enchantmentThreshold.withStyle(LORE_STYLE));

        stack.set(DataComponents.LORE, new ItemLore(loreList));
    }

    @Override
    protected void addThresholdData(CompoundTag tag) {
        if (enchantmentId != null) {
            tag.putString("enchantment", enchantmentId
                    .unwrapKey()
                    .map(ResourceKey::location)
                    .orElseThrow()
                    .toString());
        }

        tag.putInt("threshold", threshold);
    }

    @Override
    protected Integer targetValue(ItemStack stack) {
        if (enchantmentId == null) return 0;

        if (stack.has(DataComponents.STORED_ENCHANTMENTS)) {
            return Optional.ofNullable(stack.get(DataComponents.STORED_ENCHANTMENTS))
                    .map(e -> e.getLevel(enchantmentId))
                    .orElse(0);
        }

        if (stack.has(DataComponents.ENCHANTMENTS)) {
            return Optional.ofNullable(stack.get(DataComponents.ENCHANTMENTS))
                    .map(e -> e.getLevel(enchantmentId))
                    .orElse(0);
        }

        return 0;
    }
}
*///?}
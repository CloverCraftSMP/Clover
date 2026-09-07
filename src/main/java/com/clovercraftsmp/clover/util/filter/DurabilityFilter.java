package com.clovercraftsmp.clover.util.filter;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DurabilityFilter extends AbstractComparisonFilter<Double> {
    public static final DurabilityFilter DEFAULT = new DurabilityFilter();
    public static final String TYPE = "durability";

    public DurabilityFilter() {
        super(TYPE, 0.0);
    }

    public DurabilityFilter(CompoundTag tag) {
        super(TYPE, tag, tag.getDouble("threshold")/*? if >1.21.1 {*/.orElse(0.0)/*?}*/);
    }

    @Override
    public String getFilterName() {
        return "Durability Filter";
    }

    @Override
    public int getColor() {
        return 0x007F00;
    }

    @Override
    public Item getSmithingItem() {
        return Items.GREEN_DYE;
    }

    @Override
    public List<Mode> allowedModes() {
        return List.of(Mode.AT_LEAST, Mode.AT_MOST);
    }

    @Override
    public String getModeDescription(Mode mode) {
        return switch (mode) {
            case AT_LEAST -> "Passes if the durability is ≥ the percentage specified.";
            case AT_MOST -> "Passes if the durability is ≤ percentage specified.";
            default -> throw new IllegalStateException("Unexpected value: " + mode);
        };
    }

    @Override
    public boolean changeFailed(String entry) {
        if (!super.changeFailed(entry)) return false;
        if (!(entry.toLowerCase().startsWith("value="))) return true;
        String tested = entry.substring(6, entry.length() - (entry.endsWith("%") ? 1 : 0));

        double d;
        try {
            d = Double.parseDouble(tested);
        } catch (NumberFormatException e) {
            return true;
        }

        if (d < 0 || d > 100) return true;

        this.threshold = d;
        return false;
    }

    @Override
    public void formatItem(ItemStack stack) {
        super.formatItem(stack);
        ArrayList<Component> loreList = new ArrayList<>(Objects.requireNonNull(stack.get(DataComponents.LORE)).lines());
        loreList.add(Component.literal(String.format("Durability threshold: %s%.2f%%", mode == Mode.AT_LEAST ? "≥" : "≤", threshold)).withStyle(LORE_STYLE));
        stack.set(DataComponents.LORE, new ItemLore(loreList));
    }

    @Override
    protected void addThresholdData(CompoundTag tag) {
        tag.putDouble("threshold", this.threshold);
    }

    @Override
    protected Double targetValue(ItemStack stack) {
        int damageTaken = stack.getDamageValue();
        int maxDamage = stack.getMaxDamage();
        if (maxDamage == 0) return switch (mode) {
            case AT_LEAST -> -1.0;
            case AT_MOST -> 101.0;
            default -> throw new IllegalStateException("Unexpected value: " + mode);
        };
        return Math.clamp(((double) (maxDamage - damageTaken) / (double) maxDamage) * 100.0, 0, 100);
    }
}

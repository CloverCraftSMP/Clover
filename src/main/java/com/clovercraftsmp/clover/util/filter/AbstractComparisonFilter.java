package com.clovercraftsmp.clover.util.filter;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public abstract class AbstractComparisonFilter<T extends Comparable<T>> extends Filter {
    protected T threshold;

    public AbstractComparisonFilter(String type, T threshold) {
        super(type, Mode.AT_LEAST);
        this.threshold = threshold;
    }

    public AbstractComparisonFilter(String type, CompoundTag tag, T threshold) {
        super(type, tag);
        this.threshold = threshold;
    }

    protected abstract void addThresholdData(CompoundTag tag);
    protected abstract T targetValue(ItemStack stack);

    @Override
    public final boolean test(ItemStack stack) {
        int cmp = targetValue(stack).compareTo(threshold);
        return switch (mode) {
            case AT_LEAST -> cmp >= 0;
            case AT_MOST -> cmp <= 0;
            case EXACT -> cmp == 0;
            default -> throw new IllegalStateException("Unexpected value: " + mode);
        };
    }

    @Override
    protected final void addFields(CompoundTag tag) {
        addThresholdData(tag);
    }

}

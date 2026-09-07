package com.clovercraftsmp.clover.util.filter;
//? if <=1.21.1 {
/*import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractCollectionFilter<T> extends Filter {
    public final List<T> entries = new ArrayList<>();

    public AbstractCollectionFilter(String type) {
        super(type, Mode.ANY);
    }

    public AbstractCollectionFilter(String type, CompoundTag tag) {
        super(type, tag);
    }

    public abstract void addEntries(CompoundTag tag);
    public abstract boolean matchesEntry(ItemStack item, T entry);

    @Override
    public final boolean test(ItemStack stack) {
        return switch (mode) {
            case ANY -> entries.stream().anyMatch(e -> matchesEntry(stack, e));
            case ALL -> entries.stream().allMatch(e -> matchesEntry(stack, e));
            case NONE -> entries.stream().noneMatch(e -> matchesEntry(stack, e));
            default -> throw new IllegalStateException("Unexpected value: " + mode);
        };
    }

    @Override
    protected final void addFields(CompoundTag tag) {
        addEntries(tag);
    }

}
*///?}
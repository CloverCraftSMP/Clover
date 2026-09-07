package com.clovercraftsmp.clover.util.filter;
//? if <=1.21.1 {
/*import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class ContentFilter extends Filter {
    public static final ContentFilter DEFAULT = new ContentFilter();
    public static final String TYPE = "content";

    private int slot = 0;
    private Filter subFilter;

    public ContentFilter() {
        super(TYPE, Mode.EMPTY);
    }

    public ContentFilter(CompoundTag tag) {
        super(TYPE, tag);
        this.slot = tag.getInt("slot");
        this.subFilter = tag.contains("filter", 10) ? Filter.fromCompound(tag.getCompound("filter")) : null;
    }

    @Override
    public boolean test(ItemStack stack) {
        ItemContainerContents container = stack.getComponents().get(DataComponents.CONTAINER);
        BundleContents bundle = stack.getComponents().get(DataComponents.BUNDLE_CONTENTS);

        if (container == null && bundle == null) return false;
        if (this.mode == Mode.EMPTY) return isEmpty(container, bundle);

        if (this.mode == Mode.SPECIFIC) {
            ItemStack target = get(container, bundle, slot);
            return target != null && (subFilter == null || subFilter.test(target));
        }

        Iterator<ItemStack> iter = iterator(container, bundle);

        return switch (this.mode) {
            case ANY -> match(iter, false);
            case ALL -> iter.hasNext() && match(iter, true);
            case NONE -> !match(iter, false);
            default -> throw new IllegalStateException("Unexpected value: " + this.mode);
        };
    }

    private boolean isEmpty(ItemContainerContents container, BundleContents bundle) {
        boolean containerEmpty = container == null || !container.nonEmptyItems().iterator().hasNext();
        boolean bundleEmpty = bundle == null || bundle.isEmpty();
        return containerEmpty && bundleEmpty;
    }

    private ItemStack get(ItemContainerContents container, BundleContents bundle, int slot) {
        if (container != null) {
            return container.stream().skip(slot).findFirst().orElse(null);
        } else {
            Iterator<ItemStack> iter = bundle.items().iterator();
            return skip(iter, slot) && iter.hasNext() ? iter.next() : null;
        }
    }

    private boolean skip(Iterator<ItemStack> stacks, int n) {
        for (int i = 0; i < n; i++) {
            if (!stacks.hasNext()) return false;
            stacks.next();
        }
        return true;
    }

    private Iterator<ItemStack> iterator(ItemContainerContents container, BundleContents bundle) {
        return container != null ? container.nonEmptyItems().iterator() : bundle.items().iterator();
    }

    private boolean match(Iterator<ItemStack> stacks, boolean all) {
        while (stacks.hasNext()) if (all ^ (subFilter == null || subFilter.test(stacks.next()))) return !all;
        return all;
    }

    @Override
    protected void addFields(CompoundTag tag) {
        tag.putInt("slot", slot);
        if (subFilter != null) tag.put("filter", subFilter.toCompound());
    }

    @Override
    public String getFilterName() {
        return "Content Filter";
    }

    @Override
    public int getColor() {
        return 0xFF7FFF;
    }

    @Override
    public Item getSmithingItem() {
        return Items.PINK_DYE;
    }

    @Override
    public List<Mode> allowedModes() {
        return List.of(Mode.ANY, Mode.ALL, Mode.NONE, Mode.SPECIFIC, Mode.EMPTY);
    }

    @Override
    public String getModeDescription(Mode mode) {
        return switch (mode) {
            case ANY -> "Passes if any slot in the target container matches the sub-filter.";
            case ALL -> "Passes if every slot in the target container matches the sub-filter.";
            case NONE -> "Passes if no slots in the target container match the sub-filter.";
            case SPECIFIC -> "Passes if the slot specified in the target container matches the sub-filter.";
            case EMPTY -> "Passes if the target container is empty.";
            default -> throw new IllegalStateException("Unexpected value: " + mode);
        };
    }

    @Override
    public boolean handleUnpack(ServerPlayer serverPlayer, Level level, ItemStack itemStack, InteractionHand interactionHand) {
        if (subFilter == null) return false;

        ItemStack newStack = new ItemStack(Items.WRITABLE_BOOK, 1);
        CompoundTag tag = new CompoundTag();
        tag.put(Filter.FILTER_PATH, subFilter.toCompound());
        newStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        subFilter.formatItem(newStack);

        level.addFreshEntity(new ItemEntity(level, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), newStack));
        subFilter = null;

        CompoundTag existingData = Objects.requireNonNull(itemStack.get(DataComponents.CUSTOM_DATA)).copyTag();
        existingData.put(Filter.FILTER_PATH, toCompound());
        itemStack.set(DataComponents.CUSTOM_DATA, CustomData.of(existingData));
        formatItem(itemStack);
        serverPlayer.setItemInHand(interactionHand, itemStack);

        serverPlayer.closeContainer();
        return true;
    }

    @Override
    public boolean add(Filter sacrifice) {
        if (subFilter == null) {
            subFilter = sacrifice;
            return true;
        }
        return false;
    }

    @Override
    public boolean changeFailed(String entry) {
        if (!super.changeFailed(entry)) return false;
        if (!entry.toLowerCase().startsWith("slot=")) return true;

        try {
            int newSlot = Integer.parseInt(entry.substring(5));
            if (newSlot < 1) return true;
            slot = newSlot - 1;
            return false;
        } catch (NumberFormatException e) {
            return true;
        }
    }

    @Override
    public void formatItem(ItemStack stack) {
        super.formatItem(stack);
        ArrayList<Component> loreList = new ArrayList<>(Objects.requireNonNull(stack.get(DataComponents.LORE)).lines());
        loreList.add(Component.literal(String.format("Slot: %s", (slot + 1))).withStyle(LORE_STYLE));
        loreList.add(Component.literal(String.format("Sub-filter: %s", subFilter == null ? "Unspecified." : subFilter.getFilterName())).withStyle(LORE_STYLE));
        stack.set(DataComponents.LORE, new ItemLore(loreList));
    }
}
*///?}
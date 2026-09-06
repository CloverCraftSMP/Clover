package com.clovercraftsmp.clover.util.filter;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GroupFilter extends AbstractCollectionFilter<Filter>  {
    public static final GroupFilter DEFAULT = new GroupFilter();
    public static final String TYPE = "group";

    public GroupFilter() {
        super(TYPE);
    }

    public GroupFilter(CompoundTag tag) {
        super(TYPE, tag);
        resolveFilters(tag);
    }

    private void resolveFilters(CompoundTag tag) {
        ListTag tagEntries = tag.getList("entries", 10);
        for (Tag tagEntry : tagEntries) {
            entries.add(Filter.fromCompound((CompoundTag) tagEntry));
        }
    }

    @Override
    public String getFilterName() {
        return "Group Filter";
    }

    @Override
    public int getColor() {
        return 0xFF0000;
    }

    @Override
    public Item getSmithingItem() {
        return Items.RED_DYE;
    }

    @Override
    public List<Mode> allowedModes() {
        return List.of(Mode.ANY, Mode.ALL, Mode.NONE);
    }

    @Override
    public String getModeDescription(Mode mode) {
        return switch (mode) {
            case ANY -> "Passes if any of the sub-filters match.";
            case ALL -> "Passes if all of the sub-filters match.";
            case NONE -> "Passes if none of the sub-filters match.";
            default -> throw new IllegalStateException("Unexpected value: " + mode);
        };
    }

    @Override
    public boolean handleUnpack(ServerPlayer serverPlayer, Level level, ItemStack itemStack, InteractionHand interactionHand) {
        if (entries.isEmpty()) return false;

        for (int i = entries.size() - 1; i >= 0; i--) {
            Filter subFilter = entries.remove(i);

            ItemStack newStack = new ItemStack(Items.WRITABLE_BOOK, 1);
            CompoundTag tag = new CompoundTag();
            tag.put(Filter.FILTER_PATH, subFilter.toCompound());
            newStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            subFilter.formatItem(newStack);

            level.addFreshEntity(new ItemEntity(level, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), newStack));
        }

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
        entries.add(sacrifice);
        return true;
    }

    @Override
    public void formatItem(ItemStack stack) {
        super.formatItem(stack);

        ArrayList<Component> loreList = new ArrayList<>(Objects.requireNonNull(stack.get(DataComponents.LORE)).lines());
        loreList.add(Component.literal("Sub-filters: ").append(entries.isEmpty() ? "None specified." : "").withStyle(LORE_STYLE));

        for (Filter subFilter : entries) {
            loreList.add(
                    Component.literal("• ")
                            .append(Component.literal(subFilter.getFilterName()))
                            .withStyle(LORE_STYLE)
            );
        }

        stack.set(DataComponents.LORE, new ItemLore(loreList));
    }

    @Override
    public void addEntries(CompoundTag tag) {
        ListTag list = new ListTag();
        for (Filter filter : this.entries) list.add(filter.toCompound());
        tag.put("entries", list);
    }

    @Override
    public boolean matchesEntry(ItemStack item, Filter entry) {
        return entry.test(item);
    }
}

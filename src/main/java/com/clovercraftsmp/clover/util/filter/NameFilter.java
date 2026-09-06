package com.clovercraftsmp.clover.util.filter;

import net.minecraft.ChatFormatting;
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

public class NameFilter extends Filter {
    public static final NameFilter DEFAULT = new NameFilter();
    public static final String TYPE = "name";

    private String value;
    private boolean caseSensitive;

    public NameFilter() {
        super(TYPE, Mode.CONTAINS);
        this.value = "";
        this.caseSensitive = false;
    }

    public NameFilter(CompoundTag tag) {
        super(TYPE, tag);
        this.value = tag.getString("value");
        this.caseSensitive = tag.getBoolean("sensitive");
    }

    @Override
    public boolean test(ItemStack stack) {
        String name = stack.getHoverName().getString();
        String tested = value;
        if (!caseSensitive) {
            name = name.toLowerCase();
            tested = tested.toLowerCase();
        }

        return switch (mode) {
            case CONTAINS -> name.contains(tested);
            case STARTS_WITH -> name.startsWith(tested);
            case ENDS_WITH -> name.endsWith(tested);
            case EXACT -> name.equals(tested);
            default -> throw new IllegalStateException("Unexpected value: " + mode);
        };
    }

    @Override
    protected void addFields(CompoundTag tag) {
        tag.putString("value", value);
        tag.putBoolean("sensitive", caseSensitive);
    }

    @Override
    public String getFilterName() {
        return "Name Filter";
    }

    @Override
    public int getColor() {
        return 0x007F7F;
    }

    @Override
    public Item getSmithingItem() {
        return Items.CYAN_DYE;
    }

    @Override
    public List<Mode> allowedModes() {
        return List.of(Mode.CONTAINS, Mode.STARTS_WITH, Mode.ENDS_WITH, Mode.EXACT);
    }

    @Override
    public String getModeDescription(Mode mode) {
        return switch (mode) {
            case CONTAINS -> "Passes if the item name contains the target value.";
            case STARTS_WITH -> "Passes if the item name starts with the target value.";
            case ENDS_WITH -> "Passes if the item name ends with the target value.";
            case EXACT -> "Passes if the item name equals the target value.";
            default -> throw new IllegalStateException("Unexpected value: " + mode);
        };
    }

    @Override
    public boolean changeFailed(String entry) {
        if (!super.changeFailed(entry)) return false;

        if (entry.toLowerCase().startsWith("value=")) {
            value = entry.substring(6);
            return false;
        }

        if (entry.toLowerCase().startsWith("sensitive=")) {
            String attempt = entry.substring(10).toLowerCase();
            if (!(attempt.equals("true") || attempt.equals("false"))) return true;
            caseSensitive = Boolean.parseBoolean(attempt);
            return false;
        }

        return true;
    }

    @Override
    public void formatItem(ItemStack stack) {
        super.formatItem(stack);
        ArrayList<Component> loreList = new ArrayList<>(Objects.requireNonNull(stack.get(DataComponents.LORE)).lines());
        loreList.add(Component.literal(String.format("Value: %s", value.isEmpty() ? "Unspecified." : "'" + value + "'")).withStyle(LORE_STYLE));
        loreList.add(
                Component.literal("Case sensitive: ")
                        .withStyle(LORE_STYLE)
                        .append(Component.literal(caseSensitive ? "✔" : "✘")
                                .withStyle(LORE_STYLE.withColor(caseSensitive ? ChatFormatting.GREEN : ChatFormatting.RED))
                        )
        );
        stack.set(DataComponents.LORE, new ItemLore(loreList));
    }
}

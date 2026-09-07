package com.clovercraftsmp.clover.util.filter;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TypeFilter extends AbstractCollectionFilter<Item> {
    public static final TypeFilter DEFAULT = new TypeFilter();
    public static final String TYPE = "type";

    public TypeFilter() {
        super(TYPE);
    }

    public TypeFilter(CompoundTag tag) {
        super(TYPE, tag);
        resolveIds(tag);
    }

    private void resolveIds(CompoundTag tag) {
        ListTag tagEntries = tag.getList("entries", 8);
        for (int i = 0; i < tagEntries.size(); i++) {
            String path = tagEntries.getString(i);
            Identifier loc = Identifier.parse(path);
            Item item = BuiltInRegistries.ITEM.get(loc);
            if (item == Items.AIR) continue;
            entries.add(item);
        }
    }

    @Override
    public String getFilterName() {
        return "Type Filter";
    }

    @Override
    public int getColor() {
        return 0x3F3FFF;
    }

    @Override
    public Item getSmithingItem() {
        return Items.BLUE_DYE;
    }

    @Override
    public List<Mode> allowedModes() {
        return List.of(Mode.ANY, Mode.NONE);
    }

    @Override
    public String getModeDescription(Mode mode) {
        return switch (mode) {
            case ANY -> "Passes if the item matches any type listed.";
            case NONE -> "Passes if the item matches none of the types listed.";
            default -> throw new IllegalStateException("Unexpected value: " + mode);
        };
    }

    @Override
    public boolean changeFailed(String entry) {
        if (!super.changeFailed(entry)) return false;
        if (!entry.toLowerCase().startsWith("item=")) return true;

        String itemAttempt = entry.substring(5);
        Identifier itemLocation = Identifier.tryParse(itemAttempt);
        if (itemLocation == null) return true;

        Item item = BuiltInRegistries.ITEM.get(itemLocation);
        if (item == Items.AIR) return true;

        if (!(entries.removeIf(element -> element == item))) {
            entries.add(item);
        }

        return false;
    }

    @Override
    public void formatItem(ItemStack stack) {
        super.formatItem(stack);

        ArrayList<Component> loreList = new ArrayList<>(Objects.requireNonNull(stack.get(DataComponents.LORE)).lines());
        loreList.add(Component.literal("Item types: ").append(entries.isEmpty() ? "None specified." : "").withStyle(LORE_STYLE));

        for (Item item : entries) {
            Identifier loc = BuiltInRegistries.ITEM.getKey(item);
            String modName = FabricLoader.getInstance().getModContainer(loc.getNamespace())
                    .map(e -> e.getMetadata().getName())
                    .orElse(loc.getNamespace());

            loreList.add(
                    Component.literal("• ")
                            .append(Component.translatable(item.getDescriptionId()))
                            .append(Component.literal(modName.equals("Minecraft") ? "" : String.format(" (%s)", modName)))
                            .withStyle(LORE_STYLE)
            );
        }

        stack.set(DataComponents.LORE, new ItemLore(loreList));
    }

    @Override
    public void addEntries(CompoundTag tag) {
        ListTag list = new ListTag();
        for (Item item : entries) {
            list.add(StringTag.valueOf(BuiltInRegistries.ITEM.getKey(item).toString()));
        }
        tag.put("entries", list);
    }

    @Override
    public boolean matchesEntry(ItemStack item, Item entry) {
        return item.is(entry);
    }
}

package com.clovercraftsmp.clover.util.filter;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TagFilter extends AbstractCollectionFilter<TagKey<Item>> {
    public static final TagFilter DEFAULT = new TagFilter();
    public static final String TYPE = "tag";

    public TagFilter() {
        super(TYPE);
    }

    public TagFilter(CompoundTag tag) {
        super(TYPE, tag);
        resolveIds(tag);
    }

    private void resolveIds(CompoundTag tag) {
        ListTag tagEntries =
                //? if <=1.21.1 {
                /*tag.getList("entries", 8);
                 *///? } else {
                tag.getList("entries")
                        .filter(e -> e.stream().allMatch(t -> t instanceof StringTag))
                        .orElse(new ListTag());
                //? }
        for (int i = 0; i < tagEntries.size(); i++) {
            String path = tagEntries.getString(i)/*? if >1.21.1 {*/.orElse("")/*?}*/;
            Identifier loc = Identifier.parse(path);
            TagKey<Item> tagKey = TagKey.create(Registries.ITEM, loc);
            //? if <=1.21.1 {
            /*if (BuiltInRegistries.ITEM.getTag(tagKey).isEmpty()) continue;
            *///? } else {
            if (BuiltInRegistries.ITEM.get(tagKey).isEmpty()) continue;
            //? }
            entries.add(tagKey);
        }
    }

    @Override
    public String getFilterName() {
        return "Tag Filter";
    }

    @Override
    public int getColor() {
        return 0x7FBFFF;
    }

    @Override
    public Item getSmithingItem() {
        return Items.LIGHT_BLUE_DYE;
    }

    @Override
    public List<Mode> allowedModes() {
        return List.of(Mode.ANY, Mode.ALL, Mode.NONE);
    }

    @Override
    public String getModeDescription(Mode mode) {
        return switch (mode) {
            case ANY -> "Passes if the item matches any tag listed.";
            case ALL -> "Passes if the item matches all tags listed.";
            case NONE -> "Passes if the item matches none of the tags listed.";
            default -> throw new IllegalStateException("Unexpected value: " + mode);
        };
    }

    @Override
    public boolean changeFailed(String entry) {
        if (!super.changeFailed(entry)) return false;
        if (!entry.toLowerCase().startsWith("tag=")) return true;

        String tagAttempt = entry.substring(4);
        if (tagAttempt.startsWith("#")) tagAttempt = tagAttempt.substring(1);
        Identifier tagLocation = Identifier.tryParse(tagAttempt);
        if (tagLocation == null) return true;

        TagKey<Item> tagKey = TagKey.create(Registries.ITEM, tagLocation);
        //? if <=1.21.1 {
        /*if (BuiltInRegistries.ITEM.getTag(tagKey).isEmpty()) return true;
         *///? } else {
        if (BuiltInRegistries.ITEM.get(tagKey).isEmpty()) return true;
        //? }

        if (!(entries.removeIf(tag -> tag.location().equals(tagLocation)))) {
            entries.add(tagKey);
        }

        return false;
    }

    @Override
    public void formatItem(ItemStack stack) {
        super.formatItem(stack);

        ArrayList<Component> loreList = new ArrayList<>(Objects.requireNonNull(stack.get(DataComponents.LORE)).lines());
        loreList.add(Component.literal("Tags: ").append(entries.isEmpty() ? "None specified." : "").withStyle(LORE_STYLE));

        for (TagKey<Item> tag : entries) {
            Identifier loc = tag.location();
            String modName = FabricLoader.getInstance().getModContainer(loc.getNamespace())
                    .map(e -> e.getMetadata().getName())
                    .orElse(loc.getNamespace());

            String tagTranslate = String.format("tag.item.%s.%s", loc.getNamespace(), loc.getPath().replace('/','.'));

            loreList.add(
                    Component.literal("• ")
                            .append(Component.translatableWithFallback(tagTranslate, loc.getPath()))
                            .append(Component.literal(modName.equals("Minecraft") ? "" : String.format(" (%s)", modName)))
                            .withStyle(LORE_STYLE)
            );
        }

        stack.set(DataComponents.LORE, new ItemLore(loreList));
    }

    @Override
    public void addEntries(CompoundTag tag) {
        ListTag list = new ListTag();
        for (TagKey<Item> entry : entries) list.add(StringTag.valueOf(entry.location().toString()));
        tag.put("entries", list);
    }

    @Override
    public boolean matchesEntry(ItemStack stack, TagKey<Item> entry) {
        return stack.is(entry);
    }
}

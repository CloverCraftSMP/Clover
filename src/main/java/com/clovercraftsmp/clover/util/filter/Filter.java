package com.clovercraftsmp.clover.util.filter;
//? if <=1.21.1 {
/*import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

public abstract class Filter {
    public enum Mode {
        ANY,
        ALL,
        NONE,
        AT_LEAST,
        AT_MOST,
        EXACT,
        SPECIFIC,
        EMPTY,
        CONTAINS,
        STARTS_WITH,
        ENDS_WITH
    }

    public static final String FILTER_PATH = "clover:filter_data";
    static final Style LORE_STYLE = Style.EMPTY.withItalic(false).withColor(ChatFormatting.WHITE);
    private static final Map<String, Function<CompoundTag, Filter>> REGISTRY = new HashMap<>();
    private final String type;

    Mode mode;

    public Filter(String type, Mode mode) {
        this.type = type;
        this.mode = mode;
    }

    public Filter(String type, CompoundTag tag) {
        this.type = type;
        Mode retrievedMode = Mode.valueOf(tag.getString("mode"));
        List<Mode> allowed = this.allowedModes();
        if (!this.allowedModes().contains(retrievedMode)) retrievedMode = allowed.getFirst();
        this.mode = retrievedMode;
    }

    public abstract boolean test(ItemStack stack);
    protected abstract void addFields(CompoundTag tag);
    public abstract String getFilterName();
    public abstract int getColor();
    public abstract Item getSmithingItem();
    public abstract List<Mode> allowedModes();
    public abstract String getModeDescription(Mode mode);

    public static List<Filter> craftableFilters() {
        return List.of(
                ContentFilter.DEFAULT,
                DurabilityFilter.DEFAULT,
                EnchantmentFilter.DEFAULT,
                GroupFilter.DEFAULT,
                TagFilter.DEFAULT,
                NameFilter.DEFAULT,
                TypeFilter.DEFAULT
        );
    }

    public static void registerFilters() {
        register(ContentFilter.TYPE, ContentFilter::new);
        register(DurabilityFilter.TYPE, DurabilityFilter::new);
        register(EnchantmentFilter.TYPE, EnchantmentFilter::new);
        register(GroupFilter.TYPE, GroupFilter::new);
        register(TagFilter.TYPE, TagFilter::new);
        register(NameFilter.TYPE, NameFilter::new);
        register(TypeFilter.TYPE, TypeFilter::new);
    }

    private static void register(String type, Function<CompoundTag, Filter> factory) {
        REGISTRY.put(type, factory);
    }

    public static boolean isFilter(ItemStack stack) {
        CompoundTag tag = Optional.ofNullable(stack.get(DataComponents.CUSTOM_DATA))
                .map(CustomData::copyTag)
                .orElse(null);

        return tag != null && tag.contains(FILTER_PATH);
    }

    public static @Nullable Filter fromItem(ItemStack stack) {
        if (!isFilter(stack)) return null;
        return Filter.fromCompound(Objects.requireNonNull(stack.get(DataComponents.CUSTOM_DATA)).copyTag().getCompound(FILTER_PATH));
    }

    public static Filter fromCompound(CompoundTag tag) {
        String type = tag.getString("type");
        Function<CompoundTag, Filter> factory = REGISTRY.get(type);
        if (factory == null) throw new RuntimeException("Unexpected type found when parsing filter: " + type);
        return factory.apply(tag);
    }

    public CompoundTag toCompound() {
        CompoundTag tag = new CompoundTag();
        tag.putString("type", type);
        tag.putString("mode", mode.name());
        addFields(tag);
        return tag;
    }

    public boolean handleUnpack(ServerPlayer serverPlayer, Level level, ItemStack itemStack, InteractionHand interactionHand) {
        return false;
    }

    public boolean add(Filter sacrifice) {
        return false;
    }

    public boolean changeFailed(String entry) {
        if (!entry.toLowerCase().startsWith("mode=")) return true;
        Mode newMode;
        try {
            newMode = Mode.valueOf(entry.substring(5).toUpperCase().replace(' ', '_'));
        } catch (IllegalArgumentException e) {
            return true;
        }

        if (!this.allowedModes().contains(newMode)) return true;

        this.mode = newMode;
        return false;
    }

    private static String prettyName(Mode mode) {
        String modeName = mode.name().toLowerCase().replace('_', ' ');
        modeName = modeName.substring(0, 1).toUpperCase() + modeName.substring(1);
        return modeName;
    }

    public void formatItem(ItemStack stack) {
        List<Component> loreList = new ArrayList<>(List.of(
                Component.literal(String.format("Current Mode: %s", prettyName(mode))).withStyle(LORE_STYLE),
                Component.literal(getModeDescription(mode)).withStyle(LORE_STYLE),
                Component.empty(),
                Component.literal("Allowed modes: ").withStyle(LORE_STYLE)
        ));

        allowedModes().forEach(allowedMode -> loreList.add(Component.literal(String.format("• %s", prettyName(allowedMode))).withStyle(LORE_STYLE)));

        loreList.add(Component.empty());

        stack.set(DataComponents.ITEM_NAME, Component.literal(getFilterName()).withStyle(Style.EMPTY.withItalic(false).withColor(getColor())));
        stack.set(DataComponents.LORE, new ItemLore(loreList));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        stack.remove(DataComponents.WRITABLE_BOOK_CONTENT);
    }
}
*///?}

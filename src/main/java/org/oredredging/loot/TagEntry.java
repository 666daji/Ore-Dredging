package org.oredredging.loot;

import com.google.gson.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.entry.LeafEntry;
import net.minecraft.loot.entry.LootPoolEntry;
import net.minecraft.loot.entry.LootPoolEntryType;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.oredredging.registry.ModLootPoolEntryTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 战利品表条目：从指定物品标签中均匀随机选取一个物品生成。
 * 支持可选的排除列表，排除标签中的某些物品。
 */
public class TagEntry extends LeafEntry {
    private final TagKey<Item> tag;
    private final List<Item> exclusions; // 排除的物品列表

    public TagEntry(TagKey<Item> tag, List<Item> exclusions, int weight, int quality, LootCondition[] conditions, LootFunction[] functions) {
        super(weight, quality, conditions, functions);
        this.tag = tag;
        this.exclusions = exclusions != null ? exclusions : List.of();
    }

    @Override
    public LootPoolEntryType getType() {
        return ModLootPoolEntryTypes.TAG_ITEM;
    }

    @Override
    public void generateLoot(Consumer<ItemStack> lootConsumer, LootContext context) {
        // 获取标签中的所有物品
        Optional<RegistryEntryList.Named<Item>> optional = Registries.ITEM.getEntryList(tag);
        if (optional.isEmpty()) return;

        List<Item> items = optional.get().stream()
                .map(RegistryEntry::value)
                .collect(Collectors.toList());

        // 移除排除的物品
        items.removeAll(exclusions);

        // 如果结果为空，不生成任何物品
        if (items.isEmpty()) return;

        // 随机选择一个物品
        int index = context.getRandom().nextInt(items.size());
        lootConsumer.accept(new ItemStack(items.get(index)));
    }

    /**
     * 创建构建器
     */
    public static Builder<?> builder(TagKey<Item> tag) {
        return new Builder<>(tag);
    }

    /**
     * 构建器类，支持设置排除列表
     */
    public static class Builder<T extends Builder<T>> extends LeafEntry.Builder<T> {
        private final TagKey<Item> tag;
        private final List<Item> exclusions = new ArrayList<>();

        public Builder(TagKey<Item> tag) {
            this.tag = tag;
        }

        @Override
        protected T getThisBuilder() {
            return (T) this;
        }

        /**
         * 添加要排除的物品（可变参数）
         */
        public T exclude(ItemConvertible... items) {
            for (ItemConvertible item : items) {
                exclusions.add(item.asItem());
            }
            return (T) this;
        }

        /**
         * 设置排除列表（覆盖原有）
         */
        public T exclude(List<Item> items) {
            this.exclusions.clear();
            this.exclusions.addAll(items);
            return (T) this;
        }

        @Override
        public LootPoolEntry build() {
            return new TagEntry(tag, exclusions, weight, quality, getConditions(), getFunctions());
        }

        @Override
        public T getThisFunctionConsumingBuilder() {
            return getThisBuilder();
        }
    }

    /**
     * 序列化器，用于 JSON 与 Java 对象的转换
     */
    public static class Serializer extends LeafEntry.Serializer<TagEntry> {
        @Override
        public void addEntryFields(JsonObject json, TagEntry entry, JsonSerializationContext context) {
            super.addEntryFields(json, entry, context);
            json.addProperty("tag", entry.tag.id().toString());
            if (!entry.exclusions.isEmpty()) {
                JsonArray excludeArray = new JsonArray();
                for (Item item : entry.exclusions) {
                    Identifier id = Registries.ITEM.getId(item);
                    excludeArray.add(id.toString());
                }
                json.add("exclude", excludeArray);
            }
        }

        @Override
        protected TagEntry fromJson(JsonObject json, JsonDeserializationContext context, int weight, int quality, LootCondition[] conditions, LootFunction[] functions) {
            // 解析标签
            Identifier tagId = new Identifier(JsonHelper.getString(json, "tag"));
            TagKey<Item> tag = TagKey.of(RegistryKeys.ITEM, tagId);

            // 解析排除列表（可选）
            List<Item> exclusions = new ArrayList<>();
            if (json.has("exclude") && json.get("exclude").isJsonArray()) {
                JsonArray excludeArray = JsonHelper.getArray(json, "exclude");
                for (int i = 0; i < excludeArray.size(); i++) {
                    String itemIdStr = excludeArray.get(i).getAsString();
                    Identifier itemId = Identifier.tryParse(itemIdStr);
                    if (itemId == null) {
                        throw new JsonSyntaxException("Invalid item ID: " + itemIdStr);
                    }
                    Item item = Registries.ITEM.get(itemId);
                    exclusions.add(item);
                }
            }

            return new TagEntry(tag, exclusions, weight, quality, conditions, functions);
        }
    }
}
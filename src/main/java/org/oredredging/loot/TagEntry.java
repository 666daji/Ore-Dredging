package org.oredredging.loot;

import com.google.gson.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.registries.ForgeRegistries;
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
public class TagEntry extends LootPoolSingletonContainer {
    private final TagKey<Item> tag;
    private final List<Item> exclusions;

    protected TagEntry(TagKey<Item> tag, List<Item> exclusions, int weight, int quality, LootItemCondition[] conditions, LootItemFunction[] functions) {
        super(weight, quality, conditions, functions);
        this.tag = tag;
        this.exclusions = exclusions != null ? exclusions : List.of();
    }

    @Override
    public LootPoolEntryType getType() {
        return ModLootPoolEntryTypes.TAG_ITEM.get();
    }

    @Override
    protected void createItemStack(Consumer<ItemStack> consumer, LootContext context) {
        // 获取标签中的所有物品
        Optional<net.minecraft.core.HolderSet.Named<Item>> optional = context.getLevel().registryAccess()
                .registryOrThrow(Registries.ITEM).getTag(tag);
        if (optional.isEmpty()) return;

        List<Item> items = optional.get().stream()
                .map(holder -> holder.value())
                .collect(Collectors.toList());

        // 移除排除的物品
        items.removeAll(exclusions);

        if (items.isEmpty()) return;

        int index = context.getRandom().nextInt(items.size());
        consumer.accept(new ItemStack(items.get(index)));
    }

    /**
     * 创建构建器
     */
    public static Builder<?> builder(TagKey<Item> tag) {
        return new Builder<>(tag);
    }

    /**
     * 构建器类
     */
    public static class Builder<T extends Builder<T>> extends LootPoolSingletonContainer.Builder<T> {
        private final TagKey<Item> tag;
        private final List<Item> exclusions = new ArrayList<>();

        public Builder(TagKey<Item> tag) {
            this.tag = tag;
        }

        @Override
        protected T getThis() {
            return (T) this;
        }

        /**
         * 添加要排除的物品（可变参数）
         */
        public T exclude(Item... items) {
            for (Item item : items) {
                exclusions.add(item);
            }
            return getThis();
        }

        /**
         * 设置排除列表（覆盖原有）
         */
        public T exclude(List<Item> items) {
            this.exclusions.clear();
            this.exclusions.addAll(items);
            return getThis();
        }

        @Override
        public LootPoolSingletonContainer build() {
            return new TagEntry(tag, exclusions, weight, quality, getConditions(), getFunctions());
        }
    }

    /**
     * 序列化器
     */
    public static class Serializer extends LootPoolSingletonContainer.Serializer<TagEntry> {
        @Override
        public void serializeCustom(JsonObject json, TagEntry entry, JsonSerializationContext context) {
            super.serializeCustom(json, entry, context);
            json.addProperty("tag", entry.tag.location().toString());
            if (!entry.exclusions.isEmpty()) {
                JsonArray excludeArray = new JsonArray();
                for (Item item : entry.exclusions) {
                    ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
                    excludeArray.add(id.toString());
                }
                json.add("exclude", excludeArray);
            }
        }

        @Override
        protected TagEntry deserialize(JsonObject json, JsonDeserializationContext context, int weight, int quality, LootItemCondition[] conditions, LootItemFunction[] functions) {
            ResourceLocation tagId = ResourceLocation.tryParse(GsonHelper.getAsString(json, "tag"));
            TagKey<Item> tag = TagKey.create(Registries.ITEM, tagId);

            List<Item> exclusions = new ArrayList<>();
            if (json.has("exclude") && json.get("exclude").isJsonArray()) {
                JsonArray excludeArray = GsonHelper.getAsJsonArray(json, "exclude");
                for (int i = 0; i < excludeArray.size(); i++) {
                    String itemIdStr = excludeArray.get(i).getAsString();
                    ResourceLocation itemId = ResourceLocation.tryParse(itemIdStr);
                    if (itemId == null) {
                        throw new JsonSyntaxException("Invalid item ID: " + itemIdStr);
                    }
                    Item item = ForgeRegistries.ITEMS.getValue(itemId);
                    if (item == null) {
                        throw new JsonSyntaxException("Unknown item: " + itemId);
                    }
                    exclusions.add(item);
                }
            }

            return new TagEntry(tag, exclusions, weight, quality, conditions, functions);
        }
    }
}
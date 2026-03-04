package org.oredredging.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
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
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.MathHelper;
import org.oredredging.registry.ModLootPoolEntryTypes;

import java.util.function.Consumer;

/**
 * 带概率控制的物品条目，当被选中时，只有指定概率实际生成物品。
 * 概率由 probability 字段指定，范围 0~10000（分母固定为10000）。
 */
public class ProbabilityItemEntry extends LeafEntry {
    private final Item item;
    private final int probability; // 0~10000，分子

    public ProbabilityItemEntry(Item item, int weight, int quality, LootCondition[] conditions, LootFunction[] functions, int probability) {
        super(weight, quality, conditions, functions);
        this.item = item;
        this.probability = MathHelper.clamp(probability, 0, 10000);
    }

    @Override
    public LootPoolEntryType getType() {
        return ModLootPoolEntryTypes.PROBABILITY_ITEM;
    }

    @Override
    public void generateLoot(Consumer<ItemStack> lootConsumer, LootContext context) {
        // 根据概率决定是否生成物品
        if (probability >= 10000 || (probability > 0 && context.getRandom().nextInt(10000) < probability)) {
            lootConsumer.accept(new ItemStack(item));
        }
    }

    /**
     * 创建构建器
     */
    public static Builder<?> builder(ItemConvertible item) {
        return new Builder<>(item.asItem());
    }

    /**
     * 构建器类，支持设置概率
     */
    public static class Builder<T extends Builder<T>> extends LeafEntry.Builder<T> {
        private final Item item;
        private int probability = 10000; // 默认100%

        public Builder(Item item) {
            this.item = item;
        }

        @Override
        protected T getThisBuilder() {
            return (T) this;
        }

        /**
         * 设置概率（分子，分母固定10000）
         */
        public T probability(int probability) {
            this.probability = probability;
            return (T) this;
        }

        @Override
        public LootPoolEntry build() {
            return new ProbabilityItemEntry(item, weight, quality, getConditions(), getFunctions(), probability);
        }

        @Override
        public T getThisFunctionConsumingBuilder() {
            return getThisBuilder();
        }
    }

    /**
     * 序列化器，用于 JSON 与 Java 对象的转换
     */
    public static class Serializer extends LeafEntry.Serializer<ProbabilityItemEntry> {
        @Override
        public void addEntryFields(JsonObject jsonObject, ProbabilityItemEntry entry, JsonSerializationContext context) {
            super.addEntryFields(jsonObject, entry, context);

            // 写入物品ID
            Identifier id = Registries.ITEM.getId(entry.item);
            jsonObject.addProperty("name", id.toString());

            // 如果概率不是默认值10000，则写入
            if (entry.probability != 10000) {
                jsonObject.addProperty("probability", entry.probability);
            }
        }

        @Override
        protected ProbabilityItemEntry fromJson(JsonObject json, JsonDeserializationContext context, int weight, int quality, LootCondition[] conditions, LootFunction[] functions) {
            Item item = JsonHelper.getItem(json, "name");
            int probability = JsonHelper.getInt(json, "probability", 10000);
            return new ProbabilityItemEntry(item, weight, quality, conditions, functions, probability);
        }
    }
}
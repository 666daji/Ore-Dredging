package org.oredredging.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.registries.ForgeRegistries;
import org.oredredging.item.CrushedDropGain;
import org.oredredging.registry.ModLootPoolEntryTypes;

import java.util.function.Consumer;

/**
 * 带概率控制的物品条目，当被选中时，只有指定概率实际生成物品。
 * 概率由 probability 字段指定，范围 0~10000（分母固定为10000）。
 */
public class ProbabilityItemEntry extends LootPoolSingletonContainer {
    private final Item item;
    private final int probability; // 0~10000，分子

    protected ProbabilityItemEntry(Item item, int weight, int quality, LootItemCondition[] conditions, LootItemFunction[] functions, int probability) {
        super(weight, quality, conditions, functions);
        this.item = item;
        this.probability = Mth.clamp(probability, 0, 10000);
    }

    @Override
    public LootPoolEntryType getType() {
        return ModLootPoolEntryTypes.PROBABILITY_ITEM.get();
    }

    @Override
    protected void createItemStack(Consumer<ItemStack> consumer, LootContext context) {
        int probability = this.probability;
        ItemStack tool = context.getParamOrNull(LootContextParams.TOOL);

        // 依据物品获取概率增益
        if (tool != null && tool.getItem() instanceof CrushedDropGain gainItem) {
            probability = gainItem.getProbability(probability);
        }

        // 根据概率决定是否生成物品
        if (probability >= 10000 || (probability > 0 && context.getRandom().nextInt(10000) < probability)) {
            consumer.accept(new ItemStack(item));
        }
    }

    /**
     * 创建构建器
     */
    public static Builder<?> builder(Item item) {
        return new Builder<>(item);
    }

    /**
     * 构建器类，支持设置概率
     */
    public static class Builder<T extends Builder<T>> extends LootPoolSingletonContainer.Builder<T> {
        private final Item item;
        private int probability = 10000; // 默认100%

        public Builder(Item item) {
            this.item = item;
        }

        @Override
        protected T getThis() {
            return (T) this;
        }

        /**
         * 设置概率（分子，分母固定10000）
         */
        public T probability(int probability) {
            this.probability = probability;
            return getThis();
        }

        @Override
        public LootPoolSingletonContainer build() {
            return new ProbabilityItemEntry(item, weight, quality, getConditions(), getFunctions(), probability);
        }
    }

    public static class Serializer extends LootPoolSingletonContainer.Serializer<ProbabilityItemEntry> {
        @Override
        public void serializeCustom(JsonObject json, ProbabilityItemEntry entry, JsonSerializationContext context) {
            super.serializeCustom(json, entry, context);
            json.addProperty("name", ForgeRegistries.ITEMS.getKey(entry.item).toString());
            if (entry.probability != 10000) {
                json.addProperty("probability", entry.probability);
            }
        }

        @Override
        protected ProbabilityItemEntry deserialize(JsonObject json, JsonDeserializationContext context, int weight, int quality, LootItemCondition[] conditions, LootItemFunction[] functions) {
            Item item = GsonHelper.getAsItem(json, "name");
            int probability = GsonHelper.getAsInt(json, "probability", 10000);
            return new ProbabilityItemEntry(item, weight, quality, conditions, functions, probability);
        }
    }
}
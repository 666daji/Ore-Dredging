package org.oredredging.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.oredredging.registry.ModEnchantments;
import org.oredredging.registry.ModItems;
import org.oredredging.registry.ModLootFunctionTypes;

/**
 * 战利品函数：根据时运等级调整压碎物品的掉落数量分布。
 */
public class CrushedDropCountFunction extends LootItemConditionalFunction {
    private static final double[] DEFAULT_PROBABILITIES = new double[]{0.25, 0.35, 0.10, 0.30};
    private static final int[] DEFAULT_MIN_MAX = new int[]{1, 2};

    /** 长度为4，依次对应 [1-2个, 3个, 4个, 5个] 的概率 */
    private final double[] probabilities;
    /** 长度为2，表示第一项的最小和最大数量 */
    private final int[] minMax;

    protected CrushedDropCountFunction(LootItemCondition[] conditions, double[] probabilities, int[] minMax) {
        super(conditions);
        this.probabilities = probabilities;
        this.minMax = minMax;
    }

    @Override
    public LootItemFunctionType getType() {
        return ModLootFunctionTypes.CRUSHED_DROP_COUNT.get();
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        // 获取时运等级
        int fortuneLevel = 0;
        ItemStack tool = context.getParamOrNull(LootContextParams.TOOL);
        if (tool != null && !tool.isEmpty()) {
            fortuneLevel = tool.getEnchantmentLevel(Enchantments.BLOCK_FORTUNE);
        }

        // 根据时运等级调整概率
        double[] adjustedProbs = adjustProbabilitiesForFortune(probabilities, fortuneLevel);

        // 生成随机数量
        int count = generateCount(context.getRandom(), adjustedProbs, minMax);

        // 设置物品数量
        stack.setCount(count);

        // 附魔
        if (stack.is(ModItems.ARMOR_FRAGMENTS.get())) {
            stack.enchant(ModEnchantments.UNYIELDING.get(), 1);
        }

        return stack;
    }

    /**
     * 根据时运等级调整概率分布。
     * 每级时运将“1-2个”的概率减少5%，将“5个”的概率增加5%，并保持总和为1。
     */
    private double[] adjustProbabilitiesForFortune(double[] baseProbs, int fortuneLevel) {
        if (fortuneLevel <= 0) {
            return baseProbs.clone();
        }

        double[] adjusted = baseProbs.clone();
        double decreasePerLevel = 0.05;
        double increasePerLevel = 0.05;

        double maxDecrease = Math.min(decreasePerLevel * fortuneLevel, adjusted[0]);
        double maxIncrease = Math.min(increasePerLevel * fortuneLevel, 1.0 - adjusted[3]);

        adjusted[0] -= maxDecrease;
        adjusted[3] += maxIncrease;

        double sum = adjusted[0] + adjusted[1] + adjusted[2] + adjusted[3];
        if (Math.abs(sum - 1.0) > 1e-6) {
            adjusted[1] += (1.0 - sum);
        }

        return adjusted;
    }

    /**
     * 根据概率生成随机数量。
     */
    private int generateCount(net.minecraft.util.RandomSource random, double[] probs, int[] minMax) {
        float r = random.nextFloat();
        double cumulative = 0.0;
        for (int i = 0; i < probs.length; i++) {
            cumulative += probs[i];
            if (r < cumulative) {
                if (i == 0) {
                    return random.nextInt(minMax[1] - minMax[0] + 1) + minMax[0];
                } else {
                    return i + 2; // i=1->3, i=2->4, i=3->5
                }
            }
        }
        return 1;
    }

    /**
     * 构建器
     */
    public static Builder<?> builder() {
        return new Builder<>();
    }

    public static class Builder<T extends Builder<T>> extends LootItemConditionalFunction.Builder<T> {
        private double[] probabilities = DEFAULT_PROBABILITIES;
        private int[] minMax = DEFAULT_MIN_MAX;

        public Builder<T> probabilities(double[] probabilities) {
            this.probabilities = probabilities;
            return this;
        }

        public Builder<T> minMax(int min, int max) {
            this.minMax = new int[]{min, max};
            return this;
        }

        @Override
        protected T getThis() {
            return (T) this;
        }

        @Override
        public LootItemFunction build() {
            return new CrushedDropCountFunction(getConditions(), probabilities, minMax);
        }
    }

    /**
     * 序列化器
     */
    public static class Serializer extends LootItemConditionalFunction.Serializer<CrushedDropCountFunction> {
        @Override
        public void serialize(JsonObject json, CrushedDropCountFunction function, JsonSerializationContext context) {
            super.serialize(json, function, context);
            json.add("probabilities", context.serialize(function.probabilities));
            json.add("minMax", context.serialize(function.minMax));
        }

        @Override
        public CrushedDropCountFunction deserialize(JsonObject json, JsonDeserializationContext context, LootItemCondition[] conditions) {
            double[] probabilities = DEFAULT_PROBABILITIES;
            if (json.has("probabilities")) {
                probabilities = context.deserialize(json.get("probabilities"), double[].class);
            }
            int[] minMax = DEFAULT_MIN_MAX;
            if (json.has("minMax")) {
                minMax = context.deserialize(json.get("minMax"), int[].class);
            }
            return new CrushedDropCountFunction(conditions, probabilities, minMax);
        }
    }
}
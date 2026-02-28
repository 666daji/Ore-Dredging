package org.oredredging.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.util.JsonSerializer;
import net.minecraft.util.math.random.Random;
import org.oredredging.registry.ModLootFunctionTypes;

public class CrushedDropCountFunction implements LootFunction {
    private static final double[] DEFAULT_PROBABILITIES = new double[]{0.25, 0.35, 0.10, 0.30};
    private static final int[] DEFAULT_MIN_MAX = new int[]{1, 2};

    /** 长度为4，依次对应 [1-2个, 3个, 4个, 5个] 的概率 */
    private final double[] probabilities;
    /** 长度为2，表示第一项的最小和最大数量 */
    private final int[] minMax;

    public CrushedDropCountFunction(double[] probabilities, int[] minMax) {
        this.probabilities = probabilities;
        this.minMax = minMax;
    }

    @Override
    public LootFunctionType getType() {
        return ModLootFunctionTypes.CRUSHED_DROP_COUNT;
    }

    @Override
    public ItemStack apply(ItemStack stack, LootContext context) {
        // 获取时运等级
        int fortuneLevel = 0;
        if (context.hasParameter(LootContextParameters.TOOL)) {
            ItemStack tool = context.get(LootContextParameters.TOOL);
            if (tool != null && !tool.isEmpty()) {
                fortuneLevel = EnchantmentHelper.getLevel(Enchantments.SILK_TOUCH, tool);
            }
        }

        // 根据时运等级调整概率
        double[] adjustedProbs = adjustProbabilitiesForFortune(probabilities, fortuneLevel);

        // 生成随机数量
        int count = generateCount(context.getRandom(), adjustedProbs, minMax);

        // 设置物品数量并返回
        stack.setCount(count);
        return stack;
    }

    /**
     * 根据时运等级调整概率分布。
     * <p>每级时运将“1-2个”的概率减少5%，将“5个”的概率增加5%，并保持总和为1。</p>
     *
     * @param baseProbs 基础概率
     * @param fortuneLevel 时运等级
     * @return 调整后的概率分布
     */
    private double[] adjustProbabilitiesForFortune(double[] baseProbs, int fortuneLevel) {
        if (fortuneLevel <= 0) {
            return baseProbs.clone();
        }

        double[] adjusted = baseProbs.clone();
        double decreasePerLevel = 0.05;
        double increasePerLevel = 0.05;

        // 计算可调整的最大值（不能低于0或超过1）
        double maxDecrease = Math.min(decreasePerLevel * fortuneLevel, adjusted[0]);
        double maxIncrease = Math.min(increasePerLevel * fortuneLevel, 1.0 - adjusted[3]);

        adjusted[0] -= maxDecrease;
        adjusted[3] += maxIncrease;

        // 由于调整可能造成总和偏离1，将差值补偿给中间项（这里补偿给第二项）
        double sum = adjusted[0] + adjusted[1] + adjusted[2] + adjusted[3];
        if (Math.abs(sum - 1.0) > 1e-6) {
            adjusted[1] += (1.0 - sum);
        }

        return adjusted;
    }

    /**
     * 根据调整后的概率生成随机数量。
     *
     * @return 最终数量
     */
    private int generateCount(Random random, double[] probs, int[] minMax) {
        float r = random.nextFloat();
        double cumulative = 0.0;
        for (int i = 0; i < probs.length; i++) {
            cumulative += probs[i];
            if (r < cumulative) {
                if (i == 0) {
                    // 均匀掉落 [min, max] 区间内的整数
                    return random.nextInt(minMax[1] - minMax[0] + 1) + minMax[0];
                } else {
                    // i=1 -> 3个, i=2 -> 4个, i=3 -> 5个
                    return i + 2;
                }
            }
        }
        // 极罕见的浮点误差回退
        return 1;
    }

    public static class Serializer implements JsonSerializer<CrushedDropCountFunction> {
        @Override
        public void toJson(JsonObject json, CrushedDropCountFunction function, JsonSerializationContext context) {
            json.add("probabilities", context.serialize(function.probabilities));
            json.add("minMax", context.serialize(function.minMax));
        }

        @Override
        public CrushedDropCountFunction fromJson(JsonObject json, JsonDeserializationContext context) {
            double[] probabilities = DEFAULT_PROBABILITIES;
            if (json.has("probabilities")) {
                probabilities = context.deserialize(json.get("probabilities"), double[].class);
            }
            int[] minMax = DEFAULT_MIN_MAX;
            if (json.has("minMax")) {
                minMax = context.deserialize(json.get("minMax"), int[].class);
            }
            return new CrushedDropCountFunction(probabilities, minMax);
        }
    }
}
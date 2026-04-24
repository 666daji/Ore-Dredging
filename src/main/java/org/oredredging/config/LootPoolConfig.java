package org.oredredging.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.jetbrains.annotations.NotNull;
import org.oredredging.registry.ModItems;

import java.util.List;

public record LootPoolConfig(List<PoolEntry> entries) {
    public static final Codec<LootPoolConfig> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.list(PoolEntry.CODEC).fieldOf("entries").forGetter(LootPoolConfig::entries)
            ).apply(instance, LootPoolConfig::new)
    );

    public static final LootPoolConfig DEFAULT = new LootPoolConfig(List.of(
            new PoolEntry(new ItemStack(ModItems.RAW_IRON_NUGGET), 0.6f, 4000),
            new PoolEntry(new ItemStack(Items.GOLD_INGOT), 0.3f, 8000),
            new PoolEntry(new ItemStack(Items.DIAMOND), 0.1f, 16000)
    ));

    public LootPoolConfig(List<PoolEntry> entries) {
        List<PoolEntry> valid = entries.stream()
                .filter(PoolEntry::isValid)
                .toList();
        if (valid.isEmpty()) {
            throw new RuntimeException("Pool entry all are invalid");
        }

        this.entries = valid;
    }

    public static int minToTick(int min) {
        return min * 60 * 20;
    }

    /**
     * 表示一个抽奖池的条目。
     *
     * @param item 条目的目标物品堆栈
     * @param probability 成功选中条目的概率
     * @param cost 完成条目需要花费的成本
     */
    public record PoolEntry(ItemStack item, float probability, int cost) {
        public static final Codec<PoolEntry> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        ItemStack.CODEC.fieldOf("item").forGetter(PoolEntry::item),
                        Codec.FLOAT.fieldOf("probability").forGetter(PoolEntry::probability),
                        Codec.INT.fieldOf("cost").forGetter(PoolEntry::cost)
                ).apply(instance, PoolEntry::new)
        );

        /**
         * 验证概率是否合法（0 < probability <= 1）
         *
         * @return 如果概率有效，则为 true，否则为 false
         */
        public boolean isValid() {
            return probability > 0.0f && probability <= 1.0f && cost > 0;
        }

        @Override
        public @NotNull String toString() {
            return item.toString() + "|" + probability + "|" + cost;
        }
    }
}
package org.oredredging.config;

import com.mojang.serialization.Codec;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import org.oredredging.OreDredging;
import org.oredredging.item.MinerBundleItem;
import org.oredredging.registry.ModItems;
import org.oredredging.util.PredicateParser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * 配置{@link MinerBundleItem}可装入的物品堆栈谓词。
 * <p>每个矿工袋实例都可以拥有一个独立的谓词。</p>
 *
 * @see MinerBundleItem
 * @see PredicateParser
 */
public record BundlesData(
        Map<MinerBundleItem, Predicate<ItemStack>> predicates, // 运行时使用的谓词
        Map<Item, List<String>> rawRules             // 序列化使用的原始规则
) {
    /**
     * Codec：处理 Map<Item, List<String>> 的序列化，并通过 xmap 转换为 BundlesData。
     */
    public static final Codec<BundlesData> CODEC = Codec.unboundedMap(Registries.ITEM.getCodec(), Codec.STRING.listOf())
            .xmap(BundlesData::fromRaw, BundlesData::toRaw);

    public static final List<String> BASE_ALLOWED_ITEMS = List.of(
            "item|minecraft:iron_nugget",
            "item|minecraft:coal",
            "item|minecraft:raw_copper",
            "item|minecraft:raw_iron",
            "item|minecraft:raw_gold",
            "item|minecraft:emerald",
            "item|minecraft:diamond",
            "item|minecraft:lapis_lazuli",
            "item|minecraft:ancient_debris",
            "item|minecraft:amethyst_shard",
            "item|minecraft:gold_nugget",
            "item|minecraft:iron_ingot",
            "item|minecraft:gold_ingot",
            "item|minecraft:copper_ingot",
            "item|minecraft:netherite_ingot",
            "item|minecraft:netherite_scrap",
            "item|minecraft:gravel",
            "item|minecraft:clay",
            "item|minecraft:prismarine_crystals",
            "item|minecraft:clay_ball",
            "item|minecraft:flint",
            "item|ore_dredging:raw_copper_nugget",
            "item|ore_dredging:raw_iron_nugget",
            "item|ore_dredging:raw_gold_nugget",
            "item|ore_dredging:golden_ball",
            "item|ore_dredging:gray_quartz",
            "tag|ore_dredging:pebble",
            "tag|ore_dredging:gravel_piles"
    );

    public static final BundlesData DEFAULT = fromRaw(
            Map.of(ModItems.LEATHER_MINER_BUNDLE, BASE_ALLOWED_ITEMS,
                    ModItems.CHAIN_MINER_BUNDLE, List.of("base"),
                    ModItems.PHANTOM_MINER_BUNDLE, List.of("base")));

    /**
     * 从原始规则映射构建 BundlesData，同时解析谓词。
     *
     * @param raw 原始字符串规则
     */
    public static BundlesData fromRaw(Map<Item, List<String>> raw) {
        Map<MinerBundleItem, Predicate<ItemStack>> predicates = new HashMap<>();
        for (Map.Entry<Item, List<String>> entry : raw.entrySet()) {
            Item item = entry.getKey();

            // 安全转换
            if (!(item instanceof MinerBundleItem minerBundle)) {
                OreDredging.LOGGER.warn("{} is not miner bundle,Automatically ignored", item);
                continue;
            }

            List<String> rules = entry.getValue();
            Predicate<ItemStack> predicate = (rules == null || rules.isEmpty())
                    ? stack -> false
                    : PredicateParser.combine(rules);
            predicates.put(minerBundle, predicate);
        }
        return new BundlesData(predicates, raw);
    }

    /**
     * 获取原始规则映射（用于序列化）。
     */
    public Map<Item, List<String>> toRaw() {
        return rawRules;
    }

    public Predicate<ItemStack> getPredicate(MinerBundleItem minerBundle) {
        if (predicates.containsKey(minerBundle)) {
            return predicates.get(minerBundle);
        }

        return stack -> false;
    }
}
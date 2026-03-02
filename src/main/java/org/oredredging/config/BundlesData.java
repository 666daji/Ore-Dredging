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
            "item|minecraft:iron_nugget",      // 铁粒
            "item|minecraft:coal",                  // 煤炭
            "item|minecraft:raw_copper",             // 粗铜
            "item|minecraft:raw_gold",               // 粗金
            "item|minecraft:emerald",                // 绿宝石
            "item|minecraft:diamond",                // 钻石
            "item|minecraft:lapis_lazuli",           // 青金石
            "item|minecraft:ancient_debris",         // 远古残骸
            "item|minecraft:amethyst_shard",         // 紫水晶碎片
            "item|minecraft:gold_nugget",            // 金粒
            "item|minecraft:iron_ingot",             // 铁锭
            "item|minecraft:gold_ingot",             // 金锭
            "item|minecraft:copper_ingot",           // 铜锭
            "item|minecraft:netherite_ingot",        // 下界合金锭
            "item|minecraft:netherite_scrap",        // 下界合金碎片
            "item|minecraft:gravel",                  // 碎石
            "item|minecraft:clay",                    // 粘土块
            "item|minecraft:prismarine_shard",        // 海晶沙砾
            "item|minecraft:clay_ball",               // 粘土球
            "item|ore_dredging:raw_copper_nugget",    // 粗铜粒
            "item|ore_dredging:raw_iron_nugget",      // 粗铁粒
            "item|ore_dredging:raw_gold_nugget",      // 粗金粒
            "item|ore_dredging:golden_ball",          // 金球
            "item|ore_dredging:gray_quartz"           // 灰石英
    );

    public static final BundlesData DEFAULT = fromRaw(
            Map.of(ModItems.MinerBundle, BASE_ALLOWED_ITEMS));

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
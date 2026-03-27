package org.oredredging.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import org.oredredging.OreDredging;
import org.oredredging.config.framework.ConfigMigrator;
import org.oredredging.item.MinerBundleItem;
import org.oredredging.registry.ModItems;
import org.oredredging.util.PredicateParser;

import java.util.*;
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
            "tag|ore_dredging:gravel_piles",
            "tag|ore_dredging:cimelia"
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

    /**
     * 返回一个迁移器，用于将旧版本配置合并当前默认值。
     * 适用于版本升级时新增默认规则的情况。
     *
     * @return 迁移器实例
     */
    public static ConfigMigrator<BundlesData> migrator() {
        return (oldJson, oldVersion) -> {
            // 解析旧配置为 BundlesData
            DataResult<BundlesData> parseResult = CODEC.parse(JsonOps.INSTANCE, oldJson);
            if (parseResult.error().isPresent()) {
                return DataResult.error(() -> "Failed to parse old BundlesData: " + parseResult.error().get().message());
            }
            BundlesData oldData = parseResult.result().get();
            Map<Item, List<String>> oldRaw = oldData.rawRules();

            // 获取默认配置的原始映射
            Map<Item, List<String>> defaultRaw = DEFAULT.rawRules();

            // 合并映射
            Map<Item, List<String>> merged = new HashMap<>(oldRaw);
            for (Map.Entry<Item, List<String>> entry : defaultRaw.entrySet()) {
                Item item = entry.getKey();
                List<String> defaultRules = entry.getValue();

                if (merged.containsKey(item)) {
                    // 合并规则列表（去重，保持原顺序）
                    List<String> oldRules = merged.get(item);
                    Set<String> combined = new LinkedHashSet<>(oldRules);
                    combined.addAll(defaultRules);
                    merged.put(item, List.copyOf(combined));
                } else {
                    merged.put(item, defaultRules);
                }
            }

            // 构建新 BundlesData
            BundlesData migrated = fromRaw(merged);
            return DataResult.success(migrated);
        };
    }
}
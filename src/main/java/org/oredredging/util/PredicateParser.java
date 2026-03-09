package org.oredredging.util;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.oredredging.OreDredging;
import org.oredredging.config.BundlesData;
import org.oredredging.config.ModConfigs;
import org.oredredging.config.framework.ConfigManager;
import org.oredredging.item.MinerBundleItem;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 将字符串形式的谓词规则解析为 Predicate<ItemStack>。
 * 支持多种谓词类型，通过字符串格式 "类型|参数1|参数2|..." 定义。
 * 多个规则之间为 OR 关系。
 */
public final class PredicateParser {
    private static final Logger LOGGER = OreDredging.LOGGER;

    // 谓词工厂映射：类型名 -> 参数列表 -> 谓词
    private static final Map<String, Function<List<String>, Predicate<ItemStack>>> FACTORIES = new HashMap<>();

    static {
        // 物品类型：item|物品ID
        FACTORIES.put("item", args -> {
            if (args.isEmpty()) {
                LOGGER.warn("Item predicate missing item ID");
                return stack -> false;
            }
            String itemId = args.get(0);
            Item item = Registries.ITEM.get(Identifier.tryParse(itemId));
            return stack -> stack.isOf(item);
        });

        // 标签类型：tag|标签ID
        FACTORIES.put("tag", args -> {
            if (args.isEmpty()) {
                LOGGER.warn("Tag predicate missing tag ID");
                return stack -> false;
            }
            String tagId = args.get(0);
            Identifier id = Identifier.tryParse(tagId);
            if (id == null) {
                LOGGER.warn("Invalid tag ID: {}", tagId);
                return stack -> false;
            }
            TagKey<Item> tag = TagKey.of(RegistryKeys.ITEM, id);
            return stack -> stack.isIn(tag);
        });

        // 复制类型：copy|袋子ID
        FACTORIES.put("copy", args -> {
            if (args.isEmpty()) {
                LOGGER.warn("Copy predicate missing bundle ID");
                return stack -> false;
            }

            String bundleIdStr = args.get(0);
            Identifier bundleId = Identifier.tryParse(bundleIdStr);
            if (bundleId == null) {
                LOGGER.warn("Invalid bundle ID: {}", bundleIdStr);
                return stack -> false;
            }

            Item item = Registries.ITEM.get(bundleId);
            if (!(item instanceof MinerBundleItem minerBundle)) {
                LOGGER.warn("Invalid bundle ID: {}", bundleIdStr);
                return stack -> false;
            }

            // 返回动态谓词，每次 test 时从当前配置获取目标袋子的谓词
            return stack -> {
                BundlesData data = ConfigManager.get(ModConfigs.BUNDLES);
                if (data == null) return false;
                Predicate<ItemStack> targetPredicate = data.getPredicate(minerBundle);
                return targetPredicate.test(stack);
            };
        });

        // 基础类型：base
        FACTORIES.put("base", args -> PredicateParser.combine(BundlesData.BASE_ALLOWED_ITEMS));

        // 全部类型：all
        FACTORIES.put("all", args -> stack -> true);
    }

    private PredicateParser() {}

    /**
     * 解析单个谓词字符串。
     */
    public static Predicate<ItemStack> parse(String predicateString) {
        String[] parts = predicateString.split("\\|");
        String type = parts[0];
        List<String> args = Arrays.asList(parts).subList(1, parts.length);

        Function<List<String>, Predicate<ItemStack>> factory = FACTORIES.get(type);
        if (factory == null) {
            LOGGER.warn("Unknown predicate type: {}", type);
            return stack -> false;
        }
        return factory.apply(args);
    }

    /**
     * 将多个谓词字符串组合为一个 OR 谓词。
     */
    public static Predicate<ItemStack> combine(List<String> predicateStrings) {
        return predicateStrings.stream()
                .map(PredicateParser::parse)
                .reduce(Predicate::or)
                .orElse(stack -> false);
    }
}
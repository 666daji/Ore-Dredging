package org.oredredging.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import org.oredredging.OreDredging;
import org.oredredging.config.framework.ConfigMigrator;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 破碎掉落配置数据，对应 crushedDrops.json 文件内容。
 * 内部使用 Block 对象，但序列化时自动转换为方块的 Identifier 字符串。
 */
public record CrushedDropsData(
        List<Block> canCrushed,
        List<Block> haveExtraDrop
) {
    private static final Codec<Block> BLOCK_CODEC = Registries.BLOCK.getCodec();

    public static final Codec<CrushedDropsData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    BLOCK_CODEC.listOf().fieldOf("canCrushed").forGetter(CrushedDropsData::canCrushed),
                    BLOCK_CODEC.listOf().fieldOf("haveExtraDrop").forGetter(CrushedDropsData::haveExtraDrop)
            ).apply(instance, CrushedDropsData::new)
    );

    // 默认配置
    public static final CrushedDropsData DEFAULT = new CrushedDropsData(
            List.of(
                    Blocks.STONE,
                    Blocks.SANDSTONE,
                    Blocks.SAND,
                    Blocks.DEEPSLATE,
                    Blocks.ANDESITE,
                    Blocks.DIORITE,
                    Blocks.GRANITE,
                    Blocks.GRAVEL
            ),
            List.of(
                    Blocks.GOLD_ORE,
                    Blocks.DEEPSLATE_GOLD_ORE,
                    Blocks.COPPER_ORE,
                    Blocks.DEEPSLATE_COPPER_ORE,
                    Blocks.IRON_ORE,
                    Blocks.DEEPSLATE_IRON_ORE
            )
    );

    /**
     * 返回一个迁移器，用于将旧版本配置合并当前默认值。
     * 适用于版本升级时新增默认方块的情况。
     *
     * @return 迁移器实例
     */
    public static ConfigMigrator<CrushedDropsData> migrator() {
        return (oldJson, oldVersion) -> {
            // 确保输入是 JSON 对象
            if (!oldJson.isJsonObject()) {
                return DataResult.error(() -> "Expected JSON object, got: " + oldJson);
            }
            JsonObject obj = oldJson.getAsJsonObject();

            // 解析旧配置中的两个列表（如果字段缺失则使用空列表）
            List<Block> oldCanCrushed = parseBlockList(obj, "canCrushed");
            List<Block> oldHaveExtra = parseBlockList(obj, "haveExtraDrop");

            // 合并默认列表（使用 LinkedHashSet 保持顺序并去重）
            Set<Block> mergedCanCrushed = new LinkedHashSet<>(oldCanCrushed);
            mergedCanCrushed.addAll(DEFAULT.canCrushed());

            Set<Block> mergedHaveExtra = new LinkedHashSet<>(oldHaveExtra);
            mergedHaveExtra.addAll(DEFAULT.haveExtraDrop());

            // 构建新对象
            CrushedDropsData migrated = new CrushedDropsData(
                    List.copyOf(mergedCanCrushed),
                    List.copyOf(mergedHaveExtra)
            );

            return DataResult.success(migrated);
        };
    }

    /**
     * 辅助方法：从 JSON 对象中解析指定字段的方块列表，字段不存在或解析失败时返回空列表。
     */
    private static List<Block> parseBlockList(JsonObject obj, String fieldName) {
        if (!obj.has(fieldName)) {
            return List.of();
        }
        JsonElement element = obj.get(fieldName);
        return BLOCK_CODEC.listOf().parse(JsonOps.INSTANCE, element)
                .resultOrPartial(error -> OreDredging.LOGGER.warn("Failed to parse '{}' during migration: {}", fieldName, error))
                .orElse(List.of());
    }
}
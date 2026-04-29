package org.oredredging.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;

/**
 * 包裹矿石地物配置
 * @param coreProvider 核心方块提供器
 * @param shellProvider 外壳方块提供器
 * @param coreSize     核心边长（正整数，1~较大值）
 */
public record WrapOreFeatureConfig(
        BlockStateProvider coreProvider,
        BlockStateProvider shellProvider,
        IntProvider coreSize
) implements FeatureConfig {

    public static final Codec<WrapOreFeatureConfig> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    BlockStateProvider.TYPE_CODEC.fieldOf("core_block").forGetter(WrapOreFeatureConfig::coreProvider),
                    BlockStateProvider.TYPE_CODEC.fieldOf("shell_block").forGetter(WrapOreFeatureConfig::shellProvider),
                    IntProvider.POSITIVE_CODEC.fieldOf("core_size").forGetter(WrapOreFeatureConfig::coreSize)
            ).apply(instance, WrapOreFeatureConfig::new)
    );
}
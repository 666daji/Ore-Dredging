package org.oredredging.registry;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;
import org.oredredging.OreDredging;
import org.oredredging.feature.WrapOreFeature;
import org.oredredging.feature.WrapOreFeatureConfig;

public class ModFeatures {
    public static final Feature<WrapOreFeatureConfig> WRAP_ORE = register(
            "wrap_ore",
            new WrapOreFeature(WrapOreFeatureConfig.CODEC)
    );

    /**
     * 注册地物到 FEATURE 注册表
     * @param id      地物 ID（不含命名空间）
     * @param feature 地物实例
     * @return 注册后的地物
     * @param <FC> 配置类型
     * @param <F>  地物类型
     */
    private static <FC extends FeatureConfig, F extends Feature<FC>> F register(String id, F feature) {
        return Registry.register(Registries.FEATURE, new Identifier(OreDredging.MOD_ID, id), feature);
    }

    public static void registerAll() {}
}
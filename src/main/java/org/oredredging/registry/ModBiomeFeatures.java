package org.oredredging.registry;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStep;
import org.oredredging.OreDredging;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class ModBiomeFeatures {
    private static final String STONE_GRAVEL = "stone_gravel_piles";
    private static final List<String> IGNEOUS_GRAVEL_PILES = Arrays.asList(
            "andesite_gravel_piles",
            "diorite_gravel_piles",
            "granite_gravel_piles"
    );
    private static final String DEEPSLATE_GRAVEL = "deepslate_gravel_piles";
    private static final String SANDSTONE_GRAVEL = "sandstone_gravel_piles";

    // 群系标签和键常量
    private static final TagKey<Biome> BEACH_TAG =
            TagKey.of(RegistryKeys.BIOME, new Identifier("is_beach"));
    private static final RegistryKey<Biome> DESERT_KEY =
            RegistryKey.of(RegistryKeys.BIOME, new Identifier("desert"));

    // 排除海滩和沙漠
    private static final Predicate<BiomeSelectionContext> EXCLUDE_BEACH_AND_DESERT =
            BiomeSelectors.tag(BEACH_TAG).or(BiomeSelectors.includeByKey(DESERT_KEY)).negate();

    public static void registerAll() {
        // 1. 石头碎石堆
        BiomeModifications.addFeature(
                BiomeSelectors.foundInOverworld(),
                GenerationStep.Feature.VEGETAL_DECORATION,
                RegistryKey.of(RegistryKeys.PLACED_FEATURE, new Identifier(OreDredging.MOD_ID, STONE_GRAVEL))
        );

        // 2. 安山岩、闪长岩、花岗岩碎石堆
        for (String id : IGNEOUS_GRAVEL_PILES) {
            BiomeModifications.addFeature(
                    BiomeSelectors.foundInOverworld().and(EXCLUDE_BEACH_AND_DESERT),
                    GenerationStep.Feature.VEGETAL_DECORATION,
                    RegistryKey.of(RegistryKeys.PLACED_FEATURE, new Identifier(OreDredging.MOD_ID, id))
            );
        }

        // 3. 深板岩碎石堆
        BiomeModifications.addFeature(
                BiomeSelectors.foundInOverworld(),
                GenerationStep.Feature.UNDERGROUND_DECORATION,
                RegistryKey.of(RegistryKeys.PLACED_FEATURE, new Identifier(OreDredging.MOD_ID, DEEPSLATE_GRAVEL))
        );

        // 4. 砂岩碎石堆
        BiomeModifications.addFeature(
                BiomeSelectors.tag(BEACH_TAG).or(BiomeSelectors.includeByKey(DESERT_KEY)),
                GenerationStep.Feature.VEGETAL_DECORATION,
                RegistryKey.of(RegistryKeys.PLACED_FEATURE, new Identifier(OreDredging.MOD_ID, SANDSTONE_GRAVEL))
        );
    }
}
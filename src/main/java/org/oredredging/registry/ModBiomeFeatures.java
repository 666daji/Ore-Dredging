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
import net.minecraft.world.gen.feature.PlacedFeature;
import org.oredredging.OreDredging;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class ModBiomeFeatures {
    // 碎石堆
    public static final RegistryKey<PlacedFeature> STONE_GRAVEL_PILES = of("stone_gravel_piles");
    public static final RegistryKey<PlacedFeature> DIORITE_GRAVEL_PILES = of("diorite_gravel_piles");
    public static final RegistryKey<PlacedFeature> ANDESITE_GRAVEL_PILES = of("andesite_gravel_piles");
    public static final RegistryKey<PlacedFeature> GRANITE_GRAVEL_PILES = of("granite_gravel_piles");
    public static final RegistryKey<PlacedFeature> SANDSTONE_GRAVEL_PILES = of("sandstone_gravel_piles");
    public static final RegistryKey<PlacedFeature> DEEPSLATE_GRAVEL_PILES = of("deepslate_gravel_piles");
    public static final RegistryKey<PlacedFeature> TUFF_GRAVEL_PILES = of("tuff_gravel_piles");

    // 小石子
    public static final RegistryKey<PlacedFeature> STONE_PEBBLE = of("stone_pebble");
    public static final RegistryKey<PlacedFeature> DIORITE_PEBBLE = of("diorite_pebble");
    public static final RegistryKey<PlacedFeature> ANDESITE_PEBBLE = of("andesite_pebble");
    public static final RegistryKey<PlacedFeature> GRANITE_PEBBLE = of("granite_pebble");
    public static final RegistryKey<PlacedFeature> DEEPSLATE_PEBBLE = of("deepslate_pebble");

    // 浅层洞穴
    public static final RegistryKey<PlacedFeature> STONE_GRAVEL_PILES_CAVE = of("stone_gravel_piles_cave");
    public static final RegistryKey<PlacedFeature> DIORITE_GRAVEL_PILES_CAVE = of("diorite_gravel_piles_cave");
    public static final RegistryKey<PlacedFeature> ANDESITE_GRAVEL_PILES_CAVE = of("andesite_gravel_piles_cave");
    public static final RegistryKey<PlacedFeature> GRANITE_GRAVEL_PILES_CAVE = of("granite_gravel_piles_cave");
    public static final RegistryKey<PlacedFeature> TUFF_GRAVEL_PILES_CAVE = of("tuff_gravel_piles_cave");
    public static final RegistryKey<PlacedFeature> STONE_PEBBLE_CAVE = of("stone_pebble_cave");
    public static final RegistryKey<PlacedFeature> DIORITE_PEBBLE_CAVE = of("diorite_pebble_cave");
    public static final RegistryKey<PlacedFeature> ANDESITE_PEBBLE_CAVE = of("andesite_pebble_cave");
    public static final RegistryKey<PlacedFeature> GRANITE_PEBBLE_CAVE = of("granite_pebble_cave");

    // 矿石
    public static final RegistryKey<PlacedFeature> SWAMP_IRON_ORE = of("swamp_iron_ore");

    private static final List<RegistryKey<PlacedFeature>> IGNEOUS_GRAVEL_PILES = Arrays.asList(
            ANDESITE_GRAVEL_PILES,
            DIORITE_GRAVEL_PILES,
            GRANITE_GRAVEL_PILES,
            DIORITE_PEBBLE,
            ANDESITE_PEBBLE,
            GRANITE_PEBBLE
    );

    private static final List<RegistryKey<PlacedFeature>> CAVE =  Arrays.asList(
            DEEPSLATE_GRAVEL_PILES,
            DEEPSLATE_PEBBLE,
            TUFF_GRAVEL_PILES,
            STONE_GRAVEL_PILES_CAVE,
            DIORITE_GRAVEL_PILES_CAVE,
            ANDESITE_GRAVEL_PILES_CAVE,
            GRANITE_GRAVEL_PILES_CAVE,
            TUFF_GRAVEL_PILES_CAVE,
            STONE_PEBBLE_CAVE,
            DIORITE_PEBBLE_CAVE,
            ANDESITE_PEBBLE_CAVE,
            GRANITE_PEBBLE_CAVE
    );

    // 群系标签
    private static final TagKey<Biome> BEACH_TAG =
            TagKey.of(RegistryKeys.BIOME, new Identifier("is_beach"));
    private static final RegistryKey<Biome> DESERT_KEY =
            RegistryKey.of(RegistryKeys.BIOME, new Identifier("desert"));

    // 排除海滩和沙漠
    private static final Predicate<BiomeSelectionContext> EXCLUDE_BEACH_AND_DESERT =
            BiomeSelectors.tag(BEACH_TAG).or(BiomeSelectors.includeByKey(DESERT_KEY)).negate();

    public static void registerAll() {
        // 石头碎石堆
        BiomeModifications.addFeature(
                BiomeSelectors.foundInOverworld(),
                GenerationStep.Feature.VEGETAL_DECORATION,
                STONE_GRAVEL_PILES
        );

        BiomeModifications.addFeature(
                BiomeSelectors.foundInOverworld(),
                GenerationStep.Feature.VEGETAL_DECORATION,
                STONE_PEBBLE
        );

        // 安山岩、闪长岩、花岗岩
        for (RegistryKey<PlacedFeature> id : IGNEOUS_GRAVEL_PILES) {
            BiomeModifications.addFeature(
                    BiomeSelectors.foundInOverworld().and(EXCLUDE_BEACH_AND_DESERT),
                    GenerationStep.Feature.VEGETAL_DECORATION, id
            );
        }

        // 深板岩碎石堆
        for (RegistryKey<PlacedFeature> id : CAVE) {
            BiomeModifications.addFeature(
                    BiomeSelectors.foundInOverworld().and(EXCLUDE_BEACH_AND_DESERT),
                    GenerationStep.Feature.UNDERGROUND_DECORATION, id
            );
        }

        // 砂岩碎石堆
        BiomeModifications.addFeature(
                BiomeSelectors.tag(BEACH_TAG).or(BiomeSelectors.includeByKey(DESERT_KEY)),
                GenerationStep.Feature.VEGETAL_DECORATION,
                SANDSTONE_GRAVEL_PILES
        );

        // 沼铁矿
        BiomeModifications.addFeature(
                BiomeSelectors.foundInOverworld(),
                GenerationStep.Feature.VEGETAL_DECORATION,
                SWAMP_IRON_ORE
        );
    }

    private static RegistryKey<PlacedFeature> of(String id) {
        return RegistryKey.of(RegistryKeys.PLACED_FEATURE, new Identifier(OreDredging.MOD_ID, id));
    }
}
package org.oredredging.registry;

import net.minecraft.block.*;
import net.minecraft.block.enums.Instrument;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import org.oredredging.OreDredging;
import org.oredredging.block.GravelPilesBlock;
import org.oredredging.block.PebbleBlock;

public class ModBlocks {
    // 花岗岩系列
    public static final Block GRANITE_BRICKS = register("granite_bricks",
            new Block(AbstractBlock.Settings.copy(Blocks.STONE_BRICKS)));
    public static final Block GRANITE_BRICK_STAIRS = register("granite_brick_stairs",
            new StairsBlock(GRANITE_BRICKS.getDefaultState(), AbstractBlock.Settings.copy(Blocks.STONE_BRICK_STAIRS)));
    public static final Block GRANITE_BRICK_SLAB = register("granite_brick_slab",
            new SlabBlock(AbstractBlock.Settings.copy(Blocks.STONE_BRICK_SLAB)));
    public static final Block GRANITE_BRICK_WALL = register("granite_brick_wall",
            new WallBlock(AbstractBlock.Settings.copy(Blocks.STONE_BRICK_WALL)));

    // 闪长岩系列
    public static final Block DIORITE_BRICKS = register("diorite_bricks",
            new Block(AbstractBlock.Settings.copy(Blocks.STONE_BRICKS)));
    public static final Block DIORITE_BRICK_STAIRS = register("diorite_brick_stairs",
            new StairsBlock(DIORITE_BRICKS.getDefaultState(), AbstractBlock.Settings.copy(Blocks.STONE_BRICK_STAIRS)));
    public static final Block DIORITE_BRICK_SLAB = register("diorite_brick_slab",
            new SlabBlock(AbstractBlock.Settings.copy(Blocks.STONE_BRICK_SLAB)));
    public static final Block DIORITE_BRICK_WALL = register("diorite_brick_wall",
            new WallBlock(AbstractBlock.Settings.copy(Blocks.STONE_BRICK_WALL)));

    // 安山岩系列
    public static final Block ANDESITE_BRICKS = register("andesite_bricks",
            new Block(AbstractBlock.Settings.copy(Blocks.STONE_BRICKS)));
    public static final Block ANDESITE_BRICK_STAIRS = register("andesite_brick_stairs",
            new StairsBlock(ANDESITE_BRICKS.getDefaultState(), AbstractBlock.Settings.copy(Blocks.STONE_BRICK_STAIRS)));
    public static final Block ANDESITE_BRICK_SLAB = register("andesite_brick_slab",
            new SlabBlock(AbstractBlock.Settings.copy(Blocks.STONE_BRICK_SLAB)));
    public static final Block ANDESITE_BRICK_WALL = register("andesite_brick_wall",
            new WallBlock(AbstractBlock.Settings.copy(Blocks.STONE_BRICK_WALL)));

    // 碎石堆
    public static final Block STONE_GRAVEL_PILES = register("stone_gravel_piles",
            new GravelPilesBlock(AbstractBlock.Settings.create().mapColor(MapColor.STONE_GRAY).strength(1.5F, 2.0F).nonOpaque().pistonBehavior(PistonBehavior.DESTROY)));
    public static final Block DIORITE_GRAVEL_PILES = register("diorite_gravel_piles",
            new GravelPilesBlock(AbstractBlock.Settings.copy(STONE_GRAVEL_PILES).mapColor(MapColor.OFF_WHITE)));
    public static final Block ANDESITE_GRAVEL_PILES = register("andesite_gravel_piles",
            new GravelPilesBlock(AbstractBlock.Settings.copy(STONE_GRAVEL_PILES).mapColor(MapColor.STONE_GRAY)));
    public static final Block GRANITE_GRAVEL_PILES = register("granite_gravel_piles",
            new GravelPilesBlock(AbstractBlock.Settings.copy(STONE_GRAVEL_PILES).mapColor(MapColor.DIRT_BROWN)));
    public static final Block SANDSTONE_GRAVEL_PILES = register("sandstone_gravel_piles",
            new GravelPilesBlock(AbstractBlock.Settings.copy(STONE_GRAVEL_PILES).mapColor(MapColor.PALE_YELLOW)));
    public static final Block DEEPSLATE_GRAVEL_PILES = register("deepslate_gravel_piles",
            new GravelPilesBlock(AbstractBlock.Settings.copy(STONE_GRAVEL_PILES).mapColor(MapColor.DEEPSLATE_GRAY)));
    public static final Block TUFF_GRAVEL_PILES = register("tuff_gravel_piles",
            new GravelPilesBlock(AbstractBlock.Settings.copy(STONE_GRAVEL_PILES).mapColor(MapColor.TERRACOTTA_GRAY)));
    public static final Block NETHERRACK_GRAVEL_PILES = register("netherrack_gravel_piles",
            new GravelPilesBlock(AbstractBlock.Settings.copy(STONE_GRAVEL_PILES).mapColor(MapColor.RED)));
    public static final Block BASALT_GRAVEL_PILES = register("basalt_gravel_piles",
            new GravelPilesBlock(AbstractBlock.Settings.copy(STONE_GRAVEL_PILES).mapColor(MapColor.BLACK)));

    // 小石子
    public static final Block STONE_PEBBLE = register("stone_pebble",
            new PebbleBlock(AbstractBlock.Settings.create().mapColor(MapColor.STONE_GRAY).strength(1.0F, 1.0F).nonOpaque().pistonBehavior(PistonBehavior.DESTROY)));
    public static final Block DIORITE_PEBBLE = register("diorite_pebble",
            new PebbleBlock(AbstractBlock.Settings.copy(STONE_PEBBLE).mapColor(MapColor.OFF_WHITE)));
    public static final Block ANDESITE_PEBBLE = register("andesite_pebble",
            new PebbleBlock(AbstractBlock.Settings.copy(STONE_PEBBLE).mapColor(MapColor.STONE_GRAY)));
    public static final Block GRANITE_PEBBLE = register("granite_pebble",
            new PebbleBlock(AbstractBlock.Settings.copy(STONE_PEBBLE).mapColor(MapColor.DIRT_BROWN)));
    public static final Block DEEPSLATE_PEBBLE = register("deepslate_pebble",
            new PebbleBlock(AbstractBlock.Settings.copy(STONE_PEBBLE).mapColor(MapColor.DEEPSLATE_GRAY)));

    // 矿石
    public static final Block SWAMP_IRON_ORE = register("swamp_iron_ore",
            new ExperienceDroppingBlock(AbstractBlock.Settings.create().mapColor(MapColor.STONE_GRAY).instrument(Instrument.BASEDRUM).strength(1.0F, 1.0F)));
    public static final Block QUARTZ_GLASS = register("quartz_glass",
            new GlassBlock(AbstractBlock.Settings.create().nonOpaque().mapColor(MapColor.WHITE).instrument(Instrument.BASEDRUM).requiresTool().strength(50.0F, 1200.0F).sounds(BlockSoundGroup.GLASS)));
    public static final Block QUARTZ_GLASS_PANES = register("quartz_glass_panes",
            new PaneBlock(AbstractBlock.Settings.create().nonOpaque().mapColor(MapColor.WHITE).instrument(Instrument.BASEDRUM).requiresTool().strength(50.0F, 1200.0F).sounds(BlockSoundGroup.GLASS)));

    private static Block register(String id, Block block) {
        return Registry.register(Registries.BLOCK, new Identifier(OreDredging.MOD_ID, id), block);
    }
}
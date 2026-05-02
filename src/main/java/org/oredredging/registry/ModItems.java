package org.oredredging.registry;

import net.minecraft.block.Block;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.oredredging.OreDredging;
import org.oredredging.item.*;

import java.util.function.BiFunction;

public class ModItems {
    // 矿粒
    public static final Item RAW_COPPER_NUGGET = register("raw_copper_nugget");
    public static final Item RAW_IRON_NUGGET = register("raw_iron_nugget");
    public static final Item RAW_GOLD_NUGGET = register("raw_gold_nugget");
    public static final Item NETHERITE_NUGGET = register("netherite_nugget");

    // 花岗岩砖
    public static final Item GRANITE_BRICKS = register(ModBlocks.GRANITE_BRICKS);
    public static final Item GRANITE_BRICK_STAIRS = register(ModBlocks.GRANITE_BRICK_STAIRS);
    public static final Item GRANITE_BRICK_SLAB = register(ModBlocks.GRANITE_BRICK_SLAB);
    public static final Item GRANITE_BRICK_WALL = register(ModBlocks.GRANITE_BRICK_WALL);

    // 闪长岩砖
    public static final Item DIORITE_BRICKS = register(ModBlocks.DIORITE_BRICKS);
    public static final Item DIORITE_BRICK_STAIRS = register(ModBlocks.DIORITE_BRICK_STAIRS);
    public static final Item DIORITE_BRICK_SLAB = register(ModBlocks.DIORITE_BRICK_SLAB);
    public static final Item DIORITE_BRICK_WALL = register(ModBlocks.DIORITE_BRICK_WALL);

    // 安山岩砖
    public static final Item ANDESITE_BRICKS = register(ModBlocks.ANDESITE_BRICKS);
    public static final Item ANDESITE_BRICK_STAIRS = register(ModBlocks.ANDESITE_BRICK_STAIRS);
    public static final Item ANDESITE_BRICK_SLAB = register(ModBlocks.ANDESITE_BRICK_SLAB);
    public static final Item ANDESITE_BRICK_WALL = register(ModBlocks.ANDESITE_BRICK_WALL);

    // 碎石堆
    public static final Item STONE_GRAVEL_PILES = register(ModBlocks.STONE_GRAVEL_PILES);
    public static final Item DIORITE_GRAVEL_PILES = register(ModBlocks.DIORITE_GRAVEL_PILES);
    public static final Item ANDESITE_GRAVEL_PILES = register(ModBlocks.ANDESITE_GRAVEL_PILES);
    public static final Item GRANITE_GRAVEL_PILES = register(ModBlocks.GRANITE_GRAVEL_PILES);
    public static final Item SANDSTONE_GRAVEL_PILES = register(ModBlocks.SANDSTONE_GRAVEL_PILES);
    public static final Item DEEPSLATE_GRAVEL_PILES = register(ModBlocks.DEEPSLATE_GRAVEL_PILES);
    public static final Item TUFF_GRAVEL_PILES = register(ModBlocks.TUFF_GRAVEL_PILES);
    public static final Item NETHERRACK_GRAVEL_PILES = register(ModBlocks.NETHERRACK_GRAVEL_PILES);
    public static final Item BASALT_GRAVEL_PILES = register(ModBlocks.BASALT_GRAVEL_PILES);
    public static final Item RAW_COPPER_GRAVEL_PILES = register(ModBlocks.RAW_COPPER_GRAVEL_PILES);
    public static final Item RAW_IRON_PILES = register(ModBlocks.RAW_IRON_PILES);
    public static final Item RAW_GOLD_PILES = register(ModBlocks.RAW_GOLD_PILES);

    // 小石子
    public static final Item STONE_PEBBLE = register(ModBlocks.STONE_PEBBLE, PebbleItem::new);
    public static final Item DIORITE_PEBBLE = register(ModBlocks.DIORITE_PEBBLE, ((block, settings) -> new PebbleItem(block, settings, PebbleItem.Performance.DIORITE)));
    public static final Item ANDESITE_PEBBLE = register(ModBlocks.ANDESITE_PEBBLE, ((block, settings) -> new PebbleItem(block, settings, PebbleItem.Performance.ANDESITE)));
    public static final Item GRANITE_PEBBLE = register(ModBlocks.GRANITE_PEBBLE, ((block, settings) -> new PebbleItem(block, settings, PebbleItem.Performance.GRANITE)));
    public static final Item DEEPSLATE_PEBBLE = register(ModBlocks.DEEPSLATE_PEBBLE, ((block, settings) -> new PebbleItem(block, settings, PebbleItem.Performance.DEEPSLATE)));

    // 粗金属块半砖
    public static final Item RAW_IRON_SLAB = register(ModBlocks.RAW_IRON_SLAB);
    public static final Item RAW_COPPER_SLAB = register(ModBlocks.RAW_COPPER_SLAB);
    public static final Item RAW_GOLD_SLAB = register(ModBlocks.RAW_GOLD_SLAB);

    // 金属块半砖
    public static final Item IRON_SLAB = register(ModBlocks.IRON_SLAB);
    public static final Item GOLD_SLAB = register(ModBlocks.GOLD_SLAB);
    public static final Item COPPER_SLAB = register(ModBlocks.COPPER_SLAB);
    public static final Item NETHERITE_SLAB = register(ModBlocks.NETHERITE_SLAB);
    public static final Item DIAMOND_SLAB = register(ModBlocks.DIAMOND_SLAB);
    public static final Item EMERALD_SLAB = register(ModBlocks.EMERALD_SLAB);
    public static final Item LAPIS_SLAB = register(ModBlocks.LAPIS_SLAB);
    public static final Item REDSTONE_SLAB = register(ModBlocks.REDSTONE_SLAB);
    public static final Item COAL_SLAB = register(ModBlocks.COAL_SLAB);

    // 矿工袋
    public static final Item LEATHER_MINER_BUNDLE = register("leather_miner_bundle", new MinerBundleItem(new Item.Settings(), 4));
    public static final Item CHAIN_MINER_BUNDLE = register("chain_miner_bundle", new MinerBundleItem(new Item.Settings(), 8));
    public static final Item PHANTOM_MINER_BUNDLE = register("phantom_miner_bundle", new MinerBundleItem(new Item.Settings(), 12));

    // 宝物
    public static final Item GOLDEN_BALL = register("golden_ball", new SimpleCimeliaItem(new Item.Settings(), Cimelia.Category.NATURE, 2));
    public static final Item NEPHRITE = register("nephrite", new SimpleCimeliaItem(new Item.Settings(), Cimelia.Category.NATURE, 2));
    public static final Item ARMOR_FRAGMENTS = register("armor_fragments", new SimpleCimeliaItem(new Item.Settings(), Cimelia.Category.ANCIENT, 2));
    public static final Item ENERGETIC_CRYSTAL = register("energetic_crystal", new SimpleCimeliaItem(new Item.Settings(), Cimelia.Category.NATURE, 3));
    public static final Item MIMETIC_LEY_LINE = register(ModBlocks.MIMETIC_LEY_LINE, (block, settings) -> new MimeticLeyLineItem(block, settings, Cimelia.Category.ANCIENT, 3));
    public static final Item ASTRALIUM = register("astralium", new SimpleCimeliaItem(new Item.Settings(), Cimelia.Category.NATURE, 3));
    public static final Item FLAME_CRYSTAL = register("flame_crystal", new FlameCrystalItem(new Item.Settings()));
    public static final Item FLAME_CRYSTAL_CLUSTER = register("flame_crystal_cluster", new SimpleCimeliaItem(new Item.Settings(), Cimelia.Category.NATURE, 3));

    public static final Item GRAY_QUARTZ = register("gray_quartz");
    public static final Item SOFT_ARMOR_TEMPLATE = register("soft_armor_template");
    public static final Item ASHES = register("ashes");

    // 矿石
    public static final Item SWAMP_IRON_ORE = register(ModBlocks.SWAMP_IRON_ORE);
    public static final Item ASTRALIUM_ORE = register(ModBlocks.ASTRALIUM_ORE);

    // 矿产
    public static final Item ASTRALIUM_STEEL = register("astralium_steel");
    public static final Item ASTRALIUM_STEEL_NUGGET = register("astralium_steel_nugget");

    // 工具
    public static final Item GEOLOGICAL_HAMMER = register("geological_hammer", new GeologicalHammerItem(ModToolMaterials.GEOLOGICAL_HAMMER, 0, -2.4F, new Item.Settings().maxCount(1).maxDamage(230)));
    public static final Item COLLAPSE_STONE_HAMMER = register("collapse_stone_hammer", new CollapseStoneHammerItem(ModToolMaterials.COLLAPSE_STONE_HAMMER, 7, -3.3F, new Item.Settings().maxCount(1).maxDamage(230)));

    // 装备
    public static final Item MINER_HELMET = register("miner_helmet", new MinerHelmetItem(MinerHelmetItem.ArmorMaterials.MINER_HELMET, new Item.Settings().maxCount(1).maxDamage(230)));

    // 雷管
    public static final Item DETONATOR = register(ModBlocks.DETONATOR, ((block, settings) -> new DetonatorItem(block, settings, 100)));
    public static final Item SLIMY_DETONATOR = register(ModBlocks.SLIMY_DETONATOR, ((block, settings) -> new SlimyDetonatorItem(block, settings, 100)));
    public static final Item IMPACT_DETONATOR = register("impact_detonator", new ImpactDetonatorItem(new Item.Settings()));

    // 武器
    public static final Item FLAME_CRYSTAL_ARROW = register("flame_crystal_arrow", new FlameCrystalArrowItem(new Item.Settings()));

    // 其他
    public static final Item QUARTZ_GLASS = register(ModBlocks.QUARTZ_GLASS);
    public static final Item QUARTZ_GLASS_PANES = register(ModBlocks.QUARTZ_GLASS_PANES);

    // 工作方块
    public static final Item THUNDER_SMELTER_PIPE = register(ModBlocks.THUNDER_SMELTER_PIPE);

    private static Item register(String id) {
        return register(id, new Item(new Item.Settings()));
    }

    private static Item register(Block block) {
        return register(block, BlockItem::new);
    }

    private static Item register(Block block, BiFunction<Block, Item.Settings, BlockItem> create) {
        return register(Registries.BLOCK.getId(block).getPath(), create.apply(block, new Item.Settings()));
    }

    private static Item register(String id, Item item) {
        return Registry.register(Registries.ITEM, new Identifier(OreDredging.MOD_ID, id), item);
    }

    public static void registerAll() {}
}
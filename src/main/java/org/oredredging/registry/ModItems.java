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

    // 小石子
    public static final Item STONE_PEBBLE = register(ModBlocks.STONE_PEBBLE, PebbleItem::new);
    public static final Item DIORITE_PEBBLE = register(ModBlocks.DIORITE_PEBBLE, ((block, settings) -> new PebbleItem(block, settings, PebbleItem.Performance.DIORITE)));
    public static final Item ANDESITE_PEBBLE = register(ModBlocks.ANDESITE_PEBBLE, ((block, settings) -> new PebbleItem(block, settings, PebbleItem.Performance.ANDESITE)));
    public static final Item GRANITE_PEBBLE = register(ModBlocks.GRANITE_PEBBLE, ((block, settings) -> new PebbleItem(block, settings, PebbleItem.Performance.GRANITE)));
    public static final Item DEEPSLATE_PEBBLE = register(ModBlocks.DEEPSLATE_PEBBLE, ((block, settings) -> new PebbleItem(block, settings, PebbleItem.Performance.DEEPSLATE)));

    // 矿工袋
    public static final Item LEATHER_MINER_BUNDLE = register("leather_miner_bundle", new MinerBundleItem(new Item.Settings(), 4));
    public static final Item CHAIN_MINER_BUNDLE = register("chain_miner_bundle", new MinerBundleItem(new Item.Settings(), 8));
    public static final Item PHANTOM_MINER_BUNDLE = register("phantom_miner_bundle", new MinerBundleItem(new Item.Settings(), 12));

    // 宝物
    public static final Item GOLDEN_BALL = register("golden_ball", new CimeliaItem(new Item.Settings(), CimeliaItem.Category.NATURE, 2));
    public static final Item NEPHRITE = register("nephrite", new CimeliaItem(new Item.Settings(), CimeliaItem.Category.NATURE, 2));
    public static final Item ARMOR_FRAGMENTS = register("armor_fragments", new CimeliaItem(new Item.Settings(), CimeliaItem.Category.ANCIENT, 2));
    public static final Item ENERGETIC_CRYSTAL = register("energetic_crystal", new CimeliaItem(new Item.Settings(), CimeliaItem.Category.NATURE, 3));

    public static final Item GRAY_QUARTZ = register("gray_quartz");
    public static final Item SOFT_ARMOR_TEMPLATE = register("soft_armor_template");

    // 矿石
    public static final Item SWAMP_IRON_ORE = register(ModBlocks.SWAMP_IRON_ORE);
    public static final Item QUARTZ_GLASS = register(ModBlocks.QUARTZ_GLASS);

    // 工具
    public static final Item GEOLOGICAL_HAMMER = register("geological_hammer", new PickaxeItem(ToolMaterials.DIAMOND, 1, -2.8F, new Item.Settings().maxCount(1).maxDamage(230)));
    public static final Item COLLAPSE_STONE_HAMMER = register("collapse_stone_hammer", new CollapseStoneHammerItem(new Item.Settings().maxCount(1).maxDamage(230)));

    // 装备
    public static final Item MINER_HELMET = register("miner_helmet", new MinerHelmetItem(MinerHelmetItem.ArmorMaterials.MINER_HELMET, new Item.Settings().maxCount(1).maxDamage(230)));

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
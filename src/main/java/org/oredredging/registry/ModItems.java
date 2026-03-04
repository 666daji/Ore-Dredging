package org.oredredging.registry;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.oredredging.OreDredging;
import org.oredredging.item.CimeliaItem;
import org.oredredging.item.MinerBundleItem;
import org.oredredging.item.PebbleItem;

import java.util.function.BiFunction;

public class ModItems {
    // 粗矿粒
    public static final Item RAW_COPPER_NUGGET = register("raw_copper_nugget");
    public static final Item RAW_IRON_NUGGET = register("raw_iron_nugget");
    public static final Item RAW_GOLD_NUGGET = register("raw_gold_nugget");

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

    // 小石子
    public static final Item STONE_PEBBLE = register(ModBlocks.STONE_PEBBLE, PebbleItem::new);
    public static final Item DIORITE_PEBBLE = register(ModBlocks.DIORITE_PEBBLE, PebbleItem::new);
    public static final Item ANDESITE_PEBBLE = register(ModBlocks.ANDESITE_PEBBLE, PebbleItem::new);
    public static final Item GRANITE_PEBBLE = register(ModBlocks.GRANITE_PEBBLE, PebbleItem::new);
    public static final Item DEEPSLATE_PEBBLE = register(ModBlocks.DEEPSLATE_PEBBLE, PebbleItem::new);
    public static final Item TUFF_PEBBLE = register(ModBlocks.TUFF_PEBBLE, PebbleItem::new);

    // 矿工袋
    public static final Item LEATHER_MINER_BUNDLE = register("leather_miner_bundle", new MinerBundleItem(new Item.Settings(), 4));
    public static final Item CHAIN_MINER_BUNDLE = register("chain_miner_bundle", new MinerBundleItem(new Item.Settings(), 8));
    public static final Item PHANTOM_MINER_BUNDLE = register("phantom_miner_bundle", new MinerBundleItem(new Item.Settings(), 12));

    // 宝物
    public static final Item GOLDEN_BALL = register("golden_ball", new CimeliaItem(new Item.Settings(), CimeliaItem.Category.NATURE, 2));

    public static final Item GRAY_QUARTZ = register("gray_quartz");

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
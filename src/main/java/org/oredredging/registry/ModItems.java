package org.oredredging.registry;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.oredredging.OreDredging;
import org.oredredging.item.*;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, OreDredging.MOD_ID);

    // 矿粒
    public static final RegistryObject<Item> RAW_COPPER_NUGGET = register("raw_copper_nugget");
    public static final RegistryObject<Item> RAW_IRON_NUGGET = register("raw_iron_nugget");
    public static final RegistryObject<Item> RAW_GOLD_NUGGET = register("raw_gold_nugget");
    public static final RegistryObject<Item> NETHERITE_NUGGET = register("netherite_nugget");

    // 花岗岩砖
    public static final RegistryObject<Item> GRANITE_BRICKS = fromBlock(ModBlocks.GRANITE_BRICKS);
    public static final RegistryObject<Item> GRANITE_BRICK_STAIRS = fromBlock(ModBlocks.GRANITE_BRICK_STAIRS);
    public static final RegistryObject<Item> GRANITE_BRICK_SLAB = fromBlock(ModBlocks.GRANITE_BRICK_SLAB);
    public static final RegistryObject<Item> GRANITE_BRICK_WALL = fromBlock(ModBlocks.GRANITE_BRICK_WALL);

    // 闪长岩砖
    public static final RegistryObject<Item> DIORITE_BRICKS = fromBlock(ModBlocks.DIORITE_BRICKS);
    public static final RegistryObject<Item> DIORITE_BRICK_STAIRS = fromBlock(ModBlocks.DIORITE_BRICK_STAIRS);
    public static final RegistryObject<Item> DIORITE_BRICK_SLAB = fromBlock(ModBlocks.DIORITE_BRICK_SLAB);
    public static final RegistryObject<Item> DIORITE_BRICK_WALL = fromBlock(ModBlocks.DIORITE_BRICK_WALL);

    // 安山岩砖
    public static final RegistryObject<Item> ANDESITE_BRICKS = fromBlock(ModBlocks.ANDESITE_BRICKS);
    public static final RegistryObject<Item> ANDESITE_BRICK_STAIRS = fromBlock(ModBlocks.ANDESITE_BRICK_STAIRS);
    public static final RegistryObject<Item> ANDESITE_BRICK_SLAB = fromBlock(ModBlocks.ANDESITE_BRICK_SLAB);
    public static final RegistryObject<Item> ANDESITE_BRICK_WALL = fromBlock(ModBlocks.ANDESITE_BRICK_WALL);

    // 碎石堆
    public static final RegistryObject<Item> STONE_GRAVEL_PILES = fromBlock(ModBlocks.STONE_GRAVEL_PILES);
    public static final RegistryObject<Item> DIORITE_GRAVEL_PILES = fromBlock(ModBlocks.DIORITE_GRAVEL_PILES);
    public static final RegistryObject<Item> ANDESITE_GRAVEL_PILES = fromBlock(ModBlocks.ANDESITE_GRAVEL_PILES);
    public static final RegistryObject<Item> GRANITE_GRAVEL_PILES = fromBlock(ModBlocks.GRANITE_GRAVEL_PILES);
    public static final RegistryObject<Item> SANDSTONE_GRAVEL_PILES = fromBlock(ModBlocks.SANDSTONE_GRAVEL_PILES);
    public static final RegistryObject<Item> DEEPSLATE_GRAVEL_PILES = fromBlock(ModBlocks.DEEPSLATE_GRAVEL_PILES);
    public static final RegistryObject<Item> TUFF_GRAVEL_PILES = fromBlock(ModBlocks.TUFF_GRAVEL_PILES);
    public static final RegistryObject<Item> NETHERRACK_GRAVEL_PILES = fromBlock(ModBlocks.NETHERRACK_GRAVEL_PILES);
    public static final RegistryObject<Item> BASALT_GRAVEL_PILES = fromBlock(ModBlocks.BASALT_GRAVEL_PILES);

    // 小石子（带自定义行为）
    public static final RegistryObject<Item> STONE_PEBBLE = register(ModBlocks.STONE_PEBBLE, PebbleItem::new);
    public static final RegistryObject<Item> DIORITE_PEBBLE = register(ModBlocks.DIORITE_PEBBLE,
            (block, props) -> new PebbleItem(block, props, PebbleItem.Performance.DIORITE));
    public static final RegistryObject<Item> ANDESITE_PEBBLE = register(ModBlocks.ANDESITE_PEBBLE,
            (block, props) -> new PebbleItem(block, props, PebbleItem.Performance.ANDESITE));
    public static final RegistryObject<Item> GRANITE_PEBBLE = register(ModBlocks.GRANITE_PEBBLE,
            (block, props) -> new PebbleItem(block, props, PebbleItem.Performance.GRANITE));
    public static final RegistryObject<Item> DEEPSLATE_PEBBLE = register(ModBlocks.DEEPSLATE_PEBBLE,
            (block, props) -> new PebbleItem(block, props, PebbleItem.Performance.DEEPSLATE));

    // 矿工袋
    public static final RegistryObject<Item> LEATHER_MINER_BUNDLE = register("leather_miner_bundle",
            () -> new MinerBundleItem(new Item.Properties(), 4));
    public static final RegistryObject<Item> CHAIN_MINER_BUNDLE = register("chain_miner_bundle",
            () -> new MinerBundleItem(new Item.Properties(), 8));
    public static final RegistryObject<Item> PHANTOM_MINER_BUNDLE = register("phantom_miner_bundle",
            () -> new MinerBundleItem(new Item.Properties(), 12));

    // 宝物
    public static final RegistryObject<Item> GOLDEN_BALL = register("golden_ball",
            () -> new CimeliaItem(new Item.Properties(), CimeliaItem.Category.NATURE, 2));
    public static final RegistryObject<Item> NEPHRITE = register("nephrite",
            () -> new CimeliaItem(new Item.Properties(), CimeliaItem.Category.NATURE, 2));
    public static final RegistryObject<Item> ARMOR_FRAGMENTS = register("armor_fragments",
            () -> new CimeliaItem(new Item.Properties(), CimeliaItem.Category.ANCIENT, 2));
    public static final RegistryObject<Item> ENERGETIC_CRYSTAL = register("energetic_crystal",
            () -> new CimeliaItem(new Item.Properties(), CimeliaItem.Category.NATURE, 3));

    public static final RegistryObject<Item> GRAY_QUARTZ = register("gray_quartz");
    public static final RegistryObject<Item> SOFT_ARMOR_TEMPLATE = register("soft_armor_template");
    public static final RegistryObject<Item> ASHES = register("ashes");

    // 矿石类方块物品
    public static final RegistryObject<Item> SWAMP_IRON_ORE = fromBlock(ModBlocks.SWAMP_IRON_ORE);
    public static final RegistryObject<Item> QUARTZ_GLASS = fromBlock(ModBlocks.QUARTZ_GLASS);
    public static final RegistryObject<Item> QUARTZ_GLASS_PANES = fromBlock(ModBlocks.QUARTZ_GLASS_PANES);

    // 工具
    public static final RegistryObject<Item> GEOLOGICAL_HAMMER = register("geological_hammer",
            () -> new GeologicalHammerItem(ModToolMaterials.GEOLOGICAL_HAMMER, 0, -2.4F, new Item.Properties().durability(230)));
    public static final RegistryObject<Item> COLLAPSE_STONE_HAMMER = register("collapse_stone_hammer",
            () -> new CollapseStoneHammerItem(ModToolMaterials.COLLAPSE_STONE_HAMMER, 7, -3.3F, new Item.Properties().durability(230)));

    // 装备
    public static final RegistryObject<Item> MINER_HELMET = register("miner_helmet",
            () -> new MinerHelmetItem(MinerHelmetItem.ArmorMaterials.MINER_HELMET, new Item.Properties().durability(230)));

    // 雷管
    public static final RegistryObject<Item> DETONATOR = register(ModBlocks.DETONATOR,
            (block, props) -> new DetonatorItem(block, props, 100));
    public static final RegistryObject<Item> SLIMY_DETONATOR = register(ModBlocks.SLIMY_DETONATOR,
            (block, props) -> new SlimyDetonatorItem(block, props, 100));
    public static final RegistryObject<Item> IMPACT_DETONATOR = register("impact_detonator",
            () -> new ImpactDetonatorItem(new Item.Properties()));

    // ========== 辅助注册方法 ==========
    private static RegistryObject<Item> register(String id) {
        return register(id, () -> new Item(new Item.Properties()));
    }

    private static RegistryObject<Item> register(String id, Supplier<Item> supplier) {
        return ITEMS.register(id, supplier);
    }

    private static RegistryObject<Item> fromBlock(RegistryObject<? extends Block> block) {
        return register(Objects.requireNonNull(block.getId()).getPath(), () -> new BlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Item> register(RegistryObject<? extends Block> block,
                                                 BiFunction<Block, Item.Properties, Item> itemFactory) {
        return register(Objects.requireNonNull(block.getId()).getPath(), () -> itemFactory.apply(block.get(), new Item.Properties()));
    }

    public static void registerAll(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
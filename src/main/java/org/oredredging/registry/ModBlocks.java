package org.oredredging.registry;

import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.oredredging.OreDredging;
import org.oredredging.block.DetonatorBlock;
import org.oredredging.block.GravelPilesBlock;
import org.oredredging.block.PebbleBlock;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, OreDredging.MOD_ID);

    // 花岗岩系列
    public static final RegistryObject<Block> GRANITE_BRICKS = register("granite_bricks",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.STONE_BRICKS)));
    public static final RegistryObject<Block> GRANITE_BRICK_STAIRS = register("granite_brick_stairs",
            () -> new StairBlock(() -> GRANITE_BRICKS.get().defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.STONE_BRICK_STAIRS)));
    public static final RegistryObject<Block> GRANITE_BRICK_SLAB = register("granite_brick_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.STONE_BRICK_SLAB)));
    public static final RegistryObject<Block> GRANITE_BRICK_WALL = register("granite_brick_wall",
            () -> new WallBlock(BlockBehaviour.Properties.copy(Blocks.STONE_BRICK_WALL)));

    // 闪长岩系列
    public static final RegistryObject<Block> DIORITE_BRICKS = register("diorite_bricks",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.STONE_BRICKS)));
    public static final RegistryObject<Block> DIORITE_BRICK_STAIRS = register("diorite_brick_stairs",
            () -> new StairBlock(() -> DIORITE_BRICKS.get().defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.STONE_BRICK_STAIRS)));
    public static final RegistryObject<Block> DIORITE_BRICK_SLAB = register("diorite_brick_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.STONE_BRICK_SLAB)));
    public static final RegistryObject<Block> DIORITE_BRICK_WALL = register("diorite_brick_wall",
            () -> new WallBlock(BlockBehaviour.Properties.copy(Blocks.STONE_BRICK_WALL)));

    // 安山岩系列
    public static final RegistryObject<Block> ANDESITE_BRICKS = register("andesite_bricks",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.STONE_BRICKS)));
    public static final RegistryObject<Block> ANDESITE_BRICK_STAIRS = register("andesite_brick_stairs",
            () -> new StairBlock(() -> ANDESITE_BRICKS.get().defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.STONE_BRICK_STAIRS)));
    public static final RegistryObject<Block> ANDESITE_BRICK_SLAB = register("andesite_brick_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.STONE_BRICK_SLAB)));
    public static final RegistryObject<Block> ANDESITE_BRICK_WALL = register("andesite_brick_wall",
            () -> new WallBlock(BlockBehaviour.Properties.copy(Blocks.STONE_BRICK_WALL)));

    // 碎石堆
    public static final RegistryObject<Block> STONE_GRAVEL_PILES = register("stone_gravel_piles",
            () -> new GravelPilesBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(1.5F, 2.0F).noOcclusion().pushReaction(PushReaction.DESTROY)));
    public static final RegistryObject<Block> DIORITE_GRAVEL_PILES = register("diorite_gravel_piles",
            () -> new GravelPilesBlock(BlockBehaviour.Properties.copy(STONE_GRAVEL_PILES.get()).mapColor(MapColor.QUARTZ)));
    public static final RegistryObject<Block> ANDESITE_GRAVEL_PILES = register("andesite_gravel_piles",
            () -> new GravelPilesBlock(BlockBehaviour.Properties.copy(STONE_GRAVEL_PILES.get()).mapColor(MapColor.STONE)));
    public static final RegistryObject<Block> GRANITE_GRAVEL_PILES = register("granite_gravel_piles",
            () -> new GravelPilesBlock(BlockBehaviour.Properties.copy(STONE_GRAVEL_PILES.get()).mapColor(MapColor.DIRT)));
    public static final RegistryObject<Block> SANDSTONE_GRAVEL_PILES = register("sandstone_gravel_piles",
            () -> new GravelPilesBlock(BlockBehaviour.Properties.copy(STONE_GRAVEL_PILES.get()).mapColor(MapColor.SAND)));
    public static final RegistryObject<Block> DEEPSLATE_GRAVEL_PILES = register("deepslate_gravel_piles",
            () -> new GravelPilesBlock(BlockBehaviour.Properties.copy(STONE_GRAVEL_PILES.get()).mapColor(MapColor.DEEPSLATE)));
    public static final RegistryObject<Block> TUFF_GRAVEL_PILES = register("tuff_gravel_piles",
            () -> new GravelPilesBlock(BlockBehaviour.Properties.copy(STONE_GRAVEL_PILES.get()).mapColor(MapColor.TERRACOTTA_GRAY)));
    public static final RegistryObject<Block> NETHERRACK_GRAVEL_PILES = register("netherrack_gravel_piles",
            () -> new GravelPilesBlock(BlockBehaviour.Properties.copy(STONE_GRAVEL_PILES.get()).mapColor(MapColor.NETHER)));
    public static final RegistryObject<Block> BASALT_GRAVEL_PILES = register("basalt_gravel_piles",
            () -> new GravelPilesBlock(BlockBehaviour.Properties.copy(STONE_GRAVEL_PILES.get()).mapColor(MapColor.COLOR_BLACK)));

    // 小石子
    public static final RegistryObject<Block> STONE_PEBBLE = register("stone_pebble",
            () -> new PebbleBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(1.0F, 1.0F).noOcclusion().pushReaction(PushReaction.DESTROY)));
    public static final RegistryObject<Block> DIORITE_PEBBLE = register("diorite_pebble",
            () -> new PebbleBlock(BlockBehaviour.Properties.copy(STONE_PEBBLE.get()).mapColor(MapColor.QUARTZ)));
    public static final RegistryObject<Block> ANDESITE_PEBBLE = register("andesite_pebble",
            () -> new PebbleBlock(BlockBehaviour.Properties.copy(STONE_PEBBLE.get()).mapColor(MapColor.STONE)));
    public static final RegistryObject<Block> GRANITE_PEBBLE = register("granite_pebble",
            () -> new PebbleBlock(BlockBehaviour.Properties.copy(STONE_PEBBLE.get()).mapColor(MapColor.DIRT)));
    public static final RegistryObject<Block> DEEPSLATE_PEBBLE = register("deepslate_pebble",
            () -> new PebbleBlock(BlockBehaviour.Properties.copy(STONE_PEBBLE.get()).mapColor(MapColor.DEEPSLATE)));

    // 矿石
    public static final RegistryObject<Block> SWAMP_IRON_ORE = register("swamp_iron_ore",
            () -> new DropExperienceBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(1.0F, 1.0F)));
    public static final RegistryObject<Block> QUARTZ_GLASS = register("quartz_glass",
            () -> new GlassBlock(BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ).strength(50.0F, 1200.0F).sound(SoundType.GLASS).noOcclusion()));
    public static final RegistryObject<Block> QUARTZ_GLASS_PANES = register("quartz_glass_panes",
            () -> new IronBarsBlock(BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ).strength(50.0F, 1200.0F).sound(SoundType.GLASS).noOcclusion()));

    // 雷管
    public static final RegistryObject<Block> DETONATOR = register("detonator",
            () -> new DetonatorBlock<>(BlockBehaviour.Properties.of().noOcclusion(), ModEntities.DETONATOR));
    public static final RegistryObject<Block> SLIMY_DETONATOR = register("slimy_detonator",
            () -> new DetonatorBlock<>(BlockBehaviour.Properties.of().noOcclusion(), ModEntities.SLIMY_DETONATOR));

    private static RegistryObject<Block> register(String id, Supplier<Block> blockSupplier) {
        return BLOCKS.register(id, blockSupplier);
    }

    public static void registerAll(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }
}
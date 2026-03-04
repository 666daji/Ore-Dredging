package org.oredredging.client.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import org.oredredging.registry.ModBlocks;

public class ModBlockLootTableGenerator extends FabricBlockLootTableProvider {
    public ModBlockLootTableGenerator(FabricDataOutput dataOutput) {
        super(dataOutput);
    }

    @Override
    public void generate() {
        // 花岗岩砖系列
        addDrop(ModBlocks.GRANITE_BRICKS);
        addDrop(ModBlocks.GRANITE_BRICK_STAIRS);
        addDrop(ModBlocks.GRANITE_BRICK_SLAB);
        addDrop(ModBlocks.GRANITE_BRICK_WALL);

        // 闪长岩砖系列
        addDrop(ModBlocks.DIORITE_BRICKS);
        addDrop(ModBlocks.DIORITE_BRICK_STAIRS);
        addDrop(ModBlocks.DIORITE_BRICK_SLAB);
        addDrop(ModBlocks.DIORITE_BRICK_WALL);

        // 安山岩砖系列
        addDrop(ModBlocks.ANDESITE_BRICKS);
        addDrop(ModBlocks.ANDESITE_BRICK_STAIRS);
        addDrop(ModBlocks.ANDESITE_BRICK_SLAB);
        addDrop(ModBlocks.ANDESITE_BRICK_WALL);

        // 碎石堆方块
        addDrop(ModBlocks.STONE_GRAVEL_PILES);
        addDrop(ModBlocks.DIORITE_GRAVEL_PILES);
        addDrop(ModBlocks.ANDESITE_GRAVEL_PILES);
        addDrop(ModBlocks.GRANITE_GRAVEL_PILES);
        addDrop(ModBlocks.SANDSTONE_GRAVEL_PILES);
        addDrop(ModBlocks.DEEPSLATE_GRAVEL_PILES);
        addDrop(ModBlocks.TUFF_GRAVEL_PILES);

        // 碎石堆方块
        addDrop(ModBlocks.STONE_PEBBLE);
        addDrop(ModBlocks.DIORITE_PEBBLE);
        addDrop(ModBlocks.ANDESITE_PEBBLE);
        addDrop(ModBlocks.GRANITE_PEBBLE);
        addDrop(ModBlocks.DEEPSLATE_PEBBLE);
        addDrop(ModBlocks.TUFF_PEBBLE);
    }
}
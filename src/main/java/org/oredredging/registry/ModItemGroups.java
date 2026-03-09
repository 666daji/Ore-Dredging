package org.oredredging.registry;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.oredredging.OreDredging;

public class ModItemGroups {
    public static void RegistryModItemGroups() {
        Registry.register(
                Registries.ITEM_GROUP,
                new Identifier(OreDredging.MOD_ID, "ore_dredging_group"),
                ItemGroup.create(ItemGroup.Row.TOP, -1)
                        .displayName(Text.translatable("itemGroup.ore_dredging"))
                        .icon(() -> new ItemStack(ModItems.RAW_COPPER_NUGGET))
                        .entries(((displayContext, entries) -> {
                            // 宝物
                            entries.add(ModItems.GOLDEN_BALL);
                            entries.add(ModItems.NEPHRITE);
                            entries.add(ModItems.ARMOR_FRAGMENTS);
                            entries.add(ModItems.ENERGETIC_CRYSTAL);

                            entries.add(ModItems.GRAY_QUARTZ);
                            entries.add(ModItems.SOFT_ARMOR_TEMPLATE);

                            // 粗矿粒
                            entries.add(ModItems.RAW_COPPER_NUGGET);
                            entries.add(ModItems.RAW_IRON_NUGGET);
                            entries.add(ModItems.RAW_GOLD_NUGGET);
                            entries.add(ModItems.NETHERITE_NUGGET);

                            // 花岗岩砖系列
                            entries.add(ModItems.GRANITE_BRICKS);
                            entries.add(ModItems.GRANITE_BRICK_STAIRS);
                            entries.add(ModItems.GRANITE_BRICK_SLAB);
                            entries.add(ModItems.GRANITE_BRICK_WALL);

                            // 闪长岩砖系列
                            entries.add(ModItems.DIORITE_BRICKS);
                            entries.add(ModItems.DIORITE_BRICK_STAIRS);
                            entries.add(ModItems.DIORITE_BRICK_SLAB);
                            entries.add(ModItems.DIORITE_BRICK_WALL);

                            // 安山岩砖系列
                            entries.add(ModItems.ANDESITE_BRICKS);
                            entries.add(ModItems.ANDESITE_BRICK_STAIRS);
                            entries.add(ModItems.ANDESITE_BRICK_SLAB);
                            entries.add(ModItems.ANDESITE_BRICK_WALL);

                            // 碎石堆
                            entries.add(ModItems.STONE_GRAVEL_PILES);
                            entries.add(ModItems.DIORITE_GRAVEL_PILES);
                            entries.add(ModItems.ANDESITE_GRAVEL_PILES);
                            entries.add(ModItems.GRANITE_GRAVEL_PILES);
                            entries.add(ModItems.SANDSTONE_GRAVEL_PILES);
                            entries.add(ModItems.DEEPSLATE_GRAVEL_PILES);
                            entries.add(ModItems.TUFF_GRAVEL_PILES);

                            // 矿石
                            entries.add(ModItems.SWAMP_IRON_ORE);

                            // 碎石堆
                            entries.add(ModItems.STONE_PEBBLE);
                            entries.add(ModItems.DIORITE_PEBBLE);
                            entries.add(ModItems.ANDESITE_PEBBLE);
                            entries.add(ModItems.GRANITE_PEBBLE);
                            entries.add(ModItems.DEEPSLATE_PEBBLE);

                            // 储矿袋
                            entries.add(ModItems.LEATHER_MINER_BUNDLE);
                            entries.add(ModItems.CHAIN_MINER_BUNDLE);
                            entries.add(ModItems.PHANTOM_MINER_BUNDLE);
                        }))
                        .build()
        );
    }
}
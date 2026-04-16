package org.oredredging.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.oredredging.OreDredging;

public class ModItemGroups {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, OreDredging.MOD_ID);

    public static final RegistryObject<CreativeModeTab> ORE_DREDGING_TAB = CREATIVE_TABS.register("ore_dredging_group",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.ore_dredging"))
                    .icon(() -> new ItemStack(ModItems.RAW_COPPER_NUGGET.get()))
                    .displayItems((params, output) -> {
                        // 宝物
                        output.accept(ModItems.GOLDEN_BALL.get());
                        output.accept(ModItems.NEPHRITE.get());
                        output.accept(ModItems.ARMOR_FRAGMENTS.get());
                        output.accept(ModItems.ENERGETIC_CRYSTAL.get());
                        output.accept(ModItems.GRAY_QUARTZ.get());
                        output.accept(ModItems.SOFT_ARMOR_TEMPLATE.get());

                        // 粗矿粒
                        output.accept(ModItems.RAW_COPPER_NUGGET.get());
                        output.accept(ModItems.RAW_IRON_NUGGET.get());
                        output.accept(ModItems.RAW_GOLD_NUGGET.get());
                        output.accept(ModItems.NETHERITE_NUGGET.get());

                        // 花岗岩砖系列
                        output.accept(ModItems.GRANITE_BRICKS.get());
                        output.accept(ModItems.GRANITE_BRICK_STAIRS.get());
                        output.accept(ModItems.GRANITE_BRICK_SLAB.get());
                        output.accept(ModItems.GRANITE_BRICK_WALL.get());

                        // 闪长岩砖系列
                        output.accept(ModItems.DIORITE_BRICKS.get());
                        output.accept(ModItems.DIORITE_BRICK_STAIRS.get());
                        output.accept(ModItems.DIORITE_BRICK_SLAB.get());
                        output.accept(ModItems.DIORITE_BRICK_WALL.get());

                        // 安山岩砖系列
                        output.accept(ModItems.ANDESITE_BRICKS.get());
                        output.accept(ModItems.ANDESITE_BRICK_STAIRS.get());
                        output.accept(ModItems.ANDESITE_BRICK_SLAB.get());
                        output.accept(ModItems.ANDESITE_BRICK_WALL.get());

                        // 碎石堆
                        output.accept(ModItems.STONE_GRAVEL_PILES.get());
                        output.accept(ModItems.DIORITE_GRAVEL_PILES.get());
                        output.accept(ModItems.ANDESITE_GRAVEL_PILES.get());
                        output.accept(ModItems.GRANITE_GRAVEL_PILES.get());
                        output.accept(ModItems.SANDSTONE_GRAVEL_PILES.get());
                        output.accept(ModItems.DEEPSLATE_GRAVEL_PILES.get());
                        output.accept(ModItems.TUFF_GRAVEL_PILES.get());
                        output.accept(ModItems.NETHERRACK_GRAVEL_PILES.get());
                        output.accept(ModItems.BASALT_GRAVEL_PILES.get());

                        // 小石子
                        output.accept(ModItems.STONE_PEBBLE.get());
                        output.accept(ModItems.DIORITE_PEBBLE.get());
                        output.accept(ModItems.ANDESITE_PEBBLE.get());
                        output.accept(ModItems.GRANITE_PEBBLE.get());
                        output.accept(ModItems.DEEPSLATE_PEBBLE.get());

                        // 矿石
                        output.accept(ModItems.SWAMP_IRON_ORE.get());
                        output.accept(ModItems.QUARTZ_GLASS.get());
                        output.accept(ModItems.QUARTZ_GLASS_PANES.get());

                        // 储矿袋
                        output.accept(ModItems.LEATHER_MINER_BUNDLE.get());
                        output.accept(ModItems.CHAIN_MINER_BUNDLE.get());
                        output.accept(ModItems.PHANTOM_MINER_BUNDLE.get());

                        // 工具
                        output.accept(ModItems.GEOLOGICAL_HAMMER.get());
                        output.accept(ModItems.COLLAPSE_STONE_HAMMER.get());

                        // 装备
                        output.accept(ModItems.MINER_HELMET.get());

                        // 雷管
                        output.accept(ModItems.DETONATOR.get());
                        output.accept(ModItems.SLIMY_DETONATOR.get());
                        output.accept(ModItems.IMPACT_DETONATOR.get());
                    })
                    .build());

    public static void registerAll(IEventBus modEventBus) {
        CREATIVE_TABS.register(modEventBus);
    }
}
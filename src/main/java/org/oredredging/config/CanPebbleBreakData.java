package org.oredredging.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.world.gen.blockpredicate.BlockPredicate;
import org.oredredging.registry.ModBlockTags;

import java.util.List;

public record CanPebbleBreakData(List<BlockPredicate> blocks) {
    public static final Codec<CanPebbleBreakData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    BlockPredicate.BASE_CODEC.listOf().fieldOf("canBreaks").forGetter(CanPebbleBreakData::blocks)
            ).apply(instance, CanPebbleBreakData::new)
    );

    public static final CanPebbleBreakData DEFAULT = new CanPebbleBreakData(List.of(
            // 玻璃
            BlockPredicate.matchingBlockTag(BlockTags.IMPERMEABLE),
            // 玻璃板
            BlockPredicate.matchingBlockTag(ModBlockTags.GLASS_PANES),
            // 树叶
            BlockPredicate.matchingBlockTag(BlockTags.LEAVES),
            // 花
            BlockPredicate.matchingBlockTag(BlockTags.SMALL_FLOWERS),
            // 高花
            BlockPredicate.matchingBlockTag(BlockTags.TALL_FLOWERS),
            // 可被替换的植物
            BlockPredicate.matchingBlockTag(BlockTags.REPLACEABLE),
            // 树苗
            BlockPredicate.matchingBlockTag(BlockTags.SAPLINGS),
            // 作物
            BlockPredicate.matchingBlockTag(BlockTags.CROPS),
            // 花盆
            BlockPredicate.matchingBlockTag(BlockTags.FLOWER_POTS),
            //其他
            BlockPredicate.matchingBlockTag(ModBlockTags.CAN_PEBBLE_BREAK_OTHER)
    ));
}
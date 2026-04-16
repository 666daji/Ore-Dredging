package org.oredredging.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import org.oredredging.registry.ModBlockTags;

import java.util.List;

public record CanPebbleBreakData(List<BlockPredicate> blocks) {
    public static final Codec<CanPebbleBreakData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    BlockPredicate.CODEC.listOf().fieldOf("canBreaks").forGetter(CanPebbleBreakData::blocks)
            ).apply(instance, CanPebbleBreakData::new)
    );

    public static final CanPebbleBreakData DEFAULT = new CanPebbleBreakData(List.of(
            // 玻璃
            BlockPredicate.matchesTag(BlockTags.IMPERMEABLE),
            // 玻璃板
            BlockPredicate.matchesTag(ModBlockTags.GLASS_PANES),
            // 树叶
            BlockPredicate.matchesTag(BlockTags.LEAVES),
            // 花
            BlockPredicate.matchesTag(BlockTags.SMALL_FLOWERS),
            // 高花
            BlockPredicate.matchesTag(BlockTags.TALL_FLOWERS),
            // 可被替换的植物
            BlockPredicate.matchesTag(BlockTags.REPLACEABLE),
            // 树苗
            BlockPredicate.matchesTag(BlockTags.SAPLINGS),
            // 作物
            BlockPredicate.matchesTag(BlockTags.CROPS),
            // 花盆
            BlockPredicate.matchesTag(BlockTags.FLOWER_POTS),
            //其他
            BlockPredicate.matchesTag(ModBlockTags.CAN_PEBBLE_BREAK_OTHER)
    ));
}
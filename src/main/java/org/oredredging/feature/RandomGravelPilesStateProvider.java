package org.oredredging.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProviderType;
import org.oredredging.block.GravelPilesBlock;
import org.oredredging.registry.ModBlockStateProviderTypes;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 从给定的碎石堆方块列表中随机选择一个方块，并随机设置朝向和形状。
 */
public class RandomGravelPilesStateProvider extends BlockStateProvider {
    private final List<GravelPilesBlock> blocks;

    public static final Codec<RandomGravelPilesStateProvider> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.list(ResourceLocation.CODEC)
                            .fieldOf("blocks")
                            .forGetter(provider -> provider.blocks.stream()
                                    .map(BuiltInRegistries.BLOCK::getKey)
                                    .collect(Collectors.toList()))
            ).apply(instance, RandomGravelPilesStateProvider::new));

    private RandomGravelPilesStateProvider(List<ResourceLocation> blockIds) {
        this.blocks = blockIds.stream()
                .map(BuiltInRegistries.BLOCK::get)
                .filter(block -> block instanceof GravelPilesBlock)
                .map(block -> (GravelPilesBlock) block)
                .collect(Collectors.toList());
    }

    @Override
    protected BlockStateProviderType<?> type() {
        return ModBlockStateProviderTypes.RANDOM_GRAVEL_PILES.get();
    }

    @Override
    public BlockState getState(RandomSource random, BlockPos pos) {
        if (blocks.isEmpty()) {
            return Blocks.AIR.defaultBlockState();
        }

        GravelPilesBlock selectedBlock = blocks.get(random.nextInt(blocks.size()));
        Direction facing = Direction.from2DDataValue(random.nextInt(4)); // 水平方向 0-3
        int shape = random.nextInt(selectedBlock.getMaxShapeCount()) + 1;

        return selectedBlock.defaultBlockState()
                .setValue(GravelPilesBlock.FACING, facing)
                .setValue(selectedBlock.getShapeProperty(), shape);
    }
}
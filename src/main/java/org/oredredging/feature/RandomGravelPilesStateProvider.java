package org.oredredging.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import net.minecraft.world.gen.stateprovider.BlockStateProviderType;
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
                    Codec.list(Identifier.CODEC)
                            .fieldOf("blocks")
                            .forGetter(provider -> provider.blocks.stream()
                                    .map(Registries.BLOCK::getId)
                                    .collect(Collectors.toList()))
            ).apply(instance, RandomGravelPilesStateProvider::new));

    private RandomGravelPilesStateProvider(List<Identifier> blockIds) {
        this.blocks = blockIds.stream()
                .map(Registries.BLOCK::get)
                .filter(block -> block instanceof GravelPilesBlock)
                .map(block -> (GravelPilesBlock) block)
                .collect(Collectors.toList());
    }

    @Override
    protected BlockStateProviderType<?> getType() {
        return ModBlockStateProviderTypes.RANDOM_GRAVEL_PILES;
    }

    @Override
    public BlockState get(Random random, BlockPos pos) {
        if (blocks.isEmpty()) {
            return Registries.BLOCK.get(new Identifier("minecraft:air")).getDefaultState();
        }

        // 随机生成碎石的方块状态数据
        GravelPilesBlock selectedBlock = blocks.get(random.nextInt(blocks.size()));
        Direction facing = Direction.fromHorizontal(random.nextInt(4));
        int shape = random.nextInt(5) + 1;

        // 应用属性
        return selectedBlock.getDefaultState()
                .with(GravelPilesBlock.FACING, facing)
                .with(GravelPilesBlock.SHAPE, shape);
    }
}
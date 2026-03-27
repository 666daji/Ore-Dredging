package org.oredredging.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.blockpredicate.BlockPredicate;
import net.minecraft.world.gen.feature.FeaturePlacementContext;
import net.minecraft.world.gen.placementmodifier.PlacementModifier;
import net.minecraft.world.gen.placementmodifier.PlacementModifierType;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * 一个放置修饰符，用于在特定列上根据高度图和方块谓词筛选出符合条件的生成位置。
 *
 * <p>该修饰符会针对输入的每一个 {@link BlockPos}，提取其 X 与 Z 坐标，并在该列上
 * 遍历从最低点到最高点的所有 Y 坐标，对每个 Y 对应的位置应用一组方块谓词（使用逻辑“与”）。
 * 所有通过谓词检查的位置将被输出为新的流。</p>
 *
 * <p>遍历范围可通过以下方式配置：</p>
 * <ul>
 *   <li><b>高度图（Heightmap.Type）</b>：确定该列的最高有效 Y 值，即 {@code context.getTopY(...)} 的返回值。
 *       该值通常表示最高非空气方块的上方一格（即地表/洞穴顶部的上一格）。</li>
 *   <li><b>是否包含顶部（includeTopY）</b>：若为 {@code true}，则遍历范围包含高度图返回的 Y 值（即最高非空气方块的上一格），
 *       否则只遍历到该值减一（即最高非空气方块）。默认值为 {@code false}，以保证与通常的地表生成行为一致。</li>
 *   <li><b>最低点（minY）</b>：允许显式指定遍历的起始绝对 Y 坐标。若未提供，则使用世界的底部 Y（{@code context.getBottomY()}）。
 *       这对于限制生成范围（例如仅在洞穴层及以上生成地物）非常有用。</li>
 * </ul>
 *
 * <p>方块谓词（BlockPredicate）以列表形式传入，所有谓词必须同时满足（AND 逻辑）才会保留该位置。</p>
 *
 * <p>该修饰符适用于需要在特定列上根据地形高度和自定义方块条件（如是否空气、是否可替换、是否特定方块等）生成地物的场景，
 * 尤其适合在洞穴、地表上方或其他特定高度区间内布置特征。</p>
 *
 * <p>使用示例：</p>
 * <pre>{@code
 * // 仅在基岩层到地表之间，且方块为空气或水的位置生成
 * HeightmapAndBlockPredicatesPlacementModifier.of(
 *     Heightmap.Type.WORLD_SURFACE,
 *     BlockPredicate.IS_AIR_OR_WATER
 * );
 *
 * // 包含地表上方一格（即地表之上），且方块为空气的位置生成
 * HeightmapAndBlockPredicatesPlacementModifier.of(
 *     Heightmap.Type.WORLD_SURFACE,
 *     true,   // includeTopY = true
 *     BlockPredicate.IS_AIR
 * );
 *
 * // 从 Y=40 到地表上方一格，且方块为空气的位置生成
 * HeightmapAndBlockPredicatesPlacementModifier.of(
 *     Heightmap.Type.WORLD_SURFACE,
 *     true,   // includeTopY = true
 *     40,     // minY = 40
 *     BlockPredicate.IS_AIR
 * );
 * }</pre>
 *
 * @see net.minecraft.world.gen.placementmodifier.PlacementModifier
 * @see net.minecraft.world.gen.blockpredicate.BlockPredicate
 * @see net.minecraft.world.Heightmap.Type
 */
public class ColumnScanPlacementModifier extends PlacementModifier {
    public static final Codec<ColumnScanPlacementModifier> MODIFIER_CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Heightmap.Type.CODEC.fieldOf("heightmap").forGetter(m -> m.heightmap),
                    BlockPredicate.BASE_CODEC.listOf().fieldOf("predicates").forGetter(m -> m.predicates),
                    Codec.BOOL.fieldOf("include_top_y").orElse(false).forGetter(m -> m.includeTopY),
                    Codec.INT.optionalFieldOf("min_y").forGetter(m -> m.minY)
            ).apply(instance, ColumnScanPlacementModifier::new)
    );

    // 注册时使用的类型，需在模组初始化时注册
    public static final PlacementModifierType<ColumnScanPlacementModifier> TYPE = () -> MODIFIER_CODEC;

    private final Heightmap.Type heightmap;
    private final List<BlockPredicate> predicates;
    private final boolean includeTopY;
    private final Optional<Integer> minY;

    private ColumnScanPlacementModifier(Heightmap.Type heightmap, List<BlockPredicate> predicates, boolean includeTopY, Optional<Integer> minY) {
        this.heightmap = heightmap;
        this.predicates = predicates;
        this.includeTopY = includeTopY;
        this.minY = minY;
    }

    @Override
    public Stream<BlockPos> getPositions(FeaturePlacementContext context, Random random, BlockPos pos) {
        int x = pos.getX();
        int z = pos.getZ();
        int topY = context.getTopY(this.heightmap, x, z); // 最高非空气方块 y + 1
        int upperBound = includeTopY ? topY : topY - 1;

        // 最低点：若 minY 有值则使用，否则用世界底部
        int startY = minY.orElse(context.getBottomY());

        // 若无有效范围则返回空流
        if (startY > upperBound) {
            return Stream.empty();
        }

        Stream.Builder<BlockPos> builder = Stream.builder();
        for (int y = startY; y <= upperBound; y++) {
            int xOffset = random.nextInt(10) - 5;
            int zOffset = random.nextInt(10) - 5;
            BlockPos checkPos = new BlockPos(x + xOffset, y, z + zOffset);
            boolean allMatch = true;
            for (BlockPredicate predicate : predicates) {
                if (!predicate.test(context.getWorld(), checkPos)) {
                    allMatch = false;
                    break;
                }
            }

            // 满足所有谓词
            if (allMatch) {
                builder.add(checkPos);
            }
        }
        return builder.build();
    }

    @Override
    public PlacementModifierType<?> getType() {
        return TYPE;
    }
}
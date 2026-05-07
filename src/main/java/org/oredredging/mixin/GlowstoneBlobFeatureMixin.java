package org.oredredging.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.GlowstoneBlobFeature;
import net.minecraft.world.gen.feature.util.FeatureContext;
import org.oredredging.block.FlameCrystalClusterBlock;
import org.oredredging.registry.ModBlocks;
import org.oredredging.util.RandomUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GlowstoneBlobFeature.class)
public abstract class GlowstoneBlobFeatureMixin {

    @Inject(method = "generate", at = @At("RETURN"))
    private void onGenerateComplete(FeatureContext<DefaultFeatureConfig> context,
                                    CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            StructureWorldAccess world = context.getWorld();
            BlockPos origin = context.getOrigin();
            Random random = context.getRandom();
            decorateWithHangings(world, origin, random);
        }
    }

    /**
     * 扫描荧石堆区域，随机在荧石下方悬挂紫水晶簇
     */
    @Unique
    private void decorateWithHangings(StructureWorldAccess world, BlockPos center, Random random) {
        // 与原版生成时相同的偏移范围：x,z ±7，y 0~-11
        for (int dx = -8; dx <= 8; dx++) {
            for (int dz = -8; dz <= 8; dz++) {
                for (int dy = -12; dy <= 0; dy++) {
                    BlockPos pos = center.add(dx, dy, dz);
                    // 找到荧石
                    if (world.getBlockState(pos).isOf(Blocks.GLOWSTONE)) {
                        tryAddAmethystCluster(world, pos, random);
                    }
                }
            }
        }
    }

    /**
     * 若荧石下方为空气，则有概率放置向下的紫水晶簇
     */
    @Unique
    private void tryAddAmethystCluster(StructureWorldAccess world, BlockPos glowstonePos, Random random) {
        BlockPos belowPos = glowstonePos.down();
        if (world.getBlockState(belowPos).isAir()) {
            if (RandomUtil.randomBoolean(0.05F)) {
                world.setBlockState(
                        belowPos,
                        ModBlocks.FLAME_CRYSTAL_CLUSTER.getDefaultState()
                                .with(FlameCrystalClusterBlock.FACING, Direction.DOWN)
                                .with(FlameCrystalClusterBlock.WATERLOGGED, false),
                        Block.NOTIFY_LISTENERS
                );
            }
        }
    }
}
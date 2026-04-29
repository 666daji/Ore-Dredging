package org.oredredging.feature;

import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;
import org.oredredging.OreDredging;

public class WrapOreFeature extends Feature<WrapOreFeatureConfig> {

    public WrapOreFeature(Codec<WrapOreFeatureConfig> configCodec) {
        super(configCodec);
    }

    @Override
    public boolean generate(FeatureContext<WrapOreFeatureConfig> context) {
        StructureWorldAccess world = context.getWorld();
        BlockPos origin = context.getOrigin();
        Random random = context.getRandom();
        WrapOreFeatureConfig config = context.getConfig();

        int size = config.coreSize().get(random);
        if (size < 1) return false;

        int minX = origin.getX();
        int minY = origin.getY();
        int minZ = origin.getZ();
        int maxX = minX + (size + 2);
        int maxY = minY + (size + 2);
        int maxZ = minZ + (size + 2);

        // 检查所有将要被放置方块的位置是否均可替换
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos checkPos = new BlockPos(x, y, z);
                    BlockState existing = world.getBlockState(checkPos);
                    if (existing.isReplaceable() || existing.isAir()) {
                        return false;
                    }
                }
            }
        }

        OreDredging.LOGGER.info("{}", origin);

        // 核心区域范围
        int coreMinX = origin.getX();
        int coreMinY = origin.getY();
        int coreMinZ = origin.getZ();
        int coreMaxX = coreMinX + size - 1;
        int coreMaxY = coreMinY + size - 1;
        int coreMaxZ = coreMinZ + size - 1;

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    boolean isCore = (x >= coreMinX && x <= coreMaxX &&
                            y >= coreMinY && y <= coreMaxY &&
                            z >= coreMinZ && z <= coreMaxZ);
                    BlockState state = isCore ? config.coreProvider().get(random, pos)
                            : config.shellProvider().get(random, pos);
                    setBlockState(world, pos, state);
                }
            }
        }

        return true;
    }
}
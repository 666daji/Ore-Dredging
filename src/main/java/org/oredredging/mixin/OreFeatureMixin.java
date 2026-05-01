package org.oredredging.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.world.gen.feature.OreFeature;
import org.oredredging.registry.ModBlocks;
import org.oredredging.util.RandomUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(OreFeature.class)
public class OreFeatureMixin {

    @ModifyArg(method = "generateVeinPart", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/ChunkSection;setBlockState(IIILnet/minecraft/block/BlockState;Z)Lnet/minecraft/block/BlockState;"), index = 3)
    private BlockState replaceOre(BlockState state) {
        if (RandomUtil.randomBoolean(0.05F)) {
            if (state.isOf(Blocks.IRON_ORE)) {
                return ModBlocks.NATURAL_RAW_IRON_BLOCK.getDefaultState();
            } else if (state.isOf(Blocks.COPPER_ORE)) {
                return ModBlocks.NATURAL_RAW_COPPER_BLOCK.getDefaultState();
            }
        }

        return state;
    }
}

package org.oredredging.block;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.oredredging.block.entity.MimeticLeyLineBlockEntity;
import org.oredredging.registry.ModBlockEntities;

public class MimeticLeyLineBlock extends BlockWithEntity {
    public static final VoxelShape SHAPE = Block.createCuboidShape(6, 6, 6, 10, 10 ,10);

    public MimeticLeyLineBlock(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack handStack = player.getStackInHand(hand);

        if (world.getBlockEntity(pos) instanceof MimeticLeyLineBlockEntity blockEntity && handStack.isOf(Items.REDSTONE)) {
            if (!world.isClient()) {
                blockEntity.addProgress(3 * 60 * 20);
                if (player.isCreative()) {
                    handStack.decrement(1);
                }
            }

            // 添加粒子
            for (int l = 0; l < 5; l++) {
                world.addParticle(new DustParticleEffect(DustParticleEffect.RED, 1),
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        world.random.nextDouble(), 1.0, world.random.nextDouble());
            }
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new MimeticLeyLineBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, ModBlockEntities.MIMETIC_LEY_LINE, MimeticLeyLineBlockEntity::tick);
    }
}

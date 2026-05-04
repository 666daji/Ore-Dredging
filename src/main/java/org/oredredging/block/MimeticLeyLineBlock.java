package org.oredredging.block;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;
import org.oredredging.block.entity.MimeticLeyLineBlockEntity;
import org.oredredging.registry.ModBlockEntities;
import org.oredredging.registry.ModItems;
import org.oredredging.registry.ModSoundEvent;

public class MimeticLeyLineBlock extends BlockWithEntity {
    public static final EnumProperty<MimeticLeyLineBlockEntity.State> STATE =
            EnumProperty.of("state", MimeticLeyLineBlockEntity.State.class);
    public static final IntProperty LIGHT = IntProperty.of("light", 0, 4);
    public static final VoxelShape SHAPE = Block.createCuboidShape(6, 6, 6, 10, 10, 10);
    public static final VoxelShape GROUND_SHAPE = Block.createCuboidShape(6, 0, 6, 10, 4, 10);

    public MimeticLeyLineBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(STATE, MimeticLeyLineBlockEntity.State.DORMANT));
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack handStack = player.getStackInHand(hand);

        if (world.getBlockEntity(pos) instanceof MimeticLeyLineBlockEntity blockEntity
                && handStack.isOf(Items.REDSTONE)
                && blockEntity.getState() != MimeticLeyLineBlockEntity.State.DORMANT) {
            if (!world.isClient()) {
                blockEntity.addProgress(3 * 60 * 20);
                if (!player.isCreative()) {
                    handStack.decrement(1);
                }
            }

            for (int l = 0; l < 5; l++) {
                world.addParticle(new DustParticleEffect(DustParticleEffect.RED, 1),
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        world.random.nextDouble(), 1.0, world.random.nextDouble());
            }
        } else {
            player.giveItemStack(new ItemStack(ModItems.MIMETIC_LEY_LINE));
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
        }

        return ActionResult.SUCCESS;
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (state.get(STATE) == MimeticLeyLineBlockEntity.State.DORMANT) {
            return;
        }

        double d = pos.getX() + 0.5;
        double e = pos.getY() + 0.5;
        double f = pos.getZ() + 0.5;

        if (world.getBlockEntity(pos) instanceof MimeticLeyLineBlockEntity blockEntity && random.nextDouble() < 0.1) {
            switch (blockEntity.getState()) {
                case AMASS -> {
                    if (random.nextDouble() < 0.5) {
                        world.playSound(d, e, f, ModSoundEvent.MLL_AMASS, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
                    }
                }
                case BUDDING -> {
                    if (random.nextDouble() < 0.4) {
                        world.playSound(d, e, f, ModSoundEvent.MLL_BUDDING_RA, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
                        blockEntity.setBuddingParticleTime(4 * 20);
                    }
                }
            }
        }
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(STATE, LIGHT);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return state.get(STATE) == MimeticLeyLineBlockEntity.State.DORMANT ?
                GROUND_SHAPE:SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new MimeticLeyLineBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, ModBlockEntities.MIMETIC_LEY_LINE, MimeticLeyLineBlockEntity::tick);
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (state.get(STATE) != MimeticLeyLineBlockEntity.State.DORMANT) {
            return;
        }

        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof MimeticLeyLineBlockEntity mimetic) {
            mimetic.tryStartFalling(world);
        }
    }

    @Override
    public BlockState getStateForNeighborUpdate(
            BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos
    ) {
        world.scheduleBlockTick(pos, state.getBlock(), 5);
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        world.scheduleBlockTick(pos, this, 5);
    }
}
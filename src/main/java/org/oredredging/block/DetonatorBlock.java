package org.oredredging.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;
import org.oredredging.entity.AbstractDetonatorEntity;

import java.util.List;

public class DetonatorBlock<T extends AbstractDetonatorEntity> extends Block {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty UNSTABLE = BooleanProperty.create("unstable");
    public static final VoxelShape VOXEL_SHAPE_X = Block.box(6, 0, 2, 10, 4, 14);
    public static final VoxelShape VOXEL_SHAPE_Z = Block.box(2, 0, 6, 14, 4, 10);
    public final RegistryObject<EntityType<T>> BASE_DETONATOR;

    public DetonatorBlock(Properties properties, RegistryObject<EntityType<T>> baseDetonator) {
        super(properties);
        this.BASE_DETONATOR = baseDetonator;
        this.registerDefaultState(this.getStateDefinition().any()
                .setValue(FACING, Direction.NORTH)
                .setValue(UNSTABLE, false));
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState()
                .setValue(FACING, ctx.getHorizontalDirection().getOpposite())
                .setValue(UNSTABLE, false);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        if (state.getValue(FACING).getAxis() == Direction.Axis.X) {
            return VOXEL_SHAPE_X;
        }
        return VOXEL_SHAPE_Z;
    }

    protected void primeDetonator(Level world, BlockPos pos, @Nullable LivingEntity igniter) {
        if (world.isClientSide) return;

        AbstractDetonatorEntity entity = BASE_DETONATOR.get().create(world);
        if (entity != null) {
            entity.setOwner(igniter);
            entity.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            entity.setIgniteTime(100);
            world.addFreshEntity(entity);
            world.playSound(null, pos, SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
            world.gameEvent(igniter, GameEvent.PRIME_FUSE, pos);
        }
    }

    private void throwPrimedDetonator(Level world, BlockPos pos, Player player) {
        if (world.isClientSide) return;

        AbstractDetonatorEntity entity = BASE_DETONATOR.get().create(world);
        if (entity != null) {
            entity.setOwner(player);
            entity.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            entity.setIgniteTime(100);
            world.addFreshEntity(entity);
            world.playSound(player, player.blockPosition(), SoundEvents.FLINTANDSTEEL_USE,
                    SoundSource.PLAYERS, 1.0F, 1.0F);
            world.gameEvent(player, GameEvent.PRIME_FUSE, pos);
        }
    }

    @Override
    public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!oldState.is(state.getBlock())) {
            if (world.hasNeighborSignal(pos)) {
                primeDetonator(world, pos, null);
                world.removeBlock(pos, false);
            }
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean isMoving) {
        if (world.hasNeighborSignal(pos)) {
            primeDetonator(world, pos, null);
            world.removeBlock(pos, false);
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.is(Items.FLINT_AND_STEEL) || stack.is(Items.FIRE_CHARGE)) {
            if (!world.isClientSide) {
                if (stack.is(Items.FLINT_AND_STEEL)) {
                    stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
                } else {
                    stack.shrink(1);
                }
                throwPrimedDetonator(world, pos, player);
                world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            }
            return InteractionResult.sidedSuccess(world.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        return super.getDrops(state, builder);
    }

    @Override
    public void wasExploded(Level world, BlockPos pos, Explosion explosion) {
        if (world.isClientSide) return;
        primeDetonator(world, pos, explosion.getIndirectSourceEntity());
        world.removeBlock(pos, false);
    }

    @Override
    public void onProjectileHit(Level world, BlockState state, BlockHitResult hit, Projectile projectile) {
        if (!world.isClientSide && projectile.isOnFire() && projectile.mayInteract(world, hit.getBlockPos())) {
            BlockPos pos = hit.getBlockPos();
            LivingEntity igniter = projectile.getOwner() instanceof LivingEntity ? (LivingEntity) projectile.getOwner() : null;
            primeDetonator(world, pos, igniter);
            world.removeBlock(pos, false);
        }
    }

    @Override
    public boolean dropFromExplosion(Explosion explosion) {
        return false;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, UNSTABLE);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }
}
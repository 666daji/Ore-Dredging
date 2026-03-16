package org.oredredging.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.oredredging.util.RandomUtil;

public class CollapseStoneHammerItem extends SwordItem implements CrushedDropGain{
    private static final float CHARGE_PER_TICK = 0.05F;
    private static final double MAX_DISTANCE = 7.0;

    public CollapseStoneHammerItem(ToolMaterial toolMaterial, int attackDamage, float attackSpeed, Settings settings) {
        super(toolMaterial, attackDamage, attackSpeed, settings);
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (!(user instanceof PlayerEntity player)) return;

        BlockHitResult hitResult = raycast(world, user);
        if (hitResult.getType() != HitResult.Type.BLOCK) {
            // 没有瞄准方块
            clearTargetAndProgress(stack, world, player);
            return;
        }

        BlockPos currentPos = hitResult.getBlockPos();
        BlockState state = world.getBlockState(currentPos);
        float hardness = state.getHardness(world, currentPos);

        // 方块不可破坏则清除
        if (hardness < 0.0F || state.isAir()) {
            clearTargetAndProgress(stack, world, player);
            return;
        }

        // 读取NBT中存储的上一次目标与累计能量
        NbtCompound nbt = stack.getOrCreateNbt();
        boolean hasTarget = nbt.contains("TargetX");
        BlockPos storedPos = null;
        float accumulatedEnergy = 0.0F;

        if (hasTarget) {
            int x = nbt.getInt("TargetX");
            int y = nbt.getInt("TargetY");
            int z = nbt.getInt("TargetZ");
            storedPos = new BlockPos(x, y, z);
            accumulatedEnergy = nbt.getFloat("Energy");
        }

        // 目标是否相同
        boolean sameTarget = storedPos != null && storedPos.equals(currentPos);

        if (!sameTarget) {
            // 目标改变：清除旧位置的进度条
            if (storedPos != null) {
                world.setBlockBreakingInfo(player.getId(), storedPos, -1);
            }

            accumulatedEnergy = 0.0F;
            setTarget(nbt, currentPos);
        }

        // 本次蓄力产生的能量
        int usedTicks = stack.getMaxUseTime() - remainingUseTicks;
        float addedEnergy = usedTicks * CHARGE_PER_TICK;
        accumulatedEnergy += addedEnergy;

        // 判断是否能破坏方块
        if (accumulatedEnergy >= hardness) {
            world.breakBlock(currentPos, false, player);
            BlockEntity blockEntity = state.hasBlockEntity() ? world.getBlockEntity(currentPos) : null;
            Block.dropStacks(state, world, currentPos, blockEntity, player, stack);
            stack.damage(1, player, p -> p.sendToolBreakStatus(player.getActiveHand()));
            clearTargetAndProgress(stack, world, player);
        } else {
            nbt.putFloat("Energy", accumulatedEnergy);

            // 计算进度值
            int progress = (int) ((accumulatedEnergy / hardness) * 9);
            world.setBlockBreakingInfo(player.getId(), currentPos, progress);
            for (int i = 0; i < 10; i++) {
                world.addParticle(
                        new BlockStateParticleEffect(ParticleTypes.BLOCK, state),
                        currentPos.getX() + 0.5, currentPos.getY() + 0.5, currentPos.getZ() + 0.5,
                        RandomUtil.nextInt(7) - 3, RandomUtil.nextInt(7) - 3, RandomUtil.nextInt(7) - 3
                );
            }
            world.playSound(player, currentPos, state.getSoundGroup().getBreakSound(), SoundCategory.BLOCKS, 0.8F, 1.0F);
        }

        player.swingHand(Hand.MAIN_HAND);
        player.getItemCooldownManager().set(this, 10);
    }

    /**
     * 执行射线检测，获取玩家瞄准的方块
     */
    private BlockHitResult raycast(World world, LivingEntity user) {
        Vec3d eyePos = user.getEyePos();
        Vec3d lookVec = user.getRotationVec(1.0F);
        Vec3d endPos = eyePos.add(lookVec.multiply(CollapseStoneHammerItem.MAX_DISTANCE));
        RaycastContext context = new RaycastContext(
                eyePos,
                endPos,
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                user
        );
        return world.raycast(context);
    }

    /**
     * 将目标位置写入NBT
     */
    private void setTarget(NbtCompound nbt, BlockPos pos) {
        nbt.putInt("TargetX", pos.getX());
        nbt.putInt("TargetY", pos.getY());
        nbt.putInt("TargetZ", pos.getZ());
    }

    /**
     * 清除NBT中存储的目标和能量，并移除破坏进度条
     */
    private void clearTargetAndProgress(ItemStack stack, World world, PlayerEntity player) {
        NbtCompound nbt = stack.getNbt();
        if (nbt != null) {
            // 如果有旧目标，移除其进度条
            if (nbt.contains("TargetX")) {
                BlockPos oldPos = new BlockPos(
                        nbt.getInt("TargetX"),
                        nbt.getInt("TargetY"),
                        nbt.getInt("TargetZ")
                );
                world.setBlockBreakingInfo(player.getId(), oldPos, -1);
            }
            // 清除NBT数据
            nbt.remove("TargetX");
            nbt.remove("TargetY");
            nbt.remove("TargetZ");
            nbt.remove("Energy");
        }
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        user.setCurrentHand(hand);
        return TypedActionResult.consume(user.getStackInHand(hand));
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return 70000;
    }

    @Override
    public int getProbability(int original) {
        return (int) (original * 0.38);
    }
}
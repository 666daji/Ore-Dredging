package org.oredredging.item;

import net.minecraft.block.Block;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;

public abstract class BaseDetonatorItem extends BlockItem implements Wave{
    public final int maxIgniteTime;

    public BaseDetonatorItem(Block block, Settings settings, int maxIgniteTime) {
        super(block, settings);
        this.maxIgniteTime = maxIgniteTime;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (useConsume(world, user, hand)) {
            user.setCurrentHand(hand);
            return TypedActionResult.consume(stack);
        }
        return TypedActionResult.fail(stack);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (allowPlace(context)) {
            return super.useOnBlock(context);
        }

        return ActionResult.PASS;
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (!(user instanceof PlayerEntity player)) return;

        int usedTicks = this.getMaxUseTime(stack) - remainingUseTicks;

        world.playSound(null, user.getX(), user.getY(), user.getZ(),
                SoundEvents.ENTITY_EGG_THROW, SoundCategory.PLAYERS, 0.5F,
                0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));

        spawnDetonator(world, player, stack, usedTicks);

        player.incrementStat(Stats.USED.getOrCreateStat(this));
        if (!player.getAbilities().creativeMode) {
            stack.decrement(1);
        }

        player.swingHand(Hand.MAIN_HAND);
        player.getItemCooldownManager().set(this, 10);
        world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0F, 1.0F);
    }

    /**
     * 生成对应的已点燃雷管。
     *
     * @param player 抛出雷管的玩家
     * @param stack 原雷管物品堆栈
     * @param usedTicks 已经使用的时间
     */
    protected abstract void spawnDetonator(World world, PlayerEntity player, ItemStack stack, int usedTicks);

    /**
     * 检查雷管是否允许放置。
     *
     * @param context 放置上下文
     * @return 是否可以放置
     */
    protected boolean allowPlace(ItemUsageContext context) {
        if (context.getPlayer() != null) {
            return context.getPlayer().isSneaking();
        }

        return false;
    }

    /**
     * 使用雷管时产生消耗。
     * <p>例如减少打火石的耐久。</p>
     *
     * @param user 尝试点燃雷管的玩家
     * @param hand 点燃使用的手
     * @return 是否成功点燃
     */
    protected boolean useConsume(World world, PlayerEntity user, Hand hand) {
        if (hand == Hand.MAIN_HAND) {
            ItemStack offStack = user.getStackInHand(Hand.OFF_HAND);
            if (offStack.isOf(Items.FLINT_AND_STEEL)) {
                world.playSound(user, user.getBlockPos(), SoundEvents.ITEM_FLINTANDSTEEL_USE,
                        SoundCategory.PLAYERS, 1.0F, 1.0F);
                offStack.damage(1, user, player -> player.sendEquipmentBreakStatus(EquipmentSlot.OFFHAND));
                user.swingHand(Hand.OFF_HAND);
                return true;
            }
        }
        return false;
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        // 生成火花粒子
        if (maxIgniteTime != -1) {
            Hand hand = user.getActiveHand();
            spawnSparkParticlesFromHand(world, user, stack, remainingUseTicks, hand);
        }

        // 检查是否超时，触发手中爆炸
        if (world.isClient || maxIgniteTime == -1) return;

        int usedTicks = this.getMaxUseTime(stack) - remainingUseTicks;
        if (usedTicks >= maxIgniteTime) {
            if (user instanceof PlayerEntity player) {
                explodeInHand(world, player, stack);
                user.clearActiveItem();
            }
        }
    }

    /**
     * 从玩家手部位置生成火花粒子（客户端）
     *
     * @param world              世界
     * @param user               玩家
     * @param stack              物品栈
     * @param remainingUseTicks  剩余使用 tick 数
     * @param hand               使用的手（主手或副手）
     */
    private void spawnSparkParticlesFromHand(World world, LivingEntity user, ItemStack stack, int remainingUseTicks, Hand hand) {
        int usedTicks = this.getMaxUseTime(stack) - remainingUseTicks;
        int remainingTicks = maxIgniteTime - usedTicks;  // 剩余引信时间

        // 剩余时间越少，粒子越多（最少1个，最多5个）
        int count = Math.max(1, 5 - (remainingTicks / 20));
        count = Math.min(5, count);

        // 计算手部位置
        Vec3d forward = user.getRotationVec(1.0F);               // 玩家面向方向
        Vec3d right = forward.rotateY((float) Math.PI / 2);      // 玩家右侧方向
        double forwardDist = 0.4;   // 前伸距离
        double rightDist = (hand == Hand.MAIN_HAND) ? -0.5 : 0.5; // 主手右偏正，副手左偏负
        double upDist = 1.2;        // 手部高度

        Vec3d handPos = user.getPos()
                .add(forward.multiply(forwardDist))
                .add(right.multiply(rightDist))
                .add(0, upDist, 0);

        for (int i = 0; i < count; i++) {
            // 在基础位置周围添加随机散布
            double spread = 0.1;
            double x = handPos.x + (user.getRandom().nextDouble() - 0.5) * spread;
            double y = handPos.y + (user.getRandom().nextDouble() - 0.5) * spread;
            double z = handPos.z + (user.getRandom().nextDouble() - 0.5) * spread;

            double vx = (user.getRandom().nextDouble() - 0.5) * 0.1;
            double vy = user.getRandom().nextDouble() * 0.2;
            double vz = (user.getRandom().nextDouble() - 0.5) * 0.1;

            world.addParticle(ParticleTypes.FLAME, x, y, z, vx, vy, vz);
            if (user.getRandom().nextInt(3) == 0) {
                world.addParticle(ParticleTypes.SMOKE, x, y, z, vx * 0.5, vy * 0.5, vz * 0.5);
            }
        }
    }

    /**
     * 在手中直接爆炸雷管。
     * 用于温雷时间过长导致雷管已经燃烧完毕的情况。
     *
     * @param player 使用雷管的玩家
     * @param stack 原雷管堆栈
     */
    private void explodeInHand(World world, PlayerEntity player, ItemStack stack) {
        Explosion explosion = world.createExplosion(player, player.getX(), player.getY(), player.getZ(), 8.0F, World.ExplosionSourceType.MOB);
        player.damage(world.getDamageSources().explosion(explosion), 10.0F);
        if (!player.getAbilities().creativeMode) {
            stack.decrement(1);
        }
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return 10000;
    }
}
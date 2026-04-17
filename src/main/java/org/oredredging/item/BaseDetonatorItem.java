package org.oredredging.item;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public abstract class BaseDetonatorItem extends BlockItem implements Wave{
    public final int maxIgniteTime;

    public BaseDetonatorItem(Block block, Properties properties, int maxIgniteTime) {
        super(block, properties);
        this.maxIgniteTime = maxIgniteTime;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (useConsume(level, player, hand)) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }
        return InteractionResultHolder.fail(stack);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        if (allowPlace(context)) {
            return super.useOn(context);
        }
        return InteractionResult.PASS;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity living, int timeCharged) {
        if (!(living instanceof Player player)) return;

        int usedTicks = this.getUseDuration(stack) - timeCharged;

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.EGG_THROW, SoundSource.PLAYERS, 0.5F,
                0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));

        spawnDetonator(level, player, stack, usedTicks);

        player.awardStat(Stats.ITEM_USED.get(this));
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        player.swing(InteractionHand.MAIN_HAND);
        player.getCooldowns().addCooldown(this, 10);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    /**
     * 生成对应的已点燃雷管。
     *
     * @param player 抛出雷管的玩家
     * @param stack 原雷管物品堆栈
     * @param usedTicks 已经使用的时间
     */
    protected abstract void spawnDetonator(Level level, Player player, ItemStack stack, int usedTicks);

    protected boolean allowPlace(UseOnContext context) {
        if (context.getPlayer() != null) {
            return context.getPlayer().isShiftKeyDown();
        }

        return false;
    }

    protected boolean useConsume(Level level, Player player, InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND) {
            ItemStack offStack = player.getItemInHand(InteractionHand.OFF_HAND);
            if (offStack.is(Items.FLINT_AND_STEEL)) {
                level.playSound(player, player.blockPosition(), SoundEvents.FLINTANDSTEEL_USE,
                        SoundSource.PLAYERS, 1.0F, 1.0F);
                offStack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(EquipmentSlot.OFFHAND));
                player.swing(InteractionHand.OFF_HAND);
                return true;
            }
        }
        return false;
    }

    @Override
    public void onUseTick(Level level, LivingEntity living, ItemStack stack, int remainingUseDuration) {
        if (maxIgniteTime != -1) {
            InteractionHand hand = living.getUsedItemHand();
            spawnSparkParticlesFromHand(level, living, stack, remainingUseDuration, hand);
        }

        if (level.isClientSide() || maxIgniteTime == -1) return;

        int usedTicks = this.getUseDuration(stack) - remainingUseDuration;
        if (usedTicks >= maxIgniteTime) {
            if (living instanceof Player player) {
                explodeInHand(level, player, stack);
                player.releaseUsingItem();
            }
        }
    }

    private void spawnSparkParticlesFromHand(Level level, LivingEntity living, ItemStack stack, int remainingUseDuration, InteractionHand hand) {
        int usedTicks = this.getUseDuration(stack) - remainingUseDuration;
        int remainingTicks = maxIgniteTime - usedTicks;

        int count = Math.max(1, 5 - (remainingTicks / 20));
        count = Math.min(5, count);

        Vec3 forward = living.getLookAngle();
        Vec3 right = forward.yRot((float) Math.PI / 2);
        double forwardDist = 0.4;
        double rightDist = (hand == InteractionHand.MAIN_HAND) ? -0.5 : 0.5;
        double upDist = 1.2;

        Vec3 handPos = living.position()
                .add(forward.scale(forwardDist))
                .add(right.scale(rightDist))
                .add(0, upDist, 0);

        for (int i = 0; i < count; i++) {
            double spread = 0.1;
            double x = handPos.x + (living.getRandom().nextDouble() - 0.5) * spread;
            double y = handPos.y + (living.getRandom().nextDouble() - 0.5) * spread;
            double z = handPos.z + (living.getRandom().nextDouble() - 0.5) * spread;

            double vx = (living.getRandom().nextDouble() - 0.5) * 0.1;
            double vy = living.getRandom().nextDouble() * 0.2;
            double vz = (living.getRandom().nextDouble() - 0.5) * 0.1;

            level.addParticle(ParticleTypes.FLAME, x, y, z, vx, vy, vz);
            if (living.getRandom().nextInt(3) == 0) {
                level.addParticle(ParticleTypes.SMOKE, x, y, z, vx * 0.5, vy * 0.5, vz * 0.5);
            }
        }
    }

    private void explodeInHand(Level level, Player player, ItemStack stack) {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.explode(player, player.getX(), player.getY(), player.getZ(), 8.0F, Level.ExplosionInteraction.MOB);
            player.hurt(serverLevel.damageSources().explosion(null), 10.0F);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 10000;
    }
}
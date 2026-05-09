package org.oredredging.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.oredredging.entity.ImpactDetonatorEntity;

public class ImpactDetonatorItem extends Item implements Wave{
    public final float power;

    public ImpactDetonatorItem(Settings settings, float power) {
        super(settings);
        this.power = power;
    }

    public ImpactDetonatorItem(Settings settings) {
        this(settings, 6.0F);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        user.setCurrentHand(hand);
        return TypedActionResult.consume(stack);
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (!(user instanceof PlayerEntity player)) return;

        int usedTicks = this.getMaxUseTime(stack) - remainingUseTicks;

        world.playSound(null, user.getX(), user.getY(), user.getZ(),
                SoundEvents.ENTITY_EGG_THROW, SoundCategory.PLAYERS, 0.5F,
                0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));

        ImpactDetonatorEntity entity = new ImpactDetonatorEntity(world, player);
        entity.setPower(power);
        entity.setItem(stack);
        entity.setVelocity(player, player.getPitch(), player.getYaw(), 0.0F, usedTicks * 0.05F, 1.0F);
        entity.setIgniteTime(-1);
        world.spawnEntity(entity);

        player.incrementStat(Stats.USED.getOrCreateStat(this));
        if (!player.getAbilities().creativeMode) {
            stack.decrement(1);
        }

        player.swingHand(Hand.MAIN_HAND);
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return 10000;
    }
}
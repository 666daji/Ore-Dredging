package org.oredredging.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.oredredging.entity.ImpactDetonatorEntity;

public class ImpactDetonatorItem extends Item implements Wave{
    public ImpactDetonatorItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity living, int timeCharged) {
        if (!(living instanceof Player player)) return;

        int usedTicks = this.getUseDuration(stack) - timeCharged;

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.EGG_THROW, SoundSource.PLAYERS, 0.5F,
                0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));

        ImpactDetonatorEntity entity = new ImpactDetonatorEntity(level, player);
        entity.setItem(stack);
        entity.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, usedTicks * 0.05F, 1.0F);
        entity.setIgniteTime(-1);
        level.addFreshEntity(entity);

        player.awardStat(Stats.ITEM_USED.get(this));
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        player.swing(InteractionHand.MAIN_HAND);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 10000;
    }
}
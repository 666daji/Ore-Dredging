package org.oredredging.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.oredredging.entity.PebbleEntity;

public class PebbleItem extends BlockItem {
    protected final Performance performance;

    public PebbleItem(Block block, Properties properties, Performance performance) {
        super(block, properties);
        this.performance = performance;
    }

    public PebbleItem(Block block, Properties properties) {
        this(block, properties, Performance.STONE);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        level.playSound(
                null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.EGG_THROW, SoundSource.PLAYERS,
                0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F)
        );

        if (!level.isClientSide) {
            PebbleEntity pebbleEntity = new PebbleEntity(level, player);
            pebbleEntity.setItem(itemStack);
            pebbleEntity.shootFromRotation(player, player.getXRot(), player.getYRot(),
                    0.5F, performance.speed(), 1.0F);
            level.addFreshEntity(pebbleEntity);

            player.getCooldowns().addCooldown(this, performance.attackSpeed());
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        if (!player.getAbilities().instabuild) {
            itemStack.shrink(1);
        }

        return InteractionResultHolder.success(itemStack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) {
            return super.useOn(context);
        }

        return InteractionResult.PASS;
    }

    public Performance getPerformance() {
        return performance;
    }

    /**
     * 表示一个石子的投掷属性。
     *
     * @param attackSpeed 攻击速度（冷却时间 tick）
     * @param hurt        伤害值
     * @param speed       初速度
     * @param gravity     重力加速度
     */
    public record Performance(int attackSpeed, float hurt, float speed, float gravity) {
        public static final Performance STONE = new Performance(8, 4F, 2.5F, 0.1F);
        public static final Performance DEEPSLATE = new Performance(4, 3F, 3.5F, 0.07F);
        public static final Performance DIORITE = new Performance(10, 5F, 2.5F, 0.14F);
        public static final Performance GRANITE = new Performance(12, 4.5F, 3F, 0.08F);
        public static final Performance ANDESITE = new Performance(9, 4.5F, 2.5F, 0.13F);
    }
}
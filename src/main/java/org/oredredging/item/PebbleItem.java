package org.oredredging.item;

import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.oredredging.entity.PebbleEntity;

public class PebbleItem extends BlockItem {
    protected final Performance performance;

    public PebbleItem(Block block, Item.Settings settings, Performance performance) {
        super(block, settings);
        this.performance = performance;
    }

    public PebbleItem(Block block, Item.Settings settings) {
        this(block, settings, Performance.STONE);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        world.playSound(
                null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_EGG_THROW, SoundCategory.PLAYERS, 0.5F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F)
        );
        if (!world.isClient) {
            PebbleEntity pebbleEntity = new PebbleEntity(world, user);
            pebbleEntity.setItem(itemStack);
            pebbleEntity.setVelocity(user, user.getPitch(), user.getYaw(), 0.5F, performance.speed(), 1.0F);
            world.spawnEntity(pebbleEntity);

            if (user instanceof PlayerEntity) {
                user.getItemCooldownManager().set(this, performance.attackSpeed());
            }
        }

        user.incrementStat(Stats.USED.getOrCreateStat(this));
        if (!user.getAbilities().creativeMode) {
            itemStack.decrement(1);
        }

        return TypedActionResult.success(itemStack, world.isClient());
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (context.getPlayer() != null && context.getPlayer().isSneaking()) {
            return super.useOnBlock(context);
        }

        return ActionResult.PASS;
    }

    public Performance getPerformance() {
        return performance;
    }

    /**
     * 表示一个石子的投掷属性。
     *
     * @param attackSpeed
     * @param hurt
     * @param speed
     */
    public record Performance(int attackSpeed, float hurt, float speed, float gravity) {
        public static final Performance STONE = new Performance(8, 4F, 2.5F, 0.1F);
        public static final Performance DEEPSLATE = new Performance(4, 3F, 3.5F, 0.07F);
        public static final Performance DIORITE = new Performance(10, 5F, 2.5F, 0.14F);
        public static final Performance GRANITE = new Performance(12, 4.5F, 3F, 0.08F);
        public static final Performance ANDESITE = new Performance(9, 4.5F, 2.5F, 0.13F);
    }
}

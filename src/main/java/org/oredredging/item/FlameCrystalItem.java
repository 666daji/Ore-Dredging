package org.oredredging.item;

import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.oredredging.entity.FlameCrystalEntity;

public class FlameCrystalItem extends BlockCimeliaItem {
    public FlameCrystalItem(Block block, Settings settings) {
        super(block, settings, Category.NATURE, 3);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        world.playSound(
                null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_EGG_THROW, SoundCategory.PLAYERS, 0.5F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F)
        );
        if (!world.isClient) {
            FlameCrystalEntity flameCrystalEntity = new FlameCrystalEntity(world, user);
            flameCrystalEntity.setItem(itemStack);
            flameCrystalEntity.setVelocity(user, user.getPitch(), user.getYaw(), 0.5F, 2.5F, 1.0F);
            world.spawnEntity(flameCrystalEntity);

            if (user instanceof PlayerEntity) {
                user.getItemCooldownManager().set(this, 8);
            }
        }

        user.incrementStat(Stats.USED.getOrCreateStat(this));
        if (!user.getAbilities().creativeMode) {
            itemStack.decrement(1);
        }

        return TypedActionResult.success(itemStack, world.isClient());
    }
}

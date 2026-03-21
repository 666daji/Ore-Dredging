package org.oredredging.item;

import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.oredredging.entity.DetonatorEntity;

public class DetonatorItem extends BaseDetonatorItem {
    public DetonatorItem(Block block, Settings settings, int maxIgniteTime) {
        super(block, settings, maxIgniteTime);
    }

    @Override
    protected void spawnDetonator(World world, PlayerEntity player, ItemStack stack, int usedTicks) {
        int remainingTime = maxIgniteTime - usedTicks;
        if (!world.isClient) {
            DetonatorEntity entity = new DetonatorEntity(world, player);
            entity.setItem(stack);
            entity.setIgniteTime(remainingTime);
            entity.setVelocity(player, player.getPitch(), player.getYaw(), 0.5F, usedTicks * 0.05F, 1.0F);
            world.spawnEntity(entity);
        }
    }
}
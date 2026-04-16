package org.oredredging.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.oredredging.entity.DetonatorEntity;

public class DetonatorItem extends BaseDetonatorItem {
    public DetonatorItem(Block block, Properties properties, int maxIgniteTime) {
        super(block, properties, maxIgniteTime);
    }

    @Override
    protected void spawnDetonator(Level level, Player player, ItemStack stack, int usedTicks) {
        int remainingTime = maxIgniteTime - usedTicks;
        if (!level.isClientSide) {
            DetonatorEntity entity = new DetonatorEntity(level, player);
            entity.setItem(stack);
            entity.setIgniteTime(remainingTime);
            entity.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.5F, usedTicks * 0.05F, 1.0F);
            level.addFreshEntity(entity);
        }
    }
}
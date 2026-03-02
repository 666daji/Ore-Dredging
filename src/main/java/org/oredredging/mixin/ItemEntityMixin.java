package org.oredredging.mixin;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import org.oredredging.item.MinerBundleItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemEntity.class)
public class ItemEntityMixin {

    @Redirect(method = "onPlayerCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;insertStack(Lnet/minecraft/item/ItemStack;)Z"))
    private boolean pickItem(PlayerInventory inventory, ItemStack stack) {
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack bag = inventory.getStack(i);
            if (MinerBundleItem.hasAutoPicking(bag)) {
                // 尝试自动收纳
                if (MinerBundleItem.tryAutoPickup(bag, stack, inventory.player)) {
                    stack.setCount(0);
                    return true;
                }
            }
        }

        // 未收纳成功，回退到原背包插入逻辑
        return inventory.insertStack(stack);
    }
}

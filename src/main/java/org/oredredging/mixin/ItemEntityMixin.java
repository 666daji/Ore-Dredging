package org.oredredging.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.oredredging.enchantment.MinerBundleEnchantment;
import org.oredredging.item.MinerBundleItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemEntity.class)
public class ItemEntityMixin {

    @WrapOperation(method = "playerTouch", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;add(Lnet/minecraft/world/item/ItemStack;)Z"))
    private boolean pickItem(Inventory inventory, ItemStack stack, Operation<Boolean> original) {
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack bag = inventory.getItem(i);
            if (MinerBundleItem.hasAutoPicking(bag)) {
                if (MinerBundleEnchantment.tryAutoPickup(bag, stack, inventory.player)) {
                    if (stack.isEmpty()) {
                        // 完全收纳，直接返回成功，不再调用原背包插入逻辑
                        return true;
                    }
                }
            }
        }

        // 仍有剩余，交给原背包插入逻辑
        return original.call(inventory, stack);
    }
}
